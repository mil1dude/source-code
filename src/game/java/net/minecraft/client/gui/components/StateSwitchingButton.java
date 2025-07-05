package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StateSwitchingButton extends AbstractWidget {
    @Nullable
    protected WidgetSprites sprites;
    protected boolean isStateTriggered;

    public StateSwitchingButton(int p_94615_, int p_94616_, int p_94617_, int p_94618_, boolean p_94619_) {
        super(p_94615_, p_94616_, p_94617_, p_94618_, CommonComponents.EMPTY);
        this.isStateTriggered = p_94619_;
    }

    public void initTextureValues(WidgetSprites p_297788_) {
        this.sprites = p_297788_;
    }

    public void setStateTriggered(boolean p_94636_) {
        this.isStateTriggered = p_94636_;
    }

    public boolean isStateTriggered() {
        return this.isStateTriggered;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_259073_) {
        this.defaultButtonNarrationText(p_259073_);
    }

    @Override
    public void renderWidget(GuiGraphics p_283051_, int p_283010_, int p_281379_, float p_283453_) {
        if (this.sprites != null) {
            p_283051_.blitSprite(
                RenderType::guiTextured,
                this.sprites.get(this.isStateTriggered, this.isHoveredOrFocused()),
                this.getX(),
                this.getY(),
                this.width,
                this.height
            );
        }
    }
}