package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SpriteIconButton extends Button {
    protected final ResourceLocation sprite;
    protected final int spriteWidth;
    protected final int spriteHeight;

    SpriteIconButton(
        int p_297620_,
        int p_300275_,
        Component p_297544_,
        int p_298263_,
        int p_299223_,
        ResourceLocation p_297738_,
        Button.OnPress p_297736_,
        @Nullable Button.CreateNarration p_335316_
    ) {
        super(0, 0, p_297620_, p_300275_, p_297544_, p_297736_, p_335316_ == null ? DEFAULT_NARRATION : p_335316_);
        this.spriteWidth = p_298263_;
        this.spriteHeight = p_299223_;
        this.sprite = p_297738_;
    }

    public static SpriteIconButton.Builder builder(Component p_299964_, Button.OnPress p_301369_, boolean p_298501_) {
        return new SpriteIconButton.Builder(p_299964_, p_301369_, p_298501_);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Button.OnPress onPress;
        private final boolean iconOnly;
        private int width = 150;
        private int height = 20;
        @Nullable
        private ResourceLocation sprite;
        private int spriteWidth;
        private int spriteHeight;
        @Nullable
        Button.CreateNarration narration;

        public Builder(Component p_298778_, Button.OnPress p_297973_, boolean p_297512_) {
            this.message = p_298778_;
            this.onPress = p_297973_;
            this.iconOnly = p_297512_;
        }

        public SpriteIconButton.Builder width(int p_298805_) {
            this.width = p_298805_;
            return this;
        }

        public SpriteIconButton.Builder size(int p_301312_, int p_297726_) {
            this.width = p_301312_;
            this.height = p_297726_;
            return this;
        }

        public SpriteIconButton.Builder sprite(ResourceLocation p_298738_, int p_301308_, int p_297593_) {
            this.sprite = p_298738_;
            this.spriteWidth = p_301308_;
            this.spriteHeight = p_297593_;
            return this;
        }

        public SpriteIconButton.Builder narration(Button.CreateNarration p_328959_) {
            this.narration = p_328959_;
            return this;
        }

        public SpriteIconButton build() {
            if (this.sprite == null) {
                throw new IllegalStateException("Sprite not set");
            } else {
                return (SpriteIconButton)(this.iconOnly
                    ? new SpriteIconButton.CenteredIcon(
                        this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress, this.narration
                    )
                    : new SpriteIconButton.TextAndIcon(
                        this.width, this.height, this.message, this.spriteWidth, this.spriteHeight, this.sprite, this.onPress, this.narration
                    ));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CenteredIcon extends SpriteIconButton {
        protected CenteredIcon(
            int p_300200_,
            int p_299056_,
            Component p_298209_,
            int p_300001_,
            int p_298255_,
            ResourceLocation p_298326_,
            Button.OnPress p_298485_,
            @Nullable Button.CreateNarration p_328314_
        ) {
            super(p_300200_, p_299056_, p_298209_, p_300001_, p_298255_, p_298326_, p_298485_, p_328314_);
        }

        @Override
        public void renderWidget(GuiGraphics p_299781_, int p_297898_, int p_300476_, float p_300735_) {
            super.renderWidget(p_299781_, p_297898_, p_300476_, p_300735_);
            int i = this.getX() + this.getWidth() / 2 - this.spriteWidth / 2;
            int j = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            p_299781_.blitSprite(RenderType::guiTextured, this.sprite, i, j, this.spriteWidth, this.spriteHeight);
        }

        @Override
        public void renderString(GuiGraphics p_300547_, Font p_301253_, int p_299879_) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextAndIcon extends SpriteIconButton {
        protected TextAndIcon(
            int p_299028_,
            int p_300372_,
            Component p_297448_,
            int p_300274_,
            int p_301370_,
            ResourceLocation p_298996_,
            Button.OnPress p_298623_,
            @Nullable Button.CreateNarration p_328187_
        ) {
            super(p_299028_, p_300372_, p_297448_, p_300274_, p_301370_, p_298996_, p_298623_, p_328187_);
        }

        @Override
        public void renderWidget(GuiGraphics p_299610_, int p_301138_, int p_298092_, float p_300832_) {
            super.renderWidget(p_299610_, p_301138_, p_298092_, p_300832_);
            int i = this.getX() + this.getWidth() - this.spriteWidth - 2;
            int j = this.getY() + this.getHeight() / 2 - this.spriteHeight / 2;
            p_299610_.blitSprite(RenderType::guiTextured, this.sprite, i, j, this.spriteWidth, this.spriteHeight);
        }

        @Override
        public void renderString(GuiGraphics p_297951_, Font p_300566_, int p_298347_) {
            int i = this.getX() + 2;
            int j = this.getX() + this.getWidth() - this.spriteWidth - 4;
            int k = this.getX() + this.getWidth() / 2;
            renderScrollingString(p_297951_, p_300566_, this.getMessage(), k, i, this.getY(), j, this.getY() + this.getHeight(), p_298347_);
        }
    }
}