package net.minecraft.world.item.equipment.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public record ArmorTrim(Holder<TrimMaterial> material, Holder<TrimPattern> pattern, boolean showInTooltip) implements TooltipProvider {
    public static final Codec<ArmorTrim> CODEC = RecordCodecBuilder.create(
        p_362219_ -> p_362219_.group(
                    TrimMaterial.CODEC.fieldOf("material").forGetter(ArmorTrim::material),
                    TrimPattern.CODEC.fieldOf("pattern").forGetter(ArmorTrim::pattern),
                    Codec.BOOL.optionalFieldOf("show_in_tooltip", Boolean.valueOf(true)).forGetter(p_362564_ -> p_362564_.showInTooltip)
                )
                .apply(p_362219_, ArmorTrim::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorTrim> STREAM_CODEC = StreamCodec.composite(
        TrimMaterial.STREAM_CODEC,
        ArmorTrim::material,
        TrimPattern.STREAM_CODEC,
        ArmorTrim::pattern,
        ByteBufCodecs.BOOL,
        p_367921_ -> p_367921_.showInTooltip,
        ArmorTrim::new
    );
    private static final Component UPGRADE_TITLE = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.upgrade")))
        .withStyle(ChatFormatting.GRAY);

    public ArmorTrim(Holder<TrimMaterial> p_366497_, Holder<TrimPattern> p_360935_) {
        this(p_366497_, p_360935_, true);
    }

    public boolean hasPatternAndMaterial(Holder<TrimPattern> p_366890_, Holder<TrimMaterial> p_361281_) {
        return p_366890_.equals(this.pattern) && p_361281_.equals(this.material);
    }

    @Override
    public void addToTooltip(Item.TooltipContext p_360931_, Consumer<Component> p_367392_, TooltipFlag p_368625_) {
        if (this.showInTooltip) {
            p_367392_.accept(UPGRADE_TITLE);
            p_367392_.accept(CommonComponents.space().append(this.pattern.value().copyWithStyle(this.material)));
            p_367392_.accept(CommonComponents.space().append(this.material.value().description()));
        }
    }

    public ArmorTrim withTooltip(boolean p_362973_) {
        return new ArmorTrim(this.material, this.pattern, p_362973_);
    }
}