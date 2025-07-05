package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedRenderer extends AbstractZombieRenderer<Drowned, ZombieRenderState, DrownedModel> {
    private static final ResourceLocation DROWNED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/drowned.png");

    public DrownedRenderer(EntityRendererProvider.Context p_173964_) {
        super(
            p_173964_,
            new DrownedModel(p_173964_.bakeLayer(ModelLayers.DROWNED)),
            new DrownedModel(p_173964_.bakeLayer(ModelLayers.DROWNED_BABY)),
            new DrownedModel(p_173964_.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)),
            new DrownedModel(p_173964_.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR)),
            new DrownedModel(p_173964_.bakeLayer(ModelLayers.DROWNED_BABY_INNER_ARMOR)),
            new DrownedModel(p_173964_.bakeLayer(ModelLayers.DROWNED_BABY_OUTER_ARMOR))
        );
        this.addLayer(new DrownedOuterLayer(this, p_173964_.getModelSet()));
    }

    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieRenderState p_363849_) {
        return DROWNED_LOCATION;
    }

    protected void setupRotations(ZombieRenderState p_368450_, PoseStack p_114104_, float p_114105_, float p_114106_) {
        super.setupRotations(p_368450_, p_114104_, p_114105_, p_114106_);
        float f = p_368450_.swimAmount;
        if (f > 0.0F) {
            float f1 = -10.0F - p_368450_.xRot;
            float f2 = Mth.lerp(f, 0.0F, f1);
            p_114104_.rotateAround(Axis.XP.rotationDegrees(f2), 0.0F, p_368450_.boundingBoxHeight / 2.0F / p_114106_, 0.0F);
        }
    }

    protected HumanoidModel.ArmPose getArmPose(Drowned p_376617_, HumanoidArm p_375536_) {
        ItemStack itemstack = p_376617_.getItemHeldByArm(p_375536_);
        return p_376617_.getMainArm() == p_375536_ && p_376617_.isAggressive() && itemstack.is(Items.TRIDENT)
            ? HumanoidModel.ArmPose.THROW_SPEAR
            : HumanoidModel.ArmPose.EMPTY;
    }
}