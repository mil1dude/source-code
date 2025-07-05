package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ScreenEffectRenderer {
    private static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");

    public static void renderScreenEffect(Minecraft p_110719_, PoseStack p_110720_, MultiBufferSource p_378238_) {
        Player player = p_110719_.player;
        if (!player.noPhysics) {
            BlockState blockstate = getViewBlockingState(player);
            if (blockstate != null) {
                renderTex(p_110719_.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockstate), p_110720_, p_378238_);
            }
        }

        if (!p_110719_.player.isSpectator()) {
            if (p_110719_.player.isEyeInFluid(FluidTags.WATER)) {
                renderWater(p_110719_, p_110720_, p_378238_);
            }

            if (p_110719_.player.isOnFire()) {
                renderFire(p_110720_, p_378238_);
            }
        }
    }

    @Nullable
    private static BlockState getViewBlockingState(Player p_110717_) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 8; i++) {
            double d0 = p_110717_.getX() + (double)(((float)((i >> 0) % 2) - 0.5F) * p_110717_.getBbWidth() * 0.8F);
            double d1 = p_110717_.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5F) * 0.1F * p_110717_.getScale());
            double d2 = p_110717_.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * p_110717_.getBbWidth() * 0.8F);
            blockpos$mutableblockpos.set(d0, d1, d2);
            BlockState blockstate = p_110717_.level().getBlockState(blockpos$mutableblockpos);
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE && blockstate.isViewBlocking(p_110717_.level(), blockpos$mutableblockpos)) {
                return blockstate;
            }
        }

        return null;
    }

    private static void renderTex(TextureAtlasSprite p_173297_, PoseStack p_173298_, MultiBufferSource p_376984_) {
        float f = 0.1F;
        int i = ARGB.colorFromFloat(1.0F, 0.1F, 0.1F, 0.1F);
        float f1 = -1.0F;
        float f2 = 1.0F;
        float f3 = -1.0F;
        float f4 = 1.0F;
        float f5 = -0.5F;
        float f6 = p_173297_.getU0();
        float f7 = p_173297_.getU1();
        float f8 = p_173297_.getV0();
        float f9 = p_173297_.getV1();
        Matrix4f matrix4f = p_173298_.last().pose();
        VertexConsumer vertexconsumer = p_376984_.getBuffer(RenderType.blockScreenEffect(p_173297_.atlasLocation()));
        vertexconsumer.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(f7, f9).setColor(i);
        vertexconsumer.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(f6, f9).setColor(i);
        vertexconsumer.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(f6, f8).setColor(i);
        vertexconsumer.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(f7, f8).setColor(i);
    }

    private static void renderWater(Minecraft p_110726_, PoseStack p_110727_, MultiBufferSource p_376402_) {
        BlockPos blockpos = BlockPos.containing(p_110726_.player.getX(), p_110726_.player.getEyeY(), p_110726_.player.getZ());
        float f = LightTexture.getBrightness(p_110726_.player.level().dimensionType(), p_110726_.player.level().getMaxLocalRawBrightness(blockpos));
        int i = ARGB.colorFromFloat(0.1F, f, f, f);
        float f1 = 4.0F;
        float f2 = -1.0F;
        float f3 = 1.0F;
        float f4 = -1.0F;
        float f5 = 1.0F;
        float f6 = -0.5F;
        float f7 = -p_110726_.player.getYRot() / 64.0F;
        float f8 = p_110726_.player.getXRot() / 64.0F;
        Matrix4f matrix4f = p_110727_.last().pose();
        VertexConsumer vertexconsumer = p_376402_.getBuffer(RenderType.blockScreenEffect(UNDERWATER_LOCATION));
        vertexconsumer.addVertex(matrix4f, -1.0F, -1.0F, -0.5F).setUv(4.0F + f7, 4.0F + f8).setColor(i);
        vertexconsumer.addVertex(matrix4f, 1.0F, -1.0F, -0.5F).setUv(0.0F + f7, 4.0F + f8).setColor(i);
        vertexconsumer.addVertex(matrix4f, 1.0F, 1.0F, -0.5F).setUv(0.0F + f7, 0.0F + f8).setColor(i);
        vertexconsumer.addVertex(matrix4f, -1.0F, 1.0F, -0.5F).setUv(4.0F + f7, 0.0F + f8).setColor(i);
    }

    private static void renderFire(PoseStack p_110730_, MultiBufferSource p_376973_) {
        TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_1.sprite();
        VertexConsumer vertexconsumer = p_376973_.getBuffer(RenderType.fireScreenEffect(textureatlassprite.atlasLocation()));
        float f = textureatlassprite.getU0();
        float f1 = textureatlassprite.getU1();
        float f2 = (f + f1) / 2.0F;
        float f3 = textureatlassprite.getV0();
        float f4 = textureatlassprite.getV1();
        float f5 = (f3 + f4) / 2.0F;
        float f6 = textureatlassprite.uvShrinkRatio();
        float f7 = Mth.lerp(f6, f, f2);
        float f8 = Mth.lerp(f6, f1, f2);
        float f9 = Mth.lerp(f6, f3, f5);
        float f10 = Mth.lerp(f6, f4, f5);
        float f11 = 1.0F;

        for (int i = 0; i < 2; i++) {
            p_110730_.pushPose();
            float f12 = -0.5F;
            float f13 = 0.5F;
            float f14 = -0.5F;
            float f15 = 0.5F;
            float f16 = -0.5F;
            p_110730_.translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
            p_110730_.mulPose(Axis.YP.rotationDegrees((float)(i * 2 - 1) * 10.0F));
            Matrix4f matrix4f = p_110730_.last().pose();
            vertexconsumer.addVertex(matrix4f, -0.5F, -0.5F, -0.5F).setUv(f8, f10).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            vertexconsumer.addVertex(matrix4f, 0.5F, -0.5F, -0.5F).setUv(f7, f10).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            vertexconsumer.addVertex(matrix4f, 0.5F, 0.5F, -0.5F).setUv(f7, f9).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            vertexconsumer.addVertex(matrix4f, -0.5F, 0.5F, -0.5F).setUv(f8, f9).setColor(1.0F, 1.0F, 1.0F, 0.9F);
            p_110730_.popPose();
        }
    }
}