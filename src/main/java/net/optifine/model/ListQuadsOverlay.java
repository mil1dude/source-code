package net.optifine.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.level.block.Blocks;

public class ListQuadsOverlay {
    private List<BakedQuad> listQuads = new ArrayList<>();
    private List<BlockState> listBlockStates = new ArrayList<>();
    private List<BakedQuad> listQuadsSingle = Arrays.<BakedQuad>asList(new BakedQuad[1]);

    public void addQuad(BakedQuad quad, BlockState blockState) {
        if (quad != null) {
            this.listQuads.add(quad);
            this.listBlockStates.add(blockState);
        }
    }

    public int size() {
        return this.listQuads.size();
    }

    public BakedQuad getQuad(int index) {
        return this.listQuads.get(index);
    }

    public BlockState getBlockState(int index) {
        return index >= 0 && index < this.listBlockStates.size() ? this.listBlockStates.get(index)
                : Blocks.AIR.defaultBlockState();
    }

    public List<BakedQuad> getListQuadsSingle(BakedQuad quad) {
        this.listQuadsSingle.set(0, quad);
        return this.listQuadsSingle;
    }

    public void clear() {
        this.listQuads.clear();
        this.listBlockStates.clear();
    }
}
