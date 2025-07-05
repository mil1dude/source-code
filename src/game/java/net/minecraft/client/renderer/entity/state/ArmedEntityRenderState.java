package net.minecraft.client.renderer.entity.state;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmedEntityRenderState extends LivingEntityRenderState {
    public HumanoidArm mainArm = HumanoidArm.RIGHT;
    public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;
    public final ItemStackRenderState rightHandItem = new ItemStackRenderState();
    public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
    public final ItemStackRenderState leftHandItem = new ItemStackRenderState();

    public ItemStackRenderState getMainHandItem() {
        return this.mainArm == HumanoidArm.RIGHT ? this.rightHandItem : this.leftHandItem;
    }

    public static void extractArmedEntityRenderState(LivingEntity p_378749_, ArmedEntityRenderState p_378508_, ItemModelResolver p_378441_) {
        p_378508_.mainArm = p_378749_.getMainArm();
        p_378441_.updateForLiving(p_378508_.rightHandItem, p_378749_.getItemHeldByArm(HumanoidArm.RIGHT), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, p_378749_);
        p_378441_.updateForLiving(p_378508_.leftHandItem, p_378749_.getItemHeldByArm(HumanoidArm.LEFT), ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, p_378749_);
    }
}