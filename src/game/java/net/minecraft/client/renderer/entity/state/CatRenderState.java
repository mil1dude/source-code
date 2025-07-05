package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatRenderState extends FelineRenderState {
    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/cat/tabby.png");
    public ResourceLocation texture = DEFAULT_TEXTURE;
    public boolean isLyingOnTopOfSleepingPlayer;
    @Nullable
    public DyeColor collarColor;
}