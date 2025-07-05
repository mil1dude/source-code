package net.minecraft.world.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;

public interface Leashable {
    String LEASH_TAG = "leash";
    double LEASH_TOO_FAR_DIST = 10.0;
    double LEASH_ELASTIC_DIST = 6.0;

    @Nullable
    Leashable.LeashData getLeashData();

    void setLeashData(@Nullable Leashable.LeashData p_345228_);

    default boolean isLeashed() {
        return this.getLeashData() != null && this.getLeashData().leashHolder != null;
    }

    default boolean mayBeLeashed() {
        return this.getLeashData() != null;
    }

    default boolean canHaveALeashAttachedToIt() {
        return this.canBeLeashed() && !this.isLeashed();
    }

    default boolean canBeLeashed() {
        return true;
    }

    default void setDelayedLeashHolderId(int p_345000_) {
        this.setLeashData(new Leashable.LeashData(p_345000_));
        dropLeash((Entity & Leashable)this, false, false);
    }

    default void readLeashData(CompoundTag p_376404_) {
        Leashable.LeashData leashable$leashdata = readLeashDataInternal(p_376404_);
        if (this.getLeashData() != null && leashable$leashdata == null) {
            this.removeLeash();
        }

        this.setLeashData(leashable$leashdata);
    }

    @Nullable
    private static Leashable.LeashData readLeashDataInternal(CompoundTag p_344001_) {
        if (p_344001_.contains("leash", 10)) {
            return new Leashable.LeashData(Either.left(p_344001_.getCompound("leash").getUUID("UUID")));
        } else {
            if (p_344001_.contains("leash", 11)) {
                Either<UUID, BlockPos> either = NbtUtils.readBlockPos(p_344001_, "leash").map(Either::<UUID, BlockPos>right).orElse(null);
                if (either != null) {
                    return new Leashable.LeashData(either);
                }
            }

            return null;
        }
    }

    default void writeLeashData(CompoundTag p_344282_, @Nullable Leashable.LeashData p_345503_) {
        if (p_345503_ != null) {
            Either<UUID, BlockPos> either = p_345503_.delayedLeashInfo;
            if (p_345503_.leashHolder instanceof LeashFenceKnotEntity leashfenceknotentity) {
                either = Either.right(leashfenceknotentity.getPos());
            } else if (p_345503_.leashHolder != null) {
                either = Either.left(p_345503_.leashHolder.getUUID());
            }

            if (either != null) {
                p_344282_.put("leash", either.map(p_345095_ -> {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putUUID("UUID", p_345095_);
                    return compoundtag;
                }, NbtUtils::writeBlockPos));
            }
        }
    }

    private static <E extends Entity & Leashable> void restoreLeashFromSave(E p_343564_, Leashable.LeashData p_344259_) {
        if (p_344259_.delayedLeashInfo != null && p_343564_.level() instanceof ServerLevel serverlevel) {
            Optional<UUID> optional1 = p_344259_.delayedLeashInfo.left();
            Optional<BlockPos> optional = p_344259_.delayedLeashInfo.right();
            if (optional1.isPresent()) {
                Entity entity = serverlevel.getEntity(optional1.get());
                if (entity != null) {
                    setLeashedTo(p_343564_, entity, true);
                    return;
                }
            } else if (optional.isPresent()) {
                setLeashedTo(p_343564_, LeashFenceKnotEntity.getOrCreateKnot(serverlevel, optional.get()), true);
                return;
            }

            if (p_343564_.tickCount > 100) {
                p_343564_.spawnAtLocation(serverlevel, Items.LEAD);
                p_343564_.setLeashData(null);
            }
        }
    }

    default void dropLeash() {
        dropLeash((Entity & Leashable)this, true, true);
    }

    default void removeLeash() {
        dropLeash((Entity & Leashable)this, true, false);
    }

    default void onLeashRemoved() {
    }

    private static <E extends Entity & Leashable> void dropLeash(E p_343459_, boolean p_342580_, boolean p_344786_) {
        Leashable.LeashData leashable$leashdata = p_343459_.getLeashData();
        if (leashable$leashdata != null && leashable$leashdata.leashHolder != null) {
            p_343459_.setLeashData(null);
            p_343459_.onLeashRemoved();
            if (p_343459_.level() instanceof ServerLevel serverlevel) {
                if (p_344786_) {
                    p_343459_.spawnAtLocation(serverlevel, Items.LEAD);
                }

                if (p_342580_) {
                    serverlevel.getChunkSource().broadcast(p_343459_, new ClientboundSetEntityLinkPacket(p_343459_, null));
                }
            }
        }
    }

