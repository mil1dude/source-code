package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
    protected static final int TEXT_MARGIN = 2;
    private static final WidgetSprites SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("widget/button"),
        ResourceLocation.withDefaultNamespace("widget/button_disabled"),
        ResourceLocation.withDefaultNamespace("widget/button_highlighted")
    );

    public AbstractButton(int p_93365_, int p_93366_, int p_93367_, int p_93368_, Component p_93369_) {
        super(p_93365_, p_93366_, p_93367_, p_93368_, p_93369_);
    }

    public abstract void onPress();

    @Override
    protected void renderWidget(GuiGraphics p_281670_, int p_282682_, int p_281714_, float p_282542_) {
        Minecraft minecraft = Minecraft.getInstance();
        p_281670_.blitSprite(
            RenderType::guiTextured,
            SPRITES.get(this.active, this.isHoveredOrFocused()),
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            ARGB.white(this.alpha)
        );
        int i = this.active ? 16777215 : 10526880;
        this.renderString(p_281670_, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void renderString(GuiGraphics p_283366_, Font p_283054_, int p_281656_) {
        this.renderScrollingString(p_283366_, p_283054_, 2, p_281656_);
    }

    @Override
    public void onClick(double p_93371_, double p_93372_) {
        this.onPress();
    }

    @Override
    public boolean keyPressed(int p_93374_, int p_93375_, int p_93376_) {
        if (!this.active || !this.visible) {
            return false;
        } else if (CommonInputs.selected(p_93374_)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        } else {
            return false;
        }
    }
}