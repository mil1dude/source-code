package net.minecraft.tags;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedSet;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import org.slf4j.Logger;

public class TagLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final TagLoader.ElementLookup<T> elementLookup;
    private final String directory;

    public TagLoader(TagLoader.ElementLookup<T> p_365781_, String p_144494_) {
        this.elementLookup = p_365781_;
        this.directory = p_144494_;
    }

    public Map<ResourceLocation, List<TagLoader.EntryWithSource>> load(ResourceManager p_144496_) {
        Map<ResourceLocation, List<TagLoader.EntryWithSource>> map = new HashMap<>();
        FileToIdConverter filetoidconverter = FileToIdConverter.json(this.directory);

        for (Entry<ResourceLocation, List<Resource>> entry : filetoidconverter.listMatchingResourceStacks(p_144496_).entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            ResourceLocation resourcelocation1 = filetoidconverter.fileToId(resourcelocation);

            for (Resource resource : entry.getValue()) {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement jsonelement = JsonParser.parseReader(reader);
                    List<TagLoader.EntryWithSource> list = map.computeIfAbsent(resourcelocation1, p_215974_ -> new ArrayList<>());
                    TagFile tagfile = TagFile.CODEC.parse(new Dynamic<>(JsonOps.INSTANCE, jsonelement)).getOrThrow();
                    if (tagfile.replace()) {
                        list.clear();
                    }

                    String s = resource.sourcePackId();
                    tagfile.entries().forEach(p_215997_ -> list.add(new TagLoader.EntryWithSource(p_215997_, s)));
                } catch (Exception exception) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", resourcelocation1, resourcelocation, resource.sourcePackId(), exception);
                }
            }
        }

        return map;
    }

    private Either<List<TagLoader.EntryWithSource>, List<T>> tryBuildTag(TagEntry.Lookup<T> p_215979_, List<TagLoader.EntryWithSource> p_215980_) {
        SequencedSet<T> sequencedset = new LinkedHashSet<>();
        List<TagLoader.EntryWithSource> list = new ArrayList<>();

        for (TagLoader.EntryWithSource tagloader$entrywithsource : p_215980_) {
            if (!tagloader$entrywithsource.entry().build(p_215979_, sequencedset::add)) {
                list.add(tagloader$entrywithsource);
            }
        }

        return list.isEmpty() ? Either.right(List.copyOf(sequencedset)) : Either.left(list);
    }

    public Map<ResourceLocation, List<T>> build(Map<ResourceLocation, List<TagLoader.EntryWithSource>> p_203899_) {
        final Map<ResourceLocation, List<T>> map = new HashMap<>();
        TagEntry.Lookup<T> lookup = new TagEntry.Lookup<T>() {
            @Nullable
            @Override
            public T element(ResourceLocation p_216039_, boolean p_366980_) {
                return (T)TagLoader.this.elementLookup.get(p_216039_, p_366980_).orElse(null);
            }

            @Nullable
            @Override
            public Collection<T> tag(ResourceLocation p_216041_) {
                return map.get(p_216041_);
            }
        };
        DependencySorter<ResourceLocation, TagLoader.SortingEntry> dependencysorter = new DependencySorter<>();
        p_203899_.forEach(
            (p_284685_, p_284686_) -> dependencysorter.addEntry(p_284685_, new TagLoader.SortingEntry((List<TagLoader.EntryWithSource>)p_284686_))
        );
        dependencysorter.orderByDependencies(
            (p_358780_, p_358781_) -> this.tryBuildTag(lookup, p_358781_.entries)
                    .ifLeft(
                        p_358772_ -> LOGGER.error(
                                "Couldn't load tag {} as it is missing following references: {}",
                                p_358780_,
                                p_358772_.stream().map(Objects::toString).collect(Collectors.joining(", "))
                            )
                    )
                    .ifRight(p_369415_ -> map.put(p_358780_, (List<T>)p_369415_))
        );
        return map;
    }

    public static <T> void loadTagsFromNetwork(TagNetworkSerialization.NetworkPayload p_363340_, WritableRegistry<T> p_362274_) {
        p_363340_.resolve(p_362274_).tags.forEach(p_362274_::bindTag);
    }

    public static List<Registry.PendingTags<?>> loadTagsForExistingRegistries(ResourceManager p_363516_, RegistryAccess p_365200_) {
        return p_365200_.registries()
            .map(p_358777_ -> loadPendingTags(p_363516_, p_358777_.value()))
            .flatMap(Optional::stream)
            .collect(Collectors.toUnmodifiableList());
    }

    public static <T> void loadTagsForRegistry(ResourceManager p_361002_, WritableRegistry<T> p_369889_) {
        ResourceKey<? extends Registry<T>> resourcekey = p_369889_.key();
        TagLoader<Holder<T>> tagloader = new TagLoader<>(TagLoader.ElementLookup.fromWritableRegistry(p_369889_), Registries.tagsDirPath(resourcekey));
        tagloader.build(tagloader.load(p_361002_))
            .forEach((p_358786_, p_358787_) -> p_369889_.bindTag(TagKey.create(resourcekey, p_358786_), (List<Holder<T>>)p_358787_));
    }

    private static <T> Map<TagKey<T>, List<Holder<T>>> wrapTags(ResourceKey<? extends Registry<T>> p_369888_, Map<ResourceLocation, List<Holder<T>>> p_362414_) {
        return p_362414_.entrySet()
            .stream()
            .collect(Collectors.toUnmodifiableMap(p_358783_ -> TagKey.create(p_369888_, p_358783_.getKey()), Entry::getValue));
    }

    private static <T> Optional<Registry.PendingTags<T>> loadPendingTags(ResourceManager p_366215_, Registry<T> p_369074_) {
        ResourceKey<? extends Registry<T>> resourcekey = p_369074_.key();
        TagLoader<Holder<T>> tagloader = new TagLoader<>(
            (TagLoader.ElementLookup<Holder<T>>)TagLoader.ElementLookup.fromFrozenRegistry(p_369074_), Registries.tagsDirPath(resourcekey)
        );
        TagLoader.LoadResult<T> loadresult = new TagLoader.LoadResult<>(
            resourcekey, wrapTags(p_369074_.key(), tagloader.build(tagloader.load(p_366215_)))
        );
        return loadresult.tags().isEmpty() ? Optional.empty() : Optional.of(p_369074_.prepareTagReload(loadresult));
    }

    public static List<HolderLookup.RegistryLookup<?>> buildUpdatedLookups(RegistryAccess.Frozen p_361092_, List<Registry.PendingTags<?>> p_361987_) {
        List<HolderLookup.RegistryLookup<?>> list = new ArrayList<>();
        p_361092_.registries().forEach(p_358775_ -> {
            Registry.PendingTags<?> pendingtags = findTagsForRegistry(p_361987_, p_358775_.key());
            list.add((HolderLookup.RegistryLookup<?>)(pendingtags != null ? pendingtags.lookup() : p_358775_.value()));
        });
        return list;
    }

    @Nullable
    private static Registry.PendingTags<?> findTagsForRegistry(List<Registry.PendingTags<?>> p_361794_, ResourceKey<? extends Registry<?>> p_361930_) {
        for (Registry.PendingTags<?> pendingtags : p_361794_) {
            if (pendingtags.key() == p_361930_) {
                return pendingtags;
            }
        }

        return null;
    }

    public interface ElementLookup<T> {
        Optional<? extends T> get(ResourceLocation p_362946_, boolean p_368007_);

        static <T> TagLoader.ElementLookup<? extends Holder<T>> fromFrozenRegistry(Registry<T> p_369869_) {
            return (p_367027_, p_367996_) -> p_369869_.get(p_367027_);
        }

        static <T> TagLoader.ElementLookup<Holder<T>> fromWritableRegistry(WritableRegistry<T> p_361559_) {
            HolderGetter<T> holdergetter = p_361559_.createRegistrationLookup();
            return (p_367634_, p_365243_) -> ((HolderGetter<T>)(p_365243_ ? holdergetter : p_361559_))
                    .get(ResourceKey.create(p_361559_.key(), p_367634_));
        }
    }

    public static record EntryWithSource(TagEntry entry, String source) {
        @Override
        public String toString() {
            return this.entry + " (from " + this.source + ")";
        }
    }

    public static record LoadResult<T>(ResourceKey<? extends Registry<T>> key, Map<TagKey<T>, List<Holder<T>>> tags) {
    }

    static record SortingEntry(List<TagLoader.EntryWithSource> entries) implements DependencySorter.Entry<ResourceLocation> {
        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> p_285529_) {
            this.entries.forEach(p_285236_ -> p_285236_.entry.visitRequiredDependencies(p_285529_));
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> p_285469_) {
            this.entries.forEach(p_284943_ -> p_284943_.entry.visitOptionalDependencies(p_285469_));
        }
    }
}