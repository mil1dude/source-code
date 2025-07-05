package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DecoratedPotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<DecoratedPotBlock> CODEC = simpleCodec(DecoratedPotBlock::new);
    public static final ResourceLocation SHERDS_DYNAMIC_DROP_ID = ResourceLocation.withDefaultNamespace("sherds");
    private static final VoxelShape BOUNDING_BOX = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
    private static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty CRACKED = BlockStateProperties.CRACKED;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    @Override
    public MapCodec<DecoratedPotBlock> codec() {
        return CODEC;
    }

    protected DecoratedPotBlock(BlockBehaviour.Properties p_273064_) {
        super(p_273064_);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.valueOf(false))
                .setValue(CRACKED, Boolean.valueOf(false))
        );
    }

    @Override
    protected BlockState updateShape(
        BlockState p_276307_,
        LevelReader p_363776_,
        ScheduledTickAccess p_364929_,
        BlockPos p_276270_,
        Direction p_276322_,
        BlockPos p_276312_,
        BlockState p_276280_,
        RandomSource p_361966_
    ) {
        if (p_276307_.getValue(WATERLOGGED)) {
            p_364929_.scheduleTick(p_276270_, Fluids.WATER, Fluids.WATER.getTickDelay(p_363776_));
        }

        return super.updateShape(p_276307_, p_363776_, p_364929_, p_276270_, p_276322_, p_276312_, p_276280_, p_361966_);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_272711_) {
        FluidState fluidstate = p_272711_.getLevel().getFluidState(p_272711_.getClickedPos());
        return this.defaultBlockState()
            .setValue(HORIZONTAL_FACING, p_272711_.getHorizontalDirection())
            .setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER))
            .setValue(CRACKED, Boolean.valueOf(false));
    }

    @Override
    protected InteractionResult useItemOn(
        ItemStack p_335411_, BlockState p_334873_, Level p_328717_, BlockPos p_332886_, Player p_331165_, InteractionHand p_330433_, BlockHitResult p_330105_
    ) {
        if (p_328717_.getBlockEntity(p_332886_) instanceof DecoratedPotBlockEntity decoratedpotblockentity) {
            if (p_328717_.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                ItemStack itemstack1 = decoratedpotblockentity.getTheItem();
                if (!p_335411_.isEmpty()
                    && (itemstack1.isEmpty() || ItemStack.isSameItemSameComponents(itemstack1, p_335411_) && itemstack1.getCount() < itemstack1.getMaxStackSize())) {
                    decoratedpotblockentity.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
                    p_331165_.awardStat(Stats.ITEM_USED.get(p_335411_.getItem()));
                    ItemStack itemstack = p_335411_.consumeAndReturn(1, p_331165_);
                    float f;
                    if (decoratedpotblockentity.isEmpty()) {
                        decoratedpotblockentity.setTheItem(itemstack);
                        f = (float)itemstack.getCount() / (float)itemstack.getMaxStackSize();
                    } else {
                        itemstack1.grow(1);
                        f = (float)itemstack1.getCount() / (float)itemstack1.getMaxStackSize();
                    }

                    p_328717_.playSound(null, p_332886_, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * f);
                    if (p_328717_ instanceof ServerLevel serverlevel) {
                        serverlevel.sendParticles(
                            ParticleTypes.DUST_PLUME,
                            (double)p_332886_.getX() + 0.5,
                            (double)p_332886_.getY() + 1.2,
                            (double)p_332886_.getZ() + 0.5,
                            7,
                            0.0,
                            0.0,
                            0.0,
                            0.0
                        );
                    }

                    decoratedpotblockentity.setChanged();
                    p_328717_.gameEvent(p_331165_, GameEvent.BLOCK_CHANGE, p_332886_);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.TRY_WITH_EMPTY_HAND;
                }
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_329061_, Level p_331143_, BlockPos p_332658_, Player p_330362_, BlockHitResult p_330700_) {
        if (p_331143_.getBlockEntity(p_332658_) instanceof DecoratedPotBlockEntity decoratedpotblockentity) {
            p_331143_.playSound(null, p_332658_, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
            decoratedpotblockentity.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
            p_331143_.gameEvent(p_330362_, GameEvent.BLOCK_CHANGE, p_332658_);
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    protected boolean isPathfindable(BlockState p_276295_, PathComputationType p_276303_) {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState p_273112_, BlockGetter p_273055_, BlockPos p_273137_, CollisionContext p_273151_) {
        return BOUNDING_BOX;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_273169_) {
        p_273169_.add(HORIZONTAL_FACING, WATERLOGGED, CRACKED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_273396_, BlockState p_272674_) {
        return new DecoratedPotBlockEntity(p_273396_, p_272674_);
    }

    @Override
    protected void onRemove(BlockState p_312694_, Level p_313251_, BlockPos p_312873_, BlockState p_312133_, boolean p_311809_) {
        Containers.dropContentsOnDestroy(p_312694_, p_312133_, p_313251_, p_312873_);
        super.onRemove(p_312694_, p_313251_, p_312873_, p_312133_, p_311809_);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState p_287683_, LootParams.Builder p_287582_) {
        BlockEntity blockentity = p_287582_.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockentity instanceof DecoratedPotBlockEntity decoratedpotblockentity) {
            p_287582_.withDynamicDrop(SHERDS_DYNAMIC_DROP_ID, p_327259_ -> {
                for (Item item : decoratedpotblockentity.getDecorations().ordered()) {
                    p_327259_.accept(item.getDefaultInstance());
                }
            });
        }

        return super.getDrops(p_287683_, p_287582_);
    }

    @Override
    public BlockState playerWillDestroy(Level p_273590_, BlockPos p_273343_, BlockState p_272869_, Player p_273002_) {
        ItemStack itemstack = p_273002_.getMainHandItem();
        BlockState blockstate = p_272869_;
        if (itemstack.is(ItemTags.BREAKS_DECORATED_POTS) && !EnchantmentHelper.hasTag(itemstack, EnchantmentTags.PREVENTS_DECORATED_POT_SHATTERING)) {
            blockstate = p_272869_.setValue(CRACKED, Boolean.valueOf(true));
            p_273590_.setBlock(p_273343_, blockstate, 4);
        }

        return super.playerWillDestroy(p_273590_, p_273343_, blockstate, p_273002_);
    }

    @Override
    protected FluidState getFluidState(BlockState p_272593_) {
        return p_272593_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_272593_);
    }

    @Override
    protected SoundType getSoundType(BlockState p_277561_) {
        return p_277561_.getValue(CRACKED) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
    }

    @Override
    public void appendHoverText(ItemStack p_285238_, Item.TooltipContext p_331405_, List<Component> p_285448_, TooltipFlag p_284997_) {
        super.appendHoverText(p_285238_, p_331405_, p_285448_, p_284997_);
        PotDecorations potdecorations = p_285238_.getOrDefault(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY);
        if (!potdecorations.equals(PotDecorations.EMPTY)) {
            p_285448_.add(CommonComponents.EMPTY);
            Stream.of(potdecorations.front(), potdecorations.left(), potdecorations.right(), potdecorations.back())
                .forEach(p_327261_ -> p_285448_.add(new ItemStack(p_327261_.orElse(Items.BRICK), 1).getHoverName().plainCopy().withStyle(ChatFormatting.GRAY)));
        }
    }

    @Override
    protected void onProjectileHit(Level p_310477_, BlockState p_309479_, BlockHitResult p_309542_, Projectile p_309867_) {
        BlockPos blockpos = p_309542_.getBlockPos();
        if (p_310477_ instanceof ServerLevel serverlevel && p_309867_.mayInteract(serverlevel, blockpos) && p_309867_.mayBreak(serverlevel)) {
            p_310477_.setBlock(blockpos, p_309479_.setValue(CRACKED, Boolean.valueOf(true)), 4);
            p_310477_.destroyBlock(blockpos, true, p_309867_);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader p_312375_, BlockPos p_300759_, BlockState p_297348_, boolean p_378578_) {
        if (p_312375_.getBlockEntity(p_300759_) instanceof DecoratedPotBlockEntity decoratedpotblockentity) {
            PotDecorations potdecorations = decoratedpotblockentity.getDecorations();
            return DecoratedPotBlockEntity.createDecoratedPotItem(potdecorations);
        } else {
            return super.getCloneItemStack(p_312375_, p_300759_, p_297348_, p_378578_);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState p_310567_) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState p_310830_, Level p_312569_, BlockPos p_309943_) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(p_312569_.getBlockEntity(p_309943_));
    }

    @Override
    protected BlockState rotate(BlockState p_335606_, Rotation p_331991_) {
        return p_335606_.setValue(HORIZONTAL_FACING, p_331991_.rotate(p_335606_.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected BlockState mirror(BlockState p_332589_, Mirror p_332235_) {
        return p_332589_.rotate(p_332235_.getRotation(p_332589_.getValue(HORIZONTAL_FACING)));
    }
}