package net.minecraft.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class BannerPatternItem extends Item {
    private final TagKey<BannerPattern> bannerPattern;

    public BannerPatternItem(TagKey<BannerPattern> p_220008_, Item.Properties p_220009_) {
        super(p_220009_);
        this.bannerPattern = p_220008_;
    }

    public TagKey<BannerPattern> getBannerPattern() {
        return this.bannerPattern;
    }
}