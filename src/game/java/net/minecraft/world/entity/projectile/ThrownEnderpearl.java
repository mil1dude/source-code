package net.minecraft.world.entity.projectile;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEnderpearl extends ThrowableItemProjectile {
    private long ticketTimer = 0L;

    public ThrownEnderpearl(EntityType<? extends ThrownEnderpearl> p_37491_, Level p_37492_) {
        super(p_37491_, p_37492_);
    }

    public ThrownEnderpearl(Level p_37499_, LivingEntity p_37500_, ItemStack p_365479_) {
        super(EntityType.ENDER_PEARL, p_37500_, p_37499_, p_365479_);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void setOwnerThroughUUID(UUID p_364520_) {
        this.deregisterFromCurrentOwner();
        super.setOwnerThroughUUID(p_364520_);
        this.registerToCurrentOwner();
    }

    @Override
    public void setOwner(@Nullable Entity p_365453_) {
        this.deregisterFromCurrentOwner();
        super.setOwner(p_365453_);
        this.registerToCurrentOwner();
    }

    private void deregisterFromCurrentOwner() {
        if (this.getOwner() instanceof ServerPlayer serverplayer) {
            serverplayer.deregisterEnderPearl(this);
        }
    }

    private void registerToCurrentOwner() {
        if (this.getOwner() instanceof ServerPlayer serverplayer) {
            serverplayer.registerEnderPearl(this);
        }
    }

    @Nullable
    @Override
    protected Entity findOwner(UUID p_369345_) {
        if (this.level() instanceof ServerLevel serverlevel) {
            Entity entity = super.findOwner(p_369345_);
            if (entity != null) {
                return entity;
            } else {
                for (ServerLevel serverlevel1 : serverlevel.getServer().getAllLevels()) {
                    if (serverlevel1 != serverlevel) {
                        entity = serverlevel1.getEntity(p_369345_);
                        if (entity != null) {
                            return entity;
                        }
                    }
                }

                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult p_37502_) {
        super.onHitEntity(p_37502_);
        p_37502_.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    @Override
    protected void onHit(HitResult p_37504_) {
        super.onHit(p_37504_);

        for (int i = 0; i < 32; i++) {
            this.level()
                .addParticle(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY() + this.random.nextDouble() * 2.0,
                    this.getZ(),
                    this.random.nextGaussian(),
                    0.0,
                    this.random.nextGaussian()
                );
        }

        if (this.level() instanceof ServerLevel serverlevel && !this.isRemoved()) {
            Entity entity = this.getOwner();
            if (entity != null && isAllowedToTeleportOwner(entity, serverlevel)) {
                if (entity.isPassenger()) {
                    entity.unRide();
                }

                Vec3 vec3 = this.oldPosition();
                if (entity instanceof ServerPlayer serverplayer) {
                    if (serverplayer.connection.isAcceptingMessages()) {
                        if (this.random.nextFloat() < 0.05F && serverlevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                            Endermite endermite = EntityType.ENDERMITE.create(serverlevel, EntitySpawnReason.TRIGGERED);
                            if (endermite != null) {
                                endermite.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                                serverlevel.addFreshEntity(endermite);
                            }
                        }

                        if (this.isOnPortalCooldown()) {
                            entity.setPortalCooldown();
                        }

                        ServerPlayer serverplayer1 = serverplayer.teleport(
                            new TeleportTransition(
                                serverlevel,
                                vec3,
                                Vec3.ZERO,
                                0.0F,
                                0.0F,
                                Relative.union(Relative.ROTATION, Relative.DELTA),
                                TeleportTransition.DO_NOTHING
                            )
                        );
                        if (serverplayer1 != null) {
                            serverplayer1.resetFallDistance();
                            serverplayer1.resetCurrentImpulseContext();
                            serverplayer1.hurtServer(serverplayer.serverLevel(), this.damageSources().enderPearl(), 5.0F);
                        }

                        this.playSound(serverlevel, vec3);
                    }
                } else {
                    Entity entity1 = entity.teleport(
                        new TeleportTransition(serverlevel, vec3, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), TeleportTransition.DO_NOTHING)
                    );
                    if (entity1 != null) {
                        entity1.resetFallDistance();
                    }

                    this.playSound(serverlevel, vec3);
                }

                this.discard();
                return;
            }

            this.discard();
            return;
        }
    }

    private static boolean isAllowedToTeleportOwner(Entity p_343823_, Level p_342445_) {
        if (p_343823_.level().dimension() == p_342445_.dimension()) {
            return !(p_343823_ instanceof LivingEntity livingentity) ? p_343823_.isAlive() : livingentity.isAlive() && !livingentity.isSleeping();
        } else {
            return p_343823_.canUsePortal(true);
        }
    }

    @Override
    public void tick() {
        int i;
        int j;
        Entity entity;
        label30: {
            i = SectionPos.blockToSectionCoord(this.position().x());
            j = SectionPos.blockToSectionCoord(this.position().z());
            entity = this.getOwner();
            if (entity instanceof ServerPlayer serverplayer && !entity.isAlive() && serverplayer.serverLevel().getGameRules().getBoolean(GameRules.RULE_ENDER_PEARLS_VANISH_ON_DEATH)) {
                this.discard();
                break label30;
            }

            super.tick();
        }

        if (this.isAlive()) {
            BlockPos blockpos = BlockPos.containing(this.position());
            if ((--this.ticketTimer <= 0L || i != SectionPos.blockToSectionCoord(blockpos.getX()) || j != SectionPos.blockToSectionCoord(blockpos.getZ()))
                && entity instanceof ServerPlayer serverplayer1) {
                this.ticketTimer = serverplayer1.registerAndUpdateEnderPearlTicket(this);
            }
        }
    }

    private void playSound(Level p_344184_, Vec3 p_345358_) {
        p_344184_.playSound(null, p_345358_.x, p_345358_.y, p_345358_.z, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
    }

    @Nullable
    @Override
    public Entity teleport(TeleportTransition p_361132_) {
        Entity entity = super.teleport(p_361132_);
        if (entity != null) {
            entity.placePortalTicket(BlockPos.containing(entity.position()));
        }

        return entity;
    }

    @Override
    public boolean canTeleport(Level p_366889_, Level p_366581_) {
        return p_366889_.dimension() == Level.END && p_366581_.dimension() == Level.OVERWORLD && this.getOwner() instanceof ServerPlayer serverplayer
            ? super.canTeleport(p_366889_, p_366581_) && serverplayer.seenCredits
            : super.canTeleport(p_366889_, p_366581_);
    }

    @Override
    protected void onInsideBlock(BlockState p_345184_) {
        super.onInsideBlock(p_345184_);
        if (p_345184_.is(Blocks.END_GATEWAY) && this.getOwner() instanceof ServerPlayer serverplayer) {
            serverplayer.onInsideBlock(p_345184_);
        }
    }

    @Override
    public void onRemoval(Entity.RemovalReason p_366133_) {
        if (p_366133_ != Entity.RemovalReason.UNLOADED_WITH_PLAYER) {
            this.deregisterFromCurrentOwner();
        }

        super.onRemoval(p_366133_);
    }
}