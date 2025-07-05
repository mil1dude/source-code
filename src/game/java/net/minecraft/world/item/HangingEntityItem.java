package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HangingEntityItem extends Item {
    private static final Component TOOLTIP_RANDOM_VARIANT = Component.translatable("painting.random").withStyle(ChatFormatting.GRAY);
    private final EntityType<? extends HangingEntity> type;

    public HangingEntityItem(EntityType<? extends HangingEntity> p_41324_, Item.Properties p_41325_) {
        super(p_41325_);
        this.type = p_41324_;
    }

    @Override
    public InteractionResult useOn(UseOnContext p_41331_) {
        BlockPos blockpos = p_41331_.getClickedPos();
        Direction direction = p_41331_.getClickedFace();
        BlockPos blockpos1 = blockpos.relative(direction);
        Player player = p_41331_.getPlayer();
        ItemStack itemstack = p_41331_.getItemInHand();
        if (player != null && !this.mayPlace(player, direction, itemstack, blockpos1)) {
            return InteractionResult.FAIL;
        } else {
            Level level = p_41331_.getLevel();
            HangingEntity hangingentity;
            if (this.type == EntityType.PAINTING) {
                Optional<Painting> optional = Painting.create(level, blockpos1, direction);
                if (optional.isEmpty()) {
                    return InteractionResult.CONSUME;
                }

                hangingentity = optional.get();
            } else if (this.type == EntityType.ITEM_FRAME) {
                hangingentity = new ItemFrame(level, blockpos1, direction);
            } else {
                if (this.type != EntityType.GLOW_ITEM_FRAME) {
                    return InteractionResult.SUCCESS;
                }

                hangingentity = new GlowItemFrame(level, blockpos1, direction);
            }

            CustomData customdata = itemstack.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
            if (!customdata.isEmpty()) {
                EntityType.updateCustomEntityTag(level, player, hangingentity, customdata);
            }

            if (hangingentity.survives()) {
                if (!level.isClientSide) {
                    hangingentity.playPlacementSound();
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, hangingentity.position());
                    level.addFreshEntity(hangingentity);
                }

                itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    protected boolean mayPlace(Player p_41326_, Direction p_41327_, ItemStack p_41328_, BlockPos p_41329_) {
        return !p_41327_.getAxis().isVertical() && p_41326_.mayUseItemAt(p_41329_, p_41327_, p_41328_);
    }

    @Override
    public void appendHoverText(ItemStack p_270235_, Item.TooltipContext p_336046_, List<Component> p_270630_, TooltipFlag p_270170_) {
        super.appendHoverText(p_270235_, p_336046_, p_270630_, p_270170_);
        HolderLookup.Provider holderlookup$provider = p_336046_.registries();
        if (holderlookup$provider != null && this.type == EntityType.PAINTING) {
            CustomData customdata = p_270235_.getOrDefault(DataComponents.ENTITY_DATA, CustomData.EMPTY);
            if (!customdata.isEmpty()) {
                customdata.read(holderlookup$provider.createSerializationContext(NbtOps.INSTANCE), Painting.VARIANT_MAP_CODEC).result().ifPresentOrElse(p_359403_ -> {
                    p_359403_.value().title().ifPresent(p_270630_::add);
                    p_359403_.value().author().ifPresent(p_270630_::add);
                    p_270630_.add(Component.translatable("painting.dimensions", p_359403_.value().width(), p_359403_.value().height()));
                }, () -> p_270630_.add(TOOLTIP_RANDOM_VARIANT));
            } else if (p_270170_.isCreative()) {
                p_270630_.add(TOOLTIP_RANDOM_VARIANT);
            }
        }
    }
}