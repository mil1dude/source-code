package net.minecraft.client.renderer.block.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class ItemModelGenerator implements UnbakedModel {
    public static final ResourceLocation GENERATED_ITEM_MODEL_ID = ResourceLocation.withDefaultNamespace("builtin/generated");
    public static final List<String> LAYERS = List.of("layer0", "layer1", "layer2", "layer3", "layer4");
    private static final float MIN_Z = 7.5F;
    private static final float MAX_Z = 8.5F;
    private static final TextureSlots.Data TEXTURE_SLOTS = new TextureSlots.Data.Builder().addReference("particle", "layer0").build();

    @Override
    public TextureSlots.Data getTextureSlots() {
        return TEXTURE_SLOTS;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver p_376467_) {
    }

    @Nullable
    @Override
    public UnbakedModel.GuiLight getGuiLight() {
        return UnbakedModel.GuiLight.FRONT;
    }

    @Override
    public BakedModel bake(
        TextureSlots p_378742_, ModelBaker p_376373_, ModelState p_377118_, boolean p_375903_, boolean p_378244_, ItemTransforms p_377372_
    ) {
        return this.bake(p_378742_, p_376373_.sprites(), p_377118_, p_375903_, p_378244_, p_377372_);
    }

    private BakedModel bake(
        TextureSlots p_377946_, SpriteGetter p_378401_, ModelState p_375548_, boolean p_377983_, boolean p_377097_, ItemTransforms p_377316_
    ) {
        TextureSlots.Data.Builder textureslots$data$builder = new TextureSlots.Data.Builder();
        List<BlockElement> list = new ArrayList<>();

        for (int i = 0; i < LAYERS.size(); i++) {
            String s = LAYERS.get(i);
            Material material = p_377946_.getMaterial(s);
            if (material == null) {
                break;
            }

            textureslots$data$builder.addTexture(s, material);
            SpriteContents spritecontents = p_378401_.get(material).contents();
            list.addAll(this.processFrames(i, s, spritecontents));
        }

        return SimpleBakedModel.bakeElements(list, p_377946_, p_378401_, p_375548_, p_377983_, p_377097_, false, p_377316_);
    }

    private List<BlockElement> processFrames(int p_111639_, String p_111640_, SpriteContents p_251768_) {
        Map<Direction, BlockElementFace> map = Map.of(
            Direction.SOUTH,
            new BlockElementFace(null, p_111639_, p_111640_, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)),
            Direction.NORTH,
            new BlockElementFace(null, p_111639_, p_111640_, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0))
        );
        List<BlockElement> list = new ArrayList<>();
        list.add(new BlockElement(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), map));
        list.addAll(this.createSideElements(p_251768_, p_111640_, p_111639_));
        return list;
    }

    private List<BlockElement> createSideElements(SpriteContents p_248810_, String p_111663_, int p_111664_) {
        float f = (float)p_248810_.width();
        float f1 = (float)p_248810_.height();
        List<BlockElement> list = new ArrayList<>();

        for (ItemModelGenerator.Span itemmodelgenerator$span : this.getSpans(p_248810_)) {
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 0.0F;
            float f7 = 0.0F;
            float f8 = 0.0F;
            float f9 = 0.0F;
            float f10 = 16.0F / f;
            float f11 = 16.0F / f1;
            float f12 = (float)itemmodelgenerator$span.getMin();
            float f13 = (float)itemmodelgenerator$span.getMax();
            float f14 = (float)itemmodelgenerator$span.getAnchor();
            ItemModelGenerator.SpanFacing itemmodelgenerator$spanfacing = itemmodelgenerator$span.getFacing();
            switch (itemmodelgenerator$spanfacing) {
                case UP:
                    f6 = f12;
                    f2 = f12;
                    f4 = f7 = f13 + 1.0F;
                    f8 = f14;
                    f3 = f14;
                    f5 = f14;
                    f9 = f14 + 1.0F;
                    break;
                case DOWN:
                    f8 = f14;
                    f9 = f14 + 1.0F;
                    f6 = f12;
                    f2 = f12;
                    f4 = f7 = f13 + 1.0F;
                    f3 = f14 + 1.0F;
                    f5 = f14 + 1.0F;
                    break;
                case LEFT:
                    f6 = f14;
                    f2 = f14;
                    f4 = f14;
                    f7 = f14 + 1.0F;
                    f9 = f12;
                    f3 = f12;
                    f5 = f8 = f13 + 1.0F;
                    break;
                case RIGHT:
                    f6 = f14;
                    f7 = f14 + 1.0F;
                    f2 = f14 + 1.0F;
                    f4 = f14 + 1.0F;
                    f9 = f12;
                    f3 = f12;
                    f5 = f8 = f13 + 1.0F;
            }

            f2 *= f10;
            f4 *= f10;
            f3 *= f11;
            f5 *= f11;
            f3 = 16.0F - f3;
            f5 = 16.0F - f5;
            f6 *= f10;
            f7 *= f10;
            f8 *= f11;
            f9 *= f11;
            Map<Direction, BlockElementFace> map = Map.of(
                itemmodelgenerator$spanfacing.getDirection(), new BlockElementFace(null, p_111664_, p_111663_, new BlockFaceUV(new float[]{f6, f8, f7, f9}, 0))
            );
            switch (itemmodelgenerator$spanfacing) {
                case UP:
                    list.add(new BlockElement(new Vector3f(f2, f3, 7.5F), new Vector3f(f4, f3, 8.5F), map));
                    break;
                case DOWN:
                    list.add(new BlockElement(new Vector3f(f2, f5, 7.5F), new Vector3f(f4, f5, 8.5F), map));
                    break;
                case LEFT:
                    list.add(new BlockElement(new Vector3f(f2, f3, 7.5F), new Vector3f(f2, f5, 8.5F), map));
                    break;
                case RIGHT:
                    list.add(new BlockElement(new Vector3f(f4, f3, 7.5F), new Vector3f(f4, f5, 8.5F), map));
            }
        }

        return list;
    }

    private List<ItemModelGenerator.Span> getSpans(SpriteContents p_250338_) {
        int i = p_250338_.width();
        int j = p_250338_.height();
        List<ItemModelGenerator.Span> list = new ArrayList<>();
        p_250338_.getUniqueFrames().forEach(p_173444_ -> {
            for (int k = 0; k < j; k++) {
                for (int l = 0; l < i; l++) {
                    boolean flag = !this.isTransparent(p_250338_, p_173444_, l, k, i, j);
                    this.checkTransition(ItemModelGenerator.SpanFacing.UP, list, p_250338_, p_173444_, l, k, i, j, flag);
                    this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, list, p_250338_, p_173444_, l, k, i, j, flag);
                    this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, list, p_250338_, p_173444_, l, k, i, j, flag);
                    this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, list, p_250338_, p_173444_, l, k, i, j, flag);
                }
            }
        });
        return list;
    }

    private void checkTransition(
        ItemModelGenerator.SpanFacing p_251572_,
        List<ItemModelGenerator.Span> p_248882_,
        SpriteContents p_249847_,
        int p_250616_,
        int p_251416_,
        int p_249664_,
        int p_250174_,
        int p_250897_,
        boolean p_248773_
    ) {
        boolean flag = this.isTransparent(p_249847_, p_250616_, p_251416_ + p_251572_.getXOffset(), p_249664_ + p_251572_.getYOffset(), p_250174_, p_250897_)
            && p_248773_;
        if (flag) {
            this.createOrExpandSpan(p_248882_, p_251572_, p_251416_, p_249664_);
        }
    }

    private void createOrExpandSpan(List<ItemModelGenerator.Span> p_111666_, ItemModelGenerator.SpanFacing p_111667_, int p_111668_, int p_111669_) {
        ItemModelGenerator.Span itemmodelgenerator$span = null;

        for (ItemModelGenerator.Span itemmodelgenerator$span1 : p_111666_) {
            if (itemmodelgenerator$span1.getFacing() == p_111667_) {
                int i = p_111667_.isHorizontal() ? p_111669_ : p_111668_;
                if (itemmodelgenerator$span1.getAnchor() == i) {
                    itemmodelgenerator$span = itemmodelgenerator$span1;
                    break;
                }
            }
        }

        int j = p_111667_.isHorizontal() ? p_111669_ : p_111668_;
        int k = p_111667_.isHorizontal() ? p_111668_ : p_111669_;
        if (itemmodelgenerator$span == null) {
            p_111666_.add(new ItemModelGenerator.Span(p_111667_, k, j));
        } else {
            itemmodelgenerator$span.expand(k);
        }
    }

    private boolean isTransparent(SpriteContents p_249650_, int p_250692_, int p_251914_, int p_252343_, int p_250258_, int p_248997_) {
        return p_251914_ >= 0 && p_252343_ >= 0 && p_251914_ < p_250258_ && p_252343_ < p_248997_ ? p_249650_.isTransparent(p_250692_, p_251914_, p_252343_) : true;
    }

    @OnlyIn(Dist.CLIENT)
    static class Span {
        private final ItemModelGenerator.SpanFacing facing;
        private int min;
        private int max;
        private final int anchor;

        public Span(ItemModelGenerator.SpanFacing p_111680_, int p_111681_, int p_111682_) {
            this.facing = p_111680_;
            this.min = p_111681_;
            this.max = p_111681_;
            this.anchor = p_111682_;
        }

        public void expand(int p_111685_) {
            if (p_111685_ < this.min) {
                this.min = p_111685_;
            } else if (p_111685_ > this.max) {
                this.max = p_111685_;
            }
        }

        public ItemModelGenerator.SpanFacing getFacing() {
            return this.facing;
        }

        public int getMin() {
            return this.min;
        }

        public int getMax() {
            return this.max;
        }

        public int getAnchor() {
            return this.anchor;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum SpanFacing {
        UP(Direction.UP, 0, -1),
        DOWN(Direction.DOWN, 0, 1),
        LEFT(Direction.EAST, -1, 0),
        RIGHT(Direction.WEST, 1, 0);

        private final Direction direction;
        private final int xOffset;
        private final int yOffset;

        private SpanFacing(final Direction p_111701_, final int p_111702_, final int p_111703_) {
            this.direction = p_111701_;
            this.xOffset = p_111702_;
            this.yOffset = p_111703_;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public int getXOffset() {
            return this.xOffset;
        }

        public int getYOffset() {
            return this.yOffset;
        }

        boolean isHorizontal() {
            return this == DOWN || this == UP;
        }
    }
}