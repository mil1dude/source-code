package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SectionBufferBuilderPack implements AutoCloseable {
    private static final List<RenderType> RENDER_TYPES = RenderType.chunkBufferLayers();
    public static final int TOTAL_BUFFERS_SIZE = RENDER_TYPES.stream().mapToInt(RenderType::bufferSize).sum();
    private final Map<RenderType, ByteBufferBuilder> buffers = Util.make(new Reference2ObjectArrayMap<>(RENDER_TYPES.size()), p_340902_ -> {
        for (RenderType rendertype : RENDER_TYPES) {
            p_340902_.put(rendertype, new ByteBufferBuilder(rendertype.bufferSize()));
        }
    });

    public ByteBufferBuilder buffer(RenderType p_342149_) {
        return this.buffers.get(p_342149_);
    }

    public void clearAll() {
        this.buffers.values().forEach(ByteBufferBuilder::clear);
    }

    public void discardAll() {
        this.buffers.values().forEach(ByteBufferBuilder::discard);
    }

    @Override
    public void close() {
        this.buffers.values().forEach(ByteBufferBuilder::close);
    }
}