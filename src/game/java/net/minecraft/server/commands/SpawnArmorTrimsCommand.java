package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import net.minecraft.world.level.Level;

public class SpawnArmorTrimsCommand {
    private static final List<ResourceKey<TrimPattern>> VANILLA_TRIM_PATTERNS = List.of(
        TrimPatterns.SENTRY,
        TrimPatterns.DUNE,
        TrimPatterns.COAST,
        TrimPatterns.WILD,
        TrimPatterns.WARD,
        TrimPatterns.EYE,
        TrimPatterns.VEX,
        TrimPatterns.TIDE,
        TrimPatterns.SNOUT,
        TrimPatterns.RIB,
        TrimPatterns.SPIRE,
        TrimPatterns.WAYFINDER,
        TrimPatterns.SHAPER,
        TrimPatterns.SILENCE,
        TrimPatterns.RAISER,
        TrimPatterns.HOST,
        TrimPatterns.FLOW,
        TrimPatterns.BOLT
    );
    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of(
        TrimMaterials.QUARTZ,
        TrimMaterials.IRON,
        TrimMaterials.NETHERITE,
        TrimMaterials.REDSTONE,
        TrimMaterials.COPPER,
        TrimMaterials.GOLD,
        TrimMaterials.EMERALD,
        TrimMaterials.DIAMOND,
        TrimMaterials.LAPIS,
        TrimMaterials.AMETHYST,
        TrimMaterials.RESIN
    );
    private static final ToIntFunction<ResourceKey<TrimPattern>> TRIM_PATTERN_ORDER = Util.createIndexLookup(VANILLA_TRIM_PATTERNS);
    private static final ToIntFunction<ResourceKey<TrimMaterial>> TRIM_MATERIAL_ORDER = Util.createIndexLookup(VANILLA_TRIM_MATERIALS);

    public static void register(CommandDispatcher<CommandSourceStack> p_266758_) {
        p_266758_.register(
            Commands.literal("spawn_armor_trims")
                .requires(p_277270_ -> p_277270_.hasPermission(2))
                .executes(p_267005_ -> spawnArmorTrims(p_267005_.getSource(), p_267005_.getSource().getPlayerOrException()))
        );
    }

    private static int spawnArmorTrims(CommandSourceStack p_266993_, Player p_266983_) {
        Level level = p_266983_.level();
        NonNullList<ArmorTrim> nonnulllist = NonNullList.create();
        Registry<TrimPattern> registry = level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
        Registry<TrimMaterial> registry1 = level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
        HolderLookup<Item> holderlookup = level.holderLookup(Registries.ITEM);
        Map<ResourceKey<EquipmentAsset>, List<Item>> map = holderlookup.listElements().map(Holder.Reference::value).filter(p_374876_ -> {
            Equippable equippable1 = p_374876_.components().get(DataComponents.EQUIPPABLE);
            return equippable1 != null && equippable1.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR && equippable1.assetId().isPresent();
        }).collect(Collectors.groupingBy(p_374877_ -> p_374877_.components().get(DataComponents.EQUIPPABLE).assetId().get()));
        registry.stream()
            .sorted(Comparator.comparing(p_366237_ -> TRIM_PATTERN_ORDER.applyAsInt(registry.getResourceKey(p_366237_).orElse(null))))
            .forEachOrdered(
                p_361579_ -> registry1.stream()
                        .sorted(Comparator.comparing(p_365493_ -> TRIM_MATERIAL_ORDER.applyAsInt(registry1.getResourceKey(p_365493_).orElse(null))))
                        .forEachOrdered(p_358630_ -> nonnulllist.add(new ArmorTrim(registry1.wrapAsHolder(p_358630_), registry.wrapAsHolder(p_361579_))))
            );
        BlockPos blockpos = p_266983_.blockPosition().relative(p_266983_.getDirection(), 5);
        int i = map.size() - 1;
        double d0 = 3.0;
        int j = 0;
        int k = 0;

        for (ArmorTrim armortrim : nonnulllist) {
            for (List<Item> list : map.values()) {
                double d1 = (double)blockpos.getX() + 0.5 - (double)(j % registry1.size()) * 3.0;
                double d2 = (double)blockpos.getY() + 0.5 + (double)(k % i) * 3.0;
                double d3 = (double)blockpos.getZ() + 0.5 + (double)(j / registry1.size() * 10);
                ArmorStand armorstand = new ArmorStand(level, d1, d2, d3);
                armorstand.setYRot(180.0F);
                armorstand.setNoGravity(true);

                for (Item item : list) {
                    Equippable equippable = Objects.requireNonNull(item.components().get(DataComponents.EQUIPPABLE));
                    ItemStack itemstack = new ItemStack(item);
                    itemstack.set(DataComponents.TRIM, armortrim);
                    armorstand.setItemSlot(equippable.slot(), itemstack);
                    if (itemstack.is(Items.TURTLE_HELMET)) {
                        armorstand.setCustomName(
                            armortrim.pattern()
                                .value()
                                .copyWithStyle(armortrim.material())
                                .copy()
                                .append(" ")
                                .append(armortrim.material().value().description())
                        );
                        armorstand.setCustomNameVisible(true);
                    } else {
                        armorstand.setInvisible(true);
                    }
                }

                level.addFreshEntity(armorstand);
                k++;
            }

            j++;
        }

        p_266993_.sendSuccess(() -> Component.literal("Armorstands with trimmed armor spawned around you"), true);
        return 1;
    }
}