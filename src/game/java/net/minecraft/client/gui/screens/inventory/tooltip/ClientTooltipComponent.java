package net.minecraft.client.gui.screens.inventory.tooltip;

import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public interface ClientTooltipComponent {
    static ClientTooltipComponent create(FormattedCharSequence p_169949_) {
        return new ClientTextTooltip(p_169949_);
    }

    static ClientTooltipComponent create(TooltipComponent p_169951_) {
        Objects.requireNonNull(p_169951_);

        return (ClientTooltipComponent)(switch (p_169951_) {
            case BundleTooltip bundletooltip -> new ClientBundleTooltip(bundletooltip.contents());
            case ClientActivePlayersTooltip.ActivePlayersTooltip clientactiveplayerstooltip$activeplayerstooltip -> new ClientActivePlayersTooltip(
            clientactiveplayerstooltip$activeplayerstooltip
        );
            default -> throw new IllegalArgumentException("Unknown TooltipComponent");
        });
    }

    int getHeight(Font p_361819_);

    int getWidth(Font p_169952_);

    default boolean showTooltipWithItemInHand() {
        return false;
    }

    default void renderText(Font p_169953_, int p_169954_, int p_169955_, Matrix4f p_253692_, MultiBufferSource.BufferSource p_169957_) {
    }

    default void renderImage(Font p_194048_, int p_194049_, int p_194050_, int p_362269_, int p_363564_, GuiGraphics p_283459_) {
    }
}