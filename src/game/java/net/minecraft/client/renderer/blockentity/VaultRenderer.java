package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VaultRenderer implements BlockEntityRenderer<VaultBlockEntity> {
    private final ItemModelResolver itemModelResolver;
    private final RandomSource random = RandomSource.create();
    private final ItemClusterRenderState renderState = new ItemClusterRenderState();

    public VaultRenderer(BlockEntityRendererProvider.Context p_335617_) {
        this.itemModelResolver = p_335617_.getItemModelResolver();
    }

    public void render(VaultBlockEntity p_335871_, float p_335940_, PoseStack p_331267_, MultiBufferSource p_329108_, int p_330387_, int p_332341_) {
        if (VaultBlockEntity.Client.shouldDisplayActiveEffects(p_335871_.getSharedData())) {
            Level level = p_335871_.getLevel();
            if (level != null) {
                ItemStack itemstack = p_335871_.getSharedData().getDisplayItem();
                if (!itemstack.isEmpty()) {
                    this.itemModelResolver.updateForTopItem(this.renderState.item, itemstack, ItemDisplayContext.GROUND, false, level, null, 0);
                    this.renderState.count = ItemClusterRenderState.getRenderedAmount(itemstack.getCount());
                    this.renderState.seed = ItemClusterRenderState.getSeedForItemStack(itemstack);
                    VaultClientData vaultclientdata = p_335871_.getClientData();
                    p_331267_.pushPose();
                    p_331267_.translate(0.5F, 0.4F, 0.5F);
                    p_331267_.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(p_335940_, vaultclientdata.previousSpin(), vaultclientdata.currentSpin())));
                    ItemEntityRenderer.renderMultipleFromCount(p_331267_, p_329108_, p_330387_, this.renderState, this.random);
                    p_331267_.popPose();
                }
            }
        }
    }
}