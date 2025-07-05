package net.minecraft.world.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;

public class AreaEffectCloud extends Entity implements TraceableEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TIME_BETWEEN_APPLICATIONS = 5;
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(AreaEffectCloud.class, EntityDataSerializers.PARTICLE);
    private static final float MAX_RADIUS = 32.0F;
    private static final float MINIMAL_RADIUS = 0.5F;
    private static final float DEFAULT_RADIUS = 3.0F;
    public static final float DEFAULT_WIDTH = 6.0F;
    public static final float HEIGHT = 0.5F;
    private PotionContents potionContents = PotionContents.EMPTY;
    private final Map<Entity, Integer> victims = Maps.newHashMap();
    private int duration = 600;
    private int waitTime = 20;
    private int reapplicationDelay = 20;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public AreaEffectCloud(EntityType<? extends AreaEffectCloud> p_19704_, Level p_19705_) {
        super(p_19704_, p_19705_);
        this.noPhysics = true;
    }

    public AreaEffectCloud(Level p_19707_, double p_19708_, double p_19709_, double p_19710_) {
        this(EntityType.AREA_EFFECT_CLOUD, p_19707_);
        this.setPos(p_19708_, p_19709_, p_19710_);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_330412_) {
        p_330412_.define(DATA_RADIUS, 3.0F);
        p_330412_.define(DATA_WAITING, false);
        p_330412_.define(DATA_PARTICLE, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1));
    }

    public void setRadius(float p_19713_) {
        if (!this.level().isClientSide) {
            this.getEntityData().set(DATA_RADIUS, Mth.clamp(p_19713_, 0.0F, 32.0F));
        }
    }

    @Override
    public void refreshDimensions() {
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    public float getRadius() {
        return this.getEntityData().get(DATA_RADIUS);
    }

    public void setPotionContents(PotionContents p_332440_) {
        this.potionContents = p_332440_;
        this.updateColor();
    }

    private void updateColor() {
        ParticleOptions particleoptions = this.entityData.get(DATA_PARTICLE);
        if (particleoptions instanceof ColorParticleOption colorparticleoption) {
            int i = this.potionContents.equals(PotionContents.EMPTY) ? 0 : this.potionContents.getColor();
            this.entityData.set(DATA_PARTICLE, ColorParticleOption.create(colorparticleoption.getType(), ARGB.opaque(i)));
        }
    }

    public void addEffect(MobEffectInstance p_19717_) {
        this.setPotionContents(this.potionContents.withEffectAdded(p_19717_));
    }

    public ParticleOptions getParticle() {
        return this.getEntityData().get(DATA_PARTICLE);
    }

    public void setParticle(ParticleOptions p_19725_) {
        this.getEntityData().set(DATA_PARTICLE, p_19725_);
    }

    protected void setWaiting(boolean p_19731_) {
        this.getEntityData().set(DATA_WAITING, p_19731_);
    }

    public boolean isWaiting() {
        return this.getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int p_19735_) {
        this.duration = p_19735_;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverlevel) {
            this.serverTick(serverlevel);
        } else {
            this.clientTick();
        }
    }

    private void clientTick() {
        boolean flag = this.isWaiting();
        float f = this.getRadius();
        if (!flag || !this.random.nextBoolean()) {
            ParticleOptions particleoptions = this.getParticle();
            int i;
            float f1;
            if (flag) {
                i = 2;
                f1 = 0.2F;
            } else {
                i = Mth.ceil((float) Math.PI * f * f);
                f1 = f;
            }

            for (int j = 0; j < i; j++) {
                float f2 = this.random.nextFloat() * (float) (Math.PI * 2);
                float f3 = Mth.sqrt(this.random.nextFloat()) * f1;
                double d0 = this.getX() + (double)(Mth.cos(f2) * f3);
                double d1 = this.getY();
                double d2 = this.getZ() + (double)(Mth.sin(f2) * f3);
                if (particleoptions.getType() == ParticleTypes.ENTITY_EFFECT) {
                    if (flag && this.random.nextBoolean()) {
                        this.level().addAlwaysVisibleParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1), d0, d1, d2, 0.0, 0.0, 0.0);
                    } else {
                        this.level().addAlwaysVisibleParticle(particleoptions, d0, d1, d2, 0.0, 0.0, 0.0);
                    }
                } else if (flag) {
                    this.level().addAlwaysVisibleParticle(particleoptions, d0, d1, d2, 0.0, 0.0, 0.0);
                } else {
                    this.level()
                        .addAlwaysVisibleParticle(particleoptions, d0, d1, d2, (0.5 - this.random.nextDouble()) * 0.15, 0.01F, (0.5 - this.random.nextDouble()) * 0.15);
                }
            }
        }
    }

    private void serverTick(ServerLevel p_361199_) {
        if (this.tickCount >= this.waitTime + this.duration) {
            this.discard();
        } else {
            boolean flag = this.isWaiting();
            boolean flag1 = this.tickCount < this.waitTime;
            if (flag != flag1) {
                this.setWaiting(flag1);
            }

            if (!flag1) {
                float f = this.getRadius();
                if (this.radiusPerTick != 0.0F) {
                    f += this.radiusPerTick;
                    if (f < 0.5F) {
                        this.discard();
                        return;
                    }

                    this.setRadius(f);
                }

                if (this.tickCount % 5 == 0) {
                    this.victims.entrySet().removeIf(p_287380_ -> this.tickCount >= p_287380_.getValue());
                    if (!this.potionContents.hasEffects()) {
                        this.victims.clear();
                    } else {
                        List<MobEffectInstance> list = Lists.newArrayList();
                        if (this.potionContents.potion().isPresent()) {
                            for (MobEffectInstance mobeffectinstance : this.potionContents.potion().get().value().getEffects()) {
                                list.add(
                                    new MobEffectInstance(
                                        mobeffectinstance.getEffect(),
                                        mobeffectinstance.mapDuration(p_267926_ -> p_267926_ / 4),
                                        mobeffectinstance.getAmplifier(),
                                        mobeffectinstance.isAmbient(),
                                        mobeffectinstance.isVisible()
                                    )
                                );
                            }
                        }

                        list.addAll(this.potionContents.customEffects());
                        List<LivingEntity> list1 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
                        if (!list1.isEmpty()) {
                            for (LivingEntity livingentity : list1) {
                                if (!this.victims.containsKey(livingentity) && livingentity.isAffectedByPotions() && !list.stream().noneMatch(livingentity::canBeAffected)) {
                                    double d0 = livingentity.getX() - this.getX();
                                    double d1 = livingentity.getZ() - this.getZ();
                                    double d2 = d0 * d0 + d1 * d1;
                                    if (d2 <= (double)(f * f)) {
                                        this.victims.put(livingentity, this.tickCount + this.reapplicationDelay);

                                        for (MobEffectInstance mobeffectinstance1 : list) {
                                            if (mobeffectinstance1.getEffect().value().isInstantenous()) {
                                                mobeffectinstance1.getEffect()
                                                    .value()
                                                    .applyInstantenousEffect(p_361199_, this, this.getOwner(), livingentity, mobeffectinstance1.getAmplifier(), 0.5);
                                            } else {
                                                livingentity.addEffect(new MobEffectInstance(mobeffectinstance1), this);
                                            }
                                        }

                                        if (this.radiusOnUse != 0.0F) {
                                            f += this.radiusOnUse;
                                            if (f < 0.5F) {
                                                this.discard();
                                                return;
                                            }

                                            this.setRadius(f);
                                        }

                                        if (this.durationOnUse != 0) {
                                            this.duration = this.duration + this.durationOnUse;
                                            if (this.duration <= 0) {
                                                this.discard();
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public float getRadiusOnUse() {
        return this.radiusOnUse;
    }

    public void setRadiusOnUse(float p_19733_) {
        this.radiusOnUse = p_19733_;
    }

    public float getRadiusPerTick() {
        return this.radiusPerTick;
    }

    public void setRadiusPerTick(float p_19739_) {
        this.radiusPerTick = p_19739_;
    }

    public int getDurationOnUse() {
        return this.durationOnUse;
    }

    public void setDurationOnUse(int p_146786_) {
        this.durationOnUse = p_146786_;
    }

    public int getWaitTime() {
        return this.waitTime;
    }

    public void setWaitTime(int p_19741_) {
        this.waitTime = p_19741_;
    }

    public void setOwner(@Nullable LivingEntity p_19719_) {
        this.owner = p_19719_;
        this.ownerUUID = p_19719_ == null ? null : p_19719_.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner != null && !this.owner.isRemoved()) {
            return this.owner;
        } else {
            if (this.ownerUUID != null && this.level() instanceof ServerLevel serverlevel) {
                this.owner = serverlevel.getEntity(this.ownerUUID) instanceof LivingEntity livingentity ? livingentity : null;
            }

            return this.owner;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_19727_) {
        this.tickCount = p_19727_.getInt("Age");
        this.duration = p_19727_.getInt("Duration");
        this.waitTime = p_19727_.getInt("WaitTime");
        this.reapplicationDelay = p_19727_.getInt("ReapplicationDelay");
        this.durationOnUse = p_19727_.getInt("DurationOnUse");
        this.radiusOnUse = p_19727_.getFloat("RadiusOnUse");
        this.radiusPerTick = p_19727_.getFloat("RadiusPerTick");
        this.setRadius(p_19727_.getFloat("Radius"));
        if (p_19727_.hasUUID("Owner")) {
            this.ownerUUID = p_19727_.getUUID("Owner");
        }

        RegistryOps<Tag> registryops = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        if (p_19727_.contains("Particle", 10)) {
            ParticleTypes.CODEC
                .parse(registryops, p_19727_.get("Particle"))
                .resultOrPartial(p_326760_ -> LOGGER.warn("Failed to parse area effect cloud particle options: '{}'", p_326760_))
                .ifPresent(this::setParticle);
        }

        if (p_19727_.contains("potion_contents")) {
            PotionContents.CODEC
                .parse(registryops, p_19727_.get("potion_contents"))
                .resultOrPartial(p_326761_ -> LOGGER.warn("Failed to parse area effect cloud potions: '{}'", p_326761_))
                .ifPresent(this::setPotionContents);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_19737_) {
        p_19737_.putInt("Age", this.tickCount);
        p_19737_.putInt("Duration", this.duration);
        p_19737_.putInt("WaitTime", this.waitTime);
        p_19737_.putInt("ReapplicationDelay", this.reapplicationDelay);
        p_19737_.putInt("DurationOnUse", this.durationOnUse);
        p_19737_.putFloat("RadiusOnUse", this.radiusOnUse);
        p_19737_.putFloat("RadiusPerTick", this.radiusPerTick);
        p_19737_.putFloat("Radius", this.getRadius());
        RegistryOps<Tag> registryops = this.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        p_19737_.put("Particle", ParticleTypes.CODEC.encodeStart(registryops, this.getParticle()).getOrThrow());
        if (this.ownerUUID != null) {
            p_19737_.putUUID("Owner", this.ownerUUID);
        }

        if (!this.potionContents.equals(PotionContents.EMPTY)) {
            Tag tag = PotionContents.CODEC.encodeStart(registryops, this.potionContents).getOrThrow();
            p_19737_.put("potion_contents", tag);
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_19729_) {
        if (DATA_RADIUS.equals(p_19729_)) {
            this.refreshDimensions();
        }

        super.onSyncedDataUpdated(p_19729_);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public EntityDimensions getDimensions(Pose p_19721_) {
        return EntityDimensions.scalable(this.getRadius() * 2.0F, 0.5F);
    }

    @Override
    public final boolean hurtServer(ServerLevel p_360854_, DamageSource p_364045_, float p_363449_) {
        return false;
    }
}