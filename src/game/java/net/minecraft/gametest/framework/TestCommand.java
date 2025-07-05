package net.minecraft.gametest.framework;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class TestCommand {
    public static final int STRUCTURE_BLOCK_NEARBY_SEARCH_RADIUS = 15;
    public static final int STRUCTURE_BLOCK_FULL_SEARCH_RADIUS = 200;
    public static final int VERIFY_TEST_GRID_AXIS_SIZE = 10;
    public static final int VERIFY_TEST_BATCH_SIZE = 100;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_CLEAR_RADIUS = 200;
    private static final int MAX_CLEAR_RADIUS = 1024;
    private static final int TEST_POS_Z_OFFSET_FROM_PLAYER = 3;
    private static final int SHOW_POS_DURATION_MS = 10000;
    private static final int DEFAULT_X_SIZE = 5;
    private static final int DEFAULT_Y_SIZE = 5;
    private static final int DEFAULT_Z_SIZE = 5;
    private static final String STRUCTURE_BLOCK_ENTITY_COULD_NOT_BE_FOUND = "Structure block entity could not be found";
    private static final TestFinder.Builder<TestCommand.Runner> testFinder = new TestFinder.Builder<>(TestCommand.Runner::new);

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
        ArgumentBuilder<CommandSourceStack, ?> p_331571_,
        Function<CommandContext<CommandSourceStack>, TestCommand.Runner> p_335923_,
        Function<ArgumentBuilder<CommandSourceStack, ?>, ArgumentBuilder<CommandSourceStack, ?>> p_333739_
    ) {
        return p_331571_.executes(p_325991_ -> p_335923_.apply(p_325991_).run())
            .then(
                Commands.argument("numberOfTimes", IntegerArgumentType.integer(0))
                    .executes(
                        p_325975_ -> p_335923_.apply(p_325975_).run(new RetryOptions(IntegerArgumentType.getInteger(p_325975_, "numberOfTimes"), false))
                    )
                    .then(
                        p_333739_.apply(
                            Commands.argument("untilFailed", BoolArgumentType.bool())
                                .executes(
                                    p_325980_ -> p_335923_.apply(p_325980_)
                                            .run(
                                                new RetryOptions(
                                                    IntegerArgumentType.getInteger(p_325980_, "numberOfTimes"),
                                                    BoolArgumentType.getBool(p_325980_, "untilFailed")
                                                )
                                            )
                                )
                        )
                    )
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptions(
        ArgumentBuilder<CommandSourceStack, ?> p_335642_, Function<CommandContext<CommandSourceStack>, TestCommand.Runner> p_330546_
    ) {
        return runWithRetryOptions(p_335642_, p_330546_, p_325997_ -> p_325997_);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> runWithRetryOptionsAndBuildInfo(
        ArgumentBuilder<CommandSourceStack, ?> p_328748_, Function<CommandContext<CommandSourceStack>, TestCommand.Runner> p_328595_
    ) {
        return runWithRetryOptions(
            p_328748_,
            p_328595_,
            p_325993_ -> p_325993_.then(
                    Commands.argument("rotationSteps", IntegerArgumentType.integer())
                        .executes(
                            p_326001_ -> p_328595_.apply(p_326001_)
                                    .run(
                                        new RetryOptions(
                                            IntegerArgumentType.getInteger(p_326001_, "numberOfTimes"), BoolArgumentType.getBool(p_326001_, "untilFailed")
                                        ),
                                        IntegerArgumentType.getInteger(p_326001_, "rotationSteps")
                                    )
                        )
                        .then(
                            Commands.argument("testsPerRow", IntegerArgumentType.integer())
                                .executes(
                                    p_325977_ -> p_328595_.apply(p_325977_)
                                            .run(
                                                new RetryOptions(
                                                    IntegerArgumentType.getInteger(p_325977_, "numberOfTimes"),
                                                    BoolArgumentType.getBool(p_325977_, "untilFailed")
                                                ),
                                                IntegerArgumentType.getInteger(p_325977_, "rotationSteps"),
                                                IntegerArgumentType.getInteger(p_325977_, "testsPerRow")
                                            )
                                )
                        )
                )
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> p_127947_) {
        ArgumentBuilder<CommandSourceStack, ?> argumentbuilder = runWithRetryOptionsAndBuildInfo(
            Commands.argument("onlyRequiredTests", BoolArgumentType.bool()),
            p_326015_ -> testFinder.failedTests(p_326015_, BoolArgumentType.getBool(p_326015_, "onlyRequiredTests"))
        );
        ArgumentBuilder<CommandSourceStack, ?> argumentbuilder1 = runWithRetryOptionsAndBuildInfo(
            Commands.argument("testClassName", TestClassNameArgument.testClassName()),
            p_325999_ -> testFinder.allTestsInClass(p_325999_, TestClassNameArgument.getTestClassName(p_325999_, "testClassName"))
        );
        p_127947_.register(
            Commands.literal("test")
                .then(
                    Commands.literal("run")
                        .then(
                            runWithRetryOptionsAndBuildInfo(Commands.argument("testName", TestFunctionArgument.testFunctionArgument()), p_325988_ -> testFinder.byArgument(p_325988_, "testName"))
                        )
                )
                .then(
                    Commands.literal("runmultiple")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(p_325973_ -> testFinder.byArgument(p_325973_, "testName").run())
                                .then(
                                    Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(
                                            p_325995_ -> testFinder.createMultipleCopies(IntegerArgumentType.getInteger(p_325995_, "amount"))
                                                    .byArgument(p_325995_, "testName")
                                                    .run()
                                        )
                                )
                        )
                )
                .then(runWithRetryOptionsAndBuildInfo(Commands.literal("runall").then(argumentbuilder1), testFinder::allTests))
                .then(runWithRetryOptions(Commands.literal("runthese"), testFinder::allNearby))
                .then(runWithRetryOptions(Commands.literal("runclosest"), testFinder::nearest))
                .then(runWithRetryOptions(Commands.literal("runthat"), testFinder::lookedAt))
                .then(runWithRetryOptionsAndBuildInfo(Commands.literal("runfailed").then(argumentbuilder), testFinder::failedTests))
                .then(
                    Commands.literal("verify")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(p_341098_ -> testFinder.byArgument(p_341098_, "testName").verify())
                        )
                )
                .then(
                    Commands.literal("verifyclass")
                        .then(
                            Commands.argument("testClassName", TestClassNameArgument.testClassName())
                                .executes(p_341097_ -> testFinder.allTestsInClass(p_341097_, TestClassNameArgument.getTestClassName(p_341097_, "testClassName")).verify())
                        )
                )
                .then(
                    Commands.literal("locate")
                        .then(
                            Commands.argument("testName", TestFunctionArgument.testFunctionArgument())
                                .executes(
                                    p_325985_ -> testFinder.locateByName(
                                                p_325985_, "minecraft:" + TestFunctionArgument.getTestFunction(p_325985_, "testName").structureName()
                                            )
                                            .locate()
                                )
                        )
                )
                .then(Commands.literal("resetclosest").executes(p_325984_ -> testFinder.nearest(p_325984_).reset()))
                .then(Commands.literal("resetthese").executes(p_325994_ -> testFinder.allNearby(p_325994_).reset()))
                .then(Commands.literal("resetthat").executes(p_325983_ -> testFinder.lookedAt(p_325983_).reset()))
                .then(
                    Commands.literal("export")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(p_325998_ -> exportTestStructure(p_325998_.getSource(), "minecraft:" + StringArgumentType.getString(p_325998_, "testName")))
                        )
                )
                .then(Commands.literal("exportclosest").executes(p_326009_ -> testFinder.nearest(p_326009_).export()))
                .then(Commands.literal("exportthese").executes(p_326010_ -> testFinder.allNearby(p_326010_).export()))
                .then(Commands.literal("exportthat").executes(p_326011_ -> testFinder.lookedAt(p_326011_).export()))
                .then(Commands.literal("clearthat").executes(p_325987_ -> testFinder.lookedAt(p_325987_).clear()))
                .then(Commands.literal("clearthese").executes(p_325978_ -> testFinder.allNearby(p_325978_).clear()))
                .then(
                    Commands.literal("clearall")
                        .executes(p_325986_ -> testFinder.radius(p_325986_, 200).clear())
                        .then(
                            Commands.argument("radius", IntegerArgumentType.integer())
                                .executes(
                                    p_325996_ -> testFinder.radius(p_325996_, Mth.clamp(IntegerArgumentType.getInteger(p_325996_, "radius"), 0, 1024))
                                            .clear()
                                )
                        )
                )
                .then(
                    Commands.literal("import")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .executes(p_128025_ -> importTestStructure(p_128025_.getSource(), StringArgumentType.getString(p_128025_, "testName")))
                        )
                )
                .then(Commands.literal("stop").executes(p_326006_ -> stopTests()))
                .then(
                    Commands.literal("pos")
                        .executes(p_128023_ -> showPos(p_128023_.getSource(), "pos"))
                        .then(
                            Commands.argument("var", StringArgumentType.word())
                                .executes(p_128021_ -> showPos(p_128021_.getSource(), StringArgumentType.getString(p_128021_, "var")))
                        )
                )
                .then(
                    Commands.literal("create")
                        .then(
                            Commands.argument("testName", StringArgumentType.word())
                                .suggests(TestFunctionArgument::suggestTestFunction)
                                .executes(p_128019_ -> createNewStructure(p_128019_.getSource(), StringArgumentType.getString(p_128019_, "testName"), 5, 5, 5))
                                .then(
                                    Commands.argument("width", IntegerArgumentType.integer())
                                        .executes(
                                            p_128014_ -> createNewStructure(
                                                    p_128014_.getSource(),
                                                    StringArgumentType.getString(p_128014_, "testName"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width"),
                                                    IntegerArgumentType.getInteger(p_128014_, "width")
                                                )
                                        )
                                        .then(
                                            Commands.argument("height", IntegerArgumentType.integer())
                                                .then(
                                                    Commands.argument("depth", IntegerArgumentType.integer())
                                                        .executes(
                                                            p_128007_ -> createNewStructure(
                                                                    p_128007_.getSource(),
                                                                    StringArgumentType.getString(p_128007_, "testName"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "width"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "height"),
                                                                    IntegerArgumentType.getInteger(p_128007_, "depth")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int resetGameTestInfo(GameTestInfo p_331593_) {
        p_331593_.getLevel().getEntities(null, p_331593_.getStructureBounds()).stream().forEach(p_325989_ -> p_325989_.remove(Entity.RemovalReason.DISCARDED));
        p_331593_.getStructureBlockEntity().placeStructure(p_331593_.getLevel());
        StructureUtils.removeBarriers(p_331593_.getStructureBounds(), p_331593_.getLevel());
        say(p_331593_.getLevel(), "Reset succeded for: " + p_331593_.getTestName(), ChatFormatting.GREEN);
        return 1;
    }

    static Stream<GameTestInfo> toGameTestInfos(CommandSourceStack p_329247_, RetryOptions p_336246_, StructureBlockPosFinder p_334897_) {
        return p_334897_.findStructureBlockPos().map(p_326014_ -> createGameTestInfo(p_326014_, p_329247_.getLevel(), p_336246_)).flatMap(Optional::stream);
    }

    static Stream<GameTestInfo> toGameTestInfo(CommandSourceStack p_330917_, RetryOptions p_332428_, TestFunctionFinder p_328880_, int p_327985_) {
        return p_328880_.findTestFunctions()
            .filter(p_326008_ -> verifyStructureExists(p_330917_.getLevel(), p_326008_.structureName()))
            .map(p_326005_ -> new GameTestInfo(p_326005_, StructureUtils.getRotationForRotationSteps(p_327985_), p_330917_.getLevel(), p_332428_));
    }

    private static Optional<GameTestInfo> createGameTestInfo(BlockPos p_332856_, ServerLevel p_328153_, RetryOptions p_330368_) {
        StructureBlockEntity structureblockentity = (StructureBlockEntity)p_328153_.getBlockEntity(p_332856_);
        if (structureblockentity == null) {
            say(p_328153_, "Structure block entity could not be found", ChatFormatting.RED);
            return Optional.empty();
        } else {
            String s = structureblockentity.getMetaData();
            Optional<TestFunction> optional = GameTestRegistry.findTestFunction(s);
            if (optional.isEmpty()) {
                say(p_328153_, "Test function for test " + s + " could not be found", ChatFormatting.RED);
                return Optional.empty();
            } else {
                TestFunction testfunction = optional.get();
                GameTestInfo gametestinfo = new GameTestInfo(testfunction, structureblockentity.getRotation(), p_328153_, p_330368_);
                gametestinfo.setStructureBlockPos(p_332856_);
                return !verifyStructureExists(p_328153_, gametestinfo.getStructureName()) ? Optional.empty() : Optional.of(gametestinfo);
            }
        }
    }

    private static int createNewStructure(CommandSourceStack p_127968_, String p_127969_, int p_127970_, int p_127971_, int p_127972_) {
        if (p_127970_ <= 48 && p_127971_ <= 48 && p_127972_ <= 48) {
            ServerLevel serverlevel = p_127968_.getLevel();
            BlockPos blockpos = createTestPositionAround(p_127968_).below();
            StructureUtils.createNewEmptyStructureBlock(p_127969_.toLowerCase(), blockpos, new Vec3i(p_127970_, p_127971_, p_127972_), Rotation.NONE, serverlevel);
            BlockPos blockpos1 = blockpos.above();
            BlockPos blockpos2 = blockpos1.offset(p_127970_ - 1, 0, p_127972_ - 1);
            BlockPos.betweenClosedStream(blockpos1, blockpos2).forEach(p_325982_ -> serverlevel.setBlockAndUpdate(p_325982_, Blocks.BEDROCK.defaultBlockState()));
            StructureUtils.addCommandBlockAndButtonToStartTest(blockpos, new BlockPos(1, 0, -1), Rotation.NONE, serverlevel);
            return 0;
        } else {
            throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
        }
    }

    private static int showPos(CommandSourceStack p_127960_, String p_127961_) throws CommandSyntaxException {
        BlockHitResult blockhitresult = (BlockHitResult)p_127960_.getPlayerOrException().pick(10.0, 1.0F, false);
        BlockPos blockpos = blockhitresult.getBlockPos();
        ServerLevel serverlevel = p_127960_.getLevel();
        Optional<BlockPos> optional = StructureUtils.findStructureBlockContainingPos(blockpos, 15, serverlevel);
        if (optional.isEmpty()) {
            optional = StructureUtils.findStructureBlockContainingPos(blockpos, 200, serverlevel);
        }

        if (optional.isEmpty()) {
            p_127960_.sendFailure(Component.literal("Can't find a structure block that contains the targeted pos " + blockpos));
            return 0;
        } else {
            StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(optional.get());
            if (structureblockentity == null) {
                say(serverlevel, "Structure block entity could not be found", ChatFormatting.RED);
                return 0;
            } else {
                BlockPos blockpos1 = blockpos.subtract(optional.get());
                String s = blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ();
                String s1 = structureblockentity.getMetaData();
                Component component = Component.literal(s)
                    .setStyle(
                        Style.EMPTY
                            .withBold(true)
                            .withColor(ChatFormatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy to clipboard")))
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + p_127961_ + " = new BlockPos(" + s + ");"))
                    );
                p_127960_.sendSuccess(() -> Component.literal("Position relative to " + s1 + ": ").append(component), false);
                DebugPackets.sendGameTestAddMarker(serverlevel, new BlockPos(blockpos), s, -2147418368, 10000);
                return 1;
            }
        }
    }

    static int stopTests() {
        GameTestTicker.SINGLETON.clear();
        return 1;
    }

    static int trackAndStartRunner(CommandSourceStack p_333535_, ServerLevel p_333108_, GameTestRunner p_333430_) {
        p_333430_.addListener(new TestCommand.TestBatchSummaryDisplayer(p_333535_));
        MultipleTestTracker multipletesttracker = new MultipleTestTracker(p_333430_.getTestInfos());
        multipletesttracker.addListener(new TestCommand.TestSummaryDisplayer(p_333108_, multipletesttracker));
        multipletesttracker.addFailureListener(p_127992_ -> GameTestRegistry.rememberFailedTest(p_127992_.getTestFunction()));
        p_333430_.start();
        return 1;
    }

    static int saveAndExportTestStructure(CommandSourceStack p_309467_, StructureBlockEntity p_310131_) {
        String s = p_310131_.getStructureName();
        if (!p_310131_.saveStructure(true)) {
            say(p_309467_, "Failed to save structure " + s);
        }

        return exportTestStructure(p_309467_, s);
    }

    private static int exportTestStructure(CommandSourceStack p_128011_, String p_128012_) {
        Path path = Paths.get(StructureUtils.testStructuresDir);
        ResourceLocation resourcelocation = ResourceLocation.parse(p_128012_);
        Path path1 = p_128011_.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(resourcelocation, ".nbt");
        Path path2 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path1, resourcelocation.getPath(), path);
        if (path2 == null) {
            say(p_128011_, "Failed to export " + path1);
            return 1;
        } else {
            try {
                FileUtil.createDirectoriesSafe(path2.getParent());
            } catch (IOException ioexception) {
                say(p_128011_, "Could not create folder " + path2.getParent());
                LOGGER.error("Could not create export folder", (Throwable)ioexception);
                return 1;
            }

            say(p_128011_, "Exported " + p_128012_ + " to " + path2.toAbsolutePath());
            return 0;
        }
    }

    private static boolean verifyStructureExists(ServerLevel p_310841_, String p_330426_) {
        if (p_310841_.getStructureManager().get(ResourceLocation.parse(p_330426_)).isEmpty()) {
            say(p_310841_, "Test structure " + p_330426_ + " could not be found", ChatFormatting.RED);
            return false;
        } else {
            return true;
        }
    }

    static BlockPos createTestPositionAround(CommandSourceStack p_313084_) {
        BlockPos blockpos = BlockPos.containing(p_313084_.getPosition());
        int i = p_313084_.getLevel().getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockpos).getY();
        return new BlockPos(blockpos.getX(), i + 1, blockpos.getZ() + 3);
    }

    static void say(CommandSourceStack p_128004_, String p_128005_) {
        p_128004_.sendSuccess(() -> Component.literal(p_128005_), false);
    }

    private static int importTestStructure(CommandSourceStack p_128016_, String p_128017_) {
        Path path = Paths.get(StructureUtils.testStructuresDir, p_128017_ + ".snbt");
        ResourceLocation resourcelocation = ResourceLocation.withDefaultNamespace(p_128017_);
        Path path1 = p_128016_.getLevel().getStructureManager().createAndValidatePathToGeneratedStructure(resourcelocation, ".nbt");

        try {
            BufferedReader bufferedreader = Files.newBufferedReader(path);
            String s = IOUtils.toString(bufferedreader);
            Files.createDirectories(path1.getParent());

            try (OutputStream outputstream = Files.newOutputStream(path1)) {
                NbtIo.writeCompressed(NbtUtils.snbtToStructure(s), outputstream);
            }

            p_128016_.getLevel().getStructureManager().remove(resourcelocation);
            say(p_128016_, "Imported to " + path1.toAbsolutePath());
            return 0;
        } catch (CommandSyntaxException | IOException ioexception) {
            LOGGER.error("Failed to load structure {}", p_128017_, ioexception);
            return 1;
        }
    }

    static void say(ServerLevel p_127934_, String p_127935_, ChatFormatting p_127936_) {
        p_127934_.getPlayers(p_127945_ -> true).forEach(p_308546_ -> p_308546_.sendSystemMessage(Component.literal(p_127935_).withStyle(p_127936_)));
    }

    public static class Runner {
        private final TestFinder<TestCommand.Runner> finder;

        public Runner(TestFinder<TestCommand.Runner> p_330629_) {
            this.finder = p_330629_;
        }

        public int reset() {
            TestCommand.stopTests();
            return TestCommand.toGameTestInfos(this.finder.source(), RetryOptions.noRetries(), this.finder).map(TestCommand::resetGameTestInfo).toList().isEmpty()
                ? 0
                : 1;
        }

        private <T> void logAndRun(Stream<T> p_331509_, ToIntFunction<T> p_328365_, Runnable p_334945_, Consumer<Integer> p_335243_) {
            int i = p_331509_.mapToInt(p_328365_).sum();
            if (i == 0) {
                p_334945_.run();
            } else {
                p_335243_.accept(i);
            }
        }

        public int clear() {
            TestCommand.stopTests();
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            GameTestRunner.clearMarkers(serverlevel);
            this.logAndRun(
                this.finder.findStructureBlockPos(),
                p_330831_ -> {
                    StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(p_330831_);
                    if (structureblockentity == null) {
                        return 0;
                    } else {
                        BoundingBox boundingbox = StructureUtils.getStructureBoundingBox(structureblockentity);
                        StructureUtils.clearSpaceForStructure(boundingbox, serverlevel);
                        return 1;
                    }
                },
                () -> TestCommand.say(serverlevel, "Could not find any structures to clear", ChatFormatting.RED),
                p_330244_ -> TestCommand.say(commandsourcestack, "Cleared " + p_330244_ + " structures")
            );
            return 1;
        }

        public int export() {
            MutableBoolean mutableboolean = new MutableBoolean(true);
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            this.logAndRun(
                this.finder.findStructureBlockPos(),
                p_331429_ -> {
                    StructureBlockEntity structureblockentity = (StructureBlockEntity)serverlevel.getBlockEntity(p_331429_);
                    if (structureblockentity == null) {
                        TestCommand.say(serverlevel, "Structure block entity could not be found", ChatFormatting.RED);
                        mutableboolean.setFalse();
                        return 0;
                    } else {
                        if (TestCommand.saveAndExportTestStructure(commandsourcestack, structureblockentity) != 0) {
                            mutableboolean.setFalse();
                        }

                        return 1;
                    }
                },
                () -> TestCommand.say(serverlevel, "Could not find any structures to export", ChatFormatting.RED),
                p_333553_ -> TestCommand.say(commandsourcestack, "Exported " + p_333553_ + " structures")
            );
            return mutableboolean.getValue() ? 0 : 1;
        }

        int verify() {
            TestCommand.stopTests();
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            BlockPos blockpos = TestCommand.createTestPositionAround(commandsourcestack);
            Collection<GameTestInfo> collection = Stream.concat(
                    TestCommand.toGameTestInfos(commandsourcestack, RetryOptions.noRetries(), this.finder),
                    TestCommand.toGameTestInfo(commandsourcestack, RetryOptions.noRetries(), this.finder, 0)
                )
                .toList();
            GameTestRunner.clearMarkers(serverlevel);
            GameTestRegistry.forgetFailedTests();
            Collection<GameTestBatch> collection1 = new ArrayList<>();

            for (GameTestInfo gametestinfo : collection) {
                for (Rotation rotation : Rotation.values()) {
                    Collection<GameTestInfo> collection2 = new ArrayList<>();

                    for (int i = 0; i < 100; i++) {
                        GameTestInfo gametestinfo1 = new GameTestInfo(gametestinfo.getTestFunction(), rotation, serverlevel, new RetryOptions(1, true));
                        collection2.add(gametestinfo1);
                    }

                    GameTestBatch gametestbatch = GameTestBatchFactory.toGameTestBatch(collection2, gametestinfo.getTestFunction().batchName(), (long)rotation.ordinal());
                    collection1.add(gametestbatch);
                }
            }

            StructureGridSpawner structuregridspawner = new StructureGridSpawner(blockpos, 10, true);
            GameTestRunner gametestrunner = GameTestRunner.Builder.fromBatches(collection1, serverlevel)
                .batcher(GameTestBatchFactory.fromGameTestInfo(100))
                .newStructureSpawner(structuregridspawner)
                .existingStructureSpawner(structuregridspawner)
                .haltOnError(true)
                .build();
            return TestCommand.trackAndStartRunner(commandsourcestack, serverlevel, gametestrunner);
        }

        public int run(RetryOptions p_334797_, int p_327669_, int p_333611_) {
            TestCommand.stopTests();
            CommandSourceStack commandsourcestack = this.finder.source();
            ServerLevel serverlevel = commandsourcestack.getLevel();
            BlockPos blockpos = TestCommand.createTestPositionAround(commandsourcestack);
            Collection<GameTestInfo> collection = Stream.concat(
                    TestCommand.toGameTestInfos(commandsourcestack, p_334797_, this.finder),
                    TestCommand.toGameTestInfo(commandsourcestack, p_334797_, this.finder, p_327669_)
                )
                .toList();
            if (collection.isEmpty()) {
                TestCommand.say(commandsourcestack, "No tests found");
                return 0;
            } else {
                GameTestRunner.clearMarkers(serverlevel);
                GameTestRegistry.forgetFailedTests();
                TestCommand.say(commandsourcestack, "Running " + collection.size() + " tests...");
                GameTestRunner gametestrunner = GameTestRunner.Builder.fromInfo(collection, serverlevel)
                    .newStructureSpawner(new StructureGridSpawner(blockpos, p_333611_, false))
                    .build();
                return TestCommand.trackAndStartRunner(commandsourcestack, serverlevel, gametestrunner);
            }
        }

        public int run(int p_333354_, int p_329165_) {
            return this.run(RetryOptions.noRetries(), p_333354_, p_329165_);
        }

        public int run(int p_333969_) {
            return this.run(RetryOptions.noRetries(), p_333969_, 8);
        }

        public int run(RetryOptions p_328161_, int p_330365_) {
            return this.run(p_328161_, p_330365_, 8);
        }

        public int run(RetryOptions p_329766_) {
            return this.run(p_329766_, 0, 8);
        }

        public int run() {
            return this.run(RetryOptions.noRetries());
        }

        public int locate() {
            TestCommand.say(this.finder.source(), "Started locating test structures, this might take a while..");
            MutableInt mutableint = new MutableInt(0);
            BlockPos blockpos = BlockPos.containing(this.finder.source().getPosition());
            this.finder
                .findStructureBlockPos()
                .forEach(
                    p_327721_ -> {
                        StructureBlockEntity structureblockentity = (StructureBlockEntity)this.finder.source().getLevel().getBlockEntity(p_327721_);
                        if (structureblockentity != null) {
                            Direction direction = structureblockentity.getRotation().rotate(Direction.NORTH);
                            BlockPos blockpos1 = structureblockentity.getBlockPos().relative(direction, 2);
                            int j = (int)direction.getOpposite().toYRot();
                            String s = String.format("/tp @s %d %d %d %d 0", blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), j);
                            int k = blockpos.getX() - p_327721_.getX();
                            int l = blockpos.getZ() - p_327721_.getZ();
                            int i1 = Mth.floor(Mth.sqrt((float)(k * k + l * l)));
                            Component component = ComponentUtils.wrapInSquareBrackets(
                                    Component.translatable("chat.coordinates", p_327721_.getX(), p_327721_.getY(), p_327721_.getZ())
                                )
                                .withStyle(
                                    p_332540_ -> p_332540_.withColor(ChatFormatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, s))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                                );
                            Component component1 = Component.literal("Found structure at: ").append(component).append(" (distance: " + i1 + ")");
                            this.finder.source().sendSuccess(() -> component1, false);
                            mutableint.increment();
                        }
                    }
                );
            int i = mutableint.intValue();
            if (i == 0) {
                TestCommand.say(this.finder.source().getLevel(), "No such test structure found", ChatFormatting.RED);
                return 0;
            } else {
                TestCommand.say(this.finder.source().getLevel(), "Finished locating, found " + i + " structure(s)", ChatFormatting.GREEN);
                return 1;
            }
        }
    }

    static record TestBatchSummaryDisplayer(CommandSourceStack source) implements GameTestBatchListener {
        @Override
        public void testBatchStarting(GameTestBatch p_327831_) {
            TestCommand.say(this.source, "Starting batch: " + p_327831_.name());
        }

        @Override
        public void testBatchFinished(GameTestBatch p_335734_) {
        }
    }

    public static record TestSummaryDisplayer(ServerLevel level, MultipleTestTracker tracker) implements GameTestListener {
        @Override
        public void testStructureLoaded(GameTestInfo p_128064_) {
        }

        @Override
        public void testPassed(GameTestInfo p_177797_, GameTestRunner p_333026_) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testFailed(GameTestInfo p_128066_, GameTestRunner p_333809_) {
            showTestSummaryIfAllDone(this.level, this.tracker);
        }

        @Override
        public void testAddedForRerun(GameTestInfo p_328539_, GameTestInfo p_335500_, GameTestRunner p_328503_) {
            this.tracker.addTestToTrack(p_335500_);
        }

        private static void showTestSummaryIfAllDone(ServerLevel p_329959_, MultipleTestTracker p_331168_) {
            if (p_331168_.isDone()) {
                TestCommand.say(p_329959_, "GameTest done! " + p_331168_.getTotalCount() + " tests were run", ChatFormatting.WHITE);
                if (p_331168_.hasFailedRequired()) {
                    TestCommand.say(p_329959_, p_331168_.getFailedRequiredCount() + " required tests failed :(", ChatFormatting.RED);
                } else {
                    TestCommand.say(p_329959_, "All required tests passed :)", ChatFormatting.GREEN);
                }

                if (p_331168_.hasFailedOptional()) {
                    TestCommand.say(p_329959_, p_331168_.getFailedOptionalCount() + " optional tests failed", ChatFormatting.GRAY);
                }
            }
        }
    }
}