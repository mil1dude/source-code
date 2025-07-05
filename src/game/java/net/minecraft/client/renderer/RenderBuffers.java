package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SequencedMap;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBuffers {
    private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
    private final SectionBufferBuilderPool sectionBufferPool;
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource crumblingBufferSource;
    private final OutlineBufferSource outlineBufferSource;

    public RenderBuffers(int p_312933_) {
        this.sectionBufferPool = SectionBufferBuilderPool.allocate(p_312933_);
        SequencedMap<RenderType, ByteBufferBuilder> sequencedmap = Util.make(new Object2ObjectLinkedOpenHashMap<>(), p_357875_ -> {
            p_357875_.put(Sheets.solidBlockSheet(), this.fixedBufferPack.buffer(RenderType.solid()));
            p_357875_.put(Sheets.cutoutBlockSheet(), this.fixedBufferPack.buffer(RenderType.cutout()));
            p_357875_.put(Sheets.bannerSheet(), this.fixedBufferPack.buffer(RenderType.cutoutMipped()));
            p_357875_.put(Sheets.translucentItemSheet(), this.fixedBufferPack.buffer(RenderType.translucent()));
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, Sheets.shieldSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, Sheets.bedSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, Sheets.shulkerBoxSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, Sheets.signSheet());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, Sheets.hangingSignSheet());
            p_357875_.put(Sheets.chestSheet(), new ByteBufferBuilder(786432));
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, RenderType.armorEntityGlint());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, RenderType.glint());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, RenderType.glintTranslucent());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, RenderType.entityGlint());
            put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357875_, RenderType.waterMask());
        });
        this.bufferSource = MultiBufferSource.immediateWithBuffers(sequencedmap, new ByteBufferBuilder(786432));
        this.outlineBufferSource = new OutlineBufferSource(this.bufferSource);
        SequencedMap<RenderType, ByteBufferBuilder> sequencedmap1 = Util.make(
            new Object2ObjectLinkedOpenHashMap<>(),
            p_357874_ -> ModelBakery.DESTROY_TYPES
                    .forEach(p_173062_ -> put((Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>)p_357874_, p_173062_))
        );
        this.crumblingBufferSource = MultiBufferSource.immediateWithBuffers(sequencedmap1, new ByteBufferBuilder(0));
    }

    private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> p_110102_, RenderType p_110103_) {
        p_110102_.put(p_110103_, new ByteBufferBuilder(p_110103_.bufferSize()));
    }

    public SectionBufferBuilderPack fixedBufferPack() {
        return this.fixedBufferPack;
    }

    public SectionBufferBuilderPool sectionBufferPool() {
        return this.sectionBufferPool;
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return this.crumblingBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}