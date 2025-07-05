package net.minecraft.client.renderer.item;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MissingItemModel implements ItemModel {
    private final BakedModel model;

    public MissingItemModel(BakedModel p_378250_) {
        this.model = p_378250_;
    }

    @Override
    public void update(
        ItemStackRenderState p_378769_,
        ItemStack p_377587_,
        ItemModelResolver p_375595_,
        ItemDisplayContext p_376141_,
        @Nullable ClientLevel p_378330_,
        @Nullable LivingEntity p_377160_,
        int p_377588_
    ) {
        p_378769_.newLayer().setupBlockModel(this.model, Sheets.cutoutBlockSheet());
    }
}