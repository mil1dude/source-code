package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TheEndGatewayRenderer extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
    private static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_gateway_beam.png");

    public TheEndGatewayRenderer(BlockEntityRendererProvider.Context p_173683_) {
        super(p_173683_);
    }

    public void render(TheEndGatewayBlockEntity p_112613_, float p_112614_, PoseStack p_112615_, MultiBufferSource p_112616_, int p_112617_, int p_112618_) {
        if (p_112613_.isSpawning() || p_112613_.isCoolingDown()) {
            float f = p_112613_.isSpawning() ? p_112613_.getSpawnPercent(p_112614_) : p_112613_.getCooldownPercent(p_112614_);
            double d0 = p_112613_.isSpawning() ? (double)p_112613_.getLevel().getMaxY() : 50.0;
            f = Mth.sin(f * (float) Math.PI);
            int i = Mth.floor((double)f * d0);
            int j = p_112613_.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColor() : DyeColor.PURPLE.getTextureDiffuseColor();
            long k = p_112613_.getLevel().getGameTime();
            BeaconRenderer.renderBeaconBeam(p_112615_, p_112616_, BEAM_LOCATION, p_112614_, f, k, -i, i * 2, j, 0.15F, 0.175F);
        }

        super.render(p_112613_, p_112614_, p_112615_, p_112616_, p_112617_, p_112618_);
    }

    @Override
    protected float getOffsetUp() {
        return 1.0F;
    }

    @Override
    protected float getOffsetDown() {
        return 0.0F;
    }

    @Override
    protected RenderType renderType() {
        return RenderType.endGateway();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }
}