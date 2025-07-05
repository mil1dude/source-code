package net.minecraft.client.data.models.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelLocationUtils {
    @Deprecated
    public static ResourceLocation decorateBlockModelLocation(String p_376541_) {
        return ResourceLocation.withDefaultNamespace("block/" + p_376541_);
    }

    public static ResourceLocation decorateItemModelLocation(String p_376094_) {
        return ResourceLocation.withDefaultNamespace("item/" + p_376094_);
    }

    public static ResourceLocation getModelLocation(Block p_378693_, String p_375625_) {
        ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(p_378693_);
        return resourcelocation.withPath(p_375700_ -> "block/" + p_375700_ + p_375625_);
    }

    public static ResourceLocation getModelLocation(Block p_376958_) {
        ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(p_376958_);
        return resourcelocation.withPrefix("block/");
    }

    public static ResourceLocation getModelLocation(Item p_378416_) {
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(p_378416_);
        return resourcelocation.withPrefix("item/");
    }

    public static ResourceLocation getModelLocation(Item p_377820_, String p_375834_) {
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(p_377820_);
        return resourcelocation.withPath(p_376725_ -> "item/" + p_376725_ + p_375834_);
    }
}