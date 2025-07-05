package net.minecraft.client.renderer.texture.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface SpriteResourceLoader {
    Logger LOGGER = LogUtils.getLogger();

    static SpriteResourceLoader create(Collection<MetadataSectionType<?>> p_299052_) {
        return (p_374679_, p_374680_) -> {
            ResourceMetadata resourcemetadata;
            try {
                resourcemetadata = p_374680_.metadata().copySections(p_299052_);
            } catch (Exception exception) {
                LOGGER.error("Unable to parse metadata from {}", p_374679_, exception);
                return null;
            }

            NativeImage nativeimage;
            try (InputStream inputstream = p_374680_.open()) {
                nativeimage = NativeImage.read(inputstream);
            } catch (IOException ioexception) {
                LOGGER.error("Using missing texture, unable to load {}", p_374679_, ioexception);
                return null;
            }

            Optional<AnimationMetadataSection> optional = resourcemetadata.getSection(AnimationMetadataSection.TYPE);
            FrameSize framesize;
            if (optional.isPresent()) {
                framesize = optional.get().calculateFrameSize(nativeimage.getWidth(), nativeimage.getHeight());
                if (!Mth.isMultipleOf(nativeimage.getWidth(), framesize.width()) || !Mth.isMultipleOf(nativeimage.getHeight(), framesize.height())) {
                    LOGGER.error(
                        "Image {} size {},{} is not multiple of frame size {},{}",
                        p_374679_,
                        nativeimage.getWidth(),
                        nativeimage.getHeight(),
                        framesize.width(),
                        framesize.height()
                    );
                    nativeimage.close();
                    return null;
                }
            } else {
                framesize = new FrameSize(nativeimage.getWidth(), nativeimage.getHeight());
            }

            return new SpriteContents(p_374679_, framesize, nativeimage, resourcemetadata);
        };
    }

    @Nullable
    SpriteContents loadSprite(ResourceLocation p_301190_, Resource p_298142_);
}