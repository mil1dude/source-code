package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult.Error;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;

public class TrialSpawnerBlockEntity extends BlockEntity implements Spawner, TrialSpawner.StateAccessor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private TrialSpawner trialSpawner;

    public TrialSpawnerBlockEntity(BlockPos p_309527_, BlockState p_312341_) {
        super(BlockEntityType.TRIAL_SPAWNER, p_309527_, p_312341_);
        PlayerDetector playerdetector = PlayerDetector.NO_CREATIVE_PLAYERS;
        PlayerDetector.EntitySelector playerdetector$entityselector = PlayerDetector.EntitySelector.SELECT_FROM_LEVEL;
        this.trialSpawner = new TrialSpawner(this, playerdetector, playerdetector$entityselector);
    }

    @Override
    protected void loadAdditional(CompoundTag p_330602_, HolderLookup.Provider p_329868_) {
        super.loadAdditional(p_330602_, p_329868_);
        this.trialSpawner
            .codec()
            .parse(p_329868_.createSerializationContext(NbtOps.INSTANCE), p_330602_)
            .resultOrPartial(LOGGER::error)
            .ifPresent(p_311010_ -> this.trialSpawner = p_311010_);
        if (this.level != null) {
            this.markUpdated();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag p_310285_, HolderLookup.Provider p_332039_) {
        super.saveAdditional(p_310285_, p_332039_);
        this.trialSpawner
            .codec()
            .encodeStart(p_332039_.createSerializationContext(NbtOps.INSTANCE), this.trialSpawner)
            .ifSuccess(p_312114_ -> p_310285_.merge((CompoundTag)p_312114_))
            .ifError(p_327324_ -> LOGGER.warn("Failed to encode TrialSpawner {}", p_327324_.message()));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p_335483_) {
        return this.trialSpawner.getData().getUpdateTag(this.getBlockState().getValue(TrialSpawnerBlock.STATE));
    }

    @Override
    public void setEntityId(EntityType<?> p_312357_, RandomSource p_313173_) {
        if (this.level == null) {
            Util.logAndPauseIfInIde("Expected non-null level");
        } else {
            this.trialSpawner.overrideEntityToSpawn(p_312357_, this.level);
            this.setChanged();
        }
    }

    public TrialSpawner getTrialSpawner() {
        return this.trialSpawner;
    }

    @Override
    public TrialSpawnerState getState() {
        return !this.getBlockState().hasProperty(BlockStateProperties.TRIAL_SPAWNER_STATE)
            ? TrialSpawnerState.INACTIVE
            : this.getBlockState().getValue(BlockStateProperties.TRIAL_SPAWNER_STATE);
    }

    @Override
    public void setState(Level p_313150_, TrialSpawnerState p_310751_) {
        this.setChanged();
        p_313150_.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(BlockStateProperties.TRIAL_SPAWNER_STATE, p_310751_));
    }

    @Override
    public void markUpdated() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}