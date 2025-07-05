package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShieldSpecialRenderer implements SpecialModelRenderer<DataComponentMap> {
    private final ShieldModel model;

    public ShieldSpecialRenderer(ShieldModel p_375550_) {
        this.model = p_375550_;
    }

    @Nullable
    public DataComponentMap extractArgument(ItemStack p_376303_) {
        return p_376303_.immutableComponents();
    }

    public void render(
        @Nullable DataComponentMap p_378203_,
        ItemDisplayContext p_376388_,
        PoseStack p_378600_,
        MultiBufferSource p_378168_,
        int p_376859_,
        int p_377055_,
        boolean p_378267_
    ) {
        BannerPatternLayers bannerpatternlayers = p_378203_ != null
            ? p_378203_.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
            : BannerPatternLayers.EMPTY;
        DyeColor dyecolor = p_378203_ != null ? p_378203_.get(DataComponents.BASE_COLOR) : null;
        boolean flag = !bannerpatternlayers.layers().isEmpty() || dyecolor != null;
        p_378600_.pushPose();
        p_378600_.scale(1.0F, -1.0F, -1.0F);
        Material material = flag ? ModelBakery.SHIELD_BASE : ModelBakery.NO_PATTERN_SHIELD;
        VertexConsumer vertexconsumer = material.sprite()
            .wrap(ItemRenderer.getFoilBuffer(p_378168_, this.model.renderType(material.atlasLocation()), p_376388_ == ItemDisplayContext.GUI, p_378267_));
        this.model.handle().render(p_378600_, vertexconsumer, p_376859_, p_377055_);
        if (flag) {
            BannerRenderer.renderPatterns(
                p_378600_,
                p_378168_,
                p_376859_,
                p_377055_,
                this.model.plate(),
                material,
                false,
                Objects.requireNonNullElse(dyecolor, DyeColor.WHITE),
                bannerpatternlayers,
                p_378267_,
                false
            );
        } else {
            this.model.plate().render(p_378600_, vertexconsumer, p_376859_, p_377055_);
        }

        p_378600_.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final ShieldSpecialRenderer.Unbaked INSTANCE = new ShieldSpecialRenderer.Unbaked();
        public static final MapCodec<ShieldSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public MapCodec<ShieldSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_378175_) {
            return new ShieldSpecialRenderer(new ShieldModel(p_378175_.bakeLayer(ModelLayers.SHIELD)));
        }
    }
}