package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ChestRaft extends AbstractChestBoat {
    public ChestRaft(EntityType<? extends ChestRaft> p_365673_, Level p_368485_, Supplier<Item> p_367213_) {
        super(p_365673_, p_368485_, p_367213_);
    }

    @Override
    protected double rideHeight(EntityDimensions p_368931_) {
        return (double)(p_368931_.height() * 0.8888889F);
    }
}