package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveSpiderRenderer extends SpiderRenderer<CaveSpider> {
    private static final ResourceLocation CAVE_SPIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/spider/cave_spider.png");

    public CaveSpiderRenderer(EntityRendererProvider.Context p_173946_) {
        super(p_173946_, ModelLayers.CAVE_SPIDER);
        this.shadowRadius = 0.56F;
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState p_367672_) {
        return CAVE_SPIDER_LOCATION;
    }
}