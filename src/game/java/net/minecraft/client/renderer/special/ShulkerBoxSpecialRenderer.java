package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBoxSpecialRenderer implements NoDataSpecialModelRenderer {
    private final ShulkerBoxRenderer shulkerBoxRenderer;
    private final float openness;
    private final Direction orientation;
    private final Material material;

    public ShulkerBoxSpecialRenderer(ShulkerBoxRenderer p_378569_, float p_376947_, Direction p_375769_, Material p_377130_) {
        this.shulkerBoxRenderer = p_378569_;
        this.openness = p_376947_;
        this.orientation = p_375769_;
        this.material = p_377130_;
    }

    @Override
    public void render(ItemDisplayContext p_375824_, PoseStack p_378669_, MultiBufferSource p_378773_, int p_376847_, int p_376981_, boolean p_375711_) {
        this.shulkerBoxRenderer.render(p_378669_, p_378773_, p_376847_, p_376981_, this.orientation, this.openness, this.material);
    }

    @OnlyIn(Dist.CLIENT)
    public static record Unbaked(ResourceLocation texture, float openness, Direction orientation) implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<ShulkerBoxSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
            p_376772_ -> p_376772_.group(
                        ResourceLocation.CODEC.fieldOf("texture").forGetter(ShulkerBoxSpecialRenderer.Unbaked::texture),
                        Codec.FLOAT.optionalFieldOf("openness", Float.valueOf(0.0F)).forGetter(ShulkerBoxSpecialRenderer.Unbaked::openness),
                        Direction.CODEC.optionalFieldOf("orientation", Direction.UP).forGetter(ShulkerBoxSpecialRenderer.Unbaked::orientation)
                    )
                    .apply(p_376772_, ShulkerBoxSpecialRenderer.Unbaked::new)
        );

        public Unbaked() {
            this(ResourceLocation.withDefaultNamespace("shulker"), 0.0F, Direction.UP);
        }

        public Unbaked(DyeColor p_375531_) {
            this(Sheets.colorToShulkerMaterial(p_375531_), 0.0F, Direction.UP);
        }

        @Override
        public MapCodec<ShulkerBoxSpecialRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet p_376043_) {
            return new ShulkerBoxSpecialRenderer(new ShulkerBoxRenderer(p_376043_), this.openness, this.orientation, Sheets.createShulkerMaterial(this.texture));
        }
    }
}