package net.minecraft.world.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class Boat extends AbstractBoat {
    public Boat(EntityType<? extends Boat> p_38290_, Level p_38291_, Supplier<Item> p_364510_) {
        super(p_38290_, p_38291_, p_364510_);
    }

    @Override
    protected double rideHeight(EntityDimensions p_367538_) {
        return (double)(p_367538_.height() / 3.0F);
    }
}