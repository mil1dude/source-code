package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record IsCarried() implements ConditionalItemModelProperty {
    public static final MapCodec<IsCarried> MAP_CODEC = MapCodec.unit(new IsCarried());

    @Override
    public boolean get(
        ItemStack p_377567_, @Nullable ClientLevel p_376720_, @Nullable LivingEntity p_376604_, int p_375610_, ItemDisplayContext p_375638_
    ) {
        if (p_376604_ instanceof LocalPlayer localplayer && localplayer.containerMenu.getCarried() == p_377567_) {
            return true;
        }

        return false;
    }

    @Override
    public MapCodec<IsCarried> type() {
        return MAP_CODEC;
    }
}