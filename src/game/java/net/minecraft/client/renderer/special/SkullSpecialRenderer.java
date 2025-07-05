package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkullSpecialRenderer implements SpecialModelRenderer<ResolvableProfile> {
    private final SkullBlock.Type skullType;
    private final SkullModelBase model;
    @Nullable
    private final ResourceLocation textureOverride;
    private final float animation;

    public SkullSpecialRenderer(SkullBlock.Type p_376879_, SkullModelBase p_375443_, @Nullable ResourceLocation p_378154_, float p_377202_) {
        this.skullType = p_376879_;
        this.model = p_375443_;
        this.textureOverride = p_378154_;
        this.animation = p_377202_;
    }

    @Nullable
    public ResolvableProfile extractArgument(ItemStack p_376567_) {
        return p_376567_.get(DataComponents.PROFILE);
    }

    public void render(
        @Nullable ResolvableProfile p_377678_,
        ItemDisplayContext p_378440_,
        PoseStack p_377644_,
        MultiBufferSource p_375574_,
        int p_376639_,
        int p_376976_,
        boolean p_378372_
    ) {
        RenderType rendertype = SkullBlockRenderer.getRenderType(this.skullType, p_377678_, this.textureOverride);
        SkullBlockRenderer.renderSkull(null, 180.0F, this.animation, p_377644_, p_375574_, p_376639_, this.model, rendertype);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(SkullBlock.Type kind, Optional<ResourceLocation> textureOverride, float animation) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<SkullSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_375918_ -> p_375918_.group(
                        SkullBlock.Type.CODEC.fieldOf("kind").forGetter(SkullSpecialRenderer.Unbaked::kind),
                        ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(SkullSpecialRenderer.Unbaked::textureOverride),
                        Codec.FLOAT.optionalFieldOf("animation", Float.valueOf(0.0F)).forGetter(SkullSpecialRenderer.Unbaked::animation)
                    )
                    .apply(p_375918_, SkullSpecialRenderer.Unbaked::new)
        );

        public Unbaked(SkullBlock.Type p_376549_) {
            this(p_376549_, Optional.empty(), 0.0F);
        }

        @Override
        public MapCodec<SkullSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Nullable
        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_376016_) {
            SkullModelBase skullmodelbase = SkullBlockRenderer.createModel(p_376016_, this.kind);
            ResourceLocation resourcelocation = this.textureOverride
                .<ResourceLocation>map(p_377495_ -> p_377495_.withPath(p_377715_ -> "textures/entity/" + p_377715_ + ".png"))
                .orElse(null);
            return skullmodelbase != null ? new SkullSpecialRenderer(this.kind, skullmodelbase, resourcelocation, this.animation) : null;
        }
    }
}