package net.minecraft.world.entity.npc;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class AbstractVillager extends AgeableMob implements InventoryCarrier, Npc, Merchant {
    private static final EntityDataAccessor<Integer> DATA_UNHAPPY_COUNTER = SynchedEntityData.defineId(AbstractVillager.class, EntityDataSerializers.INT);
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int VILLAGER_SLOT_OFFSET = 300;
    private static final int VILLAGER_INVENTORY_SIZE = 8;
    @Nullable
    private Player tradingPlayer;
    @Nullable
    protected MerchantOffers offers;
    private final SimpleContainer inventory = new SimpleContainer(8);

    public AbstractVillager(EntityType<? extends AbstractVillager> p_35267_, Level p_35268_) {
        super(p_35267_, p_35268_);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_35282_, DifficultyInstance p_35283_, EntitySpawnReason p_365249_, @Nullable SpawnGroupData p_35285_) {
        if (p_35285_ == null) {
            p_35285_ = new AgeableMob.AgeableMobGroupData(false);
        }

        return super.finalizeSpawn(p_35282_, p_35283_, p_365249_, p_35285_);
    }

    public int getUnhappyCounter() {
        return this.entityData.get(DATA_UNHAPPY_COUNTER);
    }

    public void setUnhappyCounter(int p_35320_) {
        this.entityData.set(DATA_UNHAPPY_COUNTER, p_35320_);
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_332892_) {
        super.defineSynchedData(p_332892_);
        p_332892_.define(DATA_UNHAPPY_COUNTER, 0);
    }

    @Override
    public void setTradingPlayer(@Nullable Player p_35314_) {
        this.tradingPlayer = p_35314_;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    @Override
    public MerchantOffers getOffers() {
        if (this.level().isClientSide) {
            throw new IllegalStateException("Cannot load Villager offers on the client");
        } else {
            if (this.offers == null) {
                this.offers = new MerchantOffers();
                this.updateTrades();
            }

            return this.offers;
        }
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers p_35276_) {
    }

    @Override
    public void overrideXp(int p_35322_) {
    }

    @Override
    public void notifyTrade(MerchantOffer p_35274_) {
        p_35274_.increaseUses();
        this.ambientSoundTime = -this.getAmbientSoundInterval();
        this.rewardTradeXp(p_35274_);
        if (this.tradingPlayer instanceof ServerPlayer) {
            CriteriaTriggers.TRADE.trigger((ServerPlayer)this.tradingPlayer, this, p_35274_.getResult());
        }
    }

    protected abstract void rewardTradeXp(MerchantOffer p_35299_);

    @Override
    public boolean showProgressBar() {
        return true;
    }

    @Override
    public void notifyTradeUpdated(ItemStack p_35316_) {
        if (!this.level().isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
            this.ambientSoundTime = -this.getAmbientSoundInterval();
            this.makeSound(this.getTradeUpdatedSound(!p_35316_.isEmpty()));
        }
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    protected SoundEvent getTradeUpdatedSound(boolean p_35323_) {
        return p_35323_ ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    public void playCelebrateSound() {
        this.makeSound(SoundEvents.VILLAGER_CELEBRATE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_35301_) {
        super.addAdditionalSaveData(p_35301_);
        if (!this.level().isClientSide) {
            MerchantOffers merchantoffers = this.getOffers();
            if (!merchantoffers.isEmpty()) {
                p_35301_.put("Offers", MerchantOffers.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), merchantoffers).getOrThrow());
            }
        }

        this.writeInventoryToTag(p_35301_, this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_35290_) {
        super.readAdditionalSaveData(p_35290_);
        if (p_35290_.contains("Offers")) {
            MerchantOffers.CODEC
                .parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), p_35290_.get("Offers"))
                .resultOrPartial(Util.prefix("Failed to load offers: ", LOGGER::warn))
                .ifPresent(p_328744_ -> this.offers = p_328744_);
        }

        this.readInventoryFromTag(p_35290_, this.registryAccess());
    }

    @Nullable
    @Override
    public Entity teleport(TeleportTransition p_368720_) {
        this.stopTrading();
        return super.teleport(p_368720_);
    }

    protected void stopTrading() {
        this.setTradingPlayer(null);
    }

    @Override
    public void die(DamageSource p_35270_) {
        super.die(p_35270_);
        this.stopTrading();
    }

    protected void addParticlesAroundSelf(ParticleOptions p_35288_) {
        for (int i = 0; i < 5; i++) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(p_35288_, this.getRandomX(1.0), this.getRandomY() + 1.0, this.getRandomZ(1.0), d0, d1, d2);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    public SlotAccess getSlot(int p_149995_) {
        int i = p_149995_ - 300;
        return i >= 0 && i < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, i) : super.getSlot(p_149995_);
    }

    protected abstract void updateTrades();

    protected void addOffersFromItemListings(MerchantOffers p_35278_, VillagerTrades.ItemListing[] p_35279_, int p_35280_) {
        ArrayList<VillagerTrades.ItemListing> arraylist = Lists.newArrayList(p_35279_);
        int i = 0;

        while (i < p_35280_ && !arraylist.isEmpty()) {
            MerchantOffer merchantoffer = arraylist.remove(this.random.nextInt(arraylist.size())).getOffer(this, this.random);
            if (merchantoffer != null) {
                p_35278_.add(merchantoffer);
                i++;
            }
        }
    }

    @Override
    public Vec3 getRopeHoldPosition(float p_35318_) {
        float f = Mth.lerp(p_35318_, this.yBodyRotO, this.yBodyRot) * (float) (Math.PI / 180.0);
        Vec3 vec3 = new Vec3(0.0, this.getBoundingBox().getYsize() - 1.0, 0.2);
        return this.getPosition(p_35318_).add(vec3.yRot(-f));
    }

    @Override
    public boolean isClientSide() {
        return this.level().isClientSide;
    }

    @Override
    public boolean stillValid(Player p_377036_) {
        return this.getTradingPlayer() == p_377036_ && this.isAlive() && p_377036_.canInteractWithEntity(this, 4.0);
    }
}