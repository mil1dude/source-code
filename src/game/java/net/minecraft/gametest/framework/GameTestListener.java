package net.minecraft.gametest.framework;

public interface GameTestListener {
    void testStructureLoaded(GameTestInfo p_127651_);

    void testPassed(GameTestInfo p_177494_, GameTestRunner p_328578_);

    void testFailed(GameTestInfo p_127652_, GameTestRunner p_334963_);

    void testAddedForRerun(GameTestInfo p_329777_, GameTestInfo p_335800_, GameTestRunner p_330350_);
}