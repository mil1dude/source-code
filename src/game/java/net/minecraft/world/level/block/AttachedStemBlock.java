package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AttachedStemBlock extends BushBlock {
    public static final MapCodec<AttachedStemBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_359964_ -> p_359964_.group(
                    ResourceKey.codec(Registries.BLOCK).fieldOf("fruit").forGetter(p_309932_ -> p_309932_.fruit),
                    ResourceKey.codec(Registries.BLOCK).fieldOf("stem").forGetter(p_312475_ -> p_312475_.stem),
                    ResourceKey.codec(Registries.ITEM).fieldOf("seed").forGetter(p_312517_ -> p_312517_.seed),
                    propertiesCodec()
                )
                .apply(p_359964_, AttachedStemBlock::new)
    );
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    protected static final float AABB_OFFSET = 2.0F;
    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
        ImmutableMap.of(
            Direction.SOUTH,
            Block.box(6.0, 0.0, 6.0, 10.0, 10.0, 16.0),
            Direction.WEST,
            Block.box(0.0, 0.0, 6.0, 10.0, 10.0, 10.0),
            Direction.NORTH,
            Block.box(6.0, 0.0, 0.0, 10.0, 10.0, 10.0),
            Direction.EAST,
            Block.box(6.0, 0.0, 6.0, 16.0, 10.0, 10.0)
        )
    );
    private final ResourceKey<Block> fruit;
    private final ResourceKey<Block> stem;
    private final ResourceKey<Item> seed;

    @Override
    public MapCodec<AttachedStemBlock> codec() {
        return CODEC;
    }

    protected AttachedStemBlock(ResourceKey<Block> p_309773_, ResourceKey<Block> p_312687_, ResourceKey<Item> p_310792_, BlockBehaviour.Properties p_152062_) {
        super(p_152062_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.stem = p_309773_;
        this.fruit = p_312687_;
        this.seed = p_310792_;
    }

    @Override
    protected VoxelShape getShape(BlockState p_48858_, BlockGetter p_48859_, BlockPos p_48860_, CollisionContext p_48861_) {
        return AABBS.get(p_48858_.getValue(FACING));
    }

    @Override
    protected BlockState updateShape(
        BlockState p_48848_,
        LevelReader p_367502_,
        ScheduledTickAccess p_364814_,
        BlockPos p_48852_,
        Direction p_48849_,
        BlockPos p_48853_,
        BlockState p_48850_,
        RandomSource p_368116_
    ) {
        if (!p_48850_.is(this.fruit) && p_48849_ == p_48848_.getValue(FACING)) {
            Optional<Block> optional = p_367502_.registryAccess().lookupOrThrow(Registries.BLOCK).getOptional(this.stem);
            if (optional.isPresent()) {
                return optional.get().defaultBlockState().trySetValue(StemBlock.AGE, Integer.valueOf(7));
            }
        }

        return super.updateShape(p_48848_, p_367502_, p_364814_, p_48852_, p_48849_, p_48853_, p_48850_, p_368116_);
    }

    @Override
    protected boolean mayPlaceOn(BlockState p_48863_, BlockGetter p_48864_, BlockPos p_48865_) {
        return p_48863_.is(Blocks.FARMLAND);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader p_313034_, BlockPos p_48839_, BlockState p_48840_, boolean p_376802_) {
        return new ItemStack(DataFixUtils.orElse(p_313034_.registryAccess().lookupOrThrow(Registries.ITEM).getOptional(this.seed), this));
    }

    @Override
    protected BlockState rotate(BlockState p_48845_, Rotation p_48846_) {
        return p_48845_.setValue(FACING, p_48846_.rotate(p_48845_.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState p_48842_, Mirror p_48843_) {
        return p_48842_.rotate(p_48843_.getRotation(p_48842_.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_48855_) {
        p_48855_.add(FACING);
    }
}