package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class HorseRenderer extends AbstractHorseRenderer<Horse, HorseRenderState, HorseModel> {
    private static final Map<Variant, ResourceLocation> LOCATION_BY_VARIANT = Util.make(Maps.newEnumMap(Variant.class), p_340939_ -> {
        p_340939_.put(Variant.WHITE, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_white.png"));
        p_340939_.put(Variant.CREAMY, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_creamy.png"));
        p_340939_.put(Variant.CHESTNUT, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_chestnut.png"));
        p_340939_.put(Variant.BROWN, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_brown.png"));
        p_340939_.put(Variant.BLACK, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_black.png"));
        p_340939_.put(Variant.GRAY, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_gray.png"));
        p_340939_.put(Variant.DARK_BROWN, ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_darkbrown.png"));
    });

    public HorseRenderer(EntityRendererProvider.Context p_174167_) {
        super(p_174167_, new HorseModel(p_174167_.bakeLayer(ModelLayers.HORSE)), new HorseModel(p_174167_.bakeLayer(ModelLayers.HORSE_BABY)));
        this.addLayer(new HorseMarkingLayer(this));
        this.addLayer(new HorseArmorLayer(this, p_174167_.getModelSet(), p_174167_.getEquipmentRenderer()));
    }

    public ResourceLocation getTextureLocation(HorseRenderState p_365322_) {
        return LOCATION_BY_VARIANT.get(p_365322_.variant);
    }

    public HorseRenderState createRenderState() {
        return new HorseRenderState();
    }

    public void extractRenderState(Horse p_363101_, HorseRenderState p_362954_, float p_365681_) {
        super.extractRenderState(p_363101_, p_362954_, p_365681_);
        p_362954_.variant = p_363101_.getVariant();
        p_362954_.markings = p_363101_.getMarkings();
        p_362954_.bodyArmorItem = p_363101_.getBodyArmorItem().copy();
    }
}