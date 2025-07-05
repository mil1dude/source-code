package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public class AdventureModePredicate {
    private static final Codec<AdventureModePredicate> SIMPLE_CODEC = BlockPredicate.CODEC
        .flatComapMap(p_334782_ -> new AdventureModePredicate(List.of(p_334782_), true), p_329449_ -> DataResult.error(() -> "Cannot encode"));
    private static final Codec<AdventureModePredicate> FULL_CODEC = RecordCodecBuilder.create(
        p_329746_ -> p_329746_.group(
                    ExtraCodecs.nonEmptyList(BlockPredicate.CODEC.listOf()).fieldOf("predicates").forGetter(p_329117_ -> p_329117_.predicates),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(AdventureModePredicate::showInTooltip)
                )
                .apply(p_329746_, AdventureModePredicate::new)
    );
    public static final Codec<AdventureModePredicate> CODEC = Codec.withAlternative(FULL_CODEC, SIMPLE_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, AdventureModePredicate> STREAM_CODEC = StreamCodec.composite(
        BlockPredicate.STREAM_CODEC.apply(ByteBufCodecs.list()),
        p_333442_ -> p_333442_.predicates,
        ByteBufCodecs.BOOL,
        AdventureModePredicate::showInTooltip,
        AdventureModePredicate::new
    );
    public static final Component CAN_BREAK_HEADER = Component.translatable("item.canBreak").withStyle(ChatFormatting.GRAY);
    public static final Component CAN_PLACE_HEADER = Component.translatable("item.canPlace").withStyle(ChatFormatting.GRAY);
    private static final Component UNKNOWN_USE = Component.translatable("item.canUse.unknown").withStyle(ChatFormatting.GRAY);
    private final List<BlockPredicate> predicates;
    private final boolean showInTooltip;
    @Nullable
    private List<Component> cachedTooltip;
    @Nullable
    private BlockInWorld lastCheckedBlock;
    private boolean lastResult;
    private boolean checksBlockEntity;

    public AdventureModePredicate(List<BlockPredicate> p_336068_, boolean p_330877_) {
        this.predicates = p_336068_;
        this.showInTooltip = p_330877_;
    }

    private static boolean areSameBlocks(BlockInWorld p_330769_, @Nullable BlockInWorld p_330025_, boolean p_331117_) {
        if (p_330025_ == null || p_330769_.getState() != p_330025_.getState()) {
            return false;
        } else if (!p_331117_) {
            return true;
        } else if (p_330769_.getEntity() == null && p_330025_.getEntity() == null) {
            return true;
        } else if (p_330769_.getEntity() != null && p_330025_.getEntity() != null) {
            RegistryAccess registryaccess = p_330769_.getLevel().registryAccess();
            return Objects.equals(p_330769_.getEntity().saveWithId(registryaccess), p_330025_.getEntity().saveWithId(registryaccess));
        } else {
            return false;
        }
    }

    public boolean test(BlockInWorld p_333716_) {
        if (areSameBlocks(p_333716_, this.lastCheckedBlock, this.checksBlockEntity)) {
            return this.lastResult;
        } else {
            this.lastCheckedBlock = p_333716_;
            this.checksBlockEntity = false;

            for (BlockPredicate blockpredicate : this.predicates) {
                if (blockpredicate.matches(p_333716_)) {
                    this.checksBlockEntity = this.checksBlockEntity | blockpredicate.requiresNbt();
                    this.lastResult = true;
                    return true;
                }
            }

            this.lastResult = false;
            return false;
        }
    }

    private List<Component> tooltip() {
        if (this.cachedTooltip == null) {
            this.cachedTooltip = computeTooltip(this.predicates);
        }

        return this.cachedTooltip;
    }

    public void addToTooltip(Consumer<Component> p_334654_) {
        this.tooltip().forEach(p_334654_);
    }

    public AdventureModePredicate withTooltip(boolean p_335029_) {
        return new AdventureModePredicate(this.predicates, p_335029_);
    }

    private static List<Component> computeTooltip(List<BlockPredicate> p_328947_) {
        for (BlockPredicate blockpredicate : p_328947_) {
            if (blockpredicate.blocks().isEmpty()) {
                return List.of(UNKNOWN_USE);
            }
        }

        return p_328947_.stream()
            .flatMap(p_333785_ -> p_333785_.blocks().orElseThrow().stream())
            .distinct()
            .map(p_335858_ -> (Component)p_335858_.value().getName().withStyle(ChatFormatting.DARK_GRAY))
            .toList();
    }

    public boolean showInTooltip() {
        return this.showInTooltip;
    }

    @Override
    public boolean equals(Object p_331232_) {
        if (this == p_331232_) {
            return true;
        } else {
            return !(p_331232_ instanceof AdventureModePredicate adventuremodepredicate)
                ? false
                : this.predicates.equals(adventuremodepredicate.predicates) && this.showInTooltip == adventuremodepredicate.showInTooltip;
        }
    }

    @Override
    public int hashCode() {
        return this.predicates.hashCode() * 31 + (this.showInTooltip ? 1 : 0);
    }

    @Override
    public String toString() {
        return "AdventureModePredicate{predicates=" + this.predicates + ", showInTooltip=" + this.showInTooltip + "}";
    }
}
