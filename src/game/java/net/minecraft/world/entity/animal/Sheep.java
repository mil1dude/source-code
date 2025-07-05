package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class Sheep extends Animal implements Shearable {
    private static final int EAT_ANIMATION_TICKS = 40;
    private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
    private static final Map<DyeColor, Integer> COLOR_BY_DYE = Maps.<DyeColor, Integer>newEnumMap(
        Arrays.stream(DyeColor.values()).collect(Collectors.toMap(p_29868_ -> (DyeColor)p_29868_, Sheep::createSheepColor))
    );
    private int eatAnimationTick;
    private EatBlockGoal eatBlockGoal;

    private static int createSheepColor(DyeColor p_29866_) {
        if (p_29866_ == DyeColor.WHITE) {
            return -1644826;
        } else {
            int i = p_29866_.getTextureDiffuseColor();
            float f = 0.75F;
            return ARGB.color(
                255,
                Mth.floor((float)ARGB.red(i) * 0.75F),
                Mth.floor((float)ARGB.green(i) * 0.75F),
                Mth.floor((float)ARGB.blue(i) * 0.75F)
            );
        }
    }

    public static int getColor(DyeColor p_342171_) {
        return COLOR_BY_DYE.get(p_342171_);
    }

    public Sheep(EntityType<? extends Sheep> p_29806_, Level p_29807_) {
        super(p_29806_, p_29807_);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, p_326983_ -> p_326983_.is(ItemTags.SHEEP_FOOD), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, this.eatBlockGoal);
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack p_328882_) {
        return p_328882_.is(ItemTags.SHEEP_FOOD);
    }

    @Override
    protected void customServerAiStep(ServerLevel p_369809_) {
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep(p_369809_);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
        }

        super.aiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.23F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_335407_) {
        super.defineSynchedData(p_335407_);
        p_335407_.define(DATA_WOOL_ID, (byte)0);
    }

    @Override
    public void handleEntityEvent(byte p_29814_) {
        if (p_29814_ == 10) {
            this.eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(p_29814_);
        }
    }

    public float getHeadEatPositionScale(float p_29881_) {
        if (this.eatAnimationTick <= 0) {
            return 0.0F;
        } else if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
            return 1.0F;
        } else {
            return this.eatAnimationTick < 4 ? ((float)this.eatAnimationTick - p_29881_) / 4.0F : -((float)(this.eatAnimationTick - 40) - p_29881_) / 4.0F;
        }
    }

    public float getHeadEatAngleScale(float p_29883_) {
        if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
            float f = ((float)(this.eatAnimationTick - 4) - p_29883_) / 32.0F;
            return (float) (Math.PI / 5) + 0.21991149F * Mth.sin(f * 28.7F);
        } else {
            return this.eatAnimationTick > 0 ? (float) (Math.PI / 5) : this.getXRot() * (float) (Math.PI / 180.0);
        }
    }

    @Override
    public InteractionResult mobInteract(Player p_29853_, InteractionHand p_29854_) {
        ItemStack itemstack = p_29853_.getItemInHand(p_29854_);
        if (itemstack.is(Items.SHEARS)) {
            if (this.level() instanceof ServerLevel serverlevel && this.readyForShearing()) {
                this.shear(serverlevel, SoundSource.PLAYERS, itemstack);
                this.gameEvent(GameEvent.SHEAR, p_29853_);
                itemstack.hurtAndBreak(1, p_29853_, getSlotForHand(p_29854_));
                return InteractionResult.SUCCESS_SERVER;
            }

            return InteractionResult.CONSUME;
        } else {
            return super.mobInteract(p_29853_, p_29854_);
        }
    }

    @Override
    public void shear(ServerLevel p_365174_, SoundSource p_29819_, ItemStack p_361058_) {
        p_365174_.playSound(null, this, SoundEvents.SHEEP_SHEAR, p_29819_, 1.0F, 1.0F);
        this.dropFromShearingLootTable(
            p_365174_,
            BuiltInLootTables.SHEAR_SHEEP,
            p_361058_,
            (p_359182_, p_359183_) -> {
                for (int i = 0; i < p_359183_.getCount(); i++) {
                    ItemEntity itementity = this.spawnAtLocation(p_359182_, p_359183_.copyWithCount(1), 1.0F);
                    if (itementity != null) {
                        itementity.setDeltaMovement(
                            itementity.getDeltaMovement()
                                .add(
                                    (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                                    (double)(this.random.nextFloat() * 0.05F),
                                    (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)
                                )
                        );
                    }
                }
            }
        );
        this.setSheared(true);
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_29864_) {
        super.addAdditionalSaveData(p_29864_);
        p_29864_.putBoolean("Sheared", this.isSheared());
        p_29864_.putByte("Color", (byte)this.getColor().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_29845_) {
        super.readAdditionalSaveData(p_29845_);
        this.setSheared(p_29845_.getBoolean("Sheared"));
        this.setColor(DyeColor.byId(p_29845_.getByte("Color")));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_29872_) {
        return SoundEvents.SHEEP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos p_29861_, BlockState p_29862_) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_WOOL_ID) & 15);
    }

    public void setColor(DyeColor p_29856_) {
        byte b0 = this.entityData.get(DATA_WOOL_ID);
        this.entityData.set(DATA_WOOL_ID, (byte)(b0 & 240 | p_29856_.getId() & 15));
    }

    public boolean isSheared() {
        return (this.entityData.get(DATA_WOOL_ID) & 16) != 0;
    }

    public void setSheared(boolean p_29879_) {
        byte b0 = this.entityData.get(DATA_WOOL_ID);
        if (p_29879_) {
            this.entityData.set(DATA_WOOL_ID, (byte)(b0 | 16));
        } else {
            this.entityData.set(DATA_WOOL_ID, (byte)(b0 & -17));
        }
    }

    public static DyeColor getRandomSheepColor(RandomSource p_218262_) {
        int i = p_218262_.nextInt(100);
        if (i < 5) {
            return DyeColor.BLACK;
        } else if (i < 10) {
            return DyeColor.GRAY;
        } else if (i < 15) {
            return DyeColor.LIGHT_GRAY;
        } else if (i < 18) {
            return DyeColor.BROWN;
        } else {
            return p_218262_.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
        }
    }

    @Nullable
    public Sheep getBreedOffspring(ServerLevel p_149044_, AgeableMob p_149045_) {
        Sheep sheep = EntityType.SHEEP.create(p_149044_, EntitySpawnReason.BREEDING);
        if (sheep != null) {
            DyeColor dyecolor = this.getColor();
            DyeColor dyecolor1 = ((Sheep)p_149045_).getColor();
            sheep.setColor(DyeColor.getMixedColor(p_149044_, dyecolor, dyecolor1));
        }

        return sheep;
    }

    @Override
    public void ate() {
        super.ate();
        this.setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_29835_, DifficultyInstance p_29836_, EntitySpawnReason p_364266_, @Nullable SpawnGroupData p_29838_) {
        this.setColor(getRandomSheepColor(p_29835_.getRandom()));
        return super.finalizeSpawn(p_29835_, p_29836_, p_364266_, p_29838_);
    }
}
