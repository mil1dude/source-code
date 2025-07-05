package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.WitherSkullRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherSkullRenderer extends EntityRenderer<WitherSkull, WitherSkullRenderState> {
    private static final ResourceLocation WITHER_INVULNERABLE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
    private static final ResourceLocation WITHER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/wither/wither.png");
    private final SkullModel model;

    public WitherSkullRenderer(EntityRendererProvider.Context p_174449_) {
        super(p_174449_);
        this.model = new SkullModel(p_174449_.bakeLayer(ModelLayers.WITHER_SKULL));
    }

    public static LayerDefinition createSkullLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 35).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    protected int getBlockLightLevel(WitherSkull p_116491_, BlockPos p_116492_) {
        return 15;
    }

    public void render(WitherSkullRenderState p_362201_, PoseStack p_116475_, MultiBufferSource p_116476_, int p_116477_) {
        p_116475_.pushPose();
        p_116475_.scale(-1.0F, -1.0F, 1.0F);
        VertexConsumer vertexconsumer = p_116476_.getBuffer(this.model.renderType(this.getTextureLocation(p_362201_)));
        this.model.setupAnim(0.0F, p_362201_.yRot, p_362201_.xRot);
        this.model.renderToBuffer(p_116475_, vertexconsumer, p_116477_, OverlayTexture.NO_OVERLAY);
        p_116475_.popPose();
        super.render(p_362201_, p_116475_, p_116476_, p_116477_);
    }

    private ResourceLocation getTextureLocation(WitherSkullRenderState p_366519_) {
        return p_366519_.isDangerous ? WITHER_INVULNERABLE_LOCATION : WITHER_LOCATION;
    }

    public WitherSkullRenderState createRenderState() {
        return new WitherSkullRenderState();
    }

    public void extractRenderState(WitherSkull p_360946_, WitherSkullRenderState p_363928_, float p_365976_) {
        super.extractRenderState(p_360946_, p_363928_, p_365976_);
        p_363928_.isDangerous = p_360946_.isDangerous();
        p_363928_.yRot = p_360946_.getYRot(p_365976_);
        p_363928_.xRot = p_360946_.getXRot(p_365976_);
    }
}