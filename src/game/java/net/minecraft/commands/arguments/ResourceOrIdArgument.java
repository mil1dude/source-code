package net.minecraft.commands.arguments;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceOrIdArgument<T> implements ArgumentType<Holder<T>> {
    private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
    public static final DynamicCommandExceptionType ERROR_FAILED_TO_PARSE = new DynamicCommandExceptionType(
        p_334248_ -> Component.translatableEscape("argument.resource_or_id.failed_to_parse", p_334248_)
    );
    private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(Component.translatable("argument.resource_or_id.invalid"));
    private final HolderLookup.Provider registryLookup;
    private final boolean hasRegistry;
    private final Codec<Holder<T>> codec;

    protected ResourceOrIdArgument(CommandBuildContext p_334973_, ResourceKey<Registry<T>> p_336087_, Codec<Holder<T>> p_332112_) {
        this.registryLookup = p_334973_;
        this.hasRegistry = p_334973_.lookup(p_336087_).isPresent();
        this.codec = p_332112_;
    }

    public static ResourceOrIdArgument.LootTableArgument lootTable(CommandBuildContext p_329328_) {
        return new ResourceOrIdArgument.LootTableArgument(p_329328_);
    }

    public static Holder<LootTable> getLootTable(CommandContext<CommandSourceStack> p_335148_, String p_329251_) throws CommandSyntaxException {
        return getResource(p_335148_, p_329251_);
    }

    public static ResourceOrIdArgument.LootModifierArgument lootModifier(CommandBuildContext p_329720_) {
        return new ResourceOrIdArgument.LootModifierArgument(p_329720_);
    }

    public static Holder<LootItemFunction> getLootModifier(CommandContext<CommandSourceStack> p_334458_, String p_330525_) {
        return getResource(p_334458_, p_330525_);
    }

    public static ResourceOrIdArgument.LootPredicateArgument lootPredicate(CommandBuildContext p_330159_) {
        return new ResourceOrIdArgument.LootPredicateArgument(p_330159_);
    }

    public static Holder<LootItemCondition> getLootPredicate(CommandContext<CommandSourceStack> p_335366_, String p_334649_) {
        return getResource(p_335366_, p_334649_);
    }

    private static <T> Holder<T> getResource(CommandContext<CommandSourceStack> p_328476_, String p_329877_) {
        return p_328476_.getArgument(p_329877_, Holder.class);
    }

    @Nullable
    public Holder<T> parse(StringReader p_330381_) throws CommandSyntaxException {
        Tag tag = parseInlineOrId(p_330381_);
        if (!this.hasRegistry) {
            return null;
        } else {
            RegistryOps<Tag> registryops = this.registryLookup.createSerializationContext(NbtOps.INSTANCE);
            return this.codec.parse(registryops, tag).getOrThrow(p_334690_ -> ERROR_FAILED_TO_PARSE.createWithContext(p_330381_, p_334690_));
        }
    }

    @VisibleForTesting
    static Tag parseInlineOrId(StringReader p_331361_) throws CommandSyntaxException {
        int i = p_331361_.getCursor();
        Tag tag = new TagParser(p_331361_).readValue();
        if (hasConsumedWholeArg(p_331361_)) {
            return tag;
        } else {
            p_331361_.setCursor(i);
            ResourceLocation resourcelocation = ResourceLocation.read(p_331361_);
            if (hasConsumedWholeArg(p_331361_)) {
                return StringTag.valueOf(resourcelocation.toString());
            } else {
                p_331361_.setCursor(i);
                throw ERROR_INVALID.createWithContext(p_331361_);
            }
        }
    }

    private static boolean hasConsumedWholeArg(StringReader p_330624_) {
        return !p_330624_.canRead() || p_330624_.peek() == ' ';
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class LootModifierArgument extends ResourceOrIdArgument<LootItemFunction> {
        protected LootModifierArgument(CommandBuildContext p_333515_) {
            super(p_333515_, Registries.ITEM_MODIFIER, LootItemFunctions.CODEC);
        }
    }

    public static class LootPredicateArgument extends ResourceOrIdArgument<LootItemCondition> {
        protected LootPredicateArgument(CommandBuildContext p_334679_) {
            super(p_334679_, Registries.PREDICATE, LootItemCondition.CODEC);
        }
    }

    public static class LootTableArgument extends ResourceOrIdArgument<LootTable> {
        protected LootTableArgument(CommandBuildContext p_332797_) {
            super(p_332797_, Registries.LOOT_TABLE, LootTable.CODEC);
        }
    }
}