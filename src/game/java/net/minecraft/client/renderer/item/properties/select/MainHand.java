package net.minecraft.client.renderer.item.properties.select;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record MainHand() implements SelectItemModelProperty<HumanoidArm> {
    public static final SelectItemModelProperty.Type<MainHand, HumanoidArm> TYPE = SelectItemModelProperty.Type.create(
        MapCodec.unit(new MainHand()), HumanoidArm.CODEC
    );

    @Nullable
    public HumanoidArm get(
        ItemStack p_376833_, @Nullable ClientLevel p_377152_, @Nullable LivingEntity p_376407_, int p_376651_, ItemDisplayContext p_376504_
    ) {
        return p_376407_ == null ? null : p_376407_.getMainArm();
    }

    @Override
    public SelectItemModelProperty.Type<MainHand, HumanoidArm> type() {
        return TYPE;
    }
}