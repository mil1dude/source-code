package net.minecraft.client.resources.model;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiPartBakedModel extends DelegateBakedModel {
    private final List<MultiPartBakedModel.Selector> selectors;
    private final Map<BlockState, BitSet> selectorCache = new Reference2ObjectOpenHashMap<>();

    private static BakedModel getFirstModel(List<MultiPartBakedModel.Selector> p_367507_) {
        if (p_367507_.isEmpty()) {
            throw new IllegalArgumentException("Model must have at least one selector");
        } else {
            return p_367507_.getFirst().model();
        }
    }

    public MultiPartBakedModel(List<MultiPartBakedModel.Selector> p_119462_) {
        super(getFirstModel(p_119462_));
        this.selectors = p_119462_;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState p_235050_, @Nullable Direction p_235051_, RandomSource p_235052_) {
        if (p_235050_ == null) {
            return Collections.emptyList();
        } else {
            BitSet bitset = this.selectorCache.get(p_235050_);
            if (bitset == null) {
                bitset = new BitSet();

                for (int i = 0; i < this.selectors.size(); i++) {
                    if (this.selectors.get(i).condition.test(p_235050_)) {
                        bitset.set(i);
                    }
                }

                this.selectorCache.put(p_235050_, bitset);
            }

            List<BakedQuad> list = new ArrayList<>();
            long j = p_235052_.nextLong();

            for (int k = 0; k < bitset.length(); k++) {
                if (bitset.get(k)) {
                    p_235052_.setSeed(j);
                    list.addAll(this.selectors.get(k).model.getQuads(p_235050_, p_235051_, p_235052_));
                }
            }

            return list;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record Selector(Predicate<BlockState> condition, BakedModel model) {
    }
}