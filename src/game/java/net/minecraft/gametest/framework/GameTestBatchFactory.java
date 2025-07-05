package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;

public class GameTestBatchFactory {
    private static final int MAX_TESTS_PER_BATCH = 50;

    public static Collection<GameTestBatch> fromTestFunction(Collection<TestFunction> p_327987_, ServerLevel p_333027_) {
        Map<String, List<TestFunction>> map = p_327987_.stream().collect(Collectors.groupingBy(TestFunction::batchName));
        return map.entrySet()
            .stream()
            .flatMap(
                p_332128_ -> {
                    String s = p_332128_.getKey();
                    List<TestFunction> list = p_332128_.getValue();
                    return Streams.mapWithIndex(
                        Lists.partition(list, 50).stream(),
                        (p_341093_, p_341094_) -> toGameTestBatch(p_341093_.stream().map(p_334925_ -> toGameTestInfo(p_334925_, 0, p_333027_)).toList(), s, p_341094_)
                    );
                }
            )
            .toList();
    }

    public static GameTestInfo toGameTestInfo(TestFunction p_330595_, int p_336033_, ServerLevel p_330290_) {
        return new GameTestInfo(p_330595_, StructureUtils.getRotationForRotationSteps(p_336033_), p_330290_, RetryOptions.noRetries());
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo() {
        return fromGameTestInfo(50);
    }

    public static GameTestRunner.GameTestBatcher fromGameTestInfo(int p_344529_) {
        return p_341088_ -> {
            Map<String, List<GameTestInfo>> map = p_341088_.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(p_330180_ -> p_330180_.getTestFunction().batchName()));
            return map.entrySet()
                .stream()
                .flatMap(
                    p_341090_ -> {
                        String s = p_341090_.getKey();
                        List<GameTestInfo> list = p_341090_.getValue();
                        return Streams.mapWithIndex(
                            Lists.partition(list, p_344529_).stream(), (p_341085_, p_341086_) -> toGameTestBatch(List.copyOf(p_341085_), s, p_341086_)
                        );
                    }
                )
                .toList();
        };
    }

    public static GameTestBatch toGameTestBatch(Collection<GameTestInfo> p_342407_, String p_332050_, long p_335589_) {
        Consumer<ServerLevel> consumer = GameTestRegistry.getBeforeBatchFunction(p_332050_);
        Consumer<ServerLevel> consumer1 = GameTestRegistry.getAfterBatchFunction(p_332050_);
        return new GameTestBatch(p_332050_ + ":" + p_335589_, p_342407_, consumer, consumer1);
    }
}