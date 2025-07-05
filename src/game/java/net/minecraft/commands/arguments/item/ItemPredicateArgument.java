package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument implements ArgumentType<ItemPredicateArgument.Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo:'bar'}");
    static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(
        p_325619_ -> Component.translatableEscape("argument.item.id.invalid", p_325619_)
    );
    static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType(
        p_325632_ -> Component.translatableEscape("arguments.item.tag.unknown", p_325632_)
    );
    static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType(
        p_325626_ -> Component.translatableEscape("arguments.item.component.unknown", p_325626_)
    );
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType(
        (p_325624_, p_325625_) -> Component.translatableEscape("arguments.item.component.malformed", p_325624_, p_325625_)
    );
    static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType(
        p_325623_ -> Component.translatableEscape("arguments.item.predicate.unknown", p_325623_)
    );
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_PREDICATE = new Dynamic2CommandExceptionType(
        (p_325617_, p_325618_) -> Component.translatableEscape("arguments.item.predicate.malformed", p_325617_, p_325618_)
    );
    private static final ResourceLocation COUNT_ID = ResourceLocation.withDefaultNamespace("count");
    static final Map<ResourceLocation, ItemPredicateArgument.ComponentWrapper> PSEUDO_COMPONENTS = Stream.of(
            new ItemPredicateArgument.ComponentWrapper(
                COUNT_ID, p_325630_ -> true, MinMaxBounds.Ints.CODEC.map(p_325633_ -> p_325622_ -> p_325633_.matches(p_325622_.getCount()))
            )
        )
        .collect(
            Collectors.toUnmodifiableMap(ItemPredicateArgument.ComponentWrapper::id, p_325629_ -> (ItemPredicateArgument.ComponentWrapper)p_325629_)
        );
    static final Map<ResourceLocation, ItemPredicateArgument.PredicateWrapper> PSEUDO_PREDICATES = Stream.of(
            new ItemPredicateArgument.PredicateWrapper(
                COUNT_ID, MinMaxBounds.Ints.CODEC.map(p_325620_ -> p_325628_ -> p_325620_.matches(p_325628_.getCount()))
            )
        )
        .collect(
            Collectors.toUnmodifiableMap(ItemPredicateArgument.PredicateWrapper::id, p_325631_ -> (ItemPredicateArgument.PredicateWrapper)p_325631_)
        );
    private final Grammar<List<Predicate<ItemStack>>> grammarWithContext;

    public ItemPredicateArgument(CommandBuildContext p_235352_) {
        ItemPredicateArgument.Context itempredicateargument$context = new ItemPredicateArgument.Context(p_235352_);
        this.grammarWithContext = ComponentPredicateParser.createGrammar(itempredicateargument$context);
    }

    public static ItemPredicateArgument itemPredicate(CommandBuildContext p_235354_) {
        return new ItemPredicateArgument(p_235354_);
    }

    public ItemPredicateArgument.Result parse(StringReader p_121039_) throws CommandSyntaxException {
        return Util.allOf(this.grammarWithContext.parseForCommands(p_121039_))::test;
    }

    public static ItemPredicateArgument.Result getItemPredicate(CommandContext<CommandSourceStack> p_121041_, String p_121042_) {
        return p_121041_.getArgument(p_121042_, ItemPredicateArgument.Result.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> p_121054_, SuggestionsBuilder p_121055_) {
        return this.grammarWithContext.parseForSuggestions(p_121055_);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static record ComponentWrapper(ResourceLocation id, Predicate<ItemStack> presenceChecker, Decoder<? extends Predicate<ItemStack>> valueChecker) {
        public static <T> ItemPredicateArgument.ComponentWrapper create(
            ImmutableStringReader p_336159_, ResourceLocation p_334103_, DataComponentType<T> p_331569_
        ) throws CommandSyntaxException {
            Codec<T> codec = p_331569_.codec();
            if (codec == null) {
                throw ItemPredicateArgument.ERROR_UNKNOWN_COMPONENT.createWithContext(p_336159_, p_334103_);
            } else {
                return new ItemPredicateArgument.ComponentWrapper(p_334103_, p_327858_ -> p_327858_.has(p_331569_), codec.map(p_335085_ -> p_331446_ -> {
                        T t = p_331446_.get(p_331569_);
                        return Objects.equals(p_335085_, t);
                    }));
            }
        }

        public Predicate<ItemStack> decode(ImmutableStringReader p_333508_, RegistryOps<Tag> p_329031_, Tag p_332091_) throws CommandSyntaxException {
            DataResult<? extends Predicate<ItemStack>> dataresult = this.valueChecker.parse(p_329031_, p_332091_);
            return (Predicate<ItemStack>)dataresult.getOrThrow(
                p_331995_ -> ItemPredicateArgument.ERROR_MALFORMED_COMPONENT.createWithContext(p_333508_, this.id.toString(), p_331995_)
            );
        }
    }

    static class Context
        implements ComponentPredicateParser.Context<Predicate<ItemStack>, ItemPredicateArgument.ComponentWrapper, ItemPredicateArgument.PredicateWrapper> {
        private final HolderLookup.RegistryLookup<Item> items;
        private final HolderLookup.RegistryLookup<DataComponentType<?>> components;
        private final HolderLookup.RegistryLookup<ItemSubPredicate.Type<?>> predicates;
        private final RegistryOps<Tag> registryOps;

        Context(HolderLookup.Provider p_331757_) {
            this.items = p_331757_.lookupOrThrow(Registries.ITEM);
            this.components = p_331757_.lookupOrThrow(Registries.DATA_COMPONENT_TYPE);
            this.predicates = p_331757_.lookupOrThrow(Registries.ITEM_SUB_PREDICATE_TYPE);
            this.registryOps = p_331757_.createSerializationContext(NbtOps.INSTANCE);
        }

        public Predicate<ItemStack> forElementType(ImmutableStringReader p_328916_, ResourceLocation p_333737_) throws CommandSyntaxException {
            Holder.Reference<Item> reference = this.items
                .get(ResourceKey.create(Registries.ITEM, p_333737_))
                .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_ITEM.createWithContext(p_328916_, p_333737_));
            return p_333639_ -> p_333639_.is(reference);
        }

        public Predicate<ItemStack> forTagType(ImmutableStringReader p_332402_, ResourceLocation p_328228_) throws CommandSyntaxException {
            HolderSet<Item> holderset = this.items
                .get(TagKey.create(Registries.ITEM, p_328228_))
                .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_TAG.createWithContext(p_332402_, p_328228_));
            return p_334213_ -> p_334213_.is(holderset);
        }

        public ItemPredicateArgument.ComponentWrapper lookupComponentType(ImmutableStringReader p_329300_, ResourceLocation p_330392_) throws CommandSyntaxException {
            ItemPredicateArgument.ComponentWrapper itempredicateargument$componentwrapper = ItemPredicateArgument.PSEUDO_COMPONENTS.get(p_330392_);
            if (itempredicateargument$componentwrapper != null) {
                return itempredicateargument$componentwrapper;
            } else {
                DataComponentType<?> datacomponenttype = this.components
                    .get(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, p_330392_))
                    .map(Holder::value)
                    .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_COMPONENT.createWithContext(p_329300_, p_330392_));
                return ItemPredicateArgument.ComponentWrapper.create(p_329300_, p_330392_, datacomponenttype);
            }
        }

        public Predicate<ItemStack> createComponentTest(ImmutableStringReader p_330237_, ItemPredicateArgument.ComponentWrapper p_334559_, Tag p_328343_) throws CommandSyntaxException {
            return p_334559_.decode(p_330237_, this.registryOps, p_328343_);
        }

        public Predicate<ItemStack> createComponentTest(ImmutableStringReader p_330923_, ItemPredicateArgument.ComponentWrapper p_336299_) {
            return p_336299_.presenceChecker;
        }

        public ItemPredicateArgument.PredicateWrapper lookupPredicateType(ImmutableStringReader p_330457_, ResourceLocation p_335636_) throws CommandSyntaxException {
            ItemPredicateArgument.PredicateWrapper itempredicateargument$predicatewrapper = ItemPredicateArgument.PSEUDO_PREDICATES.get(p_335636_);
            return itempredicateargument$predicatewrapper != null
                ? itempredicateargument$predicatewrapper
                : this.predicates
                    .get(ResourceKey.create(Registries.ITEM_SUB_PREDICATE_TYPE, p_335636_))
                    .map(ItemPredicateArgument.PredicateWrapper::new)
                    .orElseThrow(() -> ItemPredicateArgument.ERROR_UNKNOWN_PREDICATE.createWithContext(p_330457_, p_335636_));
        }

        public Predicate<ItemStack> createPredicateTest(ImmutableStringReader p_332241_, ItemPredicateArgument.PredicateWrapper p_335982_, Tag p_333667_) throws CommandSyntaxException {
            return p_335982_.decode(p_332241_, this.registryOps, p_333667_);
        }

        @Override
        public Stream<ResourceLocation> listElementTypes() {
            return this.items.listElementIds().map(ResourceKey::location);
        }

        @Override
        public Stream<ResourceLocation> listTagTypes() {
            return this.items.listTagIds().map(TagKey::location);
        }

        @Override
        public Stream<ResourceLocation> listComponentTypes() {
            return Stream.concat(
                ItemPredicateArgument.PSEUDO_COMPONENTS.keySet().stream(),
                this.components.listElements().filter(p_334864_ -> !p_334864_.value().isTransient()).map(p_329470_ -> p_329470_.key().location())
            );
        }

        @Override
        public Stream<ResourceLocation> listPredicateTypes() {
            return Stream.concat(ItemPredicateArgument.PSEUDO_PREDICATES.keySet().stream(), this.predicates.listElementIds().map(ResourceKey::location));
        }

        public Predicate<ItemStack> negate(Predicate<ItemStack> p_328753_) {
            return p_328753_.negate();
        }

        public Predicate<ItemStack> anyOf(List<Predicate<ItemStack>> p_329990_) {
            return Util.anyOf(p_329990_);
        }
    }

    static record PredicateWrapper(ResourceLocation id, Decoder<? extends Predicate<ItemStack>> type) {
        public PredicateWrapper(Holder.Reference<ItemSubPredicate.Type<?>> p_327901_) {
            this(p_327901_.key().location(), p_327901_.value().codec().map(p_330179_ -> p_330179_::matches));
        }

        public Predicate<ItemStack> decode(ImmutableStringReader p_335853_, RegistryOps<Tag> p_335697_, Tag p_330696_) throws CommandSyntaxException {
            DataResult<? extends Predicate<ItemStack>> dataresult = this.type.parse(p_335697_, p_330696_);
            return (Predicate<ItemStack>)dataresult.getOrThrow(
                p_334639_ -> ItemPredicateArgument.ERROR_MALFORMED_PREDICATE.createWithContext(p_335853_, this.id.toString(), p_334639_)
            );
        }
    }

    public interface Result extends Predicate<ItemStack> {
    }
}