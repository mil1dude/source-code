package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ComparatorBlockEntity extends BlockEntity {
    private int output;

    public ComparatorBlockEntity(BlockPos p_155386_, BlockState p_155387_) {
        super(BlockEntityType.COMPARATOR, p_155386_, p_155387_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187493_, HolderLookup.Provider p_328948_) {
        super.saveAdditional(p_187493_, p_328948_);
        p_187493_.putInt("OutputSignal", this.output);
    }

    @Override
    protected void loadAdditional(CompoundTag p_334222_, HolderLookup.Provider p_329151_) {
        super.loadAdditional(p_334222_, p_329151_);
        this.output = p_334222_.getInt("OutputSignal");
    }

    public int getOutputSignal() {
        return this.output;
    }

    public void setOutputSignal(int p_59176_) {
        this.output = p_59176_;
    }
}