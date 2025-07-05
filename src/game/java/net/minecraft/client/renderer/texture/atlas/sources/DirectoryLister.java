package net.minecraft.client.renderer.texture.atlas.sources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DirectoryLister implements SpriteSource {
    public static final MapCodec<DirectoryLister> CODEC = RecordCodecBuilder.mapCodec(
        p_262096_ -> p_262096_.group(
                    Codec.STRING.fieldOf("source").forGetter(p_261592_ -> p_261592_.sourcePath),
                    Codec.STRING.fieldOf("prefix").forGetter(p_262146_ -> p_262146_.idPrefix)
                )
                .apply(p_262096_, DirectoryLister::new)
    );
    private final String sourcePath;
    private final String idPrefix;

    public DirectoryLister(String p_261886_, String p_261776_) {
        this.sourcePath = p_261886_;
        this.idPrefix = p_261776_;
    }

    @Override
    public void run(ResourceManager p_261582_, SpriteSource.Output p_261898_) {
        FileToIdConverter filetoidconverter = new FileToIdConverter("textures/" + this.sourcePath, ".png");
        filetoidconverter.listMatchingResources(p_261582_).forEach((p_261906_, p_261635_) -> {
            ResourceLocation resourcelocation = filetoidconverter.fileToId(p_261906_).withPrefix(this.idPrefix);
            p_261898_.add(resourcelocation, p_261635_);
        });
    }

    @Override
    public SpriteSourceType type() {
        return SpriteSources.DIRECTORY;
    }
}