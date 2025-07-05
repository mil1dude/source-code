package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomHeadLayer<S extends LivingEntityRenderState, M extends EntityModel<S> & HeadedModel> extends RenderLayer<S, M> {
    private static final float ITEM_SCALE = 0.625F;
    private static final float SKULL_SCALE = 1.1875F;
    private final CustomHeadLayer.Transforms transforms;
    private final Function<SkullBlock.Type, SkullModelBase> skullModels;

    public CustomHeadLayer(RenderLayerParent<S, M> p_234822_, EntityModelSet p_234823_) {
        this(p_234822_, p_234823_, CustomHeadLayer.Transforms.DEFAULT);
    }

    public CustomHeadLayer(RenderLayerParent<S, M> p_234829_, EntityModelSet p_234830_, CustomHeadLayer.Transforms p_377766_) {
        super(p_234829_);
        this.transforms = p_377766_;
        this.skullModels = Util.memoize(p_378228_ -> SkullBlockRenderer.createModel(p_234830_, p_378228_));
    }

    public void render(PoseStack p_116731_, MultiBufferSource p_116732_, int p_116733_, S p_363423_, float p_116735_, float p_116736_) {
        if (!p_363423_.headItem.isEmpty() || p_363423_.wornHeadType != null) {
            p_116731_.pushPose();
            p_116731_.scale(this.transforms.horizontalScale(), 1.0F, this.transforms.horizontalScale());
            M m = this.getParentModel();
            m.root().translateAndRotate(p_116731_);
            m.getHead().translateAndRotate(p_116731_);
            if (p_363423_.wornHeadType != null) {
                p_116731_.translate(0.0F, this.transforms.skullYOffset(), 0.0F);
                p_116731_.scale(1.1875F, -1.1875F, -1.1875F);
                p_116731_.translate(-0.5, 0.0, -0.5);
                SkullBlock.Type skullblock$type = p_363423_.wornHeadType;
                SkullModelBase skullmodelbase = this.skullModels.apply(skullblock$type);
                RenderType rendertype = SkullBlockRenderer.getRenderType(skullblock$type, p_363423_.wornHeadProfile);
                SkullBlockRenderer.renderSkull(null, 180.0F, p_363423_.wornHeadAnimationPos, p_116731_, p_116732_, p_116733_, skullmodelbase, rendertype);
            } else {
                translateToHead(p_116731_, this.transforms);
                p_363423_.headItem.render(p_116731_, p_116732_, p_116733_, OverlayTexture.NO_OVERLAY);
            }

            p_116731_.popPose();
        }
    }

    public static void translateToHead(PoseStack p_174484_, CustomHeadLayer.Transforms p_366424_) {
        p_174484_.translate(0.0F, -0.25F + p_366424_.yOffset(), 0.0F);
        p_174484_.mulPose(Axis.YP.rotationDegrees(180.0F));
        p_174484_.scale(0.625F, -0.625F, -0.625F);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Transforms(float yOffset, float skullYOffset, float horizontalScale) {
        public static final CustomHeadLayer.Transforms DEFAULT = new CustomHeadLayer.Transforms(0.0F, 0.0F, 1.0F);
    }
}