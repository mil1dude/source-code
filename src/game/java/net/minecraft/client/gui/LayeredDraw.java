package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.DeltaTracker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayeredDraw {
    public static final float Z_SEPARATION = 200.0F;
    private final List<LayeredDraw.Layer> layers = new ArrayList<>();

    public LayeredDraw add(LayeredDraw.Layer p_332264_) {
        this.layers.add(p_332264_);
        return this;
    }

    public LayeredDraw add(LayeredDraw p_328749_, BooleanSupplier p_332055_) {
        return this.add((p_340772_, p_340773_) -> {
            if (p_332055_.getAsBoolean()) {
                p_328749_.renderInner(p_340772_, p_340773_);
            }
        });
    }

    public void render(GuiGraphics p_335429_, DeltaTracker p_342665_) {
        p_335429_.pose().pushPose();
        this.renderInner(p_335429_, p_342665_);
        p_335429_.pose().popPose();
    }

    private void renderInner(GuiGraphics p_333655_, DeltaTracker p_345190_) {
        for (LayeredDraw.Layer layereddraw$layer : this.layers) {
            layereddraw$layer.render(p_333655_, p_345190_);
            p_333655_.pose().translate(0.0F, 0.0F, 200.0F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface Layer {
        void render(GuiGraphics p_328217_, DeltaTracker p_344084_);
    }
}