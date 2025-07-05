package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BedSpecialRenderer implements NoDataSpecialModelRenderer {
    private final BedRenderer bedRenderer;
    private final Material material;

    public BedSpecialRenderer(BedRenderer p_376795_, Material p_375687_) {
        this.bedRenderer = p_376795_;
        this.material = p_375687_;
    }

    @Override
    public void render(ItemDisplayContext p_375946_, PoseStack p_376206_, MultiBufferSource p_378391_, int p_376392_, int p_376610_, boolean p_376275_) {
        this.bedRenderer.renderInHand(p_376206_, p_378391_, p_376392_, p_376610_, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(ResourceLocation texture) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<BedSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_375943_ -> p_375943_.group(ResourceLocation.CODEC.fieldOf("texture").forGetter(BedSpecialRenderer.Unbaked::texture))
                    .apply(p_375943_, BedSpecialRenderer.Unbaked::new)
        );

        public Unbaked(DyeColor p_378179_) {
            this(Sheets.colorToResourceMaterial(p_378179_));
        }

        @Override
        public MapCodec<BedSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_378729_) {
            return new BedSpecialRenderer(new BedRenderer(p_378729_), Sheets.createBedMaterial(this.texture));
        }
    }
}