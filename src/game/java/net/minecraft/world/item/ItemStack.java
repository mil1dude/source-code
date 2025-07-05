package net.minecraft.world.item;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.DataResult.Error;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.NullOps;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.saveddata.maps.MapId;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public final class ItemStack implements DataComponentHolder {
    private static final List<Component> OP_NBT_WARNING = List.of(
        Component.translatable("item.op_warning.line1").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
        Component.translatable("item.op_warning.line2").withStyle(ChatFormatting.RED),
        Component.translatable("item.op_warning.line3").withStyle(ChatFormatting.RED)
    );
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(
        () -> RecordCodecBuilder.create(
                p_359412_ -> p_359412_.group(
                            Item.CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                            ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
                            DataComponentPatch.CODEC
                                .optionalFieldOf("components", DataComponentPatch.EMPTY)
                                .forGetter(p_327171_ -> p_327171_.components.asPatch())
                        )
                        .apply(p_359412_, ItemStack::new)
            )
    );
    public static final Codec<ItemStack> SINGLE_ITEM_CODEC = Codec.lazyInitialized(
        () -> RecordCodecBuilder.create(
                p_359410_ -> p_359410_.group(
                            Item.CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                            DataComponentPatch.CODEC
                                .optionalFieldOf("components", DataComponentPatch.EMPTY)
                                .forGetter(p_327155_ -> p_327155_.components.asPatch())
                        )
                        .apply(p_359410_, (p_327172_, p_327173_) -> new ItemStack(p_327172_, 1, p_327173_))
            )
    );
    public static final Codec<ItemStack> STRICT_CODEC = CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> STRICT_SINGLE_ITEM_CODEC = SINGLE_ITEM_CODEC.validate(ItemStack::validateStrict);
    public static final Codec<ItemStack> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
        .xmap(p_327153_ -> p_327153_.orElse(ItemStack.EMPTY), p_327154_ -> p_327154_.isEmpty() ? Optional.empty() : Optional.of(p_327154_));
    public static final Codec<ItemStack> SIMPLE_ITEM_CODEC = Item.CODEC.xmap(ItemStack::new, ItemStack::getItemHolder);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> OPTIONAL_STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, Holder<Item>> ITEM_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.ITEM);

        public ItemStack decode(RegistryFriendlyByteBuf p_328393_) {
            int i = p_328393_.readVarInt();
            if (i <= 0) {
                return ItemStack.EMPTY;
            } else {
                Holder<Item> holder = ITEM_STREAM_CODEC.decode(p_328393_);
                DataComponentPatch datacomponentpatch = DataComponentPatch.STREAM_CODEC.decode(p_328393_);
                return new ItemStack(holder, i, datacomponentpatch);
            }
        }

        public void encode(RegistryFriendlyByteBuf p_332266_, ItemStack p_335702_) {
            if (p_335702_.isEmpty()) {
                p_332266_.writeVarInt(0);
            } else {
                p_332266_.writeVarInt(p_335702_.getCount());
                ITEM_STREAM_CODEC.encode(p_332266_, p_335702_.getItemHolder());
                DataComponentPatch.STREAM_CODEC.encode(p_332266_, p_335702_.components.asPatch());
            }
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemStack> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
        public ItemStack decode(RegistryFriendlyByteBuf p_327992_) {
            ItemStack itemstack = ItemStack.OPTIONAL_STREAM_CODEC.decode(p_327992_);
            if (itemstack.isEmpty()) {
                throw new DecoderException("Empty ItemStack not allowed");
            } else {
                return itemstack;
            }
        }

        public void encode(RegistryFriendlyByteBuf p_331904_, ItemStack p_328866_) {
            if (p_328866_.isEmpty()) {
                throw new EncoderException("Empty ItemStack not allowed");
            } else {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(p_331904_, p_328866_);
            }
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> OPTIONAL_LIST_STREAM_CODEC = OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Void)null);
    private static final Component DISABLED_ITEM_TOOLTIP = Component.translatable("item.disabled").withStyle(ChatFormatting.RED);
    private int count;
    private int popTime;
    @Deprecated
    @Nullable
    private final Item item;
    final PatchedDataComponentMap components;
    @Nullable
    private Entity entityRepresentation;

    private static DataResult<ItemStack> validateStrict(ItemStack p_332181_) {
        DataResult<Unit> dataresult = validateComponents(p_332181_.getComponents());
        if (dataresult.isError()) {
            return dataresult.map(p_327165_ -> p_332181_);
        } else {
            return p_332181_.getCount() > p_332181_.getMaxStackSize()
                ? DataResult.error(() -> "Item stack with stack size of " + p_332181_.getCount() + " was larger than maximum: " + p_332181_.getMaxStackSize())
                : DataResult.success(p_332181_);
        }
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemStack> validatedStreamCodec(final StreamCodec<RegistryFriendlyByteBuf, ItemStack> p_332790_) {
        return new StreamCodec<RegistryFriendlyByteBuf, ItemStack>() {
            public ItemStack decode(RegistryFriendlyByteBuf p_330762_) {
                ItemStack itemstack = p_332790_.decode(p_330762_);
                if (!itemstack.isEmpty()) {
                    RegistryOps<Unit> registryops = p_330762_.registryAccess().createSerializationContext(NullOps.INSTANCE);
                    ItemStack.CODEC.encodeStart(registryops, itemstack).getOrThrow(DecoderException::new);
                }

                return itemstack;
            }

            public void encode(RegistryFriendlyByteBuf p_336131_, ItemStack p_329943_) {
                p_332790_.encode(p_336131_, p_329943_);
            }
        };
    }

    public Optional<TooltipComponent> getTooltipImage() {
        return this.getItem().getTooltipImage(this);
    }

    @Override
    public DataComponentMap getComponents() {
        return (DataComponentMap)(!this.isEmpty() ? this.components : DataComponentMap.EMPTY);
    }

    public DataComponentMap getPrototype() {
        return !this.isEmpty() ? this.getItem().components() : DataComponentMap.EMPTY;
    }

    public DataComponentPatch getComponentsPatch() {
        return !this.isEmpty() ? this.components.asPatch() : DataComponentPatch.EMPTY;
    }

    public DataComponentMap immutableComponents() {
        return !this.isEmpty() ? this.components.toImmutableMap() : DataComponentMap.EMPTY;
    }

    public boolean hasNonDefault(DataComponentType<?> p_377204_) {
        return !this.isEmpty() && this.components.hasNonDefault(p_377204_);
    }

    public ItemStack(ItemLike p_41599_) {
        this(p_41599_, 1);
    }

    public ItemStack(Holder<Item> p_204116_) {
        this(p_204116_.value(), 1);
    }

    public ItemStack(Holder<Item> p_310702_, int p_41605_, DataComponentPatch p_328221_) {
        this(p_310702_.value(), p_41605_, PatchedDataComponentMap.fromPatch(p_310702_.value().components(), p_328221_));
    }

    public ItemStack(Holder<Item> p_220155_, int p_220156_) {
        this(p_220155_.value(), p_220156_);
    }

    public ItemStack(ItemLike p_41601_, int p_41602_) {
        this(p_41601_, p_41602_, new PatchedDataComponentMap(p_41601_.asItem().components()));
    }

    private ItemStack(ItemLike p_331826_, int p_332766_, PatchedDataComponentMap p_333722_) {
        this.item = p_331826_.asItem();
        this.count = p_332766_;
        this.components = p_333722_;
        this.getItem().verifyComponentsAfterLoad(this);
    }

    private ItemStack(@Nullable Void p_282703_) {
        this.item = null;
        this.components = new PatchedDataComponentMap(DataComponentMap.EMPTY);
    }

    public static DataResult<Unit> validateComponents(DataComponentMap p_336343_) {
        if (p_336343_.has(DataComponents.MAX_DAMAGE) && p_336343_.getOrDefault(DataComponents.MAX_STACK_SIZE, 1) > 1) {
            return DataResult.error(() -> "Item cannot be both damageable and stackable");
        } else {
            ItemContainerContents itemcontainercontents = p_336343_.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);

            for (ItemStack itemstack : itemcontainercontents.nonEmptyItems()) {
                int i = itemstack.getCount();
                int j = itemstack.getMaxStackSize();
                if (i > j) {
                    return DataResult.error(() -> "Item stack with count of " + i + " was larger than maximum: " + j);
                }
            }

            return DataResult.success(Unit.INSTANCE);
        }
    }

    public static Optional<ItemStack> parse(HolderLookup.Provider p_332204_, Tag p_336056_) {
        return CODEC.parse(p_332204_.createSerializationContext(NbtOps.INSTANCE), p_336056_)
            .resultOrPartial(p_327167_ -> LOGGER.error("Tried to load invalid item: '{}'", p_327167_));
    }

    public static ItemStack parseOptional(HolderLookup.Provider p_333870_, CompoundTag p_328391_) {
        return p_328391_.isEmpty() ? EMPTY : parse(p_333870_, p_328391_).orElse(EMPTY);
    }

    public boolean isEmpty() {
        return this == EMPTY || this.item == Items.AIR || this.count <= 0;
    }

    public boolean isItemEnabled(FeatureFlagSet p_250869_) {
        return this.isEmpty() || this.getItem().isEnabled(p_250869_);
    }

    public ItemStack split(int p_41621_) {
        int i = Math.min(p_41621_, this.getCount());
        ItemStack itemstack = this.copyWithCount(i);
        this.shrink(i);
        return itemstack;
    }

    public ItemStack copyAndClear() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack itemstack = this.copy();
            this.setCount(0);
            return itemstack;
        }
    }

    public Item getItem() {
        return this.isEmpty() ? Items.AIR : this.item;
    }

    public Holder<Item> getItemHolder() {
        return this.getItem().builtInRegistryHolder();
    }

    public boolean is(TagKey<Item> p_204118_) {
        return this.getItem().builtInRegistryHolder().is(p_204118_);
    }

    public boolean is(Item p_150931_) {
        return this.getItem() == p_150931_;
    }

    public boolean is(Predicate<Holder<Item>> p_220168_) {
        return p_220168_.test(this.getItem().builtInRegistryHolder());
    }

    public boolean is(Holder<Item> p_220166_) {
        return this.getItem().builtInRegistryHolder() == p_220166_;
    }

    public boolean is(HolderSet<Item> p_299078_) {
        return p_299078_.contains(this.getItemHolder());
    }

    public Stream<TagKey<Item>> getTags() {
        return this.getItem().builtInRegistryHolder().tags();
    }

    public InteractionResult useOn(UseOnContext p_41662_) {
        Player player = p_41662_.getPlayer();
        BlockPos blockpos = p_41662_.getClickedPos();
        if (player != null && !player.getAbilities().mayBuild && !this.canPlaceOnBlockInAdventureMode(new BlockInWorld(p_41662_.getLevel(), blockpos, false))) {
            return InteractionResult.PASS;
        } else {
            Item item = this.getItem();
            InteractionResult interactionresult = item.useOn(p_41662_);
            if (player != null && interactionresult instanceof InteractionResult.Success interactionresult$success && interactionresult$success.wasItemInteraction()) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }

            return interactionresult;
        }
    }

    public float getDestroySpeed(BlockState p_41692_) {
        return this.getItem().getDestroySpeed(this, p_41692_);
    }

    public InteractionResult use(Level p_41683_, Player p_41684_, InteractionHand p_41685_) {
        ItemStack itemstack = this.copy();
        boolean flag = this.getUseDuration(p_41684_) <= 0;
        InteractionResult interactionresult = this.getItem().use(p_41683_, p_41684_, p_41685_);
        return (InteractionResult)(flag && interactionresult instanceof InteractionResult.Success interactionresult$success
            ? interactionresult$success.heldItemTransformedTo(
                interactionresult$success.heldItemTransformedTo() == null
                    ? this.applyAfterUseComponentSideEffects(p_41684_, itemstack)
                    : interactionresult$success.heldItemTransformedTo().applyAfterUseComponentSideEffects(p_41684_, itemstack)
            )
            : interactionresult);
    }

    public ItemStack finishUsingItem(Level p_41672_, LivingEntity p_41673_) {
        ItemStack itemstack = this.copy();
        ItemStack itemstack1 = this.getItem().finishUsingItem(this, p_41672_, p_41673_);
        return itemstack1.applyAfterUseComponentSideEffects(p_41673_, itemstack);
    }

    private ItemStack applyAfterUseComponentSideEffects(LivingEntity p_367870_, ItemStack p_361647_) {
        UseRemainder useremainder = p_361647_.get(DataComponents.USE_REMAINDER);
        UseCooldown usecooldown = p_361647_.get(DataComponents.USE_COOLDOWN);
        int i = p_361647_.getCount();
        ItemStack itemstack = this;
        if (useremainder != null) {
            itemstack = useremainder.convertIntoRemainder(this, i, p_367870_.hasInfiniteMaterials(), p_367870_::handleExtraItemsCreatedOnUse);
        }

        if (usecooldown != null) {
            usecooldown.apply(p_361647_, p_367870_);
        }

        return itemstack;
    }

    public Tag save(HolderLookup.Provider p_330500_, Tag p_332574_) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        } else {
            return CODEC.encode(this, p_330500_.createSerializationContext(NbtOps.INSTANCE), p_332574_).getOrThrow();
        }
    }

    public Tag save(HolderLookup.Provider p_328490_) {
        if (this.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        } else {
            return CODEC.encodeStart(p_328490_.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }
    }

    public Tag saveOptional(HolderLookup.Provider p_335413_) {
        return (Tag)(this.isEmpty() ? new CompoundTag() : this.save(p_335413_, new CompoundTag()));
    }

    public int getMaxStackSize() {
        return this.getOrDefault(DataComponents.MAX_STACK_SIZE, Integer.valueOf(1));
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
    }

    public boolean isDamageableItem() {
        return this.has(DataComponents.MAX_DAMAGE) && !this.has(DataComponents.UNBREAKABLE) && this.has(DataComponents.DAMAGE);
    }

    public boolean isDamaged() {
        return this.isDamageableItem() && this.getDamageValue() > 0;
    }

    public int getDamageValue() {
        return Mth.clamp(this.getOrDefault(DataComponents.DAMAGE, Integer.valueOf(0)), 0, this.getMaxDamage());
    }

    public void setDamageValue(int p_41722_) {
        this.set(DataComponents.DAMAGE, Mth.clamp(p_41722_, 0, this.getMaxDamage()));
    }

    public int getMaxDamage() {
        return this.getOrDefault(DataComponents.MAX_DAMAGE, Integer.valueOf(0));
    }

    public boolean isBroken() {
        return this.isDamageableItem() && this.getDamageValue() >= this.getMaxDamage();
    }

    public boolean nextDamageWillBreak() {
        return this.isDamageableItem() && this.getDamageValue() >= this.getMaxDamage() - 1;
    }

    public void hurtAndBreak(int p_220158_, ServerLevel p_342197_, @Nullable ServerPlayer p_220160_, Consumer<Item> p_343361_) {
        int i = this.processDurabilityChange(p_220158_, p_342197_, p_220160_);
        if (i != 0) {
            this.applyDamage(this.getDamageValue() + i, p_220160_, p_343361_);
        }
    }

    private int processDurabilityChange(int p_362423_, ServerLevel p_364910_, @Nullable ServerPlayer p_365570_) {
        if (!this.isDamageableItem()) {
            return 0;
        } else if (p_365570_ != null && p_365570_.hasInfiniteMaterials()) {
            return 0;
        } else {
            return p_362423_ > 0 ? EnchantmentHelper.processDurabilityChange(p_364910_, this, p_362423_) : p_362423_;
        }
    }

    private void applyDamage(int p_365629_, @Nullable ServerPlayer p_367167_, Consumer<Item> p_364849_) {
        if (p_367167_ != null) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(p_367167_, this, p_365629_);
        }

        this.setDamageValue(p_365629_);
        if (this.isBroken()) {
            Item item = this.getItem();
            this.shrink(1);
            p_364849_.accept(item);
        }
    }

    public void hurtWithoutBreaking(int p_363289_, Player p_369700_) {
        if (p_369700_ instanceof ServerPlayer serverplayer) {
            int i = this.processDurabilityChange(p_363289_, serverplayer.serverLevel(), serverplayer);
            if (i == 0) {
                return;
            }

            int j = Math.min(this.getDamageValue() + i, this.getMaxDamage() - 1);
            this.applyDamage(j, serverplayer, p_359411_ -> {
            });
        }
    }

    public void hurtAndBreak(int p_41623_, LivingEntity p_41624_, EquipmentSlot p_335324_) {
        if (p_41624_.level() instanceof ServerLevel serverlevel) {
            this.hurtAndBreak(
                p_41623_,
                serverlevel,
                p_41624_ instanceof ServerPlayer serverplayer ? serverplayer : null,
                p_341563_ -> p_41624_.onEquippedItemBroken(p_341563_, p_335324_)
            );
        }
    }

    public ItemStack hurtAndConvertOnBreak(int p_343792_, ItemLike p_344647_, LivingEntity p_342270_, EquipmentSlot p_345347_) {
        this.hurtAndBreak(p_343792_, p_342270_, p_345347_);
        if (this.isEmpty()) {
            ItemStack itemstack = this.transmuteCopyIgnoreEmpty(p_344647_, 1);
            if (itemstack.isDamageableItem()) {
                itemstack.setDamageValue(0);
            }

            return itemstack;
        } else {
            return this;
        }
    }

    public boolean isBarVisible() {
        return this.getItem().isBarVisible(this);
    }

    public int getBarWidth() {
        return this.getItem().getBarWidth(this);
    }

    public int getBarColor() {
        return this.getItem().getBarColor(this);
    }

    public boolean overrideStackedOnOther(Slot p_150927_, ClickAction p_150928_, Player p_150929_) {
        return this.getItem().overrideStackedOnOther(this, p_150927_, p_150928_, p_150929_);
    }

    public boolean overrideOtherStackedOnMe(ItemStack p_150933_, Slot p_150934_, ClickAction p_150935_, Player p_150936_, SlotAccess p_150937_) {
        return this.getItem().overrideOtherStackedOnMe(this, p_150933_, p_150934_, p_150935_, p_150936_, p_150937_);
    }

    public boolean hurtEnemy(LivingEntity p_41641_, LivingEntity p_366644_) {
        Item item = this.getItem();
        if (item.hurtEnemy(this, p_41641_, p_366644_)) {
            if (p_366644_ instanceof Player player) {
                player.awardStat(Stats.ITEM_USED.get(item));
            }

            return true;
        } else {
            return false;
        }
    }

    public void postHurtEnemy(LivingEntity p_343236_, LivingEntity p_363977_) {
        this.getItem().postHurtEnemy(this, p_343236_, p_363977_);
    }

    public void mineBlock(Level p_41687_, BlockState p_41688_, BlockPos p_41689_, Player p_41690_) {
        Item item = this.getItem();
        if (item.mineBlock(this, p_41687_, p_41688_, p_41689_, p_41690_)) {
            p_41690_.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public boolean isCorrectToolForDrops(BlockState p_41736_) {
        return this.getItem().isCorrectToolForDrops(this, p_41736_);
    }

    public InteractionResult interactLivingEntity(Player p_41648_, LivingEntity p_41649_, InteractionHand p_41650_) {
        return this.getItem().interactLivingEntity(this, p_41648_, p_41649_, p_41650_);
    }

    public ItemStack copy() {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack itemstack = new ItemStack(this.getItem(), this.count, this.components.copy());
            itemstack.setPopTime(this.getPopTime());
            return itemstack;
        }
    }

    public ItemStack copyWithCount(int p_256354_) {
        if (this.isEmpty()) {
            return EMPTY;
        } else {
            ItemStack itemstack = this.copy();
            itemstack.setCount(p_256354_);
            return itemstack;
        }
    }

    public ItemStack transmuteCopy(ItemLike p_345281_) {
        return this.transmuteCopy(p_345281_, this.getCount());
    }

    public ItemStack transmuteCopy(ItemLike p_334328_, int p_334821_) {
        return this.isEmpty() ? EMPTY : this.transmuteCopyIgnoreEmpty(p_334328_, p_334821_);
    }

    private ItemStack transmuteCopyIgnoreEmpty(ItemLike p_332114_, int p_333334_) {
        return new ItemStack(p_332114_.asItem().builtInRegistryHolder(), p_333334_, this.components.asPatch());
    }

    public static boolean matches(ItemStack p_41729_, ItemStack p_41730_) {
        if (p_41729_ == p_41730_) {
            return true;
        } else {
            return p_41729_.getCount() != p_41730_.getCount() ? false : isSameItemSameComponents(p_41729_, p_41730_);
        }
    }

    @Deprecated
    public static boolean listMatches(List<ItemStack> p_335471_, List<ItemStack> p_334624_) {
        if (p_335471_.size() != p_334624_.size()) {
            return false;
        } else {
            for (int i = 0; i < p_335471_.size(); i++) {
                if (!matches(p_335471_.get(i), p_334624_.get(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isSameItem(ItemStack p_287761_, ItemStack p_287676_) {
        return p_287761_.is(p_287676_.getItem());
    }

    public static boolean isSameItemSameComponents(ItemStack p_334397_, ItemStack p_331609_) {
        if (!p_334397_.is(p_331609_.getItem())) {
            return false;
        } else {
            return p_334397_.isEmpty() && p_331609_.isEmpty() ? true : Objects.equals(p_334397_.components, p_331609_.components);
        }
    }

    public static MapCodec<ItemStack> lenientOptionalFieldOf(String p_336149_) {
        return CODEC.lenientOptionalFieldOf(p_336149_)
            .xmap(p_327174_ -> p_327174_.orElse(EMPTY), p_327162_ -> p_327162_.isEmpty() ? Optional.empty() : Optional.of(p_327162_));
    }

    public static int hashItemAndComponents(@Nullable ItemStack p_334004_) {
        if (p_334004_ != null) {
            int i = 31 + p_334004_.getItem().hashCode();
            return 31 * i + p_334004_.getComponents().hashCode();
        } else {
            return 0;
        }
    }

    @Deprecated
    public static int hashStackList(List<ItemStack> p_333449_) {
        int i = 0;

        for (ItemStack itemstack : p_333449_) {
            i = i * 31 + hashItemAndComponents(itemstack);
        }

        return i;
    }

    @Override
    public String toString() {
        return this.getCount() + " " + this.getItem();
    }

    public void inventoryTick(Level p_41667_, Entity p_41668_, int p_41669_, boolean p_41670_) {
        if (this.popTime > 0) {
            this.popTime--;
        }

        if (this.getItem() != null) {
            this.getItem().inventoryTick(this, p_41667_, p_41668_, p_41669_, p_41670_);
        }
    }

    public void onCraftedBy(Level p_41679_, Player p_41680_, int p_41681_) {
        p_41680_.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), p_41681_);
        this.getItem().onCraftedBy(this, p_41679_, p_41680_);
    }

    public void onCraftedBySystem(Level p_311164_) {
        this.getItem().onCraftedPostProcess(this, p_311164_);
    }

    public int getUseDuration(LivingEntity p_343439_) {
        return this.getItem().getUseDuration(this, p_343439_);
    }

    public ItemUseAnimation getUseAnimation() {
        return this.getItem().getUseAnimation(this);
    }

    public void releaseUsing(Level p_41675_, LivingEntity p_41676_, int p_41677_) {
        ItemStack itemstack = this.copy();
        if (this.getItem().releaseUsing(this, p_41675_, p_41676_, p_41677_)) {
            ItemStack itemstack1 = this.applyAfterUseComponentSideEffects(p_41676_, itemstack);
            if (itemstack1 != this) {
                p_41676_.setItemInHand(p_41676_.getUsedItemHand(), itemstack1);
            }
        }
    }

    public boolean useOnRelease() {
        return this.getItem().useOnRelease(this);
    }

    @Nullable
    public <T> T set(DataComponentType<? super T> p_332666_, @Nullable T p_335655_) {
        return this.components.set(p_332666_, p_335655_);
    }

    @Nullable
    public <T, U> T update(DataComponentType<T> p_331418_, T p_327708_, U p_332086_, BiFunction<T, U, T> p_329834_) {
        return this.set(p_331418_, p_329834_.apply(this.getOrDefault(p_331418_, p_327708_), p_332086_));
    }

    @Nullable
    public <T> T update(DataComponentType<T> p_329905_, T p_329705_, UnaryOperator<T> p_335114_) {
        T t = this.getOrDefault(p_329905_, p_329705_);
        return this.set(p_329905_, p_335114_.apply(t));
    }

    @Nullable
    public <T> T remove(DataComponentType<? extends T> p_333259_) {
        return this.components.remove(p_333259_);
    }

    public void applyComponentsAndValidate(DataComponentPatch p_336111_) {
        DataComponentPatch datacomponentpatch = this.components.asPatch();
        this.components.applyPatch(p_336111_);
        Optional<Error<ItemStack>> optional = validateStrict(this).error();
        if (optional.isPresent()) {
            LOGGER.error("Failed to apply component patch '{}' to item: '{}'", p_336111_, optional.get().message());
            this.components.restorePatch(datacomponentpatch);
        } else {
            this.getItem().verifyComponentsAfterLoad(this);
        }
    }

    public void applyComponents(DataComponentPatch p_328534_) {
        this.components.applyPatch(p_328534_);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public void applyComponents(DataComponentMap p_335208_) {
        this.components.setAll(p_335208_);
        this.getItem().verifyComponentsAfterLoad(this);
    }

    public Component getHoverName() {
        Component component = this.getCustomName();
        return component != null ? component : this.getItemName();
    }

    @Nullable
    public Component getCustomName() {
        Component component = this.get(DataComponents.CUSTOM_NAME);
        if (component != null) {
            return component;
        } else {
            WrittenBookContent writtenbookcontent = this.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (writtenbookcontent != null) {
                String s = writtenbookcontent.title().raw();
                if (!StringUtil.isBlank(s)) {
                    return Component.literal(s);
                }
            }

            return null;
        }
    }

    public Component getItemName() {
        return this.getItem().getName(this);
    }

    public Component getStyledHoverName() {
        MutableComponent mutablecomponent = Component.empty().append(this.getHoverName()).withStyle(this.getRarity().color());
        if (this.has(DataComponents.CUSTOM_NAME)) {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }

        return mutablecomponent;
    }

    private <T extends TooltipProvider> void addToTooltip(
        DataComponentType<T> p_331934_, Item.TooltipContext p_333562_, Consumer<Component> p_334534_, TooltipFlag p_333715_
    ) {
        T t = (T)this.get(p_331934_);
        if (t != null) {
            t.addToTooltip(p_333562_, p_334534_, p_333715_);
        }
    }

    public List<Component> getTooltipLines(Item.TooltipContext p_331329_, @Nullable Player p_41652_, TooltipFlag p_41653_) {
        boolean flag = this.getItem().shouldPrintOpWarning(this, p_41652_);
        if (!p_41653_.isCreative() && this.has(DataComponents.HIDE_TOOLTIP)) {
            return flag ? OP_NBT_WARNING : List.of();
        } else {
            List<Component> list = Lists.newArrayList();
            list.add(this.getStyledHoverName());
            if (!p_41653_.isAdvanced() && !this.has(DataComponents.CUSTOM_NAME)) {
                MapId mapid = this.get(DataComponents.MAP_ID);
                if (mapid != null) {
                    list.add(MapItem.getTooltipForId(mapid));
                }
            }

            Consumer<Component> consumer = list::add;
            if (!this.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
                this.getItem().appendHoverText(this, p_331329_, list, p_41653_);
            }

            this.addToTooltip(DataComponents.JUKEBOX_PLAYABLE, p_331329_, consumer, p_41653_);
            this.addToTooltip(DataComponents.TRIM, p_331329_, consumer, p_41653_);
            this.addToTooltip(DataComponents.STORED_ENCHANTMENTS, p_331329_, consumer, p_41653_);
            this.addToTooltip(DataComponents.ENCHANTMENTS, p_331329_, consumer, p_41653_);
            this.addToTooltip(DataComponents.DYED_COLOR, p_331329_, consumer, p_41653_);
            this.addToTooltip(DataComponents.LORE, p_331329_, consumer, p_41653_);
            this.addAttributeTooltips(consumer, p_41652_);
            this.addToTooltip(DataComponents.UNBREAKABLE, p_331329_, consumer, p_41653_);
            this.addToTooltip(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, p_331329_, consumer, p_41653_);
            this.addToTooltip(DataComponents.SUSPICIOUS_STEW_EFFECTS, p_331329_, consumer, p_41653_);
            AdventureModePredicate adventuremodepredicate = this.get(DataComponents.CAN_BREAK);
            if (adventuremodepredicate != null && adventuremodepredicate.showInTooltip()) {
                consumer.accept(CommonComponents.EMPTY);
                consumer.accept(AdventureModePredicate.CAN_BREAK_HEADER);
                adventuremodepredicate.addToTooltip(consumer);
            }

            AdventureModePredicate adventuremodepredicate1 = this.get(DataComponents.CAN_PLACE_ON);
            if (adventuremodepredicate1 != null && adventuremodepredicate1.showInTooltip()) {
                consumer.accept(CommonComponents.EMPTY);
                consumer.accept(AdventureModePredicate.CAN_PLACE_HEADER);
                adventuremodepredicate1.addToTooltip(consumer);
            }

            if (p_41653_.isAdvanced()) {
                if (this.isDamaged()) {
                    list.add(Component.translatable("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
                }

                list.add(Component.literal(BuiltInRegistries.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
                int i = this.components.size();
                if (i > 0) {
                    list.add(Component.translatable("item.components", i).withStyle(ChatFormatting.DARK_GRAY));
                }
            }

            if (p_41652_ != null && !this.getItem().isEnabled(p_41652_.level().enabledFeatures())) {
                list.add(DISABLED_ITEM_TOOLTIP);
            }

            if (flag) {
                list.addAll(OP_NBT_WARNING);
            }

            return list;
        }
    }

    private void addAttributeTooltips(Consumer<Component> p_333346_, @Nullable Player p_332769_) {
        ItemAttributeModifiers itemattributemodifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (itemattributemodifiers.showInTooltip()) {
            for (EquipmentSlotGroup equipmentslotgroup : EquipmentSlotGroup.values()) {
                MutableBoolean mutableboolean = new MutableBoolean(true);
                this.forEachModifier(equipmentslotgroup, (p_341553_, p_341554_) -> {
                    if (mutableboolean.isTrue()) {
                        p_333346_.accept(CommonComponents.EMPTY);
                        p_333346_.accept(Component.translatable("item.modifiers." + equipmentslotgroup.getSerializedName()).withStyle(ChatFormatting.GRAY));
                        mutableboolean.setFalse();
                    }

                    this.addModifierTooltip(p_333346_, p_332769_, p_341553_, p_341554_);
                });
            }
        }
    }

    private void addModifierTooltip(Consumer<Component> p_332944_, @Nullable Player p_328442_, Holder<Attribute> p_336373_, AttributeModifier p_332746_) {
        double d0 = p_332746_.amount();
        boolean flag = false;
        if (p_328442_ != null) {
            if (p_332746_.is(Item.BASE_ATTACK_DAMAGE_ID)) {
                d0 += p_328442_.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                flag = true;
            } else if (p_332746_.is(Item.BASE_ATTACK_SPEED_ID)) {
                d0 += p_328442_.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                flag = true;
            }
        }

        double d1;
        if (p_332746_.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || p_332746_.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            )
         {
            d1 = d0 * 100.0;
        } else if (p_336373_.is(Attributes.KNOCKBACK_RESISTANCE)) {
            d1 = d0 * 10.0;
        } else {
            d1 = d0;
        }

        if (flag) {
            p_332944_.accept(
                CommonComponents.space()
                    .append(
                        Component.translatable(
                            "attribute.modifier.equals." + p_332746_.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                            Component.translatable(p_336373_.value().getDescriptionId())
                        )
                    )
                    .withStyle(ChatFormatting.DARK_GREEN)
            );
        } else if (d0 > 0.0) {
            p_332944_.accept(
                Component.translatable(
                        "attribute.modifier.plus." + p_332746_.operation().id(),
                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                        Component.translatable(p_336373_.value().getDescriptionId())
                    )
                    .withStyle(p_336373_.value().getStyle(true))
            );
        } else if (d0 < 0.0) {
            p_332944_.accept(
                Component.translatable(
                        "attribute.modifier.take." + p_332746_.operation().id(),
                        ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d1),
                        Component.translatable(p_336373_.value().getDescriptionId())
                    )
                    .withStyle(p_336373_.value().getStyle(false))
            );
        }
    }

    public boolean hasFoil() {
        Boolean obool = this.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        return obool != null ? obool : this.getItem().isFoil(this);
    }

    public Rarity getRarity() {
        Rarity rarity = this.getOrDefault(DataComponents.RARITY, Rarity.COMMON);
        if (!this.isEnchanted()) {
            return rarity;
        } else {
            return switch (rarity) {
                case COMMON, UNCOMMON -> Rarity.RARE;
                case RARE -> Rarity.EPIC;
                default -> rarity;
            };
        }
    }

    public boolean isEnchantable() {
        if (!this.has(DataComponents.ENCHANTABLE)) {
            return false;
        } else {
            ItemEnchantments itemenchantments = this.get(DataComponents.ENCHANTMENTS);
            return itemenchantments != null && itemenchantments.isEmpty();
        }
    }

    public void enchant(Holder<Enchantment> p_342791_, int p_41665_) {
        EnchantmentHelper.updateEnchantments(this, p_341557_ -> p_341557_.upgrade(p_342791_, p_41665_));
    }

    public boolean isEnchanted() {
        return !this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty();
    }

    public ItemEnchantments getEnchantments() {
        return this.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
    }

    public boolean isFramed() {
        return this.entityRepresentation instanceof ItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity p_41637_) {
        if (!this.isEmpty()) {
            this.entityRepresentation = p_41637_;
        }
    }

    @Nullable
    public ItemFrame getFrame() {
        return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
    }

    @Nullable
    public Entity getEntityRepresentation() {
        return !this.isEmpty() ? this.entityRepresentation : null;
    }

    public void forEachModifier(EquipmentSlotGroup p_344758_, BiConsumer<Holder<Attribute>, AttributeModifier> p_342345_) {
        ItemAttributeModifiers itemattributemodifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        itemattributemodifiers.forEach(p_344758_, p_342345_);
        EnchantmentHelper.forEachModifier(this, p_344758_, p_342345_);
    }

    public void forEachModifier(EquipmentSlot p_331036_, BiConsumer<Holder<Attribute>, AttributeModifier> p_334430_) {
        ItemAttributeModifiers itemattributemodifiers = this.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        itemattributemodifiers.forEach(p_331036_, p_334430_);
        EnchantmentHelper.forEachModifier(this, p_331036_, p_334430_);
    }

    public Component getDisplayName() {
        MutableComponent mutablecomponent = Component.empty().append(this.getHoverName());
        if (this.has(DataComponents.CUSTOM_NAME)) {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }

        MutableComponent mutablecomponent1 = ComponentUtils.wrapInSquareBrackets(mutablecomponent);
        if (!this.isEmpty()) {
            mutablecomponent1.withStyle(this.getRarity().color())
                .withStyle(p_220170_ -> p_220170_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this))));
        }

        return mutablecomponent1;
    }

    public boolean canPlaceOnBlockInAdventureMode(BlockInWorld p_331134_) {
        AdventureModePredicate adventuremodepredicate = this.get(DataComponents.CAN_PLACE_ON);
        return adventuremodepredicate != null && adventuremodepredicate.test(p_331134_);
    }

    public boolean canBreakBlockInAdventureMode(BlockInWorld p_333133_) {
        AdventureModePredicate adventuremodepredicate = this.get(DataComponents.CAN_BREAK);
        return adventuremodepredicate != null && adventuremodepredicate.test(p_333133_);
    }

    public int getPopTime() {
        return this.popTime;
    }

    public void setPopTime(int p_41755_) {
        this.popTime = p_41755_;
    }

    public int getCount() {
        return this.isEmpty() ? 0 : this.count;
    }

    public void setCount(int p_41765_) {
        this.count = p_41765_;
    }

    public void limitSize(int p_328100_) {
        if (!this.isEmpty() && this.getCount() > p_328100_) {
            this.setCount(p_328100_);
        }
    }

    public void grow(int p_41770_) {
        this.setCount(this.getCount() + p_41770_);
    }

    public void shrink(int p_41775_) {
        this.grow(-p_41775_);
    }

    public void consume(int p_329683_, @Nullable LivingEntity p_334302_) {
        if (p_334302_ == null || !p_334302_.hasInfiniteMaterials()) {
            this.shrink(p_329683_);
        }
    }

    public ItemStack consumeAndReturn(int p_343693_, @Nullable LivingEntity p_344112_) {
        ItemStack itemstack = this.copyWithCount(p_343693_);
        this.consume(p_343693_, p_344112_);
        return itemstack;
    }

    public void onUseTick(Level p_41732_, LivingEntity p_41733_, int p_41734_) {
        Consumable consumable = this.get(DataComponents.CONSUMABLE);
        if (consumable != null && consumable.shouldEmitParticlesAndSounds(p_41734_)) {
            consumable.emitParticlesAndSounds(p_41733_.getRandom(), p_41733_, this, 5);
        }

        this.getItem().onUseTick(p_41732_, p_41733_, this, p_41734_);
    }

    public void onDestroyed(ItemEntity p_150925_) {
        this.getItem().onDestroyed(p_150925_);
    }

    public SoundEvent getBreakingSound() {
        return this.getItem().getBreakingSound();
    }

    public boolean canBeHurtBy(DamageSource p_334859_) {
        DamageResistant damageresistant = this.get(DataComponents.DAMAGE_RESISTANT);
        return damageresistant == null || !damageresistant.isResistantTo(p_334859_);
    }

    public boolean isValidRepairItem(ItemStack p_368140_) {
        Repairable repairable = this.get(DataComponents.REPAIRABLE);
        return repairable != null && repairable.isValidRepairItem(p_368140_);
    }
}