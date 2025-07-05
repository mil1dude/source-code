package net.minecraft.client.renderer.entity.state;

import net.minecraft.world.entity.npc.VillagerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface VillagerDataHolderRenderState {
    VillagerData getVillagerData();
}