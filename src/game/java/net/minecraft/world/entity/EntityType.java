package net.minecraft.world.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Bogged;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.entity.projectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.ChestRaft;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import net.minecraft.world.entity.vehicle.MinecartHopper;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.entity.vehicle.Raft;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class EntityType<T extends Entity> implements FeatureElement, EntityTypeTest<Entity, T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Holder.Reference<EntityType<?>> builtInRegistryHolder = BuiltInRegistries.ENTITY_TYPE.createIntrusiveHolder(this);
    private static final float MAGIC_HORSE_WIDTH = 1.3964844F;
    private static final int DISPLAY_TRACKING_RANGE = 10;
    public static final EntityType<Boat> ACACIA_BOAT = register(
        "acacia_boat",
        EntityType.Builder.of(boatFactory(() -> Items.ACACIA_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> ACACIA_CHEST_BOAT = register(
        "acacia_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.ACACIA_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Allay> ALLAY = register(
        "allay", EntityType.Builder.of(Allay::new, MobCategory.CREATURE).sized(0.35F, 0.6F).eyeHeight(0.36F).ridingOffset(0.04F).clientTrackingRange(8).updateInterval(2)
    );
    public static final EntityType<AreaEffectCloud> AREA_EFFECT_CLOUD = register(
        "area_effect_cloud",
        EntityType.Builder.<AreaEffectCloud>of(AreaEffectCloud::new, MobCategory.MISC)
            .noLootTable()
            .fireImmune()
            .sized(6.0F, 0.5F)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
    );
    public static final EntityType<Armadillo> ARMADILLO = register(
        "armadillo", EntityType.Builder.of(Armadillo::new, MobCategory.CREATURE).sized(0.7F, 0.65F).eyeHeight(0.26F).clientTrackingRange(10)
    );
    public static final EntityType<ArmorStand> ARMOR_STAND = register(
        "armor_stand", EntityType.Builder.<ArmorStand>of(ArmorStand::new, MobCategory.MISC).sized(0.5F, 1.975F).eyeHeight(1.7775F).clientTrackingRange(10)
    );
    public static final EntityType<Arrow> ARROW = register(
        "arrow", EntityType.Builder.<Arrow>of(Arrow::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.5F).eyeHeight(0.13F).clientTrackingRange(4).updateInterval(20)
    );
    public static final EntityType<Axolotl> AXOLOTL = register(
        "axolotl", EntityType.Builder.of(Axolotl::new, MobCategory.AXOLOTLS).sized(0.75F, 0.42F).eyeHeight(0.2751F).clientTrackingRange(10)
    );
    public static final EntityType<ChestRaft> BAMBOO_CHEST_RAFT = register(
        "bamboo_chest_raft",
        EntityType.Builder.of(chestRaftFactory(() -> Items.BAMBOO_CHEST_RAFT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Raft> BAMBOO_RAFT = register(
        "bamboo_raft",
        EntityType.Builder.of(raftFactory(() -> Items.BAMBOO_RAFT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Bat> BAT = register(
        "bat", EntityType.Builder.of(Bat::new, MobCategory.AMBIENT).sized(0.5F, 0.9F).eyeHeight(0.45F).clientTrackingRange(5)
    );
    public static final EntityType<Bee> BEE = register(
        "bee", EntityType.Builder.of(Bee::new, MobCategory.CREATURE).sized(0.7F, 0.6F).eyeHeight(0.3F).clientTrackingRange(8)
    );
    public static final EntityType<Boat> BIRCH_BOAT = register(
        "birch_boat",
        EntityType.Builder.of(boatFactory(() -> Items.BIRCH_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> BIRCH_CHEST_BOAT = register(
        "birch_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.BIRCH_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Blaze> BLAZE = register(
        "blaze", EntityType.Builder.of(Blaze::new, MobCategory.MONSTER).fireImmune().sized(0.6F, 1.8F).clientTrackingRange(8)
    );
    public static final EntityType<Display.BlockDisplay> BLOCK_DISPLAY = register(
        "block_display", EntityType.Builder.of(Display.BlockDisplay::new, MobCategory.MISC).noLootTable().sized(0.0F, 0.0F).clientTrackingRange(10).updateInterval(1)
    );
    public static final EntityType<Bogged> BOGGED = register(
        "bogged", EntityType.Builder.of(Bogged::new, MobCategory.MONSTER).sized(0.6F, 1.99F).eyeHeight(1.74F).ridingOffset(-0.7F).clientTrackingRange(8)
    );
    public static final EntityType<Breeze> BREEZE = register(
        "breeze", EntityType.Builder.of(Breeze::new, MobCategory.MONSTER).sized(0.6F, 1.77F).eyeHeight(1.3452F).clientTrackingRange(10)
    );
    public static final EntityType<BreezeWindCharge> BREEZE_WIND_CHARGE = register(
        "breeze_wind_charge",
        EntityType.Builder.<BreezeWindCharge>of(BreezeWindCharge::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.3125F, 0.3125F)
            .eyeHeight(0.0F)
            .clientTrackingRange(4)
            .updateInterval(10)
    );
    public static final EntityType<Camel> CAMEL = register(
        "camel", EntityType.Builder.of(Camel::new, MobCategory.CREATURE).sized(1.7F, 2.375F).eyeHeight(2.275F).clientTrackingRange(10)
    );
    public static final EntityType<Cat> CAT = register(
        "cat", EntityType.Builder.of(Cat::new, MobCategory.CREATURE).sized(0.6F, 0.7F).eyeHeight(0.35F).passengerAttachments(0.5125F).clientTrackingRange(8)
    );
    public static final EntityType<CaveSpider> CAVE_SPIDER = register(
        "cave_spider", EntityType.Builder.of(CaveSpider::new, MobCategory.MONSTER).sized(0.7F, 0.5F).eyeHeight(0.45F).clientTrackingRange(8)
    );
    public static final EntityType<Boat> CHERRY_BOAT = register(
        "cherry_boat",
        EntityType.Builder.of(boatFactory(() -> Items.CHERRY_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> CHERRY_CHEST_BOAT = register(
        "cherry_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.CHERRY_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<MinecartChest> CHEST_MINECART = register(
        "chest_minecart", EntityType.Builder.of(MinecartChest::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8)
    );
    public static final EntityType<Chicken> CHICKEN = register(
        "chicken",
        EntityType.Builder.of(Chicken::new, MobCategory.CREATURE).sized(0.4F, 0.7F).eyeHeight(0.644F).passengerAttachments(new Vec3(0.0, 0.7, -0.1)).clientTrackingRange(10)
    );
    public static final EntityType<Cod> COD = register(
        "cod", EntityType.Builder.of(Cod::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.3F).eyeHeight(0.195F).clientTrackingRange(4)
    );
    public static final EntityType<MinecartCommandBlock> COMMAND_BLOCK_MINECART = register(
        "command_block_minecart",
        EntityType.Builder.of(MinecartCommandBlock::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8)
    );
    public static final EntityType<Cow> COW = register(
        "cow", EntityType.Builder.of(Cow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).eyeHeight(1.3F).passengerAttachments(1.36875F).clientTrackingRange(10)
    );
    public static final EntityType<Creaking> CREAKING = register(
        "creaking", EntityType.Builder.of(Creaking::new, MobCategory.MONSTER).sized(0.9F, 2.7F).eyeHeight(2.3F).clientTrackingRange(8)
    );
    public static final EntityType<Creeper> CREEPER = register(
        "creeper", EntityType.Builder.of(Creeper::new, MobCategory.MONSTER).sized(0.6F, 1.7F).clientTrackingRange(8)
    );
    public static final EntityType<Boat> DARK_OAK_BOAT = register(
        "dark_oak_boat",
        EntityType.Builder.of(boatFactory(() -> Items.DARK_OAK_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> DARK_OAK_CHEST_BOAT = register(
        "dark_oak_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.DARK_OAK_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Dolphin> DOLPHIN = register(
        "dolphin", EntityType.Builder.of(Dolphin::new, MobCategory.WATER_CREATURE).sized(0.9F, 0.6F).eyeHeight(0.3F)
    );
    public static final EntityType<Donkey> DONKEY = register(
        "donkey", EntityType.Builder.of(Donkey::new, MobCategory.CREATURE).sized(1.3964844F, 1.5F).eyeHeight(1.425F).passengerAttachments(1.1125F).clientTrackingRange(10)
    );
    public static final EntityType<DragonFireball> DRAGON_FIREBALL = register(
        "dragon_fireball",
        EntityType.Builder.<DragonFireball>of(DragonFireball::new, MobCategory.MISC).noLootTable().sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<Drowned> DROWNED = register(
        "drowned",
        EntityType.Builder.of(Drowned::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.0125F).ridingOffset(-0.7F).clientTrackingRange(8)
    );
    public static final EntityType<ThrownEgg> EGG = register(
        "egg", EntityType.Builder.<ThrownEgg>of(ThrownEgg::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<ElderGuardian> ELDER_GUARDIAN = register(
        "elder_guardian",
        EntityType.Builder.of(ElderGuardian::new, MobCategory.MONSTER).sized(1.9975F, 1.9975F).eyeHeight(0.99875F).passengerAttachments(2.350625F).clientTrackingRange(10)
    );
    public static final EntityType<EnderMan> ENDERMAN = register(
        "enderman", EntityType.Builder.of(EnderMan::new, MobCategory.MONSTER).sized(0.6F, 2.9F).eyeHeight(2.55F).passengerAttachments(2.80625F).clientTrackingRange(8)
    );
    public static final EntityType<Endermite> ENDERMITE = register(
        "endermite", EntityType.Builder.of(Endermite::new, MobCategory.MONSTER).sized(0.4F, 0.3F).eyeHeight(0.13F).passengerAttachments(0.2375F).clientTrackingRange(8)
    );
    public static final EntityType<EnderDragon> ENDER_DRAGON = register(
        "ender_dragon", EntityType.Builder.of(EnderDragon::new, MobCategory.MONSTER).fireImmune().sized(16.0F, 8.0F).passengerAttachments(3.0F).clientTrackingRange(10)
    );
    public static final EntityType<ThrownEnderpearl> ENDER_PEARL = register(
        "ender_pearl",
        EntityType.Builder.<ThrownEnderpearl>of(ThrownEnderpearl::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<EndCrystal> END_CRYSTAL = register(
        "end_crystal",
        EntityType.Builder.<EndCrystal>of(EndCrystal::new, MobCategory.MISC)
            .noLootTable()
            .fireImmune()
            .sized(2.0F, 2.0F)
            .clientTrackingRange(16)
            .updateInterval(Integer.MAX_VALUE)
    );
    public static final EntityType<Evoker> EVOKER = register(
        "evoker", EntityType.Builder.of(Evoker::new, MobCategory.MONSTER).sized(0.6F, 1.95F).passengerAttachments(2.0F).ridingOffset(-0.6F).clientTrackingRange(8)
    );
    public static final EntityType<EvokerFangs> EVOKER_FANGS = register(
        "evoker_fangs", EntityType.Builder.<EvokerFangs>of(EvokerFangs::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.8F).clientTrackingRange(6).updateInterval(2)
    );
    public static final EntityType<ThrownExperienceBottle> EXPERIENCE_BOTTLE = register(
        "experience_bottle",
        EntityType.Builder.<ThrownExperienceBottle>of(ThrownExperienceBottle::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
    );
    public static final EntityType<ExperienceOrb> EXPERIENCE_ORB = register(
        "experience_orb",
        EntityType.Builder.<ExperienceOrb>of(ExperienceOrb::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(20)
    );
    public static final EntityType<EyeOfEnder> EYE_OF_ENDER = register(
        "eye_of_ender", EntityType.Builder.<EyeOfEnder>of(EyeOfEnder::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(4)
    );
    public static final EntityType<FallingBlockEntity> FALLING_BLOCK = register(
        "falling_block",
        EntityType.Builder.<FallingBlockEntity>of(FallingBlockEntity::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(20)
    );
    public static final EntityType<LargeFireball> FIREBALL = register(
        "fireball", EntityType.Builder.<LargeFireball>of(LargeFireball::new, MobCategory.MISC).noLootTable().sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<FireworkRocketEntity> FIREWORK_ROCKET = register(
        "firework_rocket",
        EntityType.Builder.<FireworkRocketEntity>of(FireworkRocketEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
    );
    public static final EntityType<Fox> FOX = register(
        "fox",
        EntityType.Builder.of(Fox::new, MobCategory.CREATURE)
            .sized(0.6F, 0.7F)
            .eyeHeight(0.4F)
            .passengerAttachments(new Vec3(0.0, 0.6375, -0.25))
            .clientTrackingRange(8)
            .immuneTo(Blocks.SWEET_BERRY_BUSH)
    );
    public static final EntityType<Frog> FROG = register(
        "frog", EntityType.Builder.of(Frog::new, MobCategory.CREATURE).sized(0.5F, 0.5F).passengerAttachments(new Vec3(0.0, 0.375, -0.25)).clientTrackingRange(10)
    );
    public static final EntityType<MinecartFurnace> FURNACE_MINECART = register(
        "furnace_minecart",
        EntityType.Builder.of(MinecartFurnace::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8)
    );
    public static final EntityType<Ghast> GHAST = register(
        "ghast",
        EntityType.Builder.of(Ghast::new, MobCategory.MONSTER)
            .fireImmune()
            .sized(4.0F, 4.0F)
            .eyeHeight(2.6F)
            .passengerAttachments(4.0625F)
            .ridingOffset(0.5F)
            .clientTrackingRange(10)
    );
    public static final EntityType<Giant> GIANT = register(
        "giant", EntityType.Builder.of(Giant::new, MobCategory.MONSTER).sized(3.6F, 12.0F).eyeHeight(10.44F).ridingOffset(-3.75F).clientTrackingRange(10)
    );
    public static final EntityType<GlowItemFrame> GLOW_ITEM_FRAME = register(
        "glow_item_frame",
        EntityType.Builder.<GlowItemFrame>of(GlowItemFrame::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.0F)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
    );
    public static final EntityType<GlowSquid> GLOW_SQUID = register(
        "glow_squid", EntityType.Builder.of(GlowSquid::new, MobCategory.UNDERGROUND_WATER_CREATURE).sized(0.8F, 0.8F).eyeHeight(0.4F).clientTrackingRange(10)
    );
    public static final EntityType<Goat> GOAT = register(
        "goat", EntityType.Builder.of(Goat::new, MobCategory.CREATURE).sized(0.9F, 1.3F).passengerAttachments(1.1125F).clientTrackingRange(10)
    );
    public static final EntityType<Guardian> GUARDIAN = register(
        "guardian", EntityType.Builder.of(Guardian::new, MobCategory.MONSTER).sized(0.85F, 0.85F).eyeHeight(0.425F).passengerAttachments(0.975F).clientTrackingRange(8)
    );
    public static final EntityType<Hoglin> HOGLIN = register(
        "hoglin", EntityType.Builder.of(Hoglin::new, MobCategory.MONSTER).sized(1.3964844F, 1.4F).passengerAttachments(1.49375F).clientTrackingRange(8)
    );
    public static final EntityType<MinecartHopper> HOPPER_MINECART = register(
        "hopper_minecart", EntityType.Builder.of(MinecartHopper::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8)
    );
    public static final EntityType<Horse> HORSE = register(
        "horse", EntityType.Builder.of(Horse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).eyeHeight(1.52F).passengerAttachments(1.44375F).clientTrackingRange(10)
    );
    public static final EntityType<Husk> HUSK = register(
        "husk",
        EntityType.Builder.of(Husk::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F).passengerAttachments(2.075F).ridingOffset(-0.7F).clientTrackingRange(8)
    );
    public static final EntityType<Illusioner> ILLUSIONER = register(
        "illusioner", EntityType.Builder.of(Illusioner::new, MobCategory.MONSTER).sized(0.6F, 1.95F).passengerAttachments(2.0F).ridingOffset(-0.6F).clientTrackingRange(8)
    );
    public static final EntityType<Interaction> INTERACTION = register(
        "interaction", EntityType.Builder.of(Interaction::new, MobCategory.MISC).noLootTable().sized(0.0F, 0.0F).clientTrackingRange(10)
    );
    public static final EntityType<IronGolem> IRON_GOLEM = register(
        "iron_golem", EntityType.Builder.of(IronGolem::new, MobCategory.MISC).sized(1.4F, 2.7F).clientTrackingRange(10)
    );
    public static final EntityType<ItemEntity> ITEM = register(
        "item",
        EntityType.Builder.<ItemEntity>of(ItemEntity::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.25F, 0.25F)
            .eyeHeight(0.2125F)
            .clientTrackingRange(6)
            .updateInterval(20)
    );
    public static final EntityType<Display.ItemDisplay> ITEM_DISPLAY = register(
        "item_display", EntityType.Builder.of(Display.ItemDisplay::new, MobCategory.MISC).noLootTable().sized(0.0F, 0.0F).clientTrackingRange(10).updateInterval(1)
    );
    public static final EntityType<ItemFrame> ITEM_FRAME = register(
        "item_frame",
        EntityType.Builder.<ItemFrame>of(ItemFrame::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.0F)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
    );
    public static final EntityType<Boat> JUNGLE_BOAT = register(
        "jungle_boat",
        EntityType.Builder.of(boatFactory(() -> Items.JUNGLE_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> JUNGLE_CHEST_BOAT = register(
        "jungle_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.JUNGLE_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<LeashFenceKnotEntity> LEASH_KNOT = register(
        "leash_knot",
        EntityType.Builder.<LeashFenceKnotEntity>of(LeashFenceKnotEntity::new, MobCategory.MISC)
            .noLootTable()
            .noSave()
            .sized(0.375F, 0.5F)
            .eyeHeight(0.0625F)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
    );
    public static final EntityType<LightningBolt> LIGHTNING_BOLT = register(
        "lightning_bolt",
        EntityType.Builder.of(LightningBolt::new, MobCategory.MISC).noLootTable().noSave().sized(0.0F, 0.0F).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE)
    );
    public static final EntityType<Llama> LLAMA = register(
        "llama",
        EntityType.Builder.of(Llama::new, MobCategory.CREATURE)
            .sized(0.9F, 1.87F)
            .eyeHeight(1.7765F)
            .passengerAttachments(new Vec3(0.0, 1.37, -0.3))
            .clientTrackingRange(10)
    );
    public static final EntityType<LlamaSpit> LLAMA_SPIT = register(
        "llama_spit", EntityType.Builder.<LlamaSpit>of(LlamaSpit::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<MagmaCube> MAGMA_CUBE = register(
        "magma_cube",
        EntityType.Builder.of(MagmaCube::new, MobCategory.MONSTER).fireImmune().sized(0.52F, 0.52F).eyeHeight(0.325F).spawnDimensionsScale(4.0F).clientTrackingRange(8)
    );
    public static final EntityType<Boat> MANGROVE_BOAT = register(
        "mangrove_boat",
        EntityType.Builder.of(boatFactory(() -> Items.MANGROVE_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> MANGROVE_CHEST_BOAT = register(
        "mangrove_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.MANGROVE_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Marker> MARKER = register(
        "marker", EntityType.Builder.of(Marker::new, MobCategory.MISC).noLootTable().sized(0.0F, 0.0F).clientTrackingRange(0)
    );
    public static final EntityType<Minecart> MINECART = register(
        "minecart", EntityType.Builder.of(Minecart::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8)
    );
    public static final EntityType<MushroomCow> MOOSHROOM = register(
        "mooshroom", EntityType.Builder.of(MushroomCow::new, MobCategory.CREATURE).sized(0.9F, 1.4F).eyeHeight(1.3F).passengerAttachments(1.36875F).clientTrackingRange(10)
    );
    public static final EntityType<Mule> MULE = register(
        "mule", EntityType.Builder.of(Mule::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).eyeHeight(1.52F).passengerAttachments(1.2125F).clientTrackingRange(8)
    );
    public static final EntityType<Boat> OAK_BOAT = register(
        "oak_boat",
        EntityType.Builder.of(boatFactory(() -> Items.OAK_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> OAK_CHEST_BOAT = register(
        "oak_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.OAK_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Ocelot> OCELOT = register(
        "ocelot", EntityType.Builder.of(Ocelot::new, MobCategory.CREATURE).sized(0.6F, 0.7F).passengerAttachments(0.6375F).clientTrackingRange(10)
    );
    public static final EntityType<OminousItemSpawner> OMINOUS_ITEM_SPAWNER = register(
        "ominous_item_spawner", EntityType.Builder.of(OminousItemSpawner::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(8)
    );
    public static final EntityType<Painting> PAINTING = register(
        "painting",
        EntityType.Builder.<Painting>of(Painting::new, MobCategory.MISC).noLootTable().sized(0.5F, 0.5F).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE)
    );
    public static final EntityType<Boat> PALE_OAK_BOAT = register(
        "pale_oak_boat",
        EntityType.Builder.of(boatFactory(() -> Items.PALE_OAK_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> PALE_OAK_CHEST_BOAT = register(
        "pale_oak_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.PALE_OAK_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Panda> PANDA = register(
        "panda", EntityType.Builder.of(Panda::new, MobCategory.CREATURE).sized(1.3F, 1.25F).clientTrackingRange(10)
    );
    public static final EntityType<Parrot> PARROT = register(
        "parrot", EntityType.Builder.of(Parrot::new, MobCategory.CREATURE).sized(0.5F, 0.9F).eyeHeight(0.54F).passengerAttachments(0.4625F).clientTrackingRange(8)
    );
    public static final EntityType<Phantom> PHANTOM = register(
        "phantom",
        EntityType.Builder.of(Phantom::new, MobCategory.MONSTER).sized(0.9F, 0.5F).eyeHeight(0.175F).passengerAttachments(0.3375F).ridingOffset(-0.125F).clientTrackingRange(8)
    );
    public static final EntityType<Pig> PIG = register(
        "pig", EntityType.Builder.of(Pig::new, MobCategory.CREATURE).sized(0.9F, 0.9F).passengerAttachments(0.86875F).clientTrackingRange(10)
    );
    public static final EntityType<Piglin> PIGLIN = register(
        "piglin",
        EntityType.Builder.of(Piglin::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.79F).passengerAttachments(2.0125F).ridingOffset(-0.7F).clientTrackingRange(8)
    );
    public static final EntityType<PiglinBrute> PIGLIN_BRUTE = register(
        "piglin_brute",
        EntityType.Builder.of(PiglinBrute::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .eyeHeight(1.79F)
            .passengerAttachments(2.0125F)
            .ridingOffset(-0.7F)
            .clientTrackingRange(8)
    );
    public static final EntityType<Pillager> PILLAGER = register(
        "pillager",
        EntityType.Builder.of(Pillager::new, MobCategory.MONSTER).canSpawnFarFromPlayer().sized(0.6F, 1.95F).passengerAttachments(2.0F).ridingOffset(-0.6F).clientTrackingRange(8)
    );
    public static final EntityType<PolarBear> POLAR_BEAR = register(
        "polar_bear", EntityType.Builder.of(PolarBear::new, MobCategory.CREATURE).immuneTo(Blocks.POWDER_SNOW).sized(1.4F, 1.4F).clientTrackingRange(10)
    );
    public static final EntityType<ThrownPotion> POTION = register(
        "potion", EntityType.Builder.<ThrownPotion>of(ThrownPotion::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<Pufferfish> PUFFERFISH = register(
        "pufferfish", EntityType.Builder.of(Pufferfish::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.7F).eyeHeight(0.455F).clientTrackingRange(4)
    );
    public static final EntityType<Rabbit> RABBIT = register(
        "rabbit", EntityType.Builder.of(Rabbit::new, MobCategory.CREATURE).sized(0.4F, 0.5F).clientTrackingRange(8)
    );
    public static final EntityType<Ravager> RAVAGER = register(
        "ravager", EntityType.Builder.of(Ravager::new, MobCategory.MONSTER).sized(1.95F, 2.2F).passengerAttachments(new Vec3(0.0, 2.2625, -0.0625)).clientTrackingRange(10)
    );
    public static final EntityType<Salmon> SALMON = register(
        "salmon", EntityType.Builder.of(Salmon::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.4F).eyeHeight(0.26F).clientTrackingRange(4)
    );
    public static final EntityType<Sheep> SHEEP = register(
        "sheep", EntityType.Builder.of(Sheep::new, MobCategory.CREATURE).sized(0.9F, 1.3F).eyeHeight(1.235F).passengerAttachments(1.2375F).clientTrackingRange(10)
    );
    public static final EntityType<Shulker> SHULKER = register(
        "shulker", EntityType.Builder.of(Shulker::new, MobCategory.MONSTER).fireImmune().canSpawnFarFromPlayer().sized(1.0F, 1.0F).eyeHeight(0.5F).clientTrackingRange(10)
    );
    public static final EntityType<ShulkerBullet> SHULKER_BULLET = register(
        "shulker_bullet", EntityType.Builder.<ShulkerBullet>of(ShulkerBullet::new, MobCategory.MISC).noLootTable().sized(0.3125F, 0.3125F).clientTrackingRange(8)
    );
    public static final EntityType<Silverfish> SILVERFISH = register(
        "silverfish", EntityType.Builder.of(Silverfish::new, MobCategory.MONSTER).sized(0.4F, 0.3F).eyeHeight(0.13F).passengerAttachments(0.2375F).clientTrackingRange(8)
    );
    public static final EntityType<Skeleton> SKELETON = register(
        "skeleton", EntityType.Builder.of(Skeleton::new, MobCategory.MONSTER).sized(0.6F, 1.99F).eyeHeight(1.74F).ridingOffset(-0.7F).clientTrackingRange(8)
    );
    public static final EntityType<SkeletonHorse> SKELETON_HORSE = register(
        "skeleton_horse",
        EntityType.Builder.of(SkeletonHorse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).eyeHeight(1.52F).passengerAttachments(1.31875F).clientTrackingRange(10)
    );
    public static final EntityType<Slime> SLIME = register(
        "slime", EntityType.Builder.of(Slime::new, MobCategory.MONSTER).sized(0.52F, 0.52F).eyeHeight(0.325F).spawnDimensionsScale(4.0F).clientTrackingRange(10)
    );
    public static final EntityType<SmallFireball> SMALL_FIREBALL = register(
        "small_fireball",
        EntityType.Builder.<SmallFireball>of(SmallFireball::new, MobCategory.MISC).noLootTable().sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<Sniffer> SNIFFER = register(
        "sniffer",
        EntityType.Builder.of(Sniffer::new, MobCategory.CREATURE)
            .sized(1.9F, 1.75F)
            .eyeHeight(1.05F)
            .passengerAttachments(2.09375F)
            .nameTagOffset(2.05F)
            .clientTrackingRange(10)
    );
    public static final EntityType<Snowball> SNOWBALL = register(
        "snowball", EntityType.Builder.<Snowball>of(Snowball::new, MobCategory.MISC).noLootTable().sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<SnowGolem> SNOW_GOLEM = register(
        "snow_golem", EntityType.Builder.of(SnowGolem::new, MobCategory.MISC).immuneTo(Blocks.POWDER_SNOW).sized(0.7F, 1.9F).eyeHeight(1.7F).clientTrackingRange(8)
    );
    public static final EntityType<MinecartSpawner> SPAWNER_MINECART = register(
        "spawner_minecart",
        EntityType.Builder.of(MinecartSpawner::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8)
    );
    public static final EntityType<SpectralArrow> SPECTRAL_ARROW = register(
        "spectral_arrow",
        EntityType.Builder.<SpectralArrow>of(SpectralArrow::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20)
    );
    public static final EntityType<Spider> SPIDER = register(
        "spider", EntityType.Builder.of(Spider::new, MobCategory.MONSTER).sized(1.4F, 0.9F).eyeHeight(0.65F).passengerAttachments(0.765F).clientTrackingRange(8)
    );
    public static final EntityType<Boat> SPRUCE_BOAT = register(
        "spruce_boat",
        EntityType.Builder.of(boatFactory(() -> Items.SPRUCE_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<ChestBoat> SPRUCE_CHEST_BOAT = register(
        "spruce_chest_boat",
        EntityType.Builder.of(chestBoatFactory(() -> Items.SPRUCE_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10)
    );
    public static final EntityType<Squid> SQUID = register(
        "squid", EntityType.Builder.of(Squid::new, MobCategory.WATER_CREATURE).sized(0.8F, 0.8F).eyeHeight(0.4F).clientTrackingRange(8)
    );
    public static final EntityType<Stray> STRAY = register(
        "stray",
        EntityType.Builder.of(Stray::new, MobCategory.MONSTER)
            .sized(0.6F, 1.99F)
            .eyeHeight(1.74F)
            .ridingOffset(-0.7F)
            .immuneTo(Blocks.POWDER_SNOW)
            .clientTrackingRange(8)
    );
    public static final EntityType<Strider> STRIDER = register(
        "strider", EntityType.Builder.of(Strider::new, MobCategory.CREATURE).fireImmune().sized(0.9F, 1.7F).clientTrackingRange(10)
    );
    public static final EntityType<Tadpole> TADPOLE = register(
        "tadpole", EntityType.Builder.of(Tadpole::new, MobCategory.CREATURE).sized(0.4F, 0.3F).eyeHeight(0.19500001F).clientTrackingRange(10)
    );
    public static final EntityType<Display.TextDisplay> TEXT_DISPLAY = register(
        "text_display", EntityType.Builder.of(Display.TextDisplay::new, MobCategory.MISC).noLootTable().sized(0.0F, 0.0F).clientTrackingRange(10).updateInterval(1)
    );
    public static final EntityType<PrimedTnt> TNT = register(
        "tnt",
        EntityType.Builder.<PrimedTnt>of(PrimedTnt::new, MobCategory.MISC)
            .noLootTable()
            .fireImmune()
            .sized(0.98F, 0.98F)
            .eyeHeight(0.15F)
            .clientTrackingRange(10)
            .updateInterval(10)
    );
    public static final EntityType<MinecartTNT> TNT_MINECART = register(
        "tnt_minecart", EntityType.Builder.of(MinecartTNT::new, MobCategory.MISC).noLootTable().sized(0.98F, 0.7F).passengerAttachments(0.1875F).clientTrackingRange(8)
    );
    public static final EntityType<TraderLlama> TRADER_LLAMA = register(
        "trader_llama",
        EntityType.Builder.of(TraderLlama::new, MobCategory.CREATURE)
            .sized(0.9F, 1.87F)
            .eyeHeight(1.7765F)
            .passengerAttachments(new Vec3(0.0, 1.37, -0.3))
            .clientTrackingRange(10)
    );
    public static final EntityType<ThrownTrident> TRIDENT = register(
        "trident",
        EntityType.Builder.<ThrownTrident>of(ThrownTrident::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.5F, 0.5F)
            .eyeHeight(0.13F)
            .clientTrackingRange(4)
            .updateInterval(20)
    );
    public static final EntityType<TropicalFish> TROPICAL_FISH = register(
        "tropical_fish", EntityType.Builder.of(TropicalFish::new, MobCategory.WATER_AMBIENT).sized(0.5F, 0.4F).eyeHeight(0.26F).clientTrackingRange(4)
    );
    public static final EntityType<Turtle> TURTLE = register(
        "turtle", EntityType.Builder.of(Turtle::new, MobCategory.CREATURE).sized(1.2F, 0.4F).passengerAttachments(new Vec3(0.0, 0.55625, -0.25)).clientTrackingRange(10)
    );
    public static final EntityType<Vex> VEX = register(
        "vex",
        EntityType.Builder.of(Vex::new, MobCategory.MONSTER)
            .fireImmune()
            .sized(0.4F, 0.8F)
            .eyeHeight(0.51875F)
            .passengerAttachments(0.7375F)
            .ridingOffset(0.04F)
            .clientTrackingRange(8)
    );
    public static final EntityType<Villager> VILLAGER = register(
        "villager", EntityType.Builder.<Villager>of(Villager::new, MobCategory.MISC).sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(10)
    );
    public static final EntityType<Vindicator> VINDICATOR = register(
        "vindicator", EntityType.Builder.of(Vindicator::new, MobCategory.MONSTER).sized(0.6F, 1.95F).passengerAttachments(2.0F).ridingOffset(-0.6F).clientTrackingRange(8)
    );
    public static final EntityType<WanderingTrader> WANDERING_TRADER = register(
        "wandering_trader", EntityType.Builder.of(WanderingTrader::new, MobCategory.CREATURE).sized(0.6F, 1.95F).eyeHeight(1.62F).clientTrackingRange(10)
    );
    public static final EntityType<Warden> WARDEN = register(
        "warden",
        EntityType.Builder.of(Warden::new, MobCategory.MONSTER)
            .sized(0.9F, 2.9F)
            .passengerAttachments(3.15F)
            .attach(EntityAttachment.WARDEN_CHEST, 0.0F, 1.6F, 0.0F)
            .clientTrackingRange(16)
            .fireImmune()
    );
    public static final EntityType<WindCharge> WIND_CHARGE = register(
        "wind_charge",
        EntityType.Builder.<WindCharge>of(WindCharge::new, MobCategory.MISC)
            .noLootTable()
            .sized(0.3125F, 0.3125F)
            .eyeHeight(0.0F)
            .clientTrackingRange(4)
            .updateInterval(10)
    );
    public static final EntityType<Witch> WITCH = register(
        "witch", EntityType.Builder.of(Witch::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.62F).passengerAttachments(2.2625F).clientTrackingRange(8)
    );
    public static final EntityType<WitherBoss> WITHER = register(
        "wither", EntityType.Builder.of(WitherBoss::new, MobCategory.MONSTER).fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.9F, 3.5F).clientTrackingRange(10)
    );
    public static final EntityType<WitherSkeleton> WITHER_SKELETON = register(
        "wither_skeleton",
        EntityType.Builder.of(WitherSkeleton::new, MobCategory.MONSTER)
            .fireImmune()
            .immuneTo(Blocks.WITHER_ROSE)
            .sized(0.7F, 2.4F)
            .eyeHeight(2.1F)
            .ridingOffset(-0.875F)
            .clientTrackingRange(8)
    );
    public static final EntityType<WitherSkull> WITHER_SKULL = register(
        "wither_skull",
        EntityType.Builder.<WitherSkull>of(WitherSkull::new, MobCategory.MISC).noLootTable().sized(0.3125F, 0.3125F).clientTrackingRange(4).updateInterval(10)
    );
    public static final EntityType<Wolf> WOLF = register(
        "wolf",
        EntityType.Builder.of(Wolf::new, MobCategory.CREATURE)
            .sized(0.6F, 0.85F)
            .eyeHeight(0.68F)
            .passengerAttachments(new Vec3(0.0, 0.81875, -0.0625))
            .clientTrackingRange(10)
    );
    public static final EntityType<Zoglin> ZOGLIN = register(
        "zoglin", EntityType.Builder.of(Zoglin::new, MobCategory.MONSTER).fireImmune().sized(1.3964844F, 1.4F).passengerAttachments(1.49375F).clientTrackingRange(8)
    );
    public static final EntityType<Zombie> ZOMBIE = register(
        "zombie",
        EntityType.Builder.<Zombie>of(Zombie::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .eyeHeight(1.74F)
            .passengerAttachments(2.0125F)
            .ridingOffset(-0.7F)
            .clientTrackingRange(8)
    );
    public static final EntityType<ZombieHorse> ZOMBIE_HORSE = register(
        "zombie_horse",
        EntityType.Builder.of(ZombieHorse::new, MobCategory.CREATURE).sized(1.3964844F, 1.6F).eyeHeight(1.52F).passengerAttachments(1.31875F).clientTrackingRange(10)
    );
    public static final EntityType<ZombieVillager> ZOMBIE_VILLAGER = register(
        "zombie_villager",
        EntityType.Builder.of(ZombieVillager::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .passengerAttachments(2.125F)
            .ridingOffset(-0.7F)
            .eyeHeight(1.74F)
            .clientTrackingRange(8)
    );
    public static final EntityType<ZombifiedPiglin> ZOMBIFIED_PIGLIN = register(
        "zombified_piglin",
        EntityType.Builder.of(ZombifiedPiglin::new, MobCategory.MONSTER)
            .fireImmune()
            .sized(0.6F, 1.95F)
            .eyeHeight(1.79F)
            .passengerAttachments(2.0F)
            .ridingOffset(-0.7F)
            .clientTrackingRange(8)
    );
    public static final EntityType<Player> PLAYER = register(
        "player",
        EntityType.Builder.<Player>createNothing(MobCategory.MISC)
            .noSave()
            .noSummon()
            .sized(0.6F, 1.8F)
            .eyeHeight(1.62F)
            .vehicleAttachment(Player.DEFAULT_VEHICLE_ATTACHMENT)
            .clientTrackingRange(32)
            .updateInterval(2)
    );
    public static final EntityType<FishingHook> FISHING_BOBBER = register(
        "fishing_bobber",
        EntityType.Builder.<FishingHook>of(FishingHook::new, MobCategory.MISC)
            .noLootTable()
            .noSave()
            .noSummon()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(5)
    );
    private static final Set<EntityType<?>> OP_ONLY_CUSTOM_DATA = Set.of(FALLING_BLOCK, COMMAND_BLOCK_MINECART, SPAWNER_MINECART);
    private final EntityType.EntityFactory<T> factory;
    private final MobCategory category;
    private final ImmutableSet<Block> immuneTo;
    private final boolean serialize;
    private final boolean summon;
    private final boolean fireImmune;
    private final boolean canSpawnFarFromPlayer;
    private final int clientTrackingRange;
    private final int updateInterval;
    private final String descriptionId;
    @Nullable
    private Component description;
    private final Optional<ResourceKey<LootTable>> lootTable;
    private final EntityDimensions dimensions;
    private final float spawnDimensionsScale;
    private final FeatureFlagSet requiredFeatures;

    private static <T extends Entity> EntityType<T> register(ResourceKey<EntityType<?>> p_368406_, EntityType.Builder<T> p_363129_) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, p_368406_, p_363129_.build(p_368406_));
    }

    private static ResourceKey<EntityType<?>> vanillaEntityId(String p_363004_) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.withDefaultNamespace(p_363004_));
    }

    private static <T extends Entity> EntityType<T> register(String p_20635_, EntityType.Builder<T> p_20636_) {
        return register(vanillaEntityId(p_20635_), p_20636_);
    }

    public static ResourceLocation getKey(EntityType<?> p_20614_) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(p_20614_);
    }

    public static Optional<EntityType<?>> byString(String p_20633_) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(p_20633_));
    }

    public EntityType(
        EntityType.EntityFactory<T> p_273268_,
        MobCategory p_272918_,
        boolean p_273417_,
        boolean p_273389_,
        boolean p_273556_,
        boolean p_272654_,
        ImmutableSet<Block> p_273631_,
        EntityDimensions p_272946_,
        float p_332711_,
        int p_272895_,
        int p_273451_,
        String p_367006_,
        Optional<ResourceKey<LootTable>> p_364691_,
        FeatureFlagSet p_273518_
    ) {
        this.factory = p_273268_;
        this.category = p_272918_;
        this.canSpawnFarFromPlayer = p_272654_;
        this.serialize = p_273417_;
        this.summon = p_273389_;
        this.fireImmune = p_273556_;
        this.immuneTo = p_273631_;
        this.dimensions = p_272946_;
        this.spawnDimensionsScale = p_332711_;
        this.clientTrackingRange = p_272895_;
        this.updateInterval = p_273451_;
        this.descriptionId = p_367006_;
        this.lootTable = p_364691_;
        this.requiredFeatures = p_273518_;
    }

    @Nullable
    public T spawn(
        ServerLevel p_20593_,
        @Nullable ItemStack p_20594_,
        @Nullable Player p_20595_,
        BlockPos p_20596_,
        EntitySpawnReason p_365281_,
        boolean p_20598_,
        boolean p_20599_
    ) {
        Consumer<T> consumer;
        if (p_20594_ != null) {
            consumer = createDefaultStackConfig(p_20593_, p_20594_, p_20595_);
        } else {
            consumer = p_263563_ -> {
            };
        }

        return this.spawn(p_20593_, consumer, p_20596_, p_365281_, p_20598_, p_20599_);
    }

    public static <T extends Entity> Consumer<T> createDefaultStackConfig(Level p_361811_, ItemStack p_263568_, @Nullable Player p_263575_) {
        return appendDefaultStackConfig(p_262561_ -> {
        }, p_361811_, p_263568_, p_263575_);
    }

    public static <T extends Entity> Consumer<T> appendDefaultStackConfig(Consumer<T> p_265154_, Level p_367636_, ItemStack p_265598_, @Nullable Player p_265666_) {
        return appendCustomEntityStackConfig(appendCustomNameConfig(p_265154_, p_265598_), p_367636_, p_265598_, p_265666_);
    }

    public static <T extends Entity> Consumer<T> appendCustomNameConfig(Consumer<T> p_263567_, ItemStack p_263564_) {
        Component component = p_263564_.get(DataComponents.CUSTOM_NAME);
        return component != null ? p_263567_.andThen(p_326773_ -> p_326773_.setCustomName(component)) : p_263567_;
    }

    public static <T extends Entity> Consumer<T> appendCustomEntityStackConfig(Consumer<T> p_263579_, Level p_369311_, ItemStack p_263582_, @Nullable Player p_263574_) {
        CustomData customdata = p_263582_.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
        return !customdata.isEmpty() ? p_263579_.andThen(p_326771_ -> updateCustomEntityTag(p_369311_, p_263574_, p_326771_, customdata)) : p_263579_;
    }

    @Nullable
    public T spawn(ServerLevel p_262704_, BlockPos p_262672_, EntitySpawnReason p_362589_) {
        return this.spawn(p_262704_, null, p_262672_, p_362589_, false, false);
    }

    @Nullable
    public T spawn(
        ServerLevel p_262634_, @Nullable Consumer<T> p_368499_, BlockPos p_262707_, EntitySpawnReason p_362551_, boolean p_361509_, boolean p_363500_
    ) {
        T t = this.create(p_262634_, p_368499_, p_262707_, p_362551_, p_361509_, p_363500_);
        if (t != null) {
            p_262634_.addFreshEntityWithPassengers(t);
            if (t instanceof Mob mob) {
                mob.playAmbientSound();
            }
        }

        return t;
    }

    @Nullable
    public T create(
        ServerLevel p_262637_, @Nullable Consumer<T> p_262629_, BlockPos p_262595_, EntitySpawnReason p_362277_, boolean p_262685_, boolean p_262588_
    ) {
        T t = this.create(p_262637_, p_362277_);
        if (t == null) {
            return null;
        } else {
            double d0;
            if (p_262685_) {
                t.setPos((double)p_262595_.getX() + 0.5, (double)(p_262595_.getY() + 1), (double)p_262595_.getZ() + 0.5);
                d0 = getYOffset(p_262637_, p_262595_, p_262588_, t.getBoundingBox());
            } else {
                d0 = 0.0;
            }

            t.moveTo(
                (double)p_262595_.getX() + 0.5,
                (double)p_262595_.getY() + d0,
                (double)p_262595_.getZ() + 0.5,
                Mth.wrapDegrees(p_262637_.random.nextFloat() * 360.0F),
                0.0F
            );
            if (t instanceof Mob mob) {
                mob.yHeadRot = mob.getYRot();
                mob.yBodyRot = mob.getYRot();
                mob.finalizeSpawn(p_262637_, p_262637_.getCurrentDifficultyAt(mob.blockPosition()), p_362277_, null);
            }

            if (p_262629_ != null) {
                p_262629_.accept(t);
            }

            return t;
        }
    }

    protected static double getYOffset(LevelReader p_20626_, BlockPos p_20627_, boolean p_20628_, AABB p_20629_) {
        AABB aabb = new AABB(p_20627_);
        if (p_20628_) {
            aabb = aabb.expandTowards(0.0, -1.0, 0.0);
        }

        Iterable<VoxelShape> iterable = p_20626_.getCollisions(null, aabb);
        return 1.0 + Shapes.collide(Direction.Axis.Y, p_20629_, iterable, p_20628_ ? -2.0 : -1.0);
    }

    public static void updateCustomEntityTag(Level p_20621_, @Nullable Player p_20622_, @Nullable Entity p_20623_, CustomData p_334584_) {
        MinecraftServer minecraftserver = p_20621_.getServer();
        if (minecraftserver != null && p_20623_ != null) {
            EntityType<?> entitytype = p_334584_.parseEntityType(minecraftserver.registryAccess(), Registries.ENTITY_TYPE);
            if (p_20623_.getType() == entitytype) {
                if (p_20621_.isClientSide || !p_20623_.getType().onlyOpCanSetNbt() || p_20622_ != null && minecraftserver.getPlayerList().isOp(p_20622_.getGameProfile())) {
                    p_334584_.loadInto(p_20623_);
                }
            }
        }
    }

    public boolean canSerialize() {
        return this.serialize;
    }

    public boolean canSummon() {
        return this.summon;
    }

    public boolean fireImmune() {
        return this.fireImmune;
    }

    public boolean canSpawnFarFromPlayer() {
        return this.canSpawnFarFromPlayer;
    }

    public MobCategory getCategory() {
        return this.category;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }

    public Component getDescription() {
        if (this.description == null) {
            this.description = Component.translatable(this.getDescriptionId());
        }

        return this.description;
    }

    @Override
    public String toString() {
        return this.getDescriptionId();
    }

    public String toShortString() {
        int i = this.getDescriptionId().lastIndexOf(46);
        return i == -1 ? this.getDescriptionId() : this.getDescriptionId().substring(i + 1);
    }

    public Optional<ResourceKey<LootTable>> getDefaultLootTable() {
        return this.lootTable;
    }

    public float getWidth() {
        return this.dimensions.width();
    }

    public float getHeight() {
        return this.dimensions.height();
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    @Nullable
    public T create(Level p_20616_, EntitySpawnReason p_363393_) {
        return !this.isEnabled(p_20616_.enabledFeatures()) ? null : this.factory.create(this, p_20616_);
    }

    public static Optional<Entity> create(CompoundTag p_20643_, Level p_20644_, EntitySpawnReason p_365960_) {
        return Util.ifElse(
            by(p_20643_).map(p_358872_ -> p_358872_.create(p_20644_, p_365960_)),
            p_185990_ -> p_185990_.load(p_20643_),
            () -> LOGGER.warn("Skipping Entity with id {}", p_20643_.getString("id"))
        );
    }

    public AABB getSpawnAABB(double p_332185_, double p_336348_, double p_329000_) {
        float f = this.spawnDimensionsScale * this.getWidth() / 2.0F;
        float f1 = this.spawnDimensionsScale * this.getHeight();
        return new AABB(p_332185_ - (double)f, p_336348_, p_329000_ - (double)f, p_332185_ + (double)f, p_336348_ + (double)f1, p_329000_ + (double)f);
    }

    public boolean isBlockDangerous(BlockState p_20631_) {
        if (this.immuneTo.contains(p_20631_.getBlock())) {
            return false;
        } else {
            return !this.fireImmune && NodeEvaluator.isBurningBlock(p_20631_)
                ? true
                : p_20631_.is(Blocks.WITHER_ROSE)
                    || p_20631_.is(Blocks.SWEET_BERRY_BUSH)
                    || p_20631_.is(Blocks.CACTUS)
                    || p_20631_.is(Blocks.POWDER_SNOW);
        }
    }

    public EntityDimensions getDimensions() {
        return this.dimensions;
    }

    public static Optional<EntityType<?>> by(CompoundTag p_20638_) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(p_20638_.getString("id")));
    }

    @Nullable
    public static Entity loadEntityRecursive(CompoundTag p_20646_, Level p_20647_, EntitySpawnReason p_363751_, Function<Entity, Entity> p_20648_) {
        return loadStaticEntity(p_20646_, p_20647_, p_363751_).map(p_20648_).map(p_185995_ -> {
            if (p_20646_.contains("Passengers", 9)) {
                ListTag listtag = p_20646_.getList("Passengers", 10);

                for (int i = 0; i < listtag.size(); i++) {
                    Entity entity = loadEntityRecursive(listtag.getCompound(i), p_20647_, p_363751_, p_20648_);
                    if (entity != null) {
                        entity.startRiding(p_185995_, true);
                    }
                }
            }

            return (Entity)p_185995_;
        }).orElse(null);
    }

    public static Stream<Entity> loadEntitiesRecursive(final List<? extends Tag> p_147046_, final Level p_147047_, final EntitySpawnReason p_363854_) {
        final Spliterator<? extends Tag> spliterator = p_147046_.spliterator();
        return StreamSupport.stream(new Spliterator<Entity>() {
            @Override
            public boolean tryAdvance(Consumer<? super Entity> p_147066_) {
                return spliterator.tryAdvance(p_358876_ -> EntityType.loadEntityRecursive((CompoundTag)p_358876_, p_147047_, p_363854_, p_147062_ -> {
                        p_147066_.accept(p_147062_);
                        return p_147062_;
                    }));
            }

            @Override
            public Spliterator<Entity> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return (long)p_147046_.size();
            }

            @Override
            public int characteristics() {
                return 1297;
            }
        }, false);
    }

    private static Optional<Entity> loadStaticEntity(CompoundTag p_20670_, Level p_20671_, EntitySpawnReason p_366785_) {
        try {
            return create(p_20670_, p_20671_, p_366785_);
        } catch (RuntimeException runtimeexception) {
            LOGGER.warn("Exception loading entity: ", (Throwable)runtimeexception);
            return Optional.empty();
        }
    }

    public int clientTrackingRange() {
        return this.clientTrackingRange;
    }

    public int updateInterval() {
        return this.updateInterval;
    }

    public boolean trackDeltas() {
        return this != PLAYER
            && this != LLAMA_SPIT
            && this != WITHER
            && this != BAT
            && this != ITEM_FRAME
            && this != GLOW_ITEM_FRAME
            && this != LEASH_KNOT
            && this != PAINTING
            && this != END_CRYSTAL
            && this != EVOKER_FANGS;
    }

    public boolean is(TagKey<EntityType<?>> p_204040_) {
        return this.builtInRegistryHolder.is(p_204040_);
    }

    public boolean is(HolderSet<EntityType<?>> p_300605_) {
        return p_300605_.contains(this.builtInRegistryHolder);
    }

    @Nullable
    public T tryCast(Entity p_147042_) {
        return (T)(p_147042_.getType() == this ? p_147042_ : null);
    }

    @Override
    public Class<? extends Entity> getBaseClass() {
        return Entity.class;
    }

    @Deprecated
    public Holder.Reference<EntityType<?>> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    private static EntityType.EntityFactory<Boat> boatFactory(Supplier<Item> p_370206_) {
        return (p_358862_, p_358863_) -> new Boat(p_358862_, p_358863_, p_370206_);
    }

    private static EntityType.EntityFactory<ChestBoat> chestBoatFactory(Supplier<Item> p_366671_) {
        return (p_358868_, p_358869_) -> new ChestBoat(p_358868_, p_358869_, p_366671_);
    }

    private static EntityType.EntityFactory<Raft> raftFactory(Supplier<Item> p_366849_) {
        return (p_358859_, p_358860_) -> new Raft(p_358859_, p_358860_, p_366849_);
    }

    private static EntityType.EntityFactory<ChestRaft> chestRaftFactory(Supplier<Item> p_368732_) {
        return (p_358865_, p_358866_) -> new ChestRaft(p_358865_, p_358866_, p_368732_);
    }

    public boolean onlyOpCanSetNbt() {
        return OP_ONLY_CUSTOM_DATA.contains(this);
    }

    public static class Builder<T extends Entity> {
        private final EntityType.EntityFactory<T> factory;
        private final MobCategory category;
        private ImmutableSet<Block> immuneTo = ImmutableSet.of();
        private boolean serialize = true;
        private boolean summon = true;
        private boolean fireImmune;
        private boolean canSpawnFarFromPlayer;
        private int clientTrackingRange = 5;
        private int updateInterval = 3;
        private EntityDimensions dimensions = EntityDimensions.scalable(0.6F, 1.8F);
        private float spawnDimensionsScale = 1.0F;
        private EntityAttachments.Builder attachments = EntityAttachments.builder();
        private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
        private DependantName<EntityType<?>, Optional<ResourceKey<LootTable>>> lootTable = p_358877_ -> Optional.of(
                ResourceKey.create(Registries.LOOT_TABLE, p_358877_.location().withPrefix("entities/"))
            );
        private DependantName<EntityType<?>, String> descriptionId = p_358878_ -> Util.makeDescriptionId("entity", p_358878_.location());

        private Builder(EntityType.EntityFactory<T> p_20696_, MobCategory p_20697_) {
            this.factory = p_20696_;
            this.category = p_20697_;
            this.canSpawnFarFromPlayer = p_20697_ == MobCategory.CREATURE || p_20697_ == MobCategory.MISC;
        }

        public static <T extends Entity> EntityType.Builder<T> of(EntityType.EntityFactory<T> p_20705_, MobCategory p_20706_) {
            return new EntityType.Builder<>(p_20705_, p_20706_);
        }

        public static <T extends Entity> EntityType.Builder<T> createNothing(MobCategory p_20711_) {
            return new EntityType.Builder<>((p_20708_, p_20709_) -> null, p_20711_);
        }

        public EntityType.Builder<T> sized(float p_20700_, float p_20701_) {
            this.dimensions = EntityDimensions.scalable(p_20700_, p_20701_);
            return this;
        }

        public EntityType.Builder<T> spawnDimensionsScale(float p_334402_) {
            this.spawnDimensionsScale = p_334402_;
            return this;
        }

        public EntityType.Builder<T> eyeHeight(float p_331685_) {
            this.dimensions = this.dimensions.withEyeHeight(p_331685_);
            return this;
        }

        public EntityType.Builder<T> passengerAttachments(float... p_335899_) {
            for (float f : p_335899_) {
                this.attachments = this.attachments.attach(EntityAttachment.PASSENGER, 0.0F, f, 0.0F);
            }

            return this;
        }

        public EntityType.Builder<T> passengerAttachments(Vec3... p_334238_) {
            for (Vec3 vec3 : p_334238_) {
                this.attachments = this.attachments.attach(EntityAttachment.PASSENGER, vec3);
            }

            return this;
        }

        public EntityType.Builder<T> vehicleAttachment(Vec3 p_330973_) {
            return this.attach(EntityAttachment.VEHICLE, p_330973_);
        }

        public EntityType.Builder<T> ridingOffset(float p_335381_) {
            return this.attach(EntityAttachment.VEHICLE, 0.0F, -p_335381_, 0.0F);
        }

        public EntityType.Builder<T> nameTagOffset(float p_332085_) {
            return this.attach(EntityAttachment.NAME_TAG, 0.0F, p_332085_, 0.0F);
        }

        public EntityType.Builder<T> attach(EntityAttachment p_329709_, float p_333115_, float p_330566_, float p_336085_) {
            this.attachments = this.attachments.attach(p_329709_, p_333115_, p_330566_, p_336085_);
            return this;
        }

        public EntityType.Builder<T> attach(EntityAttachment p_329452_, Vec3 p_328984_) {
            this.attachments = this.attachments.attach(p_329452_, p_328984_);
            return this;
        }

        public EntityType.Builder<T> noSummon() {
            this.summon = false;
            return this;
        }

        public EntityType.Builder<T> noSave() {
            this.serialize = false;
            return this;
        }

        public EntityType.Builder<T> fireImmune() {
            this.fireImmune = true;
            return this;
        }

        public EntityType.Builder<T> immuneTo(Block... p_20715_) {
            this.immuneTo = ImmutableSet.copyOf(p_20715_);
            return this;
        }

        public EntityType.Builder<T> canSpawnFarFromPlayer() {
            this.canSpawnFarFromPlayer = true;
            return this;
        }

        public EntityType.Builder<T> clientTrackingRange(int p_20703_) {
            this.clientTrackingRange = p_20703_;
            return this;
        }

        public EntityType.Builder<T> updateInterval(int p_20718_) {
            this.updateInterval = p_20718_;
            return this;
        }

        public EntityType.Builder<T> requiredFeatures(FeatureFlag... p_251646_) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(p_251646_);
            return this;
        }

        public EntityType.Builder<T> noLootTable() {
            this.lootTable = DependantName.fixed(Optional.empty());
            return this;
        }

        public EntityType<T> build(ResourceKey<EntityType<?>> p_369693_) {
            if (this.serialize) {
                Util.fetchChoiceType(References.ENTITY_TREE, p_369693_.location().toString());
            }

            return new EntityType<>(
                this.factory,
                this.category,
                this.serialize,
                this.summon,
                this.fireImmune,
                this.canSpawnFarFromPlayer,
                this.immuneTo,
                this.dimensions.withAttachments(this.attachments),
                this.spawnDimensionsScale,
                this.clientTrackingRange,
                this.updateInterval,
                this.descriptionId.get(p_369693_),
                this.lootTable.get(p_369693_),
                this.requiredFeatures
            );
        }
    }

    @FunctionalInterface
    public interface EntityFactory<T extends Entity> {
        T create(EntityType<T> p_20722_, Level p_20723_);
    }
}