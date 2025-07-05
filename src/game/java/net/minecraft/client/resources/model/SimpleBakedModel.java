package net.minecraft.client.resources.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleBakedModel implements BakedModel {
    public static final String PARTICLE_TEXTURE_REFERENCE = "particle";
    private final List<BakedQuad> unculledFaces;
    private final Map<Direction, List<BakedQuad>> culledFaces;
    private final boolean hasAmbientOcclusion;
    private final boolean isGui3d;
    private final boolean usesBlockLight;
    private final TextureAtlasSprite particleIcon;
    private final ItemTransforms transforms;

    public SimpleBakedModel(
        List<BakedQuad> p_119489_,
        Map<Direction, List<BakedQuad>> p_119490_,
        boolean p_119491_,
        boolean p_119492_,
        boolean p_119493_,
        TextureAtlasSprite p_119494_,
        ItemTransforms p_119495_
    ) {
        this.unculledFaces = p_119489_;
        this.culledFaces = p_119490_;
        this.hasAmbientOcclusion = p_119491_;
        this.isGui3d = p_119493_;
        this.usesBlockLight = p_119492_;
        this.particleIcon = p_119494_;
        this.transforms = p_119495_;
    }

    public static BakedModel bakeElements(
        List<BlockElement> p_377425_,
        TextureSlots p_378525_,
        SpriteGetter p_375793_,
        ModelState p_376680_,
        boolean p_375745_,
        boolean p_376866_,
        boolean p_376846_,
        ItemTransforms p_376883_
    ) {
        TextureAtlasSprite textureatlassprite = findSprite(p_375793_, p_378525_, "particle");
        SimpleBakedModel.Builder simplebakedmodel$builder = new SimpleBakedModel.Builder(p_375745_, p_376866_, p_376846_, p_376883_)
            .particle(textureatlassprite);

        for (BlockElement blockelement : p_377425_) {
            for (Direction direction : blockelement.faces.keySet()) {
                BlockElementFace blockelementface = blockelement.faces.get(direction);
                TextureAtlasSprite textureatlassprite1 = findSprite(p_375793_, p_378525_, blockelementface.texture());
                if (blockelementface.cullForDirection() == null) {
                    simplebakedmodel$builder.addUnculledFace(bakeFace(blockelement, blockelementface, textureatlassprite1, direction, p_376680_));
                } else {
                    simplebakedmodel$builder.addCulledFace(
                        Direction.rotate(p_376680_.getRotation().getMatrix(), blockelementface.cullForDirection()),
                        bakeFace(blockelement, blockelementface, textureatlassprite1, direction, p_376680_)
                    );
                }
            }
        }

        return simplebakedmodel$builder.build();
    }

    private static BakedQuad bakeFace(
        BlockElement p_377437_, BlockElementFace p_377187_, TextureAtlasSprite p_378098_, Direction p_377774_, ModelState p_376012_
    ) {
        return FaceBakery.bakeQuad(
            p_377437_.from, p_377437_.to, p_377187_, p_378098_, p_377774_, p_376012_, p_377437_.rotation, p_377437_.shade, p_377437_.lightEmission
        );
    }

    private static TextureAtlasSprite findSprite(SpriteGetter p_376857_, TextureSlots p_377315_, String p_377512_) {
        Material material = p_377315_.getMaterial(p_377512_);
        return material != null ? p_376857_.get(material) : p_376857_.reportMissingReference(p_377512_);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_235054_, @Nullable Direction p_235055_, RandomSource p_235056_) {
        return p_235055_ == null ? this.unculledFaces : this.culledFaces.get(p_235055_);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.hasAmbientOcclusion;
    }

    @Override
    public boolean isGui3d() {
        return this.isGui3d;
    }

    @Override
    public boolean usesBlockLight() {
        return this.usesBlockLight;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.particleIcon;
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.transforms;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final ImmutableList.Builder<BakedQuad> unculledFaces = ImmutableList.builder();
        private final EnumMap<Direction, ImmutableList.Builder<BakedQuad>> culledFaces = Maps.newEnumMap(Direction.class);
        private final boolean hasAmbientOcclusion;
        @Nullable
        private TextureAtlasSprite particleIcon;
        private final boolean usesBlockLight;
        private final boolean isGui3d;
        private final ItemTransforms transforms;

        public Builder(boolean p_119521_, boolean p_119522_, boolean p_119523_, ItemTransforms p_119524_) {
            this.hasAmbientOcclusion = p_119521_;
            this.usesBlockLight = p_119522_;
            this.isGui3d = p_119523_;
            this.transforms = p_119524_;

            for (Direction direction : Direction.values()) {
                this.culledFaces.put(direction, ImmutableList.builder());
            }
        }

        public SimpleBakedModel.Builder addCulledFace(Direction p_119531_, BakedQuad p_119532_) {
            this.culledFaces.get(p_119531_).add(p_119532_);
            return this;
        }

        public SimpleBakedModel.Builder addUnculledFace(BakedQuad p_119527_) {
            this.unculledFaces.add(p_119527_);
            return this;
        }

        public SimpleBakedModel.Builder particle(TextureAtlasSprite p_119529_) {
            this.particleIcon = p_119529_;
            return this;
        }

        public SimpleBakedModel.Builder item() {
            return this;
        }

        public BakedModel build() {
            if (this.particleIcon == null) {
                throw new RuntimeException("Missing particle!");
            } else {
                Map<Direction, List<BakedQuad>> map = Maps.transformValues(this.culledFaces, ImmutableList.Builder::build);
                return new SimpleBakedModel(
                    this.unculledFaces.build(), new EnumMap<>(map), this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms
                );
            }
        }
    }
}