package net.minecraft.world.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class HoneycombItem extends Item implements SignApplicator {
    public static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(
        () -> ImmutableBiMap.<Block, Block>builder()
                .put(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK)
                .put(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER)
                .put(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER)
                .put(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER)
                .put(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
                .put(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
                .put(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
                .put(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
                .put(Blocks.CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB)
                .put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB)
                .put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB)
                .put(Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB)
                .put(Blocks.CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER_STAIRS)
                .put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS)
                .put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS)
                .put(Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
                .put(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)
                .put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)
                .put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)
                .put(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)
                .put(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR)
                .put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR)
                .put(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR)
                .put(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR)
                .put(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR)
                .put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR)
                .put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR)
                .put(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR)
                .put(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE)
                .put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE)
                .put(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE)
                .put(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE)
                .put(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB)
                .put(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB)
                .put(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB)
                .put(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB)
                .build()
    );
    public static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());

    public HoneycombItem(Item.Properties p_150867_) {
        super(p_150867_);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_150869_) {
        Level level = p_150869_.getLevel();
        BlockPos blockpos = p_150869_.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        return getWaxed(blockstate).<InteractionResult>map(p_359407_ -> {
            Player player = p_150869_.getPlayer();
            ItemStack itemstack = p_150869_.getItemInHand();
            if (player instanceof ServerPlayer serverplayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverplayer, blockpos, itemstack);
            }

            itemstack.shrink(1);
            level.setBlock(blockpos, p_359407_, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, p_359407_));
            level.levelEvent(player, 3003, blockpos, 0);
            return InteractionResult.SUCCESS;
        }).orElse(InteractionResult.PASS);
    }

    public static Optional<BlockState> getWaxed(BlockState p_150879_) {
        return Optional.ofNullable(WAXABLES.get().get(p_150879_.getBlock())).map(p_150877_ -> p_150877_.withPropertiesOf(p_150879_));
    }

    @Override
    public boolean tryApplyToSign(Level p_277838_, SignBlockEntity p_277988_, boolean p_277394_, Player p_277816_) {
        if (p_277988_.setWaxed(true)) {
            p_277838_.levelEvent(null, 3003, p_277988_.getBlockPos(), 0);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean canApplyToSign(SignText p_277550_, Player p_277640_) {
        return true;
    }
}
