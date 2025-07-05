package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerWidget extends AbstractScrollArea implements ContainerEventHandler {
    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    public AbstractContainerWidget(int p_310492_, int p_309402_, int p_313085_, int p_312513_, Component p_310986_) {
        super(p_310492_, p_309402_, p_313085_, p_312513_, p_310986_);
    }

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }

    @Override
    public final void setDragging(boolean p_311596_) {
        this.isDragging = p_311596_;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener p_312828_) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (p_312828_ != null) {
            p_312828_.setFocused(true);
        }

        this.focused = p_312828_;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent p_311207_) {
        return ContainerEventHandler.super.nextFocusPath(p_311207_);
    }

    @Override
    public boolean mouseClicked(double p_312130_, double p_311814_, int p_312053_) {
        boolean flag = this.updateScrolling(p_312130_, p_311814_, p_312053_);
        return ContainerEventHandler.super.mouseClicked(p_312130_, p_311814_, p_312053_) || flag;
    }

    @Override
    public boolean mouseReleased(double p_311513_, double p_312630_, int p_310317_) {
        super.mouseReleased(p_311513_, p_312630_, p_310317_);
        return ContainerEventHandler.super.mouseReleased(p_311513_, p_312630_, p_310317_);
    }

    @Override
    public boolean mouseDragged(double p_310748_, double p_313111_, int p_309710_, double p_312859_, double p_310378_) {
        super.mouseDragged(p_310748_, p_313111_, p_309710_, p_312859_, p_310378_);
        return ContainerEventHandler.super.mouseDragged(p_310748_, p_313111_, p_309710_, p_312859_, p_310378_);
    }

    @Override
    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    @Override
    public void setFocused(boolean p_310891_) {
        ContainerEventHandler.super.setFocused(p_310891_);
    }
}