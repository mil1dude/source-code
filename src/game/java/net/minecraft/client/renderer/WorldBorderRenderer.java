package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldBorderRenderer {
    public static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");

    public void render(WorldBorder p_366746_, Vec3 p_368400_, double p_360813_, double p_369225_) {
        double d0 = p_366746_.getMinX();
        double d1 = p_366746_.getMaxX();
        double d2 = p_366746_.getMinZ();
        double d3 = p_366746_.getMaxZ();
        if (!(p_368400_.x < d1 - p_360813_)
            || !(p_368400_.x > d0 + p_360813_)
            || !(p_368400_.z < d3 - p_360813_)
            || !(p_368400_.z > d2 + p_360813_)) {
            double d4 = 1.0 - p_366746_.getDistanceToBorder(p_368400_.x, p_368400_.z) / p_360813_;
            d4 = Math.pow(d4, 4.0);
            d4 = Mth.clamp(d4, 0.0, 1.0);
            double d5 = p_368400_.x;
            double d6 = p_368400_.z;
            float f = (float)p_369225_;
            RenderType rendertype = RenderType.worldBorder(Minecraft.useShaderTransparency());
            rendertype.setupRenderState();
            int i = p_366746_.getStatus().getColor();
            float f1 = (float)ARGB.red(i) / 255.0F;
            float f2 = (float)ARGB.green(i) / 255.0F;
            float f3 = (float)ARGB.blue(i) / 255.0F;
            RenderSystem.setShaderColor(f1, f2, f3, (float)d4);
            float f4 = (float)(Util.getMillis() % 3000L) / 3000.0F;
            float f5 = (float)(-Mth.frac(p_368400_.y * 0.5));
            float f6 = f5 + f;
            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            double d7 = Math.max((double)Mth.floor(d6 - p_360813_), d2);
            double d8 = Math.min((double)Mth.ceil(d6 + p_360813_), d3);
            float f7 = (float)(Mth.floor(d7) & 1) * 0.5F;
            if (d5 > d1 - p_360813_) {
                float f8 = f7;

                for (double d9 = d7; d9 < d8; f8 += 0.5F) {
                    double d10 = Math.min(1.0, d8 - d9);
                    float f9 = (float)d10 * 0.5F;
                    bufferbuilder.addVertex((float)(d1 - d5), -f, (float)(d9 - d6)).setUv(f4 - f8, f4 + f6);
                    bufferbuilder.addVertex((float)(d1 - d5), -f, (float)(d9 + d10 - d6)).setUv(f4 - (f9 + f8), f4 + f6);
                    bufferbuilder.addVertex((float)(d1 - d5), f, (float)(d9 + d10 - d6)).setUv(f4 - (f9 + f8), f4 + f5);
                    bufferbuilder.addVertex((float)(d1 - d5), f, (float)(d9 - d6)).setUv(f4 - f8, f4 + f5);
                    d9++;
                }
            }

            if (d5 < d0 + p_360813_) {
                float f10 = f7;

                for (double d11 = d7; d11 < d8; f10 += 0.5F) {
                    double d14 = Math.min(1.0, d8 - d11);
                    float f13 = (float)d14 * 0.5F;
                    bufferbuilder.addVertex((float)(d0 - d5), -f, (float)(d11 - d6)).setUv(f4 + f10, f4 + f6);
                    bufferbuilder.addVertex((float)(d0 - d5), -f, (float)(d11 + d14 - d6)).setUv(f4 + f13 + f10, f4 + f6);
                    bufferbuilder.addVertex((float)(d0 - d5), f, (float)(d11 + d14 - d6)).setUv(f4 + f13 + f10, f4 + f5);
                    bufferbuilder.addVertex((float)(d0 - d5), f, (float)(d11 - d6)).setUv(f4 + f10, f4 + f5);
                    d11++;
                }
            }

            d7 = Math.max((double)Mth.floor(d5 - p_360813_), d0);
            d8 = Math.min((double)Mth.ceil(d5 + p_360813_), d1);
            f7 = (float)(Mth.floor(d7) & 1) * 0.5F;
            if (d6 > d3 - p_360813_) {
                float f11 = f7;

                for (double d12 = d7; d12 < d8; f11 += 0.5F) {
                    double d15 = Math.min(1.0, d8 - d12);
                    float f14 = (float)d15 * 0.5F;
                    bufferbuilder.addVertex((float)(d12 - d5), -f, (float)(d3 - d6)).setUv(f4 + f11, f4 + f6);
                    bufferbuilder.addVertex((float)(d12 + d15 - d5), -f, (float)(d3 - d6)).setUv(f4 + f14 + f11, f4 + f6);
                    bufferbuilder.addVertex((float)(d12 + d15 - d5), f, (float)(d3 - d6)).setUv(f4 + f14 + f11, f4 + f5);
                    bufferbuilder.addVertex((float)(d12 - d5), f, (float)(d3 - d6)).setUv(f4 + f11, f4 + f5);
                    d12++;
                }
            }

            if (d6 < d2 + p_360813_) {
                float f12 = f7;

                for (double d13 = d7; d13 < d8; f12 += 0.5F) {
                    double d16 = Math.min(1.0, d8 - d13);
                    float f15 = (float)d16 * 0.5F;
                    bufferbuilder.addVertex((float)(d13 - d5), -f, (float)(d2 - d6)).setUv(f4 - f12, f4 + f6);
                    bufferbuilder.addVertex((float)(d13 + d16 - d5), -f, (float)(d2 - d6)).setUv(f4 - (f15 + f12), f4 + f6);
                    bufferbuilder.addVertex((float)(d13 + d16 - d5), f, (float)(d2 - d6)).setUv(f4 - (f15 + f12), f4 + f5);
                    bufferbuilder.addVertex((float)(d13 - d5), f, (float)(d2 - d6)).setUv(f4 - f12, f4 + f5);
                    d13++;
                }
            }

            MeshData meshdata = bufferbuilder.build();
            if (meshdata != null) {
                BufferUploader.drawWithShader(meshdata);
            }

            rendertype.clearRenderState();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}