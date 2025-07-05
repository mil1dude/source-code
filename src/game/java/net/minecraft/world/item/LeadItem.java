package net.minecraft.world.item;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public class LeadItem extends Item {
    public LeadItem(Item.Properties p_42828_) {
        super(p_42828_);
    }

    @Override
    public InteractionResult useOn(UseOnContext p_42834_) {
        Level level = p_42834_.getLevel();
        BlockPos blockpos = p_42834_.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.is(BlockTags.FENCES)) {
            Player player = p_42834_.getPlayer();
            if (!level.isClientSide && player != null) {
                return bindPlayerMobs(player, level, blockpos);
            }
        }

        return InteractionResult.PASS;
    }

    public static InteractionResult bindPlayerMobs(Player p_42830_, Level p_42831_, BlockPos p_42832_) {
        LeashFenceKnotEntity leashfenceknotentity = null;
        List<Leashable> list = leashableInArea(p_42831_, p_42832_, p_341570_ -> p_341570_.getLeashHolder() == p_42830_);

        for (Leashable leashable : list) {
            if (leashfenceknotentity == null) {
                leashfenceknotentity = LeashFenceKnotEntity.getOrCreateKnot(p_42831_, p_42832_);
                leashfenceknotentity.playPlacementSound();
            }

            leashable.setLeashedTo(leashfenceknotentity, true);
        }

        if (!list.isEmpty()) {
            p_42831_.gameEvent(GameEvent.BLOCK_ATTACH, p_42832_, GameEvent.Context.of(p_42830_));
            return InteractionResult.SUCCESS_SERVER;
        } else {
            return InteractionResult.PASS;
        }
    }

    public static List<Leashable> leashableInArea(Level p_342470_, BlockPos p_343183_, Predicate<Leashable> p_342293_) {
        double d0 = 7.0;
        int i = p_343183_.getX();
        int j = p_343183_.getY();
        int k = p_343183_.getZ();
        AABB aabb = new AABB((double)i - 7.0, (double)j - 7.0, (double)k - 7.0, (double)i + 7.0, (double)j + 7.0, (double)k + 7.0);
        return p_342470_.getEntitiesOfClass(Entity.class, aabb, p_341568_ -> {
            if (p_341568_ instanceof Leashable leashable && p_342293_.test(leashable)) {
                return true;
            }

            return false;
        }).stream().map(Leashable.class::cast).toList();
    }
}