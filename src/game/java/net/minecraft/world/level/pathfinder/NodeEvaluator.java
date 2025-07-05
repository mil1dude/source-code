package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class NodeEvaluator {
    protected PathfindingContext currentContext;
    protected Mob mob;
    protected final Int2ObjectMap<Node> nodes = new Int2ObjectOpenHashMap<>();
    protected int entityWidth;
    protected int entityHeight;
    protected int entityDepth;
    protected boolean canPassDoors = true;
    protected boolean canOpenDoors;
    protected boolean canFloat;
    protected boolean canWalkOverFences;

    public void prepare(PathNavigationRegion p_77347_, Mob p_77348_) {
        this.currentContext = new PathfindingContext(p_77347_, p_77348_);
        this.mob = p_77348_;
        this.nodes.clear();
        this.entityWidth = Mth.floor(p_77348_.getBbWidth() + 1.0F);
        this.entityHeight = Mth.floor(p_77348_.getBbHeight() + 1.0F);
        this.entityDepth = Mth.floor(p_77348_.getBbWidth() + 1.0F);
    }

    public void done() {
        this.currentContext = null;
        this.mob = null;
    }

    protected Node getNode(BlockPos p_77350_) {
        return this.getNode(p_77350_.getX(), p_77350_.getY(), p_77350_.getZ());
    }

    protected Node getNode(int p_77325_, int p_77326_, int p_77327_) {
        return this.nodes.computeIfAbsent(Node.createHash(p_77325_, p_77326_, p_77327_), p_77332_ -> new Node(p_77325_, p_77326_, p_77327_));
    }

    public abstract Node getStart();

    public abstract Target getTarget(double p_336317_, double p_334044_, double p_334139_);

    protected Target getTargetNodeAt(double p_328825_, double p_331532_, double p_333874_) {
        return new Target(this.getNode(Mth.floor(p_328825_), Mth.floor(p_331532_), Mth.floor(p_333874_)));
    }

    public abstract int getNeighbors(Node[] p_77353_, Node p_77354_);

    public abstract PathType getPathTypeOfMob(PathfindingContext p_335888_, int p_331986_, int p_331764_, int p_335132_, Mob p_329853_);

    public abstract PathType getPathType(PathfindingContext p_334172_, int p_335319_, int p_333029_, int p_332756_);

    public PathType getPathType(Mob p_330121_, BlockPos p_332460_) {
        return this.getPathType(new PathfindingContext(p_330121_.level(), p_330121_), p_332460_.getX(), p_332460_.getY(), p_332460_.getZ());
    }

    public void setCanPassDoors(boolean p_77352_) {
        this.canPassDoors = p_77352_;
    }

    public void setCanOpenDoors(boolean p_77356_) {
        this.canOpenDoors = p_77356_;
    }

    public void setCanFloat(boolean p_77359_) {
        this.canFloat = p_77359_;
    }

    public void setCanWalkOverFences(boolean p_255862_) {
        this.canWalkOverFences = p_255862_;
    }

    public boolean canPassDoors() {
        return this.canPassDoors;
    }

    public boolean canOpenDoors() {
        return this.canOpenDoors;
    }

    public boolean canFloat() {
        return this.canFloat;
    }

    public boolean canWalkOverFences() {
        return this.canWalkOverFences;
    }

    public static boolean isBurningBlock(BlockState p_329628_) {
        return p_329628_.is(BlockTags.FIRE)
            || p_329628_.is(Blocks.LAVA)
            || p_329628_.is(Blocks.MAGMA_BLOCK)
            || CampfireBlock.isLitCampfire(p_329628_)
            || p_329628_.is(Blocks.LAVA_CAULDRON);
    }
}