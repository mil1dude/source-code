package net.minecraft.world.entity.npc;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.UseItemGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class WanderingTrader extends AbstractVillager implements Consumable.OverrideConsumeSound {
    private static final int NUMBER_OF_TRADE_OFFERS = 5;
    @Nullable
    private BlockPos wanderTarget;
    private int despawnDelay;

    public WanderingTrader(EntityType<? extends WanderingTrader> p_35843_, Level p_35844_) {
        super(p_35843_, p_35844_);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector
            .addGoal(
                0,
                new UseItemGoal<>(
                    this,
                    PotionContents.createItemStack(Items.POTION, Potions.INVISIBILITY),
                    SoundEvents.WANDERING_TRADER_DISAPPEARED,
                    p_375164_ -> this.level().isNight() && !p_375164_.isInvisible()
                )
            );
        this.goalSelector
            .addGoal(
                0, new UseItemGoal<>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, p_375165_ -> this.level().isDay() && p_375165_.isInvisible())
            );
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderingTrader.WanderToPositionGoal(this, 2.0, 0.35));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_150046_, AgeableMob p_150047_) {
        return null;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player p_35856_, InteractionHand p_35857_) {
        ItemStack itemstack = p_35856_.getItemInHand(p_35857_);
        if (!itemstack.is(Items.VILLAGER_SPAWN_EGG) && this.isAlive() && !this.isTrading() && !this.isBaby()) {
            if (p_35857_ == InteractionHand.MAIN_HAND) {
                p_35856_.awardStat(Stats.TALKED_TO_VILLAGER);
            }

            if (!this.level().isClientSide) {
                if (this.getOffers().isEmpty()) {
                    return InteractionResult.CONSUME;
                }

                this.setTradingPlayer(p_35856_);
                this.openTradingScreen(p_35856_, this.getDisplayName(), 1);
            }

            return InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(p_35856_, p_35857_);
        }
    }

    @Override
    protected void updateTrades() {
        if (this.level().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE)) {
            this.experimentalUpdateTrades();
        } else {
            VillagerTrades.ItemListing[] avillagertrades$itemlisting = VillagerTrades.WANDERING_TRADER_TRADES.get(1);
            VillagerTrades.ItemListing[] avillagertrades$itemlisting1 = VillagerTrades.WANDERING_TRADER_TRADES.get(2);
            if (avillagertrades$itemlisting != null && avillagertrades$itemlisting1 != null) {
                MerchantOffers merchantoffers = this.getOffers();
                this.addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting, 5);
                int i = this.random.nextInt(avillagertrades$itemlisting1.length);
                VillagerTrades.ItemListing villagertrades$itemlisting = avillagertrades$itemlisting1[i];
                MerchantOffer merchantoffer = villagertrades$itemlisting.getOffer(this, this.random);
                if (merchantoffer != null) {
                    merchantoffers.add(merchantoffer);
                }
            }
        }
    }

    private void experimentalUpdateTrades() {
        MerchantOffers merchantoffers = this.getOffers();

        for (Pair<VillagerTrades.ItemListing[], Integer> pair : VillagerTrades.EXPERIMENTAL_WANDERING_TRADER_TRADES) {
            VillagerTrades.ItemListing[] avillagertrades$itemlisting = pair.getLeft();
            this.addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting, pair.getRight());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_35861_) {
        super.addAdditionalSaveData(p_35861_);
        p_35861_.putInt("DespawnDelay", this.despawnDelay);
        if (this.wanderTarget != null) {
            p_35861_.put("wander_target", NbtUtils.writeBlockPos(this.wanderTarget));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_35852_) {
        super.readAdditionalSaveData(p_35852_);
        if (p_35852_.contains("DespawnDelay", 99)) {
            this.despawnDelay = p_35852_.getInt("DespawnDelay");
        }

        NbtUtils.readBlockPos(p_35852_, "wander_target").ifPresent(p_327049_ -> this.wanderTarget = p_327049_);
        this.setAge(Math.max(0, this.getAge()));
    }

    @Override
    public boolean removeWhenFarAway(double p_35886_) {
        return false;
    }

    @Override
    protected void rewardTradeXp(MerchantOffer p_35859_) {
        if (p_35859_.shouldRewardExp()) {
            int i = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), i));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_35870_) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    public SoundEvent getConsumeSound(ItemStack p_35865_) {
        return p_35865_.is(Items.MILK_BUCKET) ? SoundEvents.WANDERING_TRADER_DRINK_MILK : SoundEvents.WANDERING_TRADER_DRINK_POTION;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean p_35890_) {
        return p_35890_ ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public void setDespawnDelay(int p_35892_) {
        this.despawnDelay = p_35892_;
    }

    public int getDespawnDelay() {
        return this.despawnDelay;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
            this.discard();
        }
    }

    public void setWanderTarget(@Nullable BlockPos p_35884_) {
        this.wanderTarget = p_35884_;
    }

    @Nullable
    BlockPos getWanderTarget() {
        return this.wanderTarget;
    }

    class WanderToPositionGoal extends Goal {
        final WanderingTrader trader;
        final double stopDistance;
        final double speedModifier;

        WanderToPositionGoal(final WanderingTrader p_35899_, final double p_35900_, final double p_35901_) {
            this.trader = p_35899_;
            this.stopDistance = p_35900_;
            this.speedModifier = p_35901_;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void stop() {
            this.trader.setWanderTarget(null);
            WanderingTrader.this.navigation.stop();
        }

        @Override
        public boolean canUse() {
            BlockPos blockpos = this.trader.getWanderTarget();
            return blockpos != null && this.isTooFarAway(blockpos, this.stopDistance);
        }

        @Override
        public void tick() {
            BlockPos blockpos = this.trader.getWanderTarget();
            if (blockpos != null && WanderingTrader.this.navigation.isDone()) {
                if (this.isTooFarAway(blockpos, 10.0)) {
                    Vec3 vec3 = new Vec3(
                            (double)blockpos.getX() - this.trader.getX(),
                            (double)blockpos.getY() - this.trader.getY(),
                            (double)blockpos.getZ() - this.trader.getZ()
                        )
                        .normalize();
                    Vec3 vec31 = vec3.scale(10.0).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
                    WanderingTrader.this.navigation.moveTo(vec31.x, vec31.y, vec31.z, this.speedModifier);
                } else {
                    WanderingTrader.this.navigation
                        .moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), this.speedModifier);
                }
            }
        }

        private boolean isTooFarAway(BlockPos p_35904_, double p_35905_) {
            return !p_35904_.closerToCenterThan(this.trader.position(), p_35905_);
        }
    }
}