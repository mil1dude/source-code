package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BannerSpecialRenderer implements SpecialModelRenderer<BannerPatternLayers> {
    private final BannerRenderer bannerRenderer;
    private final DyeColor baseColor;

    public BannerSpecialRenderer(DyeColor p_377632_, BannerRenderer p_376830_) {
        this.bannerRenderer = p_376830_;
        this.baseColor = p_377632_;
    }

    @Nullable
    public BannerPatternLayers extractArgument(ItemStack p_376998_) {
        return p_376998_.get(DataComponents.BANNER_PATTERNS);
    }

    public void render(
        @Nullable BannerPatternLayers p_376355_,
        ItemDisplayContext p_375970_,
        PoseStack p_377190_,
        MultiBufferSource p_376107_,
        int p_377382_,
        int p_376518_,
        boolean p_378431_
    ) {
        this.bannerRenderer
            .renderInHand(p_377190_, p_376107_, p_377382_, p_376518_, this.baseColor, Objects.requireNonNullElse(p_376355_, BannerPatternLayers.EMPTY));
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(DyeColor baseColor) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<BannerSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_376470_ -> p_376470_.group(DyeColor.CODEC.fieldOf("color").forGetter(BannerSpecialRenderer.Unbaked::baseColor))
                    .apply(p_376470_, BannerSpecialRenderer.Unbaked::new)
        );

        @Override
        public MapCodec<BannerSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_377329_) {
            return new BannerSpecialRenderer(this.baseColor, new BannerRenderer(p_377329_));
        }
    }
}