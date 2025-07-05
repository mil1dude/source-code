package net.minecraft.client.renderer.item.properties.select;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ContextDimension() implements SelectItemModelProperty<ResourceKey<Level>> {
    public static final SelectItemModelProperty.Type<ContextDimension, ResourceKey<Level>> TYPE = SelectItemModelProperty.Type.create(
        MapCodec.unit(new ContextDimension()), ResourceKey.codec(Registries.DIMENSION)
    );

    @Nullable
    public ResourceKey<Level> get(
        ItemStack p_375420_, @Nullable ClientLevel p_375432_, @Nullable LivingEntity p_377954_, int p_377075_, ItemDisplayContext p_375804_
    ) {
        return p_375432_ != null ? p_375432_.dimension() : null;
    }

    @Override
    public SelectItemModelProperty.Type<ContextDimension, ResourceKey<Level>> type() {
        return TYPE;
    }
}