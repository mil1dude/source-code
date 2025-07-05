package net.minecraft.client.data.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EquipmentAssetProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public EquipmentAssetProvider(PackOutput p_378812_) {
        this.pathProvider = p_378812_.createPathProvider(PackOutput.Target.RESOURCE_PACK, "equipment");
    }

    private static void bootstrap(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> p_375964_) {
        p_375964_.accept(
            EquipmentAssets.LEATHER,
            EquipmentClientInfo.builder()
                .addHumanoidLayers(ResourceLocation.withDefaultNamespace("leather"), true)
                .addHumanoidLayers(ResourceLocation.withDefaultNamespace("leather_overlay"), false)
                .addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(ResourceLocation.withDefaultNamespace("leather"), true))
                .build()
        );
        p_375964_.accept(EquipmentAssets.CHAINMAIL, onlyHumanoid("chainmail"));
        p_375964_.accept(EquipmentAssets.IRON, humanoidAndHorse("iron"));
        p_375964_.accept(EquipmentAssets.GOLD, humanoidAndHorse("gold"));
        p_375964_.accept(EquipmentAssets.DIAMOND, humanoidAndHorse("diamond"));
        p_375964_.accept(EquipmentAssets.TURTLE_SCUTE, EquipmentClientInfo.builder().addMainHumanoidLayer(ResourceLocation.withDefaultNamespace("turtle_scute"), false).build());
        p_375964_.accept(EquipmentAssets.NETHERITE, onlyHumanoid("netherite"));
        p_375964_.accept(
            EquipmentAssets.ARMADILLO_SCUTE,
            EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WOLF_BODY, EquipmentClientInfo.Layer.onlyIfDyed(ResourceLocation.withDefaultNamespace("armadillo_scute"), false))
                .addLayers(
                    EquipmentClientInfo.LayerType.WOLF_BODY, EquipmentClientInfo.Layer.onlyIfDyed(ResourceLocation.withDefaultNamespace("armadillo_scute_overlay"), true)
                )
                .build()
        );
        p_375964_.accept(
            EquipmentAssets.ELYTRA,
            EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.WINGS, new EquipmentClientInfo.Layer(ResourceLocation.withDefaultNamespace("elytra"), Optional.empty(), true))
                .build()
        );

        for (Entry<DyeColor, ResourceKey<EquipmentAsset>> entry : EquipmentAssets.CARPETS.entrySet()) {
            DyeColor dyecolor = entry.getKey();
            ResourceKey<EquipmentAsset> resourcekey = entry.getValue();
            p_375964_.accept(
                resourcekey,
                EquipmentClientInfo.builder()
                    .addLayers(EquipmentClientInfo.LayerType.LLAMA_BODY, new EquipmentClientInfo.Layer(ResourceLocation.withDefaultNamespace(dyecolor.getSerializedName())))
                    .build()
            );
        }

        p_375964_.accept(
            EquipmentAssets.TRADER_LLAMA,
            EquipmentClientInfo.builder()
                .addLayers(EquipmentClientInfo.LayerType.LLAMA_BODY, new EquipmentClientInfo.Layer(ResourceLocation.withDefaultNamespace("trader_llama")))
                .build()
        );
    }

    private static EquipmentClientInfo onlyHumanoid(String p_376219_) {
        return EquipmentClientInfo.builder().addHumanoidLayers(ResourceLocation.withDefaultNamespace(p_376219_)).build();
    }

    private static EquipmentClientInfo humanoidAndHorse(String p_375668_) {
        return EquipmentClientInfo.builder()
            .addHumanoidLayers(ResourceLocation.withDefaultNamespace(p_375668_))
            .addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(ResourceLocation.withDefaultNamespace(p_375668_), false))
            .build();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput p_376319_) {
        Map<ResourceKey<EquipmentAsset>, EquipmentClientInfo> map = new HashMap<>();
        bootstrap((p_376477_, p_377690_) -> {
            if (map.putIfAbsent(p_376477_, p_377690_) != null) {
                throw new IllegalStateException("Tried to register equipment asset twice for id: " + p_376477_);
            }
        });
        return DataProvider.saveAll(p_376319_, EquipmentClientInfo.CODEC, this.pathProvider::json, map);
    }

    @Override
    public String getName() {
        return "Equipment Asset Definitions";
    }
}