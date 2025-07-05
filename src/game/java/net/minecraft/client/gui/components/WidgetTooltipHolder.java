package net.minecraft.client.gui.components;

import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WidgetTooltipHolder {
    @Nullable
    private Tooltip tooltip;
    private Duration delay = Duration.ZERO;
    private long displayStartTime;
    private boolean wasDisplayed;

    public void setDelay(Duration p_334379_) {
        this.delay = p_334379_;
    }

    public void set(@Nullable Tooltip p_327883_) {
        this.tooltip = p_327883_;
    }

    @Nullable
    public Tooltip get() {
        return this.tooltip;
    }

    public void refreshTooltipForNextRenderPass(boolean p_330612_, boolean p_330175_, ScreenRectangle p_331953_) {
        if (this.tooltip == null) {
            this.wasDisplayed = false;
        } else {
            boolean flag = p_330612_ || p_330175_ && Minecraft.getInstance().getLastInputType().isKeyboard();
            if (flag != this.wasDisplayed) {
                if (flag) {
                    this.displayStartTime = Util.getMillis();
                }

                this.wasDisplayed = flag;
            }

            if (flag && Util.getMillis() - this.displayStartTime > this.delay.toMillis()) {
                Screen screen = Minecraft.getInstance().screen;
                if (screen != null) {
                    screen.setTooltipForNextRenderPass(this.tooltip, this.createTooltipPositioner(p_331953_, p_330612_, p_330175_), p_330175_);
                }
            }
        }
    }

    private ClientTooltipPositioner createTooltipPositioner(ScreenRectangle p_328060_, boolean p_329268_, boolean p_336280_) {
        return (ClientTooltipPositioner)(!p_329268_ && p_336280_ && Minecraft.getInstance().getLastInputType().isKeyboard()
            ? new BelowOrAboveWidgetTooltipPositioner(p_328060_)
            : new MenuTooltipPositioner(p_328060_));
    }

    public void updateNarration(NarrationElementOutput p_329365_) {
        if (this.tooltip != null) {
            this.tooltip.updateNarration(p_329365_);
        }
    }
}