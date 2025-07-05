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
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HangingSignSpecialRenderer implements NoDataSpecialModelRenderer {
    private final Model model;
    private final Material material;

    public HangingSignSpecialRenderer(Model p_376811_, Material p_378581_) {
        this.model = p_376811_;
        this.material = p_378581_;
    }

    @Override
    public void render(ItemDisplayContext p_378287_, PoseStack p_377144_, MultiBufferSource p_378079_, int p_377252_, int p_377239_, boolean p_378304_) {
        HangingSignRenderer.renderInHand(p_377144_, p_378079_, p_377252_, p_377239_, this.model, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(WoodType woodType, Optional<ResourceLocation> texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<HangingSignSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_375993_ -> p_375993_.group(
                        WoodType.CODEC.fieldOf("wood_type").forGetter(HangingSignSpecialRenderer.Unbaked::woodType),
                        ResourceLocation.CODEC.optionalFieldOf("texture").forGetter(HangingSignSpecialRenderer.Unbaked::texture)
                    )
                    .apply(p_375993_, HangingSignSpecialRenderer.Unbaked::new)
        );

        public Unbaked(WoodType p_375515_) {
            this(p_375515_, Optional.empty());
        }

        @Override
        public MapCodec<HangingSignSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_376434_) {
            Model model = HangingSignRenderer.createSignModel(p_376434_, this.woodType, HangingSignRenderer.AttachmentType.CEILING_MIDDLE);
            Material material = this.texture.map(Sheets::createHangingSignMaterial).orElseGet(() -> Sheets.getHangingSignMaterial(this.woodType));
            return new HangingSignSpecialRenderer(model, material);
        }
    }
}