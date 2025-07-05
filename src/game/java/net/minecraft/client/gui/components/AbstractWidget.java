package net.minecraft.client.gui.components;

import java.time.Duration;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractWidget implements Renderable, GuiEventListener, LayoutElement, NarratableEntry {
    private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5;
    private static final double MIN_SCROLL_PERIOD = 3.0;
    protected int width;
    protected int height;
    private int x;
    private int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    private int tabOrderGroup;
    private boolean focused;
    private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

    public AbstractWidget(int p_93629_, int p_93630_, int p_93631_, int p_93632_, Component p_93633_) {
        this.x = p_93629_;
        this.y = p_93630_;
        this.width = p_93631_;
        this.height = p_93632_;
        this.message = p_93633_;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public final void render(GuiGraphics p_282421_, int p_93658_, int p_93659_, float p_93660_) {
        if (this.visible) {
            this.isHovered = p_282421_.containsPointInScissor(p_93658_, p_93659_)
                && p_93658_ >= this.getX()
                && p_93659_ >= this.getY()
                && p_93658_ < this.getX() + this.width
                && p_93659_ < this.getY() + this.height;
            this.renderWidget(p_282421_, p_93658_, p_93659_, p_93660_);
            this.tooltip.refreshTooltipForNextRenderPass(this.isHovered(), this.isFocused(), this.getRectangle());
        }
    }

    public void setTooltip(@Nullable Tooltip p_259796_) {
        this.tooltip.set(p_259796_);
    }

    @Nullable
    public Tooltip getTooltip() {
        return this.tooltip.get();
    }

    public void setTooltipDelay(Duration p_334848_) {
        this.tooltip.setDelay(p_334848_);
    }

    protected MutableComponent createNarrationMessage() {
        return wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component p_168800_) {
        return Component.translatable("gui.narrate.button", p_168800_);
    }

    protected abstract void renderWidget(GuiGraphics p_282139_, int p_268034_, int p_268009_, float p_268085_);

    protected static void renderScrollingString(
        GuiGraphics p_281620_, Font p_282651_, Component p_281467_, int p_283621_, int p_282084_, int p_283398_, int p_281938_, int p_283471_
    ) {
        renderScrollingString(p_281620_, p_282651_, p_281467_, (p_283621_ + p_283398_) / 2, p_283621_, p_282084_, p_283398_, p_281938_, p_283471_);
    }

    protected static void renderScrollingString(
        GuiGraphics p_298878_, Font p_301352_, Component p_297799_, int p_300294_, int p_297733_, int p_298903_, int p_300338_, int p_297281_, int p_299081_
    ) {
        int i = p_301352_.width(p_297799_);
        int j = (p_298903_ + p_297281_ - 9) / 2 + 1;
        int k = p_300338_ - p_297733_;
        if (i > k) {
            int l = i - k;
            double d0 = (double)Util.getMillis() / 1000.0;
            double d1 = Math.max((double)l * 0.5, 3.0);
            double d2 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
            double d3 = Mth.lerp(d2, 0.0, (double)l);
            p_298878_.enableScissor(p_297733_, p_298903_, p_300338_, p_297281_);
            p_298878_.drawString(p_301352_, p_297799_, p_297733_ - (int)d3, j, p_299081_);
            p_298878_.disableScissor();
        } else {
            int i1 = Mth.clamp(p_300294_, p_297733_ + i / 2, p_300338_ - i / 2);
            p_298878_.drawCenteredString(p_301352_, p_297799_, i1, j, p_299081_);
        }
    }

    protected void renderScrollingString(GuiGraphics p_281857_, Font p_282790_, int p_282664_, int p_282944_) {
        int i = this.getX() + p_282664_;
        int j = this.getX() + this.getWidth() - p_282664_;
        renderScrollingString(p_281857_, p_282790_, this.getMessage(), i, this.getY(), j, this.getY() + this.getHeight(), p_282944_);
    }

    public void onClick(double p_93634_, double p_93635_) {
    }

    public void onRelease(double p_93669_, double p_93670_) {
    }

    protected void onDrag(double p_93636_, double p_93637_, double p_93638_, double p_93639_) {
    }

    @Override
    public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(p_93643_)) {
                boolean flag = this.isMouseOver(p_93641_, p_93642_);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(p_93641_, p_93642_);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double p_93684_, double p_93685_, int p_93686_) {
        if (this.isValidClickButton(p_93686_)) {
            this.onRelease(p_93684_, p_93685_);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int p_93652_) {
        return p_93652_ == 0;
    }

    @Override
    public boolean mouseDragged(double p_93645_, double p_93646_, int p_93647_, double p_93648_, double p_93649_) {
        if (this.isValidClickButton(p_93647_)) {
            this.onDrag(p_93645_, p_93646_, p_93648_, p_93649_);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent p_265640_) {
        if (!this.active || !this.visible) {
            return null;
        } else {
            return !this.isFocused() ? ComponentPath.leaf(this) : null;
        }
    }

    @Override
    public boolean isMouseOver(double p_93672_, double p_93673_) {
        return this.active
            && this.visible
            && p_93672_ >= (double)this.getX()
            && p_93673_ >= (double)this.getY()
            && p_93672_ < (double)this.getRight()
            && p_93673_ < (double)this.getBottom();
    }

    public void playDownSound(SoundManager p_93665_) {
        playButtonClickSound(p_93665_);
    }

    public static void playButtonClickSound(SoundManager p_363924_) {
        p_363924_.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    public void setWidth(int p_93675_) {
        this.width = p_93675_;
    }

    public void setHeight(int p_298443_) {
        this.height = p_298443_;
    }

    public void setAlpha(float p_93651_) {
        this.alpha = p_93651_;
    }

    public void setMessage(Component p_93667_) {
        this.message = p_93667_;
    }

    public Component getMessage() {
        return this.message;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered() || this.isFocused();
    }

    @Override
    public boolean isActive() {
        return this.visible && this.active;
    }

    @Override
    public void setFocused(boolean p_93693_) {
        this.focused = p_93693_;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        } else {
            return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

    @Override
    public final void updateNarration(NarrationElementOutput p_259921_) {
        this.updateWidgetNarration(p_259921_);
        this.tooltip.updateNarration(p_259921_);
    }

    protected abstract void updateWidgetNarration(NarrationElementOutput p_259858_);

    protected void defaultButtonNarrationText(NarrationElementOutput p_168803_) {
        p_168803_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                p_168803_.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
            } else {
                p_168803_.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
            }
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public void setX(int p_254495_) {
        this.x = p_254495_;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setY(int p_253718_) {
        this.y = p_253718_;
    }

    public int getRight() {
        return this.getX() + this.getWidth();
    }

    public int getBottom() {
        return this.getY() + this.getHeight();
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> p_265566_) {
        p_265566_.accept(this);
    }

    public void setSize(int p_312975_, int p_312301_) {
        this.width = p_312975_;
        this.height = p_312301_;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    public void setRectangle(int p_309908_, int p_310169_, int p_312247_, int p_310380_) {
        this.setSize(p_309908_, p_310169_);
        this.setPosition(p_312247_, p_310380_);
    }

    @Override
    public int getTabOrderGroup() {
        return this.tabOrderGroup;
    }

    public void setTabOrderGroup(int p_268123_) {
        this.tabOrderGroup = p_268123_;
    }
}