package net.minecraft.world.item.equipment.trim;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TrimPatterns {
    public static final ResourceKey<TrimPattern> SENTRY = registryKey("sentry");
    public static final ResourceKey<TrimPattern> DUNE = registryKey("dune");
    public static final ResourceKey<TrimPattern> COAST = registryKey("coast");
    public static final ResourceKey<TrimPattern> WILD = registryKey("wild");
    public static final ResourceKey<TrimPattern> WARD = registryKey("ward");
    public static final ResourceKey<TrimPattern> EYE = registryKey("eye");
    public static final ResourceKey<TrimPattern> VEX = registryKey("vex");
    public static final ResourceKey<TrimPattern> TIDE = registryKey("tide");
    public static final ResourceKey<TrimPattern> SNOUT = registryKey("snout");
    public static final ResourceKey<TrimPattern> RIB = registryKey("rib");
    public static final ResourceKey<TrimPattern> SPIRE = registryKey("spire");
    public static final ResourceKey<TrimPattern> WAYFINDER = registryKey("wayfinder");
    public static final ResourceKey<TrimPattern> SHAPER = registryKey("shaper");
    public static final ResourceKey<TrimPattern> SILENCE = registryKey("silence");
    public static final ResourceKey<TrimPattern> RAISER = registryKey("raiser");
    public static final ResourceKey<TrimPattern> HOST = registryKey("host");
    public static final ResourceKey<TrimPattern> FLOW = registryKey("flow");
    public static final ResourceKey<TrimPattern> BOLT = registryKey("bolt");

    public static void bootstrap(BootstrapContext<TrimPattern> p_362921_) {
        register(p_362921_, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, SENTRY);
        register(p_362921_, Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, DUNE);
        register(p_362921_, Items.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, COAST);
        register(p_362921_, Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, WILD);
        register(p_362921_, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, WARD);
        register(p_362921_, Items.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, EYE);
        register(p_362921_, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, VEX);
        register(p_362921_, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, TIDE);
        register(p_362921_, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, SNOUT);
        register(p_362921_, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, RIB);
        register(p_362921_, Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, SPIRE);
        register(p_362921_, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, WAYFINDER);
        register(p_362921_, Items.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, SHAPER);
        register(p_362921_, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, SILENCE);
        register(p_362921_, Items.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, RAISER);
        register(p_362921_, Items.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, HOST);
        register(p_362921_, Items.FLOW_ARMOR_TRIM_SMITHING_TEMPLATE, FLOW);
        register(p_362921_, Items.BOLT_ARMOR_TRIM_SMITHING_TEMPLATE, BOLT);
    }

    public static Optional<Holder.Reference<TrimPattern>> getFromTemplate(HolderLookup.Provider p_362285_, ItemStack p_367554_) {
        return p_362285_.lookupOrThrow(Registries.TRIM_PATTERN).listElements().filter(p_369646_ -> p_367554_.is(p_369646_.value().templateItem())).findFirst();
    }

    public static void register(BootstrapContext<TrimPattern> p_363436_, Item p_361552_, ResourceKey<TrimPattern> p_366846_) {
        TrimPattern trimpattern = new TrimPattern(
            p_366846_.location(),
            BuiltInRegistries.ITEM.wrapAsHolder(p_361552_),
            Component.translatable(Util.makeDescriptionId("trim_pattern", p_366846_.location())),
            false
        );
        p_363436_.register(p_366846_, trimpattern);
    }

    private static ResourceKey<TrimPattern> registryKey(String p_368467_) {
        return ResourceKey.create(Registries.TRIM_PATTERN, ResourceLocation.withDefaultNamespace(p_368467_));
    }
}