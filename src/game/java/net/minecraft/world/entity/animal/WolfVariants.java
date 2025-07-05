package net.minecraft.world.entity.animal;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class WolfVariants {
    public static final ResourceKey<WolfVariant> PALE = createKey("pale");
    public static final ResourceKey<WolfVariant> SPOTTED = createKey("spotted");
    public static final ResourceKey<WolfVariant> SNOWY = createKey("snowy");
    public static final ResourceKey<WolfVariant> BLACK = createKey("black");
    public static final ResourceKey<WolfVariant> ASHEN = createKey("ashen");
    public static final ResourceKey<WolfVariant> RUSTY = createKey("rusty");
    public static final ResourceKey<WolfVariant> WOODS = createKey("woods");
    public static final ResourceKey<WolfVariant> CHESTNUT = createKey("chestnut");
    public static final ResourceKey<WolfVariant> STRIPED = createKey("striped");
    public static final ResourceKey<WolfVariant> DEFAULT = PALE;

    private static ResourceKey<WolfVariant> createKey(String p_335110_) {
        return ResourceKey.create(Registries.WOLF_VARIANT, ResourceLocation.withDefaultNamespace(p_335110_));
    }

    static void register(BootstrapContext<WolfVariant> p_328632_, ResourceKey<WolfVariant> p_331459_, String p_329414_, ResourceKey<Biome> p_332564_) {
        register(p_328632_, p_331459_, p_329414_, HolderSet.direct(p_328632_.lookup(Registries.BIOME).getOrThrow(p_332564_)));
    }

    static void register(BootstrapContext<WolfVariant> p_334941_, ResourceKey<WolfVariant> p_335312_, String p_334468_, TagKey<Biome> p_335491_) {
        register(p_334941_, p_335312_, p_334468_, p_334941_.lookup(Registries.BIOME).getOrThrow(p_335491_));
    }

    static void register(BootstrapContext<WolfVariant> p_332159_, ResourceKey<WolfVariant> p_330575_, String p_333153_, HolderSet<Biome> p_334914_) {
        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace("entity/wolf/" + p_333153_);
        ResourceLocation resourcelocation1 = ResourceLocation.withDefaultNamespace("entity/wolf/" + p_333153_ + "_tame");
        ResourceLocation resourcelocation2 = ResourceLocation.withDefaultNamespace("entity/wolf/" + p_333153_ + "_angry");
        p_332159_.register(p_330575_, new WolfVariant(resourcelocation, resourcelocation1, resourcelocation2, p_334914_));
    }

    public static Holder<WolfVariant> getSpawnVariant(RegistryAccess p_330241_, Holder<Biome> p_331959_) {
        Registry<WolfVariant> registry = p_330241_.lookupOrThrow(Registries.WOLF_VARIANT);
        return registry.listElements()
            .filter(p_329793_ -> p_329793_.value().biomes().contains(p_331959_))
            .findFirst()
            .or(() -> registry.get(DEFAULT))
            .or(registry::getAny)
            .orElseThrow();
    }

    public static void bootstrap(BootstrapContext<WolfVariant> p_332045_) {
        register(p_332045_, PALE, "wolf", Biomes.TAIGA);
        register(p_332045_, SPOTTED, "wolf_spotted", BiomeTags.IS_SAVANNA);
        register(p_332045_, SNOWY, "wolf_snowy", Biomes.GROVE);
        register(p_332045_, BLACK, "wolf_black", Biomes.OLD_GROWTH_PINE_TAIGA);
        register(p_332045_, ASHEN, "wolf_ashen", Biomes.SNOWY_TAIGA);
        register(p_332045_, RUSTY, "wolf_rusty", BiomeTags.IS_JUNGLE);
        register(p_332045_, WOODS, "wolf_woods", Biomes.FOREST);
        register(p_332045_, CHESTNUT, "wolf_chestnut", Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        register(p_332045_, STRIPED, "wolf_striped", BiomeTags.IS_BADLANDS);
    }
}