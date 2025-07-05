package net.minecraft.world.entity.animal;

import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class Salmon extends AbstractSchoolingFish implements VariantHolder<Salmon.Variant> {
    private static final String TAG_TYPE = "type";
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(Salmon.class, EntityDataSerializers.INT);

    public Salmon(EntityType<? extends Salmon> p_29790_, Level p_29791_) {
        super(p_29790_, p_29791_);
        this.refreshDimensions();
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override
    public ItemStack getBucketItemStack() {
        return new ItemStack(Items.SALMON_BUCKET);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_29795_) {
        return SoundEvents.SALMON_HURT;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.SALMON_FLOP;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_368809_) {
        super.defineSynchedData(p_368809_);
        p_368809_.define(DATA_TYPE, Salmon.Variant.MEDIUM.id());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_366689_) {
        super.onSyncedDataUpdated(p_366689_);
        if (DATA_TYPE.equals(p_366689_)) {
            this.refreshDimensions();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_361498_) {
        super.addAdditionalSaveData(p_361498_);
        p_361498_.putString("type", this.getVariant().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_362202_) {
        super.readAdditionalSaveData(p_362202_);
        this.setVariant(Salmon.Variant.byName(p_362202_.getString("type")));
    }

    @Override
    public void saveToBucketTag(ItemStack p_362100_) {
        Bucketable.saveDefaultDataToBucketTag(this, p_362100_);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, p_362100_, p_368336_ -> p_368336_.putString("type", this.getVariant().getSerializedName()));
    }

    @Override
    public void loadFromBucketTag(CompoundTag p_363572_) {
        Bucketable.loadDefaultDataFromBucketTag(this, p_363572_);
        this.setVariant(Salmon.Variant.byName(p_363572_.getString("type")));
    }

    public void setVariant(Salmon.Variant p_361475_) {
        this.entityData.set(DATA_TYPE, p_361475_.id);
    }

    public Salmon.Variant getVariant() {
        return Salmon.Variant.BY_ID.apply(this.entityData.get(DATA_TYPE));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_363387_, DifficultyInstance p_367892_, EntitySpawnReason p_368788_, @Nullable SpawnGroupData p_362214_) {
        SimpleWeightedRandomList.Builder<Salmon.Variant> builder = SimpleWeightedRandomList.builder();
        builder.add(Salmon.Variant.SMALL, 30);
        builder.add(Salmon.Variant.MEDIUM, 50);
        builder.add(Salmon.Variant.LARGE, 15);
        builder.build().getRandomValue(this.random).ifPresent(this::setVariant);
        return super.finalizeSpawn(p_363387_, p_367892_, p_368788_, p_362214_);
    }

    public float getSalmonScale() {
        return this.getVariant().boundingBoxScale;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose p_368896_) {
        return super.getDefaultDimensions(p_368896_).scale(this.getSalmonScale());
    }

    public static enum Variant implements StringRepresentable {
        SMALL("small", 0, 0.5F),
        MEDIUM("medium", 1, 1.0F),
        LARGE("large", 2, 1.5F);

        public static final StringRepresentable.EnumCodec<Salmon.Variant> CODEC = StringRepresentable.fromEnum(Salmon.Variant::values);
        static final IntFunction<Salmon.Variant> BY_ID = ByIdMap.continuous(Salmon.Variant::id, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        private final String name;
        final int id;
        final float boundingBoxScale;

        private Variant(final String p_364669_, final int p_375656_, final float p_368051_) {
            this.name = p_364669_;
            this.id = p_375656_;
            this.boundingBoxScale = p_368051_;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        int id() {
            return this.id;
        }

        static Salmon.Variant byName(String p_368928_) {
            return CODEC.byName(p_368928_, MEDIUM);
        }
    }
}