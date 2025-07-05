package net.minecraft.world.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public class WrittenBookItem extends Item {
    public WrittenBookItem(Item.Properties p_43455_) {
        super(p_43455_);
    }

    @Override
    public void appendHoverText(ItemStack p_43457_, Item.TooltipContext p_328911_, List<Component> p_43459_, TooltipFlag p_43460_) {
        WrittenBookContent writtenbookcontent = p_43457_.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writtenbookcontent != null) {
            if (!StringUtil.isBlank(writtenbookcontent.author())) {
                p_43459_.add(Component.translatable("book.byAuthor", writtenbookcontent.author()).withStyle(ChatFormatting.GRAY));
            }

            p_43459_.add(Component.translatable("book.generation." + writtenbookcontent.generation()).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public InteractionResult use(Level p_43468_, Player p_43469_, InteractionHand p_43470_) {
        ItemStack itemstack = p_43469_.getItemInHand(p_43470_);
        p_43469_.openItemGui(itemstack, p_43470_);
        p_43469_.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }

    public static boolean resolveBookComponents(ItemStack p_43462_, CommandSourceStack p_43463_, @Nullable Player p_43464_) {
        WrittenBookContent writtenbookcontent = p_43462_.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writtenbookcontent != null && !writtenbookcontent.resolved()) {
            WrittenBookContent writtenbookcontent1 = writtenbookcontent.resolve(p_43463_, p_43464_);
            if (writtenbookcontent1 != null) {
                p_43462_.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenbookcontent1);
                return true;
            }

            p_43462_.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenbookcontent.markResolved());
        }

        return false;
    }
}