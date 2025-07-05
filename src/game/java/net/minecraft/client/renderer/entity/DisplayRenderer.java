package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public abstract class DisplayRenderer<T extends Display, S, ST extends DisplayEntityRenderState> extends EntityRenderer<T, ST> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    protected DisplayRenderer(EntityRendererProvider.Context p_270168_) {
        super(p_270168_);
        this.entityRenderDispatcher = p_270168_.getEntityRenderDispatcher();
    }

    protected AABB getBoundingBoxForCulling(T p_368254_) {
        return p_368254_.getBoundingBoxForCulling();
    }

    protected boolean affectedByCulling(T p_365810_) {
        return p_365810_.affectedByCulling();
    }

    private static int getBrightnessOverride(Display p_365446_) {
        Display.RenderState display$renderstate = p_365446_.renderState();
        return display$renderstate != null ? display$renderstate.brightnessOverride() : -1;
    }

    protected int getSkyLightLevel(T p_367797_, BlockPos p_364805_) {
        int i = getBrightnessOverride(p_367797_);
        return i != -1 ? LightTexture.sky(i) : super.getSkyLightLevel(p_367797_, p_364805_);
    }

    protected int getBlockLightLevel(T p_362888_, BlockPos p_365686_) {
        int i = getBrightnessOverride(p_362888_);
        return i != -1 ? LightTexture.block(i) : super.getBlockLightLevel(p_362888_, p_365686_);
    }

    protected float getShadowRadius(ST p_376159_) {
        Display.RenderState display$renderstate = p_376159_.renderState;
        return display$renderstate == null ? 0.0F : display$renderstate.shadowRadius().get(p_376159_.interpolationProgress);
    }

    protected float getShadowStrength(ST p_377182_) {
        Display.RenderState display$renderstate = p_377182_.renderState;
        return display$renderstate == null ? 0.0F : display$renderstate.shadowStrength().get(p_377182_.interpolationProgress);
    }

    public void render(ST p_363838_, PoseStack p_270117_, MultiBufferSource p_270319_, int p_270659_) {
        Display.RenderState display$renderstate = p_363838_.renderState;
        if (display$renderstate != null && p_363838_.hasSubState()) {
            float f = p_363838_.interpolationProgress;
            super.render(p_363838_, p_270117_, p_270319_, p_270659_);
            p_270117_.pushPose();
            p_270117_.mulPose(this.calculateOrientation(display$renderstate, p_363838_, new Quaternionf()));
            Transformation transformation = display$renderstate.transformation().get(f);
            p_270117_.mulPose(transformation.getMatrix());
            this.renderInner(p_363838_, p_270117_, p_270319_, p_270659_, f);
            p_270117_.popPose();
        }
    }

    private Quaternionf calculateOrientation(Display.RenderState p_277846_, ST p_361564_, Quaternionf p_298476_) {
        Camera camera = this.entityRenderDispatcher.camera;

        return switch (p_277846_.billboardConstraints()) {
            case FIXED -> p_298476_.rotationYXZ((float) (-Math.PI / 180.0) * p_361564_.entityYRot, (float) (Math.PI / 180.0) * p_361564_.entityXRot, 0.0F);
            case HORIZONTAL -> p_298476_.rotationYXZ((float) (-Math.PI / 180.0) * p_361564_.entityYRot, (float) (Math.PI / 180.0) * cameraXRot(camera), 0.0F);
            case VERTICAL -> p_298476_.rotationYXZ((float) (-Math.PI / 180.0) * cameraYrot(camera), (float) (Math.PI / 180.0) * p_361564_.entityXRot, 0.0F);
            case CENTER -> p_298476_.rotationYXZ((float) (-Math.PI / 180.0) * cameraYrot(camera), (float) (Math.PI / 180.0) * cameraXRot(camera), 0.0F);
        };
    }

    private static float cameraYrot(Camera p_299213_) {
        return p_299213_.getYRot() - 180.0F;
    }

    private static float cameraXRot(Camera p_297923_) {
        return -p_297923_.getXRot();
    }

    private static <T extends Display> float entityYRot(T p_297849_, float p_297686_) {
        return p_297849_.getYRot(p_297686_);
    }

    private static <T extends Display> float entityXRot(T p_298651_, float p_297691_) {
        return p_298651_.getXRot(p_297691_);
    }

    protected abstract void renderInner(ST p_361844_, PoseStack p_277686_, MultiBufferSource p_277429_, int p_278023_, float p_277453_);

    public void extractRenderState(T p_364120_, ST p_362498_, float p_362522_) {
        super.extractRenderState(p_364120_, p_362498_, p_362522_);
        p_362498_.renderState = p_364120_.renderState();
        p_362498_.interpolationProgress = p_364120_.calculateInterpolationProgress(p_362522_);
        p_362498_.entityYRot = entityYRot(p_364120_, p_362522_);
        p_362498_.entityXRot = entityXRot(p_364120_, p_362522_);
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlockDisplayRenderer
        extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState, BlockDisplayEntityRenderState> {
        private final BlockRenderDispatcher blockRenderer;

        protected BlockDisplayRenderer(EntityRendererProvider.Context p_270283_) {
            super(p_270283_);
            this.blockRenderer = p_270283_.getBlockRenderDispatcher();
        }

        public BlockDisplayEntityRenderState createRenderState() {
            return new BlockDisplayEntityRenderState();
        }

        public void extractRenderState(Display.BlockDisplay p_367120_, BlockDisplayEntityRenderState p_364696_, float p_367582_) {
            super.extractRenderState(p_367120_, p_364696_, p_367582_);
            p_364696_.blockRenderState = p_367120_.blockRenderState();
        }

        public void renderInner(BlockDisplayEntityRenderState p_363283_, PoseStack p_277831_, MultiBufferSource p_277554_, int p_278071_, float p_277847_) {
            this.blockRenderer.renderSingleBlock(p_363283_.blockRenderState.blockState(), p_277831_, p_277554_, p_278071_, OverlayTexture.NO_OVERLAY);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ItemDisplayRenderer extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState, ItemDisplayEntityRenderState> {
        private final ItemModelResolver itemModelResolver;

        protected ItemDisplayRenderer(EntityRendererProvider.Context p_270110_) {
            super(p_270110_);
            this.itemModelResolver = p_270110_.getItemModelResolver();
        }

        public ItemDisplayEntityRenderState createRenderState() {
            return new ItemDisplayEntityRenderState();
        }

        public void extractRenderState(Display.ItemDisplay p_368800_, ItemDisplayEntityRenderState p_363947_, float p_365503_) {
            super.extractRenderState(p_368800_, p_363947_, p_365503_);
            Display.ItemDisplay.ItemRenderState display$itemdisplay$itemrenderstate = p_368800_.itemRenderState();
            if (display$itemdisplay$itemrenderstate != null) {
                this.itemModelResolver
                    .updateForNonLiving(p_363947_.item, display$itemdisplay$itemrenderstate.itemStack(), display$itemdisplay$itemrenderstate.itemTransform(), p_368800_);
            } else {
                p_363947_.item.clear();
            }
        }

        public void renderInner(ItemDisplayEntityRenderState p_361473_, PoseStack p_277361_, MultiBufferSource p_277912_, int p_277474_, float p_278032_) {
            if (!p_361473_.item.isEmpty()) {
                p_277361_.mulPose(Axis.YP.rotation((float) Math.PI));
                p_361473_.item.render(p_277361_, p_277912_, p_277474_, OverlayTexture.NO_OVERLAY);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class TextDisplayRenderer extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState, TextDisplayEntityRenderState> {
        private final Font font;

        protected TextDisplayRenderer(EntityRendererProvider.Context p_271012_) {
            super(p_271012_);
            this.font = p_271012_.getFont();
        }

        public TextDisplayEntityRenderState createRenderState() {
            return new TextDisplayEntityRenderState();
        }

        public void extractRenderState(Display.TextDisplay p_365496_, TextDisplayEntityRenderState p_366254_, float p_368471_) {
            super.extractRenderState(p_365496_, p_366254_, p_368471_);
            p_366254_.textRenderState = p_365496_.textRenderState();
            p_366254_.cachedInfo = p_365496_.cacheDisplay(this::splitLines);
        }

        private Display.TextDisplay.CachedInfo splitLines(Component p_270823_, int p_270893_) {
            List<FormattedCharSequence> list = this.font.split(p_270823_, p_270893_);
            List<Display.TextDisplay.CachedLine> list1 = new ArrayList<>(list.size());
            int i = 0;

            for (FormattedCharSequence formattedcharsequence : list) {
                int j = this.font.width(formattedcharsequence);
                i = Math.max(i, j);
                list1.add(new Display.TextDisplay.CachedLine(formattedcharsequence, j));
            }

            return new Display.TextDisplay.CachedInfo(list1, i);
        }

        public void renderInner(TextDisplayEntityRenderState p_366994_, PoseStack p_277536_, MultiBufferSource p_277845_, int p_278046_, float p_277769_) {
            Display.TextDisplay.TextRenderState display$textdisplay$textrenderstate = p_366994_.textRenderState;
            byte b0 = display$textdisplay$textrenderstate.flags();
            boolean flag = (b0 & 2) != 0;
            boolean flag1 = (b0 & 4) != 0;
            boolean flag2 = (b0 & 1) != 0;
            Display.TextDisplay.Align display$textdisplay$align = Display.TextDisplay.getAlign(b0);
            byte b1 = (byte)display$textdisplay$textrenderstate.textOpacity().get(p_277769_);
            int i;
            if (flag1) {
                float f = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
                i = (int)(f * 255.0F) << 24;
            } else {
                i = display$textdisplay$textrenderstate.backgroundColor().get(p_277769_);
            }

            float f2 = 0.0F;
            Matrix4f matrix4f = p_277536_.last().pose();
            matrix4f.rotate((float) Math.PI, 0.0F, 1.0F, 0.0F);
            matrix4f.scale(-0.025F, -0.025F, -0.025F);
            Display.TextDisplay.CachedInfo display$textdisplay$cachedinfo = p_366994_.cachedInfo;
            int j = 1;
            int k = 9 + 1;
            int l = display$textdisplay$cachedinfo.width();
            int i1 = display$textdisplay$cachedinfo.lines().size() * k - 1;
            matrix4f.translate(1.0F - (float)l / 2.0F, (float)(-i1), 0.0F);
            if (i != 0) {
                VertexConsumer vertexconsumer = p_277845_.getBuffer(flag ? RenderType.textBackgroundSeeThrough() : RenderType.textBackground());
                vertexconsumer.addVertex(matrix4f, -1.0F, -1.0F, 0.0F).setColor(i).setLight(p_278046_);
                vertexconsumer.addVertex(matrix4f, -1.0F, (float)i1, 0.0F).setColor(i).setLight(p_278046_);
                vertexconsumer.addVertex(matrix4f, (float)l, (float)i1, 0.0F).setColor(i).setLight(p_278046_);
                vertexconsumer.addVertex(matrix4f, (float)l, -1.0F, 0.0F).setColor(i).setLight(p_278046_);
            }

            for (Display.TextDisplay.CachedLine display$textdisplay$cachedline : display$textdisplay$cachedinfo.lines()) {
                float f1 = switch (display$textdisplay$align) {
                    case LEFT -> 0.0F;
                    case RIGHT -> (float)(l - display$textdisplay$cachedline.width());
                    case CENTER -> (float)l / 2.0F - (float)display$textdisplay$cachedline.width() / 2.0F;
                };
                this.font
                    .drawInBatch(
                        display$textdisplay$cachedline.contents(),
                        f1,
                        f2,
                        b1 << 24 | 16777215,
                        flag2,
                        matrix4f,
                        p_277845_,
                        flag ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
                        0,
                        p_278046_
                    );
                f2 += (float)k;
            }
        }
    }
}