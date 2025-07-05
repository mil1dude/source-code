package net.minecraft.client.renderer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record ShaderProgramConfig(
    ResourceLocation vertex,
    ResourceLocation fragment,
    List<ShaderProgramConfig.Sampler> samplers,
    List<ShaderProgramConfig.Uniform> uniforms,
    ShaderDefines defines
) {
    public static final Codec<ShaderProgramConfig> CODEC = RecordCodecBuilder.create(
        p_363443_ -> p_363443_.group(
                    ResourceLocation.CODEC.fieldOf("vertex").forGetter(ShaderProgramConfig::vertex),
                    ResourceLocation.CODEC.fieldOf("fragment").forGetter(ShaderProgramConfig::fragment),
                    ShaderProgramConfig.Sampler.CODEC.listOf().optionalFieldOf("samplers", List.of()).forGetter(ShaderProgramConfig::samplers),
                    ShaderProgramConfig.Uniform.CODEC.listOf().optionalFieldOf("uniforms", List.of()).forGetter(ShaderProgramConfig::uniforms),
                    ShaderDefines.CODEC.optionalFieldOf("defines", ShaderDefines.EMPTY).forGetter(ShaderProgramConfig::defines)
                )
                .apply(p_363443_, ShaderProgramConfig::new)
    );

    @OnlyIn(Dist.CLIENT)
    public static record Sampler(String name) {
        public static final Codec<ShaderProgramConfig.Sampler> CODEC = RecordCodecBuilder.create(
            p_369078_ -> p_369078_.group(Codec.STRING.fieldOf("name").forGetter(ShaderProgramConfig.Sampler::name))
                    .apply(p_369078_, ShaderProgramConfig.Sampler::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static record Uniform(String name, String type, int count, List<Float> values) {
        public static final Codec<ShaderProgramConfig.Uniform> CODEC = RecordCodecBuilder.<ShaderProgramConfig.Uniform>create(
                p_362962_ -> p_362962_.group(
                            Codec.STRING.fieldOf("name").forGetter(ShaderProgramConfig.Uniform::name),
                            Codec.STRING.fieldOf("type").forGetter(ShaderProgramConfig.Uniform::type),
                            Codec.INT.fieldOf("count").forGetter(ShaderProgramConfig.Uniform::count),
                            Codec.FLOAT.listOf().fieldOf("values").forGetter(ShaderProgramConfig.Uniform::values)
                        )
                        .apply(p_362962_, ShaderProgramConfig.Uniform::new)
            )
            .validate(ShaderProgramConfig.Uniform::validate);

        private static DataResult<ShaderProgramConfig.Uniform> validate(ShaderProgramConfig.Uniform p_368433_) {
            int i = p_368433_.count;
            int j = p_368433_.values.size();
            return j != i && j > 1
                ? DataResult.error(() -> "Invalid amount of uniform values specified (expected " + i + ", found " + j + ")")
                : DataResult.success(p_368433_);
        }
    }
}