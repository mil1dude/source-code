package net.minecraft.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FocusableTextWidget extends MultiLineTextWidget {
    private static final int DEFAULT_PADDING = 4;
    private final boolean alwaysShowBorder;
    private final int padding;

    public FocusableTextWidget(int p_298289_, Component p_300031_, Font p_298235_) {
        this(p_298289_, p_300031_, p_298235_, 4);
    }

    public FocusableTextWidget(int p_335481_, Component p_335339_, Font p_328204_, int p_334529_) {
        this(p_335481_, p_335339_, p_328204_, true, p_334529_);
    }

    public FocusableTextWidget(int p_299147_, Component p_299786_, Font p_299475_, boolean p_299140_, int p_335803_) {
        super(p_299786_, p_299475_);
        this.setMaxWidth(p_299147_);
        this.setCentered(true);
        this.active = true;
        this.alwaysShowBorder = p_299140_;
        this.padding = p_335803_;
    }

    public void containWithin(int p_328277_) {
        this.setMaxWidth(p_328277_ - this.padding * 4);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_300724_) {
        p_300724_.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void renderWidget(GuiGraphics p_297672_, int p_301298_, int p_300386_, float p_299545_) {
        if (this.isFocused() || this.alwaysShowBorder) {
            int i = this.getX() - this.padding;
            int j = this.getY() - this.padding;
            int k = this.getWidth() + this.padding * 2;
            int l = this.getHeight() + this.padding * 2;
            int i1 = this.alwaysShowBorder ? (this.isFocused() ? -1 : -6250336) : -1;
            p_297672_.fill(i + 1, j, i + k, j + l, -16777216);
            p_297672_.renderOutline(i, j, k, l, i1);
        }

        super.renderWidget(p_297672_, p_301298_, p_300386_, p_299545_);
    }

    @Override
    public void playDownSound(SoundManager p_297351_) {
    }
}