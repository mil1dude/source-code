package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class FeaturePoolElement extends StructurePoolElement {
    public static final MapCodec<FeaturePoolElement> CODEC = RecordCodecBuilder.mapCodec(
        p_341912_ -> p_341912_.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(p_210215_ -> p_210215_.feature), projectionCodec())
                .apply(p_341912_, FeaturePoolElement::new)
    );
    private final Holder<PlacedFeature> feature;
    private final CompoundTag defaultJigsawNBT;

    protected FeaturePoolElement(Holder<PlacedFeature> p_210209_, StructureTemplatePool.Projection p_210210_) {
        super(p_210210_);
        this.feature = p_210209_;
        this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
    }

    private CompoundTag fillDefaultJigsawNBT() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putString("name", "minecraft:bottom");
        compoundtag.putString("final_state", "minecraft:air");
        compoundtag.putString("pool", "minecraft:empty");
        compoundtag.putString("target", "minecraft:empty");
        compoundtag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
        return compoundtag;
    }

    @Override
    public Vec3i getSize(StructureTemplateManager p_227192_, Rotation p_227193_) {
        return Vec3i.ZERO;
    }

    @Override
    public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager p_227199_, BlockPos p_227200_, Rotation p_227201_, RandomSource p_227202_) {
        return List.of(
            StructureTemplate.JigsawBlockInfo.of(
                new StructureTemplate.StructureBlockInfo(
                    p_227200_,
                    Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)),
                    this.defaultJigsawNBT
                )
            )
        );
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager p_227195_, BlockPos p_227196_, Rotation p_227197_) {
        Vec3i vec3i = this.getSize(p_227195_, p_227197_);
        return new BoundingBox(
            p_227196_.getX(),
            p_227196_.getY(),
            p_227196_.getZ(),
            p_227196_.getX() + vec3i.getX(),
            p_227196_.getY() + vec3i.getY(),
            p_227196_.getZ() + vec3i.getZ()
        );
    }

    @Override
    public boolean place(
        StructureTemplateManager p_227181_,
        WorldGenLevel p_227182_,
        StructureManager p_227183_,
        ChunkGenerator p_227184_,
        BlockPos p_227185_,
        BlockPos p_227186_,
        Rotation p_227187_,
        BoundingBox p_227188_,
        RandomSource p_227189_,
        LiquidSettings p_344422_,
        boolean p_227190_
    ) {
        return this.feature.value().place(p_227182_, p_227184_, p_227189_, p_227185_);
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.FEATURE;
    }

    @Override
    public String toString() {
        return "Feature[" + this.feature + "]";
    }
}