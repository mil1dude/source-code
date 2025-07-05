package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
    public static final MapCodec<WeightedStateProvider> CODEC = SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC)
        .comapFlatMap(WeightedStateProvider::create, p_161600_ -> p_161600_.weightedList)
        .fieldOf("entries");
    private final SimpleWeightedRandomList<BlockState> weightedList;

    private static DataResult<WeightedStateProvider> create(SimpleWeightedRandomList<BlockState> p_161598_) {
        return p_161598_.isEmpty()
            ? DataResult.error(() -> "WeightedStateProvider with no states")
            : DataResult.success(new WeightedStateProvider(p_161598_));
    }

    public WeightedStateProvider(SimpleWeightedRandomList<BlockState> p_161596_) {
        this.weightedList = p_161596_;
    }

    public WeightedStateProvider(SimpleWeightedRandomList.Builder<BlockState> p_161594_) {
        this(p_161594_.build());
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource p_225966_, BlockPos p_225967_) {
        return this.weightedList.getRandomValue(p_225966_).orElseThrow(IllegalStateException::new);
    }
}