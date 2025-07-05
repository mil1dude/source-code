package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MossyCarpetBlock extends Block implements BonemealableBlock {
    public static final MapCodec<MossyCarpetBlock> CODEC = simpleCodec(MossyCarpetBlock::new);
    public static final BooleanProperty BASE = BlockStateProperties.BOTTOM;
    private static final EnumProperty<WallSide> NORTH = BlockStateProperties.NORTH_WALL;
    private static final EnumProperty<WallSide> EAST = BlockStateProperties.EAST_WALL;
    private static final EnumProperty<WallSide> SOUTH = BlockStateProperties.SOUTH_WALL;
    private static final EnumProperty<WallSide> WEST = BlockStateProperties.WEST_WALL;
    private static final Map<Direction, EnumProperty<WallSide>> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Util.make(Maps.newEnumMap(Direction.class), p_364794_ -> {
        p_364794_.put(Direction.NORTH, NORTH);
        p_364794_.put(Direction.EAST, EAST);
        p_364794_.put(Direction.SOUTH, SOUTH);
        p_364794_.put(Direction.WEST, WEST);
    }));
    private static final float AABB_OFFSET = 1.0F;
    private static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape WEST_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape EAST_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape NORTH_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SOUTH_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final int SHORT_HEIGHT = 10;
    private static final VoxelShape WEST_SHORT_AABB = Block.box(0.0, 0.0, 0.0, 1.0, 10.0, 16.0);
    private static final VoxelShape EAST_SHORT_AABB = Block.box(15.0, 0.0, 0.0, 16.0, 10.0, 16.0);
    private static final VoxelShape NORTH_SHORT_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 1.0);
    private static final VoxelShape SOUTH_SHORT_AABB = Block.box(0.0, 0.0, 15.0, 16.0, 10.0, 16.0);
    private final Map<BlockState, VoxelShape> shapesCache;

    @Override
    public MapCodec<MossyCarpetBlock> codec() {
        return CODEC;
    }

    public MossyCarpetBlock(BlockBehaviour.Properties p_364771_) {
        super(p_364771_);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(BASE, Boolean.valueOf(true))
                .setValue(NORTH, WallSide.NONE)
                .setValue(EAST, WallSide.NONE)
                .setValue(SOUTH, WallSide.NONE)
                .setValue(WEST, WallSide.NONE)
        );
        this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream().collect(Collectors.toMap(Function.identity(), MossyCarpetBlock::calculateShape)));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState p_365538_) {
        return Shapes.empty();
    }

    private static VoxelShape calculateShape(BlockState p_363638_) {
        VoxelShape voxelshape = Shapes.empty();
        if (p_363638_.getValue(BASE)) {
            voxelshape = DOWN_AABB;
        }
        voxelshape = switch ((WallSide)p_363638_.getValue(NORTH)) {
            case NONE -> voxelshape;
            case LOW -> Shapes.or(voxelshape, NORTH_SHORT_AABB);
            case TALL -> Shapes.or(voxelshape, NORTH_AABB);
        };

        voxelshape = switch ((WallSide)p_363638_.getValue(SOUTH)) {
            case NONE -> voxelshape;
            case LOW -> Shapes.or(voxelshape, SOUTH_SHORT_AABB);
            case TALL -> Shapes.or(voxelshape, SOUTH_AABB);
        };

        voxelshape = switch ((WallSide)p_363638_.getValue(EAST)) {
            case NONE -> voxelshape;
            case LOW -> Shapes.or(voxelshape, EAST_SHORT_AABB);
            case TALL -> Shapes.or(voxelshape, EAST_AABB);
        };

        voxelshape = switch ((WallSide)p_363638_.getValue(WEST)) {
            case NONE -> voxelshape;
            case LOW -> Shapes.or(voxelshape, WEST_SHORT_AABB);
            case TALL -> Shapes.or(voxelshape, WEST_AABB);
        };
        return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
    }

    @Override
    protected VoxelShape getShape(BlockState p_363320_, BlockGetter p_360809_, BlockPos p_366148_, CollisionContext p_364013_) {
        return this.shapesCache.get(p_363320_);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState p_362636_, BlockGetter p_367446_, BlockPos p_361178_, CollisionContext p_362275_) {
        return p_362636_.getValue(BASE) ? DOWN_AABB : Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState p_366765_) {
        return true;
    }

    @Override
    protected boolean canSurvive(BlockState p_367593_, LevelReader p_363307_, BlockPos p_365117_) {
        BlockState blockstate = p_363307_.getBlockState(p_365117_.below());
        return p_367593_.getValue(BASE) ? !blockstate.isAir() : blockstate.is(this) && blockstate.getValue(BASE);
    }

    private static boolean hasFaces(BlockState p_361239_) {
        if (p_361239_.getValue(BASE)) {
            return true;
        } else {
            for (EnumProperty<WallSide> enumproperty : PROPERTY_BY_DIRECTION.values()) {
                if (p_361239_.getValue(enumproperty) != WallSide.NONE) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean canSupportAtFace(BlockGetter p_370010_, BlockPos p_362757_, Direction p_361992_) {
        return p_361992_ == Direction.UP ? false : MultifaceBlock.canAttachTo(p_370010_, p_362757_, p_361992_);
    }

    private static BlockState getUpdatedState(BlockState p_368960_, BlockGetter p_360799_, BlockPos p_361234_, boolean p_368579_) {
        BlockState blockstate = null;
        BlockState blockstate1 = null;
        p_368579_ |= p_368960_.getValue(BASE);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            EnumProperty<WallSide> enumproperty = getPropertyForFace(direction);
            WallSide wallside = canSupportAtFace(p_360799_, p_361234_, direction) ? (p_368579_ ? WallSide.LOW : p_368960_.getValue(enumproperty)) : WallSide.NONE;
            if (wallside == WallSide.LOW) {
                if (blockstate == null) {
                    blockstate = p_360799_.getBlockState(p_361234_.above());
                }

                if (blockstate.is(Blocks.PALE_MOSS_CARPET) && blockstate.getValue(enumproperty) != WallSide.NONE && !blockstate.getValue(BASE)) {
                    wallside = WallSide.TALL;
                }

                if (!p_368960_.getValue(BASE)) {
                    if (blockstate1 == null) {
                        blockstate1 = p_360799_.getBlockState(p_361234_.below());
                    }

                    if (blockstate1.is(Blocks.PALE_MOSS_CARPET) && blockstate1.getValue(enumproperty) == WallSide.NONE) {
                        wallside = WallSide.NONE;
                    }
                }
            }

            p_368960_ = p_368960_.setValue(enumproperty, wallside);
        }

        return p_368960_;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_363369_) {
        return getUpdatedState(this.defaultBlockState(), p_363369_.getLevel(), p_363369_.getClickedPos(), true);
    }

    public static void placeAt(LevelAccessor p_369832_, BlockPos p_369165_, RandomSource p_364489_, int p_362052_) {
        BlockState blockstate = Blocks.PALE_MOSS_CARPET.defaultBlockState();
        BlockState blockstate1 = getUpdatedState(blockstate, p_369832_, p_369165_, true);
        p_369832_.setBlock(p_369165_, blockstate1, 3);
        BlockState blockstate2 = createTopperWithSideChance(p_369832_, p_369165_, p_364489_::nextBoolean);
        if (!blockstate2.isAir()) {
            p_369832_.setBlock(p_369165_.above(), blockstate2, p_362052_);
        }
    }

    @Override
    public void setPlacedBy(Level p_362741_, BlockPos p_360970_, BlockState p_365361_, @Nullable LivingEntity p_369935_, ItemStack p_364687_) {
        if (!p_362741_.isClientSide) {
            RandomSource randomsource = p_362741_.getRandom();
            BlockState blockstate = createTopperWithSideChance(p_362741_, p_360970_, randomsource::nextBoolean);
            if (!blockstate.isAir()) {
                p_362741_.setBlock(p_360970_.above(), blockstate, 3);
            }
        }
    }

    private static BlockState createTopperWithSideChance(BlockGetter p_362586_, BlockPos p_370077_, BooleanSupplier p_367276_) {
        BlockPos blockpos = p_370077_.above();
        BlockState blockstate = p_362586_.getBlockState(blockpos);
        boolean flag = blockstate.is(Blocks.PALE_MOSS_CARPET);
        if ((!flag || !blockstate.getValue(BASE)) && (flag || blockstate.canBeReplaced())) {
            BlockState blockstate1 = Blocks.PALE_MOSS_CARPET.defaultBlockState().setValue(BASE, Boolean.valueOf(false));
            BlockState blockstate2 = getUpdatedState(blockstate1, p_362586_, p_370077_.above(), true);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                EnumProperty<WallSide> enumproperty = getPropertyForFace(direction);
                if (blockstate2.getValue(enumproperty) != WallSide.NONE && !p_367276_.getAsBoolean()) {
                    blockstate2 = blockstate2.setValue(enumproperty, WallSide.NONE);
                }
            }

            return hasFaces(blockstate2) && blockstate2 != blockstate ? blockstate2 : Blocks.AIR.defaultBlockState();
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    @Override
    protected BlockState updateShape(
        BlockState p_367293_,
        LevelReader p_364514_,
        ScheduledTickAccess p_366367_,
        BlockPos p_370081_,
        Direction p_361388_,
        BlockPos p_367050_,
        BlockState p_368028_,
        RandomSource p_366712_
    ) {
        if (!p_367293_.canSurvive(p_364514_, p_370081_)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            BlockState blockstate = getUpdatedState(p_367293_, p_364514_, p_370081_, false);
            return !hasFaces(blockstate) ? Blocks.AIR.defaultBlockState() : blockstate;
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_362311_) {
        p_362311_.add(BASE, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected BlockState rotate(BlockState p_363231_, Rotation p_369895_) {
        return switch (p_369895_) {
            case CLOCKWISE_180 -> (BlockState)p_363231_.setValue(NORTH, p_363231_.getValue(SOUTH))
            .setValue(EAST, p_363231_.getValue(WEST))
            .setValue(SOUTH, p_363231_.getValue(NORTH))
            .setValue(WEST, p_363231_.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> (BlockState)p_363231_.setValue(NORTH, p_363231_.getValue(EAST))
            .setValue(EAST, p_363231_.getValue(SOUTH))
            .setValue(SOUTH, p_363231_.getValue(WEST))
            .setValue(WEST, p_363231_.getValue(NORTH));
            case CLOCKWISE_90 -> (BlockState)p_363231_.setValue(NORTH, p_363231_.getValue(WEST))
            .setValue(EAST, p_363231_.getValue(NORTH))
            .setValue(SOUTH, p_363231_.getValue(EAST))
            .setValue(WEST, p_363231_.getValue(SOUTH));
            default -> p_363231_;
        };
    }

    @Override
    protected BlockState mirror(BlockState p_368204_, Mirror p_366787_) {
        return switch (p_366787_) {
            case LEFT_RIGHT -> (BlockState)p_368204_.setValue(NORTH, p_368204_.getValue(SOUTH)).setValue(SOUTH, p_368204_.getValue(NORTH));
            case FRONT_BACK -> (BlockState)p_368204_.setValue(EAST, p_368204_.getValue(WEST)).setValue(WEST, p_368204_.getValue(EAST));
            default -> super.mirror(p_368204_, p_366787_);
        };
    }

    @Nullable
    public static EnumProperty<WallSide> getPropertyForFace(Direction p_368837_) {
        return PROPERTY_BY_DIRECTION.get(p_368837_);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader p_362652_, BlockPos p_369062_, BlockState p_361167_) {
        return p_361167_.getValue(BASE) && !createTopperWithSideChance(p_362652_, p_369062_, () -> true).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level p_362053_, RandomSource p_363617_, BlockPos p_362482_, BlockState p_365063_) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel p_366160_, RandomSource p_369242_, BlockPos p_362249_, BlockState p_362904_) {
        BlockState blockstate = createTopperWithSideChance(p_366160_, p_362249_, () -> true);
        if (!blockstate.isAir()) {
            p_366160_.setBlock(p_362249_.above(), blockstate, 3);
        }
    }
}