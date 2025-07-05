package net.minecraft.client.data.models;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ItemModelOutput {
    void accept(Item p_375406_, ItemModel.Unbaked p_376490_);

    void copy(Item p_376683_, Item p_376863_);
}