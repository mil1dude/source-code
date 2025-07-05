package net.minecraft.core.component;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.EncoderCache;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.LockCode;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.DebugStickState;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.saveddata.maps.MapId;

public class DataComponents {
    static final EncoderCache ENCODER_CACHE = new EncoderCache(512);
    public static final DataComponentType<CustomData> CUSTOM_DATA = register("custom_data", p_333248_ -> p_333248_.persistent(CustomData.CODEC));
    public static final DataComponentType<Integer> MAX_STACK_SIZE = register(
        "max_stack_size", p_333287_ -> p_333287_.persistent(ExtraCodecs.intRange(1, 99)).networkSynchronized(ByteBufCodecs.VAR_INT)
    );
    public static final DataComponentType<Integer> MAX_DAMAGE = register(
        "max_damage", p_330941_ -> p_330941_.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );
    public static final DataComponentType<Integer> DAMAGE = register(
        "damage", p_333134_ -> p_333134_.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );
    public static final DataComponentType<Unbreakable> UNBREAKABLE = register(
        "unbreakable", p_335474_ -> p_335474_.persistent(Unbreakable.CODEC).networkSynchronized(Unbreakable.STREAM_CODEC)
    );
    public static final DataComponentType<Component> CUSTOM_NAME = register(
        "custom_name", p_332927_ -> p_332927_.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Component> ITEM_NAME = register(
        "item_name", p_332965_ -> p_332965_.persistent(ComponentSerialization.FLAT_CODEC).networkSynchronized(ComponentSerialization.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ResourceLocation> ITEM_MODEL = register(
        "item_model", p_358140_ -> p_358140_.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ItemLore> LORE = register(
        "lore", p_328310_ -> p_328310_.persistent(ItemLore.CODEC).networkSynchronized(ItemLore.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Rarity> RARITY = register(
        "rarity", p_332804_ -> p_332804_.persistent(Rarity.CODEC).networkSynchronized(Rarity.STREAM_CODEC)
    );
    public static final DataComponentType<ItemEnchantments> ENCHANTMENTS = register(
        "enchantments", p_332435_ -> p_332435_.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<AdventureModePredicate> CAN_PLACE_ON = register(
        "can_place_on", p_328700_ -> p_328700_.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<AdventureModePredicate> CAN_BREAK = register(
        "can_break", p_334730_ -> p_334730_.persistent(AdventureModePredicate.CODEC).networkSynchronized(AdventureModePredicate.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ItemAttributeModifiers> ATTRIBUTE_MODIFIERS = register(
        "attribute_modifiers", p_327741_ -> p_327741_.persistent(ItemAttributeModifiers.CODEC).networkSynchronized(ItemAttributeModifiers.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<CustomModelData> CUSTOM_MODEL_DATA = register(
        "custom_model_data", p_332321_ -> p_332321_.persistent(CustomModelData.CODEC).networkSynchronized(CustomModelData.STREAM_CODEC)
    );
    public static final DataComponentType<Unit> HIDE_ADDITIONAL_TOOLTIP = register(
        "hide_additional_tooltip", p_341001_ -> p_341001_.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );
    public static final DataComponentType<Unit> HIDE_TOOLTIP = register(
        "hide_tooltip", p_332868_ -> p_332868_.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );
    public static final DataComponentType<Integer> REPAIR_COST = register(
        "repair_cost", p_329633_ -> p_329633_.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
    );
    public static final DataComponentType<Unit> CREATIVE_SLOT_LOCK = register(
        "creative_slot_lock", p_331381_ -> p_331381_.networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );
    public static final DataComponentType<Boolean> ENCHANTMENT_GLINT_OVERRIDE = register(
        "enchantment_glint_override", p_331407_ -> p_331407_.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
    );
    public static final DataComponentType<Unit> INTANGIBLE_PROJECTILE = register("intangible_projectile", p_340998_ -> p_340998_.persistent(Unit.CODEC));
    public static final DataComponentType<FoodProperties> FOOD = register(
        "food", p_332099_ -> p_332099_.persistent(FoodProperties.DIRECT_CODEC).networkSynchronized(FoodProperties.DIRECT_STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Consumable> CONSUMABLE = register(
        "consumable", p_358129_ -> p_358129_.persistent(Consumable.CODEC).networkSynchronized(Consumable.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<UseRemainder> USE_REMAINDER = register(
        "use_remainder", p_358131_ -> p_358131_.persistent(UseRemainder.CODEC).networkSynchronized(UseRemainder.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<UseCooldown> USE_COOLDOWN = register(
        "use_cooldown", p_358137_ -> p_358137_.persistent(UseCooldown.CODEC).networkSynchronized(UseCooldown.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<DamageResistant> DAMAGE_RESISTANT = register(
        "damage_resistant", p_358139_ -> p_358139_.persistent(DamageResistant.CODEC).networkSynchronized(DamageResistant.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Tool> TOOL = register(
        "tool", p_335506_ -> p_335506_.persistent(Tool.CODEC).networkSynchronized(Tool.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Enchantable> ENCHANTABLE = register(
        "enchantable", p_358132_ -> p_358132_.persistent(Enchantable.CODEC).networkSynchronized(Enchantable.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Equippable> EQUIPPABLE = register(
        "equippable", p_358130_ -> p_358130_.persistent(Equippable.CODEC).networkSynchronized(Equippable.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Repairable> REPAIRABLE = register(
        "repairable", p_358135_ -> p_358135_.persistent(Repairable.CODEC).networkSynchronized(Repairable.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Unit> GLIDER = register(
        "glider", p_340999_ -> p_340999_.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
    );
    public static final DataComponentType<ResourceLocation> TOOLTIP_STYLE = register(
        "tooltip_style", p_358141_ -> p_358141_.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<DeathProtection> DEATH_PROTECTION = register(
        "death_protection", p_358133_ -> p_358133_.persistent(DeathProtection.CODEC).networkSynchronized(DeathProtection.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ItemEnchantments> STORED_ENCHANTMENTS = register(
        "stored_enchantments", p_331708_ -> p_331708_.persistent(ItemEnchantments.CODEC).networkSynchronized(ItemEnchantments.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<DyedItemColor> DYED_COLOR = register(
        "dyed_color", p_331118_ -> p_331118_.persistent(DyedItemColor.CODEC).networkSynchronized(DyedItemColor.STREAM_CODEC)
    );
    public static final DataComponentType<MapItemColor> MAP_COLOR = register(
        "map_color", p_335015_ -> p_335015_.persistent(MapItemColor.CODEC).networkSynchronized(MapItemColor.STREAM_CODEC)
    );
    public static final DataComponentType<MapId> MAP_ID = register("map_id", p_329955_ -> p_329955_.persistent(MapId.CODEC).networkSynchronized(MapId.STREAM_CODEC));
    public static final DataComponentType<MapDecorations> MAP_DECORATIONS = register(
        "map_decorations", p_333417_ -> p_333417_.persistent(MapDecorations.CODEC).cacheEncoding()
    );
    public static final DataComponentType<MapPostProcessing> MAP_POST_PROCESSING = register(
        "map_post_processing", p_335188_ -> p_335188_.networkSynchronized(MapPostProcessing.STREAM_CODEC)
    );
    public static final DataComponentType<ChargedProjectiles> CHARGED_PROJECTILES = register(
        "charged_projectiles", p_335344_ -> p_335344_.persistent(ChargedProjectiles.CODEC).networkSynchronized(ChargedProjectiles.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<BundleContents> BUNDLE_CONTENTS = register(
        "bundle_contents", p_328223_ -> p_328223_.persistent(BundleContents.CODEC).networkSynchronized(BundleContents.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<PotionContents> POTION_CONTENTS = register(
        "potion_contents", p_331403_ -> p_331403_.persistent(PotionContents.CODEC).networkSynchronized(PotionContents.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<SuspiciousStewEffects> SUSPICIOUS_STEW_EFFECTS = register(
        "suspicious_stew_effects", p_333712_ -> p_333712_.persistent(SuspiciousStewEffects.CODEC).networkSynchronized(SuspiciousStewEffects.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<WritableBookContent> WRITABLE_BOOK_CONTENT = register(
        "writable_book_content", p_335814_ -> p_335814_.persistent(WritableBookContent.CODEC).networkSynchronized(WritableBookContent.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<WrittenBookContent> WRITTEN_BOOK_CONTENT = register(
        "written_book_content", p_330688_ -> p_330688_.persistent(WrittenBookContent.CODEC).networkSynchronized(WrittenBookContent.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ArmorTrim> TRIM = register(
        "trim", p_358138_ -> p_358138_.persistent(ArmorTrim.CODEC).networkSynchronized(ArmorTrim.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<DebugStickState> DEBUG_STICK_STATE = register(
        "debug_stick_state", p_330393_ -> p_330393_.persistent(DebugStickState.CODEC).cacheEncoding()
    );
    public static final DataComponentType<CustomData> ENTITY_DATA = register(
        "entity_data", p_330635_ -> p_330635_.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC)
    );
    public static final DataComponentType<CustomData> BUCKET_ENTITY_DATA = register(
        "bucket_entity_data", p_335954_ -> p_335954_.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC)
    );
    public static final DataComponentType<CustomData> BLOCK_ENTITY_DATA = register(
        "block_entity_data", p_329366_ -> p_329366_.persistent(CustomData.CODEC_WITH_ID).networkSynchronized(CustomData.STREAM_CODEC)
    );
    public static final DataComponentType<Holder<Instrument>> INSTRUMENT = register(
        "instrument", p_330109_ -> p_330109_.persistent(Instrument.CODEC).networkSynchronized(Instrument.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<OminousBottleAmplifier> OMINOUS_BOTTLE_AMPLIFIER = register(
        "ominous_bottle_amplifier", p_358136_ -> p_358136_.persistent(OminousBottleAmplifier.CODEC).networkSynchronized(OminousBottleAmplifier.STREAM_CODEC)
    );
    public static final DataComponentType<JukeboxPlayable> JUKEBOX_PLAYABLE = register(
        "jukebox_playable", p_341000_ -> p_341000_.persistent(JukeboxPlayable.CODEC).networkSynchronized(JukeboxPlayable.STREAM_CODEC)
    );
    public static final DataComponentType<List<ResourceKey<Recipe<?>>>> RECIPES = register(
        "recipes", p_358134_ -> p_358134_.persistent(ResourceKey.codec(Registries.RECIPE).listOf()).cacheEncoding()
    );
    public static final DataComponentType<LodestoneTracker> LODESTONE_TRACKER = register(
        "lodestone_tracker", p_333432_ -> p_333432_.persistent(LodestoneTracker.CODEC).networkSynchronized(LodestoneTracker.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<FireworkExplosion> FIREWORK_EXPLOSION = register(
        "firework_explosion", p_331824_ -> p_331824_.persistent(FireworkExplosion.CODEC).networkSynchronized(FireworkExplosion.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<Fireworks> FIREWORKS = register(
        "fireworks", p_335894_ -> p_335894_.persistent(Fireworks.CODEC).networkSynchronized(Fireworks.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ResolvableProfile> PROFILE = register(
        "profile", p_334854_ -> p_334854_.persistent(ResolvableProfile.CODEC).networkSynchronized(ResolvableProfile.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ResourceLocation> NOTE_BLOCK_SOUND = register(
        "note_block_sound", p_333150_ -> p_333150_.persistent(ResourceLocation.CODEC).networkSynchronized(ResourceLocation.STREAM_CODEC)
    );
    public static final DataComponentType<BannerPatternLayers> BANNER_PATTERNS = register(
        "banner_patterns", p_328399_ -> p_328399_.persistent(BannerPatternLayers.CODEC).networkSynchronized(BannerPatternLayers.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<DyeColor> BASE_COLOR = register(
        "base_color", p_328641_ -> p_328641_.persistent(DyeColor.CODEC).networkSynchronized(DyeColor.STREAM_CODEC)
    );
    public static final DataComponentType<PotDecorations> POT_DECORATIONS = register(
        "pot_decorations", p_336126_ -> p_336126_.persistent(PotDecorations.CODEC).networkSynchronized(PotDecorations.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<ItemContainerContents> CONTAINER = register(
        "container", p_329021_ -> p_329021_.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<BlockItemStateProperties> BLOCK_STATE = register(
        "block_state", p_329706_ -> p_329706_.persistent(BlockItemStateProperties.CODEC).networkSynchronized(BlockItemStateProperties.STREAM_CODEC).cacheEncoding()
    );
    public static final DataComponentType<List<BeehiveBlockEntity.Occupant>> BEES = register(
        "bees",
        p_329155_ -> p_329155_.persistent(BeehiveBlockEntity.Occupant.LIST_CODEC)
                .networkSynchronized(BeehiveBlockEntity.Occupant.STREAM_CODEC.apply(ByteBufCodecs.list()))
                .cacheEncoding()
    );
    public static final DataComponentType<LockCode> LOCK = register("lock", p_327916_ -> p_327916_.persistent(LockCode.CODEC));
    public static final DataComponentType<SeededContainerLoot> CONTAINER_LOOT = register(
        "container_loot", p_332758_ -> p_332758_.persistent(SeededContainerLoot.CODEC)
    );
    public static final DataComponentMap COMMON_ITEM_COMPONENTS = DataComponentMap.builder()
        .set(MAX_STACK_SIZE, 64)
        .set(LORE, ItemLore.EMPTY)
        .set(ENCHANTMENTS, ItemEnchantments.EMPTY)
        .set(REPAIR_COST, 0)
        .set(ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
        .set(RARITY, Rarity.COMMON)
        .build();

    public static DataComponentType<?> bootstrap(Registry<DataComponentType<?>> p_330257_) {
        return CUSTOM_DATA;
    }

    private static <T> DataComponentType<T> register(String p_335254_, UnaryOperator<DataComponentType.Builder<T>> p_329979_) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, p_335254_, p_329979_.apply(DataComponentType.builder()).build());
    }
}