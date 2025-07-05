package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombieRenderer extends AbstractZombieRenderer<Zombie, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    public ZombieRenderer(EntityRendererProvider.Context p_174456_) {
        this(
            p_174456_, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR, ModelLayers.ZOMBIE_BABY_INNER_ARMOR, ModelLayers.ZOMBIE_BABY_OUTER_ARMOR
        );
    }

    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    public ZombieRenderer(
        EntityRendererProvider.Context p_174458_,
        ModelLayerLocation p_174459_,
        ModelLayerLocation p_174460_,
        ModelLayerLocation p_174461_,
        ModelLayerLocation p_362643_,
        ModelLayerLocation p_361704_,
        ModelLayerLocation p_369873_
    ) {
        super(
            p_174458_,
            new ZombieModel<>(p_174458_.bakeLayer(p_174459_)),
            new ZombieModel<>(p_174458_.bakeLayer(p_174460_)),
            new ZombieModel<>(p_174458_.bakeLayer(p_174461_)),
            new ZombieModel<>(p_174458_.bakeLayer(p_362643_)),
            new ZombieModel<>(p_174458_.bakeLayer(p_361704_)),
            new ZombieModel<>(p_174458_.bakeLayer(p_369873_))
        );
    }
}