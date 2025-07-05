package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.io.IOException;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface GlyphProviderDefinition {
    MapCodec<GlyphProviderDefinition> MAP_CODEC = GlyphProviderType.CODEC.dispatchMap(GlyphProviderDefinition::type, GlyphProviderType::mapCodec);

    GlyphProviderType type();

    Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack();

    @OnlyIn(Dist.CLIENT)
    public static record Conditional(GlyphProviderDefinition definition, FontOption.Filter filter) {
        public static final Codec<GlyphProviderDefinition.Conditional> CODEC = RecordCodecBuilder.create(
            p_330851_ -> p_330851_.group(
                        GlyphProviderDefinition.MAP_CODEC.forGetter(GlyphProviderDefinition.Conditional::definition),
                        FontOption.Filter.CODEC
                            .optionalFieldOf("filter", FontOption.Filter.ALWAYS_PASS)
                            .forGetter(GlyphProviderDefinition.Conditional::filter)
                    )
                    .apply(p_330851_, GlyphProviderDefinition.Conditional::new)
        );
    }

    @OnlyIn(Dist.CLIENT)
    public interface Loader {
        GlyphProvider load(ResourceManager p_286639_) throws IOException;
    }

    @OnlyIn(Dist.CLIENT)
    public static record Reference(ResourceLocation id) {
    }
}