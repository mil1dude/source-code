package net.minecraft.world.entity.decoration;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class PaintingVariants {
    public static final ResourceKey<PaintingVariant> KEBAB = create("kebab");
    public static final ResourceKey<PaintingVariant> AZTEC = create("aztec");
    public static final ResourceKey<PaintingVariant> ALBAN = create("alban");
    public static final ResourceKey<PaintingVariant> AZTEC2 = create("aztec2");
    public static final ResourceKey<PaintingVariant> BOMB = create("bomb");
    public static final ResourceKey<PaintingVariant> PLANT = create("plant");
    public static final ResourceKey<PaintingVariant> WASTELAND = create("wasteland");
    public static final ResourceKey<PaintingVariant> POOL = create("pool");
    public static final ResourceKey<PaintingVariant> COURBET = create("courbet");
    public static final ResourceKey<PaintingVariant> SEA = create("sea");
    public static final ResourceKey<PaintingVariant> SUNSET = create("sunset");
    public static final ResourceKey<PaintingVariant> CREEBET = create("creebet");
    public static final ResourceKey<PaintingVariant> WANDERER = create("wanderer");
    public static final ResourceKey<PaintingVariant> GRAHAM = create("graham");
    public static final ResourceKey<PaintingVariant> MATCH = create("match");
    public static final ResourceKey<PaintingVariant> BUST = create("bust");
    public static final ResourceKey<PaintingVariant> STAGE = create("stage");
    public static final ResourceKey<PaintingVariant> VOID = create("void");
    public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = create("skull_and_roses");
    public static final ResourceKey<PaintingVariant> WITHER = create("wither");
    public static final ResourceKey<PaintingVariant> FIGHTERS = create("fighters");
    public static final ResourceKey<PaintingVariant> POINTER = create("pointer");
    public static final ResourceKey<PaintingVariant> PIGSCENE = create("pigscene");
    public static final ResourceKey<PaintingVariant> BURNING_SKULL = create("burning_skull");
    public static final ResourceKey<PaintingVariant> SKELETON = create("skeleton");
    public static final ResourceKey<PaintingVariant> DONKEY_KONG = create("donkey_kong");
    public static final ResourceKey<PaintingVariant> EARTH = create("earth");
    public static final ResourceKey<PaintingVariant> WIND = create("wind");
    public static final ResourceKey<PaintingVariant> WATER = create("water");
    public static final ResourceKey<PaintingVariant> FIRE = create("fire");
    public static final ResourceKey<PaintingVariant> BAROQUE = create("baroque");
    public static final ResourceKey<PaintingVariant> HUMBLE = create("humble");
    public static final ResourceKey<PaintingVariant> MEDITATIVE = create("meditative");
    public static final ResourceKey<PaintingVariant> PRAIRIE_RIDE = create("prairie_ride");
    public static final ResourceKey<PaintingVariant> UNPACKED = create("unpacked");
    public static final ResourceKey<PaintingVariant> BACKYARD = create("backyard");
    public static final ResourceKey<PaintingVariant> BOUQUET = create("bouquet");
    public static final ResourceKey<PaintingVariant> CAVEBIRD = create("cavebird");
    public static final ResourceKey<PaintingVariant> CHANGING = create("changing");
    public static final ResourceKey<PaintingVariant> COTAN = create("cotan");
    public static final ResourceKey<PaintingVariant> ENDBOSS = create("endboss");
    public static final ResourceKey<PaintingVariant> FERN = create("fern");
    public static final ResourceKey<PaintingVariant> FINDING = create("finding");
    public static final ResourceKey<PaintingVariant> LOWMIST = create("lowmist");
    public static final ResourceKey<PaintingVariant> ORB = create("orb");
    public static final ResourceKey<PaintingVariant> OWLEMONS = create("owlemons");
    public static final ResourceKey<PaintingVariant> PASSAGE = create("passage");
    public static final ResourceKey<PaintingVariant> POND = create("pond");
    public static final ResourceKey<PaintingVariant> SUNFLOWERS = create("sunflowers");
    public static final ResourceKey<PaintingVariant> TIDES = create("tides");

    public static void bootstrap(BootstrapContext<PaintingVariant> p_342103_) {
        register(p_342103_, KEBAB, 1, 1);
        register(p_342103_, AZTEC, 1, 1);
        register(p_342103_, ALBAN, 1, 1);
        register(p_342103_, AZTEC2, 1, 1);
        register(p_342103_, BOMB, 1, 1);
        register(p_342103_, PLANT, 1, 1);
        register(p_342103_, WASTELAND, 1, 1);
        register(p_342103_, POOL, 2, 1);
        register(p_342103_, COURBET, 2, 1);
        register(p_342103_, SEA, 2, 1);
        register(p_342103_, SUNSET, 2, 1);
        register(p_342103_, CREEBET, 2, 1);
        register(p_342103_, WANDERER, 1, 2);
        register(p_342103_, GRAHAM, 1, 2);
        register(p_342103_, MATCH, 2, 2);
        register(p_342103_, BUST, 2, 2);
        register(p_342103_, STAGE, 2, 2);
        register(p_342103_, VOID, 2, 2);
        register(p_342103_, SKULL_AND_ROSES, 2, 2);
        register(p_342103_, WITHER, 2, 2, false);
        register(p_342103_, FIGHTERS, 4, 2);
        register(p_342103_, POINTER, 4, 4);
        register(p_342103_, PIGSCENE, 4, 4);
        register(p_342103_, BURNING_SKULL, 4, 4);
        register(p_342103_, SKELETON, 4, 3);
        register(p_342103_, EARTH, 2, 2, false);
        register(p_342103_, WIND, 2, 2, false);
        register(p_342103_, WATER, 2, 2, false);
        register(p_342103_, FIRE, 2, 2, false);
        register(p_342103_, DONKEY_KONG, 4, 3);
        register(p_342103_, BAROQUE, 2, 2);
        register(p_342103_, HUMBLE, 2, 2);
        register(p_342103_, MEDITATIVE, 1, 1);
        register(p_342103_, PRAIRIE_RIDE, 1, 2);
        register(p_342103_, UNPACKED, 4, 4);
        register(p_342103_, BACKYARD, 3, 4);
        register(p_342103_, BOUQUET, 3, 3);
        register(p_342103_, CAVEBIRD, 3, 3);
        register(p_342103_, CHANGING, 4, 2);
        register(p_342103_, COTAN, 3, 3);
        register(p_342103_, ENDBOSS, 3, 3);
        register(p_342103_, FERN, 3, 3);
        register(p_342103_, FINDING, 4, 2);
        register(p_342103_, LOWMIST, 4, 2);
        register(p_342103_, ORB, 4, 4);
        register(p_342103_, OWLEMONS, 3, 3);
        register(p_342103_, PASSAGE, 4, 2);
        register(p_342103_, POND, 3, 4);
        register(p_342103_, SUNFLOWERS, 3, 3);
        register(p_342103_, TIDES, 3, 3);
    }

    private static void register(BootstrapContext<PaintingVariant> p_343708_, ResourceKey<PaintingVariant> p_344787_, int p_342193_, int p_342653_) {
        register(p_343708_, p_344787_, p_342193_, p_342653_, true);
    }

    private static void register(
        BootstrapContext<PaintingVariant> p_367848_, ResourceKey<PaintingVariant> p_364046_, int p_362569_, int p_366442_, boolean p_366548_
    ) {
        p_367848_.register(
            p_364046_,
            new PaintingVariant(
                p_362569_,
                p_366442_,
                p_364046_.location(),
                Optional.of(Component.translatable(p_364046_.location().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW)),
                p_366548_
                    ? Optional.of(Component.translatable(p_364046_.location().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY))
                    : Optional.empty()
            )
        );
    }

    private static ResourceKey<PaintingVariant> create(String p_218945_) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, ResourceLocation.withDefaultNamespace(p_218945_));
    }
}