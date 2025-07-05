package net.minecraft.client.renderer.entity.state;

import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseRenderState extends EquineRenderState {
    public Variant variant = Variant.WHITE;
    public Markings markings = Markings.NONE;
    public ItemStack bodyArmorItem = ItemStack.EMPTY;
}