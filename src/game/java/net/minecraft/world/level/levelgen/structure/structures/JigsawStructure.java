package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public final class JigsawStructure extends Structure {
    public static final DimensionPadding DEFAULT_DIMENSION_PADDING = DimensionPadding.ZERO;
    public static final LiquidSettings DEFAULT_LIQUID_SETTINGS = LiquidSettings.APPLY_WATERLOGGING;
    public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
    public static final int MIN_DEPTH = 0;
    public static final int MAX_DEPTH = 20;
    public static final MapCodec<JigsawStructure> CODEC = RecordCodecBuilder.<JigsawStructure>mapCodec(
            p_227640_ -> p_227640_.group(
                        settingsCodec(p_227640_),
                        StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(p_227656_ -> p_227656_.startPool),
                        ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(p_227654_ -> p_227654_.startJigsawName),
                        Codec.intRange(0, 20).fieldOf("size").forGetter(p_227652_ -> p_227652_.maxDepth),
                        HeightProvider.CODEC.fieldOf("start_height").forGetter(p_227649_ -> p_227649_.startHeight),
                        Codec.BOOL.fieldOf("use_expansion_hack").forGetter(p_227646_ -> p_227646_.useExpansionHack),
                        Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(p_227644_ -> p_227644_.projectStartToHeightmap),
                        Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(p_227642_ -> p_227642_.maxDistanceFromCenter),
                        Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(p_309350_ -> p_309350_.poolAliases),
                        DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DEFAULT_DIMENSION_PADDING).forGetter(p_341952_ -> p_341952_.dimensionPadding),
                        LiquidSettings.CODEC.optionalFieldOf("liquid_settings", DEFAULT_LIQUID_SETTINGS).forGetter(p_341953_ -> p_341953_.liquidSettings)
                    )
                    .apply(p_227640_, JigsawStructure::new)
        )
        .validate(JigsawStructure::verifyRange);
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    private static DataResult<JigsawStructure> verifyRange(JigsawStructure p_286886_) {
        int i = switch (p_286886_.terrainAdaptation()) {
            case NONE -> 0;
            case BURY, BEARD_THIN, BEARD_BOX, ENCAPSULATE -> 12;
        };
        return p_286886_.maxDistanceFromCenter + i > 128
            ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128")
            : DataResult.success(p_286886_);
    }

    public JigsawStructure(
        Structure.StructureSettings p_227627_,
        Holder<StructureTemplatePool> p_227628_,
        Optional<ResourceLocation> p_227629_,
        int p_227630_,
        HeightProvider p_227631_,
        boolean p_227632_,
        Optional<Heightmap.Types> p_227633_,
        int p_227634_,
        List<PoolAliasBinding> p_312703_,
        DimensionPadding p_344382_,
        LiquidSettings p_344801_
    ) {
        super(p_227627_);
        this.startPool = p_227628_;
        this.startJigsawName = p_227629_;
        this.maxDepth = p_227630_;
        this.startHeight = p_227631_;
        this.useExpansionHack = p_227632_;
        this.projectStartToHeightmap = p_227633_;
        this.maxDistanceFromCenter = p_227634_;
        this.poolAliases = p_312703_;
        this.dimensionPadding = p_344382_;
        this.liquidSettings = p_344801_;
    }

    public JigsawStructure(
        Structure.StructureSettings p_227620_,
        Holder<StructureTemplatePool> p_227621_,
        int p_227622_,
        HeightProvider p_227623_,
        boolean p_227624_,
        Heightmap.Types p_227625_
    ) {
        this(p_227620_, p_227621_, Optional.empty(), p_227622_, p_227623_, p_227624_, Optional.of(p_227625_), 80, List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    public JigsawStructure(
        Structure.StructureSettings p_227614_, Holder<StructureTemplatePool> p_227615_, int p_227616_, HeightProvider p_227617_, boolean p_227618_
    ) {
        this(p_227614_, p_227615_, Optional.empty(), p_227616_, p_227617_, p_227618_, Optional.empty(), 80, List.of(), DEFAULT_DIMENSION_PADDING, DEFAULT_LIQUID_SETTINGS);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext p_227636_) {
        ChunkPos chunkpos = p_227636_.chunkPos();
        int i = this.startHeight.sample(p_227636_.random(), new WorldGenerationContext(p_227636_.chunkGenerator(), p_227636_.heightAccessor()));
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), i, chunkpos.getMinBlockZ());
        return JigsawPlacement.addPieces(
            p_227636_,
            this.startPool,
            this.startJigsawName,
            this.maxDepth,
            blockpos,
            this.useExpansionHack,
            this.projectStartToHeightmap,
            this.maxDistanceFromCenter,
            PoolAliasLookup.create(this.poolAliases, blockpos, p_227636_.seed()),
            this.dimensionPadding,
            this.liquidSettings
        );
    }

    @Override
    public StructureType<?> type() {
        return StructureType.JIGSAW;
    }
}