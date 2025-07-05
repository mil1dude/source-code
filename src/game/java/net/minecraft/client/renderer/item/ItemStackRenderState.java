package net.minecraft.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemStackRenderState {
    ItemDisplayContext displayContext = ItemDisplayContext.NONE;
    boolean isLeftHand;
    private int activeLayerCount;
    private ItemStackRenderState.LayerRenderState[] layers = new ItemStackRenderState.LayerRenderState[]{new ItemStackRenderState.LayerRenderState()};

    public void ensureCapacity(int p_378622_) {
        int i = this.layers.length;
        int j = this.activeLayerCount + p_378622_;
        if (j > i) {
            this.layers = Arrays.copyOf(this.layers, j);

            for (int k = i; k < j; k++) {
                this.layers[k] = new ItemStackRenderState.LayerRenderState();
            }
        }
    }

    public ItemStackRenderState.LayerRenderState newLayer() {
        this.ensureCapacity(1);
        return this.layers[this.activeLayerCount++];
    }

    public void clear() {
        this.displayContext = ItemDisplayContext.NONE;
        this.isLeftHand = false;

        for (int i = 0; i < this.activeLayerCount; i++) {
            this.layers[i].clear();
        }

        this.activeLayerCount = 0;
    }

    private ItemStackRenderState.LayerRenderState firstLayer() {
        return this.layers[0];
    }

    public boolean isEmpty() {
        return this.activeLayerCount == 0;
    }

    public boolean isGui3d() {
        return this.firstLayer().isGui3d();
    }

    public boolean usesBlockLight() {
        return this.firstLayer().usesBlockLight();
    }

    @Nullable
    public TextureAtlasSprite pickParticleIcon(RandomSource p_376964_) {
        if (this.activeLayerCount == 0) {
            return null;
        } else {
            BakedModel bakedmodel = this.layers[p_376964_.nextInt(this.activeLayerCount)].model;
            return bakedmodel == null ? null : bakedmodel.getParticleIcon();
        }
    }

    public ItemTransform transform() {
        return this.firstLayer().transform();
    }

    public void render(PoseStack p_375639_, MultiBufferSource p_377308_, int p_376259_, int p_376823_) {
        for (int i = 0; i < this.activeLayerCount; i++) {
            this.layers[i].render(p_375639_, p_377308_, p_376259_, p_376823_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum FoilType {
        NONE,
        STANDARD,
        SPECIAL;
    }

    @OnlyIn(Dist.CLIENT)
    public class LayerRenderState {
        @Nullable
        BakedModel model;
        @Nullable
        private RenderType renderType;
        private ItemStackRenderState.FoilType foilType = ItemStackRenderState.FoilType.NONE;
        private int[] tintLayers = new int[0];
        @Nullable
        private SpecialModelRenderer<Object> specialRenderer;
        @Nullable
        private Object argumentForSpecialRendering;

        public void clear() {
            this.model = null;
            this.renderType = null;
            this.foilType = ItemStackRenderState.FoilType.NONE;
            this.specialRenderer = null;
            this.argumentForSpecialRendering = null;
            Arrays.fill(this.tintLayers, -1);
        }

        public void setupBlockModel(BakedModel p_377991_, RenderType p_377964_) {
            this.model = p_377991_;
            this.renderType = p_377964_;
        }

        public <T> void setupSpecialModel(SpecialModelRenderer<T> p_375891_, @Nullable T p_375474_, BakedModel p_376070_) {
            this.model = p_376070_;
            this.specialRenderer = eraseSpecialRenderer(p_375891_);
            this.argumentForSpecialRendering = p_375474_;
        }

        private static SpecialModelRenderer<Object> eraseSpecialRenderer(SpecialModelRenderer<?> p_377056_) {
            return (SpecialModelRenderer<Object>)p_377056_;
        }

        public void setFoilType(ItemStackRenderState.FoilType p_377629_) {
            this.foilType = p_377629_;
        }

        public int[] prepareTintLayers(int p_375742_) {
            if (p_375742_ > this.tintLayers.length) {
                this.tintLayers = new int[p_375742_];
                Arrays.fill(this.tintLayers, -1);
            }

            return this.tintLayers;
        }

        ItemTransform transform() {
            return this.model != null ? this.model.getTransforms().getTransform(ItemStackRenderState.this.displayContext) : ItemTransform.NO_TRANSFORM;
        }

        void render(PoseStack p_377989_, MultiBufferSource p_377594_, int p_375616_, int p_376132_) {
            p_377989_.pushPose();
            this.transform().apply(ItemStackRenderState.this.isLeftHand, p_377989_);
            p_377989_.translate(-0.5F, -0.5F, -0.5F);
            if (this.specialRenderer != null) {
                this.specialRenderer
                    .render(
                        this.argumentForSpecialRendering,
                        ItemStackRenderState.this.displayContext,
                        p_377989_,
                        p_377594_,
                        p_375616_,
                        p_376132_,
                        this.foilType != ItemStackRenderState.FoilType.NONE
                    );
            } else if (this.model != null) {
                ItemRenderer.renderItem(
                    ItemStackRenderState.this.displayContext,
                    p_377989_,
                    p_377594_,
                    p_375616_,
                    p_376132_,
                    this.tintLayers,
                    this.model,
                    this.renderType,
                    this.foilType
                );
            }

            p_377989_.popPose();
        }

        boolean isGui3d() {
            return this.model != null && this.model.isGui3d();
        }

        boolean usesBlockLight() {
            return this.model != null && this.model.usesBlockLight();
        }
    }
}