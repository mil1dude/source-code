package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StandingSignSpecialRenderer implements NoDataSpecialModelRenderer {
    private final Model model;
    private final Material material;

    public StandingSignSpecialRenderer(Model p_378013_, Material p_378123_) {
        this.model = p_378013_;
        this.material = p_378123_;
    }

    @Override
    public void render(ItemDisplayContext p_377537_, PoseStack p_376209_, MultiBufferSource p_377121_, int p_377114_, int p_376317_, boolean p_378048_) {
        SignRenderer.renderInHand(p_376209_, p_377121_, p_377114_, p_376317_, this.model, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(WoodType woodType, Optional<ResourceLocation> texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<StandingSignSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_376782_ -> p_376782_.group(
                        WoodType.CODEC.fieldOf("wood_type").forGetter(StandingSignSpecialRenderer.Unbaked::woodType),
                        ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(StandingSignSpecialRenderer.Unbaked::texture)
                    )
                    .apply(p_376782_, StandingSignSpecialRenderer.Unbaked::new)
        );

        public Unbaked(WoodType p_376460_) {
            this(p_376460_, Optional.empty());
        }

        @Override
        public MapCodec<StandingSignSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_376959_) {
            Model model = SignRenderer.createSignModel(p_376959_, this.woodType, true);
            Material material = this.texture.map(Sheets::createSignMaterial).orElseGet(() -> Sheets.getSignMaterial(this.woodType));
            return new StandingSignSpecialRenderer(model, material);
        }
    }
}