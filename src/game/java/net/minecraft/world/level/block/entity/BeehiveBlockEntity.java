package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;

public class BeehiveBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_FLOWER_POS = "flower_pos";
    private static final String BEES = "bees";
    static final List<String> IGNORED_BEE_TAGS = Arrays.asList(
        "Air",
        "ArmorDropChances",
        "ArmorItems",
        "Brain",
        "CanPickUpLoot",
        "DeathTime",
        "FallDistance",
        "FallFlying",
        "Fire",
        "HandDropChances",
        "HandItems",
        "HurtByTimestamp",
        "HurtTime",
        "LeftHanded",
        "Motion",
        "NoGravity",
        "OnGround",
        "PortalCooldown",
        "Pos",
        "Rotation",
        "SleepingX",
        "SleepingY",
        "SleepingZ",
        "CannotEnterHiveTicks",
        "TicksSincePollination",
        "CropsGrownSincePollination",
        "hive_pos",
        "Passengers",
        "leash",
        "UUID"
    );
    public static final int MAX_OCCUPANTS = 3;
    private static final int MIN_TICKS_BEFORE_REENTERING_HIVE = 400;
    private static final int MIN_OCCUPATION_TICKS_NECTAR = 2400;
    public static final int MIN_OCCUPATION_TICKS_NECTARLESS = 600;
    private final List<BeehiveBlockEntity.BeeData> stored = Lists.newArrayList();
    @Nullable
    private BlockPos savedFlowerPos;

    public BeehiveBlockEntity(BlockPos p_155134_, BlockState p_155135_) {
        super(BlockEntityType.BEEHIVE, p_155134_, p_155135_);
    }

    @Override
    public void setChanged() {
        if (this.isFireNearby()) {
            this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY);
        }

        super.setChanged();
    }

    public boolean isFireNearby() {
        if (this.level == null) {
            return false;
        } else {
            for (BlockPos blockpos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
                if (this.level.getBlockState(blockpos).getBlock() instanceof FireBlock) {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable Player p_58749_, BlockState p_58750_, BeehiveBlockEntity.BeeReleaseStatus p_58751_) {
        List<Entity> list = this.releaseAllOccupants(p_58750_, p_58751_);
        if (p_58749_ != null) {
            for (Entity entity : list) {
                if (entity instanceof Bee) {
                    Bee bee = (Bee)entity;
                    if (p_58749_.position().distanceToSqr(entity.position()) <= 16.0) {
                        if (!this.isSedated()) {
                            bee.setTarget(p_58749_);
                        } else {
                            bee.setStayOutOfHiveCountdown(400);
                        }
                    }
                }
            }
        }
    }

    private List<Entity> releaseAllOccupants(BlockState p_58760_, BeehiveBlockEntity.BeeReleaseStatus p_58761_) {
        List<Entity> list = Lists.newArrayList();
        this.stored.removeIf(p_327282_ -> releaseOccupant(this.level, this.worldPosition, p_58760_, p_327282_.toOccupant(), list, p_58761_, this.savedFlowerPos));
        if (!list.isEmpty()) {
            super.setChanged();
        }

        return list;
    }

    @VisibleForDebug
    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getHoneyLevel(BlockState p_58753_) {
        return p_58753_.getValue(BeehiveBlock.HONEY_LEVEL);
    }

    @VisibleForDebug
    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }

    public void addOccupant(Bee p_377433_) {
        if (this.stored.size() < 3) {
            p_377433_.stopRiding();
            p_377433_.ejectPassengers();
            p_377433_.dropLeash();
            this.storeBee(BeehiveBlockEntity.Occupant.of(p_377433_));
            if (this.level != null) {
                if (p_377433_.hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean())) {
                    this.savedFlowerPos = p_377433_.getSavedFlowerPos();
                }

                BlockPos blockpos = this.getBlockPos();
                this.level
                    .playSound(
                        null,
                        (double)blockpos.getX(),
                        (double)blockpos.getY(),
                        (double)blockpos.getZ(),
                        SoundEvents.BEEHIVE_ENTER,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                    );
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(p_377433_, this.getBlockState()));
            }

            p_377433_.discard();
            super.setChanged();
        }
    }

    public void storeBee(BeehiveBlockEntity.Occupant p_329282_) {
        this.stored.add(new BeehiveBlockEntity.BeeData(p_329282_));
    }

    private static boolean releaseOccupant(
        Level p_155137_,
        BlockPos p_155138_,
        BlockState p_155139_,
        BeehiveBlockEntity.Occupant p_335681_,
        @Nullable List<Entity> p_155141_,
        BeehiveBlockEntity.BeeReleaseStatus p_155142_,
        @Nullable BlockPos p_155143_
    ) {
        if (Bee.isNightOrRaining(p_155137_) && p_155142_ != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
            return false;
        } else {
            Direction direction = p_155139_.getValue(BeehiveBlock.FACING);
            BlockPos blockpos = p_155138_.relative(direction);
            boolean flag = !p_155137_.getBlockState(blockpos).getCollisionShape(p_155137_, blockpos).isEmpty();
            if (flag && p_155142_ != BeehiveBlockEntity.BeeReleaseStatus.EMERGENCY) {
                return false;
            } else {
                Entity entity = p_335681_.createEntity(p_155137_, p_155138_);
                if (entity != null) {
                    if (entity instanceof Bee bee) {
                        if (p_155143_ != null && !bee.hasSavedFlowerPos() && p_155137_.random.nextFloat() < 0.9F) {
                            bee.setSavedFlowerPos(p_155143_);
                        }

                        if (p_155142_ == BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED) {
                            bee.dropOffNectar();
                            if (p_155139_.is(BlockTags.BEEHIVES, p_202037_ -> p_202037_.hasProperty(BeehiveBlock.HONEY_LEVEL))) {
                                int i = getHoneyLevel(p_155139_);
                                if (i < 5) {
                                    int j = p_155137_.random.nextInt(100) == 0 ? 2 : 1;
                                    if (i + j > 5) {
                                        j--;
                                    }

                                    p_155137_.setBlockAndUpdate(p_155138_, p_155139_.setValue(BeehiveBlock.HONEY_LEVEL, Integer.valueOf(i + j)));
                                }
                            }
                        }

                        if (p_155141_ != null) {
                            p_155141_.add(bee);
                        }

                        float f = entity.getBbWidth();
                        double d3 = flag ? 0.0 : 0.55 + (double)(f / 2.0F);
                        double d0 = (double)p_155138_.getX() + 0.5 + d3 * (double)direction.getStepX();
                        double d1 = (double)p_155138_.getY() + 0.5 - (double)(entity.getBbHeight() / 2.0F);
                        double d2 = (double)p_155138_.getZ() + 0.5 + d3 * (double)direction.getStepZ();
                        entity.moveTo(d0, d1, d2, entity.getYRot(), entity.getXRot());
                    }

                    p_155137_.playSound(null, p_155138_, SoundEvents.BEEHIVE_EXIT, SoundSource.BLOCKS, 1.0F, 1.0F);
                    p_155137_.gameEvent(GameEvent.BLOCK_CHANGE, p_155138_, GameEvent.Context.of(entity, p_155137_.getBlockState(p_155138_)));
                    return p_155137_.addFreshEntity(entity);
                } else {
                    return false;
                }
            }
        }
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    private static void tickOccupants(
        Level p_155150_, BlockPos p_155151_, BlockState p_155152_, List<BeehiveBlockEntity.BeeData> p_155153_, @Nullable BlockPos p_155154_
    ) {
        boolean flag = false;
        Iterator<BeehiveBlockEntity.BeeData> iterator = p_155153_.iterator();

        while (iterator.hasNext()) {
            BeehiveBlockEntity.BeeData beehiveblockentity$beedata = iterator.next();
            if (beehiveblockentity$beedata.tick()) {
                BeehiveBlockEntity.BeeReleaseStatus beehiveblockentity$beereleasestatus = beehiveblockentity$beedata.hasNectar()
                    ? BeehiveBlockEntity.BeeReleaseStatus.HONEY_DELIVERED
                    : BeehiveBlockEntity.BeeReleaseStatus.BEE_RELEASED;
                if (releaseOccupant(p_155150_, p_155151_, p_155152_, beehiveblockentity$beedata.toOccupant(), null, beehiveblockentity$beereleasestatus, p_155154_)) {
                    flag = true;
                    iterator.remove();
                }
            }
        }

        if (flag) {
            setChanged(p_155150_, p_155151_, p_155152_);
        }
    }

    public static void serverTick(Level p_155145_, BlockPos p_155146_, BlockState p_155147_, BeehiveBlockEntity p_155148_) {
        tickOccupants(p_155145_, p_155146_, p_155147_, p_155148_.stored, p_155148_.savedFlowerPos);
        if (!p_155148_.stored.isEmpty() && p_155145_.getRandom().nextDouble() < 0.005) {
            double d0 = (double)p_155146_.getX() + 0.5;
            double d1 = (double)p_155146_.getY();
            double d2 = (double)p_155146_.getZ() + 0.5;
            p_155145_.playSound(null, d0, d1, d2, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        DebugPackets.sendHiveInfo(p_155145_, p_155146_, p_155147_, p_155148_);
    }

    @Override
    protected void loadAdditional(CompoundTag p_333420_, HolderLookup.Provider p_335311_) {
        super.loadAdditional(p_333420_, p_335311_);
        this.stored.clear();
        if (p_333420_.contains("bees")) {
            BeehiveBlockEntity.Occupant.LIST_CODEC
                .parse(NbtOps.INSTANCE, p_333420_.get("bees"))
                .resultOrPartial(p_327283_ -> LOGGER.error("Failed to parse bees: '{}'", p_327283_))
                .ifPresent(p_327284_ -> p_327284_.forEach(this::storeBee));
        }

        this.savedFlowerPos = NbtUtils.readBlockPos(p_333420_, "flower_pos").orElse(null);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187467_, HolderLookup.Provider p_332762_) {
        super.saveAdditional(p_187467_, p_332762_);
        p_187467_.put("bees", BeehiveBlockEntity.Occupant.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.getBees()).getOrThrow());
        if (this.hasSavedFlowerPos()) {
            p_187467_.put("flower_pos", NbtUtils.writeBlockPos(this.savedFlowerPos));
        }
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput p_333166_) {
        super.applyImplicitComponents(p_333166_);
        this.stored.clear();
        List<BeehiveBlockEntity.Occupant> list = p_333166_.getOrDefault(DataComponents.BEES, List.of());
        list.forEach(this::storeBee);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder p_328977_) {
        super.collectImplicitComponents(p_328977_);
        p_328977_.set(DataComponents.BEES, this.getBees());
    }

    @Override
    public void removeComponentsFromTag(CompoundTag p_329874_) {
        super.removeComponentsFromTag(p_329874_);
        p_329874_.remove("bees");
    }

    private List<BeehiveBlockEntity.Occupant> getBees() {
        return this.stored.stream().map(BeehiveBlockEntity.BeeData::toOccupant).toList();
    }

    static class BeeData {
        private final BeehiveBlockEntity.Occupant occupant;
        private int ticksInHive;

        BeeData(BeehiveBlockEntity.Occupant p_336059_) {
            this.occupant = p_336059_;
            this.ticksInHive = p_336059_.ticksInHive();
        }

        public boolean tick() {
            return this.ticksInHive++ > this.occupant.minTicksInHive;
        }

        public BeehiveBlockEntity.Occupant toOccupant() {
            return new BeehiveBlockEntity.Occupant(this.occupant.entityData, this.ticksInHive, this.occupant.minTicksInHive);
        }

        public boolean hasNectar() {
            return this.occupant.entityData.getUnsafe().getBoolean("HasNectar");
        }
    }

    public static enum BeeReleaseStatus {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY;
    }

    public static record Occupant(CustomData entityData, int ticksInHive, int minTicksInHive) {
        public static final Codec<BeehiveBlockEntity.Occupant> CODEC = RecordCodecBuilder.create(
            p_330401_ -> p_330401_.group(
                        CustomData.CODEC.optionalFieldOf("entity_data", CustomData.EMPTY).forGetter(BeehiveBlockEntity.Occupant::entityData),
                        Codec.INT.fieldOf("ticks_in_hive").forGetter(BeehiveBlockEntity.Occupant::ticksInHive),
                        Codec.INT.fieldOf("min_ticks_in_hive").forGetter(BeehiveBlockEntity.Occupant::minTicksInHive)
                    )
                    .apply(p_330401_, BeehiveBlockEntity.Occupant::new)
        );
        public static final Codec<List<BeehiveBlockEntity.Occupant>> LIST_CODEC = CODEC.listOf();
        public static final StreamCodec<ByteBuf, BeehiveBlockEntity.Occupant> STREAM_CODEC = StreamCodec.composite(
            CustomData.STREAM_CODEC,
            BeehiveBlockEntity.Occupant::entityData,
            ByteBufCodecs.VAR_INT,
            BeehiveBlockEntity.Occupant::ticksInHive,
            ByteBufCodecs.VAR_INT,
            BeehiveBlockEntity.Occupant::minTicksInHive,
            BeehiveBlockEntity.Occupant::new
        );

        public static BeehiveBlockEntity.Occupant of(Entity p_331052_) {
            CompoundTag compoundtag = new CompoundTag();
            p_331052_.save(compoundtag);
            BeehiveBlockEntity.IGNORED_BEE_TAGS.forEach(compoundtag::remove);
            boolean flag = compoundtag.getBoolean("HasNectar");
            return new BeehiveBlockEntity.Occupant(CustomData.of(compoundtag), 0, flag ? 2400 : 600);
        }

        public static BeehiveBlockEntity.Occupant create(int p_330047_) {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE).toString());
            return new BeehiveBlockEntity.Occupant(CustomData.of(compoundtag), p_330047_, 600);
        }

        @Nullable
        public Entity createEntity(Level p_328931_, BlockPos p_336164_) {
            CompoundTag compoundtag = this.entityData.copyTag();
            BeehiveBlockEntity.IGNORED_BEE_TAGS.forEach(compoundtag::remove);
            Entity entity = EntityType.loadEntityRecursive(compoundtag, p_328931_, EntitySpawnReason.LOAD, p_334152_ -> p_334152_);
            if (entity != null && entity.getType().is(EntityTypeTags.BEEHIVE_INHABITORS)) {
                entity.setNoGravity(true);
                if (entity instanceof Bee bee) {
                    bee.setHivePos(p_336164_);
                    setBeeReleaseData(this.ticksInHive, bee);
                }

                return entity;
            } else {
                return null;
            }
        }

        private static void setBeeReleaseData(int p_330253_, Bee p_331091_) {
            int i = p_331091_.getAge();
            if (i < 0) {
                p_331091_.setAge(Math.min(0, i + p_330253_));
            } else if (i > 0) {
                p_331091_.setAge(Math.max(0, i - p_330253_));
            }

            p_331091_.setInLoveTime(Math.max(0, p_331091_.getInLoveTime() - p_330253_));
        }
    }
}