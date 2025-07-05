package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemEntityRenderer extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
    private static final float ITEM_BUNDLE_OFFSET_SCALE = 0.15F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_X = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Y = 0.0F;
    private static final float FLAT_ITEM_BUNDLE_OFFSET_Z = 0.09375F;
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();

    public ItemEntityRenderer(EntityRendererProvider.Context p_174198_) {
        super(p_174198_);
        this.itemModelResolver = p_174198_.getItemModelResolver();
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    public ItemEntityRenderState createRenderState() {
        return new ItemEntityRenderState();
    }

    public void extractRenderState(ItemEntity p_365788_, ItemEntityRenderState p_361751_, float p_369533_) {
        super.extractRenderState(p_365788_, p_361751_, p_369533_);
        p_361751_.ageInTicks = (float)p_365788_.getAge() + p_369533_;
        p_361751_.bobOffset = p_365788_.bobOffs;
        p_361751_.extractItemGroupRenderState(p_365788_, p_365788_.getItem(), this.itemModelResolver);
    }

    public void render(ItemEntityRenderState p_365095_, PoseStack p_115030_, MultiBufferSource p_115031_, int p_115032_) {
        if (!p_365095_.item.isEmpty()) {
            p_115030_.pushPose();
            float f = 0.25F;
            float f1 = Mth.sin(p_365095_.ageInTicks / 10.0F + p_365095_.bobOffset) * 0.1F + 0.1F;
            float f2 = p_365095_.item.transform().scale.y();
            p_115030_.translate(0.0F, f1 + 0.25F * f2, 0.0F);
            float f3 = ItemEntity.getSpin(p_365095_.ageInTicks, p_365095_.bobOffset);
            p_115030_.mulPose(Axis.YP.rotation(f3));
            renderMultipleFromCount(p_115030_, p_115031_, p_115032_, p_365095_, this.random);
            p_115030_.popPose();
            super.render(p_365095_, p_115030_, p_115031_, p_115032_);
        }
    }

    public static void renderMultipleFromCount(PoseStack p_330844_, MultiBufferSource p_333382_, int p_334169_, ItemClusterRenderState p_377874_, RandomSource p_331892_) {
        p_331892_.setSeed((long)p_377874_.seed);
        int i = p_377874_.count;
        ItemStackRenderState itemstackrenderstate = p_377874_.item;
        boolean flag = itemstackrenderstate.isGui3d();
        float f = itemstackrenderstate.transform().scale.x();
        float f1 = itemstackrenderstate.transform().scale.y();
        float f2 = itemstackrenderstate.transform().scale.z();
        if (!flag) {
            float f3 = -0.0F * (float)(i - 1) * 0.5F * f;
            float f4 = -0.0F * (float)(i - 1) * 0.5F * f1;
            float f5 = -0.09375F * (float)(i - 1) * 0.5F * f2;
            p_330844_.translate(f3, f4, f5);
        }

        for (int j = 0; j < i; j++) {
            p_330844_.pushPose();
            if (j > 0) {
                if (flag) {
                    float f7 = (p_331892_.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f9 = (p_331892_.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f6 = (p_331892_.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    p_330844_.translate(f7, f9, f6);
                } else {
                    float f8 = (p_331892_.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float f10 = (p_331892_.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    p_330844_.translate(f8, f10, 0.0F);
                }
            }

            itemstackrenderstate.render(p_330844_, p_333382_, p_334169_, OverlayTexture.NO_OVERLAY);
            p_330844_.popPose();
            if (!flag) {
                p_330844_.translate(0.0F * f, 0.0F * f1, 0.09375F * f2);
            }
        }
    }
}