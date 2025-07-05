package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.client.model.ChestModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChestSpecialRenderer implements NoDataSpecialModelRenderer {
    public static final ResourceLocation GIFT_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("christmas");
    public static final ResourceLocation NORMAL_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("normal");
    public static final ResourceLocation TRAPPED_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("trapped");
    public static final ResourceLocation ENDER_CHEST_TEXTURE = ResourceLocation.withDefaultNamespace("ender");
    private final ChestModel model;
    private final Material material;
    private final float openness;

    public ChestSpecialRenderer(ChestModel p_375837_, Material p_377410_, float p_378366_) {
        this.model = p_375837_;
        this.material = p_377410_;
        this.openness = p_378366_;
    }

    @Override
    public void render(ItemDisplayContext p_375966_, PoseStack p_375910_, MultiBufferSource p_377279_, int p_377702_, int p_377418_, boolean p_377829_) {
        VertexConsumer vertexconsumer = this.material.buffer(p_377279_, RenderType::entitySolid);
        this.model.setupAnim(this.openness);
        this.model.renderToBuffer(p_375910_, vertexconsumer, p_377702_, p_377418_);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(ResourceLocation texture, float openness) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ChestSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_376535_ -> p_376535_.group(
                        ResourceLocation.CODEC.fieldOf("texture").forGetter(ChestSpecialRenderer.Unbaked::texture),
                        Codec.FLOAT.optionalFieldOf("openness", Float.valueOf(0.0F)).forGetter(ChestSpecialRenderer.Unbaked::openness)
                    )
                    .apply(p_376535_, ChestSpecialRenderer.Unbaked::new)
        );

        public Unbaked(ResourceLocation p_376432_) {
            this(p_376432_, 0.0F);
        }

        @Override
        public MapCodec<ChestSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_378720_) {
            ChestModel chestmodel = new ChestModel(p_378720_.bakeLayer(ModelLayers.CHEST));
            Material material = Sheets.chestMaterial(this.texture);
            return new ChestSpecialRenderer(chestmodel, material, this.openness);
        }
    }
}