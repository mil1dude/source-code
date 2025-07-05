package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public record Unbreakable(boolean showInTooltip) implements TooltipProvider {
    public static final Codec<Unbreakable> CODEC = RecordCodecBuilder.create(
        p_330402_ -> p_330402_.group(Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(Unbreakable::showInTooltip))
                .apply(p_330402_, Unbreakable::new)
    );
    public static final StreamCodec<ByteBuf, Unbreakable> STREAM_CODEC = ByteBufCodecs.BOOL.map(Unbreakable::new, Unbreakable::showInTooltip);
    private static final Component TOOLTIP = Component.translatable("item.unbreakable").withStyle(ChatFormatting.BLUE);

    @Override
    public void addToTooltip(Item.TooltipContext p_331499_, Consumer<Component> p_335134_, TooltipFlag p_331046_) {
        if (this.showInTooltip) {
            p_335134_.accept(TOOLTIP);
        }
    }

    public Unbreakable withTooltip(boolean p_334168_) {
        return new Unbreakable(p_334168_);
    }
}