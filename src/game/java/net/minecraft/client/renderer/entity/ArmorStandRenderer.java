package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.ArmorStandModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandRenderer extends LivingEntityRenderer<ArmorStand, ArmorStandRenderState, ArmorStandArmorModel> {
    public static final ResourceLocation DEFAULT_SKIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/armorstand/wood.png");
    private final ArmorStandArmorModel bigModel = this.getModel();
    private final ArmorStandArmorModel smallModel;

    public ArmorStandRenderer(EntityRendererProvider.Context p_173915_) {
        super(p_173915_, new ArmorStandModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0F);
        this.smallModel = new ArmorStandModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_SMALL));
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)),
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_INNER_ARMOR)),
                new ArmorStandArmorModel(p_173915_.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_OUTER_ARMOR)),
                p_173915_.getEquipmentRenderer()
            )
        );
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new WingsLayer<>(this, p_173915_.getModelSet(), p_173915_.getEquipmentRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, p_173915_.getModelSet()));
    }

    public ResourceLocation getTextureLocation(ArmorStandRenderState p_361116_) {
        return DEFAULT_SKIN_LOCATION;
    }

    public ArmorStandRenderState createRenderState() {
        return new ArmorStandRenderState();
    }

    public void extractRenderState(ArmorStand p_364068_, ArmorStandRenderState p_361680_, float p_369387_) {
        super.extractRenderState(p_364068_, p_361680_, p_369387_);
        HumanoidMobRenderer.extractHumanoidRenderState(p_364068_, p_361680_, p_369387_, this.itemModelResolver);
        p_361680_.yRot = Mth.rotLerp(p_369387_, p_364068_.yRotO, p_364068_.getYRot());
        p_361680_.isMarker = p_364068_.isMarker();
        p_361680_.isSmall = p_364068_.isSmall();
        p_361680_.showArms = p_364068_.showArms();
        p_361680_.showBasePlate = p_364068_.showBasePlate();
        p_361680_.bodyPose = p_364068_.getBodyPose();
        p_361680_.headPose = p_364068_.getHeadPose();
        p_361680_.leftArmPose = p_364068_.getLeftArmPose();
        p_361680_.rightArmPose = p_364068_.getRightArmPose();
        p_361680_.leftLegPose = p_364068_.getLeftLegPose();
        p_361680_.rightLegPose = p_364068_.getRightLegPose();
        p_361680_.wiggle = (float)(p_364068_.level().getGameTime() - p_364068_.lastHit) + p_369387_;
    }

    public void render(ArmorStandRenderState p_368962_, PoseStack p_364467_, MultiBufferSource p_368924_, int p_365572_) {
        this.model = p_368962_.isSmall ? this.smallModel : this.bigModel;
        super.render(p_368962_, p_364467_, p_368924_, p_365572_);
    }

    protected void setupRotations(ArmorStandRenderState p_365303_, PoseStack p_113788_, float p_113789_, float p_113790_) {
        p_113788_.mulPose(Axis.YP.rotationDegrees(180.0F - p_113789_));
        if (p_365303_.wiggle < 5.0F) {
            p_113788_.mulPose(Axis.YP.rotationDegrees(Mth.sin(p_365303_.wiggle / 1.5F * (float) Math.PI) * 3.0F));
        }
    }

    protected boolean shouldShowName(ArmorStand p_363344_, double p_365520_) {
        return p_363344_.isCustomNameVisible();
    }

    @Nullable
    protected RenderType getRenderType(ArmorStandRenderState p_367706_, boolean p_113807_, boolean p_113808_, boolean p_113809_) {
        if (!p_367706_.isMarker) {
            return super.getRenderType(p_367706_, p_113807_, p_113808_, p_113809_);
        } else {
            ResourceLocation resourcelocation = this.getTextureLocation(p_367706_);
            if (p_113808_) {
                return RenderType.entityTranslucent(resourcelocation, false);
            } else {
                return p_113807_ ? RenderType.entityCutoutNoCull(resourcelocation, false) : null;
            }
        }
    }
}