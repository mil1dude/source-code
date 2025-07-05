package net.minecraft.client.renderer.entity.state;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidRenderState extends ArmedEntityRenderState {
    public float swimAmount;
    public float attackTime;
    public float speedValue = 1.0F;
    public float maxCrossbowChargeDuration;
    public int ticksUsingItem;
    public HumanoidArm attackArm = HumanoidArm.RIGHT;
    public InteractionHand useItemHand = InteractionHand.MAIN_HAND;
    public boolean isCrouching;
    public boolean isFallFlying;
    public boolean isVisuallySwimming;
    public boolean isPassenger;
    public boolean isUsingItem;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public ItemStack headEquipment = ItemStack.EMPTY;
    public ItemStack chestEquipment = ItemStack.EMPTY;
    public ItemStack legsEquipment = ItemStack.EMPTY;
    public ItemStack feetEquipment = ItemStack.EMPTY;
}