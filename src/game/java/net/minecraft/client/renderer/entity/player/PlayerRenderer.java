package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    public PlayerRenderer(EntityRendererProvider.Context p_174557_, boolean p_174558_) {
        super(p_174557_, new PlayerModel(p_174557_.bakeLayer(p_174558_ ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), p_174558_), 0.5F);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel(p_174557_.bakeLayer(p_174558_ ? ModelLayers.PLAYER_SLIM_INNER_ARMOR : ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel(p_174557_.bakeLayer(p_174558_ ? ModelLayers.PLAYER_SLIM_OUTER_ARMOR : ModelLayers.PLAYER_OUTER_ARMOR)),
                p_174557_.getEquipmentRenderer()
            )
        );
        this.addLayer(new PlayerItemInHandLayer<>(this));
        this.addLayer(new ArrowLayer<>(this, p_174557_));
        this.addLayer(new Deadmau5EarsLayer(this, p_174557_.getModelSet()));
        this.addLayer(new CapeLayer(this, p_174557_.getModelSet(), p_174557_.getEquipmentAssets()));
        this.addLayer(new CustomHeadLayer<>(this, p_174557_.getModelSet()));
        this.addLayer(new WingsLayer<>(this, p_174557_.getModelSet(), p_174557_.getEquipmentRenderer()));
        this.addLayer(new ParrotOnShoulderLayer(this, p_174557_.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer(this, p_174557_.getModelSet()));
        this.addLayer(new BeeStingerLayer<>(this, p_174557_));
    }

    protected boolean shouldRenderLayers(PlayerRenderState p_362318_) {
        return !p_362318_.isSpectator;
    }

    public Vec3 getRenderOffset(PlayerRenderState p_365223_) {
        Vec3 vec3 = super.getRenderOffset(p_365223_);
        return p_365223_.isCrouching ? vec3.add(0.0, (double)(p_365223_.scale * -2.0F) / 16.0, 0.0) : vec3;
    }

    private static HumanoidModel.ArmPose getArmPose(AbstractClientPlayer p_375883_, HumanoidArm p_366116_) {
        ItemStack itemstack = p_375883_.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemstack1 = p_375883_.getItemInHand(InteractionHand.OFF_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(p_375883_, itemstack, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(p_375883_, itemstack1, InteractionHand.OFF_HAND);
        if (humanoidmodel$armpose.isTwoHanded()) {
            humanoidmodel$armpose1 = itemstack1.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }

        return p_375883_.getMainArm() == p_366116_ ? humanoidmodel$armpose : humanoidmodel$armpose1;
    }

    private static HumanoidModel.ArmPose getArmPose(Player p_376602_, ItemStack p_376446_, InteractionHand p_361073_) {
        if (p_376446_.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (p_376602_.getUsedItemHand() == p_361073_ && p_376602_.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation itemuseanimation = p_376446_.getUseAnimation();
                if (itemuseanimation == ItemUseAnimation.BLOCK) {
                    return HumanoidModel.ArmPose.BLOCK;
                }

                if (itemuseanimation == ItemUseAnimation.BOW) {
                    return HumanoidModel.ArmPose.BOW_AND_ARROW;
                }

                if (itemuseanimation == ItemUseAnimation.SPEAR) {
                    return HumanoidModel.ArmPose.THROW_SPEAR;
                }

                if (itemuseanimation == ItemUseAnimation.CROSSBOW) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }

                if (itemuseanimation == ItemUseAnimation.SPYGLASS) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }

                if (itemuseanimation == ItemUseAnimation.TOOT_HORN) {
                    return HumanoidModel.ArmPose.TOOT_HORN;
                }

                if (itemuseanimation == ItemUseAnimation.BRUSH) {
                    return HumanoidModel.ArmPose.BRUSH;
                }
            } else if (!p_376602_.swinging && p_376446_.is(Items.CROSSBOW) && CrossbowItem.isCharged(p_376446_)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            return HumanoidModel.ArmPose.ITEM;
        }
    }

    public ResourceLocation getTextureLocation(PlayerRenderState p_364988_) {
        return p_364988_.skin.texture();
    }

    protected void scale(PlayerRenderState p_368476_, PoseStack p_117799_) {
        float f = 0.9375F;
        p_117799_.scale(0.9375F, 0.9375F, 0.9375F);
    }

    protected void renderNameTag(PlayerRenderState p_360888_, Component p_117809_, PoseStack p_117810_, MultiBufferSource p_117811_, int p_117812_) {
        p_117810_.pushPose();
        if (p_360888_.scoreText != null) {
            super.renderNameTag(p_360888_, p_360888_.scoreText, p_117810_, p_117811_, p_117812_);
            p_117810_.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
        }

        super.renderNameTag(p_360888_, p_117809_, p_117810_, p_117811_, p_117812_);
        p_117810_.popPose();
    }

    public PlayerRenderState createRenderState() {
        return new PlayerRenderState();
    }

    public void extractRenderState(AbstractClientPlayer p_366577_, PlayerRenderState p_364437_, float p_365590_) {
        super.extractRenderState(p_366577_, p_364437_, p_365590_);
        HumanoidMobRenderer.extractHumanoidRenderState(p_366577_, p_364437_, p_365590_, this.itemModelResolver);
        p_364437_.leftArmPose = getArmPose(p_366577_, HumanoidArm.LEFT);
        p_364437_.rightArmPose = getArmPose(p_366577_, HumanoidArm.RIGHT);
        p_364437_.skin = p_366577_.getSkin();
        p_364437_.arrowCount = p_366577_.getArrowCount();
        p_364437_.stingerCount = p_366577_.getStingerCount();
        p_364437_.useItemRemainingTicks = p_366577_.getUseItemRemainingTicks();
        p_364437_.swinging = p_366577_.swinging;
        p_364437_.isSpectator = p_366577_.isSpectator();
        p_364437_.showHat = p_366577_.isModelPartShown(PlayerModelPart.HAT);
        p_364437_.showJacket = p_366577_.isModelPartShown(PlayerModelPart.JACKET);
        p_364437_.showLeftPants = p_366577_.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        p_364437_.showRightPants = p_366577_.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        p_364437_.showLeftSleeve = p_366577_.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        p_364437_.showRightSleeve = p_366577_.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        p_364437_.showCape = p_366577_.isModelPartShown(PlayerModelPart.CAPE);
        extractFlightData(p_366577_, p_364437_, p_365590_);
        extractCapeState(p_366577_, p_364437_, p_365590_);
        if (p_364437_.distanceToCameraSq < 100.0) {
            Scoreboard scoreboard = p_366577_.getScoreboard();
            Objective objective = scoreboard.getDisplayObjective(DisplaySlot.BELOW_NAME);
            if (objective != null) {
                ReadOnlyScoreInfo readonlyscoreinfo = scoreboard.getPlayerScoreInfo(p_366577_, objective);
                Component component = ReadOnlyScoreInfo.safeFormatValue(readonlyscoreinfo, objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
                p_364437_.scoreText = Component.empty().append(component).append(CommonComponents.SPACE).append(objective.getDisplayName());
            } else {
                p_364437_.scoreText = null;
            }
        } else {
            p_364437_.scoreText = null;
        }

        p_364437_.parrotOnLeftShoulder = getParrotOnShoulder(p_366577_, true);
        p_364437_.parrotOnRightShoulder = getParrotOnShoulder(p_366577_, false);
        p_364437_.id = p_366577_.getId();
        p_364437_.name = p_366577_.getGameProfile().getName();
        p_364437_.heldOnHead.clear();
        if (p_364437_.isUsingItem) {
            ItemStack itemstack = p_366577_.getItemInHand(p_364437_.useItemHand);
            if (itemstack.is(Items.SPYGLASS)) {
                this.itemModelResolver.updateForLiving(p_364437_.heldOnHead, itemstack, ItemDisplayContext.HEAD, false, p_366577_);
            }
        }
    }

    private static void extractFlightData(AbstractClientPlayer p_366513_, PlayerRenderState p_361371_, float p_365196_) {
        p_361371_.fallFlyingTimeInTicks = (float)p_366513_.getFallFlyingTicks() + p_365196_;
        Vec3 vec3 = p_366513_.getViewVector(p_365196_);
        Vec3 vec31 = p_366513_.getDeltaMovementLerped(p_365196_);
        double d0 = vec31.horizontalDistanceSqr();
        double d1 = vec3.horizontalDistanceSqr();
        if (d0 > 0.0 && d1 > 0.0) {
            p_361371_.shouldApplyFlyingYRot = true;
            double d2 = Math.min(1.0, (vec31.x * vec3.x + vec31.z * vec3.z) / Math.sqrt(d0 * d1));
            double d3 = vec31.x * vec3.z - vec31.z * vec3.x;
            p_361371_.flyingYRot = (float)(Math.signum(d3) * Math.acos(d2));
        } else {
            p_361371_.shouldApplyFlyingYRot = false;
            p_361371_.flyingYRot = 0.0F;
        }
    }

    private static void extractCapeState(AbstractClientPlayer p_366404_, PlayerRenderState p_365208_, float p_366613_) {
        double d0 = Mth.lerp((double)p_366613_, p_366404_.xCloakO, p_366404_.xCloak)
            - Mth.lerp((double)p_366613_, p_366404_.xo, p_366404_.getX());
        double d1 = Mth.lerp((double)p_366613_, p_366404_.yCloakO, p_366404_.yCloak)
            - Mth.lerp((double)p_366613_, p_366404_.yo, p_366404_.getY());
        double d2 = Mth.lerp((double)p_366613_, p_366404_.zCloakO, p_366404_.zCloak)
            - Mth.lerp((double)p_366613_, p_366404_.zo, p_366404_.getZ());
        float f = Mth.rotLerp(p_366613_, p_366404_.yBodyRotO, p_366404_.yBodyRot);
        double d3 = (double)Mth.sin(f * (float) (Math.PI / 180.0));
        double d4 = (double)(-Mth.cos(f * (float) (Math.PI / 180.0)));
        p_365208_.capeFlap = (float)d1 * 10.0F;
        p_365208_.capeFlap = Mth.clamp(p_365208_.capeFlap, -6.0F, 32.0F);
        p_365208_.capeLean = (float)(d0 * d3 + d2 * d4) * 100.0F;
        p_365208_.capeLean = p_365208_.capeLean * (1.0F - p_365208_.fallFlyingScale());
        p_365208_.capeLean = Mth.clamp(p_365208_.capeLean, 0.0F, 150.0F);
        p_365208_.capeLean2 = (float)(d0 * d4 - d2 * d3) * 100.0F;
        p_365208_.capeLean2 = Mth.clamp(p_365208_.capeLean2, -20.0F, 20.0F);
        float f1 = Mth.lerp(p_366613_, p_366404_.oBob, p_366404_.bob);
        float f2 = Mth.lerp(p_366613_, p_366404_.walkDistO, p_366404_.walkDist);
        p_365208_.capeFlap = p_365208_.capeFlap + Mth.sin(f2 * 6.0F) * 32.0F * f1;
    }

    @Nullable
    private static Parrot.Variant getParrotOnShoulder(AbstractClientPlayer p_362348_, boolean p_363425_) {
        CompoundTag compoundtag = p_363425_ ? p_362348_.getShoulderEntityLeft() : p_362348_.getShoulderEntityRight();
        return EntityType.byString(compoundtag.getString("id")).filter(p_369258_ -> p_369258_ == EntityType.PARROT).isPresent()
            ? Parrot.Variant.byId(compoundtag.getInt("Variant"))
            : null;
    }

    public void renderRightHand(PoseStack p_117771_, MultiBufferSource p_117772_, int p_117773_, ResourceLocation p_364347_, boolean p_367689_) {
        this.renderHand(p_117771_, p_117772_, p_117773_, p_364347_, this.model.rightArm, p_367689_);
    }

    public void renderLeftHand(PoseStack p_117814_, MultiBufferSource p_117815_, int p_117816_, ResourceLocation p_368419_, boolean p_362915_) {
        this.renderHand(p_117814_, p_117815_, p_117816_, p_368419_, this.model.leftArm, p_362915_);
    }

    private void renderHand(PoseStack p_117776_, MultiBufferSource p_117777_, int p_117778_, ResourceLocation p_365409_, ModelPart p_117780_, boolean p_364227_) {
        PlayerModel playermodel = this.getModel();
        p_117780_.resetPose();
        p_117780_.visible = true;
        playermodel.leftSleeve.visible = p_364227_;
        playermodel.rightSleeve.visible = p_364227_;
        playermodel.leftArm.zRot = -0.1F;
        playermodel.rightArm.zRot = 0.1F;
        p_117780_.render(p_117776_, p_117777_.getBuffer(RenderType.entityTranslucent(p_365409_)), p_117778_, OverlayTexture.NO_OVERLAY);
    }

    protected void setupRotations(PlayerRenderState p_369667_, PoseStack p_117803_, float p_117804_, float p_117805_) {
        float f = p_369667_.swimAmount;
        float f1 = p_369667_.xRot;
        if (p_369667_.isFallFlying) {
            super.setupRotations(p_369667_, p_117803_, p_117804_, p_117805_);
            float f2 = p_369667_.fallFlyingScale();
            if (!p_369667_.isAutoSpinAttack) {
                p_117803_.mulPose(Axis.XP.rotationDegrees(f2 * (-90.0F - f1)));
            }

            if (p_369667_.shouldApplyFlyingYRot) {
                p_117803_.mulPose(Axis.YP.rotation(p_369667_.flyingYRot));
            }
        } else if (f > 0.0F) {
            super.setupRotations(p_369667_, p_117803_, p_117804_, p_117805_);
            float f4 = p_369667_.isInWater ? -90.0F - f1 : -90.0F;
            float f3 = Mth.lerp(f, 0.0F, f4);
            p_117803_.mulPose(Axis.XP.rotationDegrees(f3));
            if (p_369667_.isVisuallySwimming) {
                p_117803_.translate(0.0F, -1.0F, 0.3F);
            }
        } else {
            super.setupRotations(p_369667_, p_117803_, p_117804_, p_117805_);
        }
    }
}