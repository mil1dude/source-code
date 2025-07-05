package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public record JukeboxPlayable(EitherHolder<JukeboxSong> song, boolean showInTooltip) implements TooltipProvider {
    public static final Codec<JukeboxPlayable> CODEC = RecordCodecBuilder.create(
        p_342357_ -> p_342357_.group(
                    EitherHolder.codec(Registries.JUKEBOX_SONG, JukeboxSong.CODEC).fieldOf("song").forGetter(JukeboxPlayable::song),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(JukeboxPlayable::showInTooltip)
                )
                .apply(p_342357_, JukeboxPlayable::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, JukeboxPlayable> STREAM_CODEC = StreamCodec.composite(
        EitherHolder.streamCodec(Registries.JUKEBOX_SONG, JukeboxSong.STREAM_CODEC),
        JukeboxPlayable::song,
        ByteBufCodecs.BOOL,
        JukeboxPlayable::showInTooltip,
        JukeboxPlayable::new
    );

    @Override
    public void addToTooltip(Item.TooltipContext p_343529_, Consumer<Component> p_344027_, TooltipFlag p_344530_) {
        HolderLookup.Provider holderlookup$provider = p_343529_.registries();
        if (this.showInTooltip && holderlookup$provider != null) {
            this.song.unwrap(holderlookup$provider).ifPresent(p_343443_ -> {
                MutableComponent mutablecomponent = p_343443_.value().description().copy();
                ComponentUtils.mergeStyles(mutablecomponent, Style.EMPTY.withColor(ChatFormatting.GRAY));
                p_344027_.accept(mutablecomponent);
            });
        }
    }

    public JukeboxPlayable withTooltip(boolean p_343241_) {
        return new JukeboxPlayable(this.song, p_343241_);
    }

    public static InteractionResult tryInsertIntoJukebox(Level p_342790_, BlockPos p_344904_, ItemStack p_345065_, Player p_342036_) {
        JukeboxPlayable jukeboxplayable = p_345065_.get(DataComponents.JUKEBOX_PLAYABLE);
        if (jukeboxplayable == null) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        } else {
            BlockState blockstate = p_342790_.getBlockState(p_344904_);
            if (blockstate.is(Blocks.JUKEBOX) && !blockstate.getValue(JukeboxBlock.HAS_RECORD)) {
                if (!p_342790_.isClientSide) {
                    ItemStack itemstack = p_345065_.consumeAndReturn(1, p_342036_);
                    if (p_342790_.getBlockEntity(p_344904_) instanceof JukeboxBlockEntity jukeboxblockentity) {
                        jukeboxblockentity.setTheItem(itemstack);
                        p_342790_.gameEvent(GameEvent.BLOCK_CHANGE, p_344904_, GameEvent.Context.of(p_342036_, blockstate));
                    }

                    p_342036_.awardStat(Stats.PLAY_RECORD);
                }

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
        }
    }
}