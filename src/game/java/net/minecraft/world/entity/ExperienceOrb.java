package net.minecraft.world.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ExperienceOrb extends Entity {
    private static final int LIFETIME = 6000;
    private static final int ENTITY_SCAN_PERIOD = 20;
    private static final int MAX_FOLLOW_DIST = 8;
    private static final int ORB_GROUPS_PER_AREA = 40;
    private static final double ORB_MERGE_DISTANCE = 0.5;
    private int age;
    private int health = 5;
    private int value;
    private int count = 1;
    private Player followingPlayer;

    public ExperienceOrb(Level p_20776_, double p_20777_, double p_20778_, double p_20779_, int p_20780_) {
        this(EntityType.EXPERIENCE_ORB, p_20776_);
        this.setPos(p_20777_, p_20778_, p_20779_);
        this.setYRot((float)(this.random.nextDouble() * 360.0));
        this.setDeltaMovement((this.random.nextDouble() * 0.2F - 0.1F) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2F - 0.1F) * 2.0);
        this.value = p_20780_;
    }

    public ExperienceOrb(EntityType<? extends ExperienceOrb> p_20773_, Level p_20774_) {
        super(p_20773_, p_20774_);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_329424_) {
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
        } else {
            this.applyGravity();
        }

        if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement(
                (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F),
                0.2F,
                (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.2F)
            );
        }

        if (!this.level().noCollision(this.getBoundingBox())) {
            this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
        }

        if (this.tickCount % 20 == 1) {
            this.scanForEntities();
        }

        if (this.followingPlayer != null && (this.followingPlayer.isSpectator() || this.followingPlayer.isDeadOrDying())) {
            this.followingPlayer = null;
        }

        if (this.followingPlayer != null) {
            Vec3 vec3 = new Vec3(
                this.followingPlayer.getX() - this.getX(),
                this.followingPlayer.getY() + (double)this.followingPlayer.getEyeHeight() / 2.0 - this.getY(),
                this.followingPlayer.getZ() - this.getZ()
            );
            double d0 = vec3.lengthSqr();
            if (d0 < 64.0) {
                double d1 = 1.0 - Math.sqrt(d0) / 8.0;
                this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.1)));
            }
        }

        double d2 = this.getDeltaMovement().y;
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        float f = 0.98F;
        if (this.onGround()) {
            f = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.98, (double)f));
        if (this.onGround()) {
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x, -d2 * 0.4, this.getDeltaMovement().z));
        }

        this.age++;
        if (this.age >= 6000) {
            this.discard();
        }
    }

    @Override
    public BlockPos getBlockPosBelowThatAffectsMyMovement() {
        return this.getOnPos(0.999999F);
    }

    private void scanForEntities() {
        if (this.followingPlayer == null || this.followingPlayer.distanceToSqr(this) > 64.0) {
            this.followingPlayer = this.level().getNearestPlayer(this, 8.0);
        }

        if (this.level() instanceof ServerLevel) {
            for (ExperienceOrb experienceorb : this.level()
                .getEntities(EntityTypeTest.forClass(ExperienceOrb.class), this.getBoundingBox().inflate(0.5), this::canMerge)) {
                this.merge(experienceorb);
            }
        }
    }

    public static void award(ServerLevel p_147083_, Vec3 p_147084_, int p_147085_) {
        while (p_147085_ > 0) {
            int i = getExperienceValue(p_147085_);
            p_147085_ -= i;
            if (!tryMergeToExisting(p_147083_, p_147084_, i)) {
                p_147083_.addFreshEntity(new ExperienceOrb(p_147083_, p_147084_.x(), p_147084_.y(), p_147084_.z(), i));
            }
        }
    }

    private static boolean tryMergeToExisting(ServerLevel p_147097_, Vec3 p_147098_, int p_147099_) {
        AABB aabb = AABB.ofSize(p_147098_, 1.0, 1.0, 1.0);
        int i = p_147097_.getRandom().nextInt(40);
        List<ExperienceOrb> list = p_147097_.getEntities(EntityTypeTest.forClass(ExperienceOrb.class), aabb, p_147081_ -> canMerge(p_147081_, i, p_147099_));
        if (!list.isEmpty()) {
            ExperienceOrb experienceorb = list.get(0);
            experienceorb.count++;
            experienceorb.age = 0;
            return true;
        } else {
            return false;
        }
    }

    private boolean canMerge(ExperienceOrb p_147087_) {
        return p_147087_ != this && canMerge(p_147087_, this.getId(), this.value);
    }

    private static boolean canMerge(ExperienceOrb p_147089_, int p_147090_, int p_147091_) {
        return !p_147089_.isRemoved() && (p_147089_.getId() - p_147090_) % 40 == 0 && p_147089_.value == p_147091_;
    }

    private void merge(ExperienceOrb p_147101_) {
        this.count = this.count + p_147101_.count;
        this.age = Math.min(this.age, p_147101_.age);
        p_147101_.discard();
    }

    private void setUnderwaterMovement() {
        Vec3 vec3 = this.getDeltaMovement();
        this.setDeltaMovement(vec3.x * 0.99F, Math.min(vec3.y + 5.0E-4F, 0.06F), vec3.z * 0.99F);
    }

    @Override
    protected void doWaterSplashEffect() {
    }

    @Override
    public final boolean hurtClient(DamageSource p_369585_) {
        return !this.isInvulnerableToBase(p_369585_);
    }

    @Override
    public final boolean hurtServer(ServerLevel p_365476_, DamageSource p_362340_, float p_369855_) {
        if (this.isInvulnerableToBase(p_362340_)) {
            return false;
        } else {
            this.markHurt();
            this.health = (int)((float)this.health - p_369855_);
            if (this.health <= 0) {
                this.discard();
            }

            return true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_20796_) {
        p_20796_.putShort("Health", (short)this.health);
        p_20796_.putShort("Age", (short)this.age);
        p_20796_.putShort("Value", (short)this.value);
        p_20796_.putInt("Count", this.count);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_20788_) {
        this.health = p_20788_.getShort("Health");
        this.age = p_20788_.getShort("Age");
        this.value = p_20788_.getShort("Value");
        this.count = Math.max(p_20788_.getInt("Count"), 1);
    }

    @Override
    public void playerTouch(Player p_20792_) {
        if (p_20792_ instanceof ServerPlayer serverplayer) {
            if (p_20792_.takeXpDelay == 0) {
                p_20792_.takeXpDelay = 2;
                p_20792_.take(this, 1);
                int i = this.repairPlayerItems(serverplayer, this.value);
                if (i > 0) {
                    p_20792_.giveExperiencePoints(i);
                }

                this.count--;
                if (this.count == 0) {
                    this.discard();
                }
            }
        }
    }

    private int repairPlayerItems(ServerPlayer p_343572_, int p_147094_) {
        Optional<EnchantedItemInUse> optional = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, p_343572_, ItemStack::isDamaged);
        if (optional.isPresent()) {
            ItemStack itemstack = optional.get().itemStack();
            int i = EnchantmentHelper.modifyDurabilityToRepairFromXp(p_343572_.serverLevel(), itemstack, p_147094_);
            int j = Math.min(i, itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - j);
            if (j > 0) {
                int k = p_147094_ - j * p_147094_ / i;
                if (k > 0) {
                    return this.repairPlayerItems(p_343572_, k);
                }
            }

            return 0;
        } else {
            return p_147094_;
        }
    }

    public int getValue() {
        return this.value;
    }

    public int getIcon() {
        if (this.value >= 2477) {
            return 10;
        } else if (this.value >= 1237) {
            return 9;
        } else if (this.value >= 617) {
            return 8;
        } else if (this.value >= 307) {
            return 7;
        } else if (this.value >= 149) {
            return 6;
        } else if (this.value >= 73) {
            return 5;
        } else if (this.value >= 37) {
            return 4;
        } else if (this.value >= 17) {
            return 3;
        } else if (this.value >= 7) {
            return 2;
        } else {
            return this.value >= 3 ? 1 : 0;
        }
    }

    public static int getExperienceValue(int p_20783_) {
        if (p_20783_ >= 2477) {
            return 2477;
        } else if (p_20783_ >= 1237) {
            return 1237;
        } else if (p_20783_ >= 617) {
            return 617;
        } else if (p_20783_ >= 307) {
            return 307;
        } else if (p_20783_ >= 149) {
            return 149;
        } else if (p_20783_ >= 73) {
            return 73;
        } else if (p_20783_ >= 37) {
            return 37;
        } else if (p_20783_ >= 17) {
            return 17;
        } else if (p_20783_ >= 7) {
            return 7;
        } else {
            return p_20783_ >= 3 ? 3 : 1;
        }
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity p_342735_) {
        return new ClientboundAddExperienceOrbPacket(this, p_342735_);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }
}