package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BrewingStandBlock extends BaseEntityBlock {
    public static final MapCodec<BrewingStandBlock> CODEC = simpleCodec(BrewingStandBlock::new);
    public static final BooleanProperty[] HAS_BOTTLE = new BooleanProperty[]{
        BlockStateProperties.HAS_BOTTLE_0, BlockStateProperties.HAS_BOTTLE_1, BlockStateProperties.HAS_BOTTLE_2
    };
    protected static final VoxelShape SHAPE = Shapes.or(Block.box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0), Block.box(7.0, 0.0, 7.0, 9.0, 14.0, 9.0));

    @Override
    public MapCodec<BrewingStandBlock> codec() {
        return CODEC;
    }

    public BrewingStandBlock(BlockBehaviour.Properties p_50909_) {
        super(p_50909_);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(HAS_BOTTLE[0], Boolean.valueOf(false))
                .setValue(HAS_BOTTLE[1], Boolean.valueOf(false))
                .setValue(HAS_BOTTLE[2], Boolean.valueOf(false))
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_152698_, BlockState p_152699_) {
        return new BrewingStandBlockEntity(p_152698_, p_152699_);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152694_, BlockState p_152695_, BlockEntityType<T> p_152696_) {
        return p_152694_.isClientSide ? null : createTickerHelper(p_152696_, BlockEntityType.BREWING_STAND, BrewingStandBlockEntity::serverTick);
    }

    @Override
    protected VoxelShape getShape(BlockState p_50952_, BlockGetter p_50953_, BlockPos p_50954_, CollisionContext p_50955_) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_50930_, Level p_50931_, BlockPos p_50932_, Player p_50933_, BlockHitResult p_50935_) {
        if (!p_50931_.isClientSide && p_50931_.getBlockEntity(p_50932_) instanceof BrewingStandBlockEntity brewingstandblockentity) {
            p_50933_.openMenu(brewingstandblockentity);
            p_50933_.awardStat(Stats.INTERACT_WITH_BREWINGSTAND);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState p_220883_, Level p_220884_, BlockPos p_220885_, RandomSource p_220886_) {
        double d0 = (double)p_220885_.getX() + 0.4 + (double)p_220886_.nextFloat() * 0.2;
        double d1 = (double)p_220885_.getY() + 0.7 + (double)p_220886_.nextFloat() * 0.3;
        double d2 = (double)p_220885_.getZ() + 0.4 + (double)p_220886_.nextFloat() * 0.2;
        p_220884_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
    }

    @Override
    protected void onRemove(BlockState p_50937_, Level p_50938_, BlockPos p_50939_, BlockState p_50940_, boolean p_50941_) {
        Containers.dropContentsOnDestroy(p_50937_, p_50940_, p_50938_, p_50939_);
        super.onRemove(p_50937_, p_50938_, p_50939_, p_50940_, p_50941_);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_50919_) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState p_50926_, Level p_50927_, BlockPos p_50928_) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(p_50927_.getBlockEntity(p_50928_));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_50948_) {
        p_50948_.add(HAS_BOTTLE[0], HAS_BOTTLE[1], HAS_BOTTLE[2]);
    }

    @Override
    protected boolean isPathfindable(BlockState p_50921_, PathComputationType p_50924_) {
        return false;
    }
}