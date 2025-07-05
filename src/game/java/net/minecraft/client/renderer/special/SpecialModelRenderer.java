package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface SpecialModelRenderer<T> {
    void render(
        @Nullable T p_378499_, ItemDisplayContext p_377312_, PoseStack p_378346_, MultiBufferSource p_376164_, int p_377547_, int p_378601_, boolean p_376553_
    );

    @Nullable
    T extractArgument(ItemStack p_376218_);

    @OnlyIn(Dist.CLIENT)
    public interface Unbaked {
        @Nullable
        SpecialModelRenderer<?> bake(EntityModelSet p_378825_);

        MapCodec<? extends SpecialModelRenderer.Unbaked> type();
    }
}