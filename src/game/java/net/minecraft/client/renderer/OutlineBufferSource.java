package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import java.util.Optional;
import net.minecraft.util.ARGB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OutlineBufferSource implements MultiBufferSource {
    private final MultiBufferSource.BufferSource bufferSource;
    private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
    private int teamR = 255;
    private int teamG = 255;
    private int teamB = 255;
    private int teamA = 255;

    public OutlineBufferSource(MultiBufferSource.BufferSource p_109927_) {
        this.bufferSource = p_109927_;
    }

    @Override
    public VertexConsumer getBuffer(RenderType p_109935_) {
        if (p_109935_.isOutline()) {
            VertexConsumer vertexconsumer2 = this.outlineBufferSource.getBuffer(p_109935_);
            return new OutlineBufferSource.EntityOutlineGenerator(vertexconsumer2, this.teamR, this.teamG, this.teamB, this.teamA);
        } else {
            VertexConsumer vertexconsumer = this.bufferSource.getBuffer(p_109935_);
            Optional<RenderType> optional = p_109935_.outline();
            if (optional.isPresent()) {
                VertexConsumer vertexconsumer1 = this.outlineBufferSource.getBuffer(optional.get());
                OutlineBufferSource.EntityOutlineGenerator outlinebuffersource$entityoutlinegenerator = new OutlineBufferSource.EntityOutlineGenerator(
                    vertexconsumer1, this.teamR, this.teamG, this.teamB, this.teamA
                );
                return VertexMultiConsumer.create(outlinebuffersource$entityoutlinegenerator, vertexconsumer);
            } else {
                return vertexconsumer;
            }
        }
    }

    public void setColor(int p_109930_, int p_109931_, int p_109932_, int p_109933_) {
        this.teamR = p_109930_;
        this.teamG = p_109931_;
        this.teamB = p_109932_;
        this.teamA = p_109933_;
    }

    public void endOutlineBatch() {
        this.outlineBufferSource.endBatch();
    }

    @OnlyIn(Dist.CLIENT)
    static record EntityOutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer {
        public EntityOutlineGenerator(VertexConsumer p_109943_, int p_109944_, int p_109945_, int p_109946_, int p_109947_) {
            this(p_109943_, ARGB.color(p_109947_, p_109944_, p_109945_, p_109946_));
        }

        @Override
        public VertexConsumer addVertex(float p_342958_, float p_343747_, float p_344781_) {
            this.delegate.addVertex(p_342958_, p_343747_, p_344781_).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int p_343483_, int p_343623_, int p_342060_, int p_342967_) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float p_342182_, float p_342633_) {
            this.delegate.setUv(p_342182_, p_342633_);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int p_344004_, int p_342637_) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int p_343797_, int p_342797_) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float p_343114_, float p_344978_, float p_343069_) {
            return this;
        }
    }
}