    static <E extends Entity & Leashable> void tickLeash(ServerLevel p_366578_, E p_343570_) {
        Leashable.LeashData leashable$leashdata = p_343570_.getLeashData();
        if (leashable$leashdata != null && leashable$leashdata.delayedLeashInfo != null) {
            restoreLeashFromSave(p_343570_, leashable$leashdata);
        }

        if (leashable$leashdata != null && leashable$leashdata.leashHolder != null) {
            if (!p_343570_.isAlive() || !leashable$leashdata.leashHolder.isAlive()) {
                if (p_366578_.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    p_343570_.dropLeash();
                } else {
                    p_343570_.removeLeash();
                }
            }

            Entity entity = p_343570_.getLeashHolder();
            if (entity != null && entity.level() == p_343570_.level()) {
                float f = p_343570_.distanceTo(entity);
                if (!p_343570_.handleLeashAtDistance(entity, f)) {
                    return;
                }

                if ((double)f > 10.0) {
                    p_343570_.leashTooFarBehaviour();
                } else if ((double)f > 6.0) {
                    p_343570_.elasticRangeLeashBehaviour(entity, f);
                    p_343570_.checkSlowFallDistance();
                } else {
                    p_343570_.closeRangeLeashBehaviour(entity);
                }
            }
        }
    }

    default boolean handleLeashAtDistance(Entity p_345181_, float p_342079_) {
        return true;
    }

    default void leashTooFarBehaviour() {
        this.dropLeash();
    }

    default void closeRangeLeashBehaviour(Entity p_344596_) {
    }

    default void elasticRangeLeashBehaviour(Entity p_342226_, float p_342283_) {
        legacyElasticRangeLeashBehaviour((Entity & Leashable)this, p_342226_, p_342283_);
    }

    private static <E extends Entity & Leashable> void legacyElasticRangeLeashBehaviour(E p_342325_, Entity p_343749_, float p_343654_) {
        double d0 = (p_343749_.getX() - p_342325_.getX()) / (double)p_343654_;
        double d1 = (p_343749_.getY() - p_342325_.getY()) / (double)p_343654_;
        double d2 = (p_343749_.getZ() - p_342325_.getZ()) / (double)p_343654_;
        p_342325_.setDeltaMovement(p_342325_.getDeltaMovement().add(Math.copySign(d0 * d0 * 0.4, d0), Math.copySign(d1 * d1 * 0.4, d1), Math.copySign(d2 * d2 * 0.4, d2)));
    }

    default void setLeashedTo(Entity p_342408_, boolean p_342255_) {
        setLeashedTo((Entity & Leashable)this, p_342408_, p_342255_);
    }

    private static <E extends Entity & Leashable> void setLeashedTo(E p_342775_, Entity p_342643_, boolean p_343557_) {
        Leashable.LeashData leashable$leashdata = p_342775_.getLeashData();
        if (leashable$leashdata == null) {
            leashable$leashdata = new Leashable.LeashData(p_342643_);
            p_342775_.setLeashData(leashable$leashdata);
        } else {
            leashable$leashdata.setLeashHolder(p_342643_);
        }

        if (p_343557_ && p_342775_.level() instanceof ServerLevel serverlevel) {
            serverlevel.getChunkSource().broadcast(p_342775_, new ClientboundSetEntityLinkPacket(p_342775_, p_342643_));
        }

        if (p_342775_.isPassenger()) {
            p_342775_.stopRiding();
        }
    }

    @Nullable
    default Entity getLeashHolder() {
        return getLeashHolder((Entity & Leashable)this);
    }

    @Nullable
    private static <E extends Entity & Leashable> Entity getLeashHolder(E p_342282_) {
        Leashable.LeashData leashable$leashdata = p_342282_.getLeashData();
        if (leashable$leashdata == null) {
            return null;
        } else {
            if (leashable$leashdata.delayedLeashHolderId != 0 && p_342282_.level().isClientSide) {
                Entity entity = p_342282_.level().getEntity(leashable$leashdata.delayedLeashHolderId);
                if (entity instanceof Entity) {
                    leashable$leashdata.setLeashHolder(entity);
                }
            }

            return leashable$leashdata.leashHolder;
        }
    }

    public static final class LeashData {
        int delayedLeashHolderId;
        @Nullable
        public Entity leashHolder;
        @Nullable
        public Either<UUID, BlockPos> delayedLeashInfo;

        LeashData(Either<UUID, BlockPos> p_345305_) {
            this.delayedLeashInfo = p_345305_;
        }

        LeashData(Entity p_345447_) {
            this.leashHolder = p_345447_;
        }

        LeashData(int p_345400_) {
            this.delayedLeashHolderId = p_345400_;
        }

        public void setLeashHolder(Entity p_342311_) {
            this.leashHolder = p_342311_;
            this.delayedLeashInfo = null;
            this.delayedLeashHolderId = 0;
        }
    }
}
