package net.minecraft.client.gui.components;

import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSliderButton extends AbstractWidget {
    private static final ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider");
    private static final ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_highlighted");
    private static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");
    private static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted");
    protected static final int TEXT_MARGIN = 2;
    private static final int HANDLE_WIDTH = 8;
    private static final int HANDLE_HALF_WIDTH = 4;
    protected double value;
    private boolean canChangeValue;

    public AbstractSliderButton(int p_93579_, int p_93580_, int p_93581_, int p_93582_, Component p_93583_, double p_93584_) {
        super(p_93579_, p_93580_, p_93581_, p_93582_, p_93583_);
        this.value = p_93584_;
    }

    private ResourceLocation getSprite() {
        return this.isActive() && this.isFocused() && !this.canChangeValue ? HIGHLIGHTED_SPRITE : SLIDER_SPRITE;
    }

    private ResourceLocation getHandleSprite() {
        return !this.isActive() || !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_168798_) {
        p_168798_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                p_168798_.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.focused"));
            } else {
                p_168798_.add(NarratedElementType.USAGE, Component.translatable("narration.slider.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics p_283427_, int p_281447_, int p_282852_, float p_282409_) {
        Minecraft minecraft = Minecraft.getInstance();
        p_283427_.blitSprite(
            RenderType::guiTextured, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha)
        );
        p_283427_.blitSprite(
            RenderType::guiTextured,
            this.getHandleSprite(),
            this.getX() + (int)(this.value * (double)(this.width - 8)),
            this.getY(),
            8,
            this.getHeight(),
            ARGB.white(this.alpha)
        );
        int i = this.active ? 16777215 : 10526880;
        this.renderScrollingString(p_283427_, minecraft.font, 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public void onClick(double p_93588_, double p_93589_) {
        this.setValueFromMouse(p_93588_);
    }

    @Override
    public void setFocused(boolean p_265705_) {
        super.setFocused(p_265705_);
        if (!p_265705_) {
            this.canChangeValue = false;
        } else {
            InputType inputtype = Minecraft.getInstance().getLastInputType();
            if (inputtype == InputType.MOUSE || inputtype == InputType.KEYBOARD_TAB) {
                this.canChangeValue = true;
            }
        }
    }

    @Override
    public boolean keyPressed(int p_93596_, int p_93597_, int p_93598_) {
        if (CommonInputs.selected(p_93596_)) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        } else {
            if (this.canChangeValue) {
                boolean flag = p_93596_ == 263;
                if (flag || p_93596_ == 262) {
                    float f = flag ? -1.0F : 1.0F;
                    this.setValue(this.value + (double)(f / (float)(this.width - 8)));
                    return true;
                }
            }

            return false;
        }
    }

    private void setValueFromMouse(double p_93586_) {
        this.setValue((p_93586_ - (double)(this.getX() + 4)) / (double)(this.width - 8));
    }

    private void setValue(double p_93612_) {
        double d0 = this.value;
        this.value = Mth.clamp(p_93612_, 0.0, 1.0);
        if (d0 != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    @Override
    protected void onDrag(double p_93591_, double p_93592_, double p_93593_, double p_93594_) {
        this.setValueFromMouse(p_93591_);
        super.onDrag(p_93591_, p_93592_, p_93593_, p_93594_);
    }

    @Override
    public void playDownSound(SoundManager p_93605_) {
    }

    @Override
    public void onRelease(double p_93609_, double p_93610_) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}