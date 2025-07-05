package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.DependencySorter;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FontManager implements PreparableReloadListener, AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String FONTS_PATH = "fonts.json";
    public static final ResourceLocation MISSING_FONT = ResourceLocation.withDefaultNamespace("missing");
    private static final FileToIdConverter FONT_DEFINITIONS = FileToIdConverter.json("font");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final FontSet missingFontSet;
    private final List<GlyphProvider> providersToClose = new ArrayList<>();
    private final Map<ResourceLocation, FontSet> fontSets = new HashMap<>();
    private final TextureManager textureManager;
    @Nullable
    private volatile FontSet lastFontSetCache;

    public FontManager(TextureManager p_95005_) {
        this.textureManager = p_95005_;
        this.missingFontSet = Util.make(new FontSet(p_95005_, MISSING_FONT), p_325351_ -> p_325351_.reload(List.of(createFallbackProvider()), Set.of()));
    }

    private static GlyphProvider.Conditional createFallbackProvider() {
        return new GlyphProvider.Conditional(new AllMissingGlyphProvider(), FontOption.Filter.ALWAYS_PASS);
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparableReloadListener.PreparationBarrier p_285160_, ResourceManager p_285231_, Executor p_284975_, Executor p_285218_
    ) {
        return this.prepare(p_285231_, p_284975_)
            .thenCompose(p_285160_::wait)
            .thenAcceptAsync(p_357671_ -> this.apply(p_357671_, Profiler.get()), p_285218_);
    }

    private CompletableFuture<FontManager.Preparation> prepare(ResourceManager p_285252_, Executor p_284969_) {
        List<CompletableFuture<FontManager.UnresolvedBuilderBundle>> list = new ArrayList<>();

        for (Entry<ResourceLocation, List<Resource>> entry : FONT_DEFINITIONS.listMatchingResourceStacks(p_285252_).entrySet()) {
            ResourceLocation resourcelocation = FONT_DEFINITIONS.fileToId(entry.getKey());
            list.add(CompletableFuture.supplyAsync(() -> {
                List<Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional>> list1 = loadResourceStack(entry.getValue(), resourcelocation);
                FontManager.UnresolvedBuilderBundle fontmanager$unresolvedbuilderbundle = new FontManager.UnresolvedBuilderBundle(resourcelocation);

                for (Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional> pair : list1) {
                    FontManager.BuilderId fontmanager$builderid = pair.getFirst();
                    FontOption.Filter fontoption$filter = pair.getSecond().filter();
                    pair.getSecond().definition().unpack().ifLeft(p_325337_ -> {
                        CompletableFuture<Optional<GlyphProvider>> completablefuture = this.safeLoad(fontmanager$builderid, p_325337_, p_285252_, p_284969_);
                        fontmanager$unresolvedbuilderbundle.add(fontmanager$builderid, fontoption$filter, completablefuture);
                    }).ifRight(p_325345_ -> fontmanager$unresolvedbuilderbundle.add(fontmanager$builderid, fontoption$filter, p_325345_));
                }

                return fontmanager$unresolvedbuilderbundle;
            }, p_284969_));
        }

        return Util.sequence(list)
            .thenCompose(
                p_325341_ -> {
                    List<CompletableFuture<Optional<GlyphProvider>>> list1 = p_325341_.stream()
                        .flatMap(FontManager.UnresolvedBuilderBundle::listBuilders)
                        .collect(Util.toMutableList());
                    GlyphProvider.Conditional glyphprovider$conditional = createFallbackProvider();
                    list1.add(CompletableFuture.completedFuture(Optional.of(glyphprovider$conditional.provider())));
                    return Util.sequence(list1)
                        .thenCompose(
                            p_284618_ -> {
                                Map<ResourceLocation, List<GlyphProvider.Conditional>> map = this.resolveProviders(p_325341_);
                                CompletableFuture<?>[] completablefuture = map.values()
                                    .stream()
                                    .map(p_284585_ -> CompletableFuture.runAsync(() -> this.finalizeProviderLoading(p_284585_, glyphprovider$conditional), p_284969_))
                                    .toArray(CompletableFuture[]::new);
                                return CompletableFuture.allOf(completablefuture).thenApply(p_284595_ -> {
                                    List<GlyphProvider> list2 = p_284618_.stream().flatMap(Optional::stream).toList();
                                    return new FontManager.Preparation(map, list2);
                                });
                            }
                        );
                }
            );
    }

    private CompletableFuture<Optional<GlyphProvider>> safeLoad(
        FontManager.BuilderId p_285113_, GlyphProviderDefinition.Loader p_286561_, ResourceManager p_285424_, Executor p_285371_
    ) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Optional.of(p_286561_.load(p_285424_));
            } catch (Exception exception) {
                LOGGER.warn("Failed to load builder {}, rejecting", p_285113_, exception);
                return Optional.empty();
            }
        }, p_285371_);
    }

    private Map<ResourceLocation, List<GlyphProvider.Conditional>> resolveProviders(List<FontManager.UnresolvedBuilderBundle> p_285282_) {
        Map<ResourceLocation, List<GlyphProvider.Conditional>> map = new HashMap<>();
        DependencySorter<ResourceLocation, FontManager.UnresolvedBuilderBundle> dependencysorter = new DependencySorter<>();
        p_285282_.forEach(p_284626_ -> dependencysorter.addEntry(p_284626_.fontId, p_284626_));
        dependencysorter.orderByDependencies(
            (p_284620_, p_284621_) -> p_284621_.resolve(map::get).ifPresent(p_284590_ -> map.put(p_284620_, (List<GlyphProvider.Conditional>)p_284590_))
        );
        return map;
    }

    private void finalizeProviderLoading(List<GlyphProvider.Conditional> p_285520_, GlyphProvider.Conditional p_328834_) {
        p_285520_.add(0, p_328834_);
        IntSet intset = new IntOpenHashSet();

        for (GlyphProvider.Conditional glyphprovider$conditional : p_285520_) {
            intset.addAll(glyphprovider$conditional.provider().getSupportedGlyphs());
        }

        intset.forEach(p_325339_ -> {
            if (p_325339_ != 32) {
                for (GlyphProvider.Conditional glyphprovider$conditional1 : Lists.reverse(p_285520_)) {
                    if (glyphprovider$conditional1.provider().getGlyph(p_325339_) != null) {
                        break;
                    }
                }
            }
        });
    }

    private static Set<FontOption> getFontOptions(Options p_331588_) {
        Set<FontOption> set = EnumSet.noneOf(FontOption.class);
        if (p_331588_.forceUnicodeFont().get()) {
            set.add(FontOption.UNIFORM);
        }

        if (p_331588_.japaneseGlyphVariants().get()) {
            set.add(FontOption.JAPANESE_VARIANTS);
        }

        return set;
    }

    private void apply(FontManager.Preparation p_284939_, ProfilerFiller p_285407_) {
        p_285407_.push("closing");
        this.lastFontSetCache = null;
        this.fontSets.values().forEach(FontSet::close);
        this.fontSets.clear();
        this.providersToClose.forEach(GlyphProvider::close);
        this.providersToClose.clear();
        Set<FontOption> set = getFontOptions(Minecraft.getInstance().options);
        p_285407_.popPush("reloading");
        p_284939_.fontSets().forEach((p_325349_, p_325350_) -> {
            FontSet fontset = new FontSet(this.textureManager, p_325349_);
            fontset.reload(Lists.reverse((List<GlyphProvider.Conditional>)p_325350_), set);
            this.fontSets.put(p_325349_, fontset);
        });
        this.providersToClose.addAll(p_284939_.allProviders);
        p_285407_.pop();
        if (!this.fontSets.containsKey(Minecraft.DEFAULT_FONT)) {
            throw new IllegalStateException("Default font failed to load");
        }
    }

    public void updateOptions(Options p_335215_) {
        Set<FontOption> set = getFontOptions(p_335215_);

        for (FontSet fontset : this.fontSets.values()) {
            fontset.reload(set);
        }
    }

    private static List<Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional>> loadResourceStack(List<Resource> p_284976_, ResourceLocation p_285272_) {
        List<Pair<FontManager.BuilderId, GlyphProviderDefinition.Conditional>> list = new ArrayList<>();

        for (Resource resource : p_284976_) {
            try (Reader reader = resource.openAsReader()) {
                JsonElement jsonelement = GSON.fromJson(reader, JsonElement.class);
                FontManager.FontDefinitionFile fontmanager$fontdefinitionfile = FontManager.FontDefinitionFile.CODEC
                    .parse(JsonOps.INSTANCE, jsonelement)
                    .getOrThrow(JsonParseException::new);
                List<GlyphProviderDefinition.Conditional> list1 = fontmanager$fontdefinitionfile.providers;

                for (int i = list1.size() - 1; i >= 0; i--) {
                    FontManager.BuilderId fontmanager$builderid = new FontManager.BuilderId(p_285272_, resource.sourcePackId(), i);
                    list.add(Pair.of(fontmanager$builderid, list1.get(i)));
                }
            } catch (Exception exception) {
                LOGGER.warn("Unable to load font '{}' in {} in resourcepack: '{}'", p_285272_, "fonts.json", resource.sourcePackId(), exception);
            }
        }

        return list;
    }

    public Font createFont() {
        return new Font(this::getFontSetCached, false);
    }

    public Font createFontFilterFishy() {
        return new Font(this::getFontSetCached, true);
    }

    private FontSet getFontSetRaw(ResourceLocation p_333296_) {
        return this.fontSets.getOrDefault(p_333296_, this.missingFontSet);
    }

    private FontSet getFontSetCached(ResourceLocation p_332431_) {
        FontSet fontset = this.lastFontSetCache;
        if (fontset != null && p_332431_.equals(fontset.name())) {
            return fontset;
        } else {
            FontSet fontset1 = this.getFontSetRaw(p_332431_);
            this.lastFontSetCache = fontset1;
            return fontset1;
        }
    }

    @Override
    public void close() {
        this.fontSets.values().forEach(FontSet::close);
        this.providersToClose.forEach(GlyphProvider::close);
        this.missingFontSet.close();
    }

    @OnlyIn(Dist.CLIENT)
    static record BuilderId(ResourceLocation fontId, String pack, int index) {
        @Override
        public String toString() {
            return "(" + this.fontId + ": builder #" + this.index + " from pack " + this.pack + ")";
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record BuilderResult(
        FontManager.BuilderId id, FontOption.Filter filter, Either<CompletableFuture<Optional<GlyphProvider>>, ResourceLocation> result
    ) {
        public Optional<List<GlyphProvider.Conditional>> resolve(Function<ResourceLocation, List<GlyphProvider.Conditional>> p_284942_) {
            return this.result
                .map(
                    p_325356_ -> p_325356_.join().map(p_325357_ -> List.of(new GlyphProvider.Conditional(p_325357_, this.filter))),
                    p_325359_ -> {
                        List<GlyphProvider.Conditional> list = p_284942_.apply(p_325359_);
                        if (list == null) {
                            FontManager.LOGGER
                                .warn(
                                    "Can't find font {} referenced by builder {}, either because it's missing, failed to load or is part of loading cycle",
                                    p_325359_,
                                    this.id
                                );
                            return Optional.empty();
                        } else {
                            return Optional.of(list.stream().map(this::mergeFilters).toList());
                        }
                    }
                );
        }

        private GlyphProvider.Conditional mergeFilters(GlyphProvider.Conditional p_330532_) {
            return new GlyphProvider.Conditional(p_330532_.provider(), this.filter.merge(p_330532_.filter()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record FontDefinitionFile(List<GlyphProviderDefinition.Conditional> providers) {
        public static final Codec<FontManager.FontDefinitionFile> CODEC = RecordCodecBuilder.create(
            p_325360_ -> p_325360_.group(
                        GlyphProviderDefinition.Conditional.CODEC.listOf().fieldOf("providers").forGetter(FontManager.FontDefinitionFile::providers)
                    )
                    .apply(p_325360_, FontManager.FontDefinitionFile::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    static record Preparation(Map<ResourceLocation, List<GlyphProvider.Conditional>> fontSets, List<GlyphProvider> allProviders) {
    }

    @OnlyIn(Dist.CLIENT)
    static record UnresolvedBuilderBundle(ResourceLocation fontId, List<FontManager.BuilderResult> builders, Set<ResourceLocation> dependencies)
        implements DependencySorter.Entry<ResourceLocation> {
        public UnresolvedBuilderBundle(ResourceLocation p_284984_) {
            this(p_284984_, new ArrayList<>(), new HashSet<>());
        }

        public void add(FontManager.BuilderId p_284935_, FontOption.Filter p_336303_, GlyphProviderDefinition.Reference p_334249_) {
            this.builders.add(new FontManager.BuilderResult(p_284935_, p_336303_, Either.right(p_334249_.id())));
            this.dependencies.add(p_334249_.id());
        }

        public void add(FontManager.BuilderId p_286837_, FontOption.Filter p_334374_, CompletableFuture<Optional<GlyphProvider>> p_331945_) {
            this.builders.add(new FontManager.BuilderResult(p_286837_, p_334374_, Either.left(p_331945_)));
        }

        private Stream<CompletableFuture<Optional<GlyphProvider>>> listBuilders() {
            return this.builders.stream().flatMap(p_285041_ -> p_285041_.result.left().stream());
        }

        public Optional<List<GlyphProvider.Conditional>> resolve(Function<ResourceLocation, List<GlyphProvider.Conditional>> p_285118_) {
            List<GlyphProvider.Conditional> list = new ArrayList<>();

            for (FontManager.BuilderResult fontmanager$builderresult : this.builders) {
                Optional<List<GlyphProvider.Conditional>> optional = fontmanager$builderresult.resolve(p_285118_);
                if (!optional.isPresent()) {
                    return Optional.empty();
                }

                list.addAll(optional.get());
            }

            return Optional.of(list);
        }

        @Override
        public void visitRequiredDependencies(Consumer<ResourceLocation> p_285391_) {
            this.dependencies.forEach(p_285391_);
        }

        @Override
        public void visitOptionalDependencies(Consumer<ResourceLocation> p_285405_) {
        }
    }
}