package net.minecraft.gametest.framework;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.exception.ExceptionUtils;

class ReportGameListener implements GameTestListener {
    private int attempts = 0;
    private int successes = 0;

    public ReportGameListener() {
    }

    @Override
    public void testStructureLoaded(GameTestInfo p_177718_) {
        spawnBeacon(p_177718_, Blocks.LIGHT_GRAY_STAINED_GLASS);
        this.attempts++;
    }

    private void handleRetry(GameTestInfo p_333394_, GameTestRunner p_328423_, boolean p_328930_) {
        RetryOptions retryoptions = p_333394_.retryOptions();
        String s = String.format("[Run: %4d, Ok: %4d, Fail: %4d", this.attempts, this.successes, this.attempts - this.successes);
        if (!retryoptions.unlimitedTries()) {
            s = s + String.format(", Left: %4d", retryoptions.numberOfTries() - this.attempts);
        }

        s = s + "]";
        String s1 = p_333394_.getTestName() + " " + (p_328930_ ? "passed" : "failed") + "! " + p_333394_.getRunTime() + "ms";
        String s2 = String.format("%-53s%s", s, s1);
        if (p_328930_) {
            reportPassed(p_333394_, s2);
        } else {
            say(p_333394_.getLevel(), ChatFormatting.RED, s2);
        }

        if (retryoptions.hasTriesLeft(this.attempts, this.successes)) {
            p_328423_.rerunTest(p_333394_);
        }
    }

    @Override
    public void testPassed(GameTestInfo p_177729_, GameTestRunner p_331098_) {
        this.successes++;
        if (p_177729_.retryOptions().hasRetries()) {
            this.handleRetry(p_177729_, p_331098_, true);
        } else if (!p_177729_.isFlaky()) {
            reportPassed(p_177729_, p_177729_.getTestName() + " passed! (" + p_177729_.getRunTime() + "ms)");
        } else {
            if (this.successes >= p_177729_.requiredSuccesses()) {
                reportPassed(p_177729_, p_177729_ + " passed " + this.successes + " times of " + this.attempts + " attempts.");
            } else {
                say(
                    p_177729_.getLevel(),
                    ChatFormatting.GREEN,
                    "Flaky test " + p_177729_ + " succeeded, attempt: " + this.attempts + " successes: " + this.successes
                );
                p_331098_.rerunTest(p_177729_);
            }
        }
    }

    @Override
    public void testFailed(GameTestInfo p_177737_, GameTestRunner p_330024_) {
        if (!p_177737_.isFlaky()) {
            reportFailure(p_177737_, p_177737_.getError());
            if (p_177737_.retryOptions().hasRetries()) {
                this.handleRetry(p_177737_, p_330024_, false);
            }
        } else {
            TestFunction testfunction = p_177737_.getTestFunction();
            String s = "Flaky test " + p_177737_ + " failed, attempt: " + this.attempts + "/" + testfunction.maxAttempts();
            if (testfunction.requiredSuccesses() > 1) {
                s = s + ", successes: " + this.successes + " (" + testfunction.requiredSuccesses() + " required)";
            }

            say(p_177737_.getLevel(), ChatFormatting.YELLOW, s);
            if (p_177737_.maxAttempts() - this.attempts + this.successes >= p_177737_.requiredSuccesses()) {
                p_330024_.rerunTest(p_177737_);
            } else {
                reportFailure(p_177737_, new ExhaustedAttemptsException(this.attempts, this.successes, p_177737_));
            }
        }
    }

    @Override
    public void testAddedForRerun(GameTestInfo p_330084_, GameTestInfo p_327991_, GameTestRunner p_334385_) {
        p_327991_.addListener(this);
    }

    public static void reportPassed(GameTestInfo p_177723_, String p_177724_) {
        updateBeaconGlass(p_177723_, Blocks.LIME_STAINED_GLASS);
        visualizePassedTest(p_177723_, p_177724_);
    }

    private static void visualizePassedTest(GameTestInfo p_177731_, String p_177732_) {
        say(p_177731_.getLevel(), ChatFormatting.GREEN, p_177732_);
        GlobalTestReporter.onTestSuccess(p_177731_);
    }

