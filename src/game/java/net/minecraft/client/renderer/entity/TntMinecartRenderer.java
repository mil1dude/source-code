package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntMinecartRenderer extends AbstractMinecartRenderer<MinecartTNT, MinecartTntRenderState> {
    private final BlockRenderDispatcher blockRenderer;

    public TntMinecartRenderer(EntityRendererProvider.Context p_174424_) {
        super(p_174424_, ModelLayers.TNT_MINECART);
        this.blockRenderer = p_174424_.getBlockRenderDispatcher();
    }

    protected void renderMinecartContents(MinecartTntRenderState p_367280_, BlockState p_116146_, PoseStack p_116147_, MultiBufferSource p_116148_, int p_116149_) {
        float f = p_367280_.fuseRemainingInTicks;
        if (f > -1.0F && f < 10.0F) {
            float f1 = 1.0F - f / 10.0F;
            f1 = Mth.clamp(f1, 0.0F, 1.0F);
            f1 *= f1;
            f1 *= f1;
            float f2 = 1.0F + f1 * 0.3F;
            p_116147_.scale(f2, f2, f2);
        }

        renderWhiteSolidBlock(this.blockRenderer, p_116146_, p_116147_, p_116148_, p_116149_, f > -1.0F && (int)f / 5 % 2 == 0);
    }

    public static void renderWhiteSolidBlock(
        BlockRenderDispatcher p_234662_, BlockState p_234663_, PoseStack p_234664_, MultiBufferSource p_234665_, int p_234666_, boolean p_234667_
    ) {
        int i;
        if (p_234667_) {
            i = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        } else {
            i = OverlayTexture.NO_OVERLAY;
        }

        p_234662_.renderSingleBlock(p_234663_, p_234664_, p_234665_, p_234666_, i);
    }

    public MinecartTntRenderState createRenderState() {
        return new MinecartTntRenderState();
    }

    public void extractRenderState(MinecartTNT p_365534_, MinecartTntRenderState p_362573_, float p_365468_) {
        super.extractRenderState(p_365534_, p_362573_, p_365468_);
        p_362573_.fuseRemainingInTicks = p_365534_.getFuse() > -1 ? (float)p_365534_.getFuse() - p_365468_ + 1.0F : -1.0F;
    }
}