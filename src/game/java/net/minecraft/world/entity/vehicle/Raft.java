package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class Raft extends AbstractBoat {
    public Raft(EntityType<? extends Raft> p_368811_, Level p_365893_, Supplier<Item> p_363862_) {
        super(p_368811_, p_365893_, p_363862_);
    }

    @Override
    protected double rideHeight(EntityDimensions p_365110_) {
        return (double)(p_365110_.height() * 0.8888889F);
    }
}