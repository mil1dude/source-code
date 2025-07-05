package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.slf4j.Logger;

public class LogTestReporter implements TestReporter {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onTestFailed(GameTestInfo p_127797_) {
        String s = p_127797_.getTestOrigin().toShortString();
        if (p_127797_.isRequired()) {
            LOGGER.error("{} failed at {}! {}", p_127797_.getTestName(), s, Util.describeError(p_127797_.getError()));
        } else {
            LOGGER.warn("(optional) {} failed at {}. {}", p_127797_.getTestName(), s, Util.describeError(p_127797_.getError()));
        }
    }

    @Override
    public void onTestSuccess(GameTestInfo p_177676_) {
    }
}