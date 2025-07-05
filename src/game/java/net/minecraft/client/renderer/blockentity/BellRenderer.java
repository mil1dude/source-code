package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.BellModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellRenderer implements BlockEntityRenderer<BellBlockEntity> {
    public static final Material BELL_RESOURCE_LOCATION = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/bell/bell_body"));
    private final BellModel model;

    public BellRenderer(BlockEntityRendererProvider.Context p_173554_) {
        this.model = new BellModel(p_173554_.bakeLayer(ModelLayers.BELL));
    }

    public void render(BellBlockEntity p_112233_, float p_112234_, PoseStack p_112235_, MultiBufferSource p_112236_, int p_112237_, int p_112238_) {
        VertexConsumer vertexconsumer = BELL_RESOURCE_LOCATION.buffer(p_112236_, RenderType::entitySolid);
        this.model.setupAnim(p_112233_, p_112234_);
        this.model.renderToBuffer(p_112235_, vertexconsumer, p_112237_, p_112238_);
    }
}