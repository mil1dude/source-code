package net.minecraft.world.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.apache.commons.lang3.Validate;

public class BannerItem extends StandingAndWallBlockItem {
    public BannerItem(Block p_40534_, Block p_40535_, Item.Properties p_40536_) {
        super(p_40534_, p_40535_, Direction.DOWN, p_40536_);
        Validate.isInstanceOf(AbstractBannerBlock.class, p_40534_);
        Validate.isInstanceOf(AbstractBannerBlock.class, p_40535_);
    }

    public static void appendHoverTextFromBannerBlockEntityTag(ItemStack p_40543_, List<Component> p_40544_) {
        BannerPatternLayers bannerpatternlayers = p_40543_.get(DataComponents.BANNER_PATTERNS);
        if (bannerpatternlayers != null) {
            for (int i = 0; i < Math.min(bannerpatternlayers.layers().size(), 6); i++) {
                BannerPatternLayers.Layer bannerpatternlayers$layer = bannerpatternlayers.layers().get(i);
                p_40544_.add(bannerpatternlayers$layer.description().withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public DyeColor getColor() {
        return ((AbstractBannerBlock)this.getBlock()).getColor();
    }

    @Override
    public void appendHoverText(ItemStack p_40538_, Item.TooltipContext p_327823_, List<Component> p_40540_, TooltipFlag p_40541_) {
        appendHoverTextFromBannerBlockEntityTag(p_40538_, p_40540_);
    }
}