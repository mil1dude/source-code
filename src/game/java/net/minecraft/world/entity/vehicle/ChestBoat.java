package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ChestBoat extends AbstractChestBoat {
    public ChestBoat(EntityType<? extends ChestBoat> p_368923_, Level p_219872_, Supplier<Item> p_367695_) {
        super(p_368923_, p_219872_, p_367695_);
    }

    @Override
    protected double rideHeight(EntityDimensions p_362135_) {
        return (double)(p_362135_.height() / 3.0F);
    }
}