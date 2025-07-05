package net.minecraft.client.resources.metadata.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record TextureMetadataSection(boolean blur, boolean clamp) {
    public static final boolean DEFAULT_BLUR = false;
    public static final boolean DEFAULT_CLAMP = false;
    public static final Codec<TextureMetadataSection> CODEC = RecordCodecBuilder.create(
        p_377176_ -> p_377176_.group(
                    Codec.BOOL.optionalFieldOf("blur", Boolean.valueOf(false)).forGetter(TextureMetadataSection::blur),
                    Codec.BOOL.optionalFieldOf("clamp", Boolean.valueOf(false)).forGetter(TextureMetadataSection::clamp)
                )
                .apply(p_377176_, TextureMetadataSection::new)
    );
    public static final MetadataSectionType<TextureMetadataSection> TYPE = new MetadataSectionType<>("texture", CODEC);
}