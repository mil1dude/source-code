package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractScrollArea extends AbstractWidget {
    public static final int SCROLLBAR_WIDTH = 6;
    private double scrollAmount;
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
    private boolean scrolling;

    public AbstractScrollArea(int p_377709_, int p_378471_, int p_377440_, int p_376831_, Component p_375489_) {
        super(p_377709_, p_378471_, p_377440_, p_376831_, p_375489_);
    }

    @Override
    public boolean mouseScrolled(double p_377900_, double p_377972_, double p_376192_, double p_378419_) {
        if (!this.visible) {
            return false;
        } else {
            this.setScrollAmount(this.scrollAmount() - p_378419_ * this.scrollRate());
            return true;
        }
    }

    @Override
    public boolean mouseDragged(double p_378500_, double p_377082_, int p_376241_, double p_375440_, double p_376263_) {
        if (this.scrolling) {
            if (p_377082_ < (double)this.getY()) {
                this.setScrollAmount(0.0);
            } else if (p_377082_ > (double)this.getBottom()) {
                this.setScrollAmount((double)this.maxScrollAmount());
            } else {
                double d0 = (double)Math.max(1, this.maxScrollAmount());
                int i = this.scrollerHeight();
                double d1 = Math.max(1.0, d0 / (double)(this.height - i));
                this.setScrollAmount(this.scrollAmount() + p_376263_ * d1);
            }

            return true;
        } else {
            return super.mouseDragged(p_378500_, p_377082_, p_376241_, p_375440_, p_376263_);
        }
    }

    @Override
    public void onRelease(double p_376131_, double p_375870_) {
        this.scrolling = false;
    }

    public double scrollAmount() {
        return this.scrollAmount;
    }

    public void setScrollAmount(double p_378348_) {
        this.scrollAmount = Mth.clamp(p_378348_, 0.0, (double)this.maxScrollAmount());
    }

    public boolean updateScrolling(double p_377950_, double p_377713_, int p_377339_) {
        this.scrolling = this.scrollbarVisible()
            && this.isValidClickButton(p_377339_)
            && p_377950_ >= (double)this.scrollBarX()
            && p_377950_ <= (double)(this.scrollBarX() + 6)
            && p_377713_ >= (double)this.getY()
            && p_377713_ < (double)this.getBottom();
        return this.scrolling;
    }

    public void refreshScrollAmount() {
        this.setScrollAmount(this.scrollAmount);
    }

    public int maxScrollAmount() {
        return Math.max(0, this.contentHeight() - this.height);
    }

    protected boolean scrollbarVisible() {
        return this.maxScrollAmount() > 0;
    }

    protected int scrollerHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.contentHeight()), 32, this.height - 8);
    }

    protected int scrollBarX() {
        return this.getRight() - 6;
    }

    protected int scrollBarY() {
        return Math.max(this.getY(), (int)this.scrollAmount * (this.height - this.scrollerHeight()) / this.maxScrollAmount() + this.getY());
    }

    protected void renderScrollbar(GuiGraphics p_376117_) {
        if (this.scrollbarVisible()) {
            int i = this.scrollBarX();
            int j = this.scrollerHeight();
            int k = this.scrollBarY();
            p_376117_.blitSprite(RenderType::guiTextured, SCROLLER_BACKGROUND_SPRITE, i, this.getY(), 6, this.getHeight());
            p_376117_.blitSprite(RenderType::guiTextured, SCROLLER_SPRITE, i, k, 6, j);
        }
    }

    protected abstract int contentHeight();

    protected abstract double scrollRate();
}