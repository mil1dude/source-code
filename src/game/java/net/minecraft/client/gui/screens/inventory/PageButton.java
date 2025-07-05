package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PageButton extends Button {
    private static final ResourceLocation PAGE_FORWARD_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted");
    private static final ResourceLocation PAGE_FORWARD_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_forward");
    private static final ResourceLocation PAGE_BACKWARD_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted");
    private static final ResourceLocation PAGE_BACKWARD_SPRITE = ResourceLocation.withDefaultNamespace("widget/page_backward");
    private final boolean isForward;
    private final boolean playTurnSound;

    public PageButton(int p_99225_, int p_99226_, boolean p_99227_, Button.OnPress p_99228_, boolean p_99229_) {
        super(p_99225_, p_99226_, 23, 13, CommonComponents.EMPTY, p_99228_, DEFAULT_NARRATION);
        this.isForward = p_99227_;
        this.playTurnSound = p_99229_;
    }

    @Override
    public void renderWidget(GuiGraphics p_283468_, int p_282922_, int p_283637_, float p_282459_) {
        ResourceLocation resourcelocation;
        if (this.isForward) {
            resourcelocation = this.isHoveredOrFocused() ? PAGE_FORWARD_HIGHLIGHTED_SPRITE : PAGE_FORWARD_SPRITE;
        } else {
            resourcelocation = this.isHoveredOrFocused() ? PAGE_BACKWARD_HIGHLIGHTED_SPRITE : PAGE_BACKWARD_SPRITE;
        }

        p_283468_.blitSprite(RenderType::guiTextured, resourcelocation, this.getX(), this.getY(), 23, 13);
    }

    @Override
    public void playDownSound(SoundManager p_99231_) {
        if (this.playTurnSound) {
            p_99231_.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }
}