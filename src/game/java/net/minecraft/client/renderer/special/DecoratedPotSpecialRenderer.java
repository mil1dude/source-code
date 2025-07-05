package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DecoratedPotSpecialRenderer implements SpecialModelRenderer<PotDecorations> {
    private final DecoratedPotRenderer decoratedPotRenderer;

    public DecoratedPotSpecialRenderer(DecoratedPotRenderer p_377806_) {
        this.decoratedPotRenderer = p_377806_;
    }

    @Nullable
    public PotDecorations extractArgument(ItemStack p_375578_) {
        return p_375578_.get(DataComponents.POT_DECORATIONS);
    }

    public void render(
        @Nullable PotDecorations p_376304_,
        ItemDisplayContext p_377045_,
        PoseStack p_377344_,
        MultiBufferSource p_378083_,
        int p_376250_,
        int p_375831_,
        boolean p_376813_
    ) {
        this.decoratedPotRenderer.renderInHand(p_377344_, p_378083_, p_376250_, p_375831_, Objects.requireNonNullElse(p_376304_, PotDecorations.EMPTY));
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<DecoratedPotSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new DecoratedPotSpecialRenderer.Unbaked());

        @Override
        public MapCodec<DecoratedPotSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_376128_) {
            return new DecoratedPotSpecialRenderer(new DecoratedPotRenderer(p_376128_));
        }
    }
}