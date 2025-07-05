package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.ARGB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteContents implements Stitcher.Entry, AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation name;
    final int width;
    final int height;
    private final NativeImage originalImage;
    NativeImage[] byMipLevel;
    @Nullable
    private final SpriteContents.AnimatedTexture animatedTexture;
    private final ResourceMetadata metadata;

    public SpriteContents(ResourceLocation p_249787_, FrameSize p_251031_, NativeImage p_252131_, ResourceMetadata p_299427_) {
        this.name = p_249787_;
        this.width = p_251031_.width();
        this.height = p_251031_.height();
        this.metadata = p_299427_;
        this.animatedTexture = p_299427_.getSection(AnimationMetadataSection.TYPE)
            .map(p_374666_ -> this.createAnimatedTexture(p_251031_, p_252131_.getWidth(), p_252131_.getHeight(), p_374666_))
            .orElse(null);
        this.originalImage = p_252131_;
        this.byMipLevel = new NativeImage[]{this.originalImage};
    }

    public void increaseMipLevel(int p_248864_) {
        try {
            this.byMipLevel = MipmapGenerator.generateMipLevels(this.byMipLevel, p_248864_);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Generating mipmaps for frame");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Sprite being mipmapped");
            crashreportcategory.setDetail("First frame", () -> {
                StringBuilder stringbuilder = new StringBuilder();
                if (stringbuilder.length() > 0) {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(this.originalImage.getWidth()).append("x").append(this.originalImage.getHeight());
                return stringbuilder.toString();
            });
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Frame being iterated");
            crashreportcategory1.setDetail("Sprite name", this.name);
            crashreportcategory1.setDetail("Sprite size", () -> this.width + " x " + this.height);
            crashreportcategory1.setDetail("Sprite frames", () -> this.getFrameCount() + " frames");
            crashreportcategory1.setDetail("Mipmap levels", p_248864_);
            throw new ReportedException(crashreport);
        }
    }

    private int getFrameCount() {
        return this.animatedTexture != null ? this.animatedTexture.frames.size() : 1;
    }

    @Nullable
    private SpriteContents.AnimatedTexture createAnimatedTexture(FrameSize p_250817_, int p_249792_, int p_252353_, AnimationMetadataSection p_250947_) {
        int i = p_249792_ / p_250817_.width();
        int j = p_252353_ / p_250817_.height();
        int k = i * j;
        int l = p_250947_.defaultFrameTime();
        List<SpriteContents.FrameInfo> list;
        if (p_250947_.frames().isEmpty()) {
            list = new ArrayList<>(k);

            for (int i1 = 0; i1 < k; i1++) {
                list.add(new SpriteContents.FrameInfo(i1, l));
            }
        } else {
            List<AnimationFrame> list1 = p_250947_.frames().get();
            list = new ArrayList<>(list1.size());

            for (AnimationFrame animationframe : list1) {
                list.add(new SpriteContents.FrameInfo(animationframe.index(), animationframe.timeOr(l)));
            }

            int j1 = 0;
            IntSet intset = new IntOpenHashSet();

            for (Iterator<SpriteContents.FrameInfo> iterator = list.iterator(); iterator.hasNext(); j1++) {
                SpriteContents.FrameInfo spritecontents$frameinfo = iterator.next();
                boolean flag = true;
                if (spritecontents$frameinfo.time <= 0) {
                    LOGGER.warn("Invalid frame duration on sprite {} frame {}: {}", this.name, j1, spritecontents$frameinfo.time);
                    flag = false;
                }

                if (spritecontents$frameinfo.index < 0 || spritecontents$frameinfo.index >= k) {
                    LOGGER.warn("Invalid frame index on sprite {} frame {}: {}", this.name, j1, spritecontents$frameinfo.index);
                    flag = false;
                }

                if (flag) {
                    intset.add(spritecontents$frameinfo.index);
                } else {
                    iterator.remove();
                }
            }

            int[] aint = IntStream.range(0, k).filter(p_251185_ -> !intset.contains(p_251185_)).toArray();
            if (aint.length > 0) {
                LOGGER.warn("Unused frames in sprite {}: {}", this.name, Arrays.toString(aint));
            }
        }

        return list.size() <= 1 ? null : new SpriteContents.AnimatedTexture(List.copyOf(list), i, p_250947_.interpolatedFrames());
    }

    void upload(int p_248895_, int p_250245_, int p_250458_, int p_251337_, NativeImage[] p_248825_) {
        for (int i = 0; i < this.byMipLevel.length; i++) {
            p_248825_[i].upload(i, p_248895_ >> i, p_250245_ >> i, p_250458_ >> i, p_251337_ >> i, this.width >> i, this.height >> i, false);
        }
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }

    @Override
    public ResourceLocation name() {
        return this.name;
    }

    public IntStream getUniqueFrames() {
        return this.animatedTexture != null ? this.animatedTexture.getUniqueFrames() : IntStream.of(1);
    }

    @Nullable
    public SpriteTicker createTicker() {
        return this.animatedTexture != null ? this.animatedTexture.createTicker() : null;
    }

    public ResourceMetadata metadata() {
        return this.metadata;
    }

    @Override
    public void close() {
        for (NativeImage nativeimage : this.byMipLevel) {
            nativeimage.close();
        }
    }

    @Override
    public String toString() {
        return "SpriteContents{name=" + this.name + ", frameCount=" + this.getFrameCount() + ", height=" + this.height + ", width=" + this.width + "}";
    }

    public boolean isTransparent(int p_250374_, int p_250934_, int p_249573_) {
        int i = p_250934_;
        int j = p_249573_;
        if (this.animatedTexture != null) {
            i = p_250934_ + this.animatedTexture.getFrameX(p_250374_) * this.width;
            j = p_249573_ + this.animatedTexture.getFrameY(p_250374_) * this.height;
        }

        return ARGB.alpha(this.originalImage.getPixel(i, j)) == 0;
    }

    public void uploadFirstFrame(int p_252315_, int p_248634_) {
        if (this.animatedTexture != null) {
            this.animatedTexture.uploadFirstFrame(p_252315_, p_248634_);
        } else {
            this.upload(p_252315_, p_248634_, 0, 0, this.byMipLevel);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class AnimatedTexture {
        final List<SpriteContents.FrameInfo> frames;
        private final int frameRowSize;
        private final boolean interpolateFrames;

        AnimatedTexture(final List<SpriteContents.FrameInfo> p_250968_, final int p_251686_, final boolean p_251832_) {
            this.frames = p_250968_;
            this.frameRowSize = p_251686_;
            this.interpolateFrames = p_251832_;
        }

        int getFrameX(int p_249475_) {
            return p_249475_ % this.frameRowSize;
        }

        int getFrameY(int p_251327_) {
            return p_251327_ / this.frameRowSize;
        }

        void uploadFrame(int p_250449_, int p_248877_, int p_249060_) {
            int i = this.getFrameX(p_249060_) * SpriteContents.this.width;
            int j = this.getFrameY(p_249060_) * SpriteContents.this.height;
            SpriteContents.this.upload(p_250449_, p_248877_, i, j, SpriteContents.this.byMipLevel);
        }

        public SpriteTicker createTicker() {
            return SpriteContents.this.new Ticker(this, this.interpolateFrames ? SpriteContents.this.new InterpolationData() : null);
        }

        public void uploadFirstFrame(int p_251807_, int p_248676_) {
            this.uploadFrame(p_251807_, p_248676_, this.frames.get(0).index);
        }

        public IntStream getUniqueFrames() {
            return this.frames.stream().mapToInt(p_249981_ -> p_249981_.index).distinct();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record FrameInfo(int index, int time) {
    }

    @OnlyIn(Dist.CLIENT)
    final class InterpolationData implements AutoCloseable {
        private final NativeImage[] activeFrame = new NativeImage[SpriteContents.this.byMipLevel.length];

        InterpolationData() {
            for (int i = 0; i < this.activeFrame.length; i++) {
                int j = SpriteContents.this.width >> i;
                int k = SpriteContents.this.height >> i;
                this.activeFrame[i] = new NativeImage(j, k, false);
            }
        }

        void uploadInterpolatedFrame(int p_250513_, int p_251644_, SpriteContents.Ticker p_248626_) {
            SpriteContents.AnimatedTexture spritecontents$animatedtexture = p_248626_.animationInfo;
            List<SpriteContents.FrameInfo> list = spritecontents$animatedtexture.frames;
            SpriteContents.FrameInfo spritecontents$frameinfo = list.get(p_248626_.frame);
            float f = (float)p_248626_.subFrame / (float)spritecontents$frameinfo.time;
            int i = spritecontents$frameinfo.index;
            int j = list.get((p_248626_.frame + 1) % list.size()).index;
            if (i != j) {
                for (int k = 0; k < this.activeFrame.length; k++) {
                    int l = SpriteContents.this.width >> k;
                    int i1 = SpriteContents.this.height >> k;

                    for (int j1 = 0; j1 < i1; j1++) {
                        for (int k1 = 0; k1 < l; k1++) {
                            int l1 = this.getPixel(spritecontents$animatedtexture, i, k, k1, j1);
                            int i2 = this.getPixel(spritecontents$animatedtexture, j, k, k1, j1);
                            this.activeFrame[k].setPixel(k1, j1, ARGB.lerp(f, l1, i2));
                        }
                    }
                }

                SpriteContents.this.upload(p_250513_, p_251644_, 0, 0, this.activeFrame);
            }
        }

        private int getPixel(SpriteContents.AnimatedTexture p_251976_, int p_250761_, int p_250049_, int p_250004_, int p_251489_) {
            return SpriteContents.this.byMipLevel[p_250049_]
                .getPixel(
                    p_250004_ + (p_251976_.getFrameX(p_250761_) * SpriteContents.this.width >> p_250049_),
                    p_251489_ + (p_251976_.getFrameY(p_250761_) * SpriteContents.this.height >> p_250049_)
                );
        }

        @Override
        public void close() {
            for (NativeImage nativeimage : this.activeFrame) {
                nativeimage.close();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Ticker implements SpriteTicker {
        int frame;
        int subFrame;
        final SpriteContents.AnimatedTexture animationInfo;
        @Nullable
        private final SpriteContents.InterpolationData interpolationData;

        Ticker(final SpriteContents.AnimatedTexture p_249618_, @Nullable final SpriteContents.InterpolationData p_251097_) {
            this.animationInfo = p_249618_;
            this.interpolationData = p_251097_;
        }

        @Override
        public void tickAndUpload(int p_249105_, int p_249676_) {
            this.subFrame++;
            SpriteContents.FrameInfo spritecontents$frameinfo = this.animationInfo.frames.get(this.frame);
            if (this.subFrame >= spritecontents$frameinfo.time) {
                int i = spritecontents$frameinfo.index;
                this.frame = (this.frame + 1) % this.animationInfo.frames.size();
                this.subFrame = 0;
                int j = this.animationInfo.frames.get(this.frame).index;
                if (i != j) {
                    this.animationInfo.uploadFrame(p_249105_, p_249676_, j);
                }
            } else if (this.interpolationData != null) {
                this.interpolationData.uploadInterpolatedFrame(p_249105_, p_249676_, this);
            }
        }

        @Override
        public void close() {
            if (this.interpolationData != null) {
                this.interpolationData.close();
            }
        }
    }
}