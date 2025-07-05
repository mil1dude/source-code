package net.minecraft.data.loot.packs;

import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public interface LootData {
    Map<DyeColor, ItemLike> WOOL_ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), p_365767_ -> {
        p_365767_.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
        p_365767_.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
        p_365767_.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
        p_365767_.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
        p_365767_.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
        p_365767_.put(DyeColor.LIME, Blocks.LIME_WOOL);
        p_365767_.put(DyeColor.PINK, Blocks.PINK_WOOL);
        p_365767_.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
        p_365767_.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
        p_365767_.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
        p_365767_.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
        p_365767_.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
        p_365767_.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
        p_365767_.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
        p_365767_.put(DyeColor.RED, Blocks.RED_WOOL);
        p_365767_.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
    });
}