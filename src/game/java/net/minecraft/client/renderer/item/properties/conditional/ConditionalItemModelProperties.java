package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConditionalItemModelProperties {
    private static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ConditionalItemModelProperty>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
    public static final MapCodec<ConditionalItemModelProperty> MAP_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
        .dispatchMap("property", ConditionalItemModelProperty::type, p_376487_ -> p_376487_);

    public static void bootstrap() {
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("custom_model_data"), CustomModelDataProperty.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("using_item"), IsUsingItem.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("broken"), Broken.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("damaged"), Damaged.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("fishing_rod/cast"), FishingRodCast.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("has_component"), HasComponent.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("bundle/has_selected_item"), BundleHasSelectedItem.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("selected"), IsSelected.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("carried"), IsCarried.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("extended_view"), ExtendedView.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("keybind_down"), IsKeybindDown.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("view_entity"), IsViewEntity.MAP_CODEC);
    }
}