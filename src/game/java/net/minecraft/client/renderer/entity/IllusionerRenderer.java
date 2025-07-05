package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IllusionerRenderer extends IllagerRenderer<Illusioner, IllusionerRenderState> {
    private static final ResourceLocation ILLUSIONER = ResourceLocation.withDefaultNamespace("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRendererProvider.Context p_174186_) {
        super(p_174186_, new IllagerModel<>(p_174186_.bakeLayer(ModelLayers.ILLUSIONER)), 0.5F);
        this.addLayer(
            new ItemInHandLayer<IllusionerRenderState, IllagerModel<IllusionerRenderState>>(this) {
                public void render(
                    PoseStack p_114967_, MultiBufferSource p_114968_, int p_114969_, IllusionerRenderState p_363608_, float p_114971_, float p_114972_
                ) {
                    if (p_363608_.isCastingSpell || p_363608_.isAggressive) {
                        super.render(p_114967_, p_114968_, p_114969_, p_363608_, p_114971_, p_114972_);
                    }
                }
            }
        );
        this.model.getHat().visible = true;
    }

    public ResourceLocation getTextureLocation(IllusionerRenderState p_369391_) {
        return ILLUSIONER;
    }

    public IllusionerRenderState createRenderState() {
        return new IllusionerRenderState();
    }

    public void extractRenderState(Illusioner p_362717_, IllusionerRenderState p_369406_, float p_368331_) {
        super.extractRenderState(p_362717_, p_369406_, p_368331_);
        Vec3[] avec3 = p_362717_.getIllusionOffsets(p_368331_);
        p_369406_.illusionOffsets = Arrays.copyOf(avec3, avec3.length);
        p_369406_.isCastingSpell = p_362717_.isCastingSpell();
    }

    public void render(IllusionerRenderState p_369374_, PoseStack p_114932_, MultiBufferSource p_114933_, int p_114934_) {
        if (p_369374_.isInvisible) {
            Vec3[] avec3 = p_369374_.illusionOffsets;

            for (int i = 0; i < avec3.length; i++) {
                p_114932_.pushPose();
                p_114932_.translate(
                    avec3[i].x + (double)Mth.cos((float)i + p_369374_.ageInTicks * 0.5F) * 0.025,
                    avec3[i].y + (double)Mth.cos((float)i + p_369374_.ageInTicks * 0.75F) * 0.0125,
                    avec3[i].z + (double)Mth.cos((float)i + p_369374_.ageInTicks * 0.7F) * 0.025
                );
                super.render(p_369374_, p_114932_, p_114933_, p_114934_);
                p_114932_.popPose();
            }
        } else {
            super.render(p_369374_, p_114932_, p_114933_, p_114934_);
        }
    }

    protected boolean isBodyVisible(IllusionerRenderState p_361370_) {
        return true;
    }

    protected AABB getBoundingBoxForCulling(Illusioner p_365734_) {
        return super.getBoundingBoxForCulling(p_365734_).inflate(3.0, 0.0, 3.0);
    }
}