    protected static void reportFailure(GameTestInfo p_177726_, Throwable p_177727_) {
        updateBeaconGlass(p_177726_, p_177726_.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
        spawnLectern(p_177726_, Util.describeError(p_177727_));
        visualizeFailedTest(p_177726_, p_177727_);
    }

    protected static void visualizeFailedTest(GameTestInfo p_177734_, Throwable p_177735_) {
        String s = p_177735_.getMessage() + (p_177735_.getCause() == null ? "" : " cause: " + Util.describeError(p_177735_.getCause()));
        String s1 = (p_177734_.isRequired() ? "" : "(optional) ") + p_177734_.getTestName() + " failed! " + s;
        say(p_177734_.getLevel(), p_177734_.isRequired() ? ChatFormatting.RED : ChatFormatting.YELLOW, s1);
        Throwable throwable = MoreObjects.firstNonNull(ExceptionUtils.getRootCause(p_177735_), p_177735_);
        if (throwable instanceof GameTestAssertPosException gametestassertposexception) {
            showRedBox(p_177734_.getLevel(), gametestassertposexception.getAbsolutePos(), gametestassertposexception.getMessageToShowAtBlock());
        }

        GlobalTestReporter.onTestFailed(p_177734_);
    }

    protected static void spawnBeacon(GameTestInfo p_177720_, Block p_177721_) {
        ServerLevel serverlevel = p_177720_.getLevel();
        BlockPos blockpos = getBeaconPos(p_177720_);
        serverlevel.setBlockAndUpdate(blockpos, Blocks.BEACON.defaultBlockState().rotate(p_177720_.getRotation()));
        updateBeaconGlass(p_177720_, p_177721_);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                BlockPos blockpos1 = blockpos.offset(i, -1, j);
                serverlevel.setBlockAndUpdate(blockpos1, Blocks.IRON_BLOCK.defaultBlockState());
            }
        }
    }

    private static BlockPos getBeaconPos(GameTestInfo p_344999_) {
        BlockPos blockpos = p_344999_.getStructureBlockPos();
        BlockPos blockpos1 = new BlockPos(-1, -2, -1);
        return StructureTemplate.transform(blockpos.offset(blockpos1), Mirror.NONE, p_344999_.getRotation(), blockpos);
    }

    private static void updateBeaconGlass(GameTestInfo p_343978_, Block p_344076_) {
        ServerLevel serverlevel = p_343978_.getLevel();
        BlockPos blockpos = getBeaconPos(p_343978_);
        if (serverlevel.getBlockState(blockpos).is(Blocks.BEACON)) {
            BlockPos blockpos1 = blockpos.offset(0, 1, 0);
            serverlevel.setBlockAndUpdate(blockpos1, p_344076_.defaultBlockState());
        }
    }

    private static void spawnLectern(GameTestInfo p_177739_, String p_177740_) {
        ServerLevel serverlevel = p_177739_.getLevel();
        BlockPos blockpos = p_177739_.getStructureBlockPos();
        BlockPos blockpos1 = new BlockPos(-1, 0, -1);
        BlockPos blockpos2 = StructureTemplate.transform(blockpos.offset(blockpos1), Mirror.NONE, p_177739_.getRotation(), blockpos);
        serverlevel.setBlockAndUpdate(blockpos2, Blocks.LECTERN.defaultBlockState().rotate(p_177739_.getRotation()));
        BlockState blockstate = serverlevel.getBlockState(blockpos2);
        ItemStack itemstack = createBook(p_177739_.getTestName(), p_177739_.isRequired(), p_177740_);
        LecternBlock.tryPlaceBook(null, serverlevel, blockpos2, blockstate, itemstack);
    }

    private static ItemStack createBook(String p_177711_, boolean p_177712_, String p_177713_) {
        StringBuffer stringbuffer = new StringBuffer();
        Arrays.stream(p_177711_.split("\\.")).forEach(p_177716_ -> stringbuffer.append(p_177716_).append('\n'));
        if (!p_177712_) {
            stringbuffer.append("(optional)\n");
        }

        stringbuffer.append("-------------------\n");
        ItemStack itemstack = new ItemStack(Items.WRITABLE_BOOK);
        itemstack.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent(List.of(Filterable.passThrough(stringbuffer + p_177713_))));
        return itemstack;
    }

    protected static void say(ServerLevel p_177701_, ChatFormatting p_177702_, String p_177703_) {
        p_177701_.getPlayers(p_177705_ -> true).forEach(p_177709_ -> p_177709_.sendSystemMessage(Component.literal(p_177703_).withStyle(p_177702_)));
    }

    private static void showRedBox(ServerLevel p_177697_, BlockPos p_177698_, String p_177699_) {
        DebugPackets.sendGameTestAddMarker(p_177697_, p_177698_, p_177699_, -2130771968, Integer.MAX_VALUE);
    }
}