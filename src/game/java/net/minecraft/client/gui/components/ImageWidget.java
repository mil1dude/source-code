package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class ImageWidget extends AbstractWidget {
    ImageWidget(int p_275550_, int p_275723_, int p_301266_, int p_297426_) {
        super(p_275550_, p_275723_, p_301266_, p_297426_, CommonComponents.EMPTY);
    }

    public static ImageWidget texture(int p_298293_, int p_301221_, ResourceLocation p_299739_, int p_297694_, int p_300459_) {
        return new ImageWidget.Texture(0, 0, p_298293_, p_301221_, p_299739_, p_297694_, p_300459_);
    }

    public static ImageWidget sprite(int p_299633_, int p_299377_, ResourceLocation p_298615_) {
        return new ImageWidget.Sprite(0, 0, p_299633_, p_299377_, p_298615_);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_275454_) {
    }

    @Override
    public void playDownSound(SoundManager p_297959_) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent p_298071_) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    static class Sprite extends ImageWidget {
        private final ResourceLocation sprite;

        public Sprite(int p_299930_, int p_297218_, int p_298462_, int p_297563_, ResourceLocation p_299269_) {
            super(p_299930_, p_297218_, p_298462_, p_297563_);
            this.sprite = p_299269_;
        }

        @Override
        public void renderWidget(GuiGraphics p_298082_, int p_297761_, int p_298881_, float p_300382_) {
            p_298082_.blitSprite(RenderType::guiTextured, this.sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Texture extends ImageWidget {
        private final ResourceLocation texture;
        private final int textureWidth;
        private final int textureHeight;

        public Texture(int p_299083_, int p_301299_, int p_299901_, int p_299822_, ResourceLocation p_299550_, int p_298841_, int p_297816_) {
            super(p_299083_, p_301299_, p_299901_, p_299822_);
            this.texture = p_299550_;
            this.textureWidth = p_298841_;
            this.textureHeight = p_297816_;
        }

        @Override
        protected void renderWidget(GuiGraphics p_301123_, int p_301197_, int p_299250_, float p_300781_) {
            p_301123_.blit(
                RenderType::guiTextured,
                this.texture,
                this.getX(),
                this.getY(),
                0.0F,
                0.0F,
                this.getWidth(),
                this.getHeight(),
                this.textureWidth,
                this.textureHeight
            );
        }
    }
}