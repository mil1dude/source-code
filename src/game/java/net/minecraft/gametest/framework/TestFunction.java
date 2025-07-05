package net.minecraft.gametest.framework;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Rotation;

public record TestFunction(
    String batchName,
    String testName,
    String structureName,
    Rotation rotation,
    int maxTicks,
    long setupTicks,
    boolean required,
    boolean manualOnly,
    int maxAttempts,
    int requiredSuccesses,
    boolean skyAccess,
    Consumer<GameTestHelper> function
) {
    public TestFunction(
        String p_177801_, String p_177802_, String p_177803_, int p_177804_, long p_177805_, boolean p_177806_, Consumer<GameTestHelper> p_177807_
    ) {
        this(p_177801_, p_177802_, p_177803_, Rotation.NONE, p_177804_, p_177805_, p_177806_, false, 1, 1, false, p_177807_);
    }

    public TestFunction(
        String p_177820_,
        String p_177821_,
        String p_177822_,
        Rotation p_177823_,
        int p_177824_,
        long p_177825_,
        boolean p_177826_,
        Consumer<GameTestHelper> p_177827_
    ) {
        this(p_177820_, p_177821_, p_177822_, p_177823_, p_177824_, p_177825_, p_177826_, false, 1, 1, false, p_177827_);
    }

    public void run(GameTestHelper p_128077_) {
        this.function.accept(p_128077_);
    }

    @Override
    public String toString() {
        return this.testName;
    }

    public boolean isFlaky() {
        return this.maxAttempts > 1;
    }
}