package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RangeSelectItemModelProperties {
    private static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends RangeSelectItemModelProperty>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
    public static final MapCodec<RangeSelectItemModelProperty> MAP_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
        .dispatchMap("property", RangeSelectItemModelProperty::type, p_375502_ -> p_375502_);

    public static void bootstrap() {
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("custom_model_data"), CustomModelDataProperty.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("bundle/fullness"), BundleFullness.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("damage"), Damage.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("cooldown"), Cooldown.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("time"), Time.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("compass"), CompassAngle.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("crossbow/pull"), CrossbowPull.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("use_cycle"), UseCycle.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("use_duration"), UseDuration.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("count"), Count.MAP_CODEC);
    }
}