package net.minecraft.client.resources.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record EquipmentClientInfo(Map<EquipmentClientInfo.LayerType, List<EquipmentClientInfo.Layer>> layers) {
    private static final Codec<List<EquipmentClientInfo.Layer>> LAYER_LIST_CODEC = ExtraCodecs.nonEmptyList(EquipmentClientInfo.Layer.CODEC.listOf());
    public static final Codec<EquipmentClientInfo> CODEC = RecordCodecBuilder.create(
        p_376111_ -> p_376111_.group(
                    ExtraCodecs.nonEmptyMap(Codec.unboundedMap(EquipmentClientInfo.LayerType.CODEC, LAYER_LIST_CODEC))
                        .fieldOf("layers")
                        .forGetter(EquipmentClientInfo::layers)
                )
                .apply(p_376111_, EquipmentClientInfo::new)
    );

    public static EquipmentClientInfo.Builder builder() {
        return new EquipmentClientInfo.Builder();
    }

    public List<EquipmentClientInfo.Layer> getLayers(EquipmentClientInfo.LayerType p_377530_) {
        return this.layers.getOrDefault(p_377530_, List.of());
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Map<EquipmentClientInfo.LayerType, List<EquipmentClientInfo.Layer>> layersByType = new EnumMap<>(EquipmentClientInfo.LayerType.class);

        Builder() {
        }

        public EquipmentClientInfo.Builder addHumanoidLayers(ResourceLocation p_376576_) {
            return this.addHumanoidLayers(p_376576_, false);
        }

        public EquipmentClientInfo.Builder addHumanoidLayers(ResourceLocation p_376103_, boolean p_377923_) {
            this.addLayers(EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS, EquipmentClientInfo.Layer.leatherDyeable(p_376103_, p_377923_));
            this.addMainHumanoidLayer(p_376103_, p_377923_);
            return this;
        }

        public EquipmentClientInfo.Builder addMainHumanoidLayer(ResourceLocation p_375484_, boolean p_378714_) {
            return this.addLayers(EquipmentClientInfo.LayerType.HUMANOID, EquipmentClientInfo.Layer.leatherDyeable(p_375484_, p_378714_));
        }

        public EquipmentClientInfo.Builder addLayers(EquipmentClientInfo.LayerType p_377620_, EquipmentClientInfo.Layer... p_377277_) {
            Collections.addAll(this.layersByType.computeIfAbsent(p_377620_, p_376726_ -> new ArrayList<>()), p_377277_);
            return this;
        }

        public EquipmentClientInfo build() {
            return new EquipmentClientInfo(
                this.layersByType.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, p_378270_ -> List.copyOf(p_378270_.getValue())))
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Dyeable(Optional<Integer> colorWhenUndyed) {
        public static final Codec<EquipmentClientInfo.Dyeable> CODEC = RecordCodecBuilder.create(
            p_377022_ -> p_377022_.group(ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("color_when_undyed").forGetter(EquipmentClientInfo.Dyeable::colorWhenUndyed))
                    .apply(p_377022_, EquipmentClientInfo.Dyeable::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static record Layer(ResourceLocation textureId, Optional<EquipmentClientInfo.Dyeable> dyeable, boolean usePlayerTexture) {
        public static final Codec<EquipmentClientInfo.Layer> CODEC = RecordCodecBuilder.create(
            p_377965_ -> p_377965_.group(
                        ResourceLocation.CODEC.fieldOf("texture").forGetter(EquipmentClientInfo.Layer::textureId),
                        EquipmentClientInfo.Dyeable.CODEC.optionalFieldOf("dyeable").forGetter(EquipmentClientInfo.Layer::dyeable),
                        Codec.BOOL.optionalFieldOf("use_player_texture", Boolean.valueOf(false)).forGetter(EquipmentClientInfo.Layer::usePlayerTexture)
                    )
                    .apply(p_377965_, EquipmentClientInfo.Layer::new)
        );

        public Layer(ResourceLocation p_378074_) {
            this(p_378074_, Optional.empty(), false);
        }

        public static EquipmentClientInfo.Layer leatherDyeable(ResourceLocation p_378679_, boolean p_377080_) {
            return new EquipmentClientInfo.Layer(
                p_378679_, p_377080_ ? Optional.of(new EquipmentClientInfo.Dyeable(Optional.of(-6265536))) : Optional.empty(), false
            );
        }

        public static EquipmentClientInfo.Layer onlyIfDyed(ResourceLocation p_378779_, boolean p_376357_) {
            return new EquipmentClientInfo.Layer(
                p_378779_, p_376357_ ? Optional.of(new EquipmentClientInfo.Dyeable(Optional.empty())) : Optional.empty(), false
            );
        }

        public ResourceLocation getTextureLocation(EquipmentClientInfo.LayerType p_375959_) {
            return this.textureId.withPath(p_376895_ -> "textures/entity/equipment/" + p_375959_.getSerializedName() + "/" + p_376895_ + ".png");
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum LayerType implements StringRepresentable {
        HUMANOID("humanoid"),
        HUMANOID_LEGGINGS("humanoid_leggings"),
        WINGS("wings"),
        WOLF_BODY("wolf_body"),
        HORSE_BODY("horse_body"),
        LLAMA_BODY("llama_body");

        public static final Codec<EquipmentClientInfo.LayerType> CODEC = StringRepresentable.fromEnum(EquipmentClientInfo.LayerType::values);
        private final String id;

        private LayerType(final String p_378823_) {
            this.id = p_378823_;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }
}