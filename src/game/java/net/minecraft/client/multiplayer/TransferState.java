package net.minecraft.client.multiplayer;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record TransferState(Map<ResourceLocation, byte[]> cookies) {
}