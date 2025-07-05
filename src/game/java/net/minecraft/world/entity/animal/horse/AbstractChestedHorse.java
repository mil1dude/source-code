package net.minecraft.world.entity.animal.horse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public abstract class AbstractChestedHorse extends AbstractHorse {
    private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);
    private final EntityDimensions babyDimensions;

    protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> p_30485_, Level p_30486_) {
        super(p_30485_, p_30486_);
        this.canGallop = false;
        this.babyDimensions = p_30485_.getDimensions()
            .withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0F, p_30485_.getHeight() - 0.15625F, 0.0F))
            .scale(0.5F);
    }

    @Override
    protected void randomizeAttributes(RandomSource p_218803_) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)generateMaxHealth(p_218803_::nextInt));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_335877_) {
        super.defineSynchedData(p_335877_);
        p_335877_.define(DATA_ID_CHEST, false);
    }

    public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
        return createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, 0.175F).add(Attributes.JUMP_STRENGTH, 0.5);
    }

    public boolean hasChest() {
        return this.entityData.get(DATA_ID_CHEST);
    }

    public void setChest(boolean p_30505_) {
        this.entityData.set(DATA_ID_CHEST, p_30505_);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose p_334387_) {
        return this.isBaby() ? this.babyDimensions : super.getDefaultDimensions(p_334387_);
    }

    @Override
    protected void dropEquipment(ServerLevel p_365262_) {
        super.dropEquipment(p_365262_);
        if (this.hasChest()) {
            this.spawnAtLocation(p_365262_, Blocks.CHEST);
            this.setChest(false);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_30496_) {
        super.addAdditionalSaveData(p_30496_);
        p_30496_.putBoolean("ChestedHorse", this.hasChest());
        if (this.hasChest()) {
            ListTag listtag = new ListTag();

            for (int i = 1; i < this.inventory.getContainerSize(); i++) {
                ItemStack itemstack = this.inventory.getItem(i);
                if (!itemstack.isEmpty()) {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putByte("Slot", (byte)(i - 1));
                    listtag.add(itemstack.save(this.registryAccess(), compoundtag));
                }
            }

            p_30496_.put("Items", listtag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_30488_) {
        super.readAdditionalSaveData(p_30488_);
        this.setChest(p_30488_.getBoolean("ChestedHorse"));
        this.createInventory();
        if (this.hasChest()) {
            ListTag listtag = p_30488_.getList("Items", 10);

            for (int i = 0; i < listtag.size(); i++) {
                CompoundTag compoundtag = listtag.getCompound(i);
                int j = compoundtag.getByte("Slot") & 255;
                if (j < this.inventory.getContainerSize() - 1) {
                    this.inventory.setItem(j + 1, ItemStack.parse(this.registryAccess(), compoundtag).orElse(ItemStack.EMPTY));
                }
            }
        }

        this.syncSaddleToClients();
    }

    @Override
    public SlotAccess getSlot(int p_149479_) {
        return p_149479_ == 499 ? new SlotAccess() {
            @Override
            public ItemStack get() {
                return AbstractChestedHorse.this.hasChest() ? new ItemStack(Items.CHEST) : ItemStack.EMPTY;
            }

            @Override
            public boolean set(ItemStack p_149485_) {
                if (p_149485_.isEmpty()) {
                    if (AbstractChestedHorse.this.hasChest()) {
                        AbstractChestedHorse.this.setChest(false);
                        AbstractChestedHorse.this.createInventory();
                    }

                    return true;
                } else if (p_149485_.is(Items.CHEST)) {
                    if (!AbstractChestedHorse.this.hasChest()) {
                        AbstractChestedHorse.this.setChest(true);
                        AbstractChestedHorse.this.createInventory();
                    }

                    return true;
                } else {
                    return false;
                }
            }
        } : super.getSlot(p_149479_);
    }

    @Override
    public InteractionResult mobInteract(Player p_30493_, InteractionHand p_30494_) {
        boolean flag = !this.isBaby() && this.isTamed() && p_30493_.isSecondaryUseActive();
        if (!this.isVehicle() && !flag) {
            ItemStack itemstack = p_30493_.getItemInHand(p_30494_);
            if (!itemstack.isEmpty()) {
                if (this.isFood(itemstack)) {
                    return this.fedFood(p_30493_, itemstack);
                }

                if (!this.isTamed()) {
                    this.makeMad();
                    return InteractionResult.SUCCESS;
                }

                if (!this.hasChest() && itemstack.is(Items.CHEST)) {
                    this.equipChest(p_30493_, itemstack);
                    return InteractionResult.SUCCESS;
                }
            }

            return super.mobInteract(p_30493_, p_30494_);
        } else {
            return super.mobInteract(p_30493_, p_30494_);
        }
    }

    private void equipChest(Player p_250937_, ItemStack p_251558_) {
        this.setChest(true);
        this.playChestEquipsSound();
        p_251558_.consume(1, p_250937_);
        this.createInventory();
    }

    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? 5 : 0;
    }
}