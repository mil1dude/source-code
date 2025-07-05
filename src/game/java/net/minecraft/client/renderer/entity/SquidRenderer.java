package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidRenderer<T extends Squid> extends AgeableMobRenderer<T, SquidRenderState, SquidModel> {
    private static final ResourceLocation SQUID_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/squid/squid.png");

    public SquidRenderer(EntityRendererProvider.Context p_174406_, SquidModel p_174407_, SquidModel p_361703_) {
        super(p_174406_, p_174407_, p_361703_, 0.7F);
    }

    public ResourceLocation getTextureLocation(SquidRenderState p_365962_) {
        return SQUID_LOCATION;
    }

    public SquidRenderState createRenderState() {
        return new SquidRenderState();
    }

    public void extractRenderState(T p_363132_, SquidRenderState p_361362_, float p_367215_) {
        super.extractRenderState(p_363132_, p_361362_, p_367215_);
        p_361362_.tentacleAngle = Mth.lerp(p_367215_, p_363132_.oldTentacleAngle, p_363132_.tentacleAngle);
        p_361362_.xBodyRot = Mth.lerp(p_367215_, p_363132_.xBodyRotO, p_363132_.xBodyRot);
        p_361362_.zBodyRot = Mth.lerp(p_367215_, p_363132_.zBodyRotO, p_363132_.zBodyRot);
    }

    protected void setupRotations(SquidRenderState p_361221_, PoseStack p_116025_, float p_116026_, float p_116027_) {
        p_116025_.translate(0.0F, p_361221_.isBaby ? 0.25F : 0.5F, 0.0F);
        p_116025_.mulPose(Axis.YP.rotationDegrees(180.0F - p_116026_));
        p_116025_.mulPose(Axis.XP.rotationDegrees(p_361221_.xBodyRot));
        p_116025_.mulPose(Axis.YP.rotationDegrees(p_361221_.zBodyRot));
        p_116025_.translate(0.0F, p_361221_.isBaby ? -0.6F : -1.2F, 0.0F);
    }
}