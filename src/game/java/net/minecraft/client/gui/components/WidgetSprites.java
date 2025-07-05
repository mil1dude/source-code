package net.minecraft.client.gui.components;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WidgetSprites(ResourceLocation enabled, ResourceLocation disabled, ResourceLocation enabledFocused, ResourceLocation disabledFocused) {
    public WidgetSprites(ResourceLocation p_301376_, ResourceLocation p_300804_) {
        this(p_301376_, p_301376_, p_300804_, p_300804_);
    }

    public WidgetSprites(ResourceLocation p_299833_, ResourceLocation p_299508_, ResourceLocation p_300439_) {
        this(p_299833_, p_299508_, p_300439_, p_299508_);
    }

    public ResourceLocation get(boolean p_299771_, boolean p_299716_) {
        if (p_299771_) {
            return p_299716_ ? this.enabledFocused : this.enabled;
        } else {
            return p_299716_ ? this.disabledFocused : this.disabled;
        }
    }
}