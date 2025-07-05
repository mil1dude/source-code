package net.minecraft.gametest.framework;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class StructureUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DEFAULT_Y_SEARCH_RADIUS = 10;
    public static final String DEFAULT_TEST_STRUCTURES_DIR = "gameteststructures";
    public static String testStructuresDir = "gameteststructures";

    public static Rotation getRotationForRotationSteps(int p_127836_) {
        switch (p_127836_) {
            case 0:
                return Rotation.NONE;
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + p_127836_);
        }
    }

    public static int getRotationStepsForRotation(Rotation p_177752_) {
        switch (p_177752_) {
            case NONE:
                return 0;
            case CLOCKWISE_90:
                return 1;
            case CLOCKWISE_180:
                return 2;
            case COUNTERCLOCKWISE_90:
                return 3;
            default:
                throw new IllegalArgumentException("Unknown rotation value, don't know how many steps it represents: " + p_177752_);
        }
    }

    public static AABB getStructureBounds(StructureBlockEntity p_127848_) {
        return AABB.of(getStructureBoundingBox(p_127848_));
    }

    public static BoundingBox getStructureBoundingBox(StructureBlockEntity p_127905_) {
        BlockPos blockpos = getStructureOrigin(p_127905_);
        BlockPos blockpos1 = getTransformedFarCorner(blockpos, p_127905_.getStructureSize(), p_127905_.getRotation());
        return BoundingBox.fromCorners(blockpos, blockpos1);
    }

    public static BlockPos getStructureOrigin(StructureBlockEntity p_311311_) {
        return p_311311_.getBlockPos().offset(p_311311_.getStructurePos());
    }

    public static void addCommandBlockAndButtonToStartTest(BlockPos p_127876_, BlockPos p_127877_, Rotation p_127878_, ServerLevel p_127879_) {
        BlockPos blockpos = StructureTemplate.transform(p_127876_.offset(p_127877_), Mirror.NONE, p_127878_, p_127876_);
        p_127879_.setBlockAndUpdate(blockpos, Blocks.COMMAND_BLOCK.defaultBlockState());
        CommandBlockEntity commandblockentity = (CommandBlockEntity)p_127879_.getBlockEntity(blockpos);
        commandblockentity.getCommandBlock().setCommand("test runclosest");
        BlockPos blockpos1 = StructureTemplate.transform(blockpos.offset(0, 0, -1), Mirror.NONE, p_127878_, blockpos);
        p_127879_.setBlockAndUpdate(blockpos1, Blocks.STONE_BUTTON.defaultBlockState().rotate(p_127878_));
    }

    public static void createNewEmptyStructureBlock(String p_177765_, BlockPos p_177766_, Vec3i p_177767_, Rotation p_177768_, ServerLevel p_177769_) {
        BoundingBox boundingbox = getStructureBoundingBox(p_177766_.above(), p_177767_, p_177768_);
        clearSpaceForStructure(boundingbox, p_177769_);
        p_177769_.setBlockAndUpdate(p_177766_, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity structureblockentity = (StructureBlockEntity)p_177769_.getBlockEntity(p_177766_);
        structureblockentity.setIgnoreEntities(false);
        structureblockentity.setStructureName(ResourceLocation.parse(p_177765_));
        structureblockentity.setMetaData(p_177765_);
        structureblockentity.setStructureSize(p_177767_);
        structureblockentity.setMode(StructureMode.SAVE);
        structureblockentity.setShowBoundingBox(true);
    }

    public static BlockPos getStartCorner(GameTestInfo p_364513_, BlockPos p_364702_, Rotation p_360789_, ServerLevel p_360785_) {
        Vec3i vec3i = p_360785_.getStructureManager()
            .get(ResourceLocation.parse(p_364513_.getStructureName()))
            .orElseThrow(() -> new IllegalStateException("Missing test structure: " + p_364513_.getStructureName()))
            .getSize();
        BlockPos blockpos;
        if (p_360789_ == Rotation.NONE) {
            blockpos = p_364702_;
        } else if (p_360789_ == Rotation.CLOCKWISE_90) {
            blockpos = p_364702_.offset(vec3i.getZ() - 1, 0, 0);
        } else if (p_360789_ == Rotation.CLOCKWISE_180) {
            blockpos = p_364702_.offset(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
        } else {
            if (p_360789_ != Rotation.COUNTERCLOCKWISE_90) {
                throw new IllegalArgumentException("Invalid rotation: " + p_360789_);
            }

            blockpos = p_364702_.offset(0, 0, vec3i.getX() - 1);
        }

        return blockpos;
    }

    public static StructureBlockEntity prepareTestStructure(GameTestInfo p_311701_, BlockPos p_311042_, Rotation p_310584_, ServerLevel p_312330_) {
        Vec3i vec3i = p_312330_.getStructureManager()
            .get(ResourceLocation.parse(p_311701_.getStructureName()))
            .orElseThrow(() -> new IllegalStateException("Missing test structure: " + p_311701_.getStructureName()))
            .getSize();
        BoundingBox boundingbox = getStructureBoundingBox(p_311042_, vec3i, p_310584_);
        BlockPos blockpos = getStartCorner(p_311701_, p_311042_, p_310584_, p_312330_);
        forceLoadChunks(boundingbox, p_312330_);
        clearSpaceForStructure(boundingbox, p_312330_);
        return createStructureBlock(p_311701_, blockpos.below(), p_310584_, p_312330_);
    }

    public static void encaseStructure(AABB p_330422_, ServerLevel p_331249_, boolean p_328180_) {
        BlockPos blockpos = BlockPos.containing(p_330422_.minX, p_330422_.minY, p_330422_.minZ).offset(-1, 0, -1);
        BlockPos blockpos1 = BlockPos.containing(p_330422_.maxX, p_330422_.maxY, p_330422_.maxZ);
        BlockPos.betweenClosedStream(blockpos, blockpos1)
            .forEach(
                p_325964_ -> {
                    boolean flag = p_325964_.getX() == blockpos.getX()
                        || p_325964_.getX() == blockpos1.getX()
                        || p_325964_.getZ() == blockpos.getZ()
                        || p_325964_.getZ() == blockpos1.getZ();
                    boolean flag1 = p_325964_.getY() == blockpos1.getY();
                    if (flag || flag1 && p_328180_) {
                        p_331249_.setBlockAndUpdate(p_325964_, Blocks.BARRIER.defaultBlockState());
                    }
                }
            );
    }

    public static void removeBarriers(AABB p_336061_, ServerLevel p_334551_) {
        BlockPos blockpos = BlockPos.containing(p_336061_.minX, p_336061_.minY, p_336061_.minZ).offset(-1, 0, -1);
        BlockPos blockpos1 = BlockPos.containing(p_336061_.maxX, p_336061_.maxY, p_336061_.maxZ);
        BlockPos.betweenClosedStream(blockpos, blockpos1)
            .forEach(
                p_325970_ -> {
                    boolean flag = p_325970_.getX() == blockpos.getX()
                        || p_325970_.getX() == blockpos1.getX()
                        || p_325970_.getZ() == blockpos.getZ()
                        || p_325970_.getZ() == blockpos1.getZ();
                    boolean flag1 = p_325970_.getY() == blockpos1.getY();
                    if (p_334551_.getBlockState(p_325970_).is(Blocks.BARRIER) && (flag || flag1)) {
                        p_334551_.setBlockAndUpdate(p_325970_, Blocks.AIR.defaultBlockState());
                    }
                }
            );
    }

    private static void forceLoadChunks(BoundingBox p_312219_, ServerLevel p_127859_) {
        p_312219_.intersectingChunks().forEach(p_308541_ -> p_127859_.setChunkForced(p_308541_.x, p_308541_.z, true));
    }

    public static void clearSpaceForStructure(BoundingBox p_127850_, ServerLevel p_127852_) {
        int i = p_127850_.minY() - 1;
        BoundingBox boundingbox = new BoundingBox(
            p_127850_.minX() - 2,
            p_127850_.minY() - 3,
            p_127850_.minZ() - 3,
            p_127850_.maxX() + 3,
            p_127850_.maxY() + 20,
            p_127850_.maxZ() + 3
        );
        BlockPos.betweenClosedStream(boundingbox).forEach(p_177748_ -> clearBlock(i, p_177748_, p_127852_));
        p_127852_.getBlockTicks().clearArea(boundingbox);
        p_127852_.clearBlockEvents(boundingbox);
        AABB aabb = AABB.of(boundingbox);
        List<Entity> list = p_127852_.getEntitiesOfClass(Entity.class, aabb, p_177750_ -> !(p_177750_ instanceof Player));
        list.forEach(Entity::discard);
    }

    public static BlockPos getTransformedFarCorner(BlockPos p_310098_, Vec3i p_312132_, Rotation p_309587_) {
        BlockPos blockpos = p_310098_.offset(p_312132_).offset(-1, -1, -1);
        return StructureTemplate.transform(blockpos, Mirror.NONE, p_309587_, p_310098_);
    }

    public static BoundingBox getStructureBoundingBox(BlockPos p_177761_, Vec3i p_177762_, Rotation p_177763_) {
        BlockPos blockpos = getTransformedFarCorner(p_177761_, p_177762_, p_177763_);
        BoundingBox boundingbox = BoundingBox.fromCorners(p_177761_, blockpos);
        int i = Math.min(boundingbox.minX(), boundingbox.maxX());
        int j = Math.min(boundingbox.minZ(), boundingbox.maxZ());
        return boundingbox.move(p_177761_.getX() - i, 0, p_177761_.getZ() - j);
    }

    public static Optional<BlockPos> findStructureBlockContainingPos(BlockPos p_127854_, int p_127855_, ServerLevel p_127856_) {
        return findStructureBlocks(p_127854_, p_127855_, p_127856_).filter(p_177756_ -> doesStructureContain(p_177756_, p_127854_, p_127856_)).findFirst();
    }

    public static Optional<BlockPos> findNearestStructureBlock(BlockPos p_127907_, int p_127908_, ServerLevel p_127909_) {
        Comparator<BlockPos> comparator = Comparator.comparingInt(p_177759_ -> p_177759_.distManhattan(p_127907_));
        return findStructureBlocks(p_127907_, p_127908_, p_127909_).min(comparator);
    }

    public static Stream<BlockPos> findStructureByTestFunction(BlockPos p_333272_, int p_332388_, ServerLevel p_333747_, String p_332891_) {
        return findStructureBlocks(p_333272_, p_332388_, p_333747_)
            .map(p_325972_ -> (StructureBlockEntity)p_333747_.getBlockEntity(p_325972_))
            .filter(Objects::nonNull)
            .filter(p_325954_ -> Objects.equals(p_325954_.getStructureName(), p_332891_))
            .map(BlockEntity::getBlockPos)
            .map(BlockPos::immutable);
    }

    public static Stream<BlockPos> findStructureBlocks(BlockPos p_127911_, int p_127912_, ServerLevel p_127913_) {
        BoundingBox boundingbox = getBoundingBoxAtGround(p_127911_, p_127912_, p_127913_);
        return BlockPos.betweenClosedStream(boundingbox).filter(p_325959_ -> p_127913_.getBlockState(p_325959_).is(Blocks.STRUCTURE_BLOCK)).map(BlockPos::immutable);
    }

    private static StructureBlockEntity createStructureBlock(GameTestInfo p_309598_, BlockPos p_127892_, Rotation p_127893_, ServerLevel p_127894_) {
        p_127894_.setBlockAndUpdate(p_127892_, Blocks.STRUCTURE_BLOCK.defaultBlockState());
        StructureBlockEntity structureblockentity = (StructureBlockEntity)p_127894_.getBlockEntity(p_127892_);
        structureblockentity.setMode(StructureMode.LOAD);
        structureblockentity.setRotation(p_127893_);
        structureblockentity.setIgnoreEntities(false);
        structureblockentity.setStructureName(ResourceLocation.parse(p_309598_.getStructureName()));
        structureblockentity.setMetaData(p_309598_.getTestName());
        if (!structureblockentity.loadStructureInfo(p_127894_)) {
            throw new RuntimeException("Failed to load structure info for test: " + p_309598_.getTestName() + ". Structure name: " + p_309598_.getStructureName());
        } else {
            return structureblockentity;
        }
    }

    private static BoundingBox getBoundingBoxAtGround(BlockPos p_329849_, int p_332427_, ServerLevel p_328726_) {
        BlockPos blockpos = BlockPos.containing(
            (double)p_329849_.getX(), (double)p_328726_.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, p_329849_).getY(), (double)p_329849_.getZ()
        );
        return new BoundingBox(blockpos).inflatedBy(p_332427_, 10, p_332427_);
    }

    public static Stream<BlockPos> lookedAtStructureBlockPos(BlockPos p_333762_, Entity p_333965_, ServerLevel p_336162_) {
        int i = 200;
        Vec3 vec3 = p_333965_.getEyePosition();
        Vec3 vec31 = vec3.add(p_333965_.getLookAngle().scale(200.0));
        return findStructureBlocks(p_333762_, 200, p_336162_)
            .map(p_325966_ -> p_336162_.getBlockEntity(p_325966_, BlockEntityType.STRUCTURE_BLOCK))
            .flatMap(Optional::stream)
            .filter(p_325957_ -> getStructureBounds(p_325957_).clip(vec3, vec31).isPresent())
            .map(BlockEntity::getBlockPos)
            .sorted(Comparator.comparing(p_333762_::distSqr))
            .limit(1L);
    }

    private static void clearBlock(int p_127842_, BlockPos p_127843_, ServerLevel p_127844_) {
        BlockState blockstate;
        if (p_127843_.getY() < p_127842_) {
            blockstate = Blocks.STONE.defaultBlockState();
        } else {
            blockstate = Blocks.AIR.defaultBlockState();
        }

        BlockInput blockinput = new BlockInput(blockstate, Collections.emptySet(), null);
        blockinput.place(p_127844_, p_127843_, 2);
        p_127844_.blockUpdated(p_127843_, blockstate.getBlock());
    }

    private static boolean doesStructureContain(BlockPos p_127868_, BlockPos p_127869_, ServerLevel p_127870_) {
        StructureBlockEntity structureblockentity = (StructureBlockEntity)p_127870_.getBlockEntity(p_127868_);
        return getStructureBoundingBox(structureblockentity).isInside(p_127869_);
    }
}