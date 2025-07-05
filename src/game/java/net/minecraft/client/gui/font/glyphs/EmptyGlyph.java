package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class EmptyGlyph extends BakedGlyph {
    public static final EmptyGlyph INSTANCE = new EmptyGlyph();

    public EmptyGlyph() {
        super(GlyphRenderTypes.createForColorTexture(ResourceLocation.withDefaultNamespace("")), 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void renderChar(BakedGlyph.GlyphInstance p_361081_, Matrix4f p_253794_, VertexConsumer p_95282_, int p_95287_) {
    }
}