package net.minecraft.gametest.framework;

import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;

public class TestFinder<T> implements StructureBlockPosFinder, TestFunctionFinder {
    static final TestFunctionFinder NO_FUNCTIONS = Stream::empty;
    static final StructureBlockPosFinder NO_STRUCTURES = Stream::empty;
    private final TestFunctionFinder testFunctionFinder;
    private final StructureBlockPosFinder structureBlockPosFinder;
    private final CommandSourceStack source;
    private final Function<TestFinder<T>, T> contextProvider;

    @Override
    public Stream<BlockPos> findStructureBlockPos() {
        return this.structureBlockPosFinder.findStructureBlockPos();
    }

    TestFinder(CommandSourceStack p_332130_, Function<TestFinder<T>, T> p_332876_, TestFunctionFinder p_330000_, StructureBlockPosFinder p_332515_) {
        this.source = p_332130_;
        this.contextProvider = p_332876_;
        this.testFunctionFinder = p_330000_;
        this.structureBlockPosFinder = p_332515_;
    }

    T get() {
        return this.contextProvider.apply(this);
    }

    public CommandSourceStack source() {
        return this.source;
    }

    @Override
    public Stream<TestFunction> findTestFunctions() {
        return this.testFunctionFinder.findTestFunctions();
    }

    public static class Builder<T> {
        private final Function<TestFinder<T>, T> contextProvider;
        private final UnaryOperator<Supplier<Stream<TestFunction>>> testFunctionFinderWrapper;
        private final UnaryOperator<Supplier<Stream<BlockPos>>> structureBlockPosFinderWrapper;

        public Builder(Function<TestFinder<T>, T> p_329391_) {
            this.contextProvider = p_329391_;
            this.testFunctionFinderWrapper = p_333647_ -> p_333647_;
            this.structureBlockPosFinderWrapper = p_327811_ -> p_327811_;
        }

        private Builder(
            Function<TestFinder<T>, T> p_329078_, UnaryOperator<Supplier<Stream<TestFunction>>> p_334250_, UnaryOperator<Supplier<Stream<BlockPos>>> p_334300_
        ) {
            this.contextProvider = p_329078_;
            this.testFunctionFinderWrapper = p_334250_;
            this.structureBlockPosFinderWrapper = p_334300_;
        }

        public TestFinder.Builder<T> createMultipleCopies(int p_329806_) {
            return new TestFinder.Builder<>(this.contextProvider, createCopies(p_329806_), createCopies(p_329806_));
        }

        private static <Q> UnaryOperator<Supplier<Stream<Q>>> createCopies(int p_334571_) {
            return p_333976_ -> {
                List<Q> list = new LinkedList<>();
                List<Q> list1 = ((Stream)p_333976_.get()).toList();

                for (int i = 0; i < p_334571_; i++) {
                    list.addAll(list1);
                }

                return list::stream;
            };
        }

        private T build(CommandSourceStack p_334153_, TestFunctionFinder p_330203_, StructureBlockPosFinder p_328202_) {
            return new TestFinder<>(p_334153_, this.contextProvider, this.testFunctionFinderWrapper.apply(p_330203_::findTestFunctions)::get, this.structureBlockPosFinderWrapper.apply(p_328202_::findStructureBlockPos)::get)
                .get();
        }

        public T radius(CommandContext<CommandSourceStack> p_330481_, int p_334173_) {
            CommandSourceStack commandsourcestack = p_330481_.getSource();
            BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
            return this.build(commandsourcestack, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findStructureBlocks(blockpos, p_334173_, commandsourcestack.getLevel()));
        }

        public T nearest(CommandContext<CommandSourceStack> p_332654_) {
            CommandSourceStack commandsourcestack = p_332654_.getSource();
            BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
            return this.build(
                commandsourcestack, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findNearestStructureBlock(blockpos, 15, commandsourcestack.getLevel()).stream()
            );
        }

        public T allNearby(CommandContext<CommandSourceStack> p_335428_) {
            CommandSourceStack commandsourcestack = p_335428_.getSource();
            BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
            return this.build(commandsourcestack, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findStructureBlocks(blockpos, 200, commandsourcestack.getLevel()));
        }

        public T lookedAt(CommandContext<CommandSourceStack> p_328071_) {
            CommandSourceStack commandsourcestack = p_328071_.getSource();
            return this.build(
                commandsourcestack,
                TestFinder.NO_FUNCTIONS,
                () -> StructureUtils.lookedAtStructureBlockPos(
                        BlockPos.containing(commandsourcestack.getPosition()), commandsourcestack.getPlayer().getCamera(), commandsourcestack.getLevel()
                    )
            );
        }

        public T allTests(CommandContext<CommandSourceStack> p_331369_) {
            return this.build(
                p_331369_.getSource(), () -> GameTestRegistry.getAllTestFunctions().stream().filter(p_334467_ -> !p_334467_.manualOnly()), TestFinder.NO_STRUCTURES
            );
        }

        public T allTestsInClass(CommandContext<CommandSourceStack> p_333766_, String p_332600_) {
            return this.build(
                p_333766_.getSource(), () -> GameTestRegistry.getTestFunctionsForClassName(p_332600_).filter(p_328668_ -> !p_328668_.manualOnly()), TestFinder.NO_STRUCTURES
            );
        }

        public T failedTests(CommandContext<CommandSourceStack> p_332736_, boolean p_336399_) {
            return this.build(
                p_332736_.getSource(), () -> GameTestRegistry.getLastFailedTests().filter(p_328598_ -> !p_336399_ || p_328598_.required()), TestFinder.NO_STRUCTURES
            );
        }

        public T byArgument(CommandContext<CommandSourceStack> p_329167_, String p_334913_) {
            return this.build(p_329167_.getSource(), () -> Stream.of(TestFunctionArgument.getTestFunction(p_329167_, p_334913_)), TestFinder.NO_STRUCTURES);
        }

        public T locateByName(CommandContext<CommandSourceStack> p_330730_, String p_336390_) {
            CommandSourceStack commandsourcestack = p_330730_.getSource();
            BlockPos blockpos = BlockPos.containing(commandsourcestack.getPosition());
            return this.build(
                commandsourcestack, TestFinder.NO_FUNCTIONS, () -> StructureUtils.findStructureByTestFunction(blockpos, 1024, commandsourcestack.getLevel(), p_336390_)
            );
        }

        public T failedTests(CommandContext<CommandSourceStack> p_331687_) {
            return this.failedTests(p_331687_, false);
        }
    }
}