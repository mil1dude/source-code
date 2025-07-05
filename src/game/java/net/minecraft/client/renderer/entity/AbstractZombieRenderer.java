package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractZombieRenderer<T extends Zombie, S extends ZombieRenderState, M extends ZombieModel<S>> extends HumanoidMobRenderer<T, S, M> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    protected AbstractZombieRenderer(EntityRendererProvider.Context p_173910_, M p_173911_, M p_173912_, M p_173913_, M p_369784_, M p_360801_, M p_367849_) {
        super(p_173910_, p_173911_, p_173912_, 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, p_173913_, p_369784_, p_360801_, p_367849_, p_173910_.getEquipmentRenderer()));
    }

    public ResourceLocation getTextureLocation(S p_361963_) {
        return ZOMBIE_LOCATION;
    }

    public void extractRenderState(T p_366762_, S p_362706_, float p_366302_) {
        super.extractRenderState(p_366762_, p_362706_, p_366302_);
        p_362706_.isAggressive = p_366762_.isAggressive();
        p_362706_.isConverting = p_366762_.isUnderWaterConverting();
    }

    protected boolean isShaking(S p_361791_) {
        return super.isShaking(p_361791_) || p_361791_.isConverting;
    }
}