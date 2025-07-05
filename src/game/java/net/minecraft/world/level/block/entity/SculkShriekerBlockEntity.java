package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.SpawnUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class SculkShriekerBlockEntity extends BlockEntity implements GameEventListener.Provider<VibrationSystem.Listener>, VibrationSystem {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int WARNING_SOUND_RADIUS = 10;
    private static final int WARDEN_SPAWN_ATTEMPTS = 20;
    private static final int WARDEN_SPAWN_RANGE_XZ = 5;
    private static final int WARDEN_SPAWN_RANGE_Y = 6;
    private static final int DARKNESS_RADIUS = 40;
    private static final int SHRIEKING_TICKS = 90;
    private static final Int2ObjectMap<SoundEvent> SOUND_BY_LEVEL = Util.make(new Int2ObjectOpenHashMap<>(), p_222866_ -> {
        p_222866_.put(1, SoundEvents.WARDEN_NEARBY_CLOSE);
        p_222866_.put(2, SoundEvents.WARDEN_NEARBY_CLOSER);
        p_222866_.put(3, SoundEvents.WARDEN_NEARBY_CLOSEST);
        p_222866_.put(4, SoundEvents.WARDEN_LISTENING_ANGRY);
    });
    private int warningLevel;
    private final VibrationSystem.User vibrationUser = new SculkShriekerBlockEntity.VibrationUser();
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

    public SculkShriekerBlockEntity(BlockPos p_222835_, BlockState p_222836_) {
        super(BlockEntityType.SCULK_SHRIEKER, p_222835_, p_222836_);
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    protected void loadAdditional(CompoundTag p_327746_, HolderLookup.Provider p_335650_) {
        super.loadAdditional(p_327746_, p_335650_);
        if (p_327746_.contains("warning_level", 99)) {
            this.warningLevel = p_327746_.getInt("warning_level");
        }

        RegistryOps<Tag> registryops = p_335650_.createSerializationContext(NbtOps.INSTANCE);
        if (p_327746_.contains("listener", 10)) {
            VibrationSystem.Data.CODEC
                .parse(registryops, p_327746_.getCompound("listener"))
                .resultOrPartial(p_341844_ -> LOGGER.error("Failed to parse vibration listener for Sculk Shrieker: '{}'", p_341844_))
                .ifPresent(p_281147_ -> this.vibrationData = p_281147_);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag p_222878_, HolderLookup.Provider p_330845_) {
        super.saveAdditional(p_222878_, p_330845_);
        p_222878_.putInt("warning_level", this.warningLevel);
        RegistryOps<Tag> registryops = p_330845_.createSerializationContext(NbtOps.INSTANCE);
        VibrationSystem.Data.CODEC
            .encodeStart(registryops, this.vibrationData)
            .resultOrPartial(p_341843_ -> LOGGER.error("Failed to encode vibration listener for Sculk Shrieker: '{}'", p_341843_))
            .ifPresent(p_222871_ -> p_222878_.put("listener", p_222871_));
    }

    @Nullable
    public static ServerPlayer tryGetPlayer(@Nullable Entity p_222862_) {
        if (p_222862_ instanceof ServerPlayer) {
            return (ServerPlayer)p_222862_;
        } else {
            if (p_222862_ != null) {
                LivingEntity $$6 = p_222862_.getControllingPassenger();
                if ($$6 instanceof ServerPlayer) {
                    return (ServerPlayer)$$6;
                }
            }

            if (p_222862_ instanceof Projectile projectile) {
                Entity entity = projectile.getOwner();
                if (entity instanceof ServerPlayer) {
                    return (ServerPlayer)entity;
                }
            }

            if (p_222862_ instanceof ItemEntity itementity) {
                Entity entity1 = itementity.getOwner();
                if (entity1 instanceof ServerPlayer) {
                    return (ServerPlayer)entity1;
                }
            }

            return null;
        }
    }

    public void tryShriek(ServerLevel p_222842_, @Nullable ServerPlayer p_222843_) {
        if (p_222843_ != null) {
            BlockState blockstate = this.getBlockState();
            if (!blockstate.getValue(SculkShriekerBlock.SHRIEKING)) {
                this.warningLevel = 0;
                if (!this.canRespond(p_222842_) || this.tryToWarn(p_222842_, p_222843_)) {
                    this.shriek(p_222842_, p_222843_);
                }
            }
        }
    }

    private boolean tryToWarn(ServerLevel p_222875_, ServerPlayer p_222876_) {
        OptionalInt optionalint = WardenSpawnTracker.tryWarn(p_222875_, this.getBlockPos(), p_222876_);
        optionalint.ifPresent(p_222838_ -> this.warningLevel = p_222838_);
        return optionalint.isPresent();
    }

    private void shriek(ServerLevel p_222845_, @Nullable Entity p_222846_) {
        BlockPos blockpos = this.getBlockPos();
        BlockState blockstate = this.getBlockState();
        p_222845_.setBlock(blockpos, blockstate.setValue(SculkShriekerBlock.SHRIEKING, Boolean.valueOf(true)), 2);
        p_222845_.scheduleTick(blockpos, blockstate.getBlock(), 90);
        p_222845_.levelEvent(3007, blockpos, 0);
        p_222845_.gameEvent(GameEvent.SHRIEK, blockpos, GameEvent.Context.of(p_222846_));
    }

    private boolean canRespond(ServerLevel p_222873_) {
        return this.getBlockState().getValue(SculkShriekerBlock.CAN_SUMMON)
            && p_222873_.getDifficulty() != Difficulty.PEACEFUL
            && p_222873_.getGameRules().getBoolean(GameRules.RULE_DO_WARDEN_SPAWNING);
    }

    public void tryRespond(ServerLevel p_222840_) {
        if (this.canRespond(p_222840_) && this.warningLevel > 0) {
            if (!this.trySummonWarden(p_222840_)) {
                this.playWardenReplySound(p_222840_);
            }

            Warden.applyDarknessAround(p_222840_, Vec3.atCenterOf(this.getBlockPos()), null, 40);
        }
    }

    private void playWardenReplySound(Level p_281300_) {
        SoundEvent soundevent = SOUND_BY_LEVEL.get(this.warningLevel);
        if (soundevent != null) {
            BlockPos blockpos = this.getBlockPos();
            int i = blockpos.getX() + Mth.randomBetweenInclusive(p_281300_.random, -10, 10);
            int j = blockpos.getY() + Mth.randomBetweenInclusive(p_281300_.random, -10, 10);
            int k = blockpos.getZ() + Mth.randomBetweenInclusive(p_281300_.random, -10, 10);
            p_281300_.playSound(null, (double)i, (double)j, (double)k, soundevent, SoundSource.HOSTILE, 5.0F, 1.0F);
        }
    }

    private boolean trySummonWarden(ServerLevel p_222881_) {
        return this.warningLevel < 4
            ? false
            : SpawnUtil.trySpawnMob(EntityType.WARDEN, EntitySpawnReason.TRIGGERED, p_222881_, this.getBlockPos(), 20, 5, 6, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER, false)
                .isPresent();
    }

    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    class VibrationUser implements VibrationSystem.User {
        private static final int LISTENER_RADIUS = 8;
        private final PositionSource positionSource = new BlockPositionSource(SculkShriekerBlockEntity.this.worldPosition);

        public VibrationUser() {
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.SHRIEKER_CAN_LISTEN;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel p_281256_, BlockPos p_281528_, Holder<GameEvent> p_335342_, GameEvent.Context p_282914_) {
            return !SculkShriekerBlockEntity.this.getBlockState().getValue(SculkShriekerBlock.SHRIEKING)
                && SculkShriekerBlockEntity.tryGetPlayer(p_282914_.sourceEntity()) != null;
        }

        @Override
        public void onReceiveVibration(
            ServerLevel p_283372_, BlockPos p_281679_, Holder<GameEvent> p_330622_, @Nullable Entity p_282286_, @Nullable Entity p_281384_, float p_283119_
        ) {
            SculkShriekerBlockEntity.this.tryShriek(p_283372_, SculkShriekerBlockEntity.tryGetPlayer(p_281384_ != null ? p_281384_ : p_282286_));
        }

        @Override
        public void onDataChanged() {
            SculkShriekerBlockEntity.this.setChanged();
        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}