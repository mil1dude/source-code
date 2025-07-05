package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.Util;

public class GameTestTicker {
    public static final GameTestTicker SINGLETON = new GameTestTicker();
    private final Collection<GameTestInfo> testInfos = Lists.newCopyOnWriteArrayList();
    @Nullable
    private GameTestRunner runner;

    private GameTestTicker() {
    }

    public void add(GameTestInfo p_127789_) {
        this.testInfos.add(p_127789_);
    }

    public void clear() {
        this.testInfos.clear();
        if (this.runner != null) {
            this.runner.stop();
            this.runner = null;
        }
    }

    public void setRunner(GameTestRunner p_328613_) {
        if (this.runner != null) {
            Util.logAndPauseIfInIde("The runner was already set in GameTestTicker");
        }

        this.runner = p_328613_;
    }

    public void tick() {
        if (this.runner != null) {
            this.testInfos.forEach(p_328686_ -> p_328686_.tick(this.runner));
            this.testInfos.removeIf(GameTestInfo::isDone);
        }
    }
}