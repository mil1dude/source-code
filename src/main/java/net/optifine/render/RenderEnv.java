package net.optifine.render;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.ModelBlockRenderer; // oder ModelBlockRenderer je nach Mapping
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.StateDefinition;
import net.optifine.model.ListQuadsOverlay;

public class RenderEnv {
    private BlockState blockState;
    private BlockPos blockPos;
    private int blockId = -1;
    private int metadata = -1;
    private int breakingAnimation = -1;
    private int smartLeaves = -1;
    private float[] quadBounds = new float[Direction.values().length * 2]; // Direction → Direction
    private BitSet boundsFlags = new BitSet(3);
    // private ModelBlockRenderer.AmbientOcclusionFace aoFace = new ModelBlockRenderer.AmbientOcclusionFace(); // ModelBlockRenderer → ModelBlockRenderer
    private BlockPos colorizerBlockPosM = null;
    private boolean[] borderFlags = null;
    private boolean[] borderFlags2 = null;
    private boolean[] borderFlags3 = null;
    private Direction[] borderDirections = null; // Direction → Direction
    private List<BakedQuad> listQuadsCustomizer = new ArrayList<>();
    private List<BakedQuad> listQuadsCtmMultipass = new ArrayList<>();
    private BakedQuad[] arrayQuadsCtm1 = new BakedQuad[1];
    private BakedQuad[] arrayQuadsCtm2 = new BakedQuad[2];
    private BakedQuad[] arrayQuadsCtm3 = new BakedQuad[3];
    private BakedQuad[] arrayQuadsCtm4 = new BakedQuad[4];
    // RenderRegionCache ist meist nicht mehr öffentlich, ggf. als Object oder eigene Klasse
    private Object regionRenderCacheBuilder = null; 
    // ListQuadsOverlay[] und EnumLevelBlockLayer ggf. anpassen, da EnumLevelBlockLayer nicht mehr existiert
    // Using RenderType instead of EnumLevelBlockLayer in 1.21.4
    private ListQuadsOverlay[] listsQuadsOverlay = new ListQuadsOverlay[4]; // Assuming 4 render types/layers
    private boolean overlaysRendered = false;
    private static final int UNKNOWN = -1;
    private static final int FALSE = 0;
    private static final int TRUE = 1;

    public RenderEnv(BlockState blockState, BlockPos blockPos) {
        this.blockState = blockState;
        this.blockPos = blockPos;
    }

    public void reset(BlockState blockStateIn, BlockPos blockPosIn) {
        if (this.blockState != blockStateIn || this.blockPos != blockPosIn) {
            this.blockState = blockStateIn;
            this.blockPos = blockPosIn;
            this.blockId = -1;
            this.metadata = -1;
            this.breakingAnimation = -1;
            this.smartLeaves = -1;
            this.boundsFlags.clear();
        }
    }

    public int getBlockId() {
        if (this.blockId < 0) {
            Block block = this.blockState.getBlock();
            this.blockId = Block.getId(block.defaultBlockState());
        }
        return this.blockId;
    }

    public int getMetadata() {
        if (this.metadata < 0) {
            // In 1.21.4, metadata is not directly available, use block state properties if needed
            this.metadata = 0; // Default to 0 as metadata is largely deprecated
        }
        return this.metadata;
    }

    public float[] getQuadBounds() {
        return this.quadBounds;
    }

    public BitSet getBoundsFlags() {
        return this.boundsFlags;
    }

    // public ModelBlockRenderer.AmbientOcclusionFace getAoFace() {
    //     return this.aoFace;
    // }

    public boolean isBreakingAnimation(List<BakedQuad> listQuads) {
        if (this.breakingAnimation == -1 && !listQuads.isEmpty()) {
            // BreakingFour check removed as it's not available in 1.21.4
            this.breakingAnimation = 0; // Default to not breaking
        }
        return this.breakingAnimation == 1;
    }

    public boolean isBreakingAnimation(BakedQuad quad) {
        if (this.breakingAnimation < 0) {
            // BreakingFour check removed as it's not available in 1.21.4
            this.breakingAnimation = 0; // Default to not breaking
        }
        return this.breakingAnimation == 1;
    }

