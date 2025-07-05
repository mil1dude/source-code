package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ShaderProgram(ResourceLocation configId, VertexFormat vertexFormat, ShaderDefines defines) {
    @Override
    public String toString() {
        String s = this.configId + " (" + this.vertexFormat + ")";
        return !this.defines.isEmpty() ? s + " with " + this.defines : s;
    }
}