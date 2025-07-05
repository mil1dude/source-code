package net.minecraft.client.data.models;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.data.models.blockstates.BlockStateGenerator;
import net.minecraft.client.data.models.blockstates.Condition;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.blockstates.Variant;
import net.minecraft.client.data.models.blockstates.VariantProperties;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.special.BannerSpecialRenderer;
import net.minecraft.client.renderer.special.BedSpecialRenderer;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.special.ConduitSpecialRenderer;
import net.minecraft.client.renderer.special.DecoratedPotSpecialRenderer;
import net.minecraft.client.renderer.special.ShulkerBoxSpecialRenderer;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.MossyCarpetBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.vault.VaultState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ComparatorMode;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.block.state.properties.Tilt;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockModelGenerators {
    final Consumer<BlockStateGenerator> blockStateOutput;
    final ItemModelOutput itemModelOutput;
    final BiConsumer<ResourceLocation, ModelInstance> modelOutput;
    final List<Block> nonOrientableTrapdoor = ImmutableList.of(Blocks.OAK_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.IRON_TRAPDOOR);
    final Map<Block, BlockModelGenerators.BlockStateGeneratorSupplier> fullBlockModelCustomGenerators = ImmutableMap.<Block, BlockModelGenerators.BlockStateGeneratorSupplier>builder()
        .put(Blocks.STONE, BlockModelGenerators::createMirroredCubeGenerator)
        .put(Blocks.DEEPSLATE, BlockModelGenerators::createMirroredColumnGenerator)
        .put(Blocks.MUD_BRICKS, BlockModelGenerators::createNorthWestMirroredCubeGenerator)
        .build();
    final Map<Block, TexturedModel> texturedModels = ImmutableMap.<Block, TexturedModel>builder()
        .put(Blocks.SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.SANDSTONE))
        .put(Blocks.RED_SANDSTONE, TexturedModel.TOP_BOTTOM_WITH_WALL.get(Blocks.RED_SANDSTONE))
        .put(Blocks.SMOOTH_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top")))
        .put(Blocks.SMOOTH_RED_SANDSTONE, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top")))
        .put(
            Blocks.CUT_SANDSTONE,
            TexturedModel.COLUMN
                .get(Blocks.SANDSTONE)
                .updateTextures(p_376753_ -> p_376753_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_SANDSTONE)))
        )
        .put(
            Blocks.CUT_RED_SANDSTONE,
            TexturedModel.COLUMN
                .get(Blocks.RED_SANDSTONE)
                .updateTextures(p_376502_ -> p_376502_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CUT_RED_SANDSTONE)))
        )
        .put(Blocks.QUARTZ_BLOCK, TexturedModel.COLUMN.get(Blocks.QUARTZ_BLOCK))
        .put(Blocks.SMOOTH_QUARTZ, TexturedModel.createAllSame(TextureMapping.getBlockTexture(Blocks.QUARTZ_BLOCK, "_bottom")))
        .put(Blocks.BLACKSTONE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.BLACKSTONE))
        .put(Blocks.DEEPSLATE, TexturedModel.COLUMN_WITH_WALL.get(Blocks.DEEPSLATE))
        .put(
            Blocks.CHISELED_QUARTZ_BLOCK,
            TexturedModel.COLUMN
                .get(Blocks.CHISELED_QUARTZ_BLOCK)
                .updateTextures(p_377216_ -> p_377216_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_QUARTZ_BLOCK)))
        )
        .put(Blocks.CHISELED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_SANDSTONE).updateTextures(p_376762_ -> {
            p_376762_.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.SANDSTONE, "_top"));
            p_376762_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_SANDSTONE));
        }))
        .put(Blocks.CHISELED_RED_SANDSTONE, TexturedModel.COLUMN.get(Blocks.CHISELED_RED_SANDSTONE).updateTextures(p_377408_ -> {
            p_377408_.put(TextureSlot.END, TextureMapping.getBlockTexture(Blocks.RED_SANDSTONE, "_top"));
            p_377408_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.CHISELED_RED_SANDSTONE));
        }))
        .put(Blocks.CHISELED_TUFF_BRICKS, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF_BRICKS))
        .put(Blocks.CHISELED_TUFF, TexturedModel.COLUMN_WITH_WALL.get(Blocks.CHISELED_TUFF))
        .build();
    static final Map<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>> SHAPE_CONSUMERS = ImmutableMap.<BlockFamily.Variant, BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block>>builder()
        .put(BlockFamily.Variant.BUTTON, BlockModelGenerators.BlockFamilyProvider::button)
        .put(BlockFamily.Variant.DOOR, BlockModelGenerators.BlockFamilyProvider::door)
        .put(BlockFamily.Variant.CHISELED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
        .put(BlockFamily.Variant.CRACKED, BlockModelGenerators.BlockFamilyProvider::fullBlockVariant)
        .put(BlockFamily.Variant.CUSTOM_FENCE, BlockModelGenerators.BlockFamilyProvider::customFence)
        .put(BlockFamily.Variant.FENCE, BlockModelGenerators.BlockFamilyProvider::fence)
        .put(BlockFamily.Variant.CUSTOM_FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::customFenceGate)
        .put(BlockFamily.Variant.FENCE_GATE, BlockModelGenerators.BlockFamilyProvider::fenceGate)
        .put(BlockFamily.Variant.SIGN, BlockModelGenerators.BlockFamilyProvider::sign)
        .put(BlockFamily.Variant.SLAB, BlockModelGenerators.BlockFamilyProvider::slab)
        .put(BlockFamily.Variant.STAIRS, BlockModelGenerators.BlockFamilyProvider::stairs)
        .put(BlockFamily.Variant.PRESSURE_PLATE, BlockModelGenerators.BlockFamilyProvider::pressurePlate)
        .put(BlockFamily.Variant.TRAPDOOR, BlockModelGenerators.BlockFamilyProvider::trapdoor)
        .put(BlockFamily.Variant.WALL, BlockModelGenerators.BlockFamilyProvider::wall)
        .build();
    public static final List<Pair<Direction, Function<ResourceLocation, Variant>>> MULTIFACE_GENERATOR = List.of(
        Pair.of(Direction.NORTH, p_377876_ -> Variant.variant().with(VariantProperties.MODEL, p_377876_)),
        Pair.of(
            Direction.EAST,
            p_378589_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_378589_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.SOUTH,
            p_376247_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_376247_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.WEST,
            p_376476_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_376476_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.UP,
            p_378666_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_378666_)
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
        ),
        Pair.of(
            Direction.DOWN,
            p_376607_ -> Variant.variant()
                    .with(VariantProperties.MODEL, p_376607_)
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
        )
    );
    private static final Map<BlockModelGenerators.BookSlotModelCacheKey, ResourceLocation> CHISELED_BOOKSHELF_SLOT_MODEL_CACHE = new HashMap<>();

    private static BlockStateGenerator createMirroredCubeGenerator(
        Block p_377256_, ResourceLocation p_376577_, TextureMapping p_376649_, BiConsumer<ResourceLocation, ModelInstance> p_378385_
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_MIRRORED_ALL.create(p_377256_, p_376649_, p_378385_);
        return createRotatedVariant(p_377256_, p_376577_, resourcelocation);
    }

    private static BlockStateGenerator createNorthWestMirroredCubeGenerator(
        Block p_376344_, ResourceLocation p_376534_, TextureMapping p_375768_, BiConsumer<ResourceLocation, ModelInstance> p_378658_
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_NORTH_WEST_MIRRORED_ALL.create(p_376344_, p_375768_, p_378658_);
        return createSimpleBlock(p_376344_, resourcelocation);
    }

    private static BlockStateGenerator createMirroredColumnGenerator(
        Block p_376474_, ResourceLocation p_378259_, TextureMapping p_376415_, BiConsumer<ResourceLocation, ModelInstance> p_377524_
    ) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN_MIRRORED.create(p_376474_, p_376415_, p_377524_);
        return createRotatedVariant(p_376474_, p_378259_, resourcelocation).with(createRotatedPillar());
    }

    public BlockModelGenerators(Consumer<BlockStateGenerator> p_378137_, ItemModelOutput p_378502_, BiConsumer<ResourceLocation, ModelInstance> p_378240_) {
        this.blockStateOutput = p_378137_;
        this.itemModelOutput = p_378502_;
        this.modelOutput = p_378240_;
    }

    private void registerSimpleItemModel(Item p_375488_, ResourceLocation p_377760_) {
        this.itemModelOutput.accept(p_375488_, ItemModelUtils.plainModel(p_377760_));
    }

    void registerSimpleItemModel(Block p_376656_, ResourceLocation p_375635_) {
        this.itemModelOutput.accept(p_376656_.asItem(), ItemModelUtils.plainModel(p_375635_));
    }

    private void registerSimpleTintedItemModel(Block p_375646_, ResourceLocation p_376671_, ItemTintSource p_378262_) {
        this.itemModelOutput.accept(p_375646_.asItem(), ItemModelUtils.tintedModel(p_376671_, p_378262_));
    }

    private ResourceLocation createFlatItemModel(Item p_378261_) {
        return ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(p_378261_), TextureMapping.layer0(p_378261_), this.modelOutput);
    }

    ResourceLocation createFlatItemModelWithBlockTexture(Item p_376351_, Block p_377327_) {
        return ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(p_376351_), TextureMapping.layer0(p_377327_), this.modelOutput);
    }

    private ResourceLocation createFlatItemModelWithBlockTexture(Item p_375454_, Block p_376580_, String p_376399_) {
        return ModelTemplates.FLAT_ITEM
            .create(ModelLocationUtils.getModelLocation(p_375454_), TextureMapping.layer0(TextureMapping.getBlockTexture(p_376580_, p_376399_)), this.modelOutput);
    }

    ResourceLocation createFlatItemModelWithBlockTextureAndOverlay(Item p_377621_, Block p_376897_, String p_375836_) {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(p_376897_);
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(p_376897_, p_375836_);
        return ModelTemplates.TWO_LAYERED_ITEM
            .create(ModelLocationUtils.getModelLocation(p_377621_), TextureMapping.layered(resourcelocation, resourcelocation1), this.modelOutput);
    }

    void registerSimpleFlatItemModel(Item p_378703_) {
        this.registerSimpleItemModel(p_378703_, this.createFlatItemModel(p_378703_));
    }

    private void registerSimpleFlatItemModel(Block p_378454_) {
        Item item = p_378454_.asItem();
        if (item != Items.AIR) {
            this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, p_378454_));
        }
    }

    private void registerSimpleFlatItemModel(Block p_376201_, String p_377421_) {
        Item item = p_376201_.asItem();
        if (item != Items.AIR) {
            this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, p_376201_, p_377421_));
        }
    }

    private void registerTwoLayerFlatItemModel(Block p_377903_, String p_378009_) {
        Item item = p_377903_.asItem();
        if (item != Items.AIR) {
            ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTextureAndOverlay(item, p_377903_, p_378009_);
            this.registerSimpleItemModel(item, resourcelocation);
        }
    }

    private static PropertyDispatch createHorizontalFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant());
    }

    private static PropertyDispatch createHorizontalFacingDispatchAlt() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.SOUTH, Variant.variant())
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createTorchHorizontalDispatch() {
        return PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270));
    }

    private static PropertyDispatch createFacingDispatch() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
            .select(Direction.NORTH, Variant.variant())
            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
            .select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
    }

    private static MultiVariantGenerator createRotatedVariant(Block p_377300_, ResourceLocation p_377430_) {
        return MultiVariantGenerator.multiVariant(p_377300_, createRotatedVariants(p_377430_));
    }

    private static Variant[] createRotatedVariants(ResourceLocation p_375451_) {
        return new Variant[]{
            Variant.variant().with(VariantProperties.MODEL, p_375451_),
            Variant.variant().with(VariantProperties.MODEL, p_375451_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
            Variant.variant().with(VariantProperties.MODEL, p_375451_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, p_375451_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
        };
    }

    private static MultiVariantGenerator createRotatedVariant(Block p_376098_, ResourceLocation p_377321_, ResourceLocation p_378801_) {
        return MultiVariantGenerator.multiVariant(
            p_376098_,
            Variant.variant().with(VariantProperties.MODEL, p_377321_),
            Variant.variant().with(VariantProperties.MODEL, p_378801_),
            Variant.variant().with(VariantProperties.MODEL, p_377321_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
            Variant.variant().with(VariantProperties.MODEL, p_378801_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
        );
    }

    private static PropertyDispatch createBooleanModelDispatch(BooleanProperty p_378433_, ResourceLocation p_375735_, ResourceLocation p_376694_) {
        return PropertyDispatch.property(p_378433_)
            .select(true, Variant.variant().with(VariantProperties.MODEL, p_375735_))
            .select(false, Variant.variant().with(VariantProperties.MODEL, p_376694_));
    }

    private void createRotatedMirroredVariantBlock(Block p_375955_) {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(p_375955_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.CUBE_MIRRORED.create(p_375955_, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(p_375955_, resourcelocation, resourcelocation1));
    }

    private void createRotatedVariantBlock(Block p_376360_) {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(p_376360_, this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(p_376360_, resourcelocation));
    }

    private void createBrushableBlock(Block p_377090_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_377090_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DUSTED)
                            .generate(
                                p_376869_ -> {
                                    String s = "_" + p_376869_;
                                    ResourceLocation resourcelocation = TextureMapping.getBlockTexture(p_377090_, s);
                                    return Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            ModelTemplates.CUBE_ALL
                                                .createWithSuffix(
                                                    p_377090_, s, new TextureMapping().put(TextureSlot.ALL, resourcelocation), this.modelOutput
                                                )
                                        );
                                }
                            )
                    )
            );
        this.registerSimpleItemModel(p_377090_, ModelLocationUtils.getModelLocation(p_377090_, "_0"));
    }

    static BlockStateGenerator createButton(Block p_378101_, ResourceLocation p_375992_, ResourceLocation p_377490_) {
        return MultiVariantGenerator.multiVariant(p_378101_)
            .with(
                PropertyDispatch.property(BlockStateProperties.POWERED)
                    .select(false, Variant.variant().with(VariantProperties.MODEL, p_375992_))
                    .select(true, Variant.variant().with(VariantProperties.MODEL, p_377490_))
            )
            .with(
                PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                    .select(AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    .select(AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    .select(AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                    .select(
                        AttachFace.WALL,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.SOUTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.WALL,
                        Direction.NORTH,
                        Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.EAST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        AttachFace.CEILING,
                        Direction.WEST,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(AttachFace.CEILING, Direction.SOUTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
                    .select(
                        AttachFace.CEILING,
                        Direction.NORTH,
                        Variant.variant()
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                    )
            );
    }

    private static PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> configureDoorHalf(
        PropertyDispatch.C4<Direction, DoubleBlockHalf, DoorHingeSide, Boolean> p_376931_,
        DoubleBlockHalf p_375716_,
        ResourceLocation p_376116_,
        ResourceLocation p_376486_,
        ResourceLocation p_375802_,
        ResourceLocation p_377574_
    ) {
        return p_376931_.select(Direction.EAST, p_375716_, DoorHingeSide.LEFT, false, Variant.variant().with(VariantProperties.MODEL, p_376116_))
            .select(
                Direction.SOUTH,
                p_375716_,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_376116_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                p_375716_,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_376116_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                p_375716_,
                DoorHingeSide.LEFT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_376116_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.EAST, p_375716_, DoorHingeSide.RIGHT, false, Variant.variant().with(VariantProperties.MODEL, p_375802_))
            .select(
                Direction.SOUTH,
                p_375716_,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_375802_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.WEST,
                p_375716_,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_375802_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.NORTH,
                p_375716_,
                DoorHingeSide.RIGHT,
                false,
                Variant.variant().with(VariantProperties.MODEL, p_375802_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                p_375716_,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_376486_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.SOUTH,
                p_375716_,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_376486_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                p_375716_,
                DoorHingeSide.LEFT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_376486_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.NORTH, p_375716_, DoorHingeSide.LEFT, true, Variant.variant().with(VariantProperties.MODEL, p_376486_))
            .select(
                Direction.EAST,
                p_375716_,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_377574_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(Direction.SOUTH, p_375716_, DoorHingeSide.RIGHT, true, Variant.variant().with(VariantProperties.MODEL, p_377574_))
            .select(
                Direction.WEST,
                p_375716_,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_377574_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            )
            .select(
                Direction.NORTH,
                p_375716_,
                DoorHingeSide.RIGHT,
                true,
                Variant.variant().with(VariantProperties.MODEL, p_377574_).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            );
    }

    private static BlockStateGenerator createDoor(
        Block p_378473_,
        ResourceLocation p_378063_,
        ResourceLocation p_377717_,
        ResourceLocation p_377605_,
        ResourceLocation p_376632_,
        ResourceLocation p_376295_,
        ResourceLocation p_377850_,
        ResourceLocation p_378665_,
        ResourceLocation p_378397_
    ) {
        return MultiVariantGenerator.multiVariant(p_378473_)
            .with(
                configureDoorHalf(
                    configureDoorHalf(
                        PropertyDispatch.properties(
                            BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.DOUBLE_BLOCK_HALF, BlockStateProperties.DOOR_HINGE, BlockStateProperties.OPEN
                        ),
                        DoubleBlockHalf.LOWER,
                        p_378063_,
                        p_377717_,
                        p_377605_,
                        p_376632_
                    ),
                    DoubleBlockHalf.UPPER,
                    p_376295_,
                    p_377850_,
                    p_378665_,
                    p_378397_
                )
            );
    }

    static BlockStateGenerator createCustomFence(
        Block p_376702_,
        ResourceLocation p_377591_,
        ResourceLocation p_376187_,
        ResourceLocation p_377795_,
        ResourceLocation p_378212_,
        ResourceLocation p_377808_
    ) {
        return MultiPartGenerator.multiPart(p_376702_)
            .with(Variant.variant().with(VariantProperties.MODEL, p_377591_))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH, true),
                Variant.variant().with(VariantProperties.MODEL, p_376187_).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST, true),
                Variant.variant().with(VariantProperties.MODEL, p_377795_).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH, true),
                Variant.variant().with(VariantProperties.MODEL, p_378212_).with(VariantProperties.UV_LOCK, false)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST, true),
                Variant.variant().with(VariantProperties.MODEL, p_377808_).with(VariantProperties.UV_LOCK, false)
            );
    }

    static BlockStateGenerator createFence(Block p_378690_, ResourceLocation p_377844_, ResourceLocation p_376194_) {
        return MultiPartGenerator.multiPart(p_378690_)
            .with(Variant.variant().with(VariantProperties.MODEL, p_377844_))
            .with(
                Condition.condition().term(BlockStateProperties.NORTH, true),
                Variant.variant().with(VariantProperties.MODEL, p_376194_).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376194_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376194_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST, true),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376194_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    static BlockStateGenerator createWall(Block p_376287_, ResourceLocation p_376637_, ResourceLocation p_376587_, ResourceLocation p_376750_) {
        return MultiPartGenerator.multiPart(p_376287_)
            .with(
                Condition.condition().term(BlockStateProperties.UP, true), Variant.variant().with(VariantProperties.MODEL, p_376637_)
            )
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.LOW),
                Variant.variant().with(VariantProperties.MODEL, p_376587_).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376587_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376587_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.LOW),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376587_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.NORTH_WALL, WallSide.TALL),
                Variant.variant().with(VariantProperties.MODEL, p_376750_).with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.EAST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376750_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.SOUTH_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376750_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    .with(VariantProperties.UV_LOCK, true)
            )
            .with(
                Condition.condition().term(BlockStateProperties.WEST_WALL, WallSide.TALL),
                Variant.variant()
                    .with(VariantProperties.MODEL, p_376750_)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.UV_LOCK, true)
            );
    }

    static BlockStateGenerator createFenceGate(
        Block p_375545_, ResourceLocation p_377183_, ResourceLocation p_375501_, ResourceLocation p_377769_, ResourceLocation p_377841_, boolean p_378283_
    ) {
        return MultiVariantGenerator.multiVariant(p_375545_, Variant.variant().with(VariantProperties.UV_LOCK, p_378283_))
            .with(createHorizontalFacingDispatchAlt())
            .with(
                PropertyDispatch.properties(BlockStateProperties.IN_WALL, BlockStateProperties.OPEN)
                    .select(false, false, Variant.variant().with(VariantProperties.MODEL, p_375501_))
                    .select(true, false, Variant.variant().with(VariantProperties.MODEL, p_377841_))
                    .select(false, true, Variant.variant().with(VariantProperties.MODEL, p_377183_))
                    .select(true, true, Variant.variant().with(VariantProperties.MODEL, p_377769_))
            );
    }

    static BlockStateGenerator createStairs(Block p_377012_, ResourceLocation p_377814_, ResourceLocation p_375598_, ResourceLocation p_378150_) {
        return MultiVariantGenerator.multiVariant(p_377012_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.STAIRS_SHAPE)
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.STRAIGHT, Variant.variant().with(VariantProperties.MODEL, p_375598_))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_375598_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_375598_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_375598_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.OUTER_RIGHT, Variant.variant().with(VariantProperties.MODEL, p_378150_))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.OUTER_LEFT, Variant.variant().with(VariantProperties.MODEL, p_378150_))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.EAST, Half.BOTTOM, StairsShape.INNER_RIGHT, Variant.variant().with(VariantProperties.MODEL, p_377814_))
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(Direction.SOUTH, Half.BOTTOM, StairsShape.INNER_LEFT, Variant.variant().with(VariantProperties.MODEL, p_377814_))
                    .select(
                        Direction.NORTH,
                        Half.BOTTOM,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_375598_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_375598_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_375598_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.STRAIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_375598_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.OUTER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378150_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_RIGHT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        StairsShape.INNER_LEFT,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377814_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private static BlockStateGenerator createOrientableTrapdoor(Block p_378448_, ResourceLocation p_377661_, ResourceLocation p_378435_, ResourceLocation p_378222_) {
        return MultiVariantGenerator.multiVariant(p_378448_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_378435_))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        false,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378435_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        false,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378435_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        false,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378435_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_377661_))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        false,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377661_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        false,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377661_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        false,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377661_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, p_378222_))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378222_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378222_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378222_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.NORTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378222_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378222_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378222_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_378222_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private static BlockStateGenerator createTrapdoor(Block p_376522_, ResourceLocation p_376413_, ResourceLocation p_377280_, ResourceLocation p_376962_) {
        return MultiVariantGenerator.multiVariant(p_376522_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.HALF, BlockStateProperties.OPEN)
                    .select(Direction.NORTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_377280_))
                    .select(Direction.SOUTH, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_377280_))
                    .select(Direction.EAST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_377280_))
                    .select(Direction.WEST, Half.BOTTOM, false, Variant.variant().with(VariantProperties.MODEL, p_377280_))
                    .select(Direction.NORTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_376413_))
                    .select(Direction.SOUTH, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_376413_))
                    .select(Direction.EAST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_376413_))
                    .select(Direction.WEST, Half.TOP, false, Variant.variant().with(VariantProperties.MODEL, p_376413_))
                    .select(Direction.NORTH, Half.BOTTOM, true, Variant.variant().with(VariantProperties.MODEL, p_376962_))
                    .select(
                        Direction.SOUTH,
                        Half.BOTTOM,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_376962_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.BOTTOM,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_376962_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.BOTTOM,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_376962_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .select(Direction.NORTH, Half.TOP, true, Variant.variant().with(VariantProperties.MODEL, p_376962_))
                    .select(
                        Direction.SOUTH,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_376962_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .select(
                        Direction.EAST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_376962_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.WEST,
                        Half.TOP,
                        true,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_376962_)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    static MultiVariantGenerator createSimpleBlock(Block p_377037_, ResourceLocation p_375580_) {
        return MultiVariantGenerator.multiVariant(p_377037_, Variant.variant().with(VariantProperties.MODEL, p_375580_));
    }

    private static PropertyDispatch createRotatedPillar() {
        return PropertyDispatch.property(BlockStateProperties.AXIS)
            .select(Direction.Axis.Y, Variant.variant())
            .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.Axis.X,
                Variant.variant()
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    static BlockStateGenerator createPillarBlockUVLocked(Block p_375885_, TextureMapping p_376939_, BiConsumer<ResourceLocation, ModelInstance> p_378526_) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN_UV_LOCKED_X.create(p_375885_, p_376939_, p_378526_);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Y.create(p_375885_, p_376939_, p_378526_);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_COLUMN_UV_LOCKED_Z.create(p_375885_, p_376939_, p_378526_);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_COLUMN.create(p_375885_, p_376939_, p_378526_);
        return MultiVariantGenerator.multiVariant(p_375885_, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
            .with(
                PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.X, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .select(Direction.Axis.Z, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
            );
    }

    static BlockStateGenerator createAxisAlignedPillarBlock(Block p_375933_, ResourceLocation p_378086_) {
        return MultiVariantGenerator.multiVariant(p_375933_, Variant.variant().with(VariantProperties.MODEL, p_378086_)).with(createRotatedPillar());
    }

    private void createAxisAlignedPillarBlockCustomModel(Block p_375527_, ResourceLocation p_377731_) {
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(p_375527_, p_377731_));
    }

    public void createAxisAlignedPillarBlock(Block p_376383_, TexturedModel.Provider p_376075_) {
        ResourceLocation resourcelocation = p_376075_.create(p_376383_, this.modelOutput);
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(p_376383_, resourcelocation));
    }

    private void createHorizontallyRotatedBlock(Block p_376196_, TexturedModel.Provider p_378733_) {
        ResourceLocation resourcelocation = p_378733_.create(p_376196_, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376196_, Variant.variant().with(VariantProperties.MODEL, resourcelocation)).with(createHorizontalFacingDispatch())
            );
    }

    static BlockStateGenerator createRotatedPillarWithHorizontalVariant(Block p_376711_, ResourceLocation p_376492_, ResourceLocation p_377861_) {
        return MultiVariantGenerator.multiVariant(p_376711_)
            .with(
                PropertyDispatch.property(BlockStateProperties.AXIS)
                    .select(Direction.Axis.Y, Variant.variant().with(VariantProperties.MODEL, p_376492_))
                    .select(
                        Direction.Axis.Z,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377861_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    )
                    .select(
                        Direction.Axis.X,
                        Variant.variant()
                            .with(VariantProperties.MODEL, p_377861_)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
    }

    private void createRotatedPillarWithHorizontalVariant(Block p_377355_, TexturedModel.Provider p_375936_, TexturedModel.Provider p_375397_) {
        ResourceLocation resourcelocation = p_375936_.create(p_377355_, this.modelOutput);
        ResourceLocation resourcelocation1 = p_375397_.create(p_377355_, this.modelOutput);
        this.blockStateOutput.accept(createRotatedPillarWithHorizontalVariant(p_377355_, resourcelocation, resourcelocation1));
    }

    private void createCreakingHeart(Block p_376905_) {
        Function<TexturedModel.Provider, ResourceLocation> function = p_378672_ -> p_378672_.updateTexture(
                    p_375592_ -> p_375592_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_376905_, "_active"))
                )
                .updateTexture(p_375658_ -> p_375658_.put(TextureSlot.END, TextureMapping.getBlockTexture(p_376905_, "_top_active")))
                .createWithSuffix(p_376905_, "_active", this.modelOutput);
        ResourceLocation resourcelocation = TexturedModel.COLUMN_ALT.create(p_376905_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.COLUMN_HORIZONTAL_ALT.create(p_376905_, this.modelOutput);
        ResourceLocation resourcelocation2 = function.apply(TexturedModel.COLUMN_ALT);
        ResourceLocation resourcelocation3 = function.apply(TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376905_)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.AXIS, CreakingHeartBlock.ACTIVE)
                            .select(Direction.Axis.Y, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.Axis.Z,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.Axis.X,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(Direction.Axis.Y, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(
                                Direction.Axis.Z,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.Axis.X,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                    )
            );
    }

    private ResourceLocation createSuffixedVariant(Block p_375634_, String p_376378_, ModelTemplate p_376381_, Function<ResourceLocation, TextureMapping> p_378653_) {
        return p_376381_.createWithSuffix(p_375634_, p_376378_, p_378653_.apply(TextureMapping.getBlockTexture(p_375634_, p_376378_)), this.modelOutput);
    }

    static BlockStateGenerator createPressurePlate(Block p_378199_, ResourceLocation p_376284_, ResourceLocation p_375449_) {
        return MultiVariantGenerator.multiVariant(p_378199_).with(createBooleanModelDispatch(BlockStateProperties.POWERED, p_375449_, p_376284_));
    }

    static BlockStateGenerator createSlab(Block p_377807_, ResourceLocation p_377877_, ResourceLocation p_378104_, ResourceLocation p_376555_) {
        return MultiVariantGenerator.multiVariant(p_377807_)
            .with(
                PropertyDispatch.property(BlockStateProperties.SLAB_TYPE)
                    .select(SlabType.BOTTOM, Variant.variant().with(VariantProperties.MODEL, p_377877_))
                    .select(SlabType.TOP, Variant.variant().with(VariantProperties.MODEL, p_378104_))
                    .select(SlabType.DOUBLE, Variant.variant().with(VariantProperties.MODEL, p_376555_))
            );
    }

    public void createTrivialCube(Block p_376957_) {
        this.createTrivialBlock(p_376957_, TexturedModel.CUBE);
    }

    public void createTrivialBlock(Block p_375823_, TexturedModel.Provider p_376542_) {
        this.blockStateOutput.accept(createSimpleBlock(p_375823_, p_376542_.create(p_375823_, this.modelOutput)));
    }

    public void createTintedLeaves(Block p_375590_, TexturedModel.Provider p_376506_, int p_375511_) {
        ResourceLocation resourcelocation = p_376506_.create(p_375590_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_375590_, resourcelocation));
        this.registerSimpleTintedItemModel(p_375590_, resourcelocation, ItemModelUtils.constantTint(p_375511_));
    }

    private void createVine() {
        this.createMultifaceBlockStates(Blocks.VINE);
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(Items.VINE, Blocks.VINE);
        this.registerSimpleTintedItemModel(Blocks.VINE, resourcelocation, ItemModelUtils.constantTint(-12012264));
    }

    private void createItemWithGrassTint(Block p_377529_) {
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(p_377529_.asItem(), p_377529_);
        this.registerSimpleTintedItemModel(p_377529_, resourcelocation, new GrassColorSource());
    }

    private BlockModelGenerators.BlockFamilyProvider family(Block p_378128_) {
        TexturedModel texturedmodel = this.texturedModels.getOrDefault(p_378128_, TexturedModel.CUBE.get(p_378128_));
        return new BlockModelGenerators.BlockFamilyProvider(texturedmodel.getMapping()).fullBlock(p_378128_, texturedmodel.getTemplate());
    }

    public void createHangingSign(Block p_376300_, Block p_376812_, Block p_376678_) {
        ResourceLocation resourcelocation = this.createParticleOnlyBlockModel(p_376812_, p_376300_);
        this.blockStateOutput.accept(createSimpleBlock(p_376812_, resourcelocation));
        this.blockStateOutput.accept(createSimpleBlock(p_376678_, resourcelocation));
        this.registerSimpleFlatItemModel(p_376812_.asItem());
    }

    void createDoor(Block p_377944_) {
        TextureMapping texturemapping = TextureMapping.door(p_377944_);
        ResourceLocation resourcelocation = ModelTemplates.DOOR_BOTTOM_LEFT.create(p_377944_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.create(p_377944_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.DOOR_BOTTOM_RIGHT.create(p_377944_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.create(p_377944_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.DOOR_TOP_LEFT.create(p_377944_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.DOOR_TOP_LEFT_OPEN.create(p_377944_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.DOOR_TOP_RIGHT.create(p_377944_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.create(p_377944_, texturemapping, this.modelOutput);
        this.registerSimpleFlatItemModel(p_377944_.asItem());
        this.blockStateOutput
            .accept(
                createDoor(
                    p_377944_,
                    resourcelocation,
                    resourcelocation1,
                    resourcelocation2,
                    resourcelocation3,
                    resourcelocation4,
                    resourcelocation5,
                    resourcelocation6,
                    resourcelocation7
                )
            );
    }

    private void copyDoorModel(Block p_377434_, Block p_376354_) {
        ResourceLocation resourcelocation = ModelTemplates.DOOR_BOTTOM_LEFT.getDefaultModelLocation(p_377434_);
        ResourceLocation resourcelocation1 = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN.getDefaultModelLocation(p_377434_);
        ResourceLocation resourcelocation2 = ModelTemplates.DOOR_BOTTOM_RIGHT.getDefaultModelLocation(p_377434_);
        ResourceLocation resourcelocation3 = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN.getDefaultModelLocation(p_377434_);
        ResourceLocation resourcelocation4 = ModelTemplates.DOOR_TOP_LEFT.getDefaultModelLocation(p_377434_);
        ResourceLocation resourcelocation5 = ModelTemplates.DOOR_TOP_LEFT_OPEN.getDefaultModelLocation(p_377434_);
        ResourceLocation resourcelocation6 = ModelTemplates.DOOR_TOP_RIGHT.getDefaultModelLocation(p_377434_);
        ResourceLocation resourcelocation7 = ModelTemplates.DOOR_TOP_RIGHT_OPEN.getDefaultModelLocation(p_377434_);
        this.itemModelOutput.copy(p_377434_.asItem(), p_376354_.asItem());
        this.blockStateOutput
            .accept(
                createDoor(
                    p_376354_,
                    resourcelocation,
                    resourcelocation1,
                    resourcelocation2,
                    resourcelocation3,
                    resourcelocation4,
                    resourcelocation5,
                    resourcelocation6,
                    resourcelocation7
                )
            );
    }

    void createOrientableTrapdoor(Block p_378524_) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(p_378524_);
        ResourceLocation resourcelocation = ModelTemplates.ORIENTABLE_TRAPDOOR_TOP.create(p_378524_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.ORIENTABLE_TRAPDOOR_BOTTOM.create(p_378524_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.ORIENTABLE_TRAPDOOR_OPEN.create(p_378524_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createOrientableTrapdoor(p_378524_, resourcelocation, resourcelocation1, resourcelocation2));
        this.registerSimpleItemModel(p_378524_, resourcelocation1);
    }

    void createTrapdoor(Block p_376752_) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(p_376752_);
        ResourceLocation resourcelocation = ModelTemplates.TRAPDOOR_TOP.create(p_376752_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TRAPDOOR_BOTTOM.create(p_376752_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.TRAPDOOR_OPEN.create(p_376752_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createTrapdoor(p_376752_, resourcelocation, resourcelocation1, resourcelocation2));
        this.registerSimpleItemModel(p_376752_, resourcelocation1);
    }

    private void copyTrapdoorModel(Block p_376748_, Block p_376418_) {
        ResourceLocation resourcelocation = ModelTemplates.TRAPDOOR_TOP.getDefaultModelLocation(p_376748_);
        ResourceLocation resourcelocation1 = ModelTemplates.TRAPDOOR_BOTTOM.getDefaultModelLocation(p_376748_);
        ResourceLocation resourcelocation2 = ModelTemplates.TRAPDOOR_OPEN.getDefaultModelLocation(p_376748_);
        this.itemModelOutput.copy(p_376748_.asItem(), p_376418_.asItem());
        this.blockStateOutput.accept(createTrapdoor(p_376418_, resourcelocation, resourcelocation1, resourcelocation2));
    }

    private void createBigDripLeafBlock() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_partial_tilt");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.BIG_DRIPLEAF, "_full_tilt");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BIG_DRIPLEAF)
                    .with(createHorizontalFacingDispatch())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.TILT)
                            .select(Tilt.NONE, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Tilt.UNSTABLE, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Tilt.PARTIAL, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(Tilt.FULL, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    )
            );
    }

    private BlockModelGenerators.WoodProvider woodProvider(Block p_376901_) {
        return new BlockModelGenerators.WoodProvider(TextureMapping.logColumn(p_376901_));
    }

    private void createNonTemplateModelBlock(Block p_377331_) {
        this.createNonTemplateModelBlock(p_377331_, p_377331_);
    }

    private void createNonTemplateModelBlock(Block p_376889_, Block p_375872_) {
        this.blockStateOutput.accept(createSimpleBlock(p_376889_, ModelLocationUtils.getModelLocation(p_375872_)));
    }

    private void createCrossBlockWithDefaultItem(Block p_377523_, BlockModelGenerators.PlantType p_376483_) {
        this.registerSimpleItemModel(p_377523_.asItem(), p_376483_.createItemModel(this, p_377523_));
        this.createCrossBlock(p_377523_, p_376483_);
    }

    private void createCrossBlockWithDefaultItem(Block p_376375_, BlockModelGenerators.PlantType p_375442_, TextureMapping p_375740_) {
        this.registerSimpleFlatItemModel(p_376375_);
        this.createCrossBlock(p_376375_, p_375442_, p_375740_);
    }

    private void createCrossBlock(Block p_378311_, BlockModelGenerators.PlantType p_377491_) {
        TextureMapping texturemapping = p_377491_.getTextureMapping(p_378311_);
        this.createCrossBlock(p_378311_, p_377491_, texturemapping);
    }

    private void createCrossBlock(Block p_378299_, BlockModelGenerators.PlantType p_378744_, TextureMapping p_377447_) {
        ResourceLocation resourcelocation = p_378744_.getCross().create(p_378299_, p_377447_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_378299_, resourcelocation));
    }

    private void createCrossBlock(Block p_376503_, BlockModelGenerators.PlantType p_377030_, Property<Integer> p_377779_, int... p_376808_) {
        if (p_377779_.getPossibleValues().size() != p_376808_.length) {
            throw new IllegalArgumentException("missing values for property: " + p_377779_);
        } else {
            PropertyDispatch propertydispatch = PropertyDispatch.property(p_377779_).generate(p_378794_ -> {
                String s = "_stage" + p_376808_[p_378794_];
                TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(p_376503_, s));
                ResourceLocation resourcelocation = p_377030_.getCross().createWithSuffix(p_376503_, s, texturemapping, this.modelOutput);
                return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
            });
            this.registerSimpleFlatItemModel(p_376503_.asItem());
            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_376503_).with(propertydispatch));
        }
    }

    private void createPlantWithDefaultItem(Block p_375477_, Block p_376166_, BlockModelGenerators.PlantType p_377517_) {
        this.registerSimpleItemModel(p_375477_.asItem(), p_377517_.createItemModel(this, p_375477_));
        this.createPlant(p_375477_, p_376166_, p_377517_);
    }

    private void createPlant(Block p_376623_, Block p_378539_, BlockModelGenerators.PlantType p_378054_) {
        this.createCrossBlock(p_376623_, p_378054_);
        TextureMapping texturemapping = p_378054_.getPlantTextureMapping(p_376623_);
        ResourceLocation resourcelocation = p_378054_.getCrossPot().create(p_378539_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_378539_, resourcelocation));
    }

    private void createCoralFans(Block p_378025_, Block p_377013_) {
        TexturedModel texturedmodel = TexturedModel.CORAL_FAN.get(p_378025_);
        ResourceLocation resourcelocation = texturedmodel.create(p_378025_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_378025_, resourcelocation));
        ResourceLocation resourcelocation1 = ModelTemplates.CORAL_WALL_FAN.create(p_377013_, texturedmodel.getMapping(), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_377013_, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    .with(createHorizontalFacingDispatch())
            );
        this.registerSimpleFlatItemModel(p_378025_);
    }

    private void createStems(Block p_376872_, Block p_376213_) {
        this.registerSimpleFlatItemModel(p_376872_.asItem());
        TextureMapping texturemapping = TextureMapping.stem(p_376872_);
        TextureMapping texturemapping1 = TextureMapping.attachedStem(p_376872_, p_376213_);
        ResourceLocation resourcelocation = ModelTemplates.ATTACHED_STEM.create(p_376213_, texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376213_, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING)
                            .select(Direction.WEST, Variant.variant())
                            .select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                            .select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376872_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_7)
                            .generate(
                                p_376205_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            ModelTemplates.STEMS[p_376205_].create(p_376872_, texturemapping, this.modelOutput)
                                        )
                            )
                    )
            );
    }

    private void createPitcherPlant() {
        Block block = Blocks.PITCHER_PLANT;
        this.registerSimpleFlatItemModel(block.asItem());
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block, "_top");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block, "_bottom");
        this.createDoubleBlock(block, resourcelocation, resourcelocation1);
    }

    private void createPitcherCrop() {
        Block block = Blocks.PITCHER_CROP;
        this.registerSimpleFlatItemModel(block.asItem());
        PropertyDispatch propertydispatch = PropertyDispatch.properties(PitcherCropBlock.AGE, BlockStateProperties.DOUBLE_BLOCK_HALF)
            .generate((p_376280_, p_377384_) -> {
                return switch (p_377384_) {
                    case UPPER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_top_stage_" + p_376280_));
                    case LOWER -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block, "_bottom_stage_" + p_376280_));
                };
            });
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(propertydispatch));
    }

    private void createCoral(
        Block p_378121_, Block p_378514_, Block p_378059_, Block p_376641_, Block p_375482_, Block p_378293_, Block p_375643_, Block p_375706_
    ) {
        this.createCrossBlockWithDefaultItem(p_378121_, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createCrossBlockWithDefaultItem(p_378514_, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTrivialCube(p_378059_);
        this.createTrivialCube(p_376641_);
        this.createCoralFans(p_375482_, p_375643_);
        this.createCoralFans(p_378293_, p_375706_);
    }

    private void createDoublePlant(Block p_377839_, BlockModelGenerators.PlantType p_377454_) {
        ResourceLocation resourcelocation = this.createSuffixedVariant(p_377839_, "_top", p_377454_.getCross(), TextureMapping::cross);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(p_377839_, "_bottom", p_377454_.getCross(), TextureMapping::cross);
        this.createDoubleBlock(p_377839_, resourcelocation, resourcelocation1);
    }

    private void createDoublePlantWithDefaultItem(Block p_376248_, BlockModelGenerators.PlantType p_377473_) {
        this.registerSimpleFlatItemModel(p_376248_, "_top");
        this.createDoublePlant(p_376248_, p_377473_);
    }

    private void createTintedDoublePlant(Block p_377988_) {
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(p_377988_.asItem(), p_377988_, "_top");
        this.registerSimpleTintedItemModel(p_377988_, resourcelocation, new GrassColorSource());
        this.createDoublePlant(p_377988_, BlockModelGenerators.PlantType.TINTED);
    }

    private void createSunflower() {
        this.registerSimpleFlatItemModel(Blocks.SUNFLOWER, "_front");
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SUNFLOWER, "_top");
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.SUNFLOWER, "_bottom", BlockModelGenerators.PlantType.NOT_TINTED.getCross(), TextureMapping::cross
        );
        this.createDoubleBlock(Blocks.SUNFLOWER, resourcelocation, resourcelocation1);
    }

    private void createTallSeagrass() {
        ResourceLocation resourcelocation = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_top", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(Blocks.TALL_SEAGRASS, "_bottom", ModelTemplates.SEAGRASS, TextureMapping::defaultTexture);
        this.createDoubleBlock(Blocks.TALL_SEAGRASS, resourcelocation, resourcelocation1);
    }

    private void createSmallDripleaf() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_top");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SMALL_DRIPLEAF, "_bottom");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SMALL_DRIPLEAF)
                    .with(createHorizontalFacingDispatch())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    )
            );
    }

    private void createDoubleBlock(Block p_376427_, ResourceLocation p_375937_, ResourceLocation p_377516_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376427_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.DOUBLE_BLOCK_HALF)
                            .select(DoubleBlockHalf.LOWER, Variant.variant().with(VariantProperties.MODEL, p_377516_))
                            .select(DoubleBlockHalf.UPPER, Variant.variant().with(VariantProperties.MODEL, p_375937_))
                    )
            );
    }

    private void createPassiveRail(Block p_376794_) {
        TextureMapping texturemapping = TextureMapping.rail(p_376794_);
        TextureMapping texturemapping1 = TextureMapping.rail(TextureMapping.getBlockTexture(p_376794_, "_corner"));
        ResourceLocation resourcelocation = ModelTemplates.RAIL_FLAT.create(p_376794_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.RAIL_CURVED.create(p_376794_, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.RAIL_RAISED_NE.create(p_376794_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.RAIL_RAISED_SW.create(p_376794_, texturemapping, this.modelOutput);
        this.registerSimpleFlatItemModel(p_376794_);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376794_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RAIL_SHAPE)
                            .select(RailShape.NORTH_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                RailShape.EAST_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.ASCENDING_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(RailShape.ASCENDING_NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(RailShape.ASCENDING_SOUTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(RailShape.SOUTH_EAST, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                RailShape.SOUTH_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                RailShape.NORTH_WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                RailShape.NORTH_EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createActiveRail(Block p_378443_) {
        ResourceLocation resourcelocation = this.createSuffixedVariant(p_378443_, "", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(p_378443_, "", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(p_378443_, "", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(p_378443_, "_on", ModelTemplates.RAIL_FLAT, TextureMapping::rail);
        ResourceLocation resourcelocation4 = this.createSuffixedVariant(p_378443_, "_on", ModelTemplates.RAIL_RAISED_NE, TextureMapping::rail);
        ResourceLocation resourcelocation5 = this.createSuffixedVariant(p_378443_, "_on", ModelTemplates.RAIL_RAISED_SW, TextureMapping::rail);
        PropertyDispatch propertydispatch = PropertyDispatch.properties(BlockStateProperties.POWERED, BlockStateProperties.RAIL_SHAPE_STRAIGHT)
            .generate(
                (p_377518_, p_376635_) -> {
                    switch (p_376635_) {
                        case NORTH_SOUTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_377518_ ? resourcelocation3 : resourcelocation);
                        case EAST_WEST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_377518_ ? resourcelocation3 : resourcelocation)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_EAST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_377518_ ? resourcelocation4 : resourcelocation1)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_WEST:
                            return Variant.variant()
                                .with(VariantProperties.MODEL, p_377518_ ? resourcelocation5 : resourcelocation2)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        case ASCENDING_NORTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_377518_ ? resourcelocation4 : resourcelocation1);
                        case ASCENDING_SOUTH:
                            return Variant.variant().with(VariantProperties.MODEL, p_377518_ ? resourcelocation5 : resourcelocation2);
                        default:
                            throw new UnsupportedOperationException("Fix you generator!");
                    }
                }
            );
        this.registerSimpleFlatItemModel(p_378443_);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_378443_).with(propertydispatch));
    }

    private void createAirLikeBlock(Block p_375555_, Item p_377535_) {
        ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(p_375555_, TextureMapping.particleFromItem(p_377535_), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_375555_, resourcelocation));
    }

    private void createAirLikeBlock(Block p_377174_, ResourceLocation p_376318_) {
        ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(p_377174_, TextureMapping.particle(p_376318_), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_377174_, resourcelocation));
    }

    private ResourceLocation createParticleOnlyBlockModel(Block p_376767_, Block p_377465_) {
        return ModelTemplates.PARTICLE_ONLY.create(p_376767_, TextureMapping.particle(p_377465_), this.modelOutput);
    }

    public void createParticleOnlyBlock(Block p_377029_, Block p_376512_) {
        this.blockStateOutput.accept(createSimpleBlock(p_377029_, this.createParticleOnlyBlockModel(p_377029_, p_376512_)));
    }

    private void createParticleOnlyBlock(Block p_378546_) {
        this.createParticleOnlyBlock(p_378546_, p_378546_);
    }

    private void createFullAndCarpetBlocks(Block p_376946_, Block p_377725_) {
        this.createTrivialCube(p_376946_);
        ResourceLocation resourcelocation = TexturedModel.CARPET.get(p_376946_).create(p_377725_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_377725_, resourcelocation));
    }

    private void createFlowerBed(Block p_376829_) {
        this.registerSimpleFlatItemModel(p_376829_.asItem());
        ResourceLocation resourcelocation = TexturedModel.FLOWERBED_1.create(p_376829_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.FLOWERBED_2.create(p_376829_, this.modelOutput);
        ResourceLocation resourcelocation2 = TexturedModel.FLOWERBED_3.create(p_376829_, this.modelOutput);
        ResourceLocation resourcelocation3 = TexturedModel.FLOWERBED_4.create(p_376829_, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(p_376829_)
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 1, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 3, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.FLOWER_AMOUNT, 4).term(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createColoredBlockWithRandomRotations(TexturedModel.Provider p_378645_, Block... p_378778_) {
        for (Block block : p_378778_) {
            ResourceLocation resourcelocation = p_378645_.create(block, this.modelOutput);
            this.blockStateOutput.accept(createRotatedVariant(block, resourcelocation));
        }
    }

    private void createColoredBlockWithStateRotations(TexturedModel.Provider p_377875_, Block... p_378422_) {
        for (Block block : p_378422_) {
            ResourceLocation resourcelocation = p_377875_.create(block, this.modelOutput);
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, resourcelocation)).with(createHorizontalFacingDispatchAlt())
                );
        }
    }

    private void createGlassBlocks(Block p_376058_, Block p_376366_) {
        this.createTrivialCube(p_376058_);
        TextureMapping texturemapping = TextureMapping.pane(p_376058_, p_376366_);
        ResourceLocation resourcelocation = ModelTemplates.STAINED_GLASS_PANE_POST.create(p_376366_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.STAINED_GLASS_PANE_SIDE.create(p_376366_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(p_376366_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(p_376366_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(p_376366_, texturemapping, this.modelOutput);
        Item item = p_376366_.asItem();
        this.registerSimpleItemModel(item, this.createFlatItemModelWithBlockTexture(item, p_376058_));
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(p_376366_)
                    .with(Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createCommandBlock(Block p_377693_) {
        TextureMapping texturemapping = TextureMapping.commandBlock(p_377693_);
        ResourceLocation resourcelocation = ModelTemplates.COMMAND_BLOCK.create(p_377693_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            p_377693_, "_conditional", ModelTemplates.COMMAND_BLOCK, p_378831_ -> texturemapping.copyAndUpdate(TextureSlot.SIDE, p_378831_)
        );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_377693_)
                    .with(createBooleanModelDispatch(BlockStateProperties.CONDITIONAL, resourcelocation1, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createAnvil(Block p_377887_) {
        ResourceLocation resourcelocation = TexturedModel.ANVIL.create(p_377887_, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_377887_, resourcelocation).with(createHorizontalFacingDispatchAlt()));
    }

    private List<Variant> createBambooModels(int p_375570_) {
        String s = "_age" + p_375570_;
        return IntStream.range(1, 5)
            .mapToObj(p_376609_ -> Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, p_376609_ + s)))
            .collect(Collectors.toList());
    }

    private void createBamboo() {
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BAMBOO)
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 0), this.createBambooModels(0))
                    .with(Condition.condition().term(BlockStateProperties.AGE_1, 1), this.createBambooModels(1))
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.SMALL),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_small_leaves"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.BAMBOO_LEAVES, BambooLeaves.LARGE),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.BAMBOO, "_large_leaves"))
                    )
            );
    }

    private PropertyDispatch createColumnWithFacing() {
        return PropertyDispatch.property(BlockStateProperties.FACING)
            .select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180))
            .select(Direction.UP, Variant.variant())
            .select(Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
            .select(
                Direction.SOUTH,
                Variant.variant()
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
            )
            .select(
                Direction.WEST,
                Variant.variant()
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
            )
            .select(
                Direction.EAST,
                Variant.variant()
                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
            );
    }

    private void createBarrel() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.BARREL, "_top_open");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BARREL)
                    .with(this.createColumnWithFacing())
                    .with(
                        PropertyDispatch.property(BlockStateProperties.OPEN)
                            .select(
                                false,
                                Variant.variant().with(VariantProperties.MODEL, TexturedModel.CUBE_TOP_BOTTOM.create(Blocks.BARREL, this.modelOutput))
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        TexturedModel.CUBE_TOP_BOTTOM
                                            .get(Blocks.BARREL)
                                            .updateTextures(p_377357_ -> p_377357_.put(TextureSlot.TOP, resourcelocation))
                                            .createWithSuffix(Blocks.BARREL, "_open", this.modelOutput)
                                    )
                            )
                    )
            );
    }

    private static <T extends Comparable<T>> PropertyDispatch createEmptyOrFullDispatch(
        Property<T> p_377696_, T p_375681_, ResourceLocation p_377862_, ResourceLocation p_377205_
    ) {
        Variant variant = Variant.variant().with(VariantProperties.MODEL, p_377862_);
        Variant variant1 = Variant.variant().with(VariantProperties.MODEL, p_377205_);
        return PropertyDispatch.property(p_377696_).generate(p_375444_ -> {
            boolean flag = p_375444_.compareTo(p_375681_) >= 0;
            return flag ? variant : variant1;
        });
    }

    private void createBeeNest(Block p_377006_, Function<Block, TextureMapping> p_375956_) {
        TextureMapping texturemapping = p_375956_.apply(p_377006_).copyForced(TextureSlot.SIDE, TextureSlot.PARTICLE);
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_377006_, "_front_honey"));
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(p_377006_, "_empty", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.createWithSuffix(p_377006_, "_honey", texturemapping1, this.modelOutput);
        this.itemModelOutput
            .accept(
                p_377006_.asItem(),
                ItemModelUtils.selectBlockItemProperty(
                    BeehiveBlock.HONEY_LEVEL, ItemModelUtils.plainModel(resourcelocation), Map.of(5, ItemModelUtils.plainModel(resourcelocation1))
                )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_377006_)
                    .with(createHorizontalFacingDispatch())
                    .with(createEmptyOrFullDispatch(BeehiveBlock.HONEY_LEVEL, 5, resourcelocation1, resourcelocation))
            );
    }

    private void createCropBlock(Block p_378549_, Property<Integer> p_377514_, int... p_378260_) {
        if (p_377514_.getPossibleValues().size() != p_378260_.length) {
            throw new IllegalArgumentException();
        } else {
            Int2ObjectMap<ResourceLocation> int2objectmap = new Int2ObjectOpenHashMap<>();
            PropertyDispatch propertydispatch = PropertyDispatch.property(p_377514_)
                .generate(
                    p_377486_ -> {
                        int i = p_378260_[p_377486_];
                        ResourceLocation resourcelocation = int2objectmap.computeIfAbsent(
                            i, p_377411_ -> this.createSuffixedVariant(p_378549_, "_stage" + i, ModelTemplates.CROP, TextureMapping::crop)
                        );
                        return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
                    }
                );
            this.registerSimpleFlatItemModel(p_378549_.asItem());
            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_378549_).with(propertydispatch));
        }
    }

    private void createBell() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.BELL, "_floor");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_ceiling");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_wall");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.BELL, "_between_walls");
        this.registerSimpleFlatItemModel(Items.BELL);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.BELL)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.BELL_ATTACHMENT)
                            .select(Direction.NORTH, BellAttachType.FLOOR, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.FLOOR,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.FLOOR,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.FLOOR,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(Direction.NORTH, BellAttachType.CEILING, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                Direction.SOUTH,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.EAST,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.CEILING,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.EAST, BellAttachType.SINGLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.SINGLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation2)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.SOUTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.NORTH,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                Direction.EAST, BellAttachType.DOUBLE_WALL, Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                            )
                            .select(
                                Direction.WEST,
                                BellAttachType.DOUBLE_WALL,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation3)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                    )
            );
    }

    private void createGrindstone() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.GRINDSTONE, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.GRINDSTONE))
                    )
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(
                                AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.SOUTH,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createFurnace(Block p_376661_, TexturedModel.Provider p_378251_) {
        ResourceLocation resourcelocation = p_378251_.create(p_376661_, this.modelOutput);
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(p_376661_, "_front_on");
        ResourceLocation resourcelocation2 = p_378251_.get(p_376661_)
            .updateTextures(p_377718_ -> p_377718_.put(TextureSlot.FRONT, resourcelocation1))
            .createWithSuffix(p_376661_, "_on", this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376661_)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation2, resourcelocation))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createCampfires(Block... p_376654_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("campfire_off");

        for (Block block : p_376654_) {
            ResourceLocation resourcelocation1 = ModelTemplates.CAMPFIRE.create(block, TextureMapping.campfire(block), this.modelOutput);
            this.registerSimpleFlatItemModel(block.asItem());
            this.blockStateOutput
                .accept(
                    MultiVariantGenerator.multiVariant(block)
                        .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation1, resourcelocation))
                        .with(createHorizontalFacingDispatchAlt())
                );
        }
    }

    private void createAzalea(Block p_378737_) {
        ResourceLocation resourcelocation = ModelTemplates.AZALEA.create(p_378737_, TextureMapping.cubeTop(p_378737_), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_378737_, resourcelocation));
    }

    private void createPottedAzalea(Block p_377155_) {
        ResourceLocation resourcelocation;
        if (p_377155_ == Blocks.POTTED_FLOWERING_AZALEA) {
            resourcelocation = ModelTemplates.POTTED_FLOWERING_AZALEA.create(p_377155_, TextureMapping.pottedAzalea(p_377155_), this.modelOutput);
        } else {
            resourcelocation = ModelTemplates.POTTED_AZALEA.create(p_377155_, TextureMapping.pottedAzalea(p_377155_), this.modelOutput);
        }

        this.blockStateOutput.accept(createSimpleBlock(p_377155_, resourcelocation));
    }

    private void createBookshelf() {
        TextureMapping texturemapping = TextureMapping.column(TextureMapping.getBlockTexture(Blocks.BOOKSHELF), TextureMapping.getBlockTexture(Blocks.OAK_PLANKS));
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(Blocks.BOOKSHELF, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.BOOKSHELF, resourcelocation));
    }

    private void createRedstoneWire() {
        this.registerSimpleFlatItemModel(Items.REDSTONE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.REDSTONE_WIRE)
                    .with(
                        Condition.or(
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
                            Condition.condition()
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                            Condition.condition()
                                .term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                                .term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP)
                        ),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_dot"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side_alt1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_side1"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST_REDSTONE, RedstoneSide.UP),
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelLocationUtils.decorateBlockModelLocation("redstone_dust_up"))
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                    )
            );
    }

    private void createComparator() {
        this.registerSimpleFlatItemModel(Items.COMPARATOR);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COMPARATOR)
                    .with(createHorizontalFacingDispatchAlt())
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.MODE_COMPARATOR, BlockStateProperties.POWERED)
                            .select(
                                ComparatorMode.COMPARE,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR))
                            )
                            .select(
                                ComparatorMode.COMPARE,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_subtract"))
                            )
                            .select(
                                ComparatorMode.SUBTRACT,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COMPARATOR, "_on_subtract"))
                            )
                    )
            );
    }

    private void createSmoothStoneSlab() {
        TextureMapping texturemapping = TextureMapping.cube(Blocks.SMOOTH_STONE);
        TextureMapping texturemapping1 = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.SMOOTH_STONE_SLAB, "_side"), texturemapping.get(TextureSlot.TOP)
        );
        ResourceLocation resourcelocation = ModelTemplates.SLAB_BOTTOM.create(Blocks.SMOOTH_STONE_SLAB, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.SLAB_TOP.create(Blocks.SMOOTH_STONE_SLAB, texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_COLUMN.createWithOverride(Blocks.SMOOTH_STONE_SLAB, "_double", texturemapping1, this.modelOutput);
        this.blockStateOutput.accept(createSlab(Blocks.SMOOTH_STONE_SLAB, resourcelocation, resourcelocation1, resourcelocation2));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SMOOTH_STONE, ModelTemplates.CUBE_ALL.create(Blocks.SMOOTH_STONE, texturemapping, this.modelOutput)));
    }

    private void createBrewingStand() {
        this.registerSimpleFlatItemModel(Items.BREWING_STAND);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.BREWING_STAND)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND)))
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, true),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_bottle2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_0, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty0"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_1, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.HAS_BOTTLE_2, false),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.BREWING_STAND, "_empty2"))
                    )
            );
    }

    private void createMushroomBlock(Block p_377576_) {
        ResourceLocation resourcelocation = ModelTemplates.SINGLE_FACE.create(p_377576_, TextureMapping.defaultTexture(p_377576_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelLocationUtils.decorateBlockModelLocation("mushroom_block_inside");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(p_377576_)
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, false)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, false)
                    )
            );
        this.registerSimpleItemModel(p_377576_, TexturedModel.CUBE.createWithSuffix(p_377576_, "_inventory", this.modelOutput));
    }

    private void createCakeBlock() {
        this.registerSimpleFlatItemModel(Items.CAKE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAKE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.BITES)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE)))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice2")))
                            .select(3, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice3")))
                            .select(4, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice4")))
                            .select(5, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice5")))
                            .select(6, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.CAKE, "_slice6")))
                    )
            );
    }

    private void createCartographyTable() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.DARK_OAK_PLANKS))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side3"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side1"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.CARTOGRAPHY_TABLE, "_side2"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.CARTOGRAPHY_TABLE, ModelTemplates.CUBE.create(Blocks.CARTOGRAPHY_TABLE, texturemapping, this.modelOutput)));
    }

    private void createSmithingTable() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_bottom"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_top"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_front"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(Blocks.SMITHING_TABLE, "_side"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SMITHING_TABLE, ModelTemplates.CUBE.create(Blocks.SMITHING_TABLE, texturemapping, this.modelOutput)));
    }

    private void createCraftingTableLike(Block p_377210_, Block p_375763_, BiFunction<Block, Block, TextureMapping> p_378456_) {
        TextureMapping texturemapping = p_378456_.apply(p_377210_, p_375763_);
        this.blockStateOutput.accept(createSimpleBlock(p_377210_, ModelTemplates.CUBE.create(p_377210_, texturemapping, this.modelOutput)));
    }

    public void createGenericCube(Block p_378403_) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(p_378403_, "_particle"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(p_378403_, "_down"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(p_378403_, "_up"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(p_378403_, "_north"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(p_378403_, "_south"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(p_378403_, "_east"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(p_378403_, "_west"));
        this.blockStateOutput.accept(createSimpleBlock(p_378403_, ModelTemplates.CUBE.create(p_378403_, texturemapping, this.modelOutput)));
    }

    private void createPumpkins() {
        TextureMapping texturemapping = TextureMapping.column(Blocks.PUMPKIN);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.PUMPKIN, ModelLocationUtils.getModelLocation(Blocks.PUMPKIN)));
        this.createPumpkinVariant(Blocks.CARVED_PUMPKIN, texturemapping);
        this.createPumpkinVariant(Blocks.JACK_O_LANTERN, texturemapping);
    }

    private void createPumpkinVariant(Block p_376185_, TextureMapping p_377021_) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE
            .create(p_376185_, p_377021_.copyAndUpdate(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_376185_)), this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376185_, Variant.variant().with(VariantProperties.MODEL, resourcelocation)).with(createHorizontalFacingDispatch())
            );
    }

    private void createCauldrons() {
        this.registerSimpleFlatItemModel(Items.CAULDRON);
        this.createNonTemplateModelBlock(Blocks.CAULDRON);
        this.blockStateOutput
            .accept(
                createSimpleBlock(
                    Blocks.LAVA_CAULDRON,
                    ModelTemplates.CAULDRON_FULL
                        .create(Blocks.LAVA_CAULDRON, TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.LAVA, "_still")), this.modelOutput)
                )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.WATER_CAULDRON)
                    .with(
                        PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL1
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_level1",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL2
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_level2",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_FULL
                                            .createWithSuffix(
                                                Blocks.WATER_CAULDRON,
                                                "_full",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.WATER, "_still")),
                                                this.modelOutput
                                            )
                                    )
                            )
                    )
            );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.POWDER_SNOW_CAULDRON)
                    .with(
                        PropertyDispatch.property(LayeredCauldronBlock.LEVEL)
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL1
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_level1",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_LEVEL2
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON,
                                                "_level2",
                                                TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)),
                                                this.modelOutput
                                            )
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.CAULDRON_FULL
                                            .createWithSuffix(
                                                Blocks.POWDER_SNOW_CAULDRON, "_full", TextureMapping.cauldron(TextureMapping.getBlockTexture(Blocks.POWDER_SNOW)), this.modelOutput
                                            )
                                    )
                            )
                    )
            );
    }

    private void createChorusFlower() {
        TextureMapping texturemapping = TextureMapping.defaultTexture(Blocks.CHORUS_FLOWER);
        ResourceLocation resourcelocation = ModelTemplates.CHORUS_FLOWER.create(Blocks.CHORUS_FLOWER, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(
            Blocks.CHORUS_FLOWER, "_dead", ModelTemplates.CHORUS_FLOWER, p_377838_ -> texturemapping.copyAndUpdate(TextureSlot.TEXTURE, p_377838_)
        );
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CHORUS_FLOWER).with(createEmptyOrFullDispatch(BlockStateProperties.AGE_5, 5, resourcelocation1, resourcelocation))
            );
    }

    private void createCrafterBlock() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CRAFTER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_triggered");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.CRAFTER, "_crafting_triggered");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CRAFTER)
                    .with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(p_378460_ -> this.applyRotation(p_378460_, Variant.variant())))
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.TRIGGERED, CrafterBlock.CRAFTING)
                            .select(false, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(true, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(true, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(false, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                    )
            );
    }

    private void createDispenserBlock(Block p_376199_) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_side"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_376199_, "_front"));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.FURNACE, "_top"))
            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(p_376199_, "_front_vertical"));
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ORIENTABLE.create(p_376199_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_ORIENTABLE_VERTICAL.create(p_376199_, texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376199_)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING)
                            .select(
                                Direction.DOWN,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(Direction.UP, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createEndPortalFrame() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.END_PORTAL_FRAME, "_filled");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.END_PORTAL_FRAME)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.EYE)
                            .select(false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(true, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createChorusPlant() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_side");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside1");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside2");
        ResourceLocation resourcelocation4 = ModelLocationUtils.getModelLocation(Blocks.CHORUS_PLANT, "_noside3");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.CHORUS_PLANT)
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1).with(VariantProperties.WEIGHT, 2),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.UP, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                            .with(VariantProperties.UV_LOCK, true)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.DOWN, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation1)
                            .with(VariantProperties.WEIGHT, 2)
                            .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            .with(VariantProperties.UV_LOCK, true)
                    )
            );
    }

    private void createComposter() {
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.COMPOSTER)
                    .with(Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER)))
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 1),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents1"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 2),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents2"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 3),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents3"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 4),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents4"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 5),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents5"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 6),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents6"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 7),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents7"))
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.LEVEL_COMPOSTER, 8),
                        Variant.variant().with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.COMPOSTER, "_contents_ready"))
                    )
            );
    }

    private void createCopperBulb(Block p_377771_) {
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ALL.create(p_377771_, TextureMapping.cube(p_377771_), this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(p_377771_, "_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(p_377771_, "_lit", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(p_377771_, "_lit_powered", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        this.blockStateOutput.accept(this.createCopperBulb(p_377771_, resourcelocation, resourcelocation2, resourcelocation1, resourcelocation3));
    }

    private BlockStateGenerator createCopperBulb(
        Block p_376664_, ResourceLocation p_377659_, ResourceLocation p_376995_, ResourceLocation p_377276_, ResourceLocation p_375930_
    ) {
        return MultiVariantGenerator.multiVariant(p_376664_)
            .with(
                PropertyDispatch.properties(BlockStateProperties.LIT, BlockStateProperties.POWERED)
                    .generate(
                        (p_378586_, p_377919_) -> p_378586_
                                ? Variant.variant().with(VariantProperties.MODEL, p_377919_ ? p_375930_ : p_376995_)
                                : Variant.variant().with(VariantProperties.MODEL, p_377919_ ? p_377276_ : p_377659_)
                    )
            );
    }

    private void copyCopperBulbModel(Block p_377765_, Block p_378253_) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_377765_);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(p_377765_, "_powered");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(p_377765_, "_lit");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(p_377765_, "_lit_powered");
        this.itemModelOutput.copy(p_377765_.asItem(), p_378253_.asItem());
        this.blockStateOutput.accept(this.createCopperBulb(p_378253_, resourcelocation, resourcelocation2, resourcelocation1, resourcelocation3));
    }

    private void createAmethystCluster(Block p_376834_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        p_376834_,
                        Variant.variant()
                            .with(
                                VariantProperties.MODEL, ModelTemplates.CROSS.create(p_376834_, TextureMapping.cross(p_376834_), this.modelOutput)
                            )
                    )
                    .with(this.createColumnWithFacing())
            );
    }

    private void createAmethystClusters() {
        this.createAmethystCluster(Blocks.SMALL_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.MEDIUM_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.LARGE_AMETHYST_BUD);
        this.createAmethystCluster(Blocks.AMETHYST_CLUSTER);
    }

    private void createPointedDripstone() {
        PropertyDispatch.C2<Direction, DripstoneThickness> c2 = PropertyDispatch.properties(BlockStateProperties.VERTICAL_DIRECTION, BlockStateProperties.DRIPSTONE_THICKNESS);

        for (DripstoneThickness dripstonethickness : DripstoneThickness.values()) {
            c2.select(Direction.UP, dripstonethickness, this.createPointedDripstoneVariant(Direction.UP, dripstonethickness));
        }

        for (DripstoneThickness dripstonethickness1 : DripstoneThickness.values()) {
            c2.select(Direction.DOWN, dripstonethickness1, this.createPointedDripstoneVariant(Direction.DOWN, dripstonethickness1));
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.POINTED_DRIPSTONE).with(c2));
    }

    private Variant createPointedDripstoneVariant(Direction p_377266_, DripstoneThickness p_377993_) {
        String s = "_" + p_377266_.getSerializedName() + "_" + p_377993_.getSerializedName();
        TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.POINTED_DRIPSTONE, s));
        return Variant.variant()
            .with(VariantProperties.MODEL, ModelTemplates.POINTED_DRIPSTONE.createWithSuffix(Blocks.POINTED_DRIPSTONE, s, texturemapping, this.modelOutput));
    }

    private void createNyliumBlock(Block p_375546_) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.NETHERRACK))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(p_375546_))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_375546_, "_side"));
        this.blockStateOutput.accept(createSimpleBlock(p_375546_, ModelTemplates.CUBE_BOTTOM_TOP.create(p_375546_, texturemapping, this.modelOutput)));
    }

    private void createDaylightDetector() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_side");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_top"))
            .put(TextureSlot.SIDE, resourcelocation);
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.DAYLIGHT_DETECTOR, "_inverted_top"))
            .put(TextureSlot.SIDE, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.DAYLIGHT_DETECTOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.INVERTED)
                            .select(
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelTemplates.DAYLIGHT_DETECTOR.create(Blocks.DAYLIGHT_DETECTOR, texturemapping, this.modelOutput))
                            )
                            .select(
                                true,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.DAYLIGHT_DETECTOR
                                            .create(ModelLocationUtils.getModelLocation(Blocks.DAYLIGHT_DETECTOR, "_inverted"), texturemapping1, this.modelOutput)
                                    )
                            )
                    )
            );
    }

    private void createRotatableColumn(Block p_378792_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_378792_, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(p_378792_)))
                    .with(this.createColumnWithFacing())
            );
    }

    private void createLightningRod() {
        Block block = Blocks.LIGHTNING_ROD;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block, "_on");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(block)))
                    .with(this.createColumnWithFacing())
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation, resourcelocation1))
            );
    }

    private void createFarmland() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.DIRT, TextureMapping.getBlockTexture(Blocks.DIRT))
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"));
        ResourceLocation resourcelocation = ModelTemplates.FARMLAND.create(Blocks.FARMLAND, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FARMLAND
            .create(TextureMapping.getBlockTexture(Blocks.FARMLAND, "_moist"), texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.FARMLAND).with(createEmptyOrFullDispatch(BlockStateProperties.MOISTURE, 7, resourcelocation1, resourcelocation))
            );
    }

    private List<ResourceLocation> createFloorFireModels(Block p_378241_) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(p_378241_, "_floor0"), TextureMapping.fire0(p_378241_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_FLOOR
            .create(ModelLocationUtils.getModelLocation(p_378241_, "_floor1"), TextureMapping.fire1(p_378241_), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1);
    }

    private List<ResourceLocation> createSideFireModels(Block p_376173_) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(p_376173_, "_side0"), TextureMapping.fire0(p_376173_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_SIDE
            .create(ModelLocationUtils.getModelLocation(p_376173_, "_side1"), TextureMapping.fire1(p_376173_), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(p_376173_, "_side_alt0"), TextureMapping.fire0(p_376173_), this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FIRE_SIDE_ALT
            .create(ModelLocationUtils.getModelLocation(p_376173_, "_side_alt1"), TextureMapping.fire1(p_376173_), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3);
    }

    private List<ResourceLocation> createTopFireModels(Block p_375647_) {
        ResourceLocation resourcelocation = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(p_375647_, "_up0"), TextureMapping.fire0(p_375647_), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.FIRE_UP
            .create(ModelLocationUtils.getModelLocation(p_375647_, "_up1"), TextureMapping.fire1(p_375647_), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(p_375647_, "_up_alt0"), TextureMapping.fire0(p_375647_), this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FIRE_UP_ALT
            .create(ModelLocationUtils.getModelLocation(p_375647_, "_up_alt1"), TextureMapping.fire1(p_375647_), this.modelOutput);
        return ImmutableList.of(resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3);
    }

    private static List<Variant> wrapModels(List<ResourceLocation> p_375990_, UnaryOperator<Variant> p_377102_) {
        return p_375990_.stream()
            .map(p_376109_ -> Variant.variant().with(VariantProperties.MODEL, p_376109_))
            .map(p_377102_)
            .collect(Collectors.toList());
    }

    private void createFire() {
        Condition condition = Condition.condition()
            .term(BlockStateProperties.NORTH, false)
            .term(BlockStateProperties.EAST, false)
            .term(BlockStateProperties.SOUTH, false)
            .term(BlockStateProperties.WEST, false)
            .term(BlockStateProperties.UP, false);
        List<ResourceLocation> list = this.createFloorFireModels(Blocks.FIRE);
        List<ResourceLocation> list1 = this.createSideFireModels(Blocks.FIRE);
        List<ResourceLocation> list2 = this.createTopFireModels(Blocks.FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.FIRE)
                    .with(condition, wrapModels(list, p_375424_ -> p_375424_))
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.NORTH, true), condition),
                        wrapModels(list1, p_378300_ -> p_378300_)
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.EAST, true), condition),
                        wrapModels(list1, p_378247_ -> p_378247_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.SOUTH, true), condition),
                        wrapModels(list1, p_376235_ -> p_376235_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                    )
                    .with(
                        Condition.or(Condition.condition().term(BlockStateProperties.WEST, true), condition),
                        wrapModels(list1, p_377462_ -> p_377462_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                    )
                    .with(Condition.condition().term(BlockStateProperties.UP, true), wrapModels(list2, p_378122_ -> p_378122_))
            );
    }

    private void createSoulFire() {
        List<ResourceLocation> list = this.createFloorFireModels(Blocks.SOUL_FIRE);
        List<ResourceLocation> list1 = this.createSideFireModels(Blocks.SOUL_FIRE);
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.SOUL_FIRE)
                    .with(wrapModels(list, p_378657_ -> p_378657_))
                    .with(wrapModels(list1, p_377786_ -> p_377786_))
                    .with(wrapModels(list1, p_377835_ -> p_377835_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)))
                    .with(wrapModels(list1, p_376288_ -> p_376288_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)))
                    .with(wrapModels(list1, p_378429_ -> p_378429_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)))
            );
    }

    private void createLantern(Block p_376956_) {
        ResourceLocation resourcelocation = TexturedModel.LANTERN.create(p_376956_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.HANGING_LANTERN.create(p_376956_, this.modelOutput);
        this.registerSimpleFlatItemModel(p_376956_.asItem());
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(p_376956_).with(createBooleanModelDispatch(BlockStateProperties.HANGING, resourcelocation1, resourcelocation)));
    }

    private void createMuddyMangroveRoots() {
        TextureMapping texturemapping = TextureMapping.column(
            TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_side"), TextureMapping.getBlockTexture(Blocks.MUDDY_MANGROVE_ROOTS, "_top")
        );
        ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(Blocks.MUDDY_MANGROVE_ROOTS, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createAxisAlignedPillarBlock(Blocks.MUDDY_MANGROVE_ROOTS, resourcelocation));
    }

    private void createMangrovePropagule() {
        this.registerSimpleFlatItemModel(Items.MANGROVE_PROPAGULE);
        Block block = Blocks.MANGROVE_PROPAGULE;
        PropertyDispatch.C2<Boolean, Integer> c2 = PropertyDispatch.properties(MangrovePropaguleBlock.HANGING, MangrovePropaguleBlock.AGE);
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);

        for (int i = 0; i <= 4; i++) {
            ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(block, "_hanging_" + i);
            c2.select(true, i, Variant.variant().with(VariantProperties.MODEL, resourcelocation1));
            c2.select(false, i, Variant.variant().with(VariantProperties.MODEL, resourcelocation));
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.MANGROVE_PROPAGULE).with(c2));
    }

    private void createFrostedIce() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.FROSTED_ICE)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .select(
                                0,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_0", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                1,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_1", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                2,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_2", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                            .select(
                                3,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL, this.createSuffixedVariant(Blocks.FROSTED_ICE, "_3", ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                    )
                            )
                    )
            );
    }

    private void createGrassBlocks() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.DIRT);
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.GRASS_BLOCK, "_snow"));
        Variant variant = Variant.variant()
            .with(VariantProperties.MODEL, ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.GRASS_BLOCK, "_snow", texturemapping, this.modelOutput));
        this.createGrassLikeBlock(Blocks.GRASS_BLOCK, ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK), variant);
        this.registerSimpleTintedItemModel(Blocks.GRASS_BLOCK, ModelLocationUtils.getModelLocation(Blocks.GRASS_BLOCK), new GrassColorSource());
        ResourceLocation resourcelocation1 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.MYCELIUM)
            .updateTextures(p_376559_ -> p_376559_.put(TextureSlot.BOTTOM, resourcelocation))
            .create(Blocks.MYCELIUM, this.modelOutput);
        this.createGrassLikeBlock(Blocks.MYCELIUM, resourcelocation1, variant);
        ResourceLocation resourcelocation2 = TexturedModel.CUBE_TOP_BOTTOM
            .get(Blocks.PODZOL)
            .updateTextures(p_378353_ -> p_378353_.put(TextureSlot.BOTTOM, resourcelocation))
            .create(Blocks.PODZOL, this.modelOutput);
        this.createGrassLikeBlock(Blocks.PODZOL, resourcelocation2, variant);
    }

    private void createGrassLikeBlock(Block p_378702_, ResourceLocation p_376191_, Variant p_375414_) {
        List<Variant> list = Arrays.asList(createRotatedVariants(p_376191_));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_378702_)
                    .with(PropertyDispatch.property(BlockStateProperties.SNOWY).select(true, p_375414_).select(false, list))
            );
    }

    private void createCocoa() {
        this.registerSimpleFlatItemModel(Items.COCOA_BEANS);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.COCOA)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_2)
                            .select(0, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage0")))
                            .select(1, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage1")))
                            .select(2, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.COCOA, "_stage2")))
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createDirtPath() {
        this.blockStateOutput.accept(createRotatedVariant(Blocks.DIRT_PATH, ModelLocationUtils.getModelLocation(Blocks.DIRT_PATH)));
    }

    private void createWeightedPressurePlate(Block p_376071_, Block p_375591_) {
        TextureMapping texturemapping = TextureMapping.defaultTexture(p_375591_);
        ResourceLocation resourcelocation = ModelTemplates.PRESSURE_PLATE_UP.create(p_376071_, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.PRESSURE_PLATE_DOWN.create(p_376071_, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(p_376071_).with(createEmptyOrFullDispatch(BlockStateProperties.POWER, 1, resourcelocation1, resourcelocation)));
    }

    private void createHopper() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.HOPPER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.HOPPER, "_side");
        this.registerSimpleFlatItemModel(Items.HOPPER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.HOPPER)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.FACING_HOPPER)
                            .select(Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, resourcelocation1)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void copyModel(Block p_376488_, Block p_376633_) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_376488_);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_376633_, Variant.variant().with(VariantProperties.MODEL, resourcelocation)));
        this.itemModelOutput.copy(p_376488_.asItem(), p_376633_.asItem());
    }

    private void createIronBars() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post_ends");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_post");
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap");
        ResourceLocation resourcelocation3 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_cap_alt");
        ResourceLocation resourcelocation4 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side");
        ResourceLocation resourcelocation5 = ModelLocationUtils.getModelLocation(Blocks.IRON_BARS, "_side_alt");
        this.blockStateOutput
            .accept(
                MultiPartGenerator.multiPart(Blocks.IRON_BARS)
                    .with(Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation1)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, true)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation2)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, true)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation2)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, true)
                            .term(BlockStateProperties.WEST, false),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation3)
                    )
                    .with(
                        Condition.condition()
                            .term(BlockStateProperties.NORTH, false)
                            .term(BlockStateProperties.EAST, false)
                            .term(BlockStateProperties.SOUTH, false)
                            .term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation3)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.NORTH, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation4)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.EAST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation4)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                        Variant.variant().with(VariantProperties.MODEL, resourcelocation5)
                    )
                    .with(
                        Condition.condition().term(BlockStateProperties.WEST, true),
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation5)
                            .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                    )
            );
        this.registerSimpleFlatItemModel(Blocks.IRON_BARS);
    }

    private void createNonTemplateHorizontalBlock(Block p_375983_) {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_375983_, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(p_375983_)))
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createLever() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.LEVER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.LEVER, "_on");
        this.registerSimpleFlatItemModel(Blocks.LEVER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.LEVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation, resourcelocation1))
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACH_FACE, BlockStateProperties.HORIZONTAL_FACING)
                            .select(
                                AttachFace.CEILING,
                                Direction.NORTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.SOUTH,
                                Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.CEILING,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(AttachFace.FLOOR, Direction.NORTH, Variant.variant())
                            .select(
                                AttachFace.FLOOR, Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.FLOOR, Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.FLOOR, Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                AttachFace.WALL, Direction.NORTH, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.EAST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.SOUTH,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                AttachFace.WALL,
                                Direction.WEST,
                                Variant.variant()
                                    .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                    )
            );
    }

    private void createLilyPad() {
        ResourceLocation resourcelocation = this.createFlatItemModelWithBlockTexture(Items.LILY_PAD, Blocks.LILY_PAD);
        this.registerSimpleTintedItemModel(Blocks.LILY_PAD, resourcelocation, ItemModelUtils.constantTint(-9321636));
        this.blockStateOutput.accept(createRotatedVariant(Blocks.LILY_PAD, ModelLocationUtils.getModelLocation(Blocks.LILY_PAD)));
    }

    private void createFrogspawnBlock() {
        this.registerSimpleFlatItemModel(Blocks.FROGSPAWN);
        this.blockStateOutput.accept(createSimpleBlock(Blocks.FROGSPAWN, ModelLocationUtils.getModelLocation(Blocks.FROGSPAWN)));
    }

    private void createNetherPortalBlock() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.NETHER_PORTAL)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.HORIZONTAL_AXIS)
                            .select(
                                Direction.Axis.X,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ns"))
                            )
                            .select(
                                Direction.Axis.Z,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.NETHER_PORTAL, "_ew"))
                            )
                    )
            );
    }

    private void createNetherrack() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.NETHERRACK, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                    Blocks.NETHERRACK,
                    Variant.variant().with(VariantProperties.MODEL, resourcelocation),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R180),
                    Variant.variant()
                        .with(VariantProperties.MODEL, resourcelocation)
                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                )
            );
    }

    private void createObserver() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.OBSERVER);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.OBSERVER, "_on");
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.OBSERVER)
                    .with(createBooleanModelDispatch(BlockStateProperties.POWERED, resourcelocation1, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createPistons() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.PISTON, "_bottom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.PISTON, "_top");
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, resourcelocation);
        TextureMapping texturemapping2 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, resourcelocation1);
        ResourceLocation resourcelocation2 = ModelLocationUtils.getModelLocation(Blocks.PISTON, "_base");
        this.createPistonVariant(Blocks.PISTON, resourcelocation2, texturemapping2);
        this.createPistonVariant(Blocks.STICKY_PISTON, resourcelocation2, texturemapping1);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.PISTON, "_inventory", texturemapping.copyAndUpdate(TextureSlot.TOP, resourcelocation1), this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_BOTTOM_TOP
            .createWithSuffix(Blocks.STICKY_PISTON, "_inventory", texturemapping.copyAndUpdate(TextureSlot.TOP, resourcelocation), this.modelOutput);
        this.registerSimpleItemModel(Blocks.PISTON, resourcelocation3);
        this.registerSimpleItemModel(Blocks.STICKY_PISTON, resourcelocation4);
    }

    private void createPistonVariant(Block p_377085_, ResourceLocation p_375394_, TextureMapping p_377851_) {
        ResourceLocation resourcelocation = ModelTemplates.PISTON.create(p_377085_, p_377851_, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_377085_)
                    .with(createBooleanModelDispatch(BlockStateProperties.EXTENDED, p_375394_, resourcelocation))
                    .with(createFacingDispatch())
            );
    }

    private void createPistonHeads() {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.UNSTICKY, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        TextureMapping texturemapping1 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top_sticky"));
        TextureMapping texturemapping2 = texturemapping.copyAndUpdate(TextureSlot.PLATFORM, TextureMapping.getBlockTexture(Blocks.PISTON, "_top"));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.PISTON_HEAD)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.SHORT, BlockStateProperties.PISTON_TYPE)
                            .select(
                                false,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head", texturemapping2, this.modelOutput)
                                    )
                            )
                            .select(
                                false,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD.createWithSuffix(Blocks.PISTON, "_head_sticky", texturemapping1, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.DEFAULT,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short", texturemapping2, this.modelOutput)
                                    )
                            )
                            .select(
                                true,
                                PistonType.STICKY,
                                Variant.variant()
                                    .with(
                                        VariantProperties.MODEL,
                                        ModelTemplates.PISTON_HEAD_SHORT.createWithSuffix(Blocks.PISTON, "_head_short_sticky", texturemapping1, this.modelOutput)
                                    )
                            )
                    )
                    .with(createFacingDispatch())
            );
    }

    private void createTrialSpawner() {
        Block block = Blocks.TRIAL_SPAWNER;
        TextureMapping texturemapping = TextureMapping.trialSpawner(block, "_side_inactive", "_top_inactive");
        TextureMapping texturemapping1 = TextureMapping.trialSpawner(block, "_side_active", "_top_active");
        TextureMapping texturemapping2 = TextureMapping.trialSpawner(block, "_side_active", "_top_ejecting_reward");
        TextureMapping texturemapping3 = TextureMapping.trialSpawner(block, "_side_inactive_ominous", "_top_inactive_ominous");
        TextureMapping texturemapping4 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_active_ominous");
        TextureMapping texturemapping5 = TextureMapping.trialSpawner(block, "_side_active_ominous", "_top_ejecting_reward_ominous");
        ResourceLocation resourcelocation = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.create(block, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_active", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_ejecting_reward", texturemapping2, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_inactive_ominous", texturemapping3, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_active_ominous", texturemapping4, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.CUBE_BOTTOM_TOP_INNER_FACES.createWithSuffix(block, "_ejecting_reward_ominous", texturemapping5, this.modelOutput);
        this.registerSimpleItemModel(block, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.TRIAL_SPAWNER_STATE, BlockStateProperties.OMINOUS)
                            .generate(
                                (p_376979_, p_378033_) -> {
                                    return switch (p_376979_) {
                                        case INACTIVE, COOLDOWN -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_378033_ ? resourcelocation3 : resourcelocation);
                                        case WAITING_FOR_PLAYERS, ACTIVE, WAITING_FOR_REWARD_EJECTION -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_378033_ ? resourcelocation4 : resourcelocation1);
                                        case EJECTING_REWARD -> Variant.variant()
                                        .with(VariantProperties.MODEL, p_378033_ ? resourcelocation5 : resourcelocation2);
                                    };
                                }
                            )
                    )
            );
    }

    private void createVault() {
        Block block = Blocks.VAULT;
        TextureMapping texturemapping = TextureMapping.vault(block, "_front_off", "_side_off", "_top", "_bottom");
        TextureMapping texturemapping1 = TextureMapping.vault(block, "_front_on", "_side_on", "_top", "_bottom");
        TextureMapping texturemapping2 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top", "_bottom");
        TextureMapping texturemapping3 = TextureMapping.vault(block, "_front_ejecting", "_side_on", "_top_ejecting", "_bottom");
        ResourceLocation resourcelocation = ModelTemplates.VAULT.create(block, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.VAULT.createWithSuffix(block, "_active", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.VAULT.createWithSuffix(block, "_unlocking", texturemapping2, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward", texturemapping3, this.modelOutput);
        TextureMapping texturemapping4 = TextureMapping.vault(block, "_front_off_ominous", "_side_off_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping5 = TextureMapping.vault(block, "_front_on_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping6 = TextureMapping.vault(block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ominous", "_bottom_ominous");
        TextureMapping texturemapping7 = TextureMapping.vault(
            block, "_front_ejecting_ominous", "_side_on_ominous", "_top_ejecting_ominous", "_bottom_ominous"
        );
        ResourceLocation resourcelocation4 = ModelTemplates.VAULT.createWithSuffix(block, "_ominous", texturemapping4, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.VAULT.createWithSuffix(block, "_active_ominous", texturemapping5, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.VAULT.createWithSuffix(block, "_unlocking_ominous", texturemapping6, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.VAULT.createWithSuffix(block, "_ejecting_reward_ominous", texturemapping7, this.modelOutput);
        this.registerSimpleItemModel(block, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(block)
                    .with(createHorizontalFacingDispatch())
                    .with(PropertyDispatch.properties(VaultBlock.STATE, VaultBlock.OMINOUS).generate((p_376514_, p_378803_) -> {
                        return switch (p_376514_) {
                            case INACTIVE -> Variant.variant().with(VariantProperties.MODEL, p_378803_ ? resourcelocation4 : resourcelocation);
                            case ACTIVE -> Variant.variant().with(VariantProperties.MODEL, p_378803_ ? resourcelocation5 : resourcelocation1);
                            case UNLOCKING -> Variant.variant().with(VariantProperties.MODEL, p_378803_ ? resourcelocation6 : resourcelocation2);
                            case EJECTING -> Variant.variant().with(VariantProperties.MODEL, p_378803_ ? resourcelocation7 : resourcelocation3);
                        };
                    }))
            );
    }

    private void createSculkSensor() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_inactive");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SCULK_SENSOR, "_active");
        this.registerSimpleItemModel(Blocks.SCULK_SENSOR, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_SENSOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
                            .generate(
                                p_376441_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_376441_ != SculkSensorPhase.ACTIVE && p_376441_ != SculkSensorPhase.COOLDOWN
                                                ? resourcelocation
                                                : resourcelocation1
                                        )
                            )
                    )
            );
    }

    private void createCalibratedSculkSensor() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_inactive");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.CALIBRATED_SCULK_SENSOR, "_active");
        this.registerSimpleItemModel(Blocks.CALIBRATED_SCULK_SENSOR, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CALIBRATED_SCULK_SENSOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.SCULK_SENSOR_PHASE)
                            .generate(
                                p_378271_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_378271_ != SculkSensorPhase.ACTIVE && p_378271_ != SculkSensorPhase.COOLDOWN
                                                ? resourcelocation
                                                : resourcelocation1
                                        )
                            )
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createSculkShrieker() {
        ResourceLocation resourcelocation = ModelTemplates.SCULK_SHRIEKER.create(Blocks.SCULK_SHRIEKER, TextureMapping.sculkShrieker(false), this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.SCULK_SHRIEKER
            .createWithSuffix(Blocks.SCULK_SHRIEKER, "_can_summon", TextureMapping.sculkShrieker(true), this.modelOutput);
        this.registerSimpleItemModel(Blocks.SCULK_SHRIEKER, resourcelocation);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.SCULK_SHRIEKER).with(createBooleanModelDispatch(BlockStateProperties.CAN_SUMMON, resourcelocation1, resourcelocation)));
    }

    private void createScaffolding() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_stable");
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.SCAFFOLDING, "_unstable");
        this.registerSimpleItemModel(Blocks.SCAFFOLDING, resourcelocation);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.SCAFFOLDING).with(createBooleanModelDispatch(BlockStateProperties.BOTTOM, resourcelocation1, resourcelocation)));
    }

    private void createCaveVines() {
        ResourceLocation resourcelocation = this.createSuffixedVariant(Blocks.CAVE_VINES, "", ModelTemplates.CROSS, TextureMapping::cross);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(Blocks.CAVE_VINES, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES).with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourcelocation1, resourcelocation)));
        ResourceLocation resourcelocation2 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "", ModelTemplates.CROSS, TextureMapping::cross);
        ResourceLocation resourcelocation3 = this.createSuffixedVariant(Blocks.CAVE_VINES_PLANT, "_lit", ModelTemplates.CROSS, TextureMapping::cross);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.CAVE_VINES_PLANT).with(createBooleanModelDispatch(BlockStateProperties.BERRIES, resourcelocation3, resourcelocation2))
            );
    }

    private void createRedstoneLamp() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.REDSTONE_LAMP, this.modelOutput);
        ResourceLocation resourcelocation1 = this.createSuffixedVariant(Blocks.REDSTONE_LAMP, "_on", ModelTemplates.CUBE_ALL, TextureMapping::cube);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.REDSTONE_LAMP).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation1, resourcelocation)));
    }

    private void createNormalTorch(Block p_377444_, Block p_377353_) {
        TextureMapping texturemapping = TextureMapping.torch(p_377444_);
        this.blockStateOutput.accept(createSimpleBlock(p_377444_, ModelTemplates.TORCH.create(p_377444_, texturemapping, this.modelOutput)));
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        p_377353_,
                        Variant.variant()
                            .with(VariantProperties.MODEL, ModelTemplates.WALL_TORCH.create(p_377353_, texturemapping, this.modelOutput))
                    )
                    .with(createTorchHorizontalDispatch())
            );
        this.registerSimpleFlatItemModel(p_377444_);
    }

    private void createRedstoneTorch() {
        TextureMapping texturemapping = TextureMapping.torch(Blocks.REDSTONE_TORCH);
        TextureMapping texturemapping1 = TextureMapping.torch(TextureMapping.getBlockTexture(Blocks.REDSTONE_TORCH, "_off"));
        ResourceLocation resourcelocation = ModelTemplates.REDSTONE_TORCH.create(Blocks.REDSTONE_TORCH, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TORCH_UNLIT.createWithSuffix(Blocks.REDSTONE_TORCH, "_off", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(Blocks.REDSTONE_TORCH).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation, resourcelocation1)));
        ResourceLocation resourcelocation2 = ModelTemplates.REDSTONE_WALL_TORCH.create(Blocks.REDSTONE_WALL_TORCH, texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.WALL_TORCH_UNLIT.createWithSuffix(Blocks.REDSTONE_WALL_TORCH, "_off", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REDSTONE_WALL_TORCH)
                    .with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation2, resourcelocation3))
                    .with(createTorchHorizontalDispatch())
            );
        this.registerSimpleFlatItemModel(Blocks.REDSTONE_TORCH);
    }

    private void createRepeater() {
        this.registerSimpleFlatItemModel(Items.REPEATER);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.REPEATER)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.DELAY, BlockStateProperties.LOCKED, BlockStateProperties.POWERED)
                            .generate((p_376008_, p_376805_, p_376003_) -> {
                                StringBuilder stringbuilder = new StringBuilder();
                                stringbuilder.append('_').append(p_376008_).append("tick");
                                if (p_376003_) {
                                    stringbuilder.append("_on");
                                }

                                if (p_376805_) {
                                    stringbuilder.append("_locked");
                                }

                                return Variant.variant()
                                    .with(VariantProperties.MODEL, TextureMapping.getBlockTexture(Blocks.REPEATER, stringbuilder.toString()));
                            })
                    )
                    .with(createHorizontalFacingDispatchAlt())
            );
    }

    private void createSeaPickle() {
        this.registerSimpleFlatItemModel(Items.SEA_PICKLE);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SEA_PICKLE)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.PICKLES, BlockStateProperties.WATERLOGGED)
                            .select(1, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("dead_sea_pickle"))))
                            .select(2, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_dead_sea_pickles"))))
                            .select(3, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_dead_sea_pickles"))))
                            .select(4, false, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_dead_sea_pickles"))))
                            .select(1, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("sea_pickle"))))
                            .select(2, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("two_sea_pickles"))))
                            .select(3, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("three_sea_pickles"))))
                            .select(4, true, Arrays.asList(createRotatedVariants(ModelLocationUtils.decorateBlockModelLocation("four_sea_pickles"))))
                    )
            );
    }

    private void createSnowBlocks() {
        TextureMapping texturemapping = TextureMapping.cube(Blocks.SNOW);
        ResourceLocation resourcelocation = ModelTemplates.CUBE_ALL.create(Blocks.SNOW_BLOCK, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SNOW)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.LAYERS)
                            .generate(
                                p_377925_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            p_377925_ < 8 ? ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height" + p_377925_ * 2) : resourcelocation
                                        )
                            )
                    )
            );
        this.registerSimpleItemModel(Blocks.SNOW, ModelLocationUtils.getModelLocation(Blocks.SNOW, "_height2"));
        this.blockStateOutput.accept(createSimpleBlock(Blocks.SNOW_BLOCK, resourcelocation));
    }

    private void createStonecutter() {
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(
                        Blocks.STONECUTTER, Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.STONECUTTER))
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private void createStructureBlock() {
        ResourceLocation resourcelocation = TexturedModel.CUBE.create(Blocks.STRUCTURE_BLOCK, this.modelOutput);
        this.registerSimpleItemModel(Blocks.STRUCTURE_BLOCK, resourcelocation);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.STRUCTURE_BLOCK)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.STRUCTUREBLOCK_MODE)
                            .generate(
                                p_377722_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(Blocks.STRUCTURE_BLOCK, "_" + p_377722_.getSerializedName(), ModelTemplates.CUBE_ALL, TextureMapping::cube)
                                        )
                            )
                    )
            );
    }

    private void createSweetBerryBush() {
        this.registerSimpleFlatItemModel(Items.SWEET_BERRIES);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SWEET_BERRY_BUSH)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.AGE_3)
                            .generate(
                                p_377609_ -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            this.createSuffixedVariant(Blocks.SWEET_BERRY_BUSH, "_stage" + p_377609_, ModelTemplates.CROSS, TextureMapping::cross)
                                        )
                            )
                    )
            );
    }

    private void createTripwire() {
        this.registerSimpleFlatItemModel(Items.STRING);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE)
                    .with(
                        PropertyDispatch.properties(
                                BlockStateProperties.ATTACHED,
                                BlockStateProperties.EAST,
                                BlockStateProperties.NORTH,
                                BlockStateProperties.SOUTH,
                                BlockStateProperties.WEST
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                            )
                            .select(
                                false,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                false,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                false,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                false,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_nsew"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_n"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                false,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                false,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ne"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_ns"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                false,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                            )
                            .select(
                                true,
                                true,
                                false,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                            )
                            .select(
                                true,
                                false,
                                true,
                                true,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                            )
                            .select(
                                true,
                                true,
                                true,
                                false,
                                true,
                                Variant.variant()
                                    .with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nse"))
                                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                            )
                            .select(
                                true,
                                true,
                                true,
                                true,
                                true,
                                Variant.variant().with(VariantProperties.MODEL, ModelLocationUtils.getModelLocation(Blocks.TRIPWIRE, "_attached_nsew"))
                            )
                    )
            );
    }

    private void createTripwireHook() {
        this.registerSimpleFlatItemModel(Blocks.TRIPWIRE_HOOK);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TRIPWIRE_HOOK)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.ATTACHED, BlockStateProperties.POWERED)
                            .generate(
                                (p_378358_, p_376642_) -> Variant.variant()
                                        .with(
                                            VariantProperties.MODEL,
                                            TextureMapping.getBlockTexture(Blocks.TRIPWIRE_HOOK, (p_378358_ ? "_attached" : "") + (p_376642_ ? "_on" : ""))
                                        )
                            )
                    )
                    .with(createHorizontalFacingDispatch())
            );
    }

    private ResourceLocation createTurtleEggModel(int p_375779_, String p_376950_, TextureMapping p_378641_) {
        switch (p_375779_) {
            case 1:
                return ModelTemplates.TURTLE_EGG.create(ModelLocationUtils.decorateBlockModelLocation(p_376950_ + "turtle_egg"), p_378641_, this.modelOutput);
            case 2:
                return ModelTemplates.TWO_TURTLE_EGGS.create(ModelLocationUtils.decorateBlockModelLocation("two_" + p_376950_ + "turtle_eggs"), p_378641_, this.modelOutput);
            case 3:
                return ModelTemplates.THREE_TURTLE_EGGS.create(ModelLocationUtils.decorateBlockModelLocation("three_" + p_376950_ + "turtle_eggs"), p_378641_, this.modelOutput);
            case 4:
                return ModelTemplates.FOUR_TURTLE_EGGS.create(ModelLocationUtils.decorateBlockModelLocation("four_" + p_376950_ + "turtle_eggs"), p_378641_, this.modelOutput);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private ResourceLocation createTurtleEggModel(Integer p_377508_, Integer p_376221_) {
        switch (p_376221_) {
            case 0:
                return this.createTurtleEggModel(p_377508_, "", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG)));
            case 1:
                return this.createTurtleEggModel(p_377508_, "slightly_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_slightly_cracked")));
            case 2:
                return this.createTurtleEggModel(p_377508_, "very_cracked_", TextureMapping.cube(TextureMapping.getBlockTexture(Blocks.TURTLE_EGG, "_very_cracked")));
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void createTurtleEgg() {
        this.registerSimpleFlatItemModel(Items.TURTLE_EGG);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.TURTLE_EGG)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.EGGS, BlockStateProperties.HATCH)
                            .generateList((p_377380_, p_377113_) -> Arrays.asList(createRotatedVariants(this.createTurtleEggModel(p_377380_, p_377113_))))
                    )
            );
    }

    private void createSnifferEgg() {
        this.registerSimpleFlatItemModel(Items.SNIFFER_EGG);
        Function<Integer, ResourceLocation> function = p_378575_ -> {
            String s = switch (p_378575_) {
                case 1 -> "_slightly_cracked";
                case 2 -> "_very_cracked";
                default -> "_not_cracked";
            };
            TextureMapping texturemapping = TextureMapping.snifferEgg(s);
            return ModelTemplates.SNIFFER_EGG.createWithSuffix(Blocks.SNIFFER_EGG, s, texturemapping, this.modelOutput);
        };
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SNIFFER_EGG)
                    .with(
                        PropertyDispatch.property(SnifferEggBlock.HATCH)
                            .generate(p_378037_ -> Variant.variant().with(VariantProperties.MODEL, function.apply(p_378037_)))
                    )
            );
    }

    private void createMultiface(Block p_377870_) {
        this.registerSimpleFlatItemModel(p_377870_);
        this.createMultifaceBlockStates(p_377870_);
    }

    private void createMultiface(Block p_377112_, Item p_377596_) {
        this.registerSimpleFlatItemModel(p_377596_);
        this.createMultifaceBlockStates(p_377112_);
    }

    private void createMultifaceBlockStates(Block p_375972_) {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_375972_);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(p_375972_);
        Condition.TerminalCondition condition$terminalcondition = Util.make(
            Condition.condition(), p_377701_ -> MULTIFACE_GENERATOR.stream().map(Pair::getFirst).map(MultifaceBlock::getFaceProperty).forEach(p_376031_ -> {
                    if (p_375972_.defaultBlockState().hasProperty(p_376031_)) {
                        p_377701_.term(p_376031_, false);
                    }
                })
        );

        for (Pair<Direction, Function<ResourceLocation, Variant>> pair : MULTIFACE_GENERATOR) {
            BooleanProperty booleanproperty = MultifaceBlock.getFaceProperty(pair.getFirst());
            Function<ResourceLocation, Variant> function = pair.getSecond();
            if (p_375972_.defaultBlockState().hasProperty(booleanproperty)) {
                multipartgenerator.with(Condition.condition().term(booleanproperty, true), function.apply(resourcelocation));
                multipartgenerator.with(condition$terminalcondition, function.apply(resourcelocation));
            }
        }

        this.blockStateOutput.accept(multipartgenerator);
    }

    private void createMossyCarpet(Block p_376848_) {
        ResourceLocation resourcelocation = TexturedModel.CARPET.create(p_376848_, this.modelOutput);
        ResourceLocation resourcelocation1 = TexturedModel.MOSSY_CARPET_SIDE
            .get(p_376848_)
            .updateTextures(p_377296_ -> p_377296_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_376848_, "_side_tall")))
            .createWithSuffix(p_376848_, "_side_tall", this.modelOutput);
        ResourceLocation resourcelocation2 = TexturedModel.MOSSY_CARPET_SIDE
            .get(p_376848_)
            .updateTextures(p_378497_ -> p_378497_.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(p_376848_, "_side_small")))
            .createWithSuffix(p_376848_, "_side_small", this.modelOutput);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(p_376848_);
        Condition.TerminalCondition condition$terminalcondition = Condition.condition().term(MossyCarpetBlock.BASE, false);
        multipartgenerator.with(
            Condition.condition().term(MossyCarpetBlock.BASE, true), Variant.variant().with(VariantProperties.MODEL, resourcelocation)
        );
        multipartgenerator.with(condition$terminalcondition, Variant.variant().with(VariantProperties.MODEL, resourcelocation));
        MULTIFACE_GENERATOR.stream().map(Pair::getFirst).forEach(p_378468_ -> {
            EnumProperty<WallSide> enumproperty1 = MossyCarpetBlock.getPropertyForFace(p_378468_);
            if (enumproperty1 != null && p_376848_.defaultBlockState().hasProperty(enumproperty1)) {
                condition$terminalcondition.term(enumproperty1, WallSide.NONE);
            }
        });

        for (Pair<Direction, Function<ResourceLocation, Variant>> pair : MULTIFACE_GENERATOR) {
            Direction direction = pair.getFirst();
            EnumProperty<WallSide> enumproperty = MossyCarpetBlock.getPropertyForFace(direction);
            if (enumproperty != null) {
                Function<ResourceLocation, Variant> function = pair.getSecond();
                multipartgenerator.with(Condition.condition().term(enumproperty, WallSide.TALL), function.apply(resourcelocation1));
                multipartgenerator.with(Condition.condition().term(enumproperty, WallSide.LOW), function.apply(resourcelocation2));
                multipartgenerator.with(condition$terminalcondition, function.apply(resourcelocation1));
            }
        }

        this.blockStateOutput.accept(multipartgenerator);
    }

    private void createHangingMoss(Block p_378635_) {
        PropertyDispatch propertydispatch = PropertyDispatch.property(HangingMossBlock.TIP).generate(p_378761_ -> {
            String s = p_378761_ ? "_tip" : "";
            TextureMapping texturemapping = TextureMapping.cross(TextureMapping.getBlockTexture(p_378635_, s));
            ResourceLocation resourcelocation = BlockModelGenerators.PlantType.NOT_TINTED.getCross().createWithSuffix(p_378635_, s, texturemapping, this.modelOutput);
            return Variant.variant().with(VariantProperties.MODEL, resourcelocation);
        });
        this.registerSimpleFlatItemModel(p_378635_);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(p_378635_).with(propertydispatch));
    }

    private void createSculkCatalyst() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_bottom");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side"));
        TextureMapping texturemapping1 = new TextureMapping()
            .put(TextureSlot.BOTTOM, resourcelocation)
            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_top_bloom"))
            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.SCULK_CATALYST, "_side_bloom"));
        ResourceLocation resourcelocation1 = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.SCULK_CATALYST, "", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.SCULK_CATALYST, "_bloom", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.SCULK_CATALYST)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.BLOOM)
                            .generate(
                                p_377622_ -> Variant.variant().with(VariantProperties.MODEL, p_377622_ ? resourcelocation2 : resourcelocation1)
                            )
                    )
            );
        this.registerSimpleItemModel(Blocks.SCULK_CATALYST, resourcelocation1);
    }

    private void createChiseledBookshelf() {
        Block block = Blocks.CHISELED_BOOKSHELF;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);
        MultiPartGenerator multipartgenerator = MultiPartGenerator.multiPart(block);
        List.of(
                Pair.of(Direction.NORTH, VariantProperties.Rotation.R0),
                Pair.of(Direction.EAST, VariantProperties.Rotation.R90),
                Pair.of(Direction.SOUTH, VariantProperties.Rotation.R180),
                Pair.of(Direction.WEST, VariantProperties.Rotation.R270)
            )
            .forEach(
                p_377902_ -> {
                    Direction direction = p_377902_.getFirst();
                    VariantProperties.Rotation variantproperties$rotation = p_377902_.getSecond();
                    Condition.TerminalCondition condition$terminalcondition = Condition.condition().term(BlockStateProperties.HORIZONTAL_FACING, direction);
                    multipartgenerator.with(
                        condition$terminalcondition,
                        Variant.variant()
                            .with(VariantProperties.MODEL, resourcelocation)
                            .with(VariantProperties.Y_ROT, variantproperties$rotation)
                            .with(VariantProperties.UV_LOCK, true)
                    );
                    this.addSlotStateAndRotationVariants(multipartgenerator, condition$terminalcondition, variantproperties$rotation);
                }
            );
        this.blockStateOutput.accept(multipartgenerator);
        this.registerSimpleItemModel(block, ModelLocationUtils.getModelLocation(block, "_inventory"));
        CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.clear();
    }

    private void addSlotStateAndRotationVariants(MultiPartGenerator p_375471_, Condition.TerminalCondition p_378599_, VariantProperties.Rotation p_376915_) {
        List.of(
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_LEFT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_MID),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_TOP_RIGHT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_LEFT),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_MID),
                Pair.of(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED, ModelTemplates.CHISELED_BOOKSHELF_SLOT_BOTTOM_RIGHT)
            )
            .forEach(p_377643_ -> {
                BooleanProperty booleanproperty = p_377643_.getFirst();
                ModelTemplate modeltemplate = p_377643_.getSecond();
                this.addBookSlotModel(p_375471_, p_378599_, p_376915_, booleanproperty, modeltemplate, true);
                this.addBookSlotModel(p_375471_, p_378599_, p_376915_, booleanproperty, modeltemplate, false);
            });
    }

    private void addBookSlotModel(
        MultiPartGenerator p_378566_,
        Condition.TerminalCondition p_376967_,
        VariantProperties.Rotation p_378661_,
        BooleanProperty p_376329_,
        ModelTemplate p_376560_,
        boolean p_376040_
    ) {
        String s = p_376040_ ? "_occupied" : "_empty";
        TextureMapping texturemapping = new TextureMapping().put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(Blocks.CHISELED_BOOKSHELF, s));
        BlockModelGenerators.BookSlotModelCacheKey blockmodelgenerators$bookslotmodelcachekey = new BlockModelGenerators.BookSlotModelCacheKey(p_376560_, s);
        ResourceLocation resourcelocation = CHISELED_BOOKSHELF_SLOT_MODEL_CACHE.computeIfAbsent(
            blockmodelgenerators$bookslotmodelcachekey, p_377610_ -> p_376560_.createWithSuffix(Blocks.CHISELED_BOOKSHELF, s, texturemapping, this.modelOutput)
        );
        p_378566_.with(
            Condition.and(p_376967_, Condition.condition().term(p_376329_, p_376040_)),
            Variant.variant().with(VariantProperties.MODEL, resourcelocation).with(VariantProperties.Y_ROT, p_378661_)
        );
    }

    private void createMagmaBlock() {
        this.blockStateOutput
            .accept(
                createSimpleBlock(
                    Blocks.MAGMA_BLOCK,
                    ModelTemplates.CUBE_ALL.create(Blocks.MAGMA_BLOCK, TextureMapping.cube(ModelLocationUtils.decorateBlockModelLocation("magma")), this.modelOutput)
                )
            );
    }

    private void createShulkerBox(Block p_376780_, @Nullable DyeColor p_378224_) {
        this.createParticleOnlyBlock(p_376780_);
        Item item = p_376780_.asItem();
        ResourceLocation resourcelocation = ModelTemplates.SHULKER_BOX_INVENTORY.create(item, TextureMapping.particle(p_376780_), this.modelOutput);
        ItemModel.Unbaked itemmodel$unbaked = p_378224_ != null
            ? ItemModelUtils.specialModel(resourcelocation, new ShulkerBoxSpecialRenderer.Unbaked(p_378224_))
            : ItemModelUtils.specialModel(resourcelocation, new ShulkerBoxSpecialRenderer.Unbaked());
        this.itemModelOutput.accept(item, itemmodel$unbaked);
    }

    private void createGrowingPlant(Block p_376039_, Block p_377260_, BlockModelGenerators.PlantType p_375604_) {
        this.createCrossBlock(p_376039_, p_375604_);
        this.createCrossBlock(p_377260_, p_375604_);
    }

    private void createInfestedStone() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.STONE);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.STONE, "_mirrored");
        this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_STONE, resourcelocation, resourcelocation1));
        this.registerSimpleItemModel(Blocks.INFESTED_STONE, resourcelocation);
    }

    private void createInfestedDeepslate() {
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE);
        ResourceLocation resourcelocation1 = ModelLocationUtils.getModelLocation(Blocks.DEEPSLATE, "_mirrored");
        this.blockStateOutput.accept(createRotatedVariant(Blocks.INFESTED_DEEPSLATE, resourcelocation, resourcelocation1).with(createRotatedPillar()));
        this.registerSimpleItemModel(Blocks.INFESTED_DEEPSLATE, resourcelocation);
    }

    private void createNetherRoots(Block p_378807_, Block p_378001_) {
        this.createCrossBlockWithDefaultItem(p_378807_, BlockModelGenerators.PlantType.NOT_TINTED);
        TextureMapping texturemapping = TextureMapping.plant(TextureMapping.getBlockTexture(p_378807_, "_pot"));
        ResourceLocation resourcelocation = BlockModelGenerators.PlantType.NOT_TINTED.getCrossPot().create(p_378001_, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(p_378001_, resourcelocation));
    }

    private void createRespawnAnchor() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_bottom");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top_off");
        ResourceLocation resourcelocation2 = TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_top");
        ResourceLocation[] aresourcelocation = new ResourceLocation[5];

        for (int i = 0; i < 5; i++) {
            TextureMapping texturemapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, resourcelocation)
                .put(TextureSlot.TOP, i == 0 ? resourcelocation1 : resourcelocation2)
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(Blocks.RESPAWN_ANCHOR, "_side" + i));
            aresourcelocation[i] = ModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(Blocks.RESPAWN_ANCHOR, "_" + i, texturemapping, this.modelOutput);
        }

        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.RESPAWN_ANCHOR)
                    .with(
                        PropertyDispatch.property(BlockStateProperties.RESPAWN_ANCHOR_CHARGES)
                            .generate(p_378752_ -> Variant.variant().with(VariantProperties.MODEL, aresourcelocation[p_378752_]))
                    )
            );
        this.registerSimpleItemModel(Blocks.RESPAWN_ANCHOR, aresourcelocation[0]);
    }

    private Variant applyRotation(FrontAndTop p_377309_, Variant p_376792_) {
        switch (p_377309_) {
            case DOWN_NORTH:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
            case DOWN_SOUTH:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case DOWN_WEST:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case DOWN_EAST:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_NORTH:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case UP_SOUTH:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
            case UP_WEST:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            case UP_EAST:
                return p_376792_.with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                    .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case NORTH_UP:
                return p_376792_;
            case SOUTH_UP:
                return p_376792_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
            case WEST_UP:
                return p_376792_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
            case EAST_UP:
                return p_376792_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
            default:
                throw new UnsupportedOperationException("Rotation " + p_377309_ + " can't be expressed with existing x and y values");
        }
    }

    private void createJigsaw() {
        ResourceLocation resourcelocation = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_top");
        ResourceLocation resourcelocation1 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_bottom");
        ResourceLocation resourcelocation2 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_side");
        ResourceLocation resourcelocation3 = TextureMapping.getBlockTexture(Blocks.JIGSAW, "_lock");
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.DOWN, resourcelocation2)
            .put(TextureSlot.WEST, resourcelocation2)
            .put(TextureSlot.EAST, resourcelocation2)
            .put(TextureSlot.PARTICLE, resourcelocation)
            .put(TextureSlot.NORTH, resourcelocation)
            .put(TextureSlot.SOUTH, resourcelocation1)
            .put(TextureSlot.UP, resourcelocation3);
        ResourceLocation resourcelocation4 = ModelTemplates.CUBE_DIRECTIONAL.create(Blocks.JIGSAW, texturemapping, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(Blocks.JIGSAW, Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                    .with(PropertyDispatch.property(BlockStateProperties.ORIENTATION).generate(p_377218_ -> this.applyRotation(p_377218_, Variant.variant())))
            );
    }

    private void createPetrifiedOakSlab() {
        Block block = Blocks.OAK_PLANKS;
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(block);
        TexturedModel texturedmodel = TexturedModel.CUBE.get(block);
        Block block1 = Blocks.PETRIFIED_OAK_SLAB;
        ResourceLocation resourcelocation1 = ModelTemplates.SLAB_BOTTOM.create(block1, texturedmodel.getMapping(), this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.SLAB_TOP.create(block1, texturedmodel.getMapping(), this.modelOutput);
        this.blockStateOutput.accept(createSlab(block1, resourcelocation1, resourcelocation2, resourcelocation));
    }

    private void createHead(Block p_376902_, Block p_378202_, SkullBlock.Type p_375856_, ResourceLocation p_377201_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("skull");
        this.blockStateOutput.accept(createSimpleBlock(p_376902_, resourcelocation));
        this.blockStateOutput.accept(createSimpleBlock(p_378202_, resourcelocation));
        this.itemModelOutput.accept(p_376902_.asItem(), ItemModelUtils.specialModel(p_377201_, new SkullSpecialRenderer.Unbaked(p_375856_)));
    }

    private void createHeads() {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateItemModelLocation("template_skull");
        this.createHead(Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, SkullBlock.Types.CREEPER, resourcelocation);
        this.createHead(Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, SkullBlock.Types.PLAYER, resourcelocation);
        this.createHead(Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, SkullBlock.Types.ZOMBIE, resourcelocation);
        this.createHead(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, SkullBlock.Types.SKELETON, resourcelocation);
        this.createHead(Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, SkullBlock.Types.WITHER_SKELETON, resourcelocation);
        this.createHead(Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, SkullBlock.Types.PIGLIN, resourcelocation);
        this.createHead(Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, SkullBlock.Types.DRAGON, ModelLocationUtils.getModelLocation(Items.DRAGON_HEAD));
    }

    private void createBanner(Block p_378301_, Block p_378343_, DyeColor p_377534_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("banner");
        ResourceLocation resourcelocation1 = ModelLocationUtils.decorateItemModelLocation("template_banner");
        this.blockStateOutput.accept(createSimpleBlock(p_378301_, resourcelocation));
        this.blockStateOutput.accept(createSimpleBlock(p_378343_, resourcelocation));
        Item item = p_378301_.asItem();
        this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourcelocation1, new BannerSpecialRenderer.Unbaked(p_377534_)));
    }

    private void createBanners() {
        this.createBanner(Blocks.WHITE_BANNER, Blocks.WHITE_WALL_BANNER, DyeColor.WHITE);
        this.createBanner(Blocks.ORANGE_BANNER, Blocks.ORANGE_WALL_BANNER, DyeColor.ORANGE);
        this.createBanner(Blocks.MAGENTA_BANNER, Blocks.MAGENTA_WALL_BANNER, DyeColor.MAGENTA);
        this.createBanner(Blocks.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, DyeColor.LIGHT_BLUE);
        this.createBanner(Blocks.YELLOW_BANNER, Blocks.YELLOW_WALL_BANNER, DyeColor.YELLOW);
        this.createBanner(Blocks.LIME_BANNER, Blocks.LIME_WALL_BANNER, DyeColor.LIME);
        this.createBanner(Blocks.PINK_BANNER, Blocks.PINK_WALL_BANNER, DyeColor.PINK);
        this.createBanner(Blocks.GRAY_BANNER, Blocks.GRAY_WALL_BANNER, DyeColor.GRAY);
        this.createBanner(Blocks.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, DyeColor.LIGHT_GRAY);
        this.createBanner(Blocks.CYAN_BANNER, Blocks.CYAN_WALL_BANNER, DyeColor.CYAN);
        this.createBanner(Blocks.PURPLE_BANNER, Blocks.PURPLE_WALL_BANNER, DyeColor.PURPLE);
        this.createBanner(Blocks.BLUE_BANNER, Blocks.BLUE_WALL_BANNER, DyeColor.BLUE);
        this.createBanner(Blocks.BROWN_BANNER, Blocks.BROWN_WALL_BANNER, DyeColor.BROWN);
        this.createBanner(Blocks.GREEN_BANNER, Blocks.GREEN_WALL_BANNER, DyeColor.GREEN);
        this.createBanner(Blocks.RED_BANNER, Blocks.RED_WALL_BANNER, DyeColor.RED);
        this.createBanner(Blocks.BLACK_BANNER, Blocks.BLACK_WALL_BANNER, DyeColor.BLACK);
    }

    private void createChest(Block p_377801_, Block p_377106_, ResourceLocation p_378349_, boolean p_376992_) {
        this.createParticleOnlyBlock(p_377801_, p_377106_);
        Item item = p_377801_.asItem();
        ResourceLocation resourcelocation = ModelTemplates.CHEST_INVENTORY.create(item, TextureMapping.particle(p_377106_), this.modelOutput);
        ItemModel.Unbaked itemmodel$unbaked = ItemModelUtils.specialModel(resourcelocation, new ChestSpecialRenderer.Unbaked(p_378349_));
        if (p_376992_) {
            ItemModel.Unbaked itemmodel$unbaked1 = ItemModelUtils.specialModel(resourcelocation, new ChestSpecialRenderer.Unbaked(ChestSpecialRenderer.GIFT_CHEST_TEXTURE));
            this.itemModelOutput.accept(item, ItemModelUtils.isXmas(itemmodel$unbaked1, itemmodel$unbaked));
        } else {
            this.itemModelOutput.accept(item, itemmodel$unbaked);
        }
    }

    private void createChests() {
        this.createChest(Blocks.CHEST, Blocks.OAK_PLANKS, ChestSpecialRenderer.NORMAL_CHEST_TEXTURE, true);
        this.createChest(Blocks.TRAPPED_CHEST, Blocks.OAK_PLANKS, ChestSpecialRenderer.TRAPPED_CHEST_TEXTURE, true);
        this.createChest(Blocks.ENDER_CHEST, Blocks.OBSIDIAN, ChestSpecialRenderer.ENDER_CHEST_TEXTURE, false);
    }

    private void createBed(Block p_378031_, Block p_378477_, DyeColor p_376996_) {
        ResourceLocation resourcelocation = ModelLocationUtils.decorateBlockModelLocation("bed");
        this.blockStateOutput.accept(createSimpleBlock(p_378031_, resourcelocation));
        Item item = p_378031_.asItem();
        ResourceLocation resourcelocation1 = ModelTemplates.BED_INVENTORY
            .create(ModelLocationUtils.getModelLocation(item), TextureMapping.particle(p_378477_), this.modelOutput);
        this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourcelocation1, new BedSpecialRenderer.Unbaked(p_376996_)));
    }

    private void createBeds() {
        this.createBed(Blocks.WHITE_BED, Blocks.WHITE_WOOL, DyeColor.WHITE);
        this.createBed(Blocks.ORANGE_BED, Blocks.ORANGE_WOOL, DyeColor.ORANGE);
        this.createBed(Blocks.MAGENTA_BED, Blocks.MAGENTA_WOOL, DyeColor.MAGENTA);
        this.createBed(Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_BLUE_WOOL, DyeColor.LIGHT_BLUE);
        this.createBed(Blocks.YELLOW_BED, Blocks.YELLOW_WOOL, DyeColor.YELLOW);
        this.createBed(Blocks.LIME_BED, Blocks.LIME_WOOL, DyeColor.LIME);
        this.createBed(Blocks.PINK_BED, Blocks.PINK_WOOL, DyeColor.PINK);
        this.createBed(Blocks.GRAY_BED, Blocks.GRAY_WOOL, DyeColor.GRAY);
        this.createBed(Blocks.LIGHT_GRAY_BED, Blocks.LIGHT_GRAY_WOOL, DyeColor.LIGHT_GRAY);
        this.createBed(Blocks.CYAN_BED, Blocks.CYAN_WOOL, DyeColor.CYAN);
        this.createBed(Blocks.PURPLE_BED, Blocks.PURPLE_WOOL, DyeColor.PURPLE);
        this.createBed(Blocks.BLUE_BED, Blocks.BLUE_WOOL, DyeColor.BLUE);
        this.createBed(Blocks.BROWN_BED, Blocks.BROWN_WOOL, DyeColor.BROWN);
        this.createBed(Blocks.GREEN_BED, Blocks.GREEN_WOOL, DyeColor.GREEN);
        this.createBed(Blocks.RED_BED, Blocks.RED_WOOL, DyeColor.RED);
        this.createBed(Blocks.BLACK_BED, Blocks.BLACK_WOOL, DyeColor.BLACK);
    }

    private void generateSimpleSpecialItemModel(Block p_376478_, SpecialModelRenderer.Unbaked p_375868_) {
        Item item = p_376478_.asItem();
        ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(item);
        this.itemModelOutput.accept(item, ItemModelUtils.specialModel(resourcelocation, p_375868_));
    }

    public void run() {
        BlockFamilies.getAllFamilies().filter(BlockFamily::shouldGenerateModel).forEach(p_375984_ -> this.family(p_375984_.getBaseBlock()).generateFor(p_375984_));
        this.family(Blocks.CUT_COPPER)
            .generateFor(BlockFamilies.CUT_COPPER)
            .donateModelTo(Blocks.CUT_COPPER, Blocks.WAXED_CUT_COPPER)
            .donateModelTo(Blocks.CHISELED_COPPER, Blocks.WAXED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_CUT_COPPER);
        this.family(Blocks.EXPOSED_CUT_COPPER)
            .generateFor(BlockFamilies.EXPOSED_CUT_COPPER)
            .donateModelTo(Blocks.EXPOSED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER)
            .donateModelTo(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WAXED_EXPOSED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_EXPOSED_CUT_COPPER);
        this.family(Blocks.WEATHERED_CUT_COPPER)
            .generateFor(BlockFamilies.WEATHERED_CUT_COPPER)
            .donateModelTo(Blocks.WEATHERED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER)
            .donateModelTo(Blocks.WEATHERED_CHISELED_COPPER, Blocks.WAXED_WEATHERED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_WEATHERED_CUT_COPPER);
        this.family(Blocks.OXIDIZED_CUT_COPPER)
            .generateFor(BlockFamilies.OXIDIZED_CUT_COPPER)
            .donateModelTo(Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER)
            .donateModelTo(Blocks.OXIDIZED_CHISELED_COPPER, Blocks.WAXED_OXIDIZED_CHISELED_COPPER)
            .generateFor(BlockFamilies.WAXED_OXIDIZED_CUT_COPPER);
        this.createCopperBulb(Blocks.COPPER_BULB);
        this.createCopperBulb(Blocks.EXPOSED_COPPER_BULB);
        this.createCopperBulb(Blocks.WEATHERED_COPPER_BULB);
        this.createCopperBulb(Blocks.OXIDIZED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.COPPER_BULB, Blocks.WAXED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.WEATHERED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB);
        this.copyCopperBulbModel(Blocks.OXIDIZED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB);
        this.createNonTemplateModelBlock(Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.CAVE_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.VOID_AIR, Blocks.AIR);
        this.createNonTemplateModelBlock(Blocks.BEACON);
        this.createNonTemplateModelBlock(Blocks.CACTUS);
        this.createNonTemplateModelBlock(Blocks.BUBBLE_COLUMN, Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.DRAGON_EGG);
        this.createNonTemplateModelBlock(Blocks.DRIED_KELP_BLOCK);
        this.createNonTemplateModelBlock(Blocks.ENCHANTING_TABLE);
        this.createNonTemplateModelBlock(Blocks.FLOWER_POT);
        this.registerSimpleFlatItemModel(Items.FLOWER_POT);
        this.createNonTemplateModelBlock(Blocks.HONEY_BLOCK);
        this.createNonTemplateModelBlock(Blocks.WATER);
        this.createNonTemplateModelBlock(Blocks.LAVA);
        this.createNonTemplateModelBlock(Blocks.SLIME_BLOCK);
        this.registerSimpleFlatItemModel(Items.CHAIN);
        this.createCandleAndCandleCake(Blocks.WHITE_CANDLE, Blocks.WHITE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.ORANGE_CANDLE, Blocks.ORANGE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.MAGENTA_CANDLE, Blocks.MAGENTA_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIGHT_BLUE_CANDLE, Blocks.LIGHT_BLUE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.YELLOW_CANDLE, Blocks.YELLOW_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIME_CANDLE, Blocks.LIME_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.PINK_CANDLE, Blocks.PINK_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.GRAY_CANDLE, Blocks.GRAY_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.LIGHT_GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.CYAN_CANDLE, Blocks.CYAN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.PURPLE_CANDLE, Blocks.PURPLE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BLUE_CANDLE, Blocks.BLUE_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BROWN_CANDLE, Blocks.BROWN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.GREEN_CANDLE, Blocks.GREEN_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.RED_CANDLE, Blocks.RED_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.BLACK_CANDLE, Blocks.BLACK_CANDLE_CAKE);
        this.createCandleAndCandleCake(Blocks.CANDLE, Blocks.CANDLE_CAKE);
        this.createNonTemplateModelBlock(Blocks.POTTED_BAMBOO);
        this.createNonTemplateModelBlock(Blocks.POTTED_CACTUS);
        this.createNonTemplateModelBlock(Blocks.POWDER_SNOW);
        this.createNonTemplateModelBlock(Blocks.SPORE_BLOSSOM);
        this.createAzalea(Blocks.AZALEA);
        this.createAzalea(Blocks.FLOWERING_AZALEA);
        this.createPottedAzalea(Blocks.POTTED_AZALEA);
        this.createPottedAzalea(Blocks.POTTED_FLOWERING_AZALEA);
        this.createCaveVines();
        this.createFullAndCarpetBlocks(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET);
        this.createMossyCarpet(Blocks.PALE_MOSS_CARPET);
        this.createHangingMoss(Blocks.PALE_HANGING_MOSS);
        this.createTrivialCube(Blocks.PALE_MOSS_BLOCK);
        this.createFlowerBed(Blocks.PINK_PETALS);
        this.createAirLikeBlock(Blocks.BARRIER, Items.BARRIER);
        this.registerSimpleFlatItemModel(Items.BARRIER);
        this.createLightBlock();
        this.createAirLikeBlock(Blocks.STRUCTURE_VOID, Items.STRUCTURE_VOID);
        this.registerSimpleFlatItemModel(Items.STRUCTURE_VOID);
        this.createAirLikeBlock(Blocks.MOVING_PISTON, TextureMapping.getBlockTexture(Blocks.PISTON, "_side"));
        this.createTrivialCube(Blocks.COAL_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_COAL_ORE);
        this.createTrivialCube(Blocks.COAL_BLOCK);
        this.createTrivialCube(Blocks.DIAMOND_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_DIAMOND_ORE);
        this.createTrivialCube(Blocks.DIAMOND_BLOCK);
        this.createTrivialCube(Blocks.EMERALD_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_EMERALD_ORE);
        this.createTrivialCube(Blocks.EMERALD_BLOCK);
        this.createTrivialCube(Blocks.GOLD_ORE);
        this.createTrivialCube(Blocks.NETHER_GOLD_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_GOLD_ORE);
        this.createTrivialCube(Blocks.GOLD_BLOCK);
        this.createTrivialCube(Blocks.IRON_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_IRON_ORE);
        this.createTrivialCube(Blocks.IRON_BLOCK);
        this.createTrivialBlock(Blocks.ANCIENT_DEBRIS, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.NETHERITE_BLOCK);
        this.createTrivialCube(Blocks.LAPIS_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_LAPIS_ORE);
        this.createTrivialCube(Blocks.LAPIS_BLOCK);
        this.createTrivialCube(Blocks.RESIN_BLOCK);
        this.createTrivialCube(Blocks.NETHER_QUARTZ_ORE);
        this.createTrivialCube(Blocks.REDSTONE_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_REDSTONE_ORE);
        this.createTrivialCube(Blocks.REDSTONE_BLOCK);
        this.createTrivialCube(Blocks.GILDED_BLACKSTONE);
        this.createTrivialCube(Blocks.BLUE_ICE);
        this.createTrivialCube(Blocks.CLAY);
        this.createTrivialCube(Blocks.COARSE_DIRT);
        this.createTrivialCube(Blocks.CRYING_OBSIDIAN);
        this.createTrivialCube(Blocks.END_STONE);
        this.createTrivialCube(Blocks.GLOWSTONE);
        this.createTrivialCube(Blocks.GRAVEL);
        this.createTrivialCube(Blocks.HONEYCOMB_BLOCK);
        this.createTrivialCube(Blocks.ICE);
        this.createTrivialBlock(Blocks.JUKEBOX, TexturedModel.CUBE_TOP);
        this.createTrivialBlock(Blocks.LODESTONE, TexturedModel.COLUMN);
        this.createTrivialBlock(Blocks.MELON, TexturedModel.COLUMN);
        this.createNonTemplateModelBlock(Blocks.MANGROVE_ROOTS);
        this.createNonTemplateModelBlock(Blocks.POTTED_MANGROVE_PROPAGULE);
        this.createTrivialCube(Blocks.NETHER_WART_BLOCK);
        this.createTrivialCube(Blocks.NOTE_BLOCK);
        this.createTrivialCube(Blocks.PACKED_ICE);
        this.createTrivialCube(Blocks.OBSIDIAN);
        this.createTrivialCube(Blocks.QUARTZ_BRICKS);
        this.createTrivialCube(Blocks.SEA_LANTERN);
        this.createTrivialCube(Blocks.SHROOMLIGHT);
        this.createTrivialCube(Blocks.SOUL_SAND);
        this.createTrivialCube(Blocks.SOUL_SOIL);
        this.createTrivialBlock(Blocks.SPAWNER, TexturedModel.CUBE_INNER_FACES);
        this.createCreakingHeart(Blocks.CREAKING_HEART);
        this.createTrivialCube(Blocks.SPONGE);
        this.createTrivialBlock(Blocks.SEAGRASS, TexturedModel.SEAGRASS);
        this.registerSimpleFlatItemModel(Items.SEAGRASS);
        this.createTrivialBlock(Blocks.TNT, TexturedModel.CUBE_TOP_BOTTOM);
        this.createTrivialBlock(Blocks.TARGET, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.WARPED_WART_BLOCK);
        this.createTrivialCube(Blocks.WET_SPONGE);
        this.createTrivialCube(Blocks.AMETHYST_BLOCK);
        this.createTrivialCube(Blocks.BUDDING_AMETHYST);
        this.createTrivialCube(Blocks.CALCITE);
        this.createTrivialCube(Blocks.DRIPSTONE_BLOCK);
        this.createTrivialCube(Blocks.RAW_IRON_BLOCK);
        this.createTrivialCube(Blocks.RAW_COPPER_BLOCK);
        this.createTrivialCube(Blocks.RAW_GOLD_BLOCK);
        this.createRotatedMirroredVariantBlock(Blocks.SCULK);
        this.createNonTemplateModelBlock(Blocks.HEAVY_CORE);
        this.createPetrifiedOakSlab();
        this.createTrivialCube(Blocks.COPPER_ORE);
        this.createTrivialCube(Blocks.DEEPSLATE_COPPER_ORE);
        this.createTrivialCube(Blocks.COPPER_BLOCK);
        this.createTrivialCube(Blocks.EXPOSED_COPPER);
        this.createTrivialCube(Blocks.WEATHERED_COPPER);
        this.createTrivialCube(Blocks.OXIDIZED_COPPER);
        this.copyModel(Blocks.COPPER_BLOCK, Blocks.WAXED_COPPER_BLOCK);
        this.copyModel(Blocks.EXPOSED_COPPER, Blocks.WAXED_EXPOSED_COPPER);
        this.copyModel(Blocks.WEATHERED_COPPER, Blocks.WAXED_WEATHERED_COPPER);
        this.copyModel(Blocks.OXIDIZED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
        this.createDoor(Blocks.COPPER_DOOR);
        this.createDoor(Blocks.EXPOSED_COPPER_DOOR);
        this.createDoor(Blocks.WEATHERED_COPPER_DOOR);
        this.createDoor(Blocks.OXIDIZED_COPPER_DOOR);
        this.copyDoorModel(Blocks.COPPER_DOOR, Blocks.WAXED_COPPER_DOOR);
        this.copyDoorModel(Blocks.EXPOSED_COPPER_DOOR, Blocks.WAXED_EXPOSED_COPPER_DOOR);
        this.copyDoorModel(Blocks.WEATHERED_COPPER_DOOR, Blocks.WAXED_WEATHERED_COPPER_DOOR);
        this.copyDoorModel(Blocks.OXIDIZED_COPPER_DOOR, Blocks.WAXED_OXIDIZED_COPPER_DOOR);
        this.createTrapdoor(Blocks.COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.EXPOSED_COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.WEATHERED_COPPER_TRAPDOOR);
        this.createTrapdoor(Blocks.OXIDIZED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.COPPER_TRAPDOOR, Blocks.WAXED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR);
        this.copyTrapdoorModel(Blocks.OXIDIZED_COPPER_TRAPDOOR, Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR);
        this.createTrivialCube(Blocks.COPPER_GRATE);
        this.createTrivialCube(Blocks.EXPOSED_COPPER_GRATE);
        this.createTrivialCube(Blocks.WEATHERED_COPPER_GRATE);
        this.createTrivialCube(Blocks.OXIDIZED_COPPER_GRATE);
        this.copyModel(Blocks.COPPER_GRATE, Blocks.WAXED_COPPER_GRATE);
        this.copyModel(Blocks.EXPOSED_COPPER_GRATE, Blocks.WAXED_EXPOSED_COPPER_GRATE);
        this.copyModel(Blocks.WEATHERED_COPPER_GRATE, Blocks.WAXED_WEATHERED_COPPER_GRATE);
        this.copyModel(Blocks.OXIDIZED_COPPER_GRATE, Blocks.WAXED_OXIDIZED_COPPER_GRATE);
        this.createWeightedPressurePlate(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, Blocks.GOLD_BLOCK);
        this.createWeightedPressurePlate(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE, Blocks.IRON_BLOCK);
        this.createAmethystClusters();
        this.createBookshelf();
        this.createChiseledBookshelf();
        this.createBrewingStand();
        this.createCakeBlock();
        this.createCampfires(Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
        this.createCartographyTable();
        this.createCauldrons();
        this.createChorusFlower();
        this.createChorusPlant();
        this.createComposter();
        this.createDaylightDetector();
        this.createEndPortalFrame();
        this.createRotatableColumn(Blocks.END_ROD);
        this.createLightningRod();
        this.createFarmland();
        this.createFire();
        this.createSoulFire();
        this.createFrostedIce();
        this.createGrassBlocks();
        this.createCocoa();
        this.createDirtPath();
        this.createGrindstone();
        this.createHopper();
        this.createIronBars();
        this.createLever();
        this.createLilyPad();
        this.createNetherPortalBlock();
        this.createNetherrack();
        this.createObserver();
        this.createPistons();
        this.createPistonHeads();
        this.createScaffolding();
        this.createRedstoneTorch();
        this.createRedstoneLamp();
        this.createRepeater();
        this.createSeaPickle();
        this.createSmithingTable();
        this.createSnowBlocks();
        this.createStonecutter();
        this.createStructureBlock();
        this.createSweetBerryBush();
        this.createTripwire();
        this.createTripwireHook();
        this.createTurtleEgg();
        this.createSnifferEgg();
        this.createVine();
        this.createMultiface(Blocks.GLOW_LICHEN);
        this.createMultiface(Blocks.SCULK_VEIN);
        this.createMultiface(Blocks.RESIN_CLUMP, Items.RESIN_CLUMP);
        this.createMagmaBlock();
        this.createJigsaw();
        this.createSculkSensor();
        this.createCalibratedSculkSensor();
        this.createSculkShrieker();
        this.createFrogspawnBlock();
        this.createMangrovePropagule();
        this.createMuddyMangroveRoots();
        this.createTrialSpawner();
        this.createVault();
        this.createNonTemplateHorizontalBlock(Blocks.LADDER);
        this.registerSimpleFlatItemModel(Blocks.LADDER);
        this.createNonTemplateHorizontalBlock(Blocks.LECTERN);
        this.createBigDripLeafBlock();
        this.createNonTemplateHorizontalBlock(Blocks.BIG_DRIPLEAF_STEM);
        this.createNormalTorch(Blocks.TORCH, Blocks.WALL_TORCH);
        this.createNormalTorch(Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH);
        this.createCraftingTableLike(Blocks.CRAFTING_TABLE, Blocks.OAK_PLANKS, TextureMapping::craftingTable);
        this.createCraftingTableLike(Blocks.FLETCHING_TABLE, Blocks.BIRCH_PLANKS, TextureMapping::fletchingTable);
        this.createNyliumBlock(Blocks.CRIMSON_NYLIUM);
        this.createNyliumBlock(Blocks.WARPED_NYLIUM);
        this.createDispenserBlock(Blocks.DISPENSER);
        this.createDispenserBlock(Blocks.DROPPER);
        this.createCrafterBlock();
        this.createLantern(Blocks.LANTERN);
        this.createLantern(Blocks.SOUL_LANTERN);
        this.createAxisAlignedPillarBlockCustomModel(Blocks.CHAIN, ModelLocationUtils.getModelLocation(Blocks.CHAIN));
        this.createAxisAlignedPillarBlock(Blocks.BASALT, TexturedModel.COLUMN);
        this.createAxisAlignedPillarBlock(Blocks.POLISHED_BASALT, TexturedModel.COLUMN);
        this.createTrivialCube(Blocks.SMOOTH_BASALT);
        this.createAxisAlignedPillarBlock(Blocks.BONE_BLOCK, TexturedModel.COLUMN);
        this.createRotatedVariantBlock(Blocks.DIRT);
        this.createRotatedVariantBlock(Blocks.ROOTED_DIRT);
        this.createRotatedVariantBlock(Blocks.SAND);
        this.createBrushableBlock(Blocks.SUSPICIOUS_SAND);
        this.createBrushableBlock(Blocks.SUSPICIOUS_GRAVEL);
        this.createRotatedVariantBlock(Blocks.RED_SAND);
        this.createRotatedMirroredVariantBlock(Blocks.BEDROCK);
        this.createTrivialBlock(Blocks.REINFORCED_DEEPSLATE, TexturedModel.CUBE_TOP_BOTTOM);
        this.createRotatedPillarWithHorizontalVariant(Blocks.HAY_BLOCK, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.PURPUR_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createRotatedPillarWithHorizontalVariant(Blocks.QUARTZ_PILLAR, TexturedModel.COLUMN_ALT, TexturedModel.COLUMN_HORIZONTAL_ALT);
        this.createRotatedPillarWithHorizontalVariant(Blocks.OCHRE_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.VERDANT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createRotatedPillarWithHorizontalVariant(Blocks.PEARLESCENT_FROGLIGHT, TexturedModel.COLUMN, TexturedModel.COLUMN_HORIZONTAL);
        this.createHorizontallyRotatedBlock(Blocks.LOOM, TexturedModel.ORIENTABLE);
        this.createPumpkins();
        this.createBeeNest(Blocks.BEE_NEST, TextureMapping::orientableCube);
        this.createBeeNest(Blocks.BEEHIVE, TextureMapping::orientableCubeSameEnds);
        this.createCropBlock(Blocks.BEETROOTS, BlockStateProperties.AGE_3, 0, 1, 2, 3);
        this.createCropBlock(Blocks.CARROTS, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.NETHER_WART, BlockStateProperties.AGE_3, 0, 1, 1, 2);
        this.createCropBlock(Blocks.POTATOES, BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        this.createCropBlock(Blocks.WHEAT, BlockStateProperties.AGE_7, 0, 1, 2, 3, 4, 5, 6, 7);
        this.createCrossBlock(Blocks.TORCHFLOWER_CROP, BlockModelGenerators.PlantType.NOT_TINTED, BlockStateProperties.AGE_1, 0, 1);
        this.createPitcherCrop();
        this.createPitcherPlant();
        this.createBanners();
        this.createBeds();
        this.createHeads();
        this.createChests();
        this.createShulkerBox(Blocks.SHULKER_BOX, null);
        this.createShulkerBox(Blocks.WHITE_SHULKER_BOX, DyeColor.WHITE);
        this.createShulkerBox(Blocks.ORANGE_SHULKER_BOX, DyeColor.ORANGE);
        this.createShulkerBox(Blocks.MAGENTA_SHULKER_BOX, DyeColor.MAGENTA);
        this.createShulkerBox(Blocks.LIGHT_BLUE_SHULKER_BOX, DyeColor.LIGHT_BLUE);
        this.createShulkerBox(Blocks.YELLOW_SHULKER_BOX, DyeColor.YELLOW);
        this.createShulkerBox(Blocks.LIME_SHULKER_BOX, DyeColor.LIME);
        this.createShulkerBox(Blocks.PINK_SHULKER_BOX, DyeColor.PINK);
        this.createShulkerBox(Blocks.GRAY_SHULKER_BOX, DyeColor.GRAY);
        this.createShulkerBox(Blocks.LIGHT_GRAY_SHULKER_BOX, DyeColor.LIGHT_GRAY);
        this.createShulkerBox(Blocks.CYAN_SHULKER_BOX, DyeColor.CYAN);
        this.createShulkerBox(Blocks.PURPLE_SHULKER_BOX, DyeColor.PURPLE);
        this.createShulkerBox(Blocks.BLUE_SHULKER_BOX, DyeColor.BLUE);
        this.createShulkerBox(Blocks.BROWN_SHULKER_BOX, DyeColor.BROWN);
        this.createShulkerBox(Blocks.GREEN_SHULKER_BOX, DyeColor.GREEN);
        this.createShulkerBox(Blocks.RED_SHULKER_BOX, DyeColor.RED);
        this.createShulkerBox(Blocks.BLACK_SHULKER_BOX, DyeColor.BLACK);
        this.createParticleOnlyBlock(Blocks.CONDUIT);
        this.generateSimpleSpecialItemModel(Blocks.CONDUIT, new ConduitSpecialRenderer.Unbaked());
        this.createParticleOnlyBlock(Blocks.DECORATED_POT, Blocks.TERRACOTTA);
        this.generateSimpleSpecialItemModel(Blocks.DECORATED_POT, new DecoratedPotSpecialRenderer.Unbaked());
        this.createParticleOnlyBlock(Blocks.END_PORTAL, Blocks.OBSIDIAN);
        this.createParticleOnlyBlock(Blocks.END_GATEWAY, Blocks.OBSIDIAN);
        this.createTrivialCube(Blocks.AZALEA_LEAVES);
        this.createTrivialCube(Blocks.FLOWERING_AZALEA_LEAVES);
        this.createTrivialCube(Blocks.WHITE_CONCRETE);
        this.createTrivialCube(Blocks.ORANGE_CONCRETE);
        this.createTrivialCube(Blocks.MAGENTA_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_BLUE_CONCRETE);
        this.createTrivialCube(Blocks.YELLOW_CONCRETE);
        this.createTrivialCube(Blocks.LIME_CONCRETE);
        this.createTrivialCube(Blocks.PINK_CONCRETE);
        this.createTrivialCube(Blocks.GRAY_CONCRETE);
        this.createTrivialCube(Blocks.LIGHT_GRAY_CONCRETE);
        this.createTrivialCube(Blocks.CYAN_CONCRETE);
        this.createTrivialCube(Blocks.PURPLE_CONCRETE);
        this.createTrivialCube(Blocks.BLUE_CONCRETE);
        this.createTrivialCube(Blocks.BROWN_CONCRETE);
        this.createTrivialCube(Blocks.GREEN_CONCRETE);
        this.createTrivialCube(Blocks.RED_CONCRETE);
        this.createTrivialCube(Blocks.BLACK_CONCRETE);
        this.createColoredBlockWithRandomRotations(
            TexturedModel.CUBE,
            Blocks.WHITE_CONCRETE_POWDER,
            Blocks.ORANGE_CONCRETE_POWDER,
            Blocks.MAGENTA_CONCRETE_POWDER,
            Blocks.LIGHT_BLUE_CONCRETE_POWDER,
            Blocks.YELLOW_CONCRETE_POWDER,
            Blocks.LIME_CONCRETE_POWDER,
            Blocks.PINK_CONCRETE_POWDER,
            Blocks.GRAY_CONCRETE_POWDER,
            Blocks.LIGHT_GRAY_CONCRETE_POWDER,
            Blocks.CYAN_CONCRETE_POWDER,
            Blocks.PURPLE_CONCRETE_POWDER,
            Blocks.BLUE_CONCRETE_POWDER,
            Blocks.BROWN_CONCRETE_POWDER,
            Blocks.GREEN_CONCRETE_POWDER,
            Blocks.RED_CONCRETE_POWDER,
            Blocks.BLACK_CONCRETE_POWDER
        );
        this.createTrivialCube(Blocks.TERRACOTTA);
        this.createTrivialCube(Blocks.WHITE_TERRACOTTA);
        this.createTrivialCube(Blocks.ORANGE_TERRACOTTA);
        this.createTrivialCube(Blocks.MAGENTA_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.YELLOW_TERRACOTTA);
        this.createTrivialCube(Blocks.LIME_TERRACOTTA);
        this.createTrivialCube(Blocks.PINK_TERRACOTTA);
        this.createTrivialCube(Blocks.GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.LIGHT_GRAY_TERRACOTTA);
        this.createTrivialCube(Blocks.CYAN_TERRACOTTA);
        this.createTrivialCube(Blocks.PURPLE_TERRACOTTA);
        this.createTrivialCube(Blocks.BLUE_TERRACOTTA);
        this.createTrivialCube(Blocks.BROWN_TERRACOTTA);
        this.createTrivialCube(Blocks.GREEN_TERRACOTTA);
        this.createTrivialCube(Blocks.RED_TERRACOTTA);
        this.createTrivialCube(Blocks.BLACK_TERRACOTTA);
        this.createTrivialCube(Blocks.TINTED_GLASS);
        this.createGlassBlocks(Blocks.GLASS, Blocks.GLASS_PANE);
        this.createGlassBlocks(Blocks.WHITE_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.ORANGE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.MAGENTA_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.YELLOW_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIME_STAINED_GLASS, Blocks.LIME_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PINK_STAINED_GLASS, Blocks.PINK_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GRAY_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.CYAN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.PURPLE_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLUE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BROWN_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.GREEN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.RED_STAINED_GLASS, Blocks.RED_STAINED_GLASS_PANE);
        this.createGlassBlocks(Blocks.BLACK_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS_PANE);
        this.createColoredBlockWithStateRotations(
            TexturedModel.GLAZED_TERRACOTTA,
            Blocks.WHITE_GLAZED_TERRACOTTA,
            Blocks.ORANGE_GLAZED_TERRACOTTA,
            Blocks.MAGENTA_GLAZED_TERRACOTTA,
            Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
            Blocks.YELLOW_GLAZED_TERRACOTTA,
            Blocks.LIME_GLAZED_TERRACOTTA,
            Blocks.PINK_GLAZED_TERRACOTTA,
            Blocks.GRAY_GLAZED_TERRACOTTA,
            Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA,
            Blocks.CYAN_GLAZED_TERRACOTTA,
            Blocks.PURPLE_GLAZED_TERRACOTTA,
            Blocks.BLUE_GLAZED_TERRACOTTA,
            Blocks.BROWN_GLAZED_TERRACOTTA,
            Blocks.GREEN_GLAZED_TERRACOTTA,
            Blocks.RED_GLAZED_TERRACOTTA,
            Blocks.BLACK_GLAZED_TERRACOTTA
        );
        this.createFullAndCarpetBlocks(Blocks.WHITE_WOOL, Blocks.WHITE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.ORANGE_WOOL, Blocks.ORANGE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.MAGENTA_WOOL, Blocks.MAGENTA_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIGHT_BLUE_WOOL, Blocks.LIGHT_BLUE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.YELLOW_WOOL, Blocks.YELLOW_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIME_WOOL, Blocks.LIME_CARPET);
        this.createFullAndCarpetBlocks(Blocks.PINK_WOOL, Blocks.PINK_CARPET);
        this.createFullAndCarpetBlocks(Blocks.GRAY_WOOL, Blocks.GRAY_CARPET);
        this.createFullAndCarpetBlocks(Blocks.LIGHT_GRAY_WOOL, Blocks.LIGHT_GRAY_CARPET);
        this.createFullAndCarpetBlocks(Blocks.CYAN_WOOL, Blocks.CYAN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.PURPLE_WOOL, Blocks.PURPLE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BLUE_WOOL, Blocks.BLUE_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BROWN_WOOL, Blocks.BROWN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.GREEN_WOOL, Blocks.GREEN_CARPET);
        this.createFullAndCarpetBlocks(Blocks.RED_WOOL, Blocks.RED_CARPET);
        this.createFullAndCarpetBlocks(Blocks.BLACK_WOOL, Blocks.BLACK_CARPET);
        this.createTrivialCube(Blocks.MUD);
        this.createTrivialCube(Blocks.PACKED_MUD);
        this.createPlant(Blocks.FERN, Blocks.POTTED_FERN, BlockModelGenerators.PlantType.TINTED);
        this.createItemWithGrassTint(Blocks.FERN);
        this.createPlantWithDefaultItem(Blocks.DANDELION, Blocks.POTTED_DANDELION, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.POPPY, Blocks.POTTED_POPPY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.OPEN_EYEBLOSSOM, Blocks.POTTED_OPEN_EYEBLOSSOM, BlockModelGenerators.PlantType.EMISSIVE_NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.CLOSED_EYEBLOSSOM, Blocks.POTTED_CLOSED_EYEBLOSSOM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.BLUE_ORCHID, Blocks.POTTED_BLUE_ORCHID, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.ALLIUM, Blocks.POTTED_ALLIUM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.AZURE_BLUET, Blocks.POTTED_AZURE_BLUET, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.RED_TULIP, Blocks.POTTED_RED_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.ORANGE_TULIP, Blocks.POTTED_ORANGE_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.WHITE_TULIP, Blocks.POTTED_WHITE_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.PINK_TULIP, Blocks.POTTED_PINK_TULIP, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.OXEYE_DAISY, Blocks.POTTED_OXEYE_DAISY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.CORNFLOWER, Blocks.POTTED_CORNFLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.LILY_OF_THE_VALLEY, Blocks.POTTED_LILY_OF_THE_VALLEY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.WITHER_ROSE, Blocks.POTTED_WITHER_ROSE, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.RED_MUSHROOM, Blocks.POTTED_RED_MUSHROOM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.BROWN_MUSHROOM, Blocks.POTTED_BROWN_MUSHROOM, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.DEAD_BUSH, Blocks.POTTED_DEAD_BUSH, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPlantWithDefaultItem(Blocks.TORCHFLOWER, Blocks.POTTED_TORCHFLOWER, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createPointedDripstone();
        this.createMushroomBlock(Blocks.BROWN_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.RED_MUSHROOM_BLOCK);
        this.createMushroomBlock(Blocks.MUSHROOM_STEM);
        this.createCrossBlock(Blocks.SHORT_GRASS, BlockModelGenerators.PlantType.TINTED);
        this.createItemWithGrassTint(Blocks.SHORT_GRASS);
        this.createCrossBlock(Blocks.SUGAR_CANE, BlockModelGenerators.PlantType.TINTED);
        this.registerSimpleFlatItemModel(Items.SUGAR_CANE);
        this.createGrowingPlant(Blocks.KELP, Blocks.KELP_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerSimpleFlatItemModel(Items.KELP);
        this.createCrossBlock(Blocks.HANGING_ROOTS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createGrowingPlant(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createGrowingPlant(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT, BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerSimpleFlatItemModel(Blocks.WEEPING_VINES, "_plant");
        this.registerSimpleFlatItemModel(Blocks.TWISTING_VINES, "_plant");
        this.createCrossBlockWithDefaultItem(Blocks.BAMBOO_SAPLING, BlockModelGenerators.PlantType.TINTED, TextureMapping.cross(TextureMapping.getBlockTexture(Blocks.BAMBOO, "_stage0")));
        this.createBamboo();
        this.createCrossBlockWithDefaultItem(Blocks.COBWEB, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createDoublePlantWithDefaultItem(Blocks.LILAC, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createDoublePlantWithDefaultItem(Blocks.ROSE_BUSH, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createDoublePlantWithDefaultItem(Blocks.PEONY, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedDoublePlant(Blocks.TALL_GRASS);
        this.createTintedDoublePlant(Blocks.LARGE_FERN);
        this.createSunflower();
        this.createTallSeagrass();
        this.createSmallDripleaf();
        this.createCoral(Blocks.TUBE_CORAL, Blocks.DEAD_TUBE_CORAL, Blocks.TUBE_CORAL_BLOCK, Blocks.DEAD_TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL_FAN, Blocks.DEAD_TUBE_CORAL_FAN, Blocks.TUBE_CORAL_WALL_FAN, Blocks.DEAD_TUBE_CORAL_WALL_FAN);
        this.createCoral(Blocks.BRAIN_CORAL, Blocks.DEAD_BRAIN_CORAL, Blocks.BRAIN_CORAL_BLOCK, Blocks.DEAD_BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL_FAN, Blocks.DEAD_BRAIN_CORAL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.DEAD_BRAIN_CORAL_WALL_FAN);
        this.createCoral(Blocks.BUBBLE_CORAL, Blocks.DEAD_BUBBLE_CORAL, Blocks.BUBBLE_CORAL_BLOCK, Blocks.DEAD_BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL_FAN, Blocks.DEAD_BUBBLE_CORAL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.DEAD_BUBBLE_CORAL_WALL_FAN);
        this.createCoral(Blocks.FIRE_CORAL, Blocks.DEAD_FIRE_CORAL, Blocks.FIRE_CORAL_BLOCK, Blocks.DEAD_FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL_FAN, Blocks.DEAD_FIRE_CORAL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.DEAD_FIRE_CORAL_WALL_FAN);
        this.createCoral(Blocks.HORN_CORAL, Blocks.DEAD_HORN_CORAL, Blocks.HORN_CORAL_BLOCK, Blocks.DEAD_HORN_CORAL_BLOCK, Blocks.HORN_CORAL_FAN, Blocks.DEAD_HORN_CORAL_FAN, Blocks.HORN_CORAL_WALL_FAN, Blocks.DEAD_HORN_CORAL_WALL_FAN);
        this.createStems(Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
        this.createStems(Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        this.woodProvider(Blocks.MANGROVE_LOG).logWithHorizontal(Blocks.MANGROVE_LOG).wood(Blocks.MANGROVE_WOOD);
        this.woodProvider(Blocks.STRIPPED_MANGROVE_LOG).logWithHorizontal(Blocks.STRIPPED_MANGROVE_LOG).wood(Blocks.STRIPPED_MANGROVE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_MANGROVE_LOG, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN);
        this.createTintedLeaves(Blocks.MANGROVE_LEAVES, TexturedModel.LEAVES, -7158200);
        this.woodProvider(Blocks.ACACIA_LOG).logWithHorizontal(Blocks.ACACIA_LOG).wood(Blocks.ACACIA_WOOD);
        this.woodProvider(Blocks.STRIPPED_ACACIA_LOG).logWithHorizontal(Blocks.STRIPPED_ACACIA_LOG).wood(Blocks.STRIPPED_ACACIA_WOOD);
        this.createHangingSign(Blocks.STRIPPED_ACACIA_LOG, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.ACACIA_SAPLING, Blocks.POTTED_ACACIA_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.ACACIA_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.CHERRY_LOG).logUVLocked(Blocks.CHERRY_LOG).wood(Blocks.CHERRY_WOOD);
        this.woodProvider(Blocks.STRIPPED_CHERRY_LOG).logUVLocked(Blocks.STRIPPED_CHERRY_LOG).wood(Blocks.STRIPPED_CHERRY_WOOD);
        this.createHangingSign(Blocks.STRIPPED_CHERRY_LOG, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.CHERRY_SAPLING, Blocks.POTTED_CHERRY_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTrivialBlock(Blocks.CHERRY_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.BIRCH_LOG).logWithHorizontal(Blocks.BIRCH_LOG).wood(Blocks.BIRCH_WOOD);
        this.woodProvider(Blocks.STRIPPED_BIRCH_LOG).logWithHorizontal(Blocks.STRIPPED_BIRCH_LOG).wood(Blocks.STRIPPED_BIRCH_WOOD);
        this.createHangingSign(Blocks.STRIPPED_BIRCH_LOG, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.BIRCH_SAPLING, Blocks.POTTED_BIRCH_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.BIRCH_LEAVES, TexturedModel.LEAVES, -8345771);
        this.woodProvider(Blocks.OAK_LOG).logWithHorizontal(Blocks.OAK_LOG).wood(Blocks.OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_OAK_LOG).wood(Blocks.STRIPPED_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_OAK_LOG, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.OAK_SAPLING, Blocks.POTTED_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.OAK_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.SPRUCE_LOG).logWithHorizontal(Blocks.SPRUCE_LOG).wood(Blocks.SPRUCE_WOOD);
        this.woodProvider(Blocks.STRIPPED_SPRUCE_LOG).logWithHorizontal(Blocks.STRIPPED_SPRUCE_LOG).wood(Blocks.STRIPPED_SPRUCE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_SPRUCE_LOG, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.SPRUCE_SAPLING, Blocks.POTTED_SPRUCE_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.SPRUCE_LEAVES, TexturedModel.LEAVES, -10380959);
        this.woodProvider(Blocks.DARK_OAK_LOG).logWithHorizontal(Blocks.DARK_OAK_LOG).wood(Blocks.DARK_OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_DARK_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_DARK_OAK_LOG).wood(Blocks.STRIPPED_DARK_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_DARK_OAK_LOG, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.DARK_OAK_SAPLING, Blocks.POTTED_DARK_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.DARK_OAK_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.PALE_OAK_LOG).logWithHorizontal(Blocks.PALE_OAK_LOG).wood(Blocks.PALE_OAK_WOOD);
        this.woodProvider(Blocks.STRIPPED_PALE_OAK_LOG).logWithHorizontal(Blocks.STRIPPED_PALE_OAK_LOG).wood(Blocks.STRIPPED_PALE_OAK_WOOD);
        this.createHangingSign(Blocks.STRIPPED_PALE_OAK_LOG, Blocks.PALE_OAK_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.PALE_OAK_SAPLING, Blocks.POTTED_PALE_OAK_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTrivialBlock(Blocks.PALE_OAK_LEAVES, TexturedModel.LEAVES);
        this.woodProvider(Blocks.JUNGLE_LOG).logWithHorizontal(Blocks.JUNGLE_LOG).wood(Blocks.JUNGLE_WOOD);
        this.woodProvider(Blocks.STRIPPED_JUNGLE_LOG).logWithHorizontal(Blocks.STRIPPED_JUNGLE_LOG).wood(Blocks.STRIPPED_JUNGLE_WOOD);
        this.createHangingSign(Blocks.STRIPPED_JUNGLE_LOG, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.JUNGLE_SAPLING, Blocks.POTTED_JUNGLE_SAPLING, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createTintedLeaves(Blocks.JUNGLE_LEAVES, TexturedModel.LEAVES, -12012264);
        this.woodProvider(Blocks.CRIMSON_STEM).log(Blocks.CRIMSON_STEM).wood(Blocks.CRIMSON_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_CRIMSON_STEM).log(Blocks.STRIPPED_CRIMSON_STEM).wood(Blocks.STRIPPED_CRIMSON_HYPHAE);
        this.createHangingSign(Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.CRIMSON_FUNGUS, Blocks.POTTED_CRIMSON_FUNGUS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createNetherRoots(Blocks.CRIMSON_ROOTS, Blocks.POTTED_CRIMSON_ROOTS);
        this.woodProvider(Blocks.WARPED_STEM).log(Blocks.WARPED_STEM).wood(Blocks.WARPED_HYPHAE);
        this.woodProvider(Blocks.STRIPPED_WARPED_STEM).log(Blocks.STRIPPED_WARPED_STEM).wood(Blocks.STRIPPED_WARPED_HYPHAE);
        this.createHangingSign(Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN);
        this.createPlantWithDefaultItem(Blocks.WARPED_FUNGUS, Blocks.POTTED_WARPED_FUNGUS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.createNetherRoots(Blocks.WARPED_ROOTS, Blocks.POTTED_WARPED_ROOTS);
        this.woodProvider(Blocks.BAMBOO_BLOCK).logUVLocked(Blocks.BAMBOO_BLOCK);
        this.woodProvider(Blocks.STRIPPED_BAMBOO_BLOCK).logUVLocked(Blocks.STRIPPED_BAMBOO_BLOCK);
        this.createHangingSign(Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
        this.createCrossBlock(Blocks.NETHER_SPROUTS, BlockModelGenerators.PlantType.NOT_TINTED);
        this.registerSimpleFlatItemModel(Items.NETHER_SPROUTS);
        this.createDoor(Blocks.IRON_DOOR);
        this.createTrapdoor(Blocks.IRON_TRAPDOOR);
        this.createSmoothStoneSlab();
        this.createPassiveRail(Blocks.RAIL);
        this.createActiveRail(Blocks.POWERED_RAIL);
        this.createActiveRail(Blocks.DETECTOR_RAIL);
        this.createActiveRail(Blocks.ACTIVATOR_RAIL);
        this.createComparator();
        this.createCommandBlock(Blocks.COMMAND_BLOCK);
        this.createCommandBlock(Blocks.REPEATING_COMMAND_BLOCK);
        this.createCommandBlock(Blocks.CHAIN_COMMAND_BLOCK);
        this.createAnvil(Blocks.ANVIL);
        this.createAnvil(Blocks.CHIPPED_ANVIL);
        this.createAnvil(Blocks.DAMAGED_ANVIL);
        this.createBarrel();
        this.createBell();
        this.createFurnace(Blocks.FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.BLAST_FURNACE, TexturedModel.ORIENTABLE_ONLY_TOP);
        this.createFurnace(Blocks.SMOKER, TexturedModel.ORIENTABLE);
        this.createRedstoneWire();
        this.createRespawnAnchor();
        this.createSculkCatalyst();
        this.copyModel(Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS);
        this.copyModel(Blocks.COBBLESTONE, Blocks.INFESTED_COBBLESTONE);
        this.copyModel(Blocks.CRACKED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS);
        this.copyModel(Blocks.MOSSY_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS);
        this.createInfestedStone();
        this.copyModel(Blocks.STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS);
        this.createInfestedDeepslate();
    }

    private void createLightBlock() {
        ItemModel.Unbaked itemmodel$unbaked = ItemModelUtils.plainModel(this.createFlatItemModel(Items.LIGHT));
        Map<Integer, ItemModel.Unbaked> map = new HashMap<>(16);
        PropertyDispatch.C1<Integer> c1 = PropertyDispatch.property(BlockStateProperties.LEVEL);

        for (int i = 0; i <= 15; i++) {
            String s = String.format(Locale.ROOT, "_%02d", i);
            ResourceLocation resourcelocation = TextureMapping.getItemTexture(Items.LIGHT, s);
            c1.select(
                i,
                Variant.variant()
                    .with(
                        VariantProperties.MODEL,
                        ModelTemplates.PARTICLE_ONLY.createWithSuffix(Blocks.LIGHT, s, TextureMapping.particle(resourcelocation), this.modelOutput)
                    )
            );
            ItemModel.Unbaked itemmodel$unbaked1 = ItemModelUtils.plainModel(
                ModelTemplates.FLAT_ITEM
                    .create(ModelLocationUtils.getModelLocation(Items.LIGHT, s), TextureMapping.layer0(resourcelocation), this.modelOutput)
            );
            map.put(i, itemmodel$unbaked1);
        }

        this.itemModelOutput.accept(Items.LIGHT, ItemModelUtils.selectBlockItemProperty(LightBlock.LEVEL, itemmodel$unbaked, map));
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(Blocks.LIGHT).with(c1));
    }

    private void createCandleAndCandleCake(Block p_376372_, Block p_378320_) {
        this.registerSimpleFlatItemModel(p_376372_.asItem());
        TextureMapping texturemapping = TextureMapping.cube(TextureMapping.getBlockTexture(p_376372_));
        TextureMapping texturemapping1 = TextureMapping.cube(TextureMapping.getBlockTexture(p_376372_, "_lit"));
        ResourceLocation resourcelocation = ModelTemplates.CANDLE.createWithSuffix(p_376372_, "_one_candle", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation1 = ModelTemplates.TWO_CANDLES.createWithSuffix(p_376372_, "_two_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation2 = ModelTemplates.THREE_CANDLES.createWithSuffix(p_376372_, "_three_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation3 = ModelTemplates.FOUR_CANDLES.createWithSuffix(p_376372_, "_four_candles", texturemapping, this.modelOutput);
        ResourceLocation resourcelocation4 = ModelTemplates.CANDLE.createWithSuffix(p_376372_, "_one_candle_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation5 = ModelTemplates.TWO_CANDLES.createWithSuffix(p_376372_, "_two_candles_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation6 = ModelTemplates.THREE_CANDLES.createWithSuffix(p_376372_, "_three_candles_lit", texturemapping1, this.modelOutput);
        ResourceLocation resourcelocation7 = ModelTemplates.FOUR_CANDLES.createWithSuffix(p_376372_, "_four_candles_lit", texturemapping1, this.modelOutput);
        this.blockStateOutput
            .accept(
                MultiVariantGenerator.multiVariant(p_376372_)
                    .with(
                        PropertyDispatch.properties(BlockStateProperties.CANDLES, BlockStateProperties.LIT)
                            .select(1, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation))
                            .select(2, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation1))
                            .select(3, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation2))
                            .select(4, false, Variant.variant().with(VariantProperties.MODEL, resourcelocation3))
                            .select(1, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation4))
                            .select(2, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation5))
                            .select(3, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation6))
                            .select(4, true, Variant.variant().with(VariantProperties.MODEL, resourcelocation7))
                    )
            );
        ResourceLocation resourcelocation8 = ModelTemplates.CANDLE_CAKE.create(p_378320_, TextureMapping.candleCake(p_376372_, false), this.modelOutput);
        ResourceLocation resourcelocation9 = ModelTemplates.CANDLE_CAKE.createWithSuffix(p_378320_, "_lit", TextureMapping.candleCake(p_376372_, true), this.modelOutput);
        this.blockStateOutput
            .accept(MultiVariantGenerator.multiVariant(p_378320_).with(createBooleanModelDispatch(BlockStateProperties.LIT, resourcelocation9, resourcelocation8)));
    }

    @OnlyIn(Dist.CLIENT)
    class BlockFamilyProvider {
        private final TextureMapping mapping;
        private final Map<ModelTemplate, ResourceLocation> models = Maps.newHashMap();
        @Nullable
        private BlockFamily family;
        @Nullable
        private ResourceLocation fullBlock;
        private final Set<Block> skipGeneratingModelsFor = new HashSet<>();

        public BlockFamilyProvider(final TextureMapping p_375997_) {
            this.mapping = p_375997_;
        }

        public BlockModelGenerators.BlockFamilyProvider fullBlock(Block p_378517_, ModelTemplate p_376200_) {
            this.fullBlock = p_376200_.create(p_378517_, this.mapping, BlockModelGenerators.this.modelOutput);
            if (BlockModelGenerators.this.fullBlockModelCustomGenerators.containsKey(p_378517_)) {
                BlockModelGenerators.this.blockStateOutput
                    .accept(
                        BlockModelGenerators.this.fullBlockModelCustomGenerators
                            .get(p_378517_)
                            .create(p_378517_, this.fullBlock, this.mapping, BlockModelGenerators.this.modelOutput)
                    );
            } else {
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_378517_, this.fullBlock));
            }

            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider donateModelTo(Block p_375392_, Block p_375457_) {
            ResourceLocation resourcelocation = ModelLocationUtils.getModelLocation(p_375392_);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_375457_, resourcelocation));
            BlockModelGenerators.this.itemModelOutput.copy(p_375392_.asItem(), p_375457_.asItem());
            this.skipGeneratingModelsFor.add(p_375457_);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider button(Block p_377478_) {
            ResourceLocation resourcelocation = ModelTemplates.BUTTON.create(p_377478_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.BUTTON_PRESSED.create(p_377478_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createButton(p_377478_, resourcelocation, resourcelocation1));
            ResourceLocation resourcelocation2 = ModelTemplates.BUTTON_INVENTORY.create(p_377478_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_377478_, resourcelocation2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider wall(Block p_377084_) {
            ResourceLocation resourcelocation = ModelTemplates.WALL_POST.create(p_377084_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.WALL_LOW_SIDE.create(p_377084_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.WALL_TALL_SIDE.create(p_377084_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createWall(p_377084_, resourcelocation, resourcelocation1, resourcelocation2));
            ResourceLocation resourcelocation3 = ModelTemplates.WALL_INVENTORY.create(p_377084_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_377084_, resourcelocation3);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider customFence(Block p_377251_) {
            TextureMapping texturemapping = TextureMapping.customParticle(p_377251_);
            ResourceLocation resourcelocation = ModelTemplates.CUSTOM_FENCE_POST.create(p_377251_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUSTOM_FENCE_SIDE_NORTH.create(p_377251_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.CUSTOM_FENCE_SIDE_EAST.create(p_377251_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.CUSTOM_FENCE_SIDE_SOUTH.create(p_377251_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation4 = ModelTemplates.CUSTOM_FENCE_SIDE_WEST.create(p_377251_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createCustomFence(p_377251_, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, resourcelocation4));
            ResourceLocation resourcelocation5 = ModelTemplates.CUSTOM_FENCE_INVENTORY.create(p_377251_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_377251_, resourcelocation5);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fence(Block p_378548_) {
            ResourceLocation resourcelocation = ModelTemplates.FENCE_POST.create(p_378548_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.FENCE_SIDE.create(p_378548_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createFence(p_378548_, resourcelocation, resourcelocation1));
            ResourceLocation resourcelocation2 = ModelTemplates.FENCE_INVENTORY.create(p_378548_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.registerSimpleItemModel(p_378548_, resourcelocation2);
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider customFenceGate(Block p_378765_) {
            TextureMapping texturemapping = TextureMapping.customParticle(p_378765_);
            ResourceLocation resourcelocation = ModelTemplates.CUSTOM_FENCE_GATE_OPEN.create(p_378765_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUSTOM_FENCE_GATE_CLOSED.create(p_378765_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_OPEN.create(p_378765_, texturemapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.CUSTOM_FENCE_GATE_WALL_CLOSED.create(p_378765_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createFenceGate(p_378765_, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, false));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider fenceGate(Block p_378252_) {
            ResourceLocation resourcelocation = ModelTemplates.FENCE_GATE_OPEN.create(p_378252_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.FENCE_GATE_CLOSED.create(p_378252_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation2 = ModelTemplates.FENCE_GATE_WALL_OPEN.create(p_378252_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation3 = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(p_378252_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput
                .accept(BlockModelGenerators.createFenceGate(p_378252_, resourcelocation, resourcelocation1, resourcelocation2, resourcelocation3, true));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider pressurePlate(Block p_377761_) {
            ResourceLocation resourcelocation = ModelTemplates.PRESSURE_PLATE_UP.create(p_377761_, this.mapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.PRESSURE_PLATE_DOWN.create(p_377761_, this.mapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPressurePlate(p_377761_, resourcelocation, resourcelocation1));
            return this;
        }

        public BlockModelGenerators.BlockFamilyProvider sign(Block p_377458_) {
            if (this.family == null) {
                throw new IllegalStateException("Family not defined");
            } else {
                Block block = this.family.getVariants().get(BlockFamily.Variant.WALL_SIGN);
                ResourceLocation resourcelocation = ModelTemplates.PARTICLE_ONLY.create(p_377458_, this.mapping, BlockModelGenerators.this.modelOutput);
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_377458_, resourcelocation));
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, resourcelocation));
                BlockModelGenerators.this.registerSimpleFlatItemModel(p_377458_.asItem());
                return this;
            }
        }

        public BlockModelGenerators.BlockFamilyProvider slab(Block p_377334_) {
            if (this.fullBlock == null) {
                throw new IllegalStateException("Full block not generated yet");
            } else {
                ResourceLocation resourcelocation = this.getOrCreateModel(ModelTemplates.SLAB_BOTTOM, p_377334_);
                ResourceLocation resourcelocation1 = this.getOrCreateModel(ModelTemplates.SLAB_TOP, p_377334_);
                BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSlab(p_377334_, resourcelocation, resourcelocation1, this.fullBlock));
                BlockModelGenerators.this.registerSimpleItemModel(p_377334_, resourcelocation);
                return this;
            }
        }

        public BlockModelGenerators.BlockFamilyProvider stairs(Block p_376299_) {
            ResourceLocation resourcelocation = this.getOrCreateModel(ModelTemplates.STAIRS_INNER, p_376299_);
            ResourceLocation resourcelocation1 = this.getOrCreateModel(ModelTemplates.STAIRS_STRAIGHT, p_376299_);
            ResourceLocation resourcelocation2 = this.getOrCreateModel(ModelTemplates.STAIRS_OUTER, p_376299_);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createStairs(p_376299_, resourcelocation, resourcelocation1, resourcelocation2));
            BlockModelGenerators.this.registerSimpleItemModel(p_376299_, resourcelocation1);
            return this;
        }

        private BlockModelGenerators.BlockFamilyProvider fullBlockVariant(Block p_376700_) {
            TexturedModel texturedmodel = BlockModelGenerators.this.texturedModels.getOrDefault(p_376700_, TexturedModel.CUBE.get(p_376700_));
            ResourceLocation resourcelocation = texturedmodel.create(p_376700_, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(p_376700_, resourcelocation));
            return this;
        }

        private BlockModelGenerators.BlockFamilyProvider door(Block p_378741_) {
            BlockModelGenerators.this.createDoor(p_378741_);
            return this;
        }

        private void trapdoor(Block p_378286_) {
            if (BlockModelGenerators.this.nonOrientableTrapdoor.contains(p_378286_)) {
                BlockModelGenerators.this.createTrapdoor(p_378286_);
            } else {
                BlockModelGenerators.this.createOrientableTrapdoor(p_378286_);
            }
        }

        private ResourceLocation getOrCreateModel(ModelTemplate p_375991_, Block p_376743_) {
            return this.models.computeIfAbsent(p_375991_, p_377362_ -> p_377362_.create(p_376743_, this.mapping, BlockModelGenerators.this.modelOutput));
        }

        public BlockModelGenerators.BlockFamilyProvider generateFor(BlockFamily p_376238_) {
            this.family = p_376238_;
            p_376238_.getVariants().forEach((p_375413_, p_375795_) -> {
                if (!this.skipGeneratingModelsFor.contains(p_375795_)) {
                    BiConsumer<BlockModelGenerators.BlockFamilyProvider, Block> biconsumer = BlockModelGenerators.SHAPE_CONSUMERS.get(p_375413_);
                    if (biconsumer != null) {
                        biconsumer.accept(this, p_375795_);
                    }
                }
            });
            return this;
        }
    }

    @FunctionalInterface
    @OnlyIn(Dist.CLIENT)
    interface BlockStateGeneratorSupplier {
        BlockStateGenerator create(
            Block p_375423_, ResourceLocation p_377694_, TextureMapping p_378118_, BiConsumer<ResourceLocation, ModelInstance> p_377645_
        );
    }

    @OnlyIn(Dist.CLIENT)
    static record BookSlotModelCacheKey(ModelTemplate template, String modelSuffix) {
    }

    @OnlyIn(Dist.CLIENT)
    static enum PlantType {
        TINTED(ModelTemplates.TINTED_CROSS, ModelTemplates.TINTED_FLOWER_POT_CROSS, false),
        NOT_TINTED(ModelTemplates.CROSS, ModelTemplates.FLOWER_POT_CROSS, false),
        EMISSIVE_NOT_TINTED(ModelTemplates.CROSS_EMISSIVE, ModelTemplates.FLOWER_POT_CROSS_EMISSIVE, true);

        private final ModelTemplate blockTemplate;
        private final ModelTemplate flowerPotTemplate;
        private final boolean isEmissive;

        private PlantType(final ModelTemplate p_376693_, final ModelTemplate p_377578_, final boolean p_378590_) {
            this.blockTemplate = p_376693_;
            this.flowerPotTemplate = p_377578_;
            this.isEmissive = p_378590_;
        }

        public ModelTemplate getCross() {
            return this.blockTemplate;
        }

        public ModelTemplate getCrossPot() {
            return this.flowerPotTemplate;
        }

        public ResourceLocation createItemModel(BlockModelGenerators p_378438_, Block p_377000_) {
            Item item = p_377000_.asItem();
            return this.isEmissive ? p_378438_.createFlatItemModelWithBlockTextureAndOverlay(item, p_377000_, "_emissive") : p_378438_.createFlatItemModelWithBlockTexture(item, p_377000_);
        }

        public TextureMapping getTextureMapping(Block p_377046_) {
            return this.isEmissive ? TextureMapping.crossEmissive(p_377046_) : TextureMapping.cross(p_377046_);
        }

        public TextureMapping getPlantTextureMapping(Block p_378688_) {
            return this.isEmissive ? TextureMapping.plantEmissive(p_378688_) : TextureMapping.plant(p_378688_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WoodProvider {
        private final TextureMapping logMapping;

        public WoodProvider(final TextureMapping p_378405_) {
            this.logMapping = p_378405_;
        }

        public BlockModelGenerators.WoodProvider wood(Block p_376713_) {
            TextureMapping texturemapping = this.logMapping.copyAndUpdate(TextureSlot.END, this.logMapping.get(TextureSlot.SIDE));
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(p_376713_, texturemapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(p_376713_, resourcelocation));
            return this;
        }

        public BlockModelGenerators.WoodProvider log(Block p_378573_) {
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(p_378573_, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createAxisAlignedPillarBlock(p_378573_, resourcelocation));
            return this;
        }

        public BlockModelGenerators.WoodProvider logWithHorizontal(Block p_376308_) {
            ResourceLocation resourcelocation = ModelTemplates.CUBE_COLUMN.create(p_376308_, this.logMapping, BlockModelGenerators.this.modelOutput);
            ResourceLocation resourcelocation1 = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(p_376308_, this.logMapping, BlockModelGenerators.this.modelOutput);
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createRotatedPillarWithHorizontalVariant(p_376308_, resourcelocation, resourcelocation1));
            return this;
        }

        public BlockModelGenerators.WoodProvider logUVLocked(Block p_376867_) {
            BlockModelGenerators.this.blockStateOutput.accept(BlockModelGenerators.createPillarBlockUVLocked(p_376867_, this.logMapping, BlockModelGenerators.this.modelOutput));
            return this;
        }
    }
}