    public boolean isBreakingAnimation() {
        return this.breakingAnimation == 1;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public BlockPos getColorizerBlockPosM() {
        if (this.colorizerBlockPosM == null) {
            this.colorizerBlockPosM = new BlockPos(0, 0, 0);
        }

        return this.colorizerBlockPosM;
    }

    public boolean[] getBorderFlags() {
        if (this.borderFlags == null) {
            this.borderFlags = new boolean[4];
        }

        return this.borderFlags;
    }

    public boolean[] getBorderFlags2() {
        if (this.borderFlags2 == null) {
            this.borderFlags2 = new boolean[4];
        }

        return this.borderFlags2;
    }

    public boolean[] getBorderFlags3() {
        if (this.borderFlags3 == null) {
            this.borderFlags3 = new boolean[4];
        }

        return this.borderFlags3;
    }

    public Direction[] getBorderDirections() {
        if (this.borderDirections == null) {
            this.borderDirections = new Direction[4];
        }

        return this.borderDirections;
    }

    public Direction[] getBorderDirections(Direction dir0, Direction dir1, Direction dir2, Direction dir3) {
        Direction[] aenumfacing = this.getBorderDirections();
        aenumfacing[0] = dir0;
        aenumfacing[1] = dir1;
        aenumfacing[2] = dir2;
        aenumfacing[3] = dir3;
        return aenumfacing;
    }

    public boolean isSmartLeaves() {
        if (this.smartLeaves == -1) {
            if (this.blockState.getBlock() instanceof LeavesBlock) {
                this.smartLeaves = 1;
            } else {
                this.smartLeaves = 0;
            }
        }

        return this.smartLeaves == 1;
    }

    public List<BakedQuad> getListQuadsCustomizer() {
        return this.listQuadsCustomizer;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad) {
        this.arrayQuadsCtm1[0] = quad;
        return this.arrayQuadsCtm1;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad0, BakedQuad quad1) {
        this.arrayQuadsCtm2[0] = quad0;
        this.arrayQuadsCtm2[1] = quad1;
        return this.arrayQuadsCtm2;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad0, BakedQuad quad1, BakedQuad quad2) {
        this.arrayQuadsCtm3[0] = quad0;
        this.arrayQuadsCtm3[1] = quad1;
        this.arrayQuadsCtm3[2] = quad2;
        return this.arrayQuadsCtm3;
    }

    public BakedQuad[] getArrayQuadsCtm(BakedQuad quad0, BakedQuad quad1, BakedQuad quad2, BakedQuad quad3) {
        this.arrayQuadsCtm4[0] = quad0;
        this.arrayQuadsCtm4[1] = quad1;
        this.arrayQuadsCtm4[2] = quad2;
        this.arrayQuadsCtm4[3] = quad3;
        return this.arrayQuadsCtm4;
    }

    public List<BakedQuad> getListQuadsCtmMultipass(BakedQuad[] quads) {
        this.listQuadsCtmMultipass.clear();

        if (quads != null) {
            for (int i = 0; i < quads.length; ++i) {
                BakedQuad bakedquad = quads[i];
                this.listQuadsCtmMultipass.add(bakedquad);
            }
        }

        return this.listQuadsCtmMultipass;
    }

    public Object getRenderRegionCache() {
        return this.regionRenderCacheBuilder;
    }

    public void setRenderRegionCache(Object regionRenderCacheBuilder) {
        this.regionRenderCacheBuilder = regionRenderCacheBuilder;
    }

    public ListQuadsOverlay getListQuadsOverlay(int layer) {
        ListQuadsOverlay listquadsoverlay = this.listsQuadsOverlay[layer];
        if (listquadsoverlay == null) {
            listquadsoverlay = new ListQuadsOverlay();
            this.listsQuadsOverlay[layer] = listquadsoverlay;
        }
        return listquadsoverlay;
    }

    public boolean isOverlaysRendered() {
        return this.overlaysRendered;
    }

    public void setOverlaysRendered(boolean overlaysRendered) {
        this.overlaysRendered = overlaysRendered;
    }
}
