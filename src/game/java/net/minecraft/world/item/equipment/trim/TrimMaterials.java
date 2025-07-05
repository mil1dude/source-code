package net.minecraft.world.item.equipment.trim;

import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class TrimMaterials {
    public static final ResourceKey<TrimMaterial> QUARTZ = registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = registryKey("amethyst");
    public static final ResourceKey<TrimMaterial> RESIN = registryKey("resin");

    public static void bootstrap(BootstrapContext<TrimMaterial> p_368813_) {
        register(p_368813_, QUARTZ, Items.QUARTZ, Style.EMPTY.withColor(14931140));
        register(p_368813_, IRON, Items.IRON_INGOT, Style.EMPTY.withColor(15527148), Map.of(EquipmentAssets.IRON, "iron_darker"));
        register(p_368813_, NETHERITE, Items.NETHERITE_INGOT, Style.EMPTY.withColor(6445145), Map.of(EquipmentAssets.NETHERITE, "netherite_darker"));
        register(p_368813_, REDSTONE, Items.REDSTONE, Style.EMPTY.withColor(9901575));
        register(p_368813_, COPPER, Items.COPPER_INGOT, Style.EMPTY.withColor(11823181));
        register(p_368813_, GOLD, Items.GOLD_INGOT, Style.EMPTY.withColor(14594349), Map.of(EquipmentAssets.GOLD, "gold_darker"));
        register(p_368813_, EMERALD, Items.EMERALD, Style.EMPTY.withColor(1155126));
        register(p_368813_, DIAMOND, Items.DIAMOND, Style.EMPTY.withColor(7269586), Map.of(EquipmentAssets.DIAMOND, "diamond_darker"));
        register(p_368813_, LAPIS, Items.LAPIS_LAZULI, Style.EMPTY.withColor(4288151));
        register(p_368813_, AMETHYST, Items.AMETHYST_SHARD, Style.EMPTY.withColor(10116294));
        register(p_368813_, RESIN, Items.RESIN_BRICK, Style.EMPTY.withColor(16545810));
    }

    public static Optional<Holder.Reference<TrimMaterial>> getFromIngredient(HolderLookup.Provider p_363557_, ItemStack p_369735_) {
        return p_363557_.lookupOrThrow(Registries.TRIM_MATERIAL).listElements().filter(p_361384_ -> p_369735_.is(p_361384_.value().ingredient())).findFirst();
    }

    private static void register(BootstrapContext<TrimMaterial> p_369807_, ResourceKey<TrimMaterial> p_365636_, Item p_360710_, Style p_361695_) {
        register(p_369807_, p_365636_, p_360710_, p_361695_, Map.of());
    }

    private static void register(
        BootstrapContext<TrimMaterial> p_367478_,
        ResourceKey<TrimMaterial> p_366748_,
        Item p_365449_,
        Style p_363590_,
        Map<ResourceKey<EquipmentAsset>, String> p_376985_
    ) {
        TrimMaterial trimmaterial = TrimMaterial.create(
            p_366748_.location().getPath(),
            p_365449_,
            Component.translatable(Util.makeDescriptionId("trim_material", p_366748_.location())).withStyle(p_363590_),
            p_376985_
        );
        p_367478_.register(p_366748_, trimmaterial);
    }

    private static ResourceKey<TrimMaterial> registryKey(String p_360778_) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, ResourceLocation.withDefaultNamespace(p_360778_));
    }
}