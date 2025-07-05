package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class StructureBlockEntity extends BlockEntity {
    private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
    public static final int MAX_OFFSET_PER_AXIS = 48;
    public static final int MAX_SIZE_PER_AXIS = 48;
    public static final String AUTHOR_TAG = "author";
    @Nullable
    private ResourceLocation structureName;
    private String author = "";
    private String metaData = "";
    private BlockPos structurePos = new BlockPos(0, 1, 0);
    private Vec3i structureSize = Vec3i.ZERO;
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private StructureMode mode;
    private boolean ignoreEntities = true;
    private boolean powered;
    private boolean showAir;
    private boolean showBoundingBox = true;
    private float integrity = 1.0F;
    private long seed;

    public StructureBlockEntity(BlockPos p_155779_, BlockState p_155780_) {
        super(BlockEntityType.STRUCTURE_BLOCK, p_155779_, p_155780_);
        this.mode = p_155780_.getValue(StructureBlock.MODE);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187524_, HolderLookup.Provider p_331199_) {
        super.saveAdditional(p_187524_, p_331199_);
        p_187524_.putString("name", this.getStructureName());
        p_187524_.putString("author", this.author);
        p_187524_.putString("metadata", this.metaData);
        p_187524_.putInt("posX", this.structurePos.getX());
        p_187524_.putInt("posY", this.structurePos.getY());
        p_187524_.putInt("posZ", this.structurePos.getZ());
        p_187524_.putInt("sizeX", this.structureSize.getX());
        p_187524_.putInt("sizeY", this.structureSize.getY());
        p_187524_.putInt("sizeZ", this.structureSize.getZ());
        p_187524_.putString("rotation", this.rotation.toString());
        p_187524_.putString("mirror", this.mirror.toString());
        p_187524_.putString("mode", this.mode.toString());
        p_187524_.putBoolean("ignoreEntities", this.ignoreEntities);
        p_187524_.putBoolean("powered", this.powered);
        p_187524_.putBoolean("showair", this.showAir);
        p_187524_.putBoolean("showboundingbox", this.showBoundingBox);
        p_187524_.putFloat("integrity", this.integrity);
        p_187524_.putLong("seed", this.seed);
    }

    @Override
    protected void loadAdditional(CompoundTag p_332447_, HolderLookup.Provider p_334283_) {
        super.loadAdditional(p_332447_, p_334283_);
        this.setStructureName(p_332447_.getString("name"));
        this.author = p_332447_.getString("author");
        this.metaData = p_332447_.getString("metadata");
        int i = Mth.clamp(p_332447_.getInt("posX"), -48, 48);
        int j = Mth.clamp(p_332447_.getInt("posY"), -48, 48);
        int k = Mth.clamp(p_332447_.getInt("posZ"), -48, 48);
        this.structurePos = new BlockPos(i, j, k);
        int l = Mth.clamp(p_332447_.getInt("sizeX"), 0, 48);
        int i1 = Mth.clamp(p_332447_.getInt("sizeY"), 0, 48);
        int j1 = Mth.clamp(p_332447_.getInt("sizeZ"), 0, 48);
        this.structureSize = new Vec3i(l, i1, j1);

        try {
            this.rotation = Rotation.valueOf(p_332447_.getString("rotation"));
        } catch (IllegalArgumentException illegalargumentexception2) {
            this.rotation = Rotation.NONE;
        }

        try {
            this.mirror = Mirror.valueOf(p_332447_.getString("mirror"));
        } catch (IllegalArgumentException illegalargumentexception1) {
            this.mirror = Mirror.NONE;
        }

        try {
            this.mode = StructureMode.valueOf(p_332447_.getString("mode"));
        } catch (IllegalArgumentException illegalargumentexception) {
            this.mode = StructureMode.DATA;
        }

        this.ignoreEntities = p_332447_.getBoolean("ignoreEntities");
        this.powered = p_332447_.getBoolean("powered");
        this.showAir = p_332447_.getBoolean("showair");
        this.showBoundingBox = p_332447_.getBoolean("showboundingbox");
        if (p_332447_.contains("integrity")) {
            this.integrity = p_332447_.getFloat("integrity");
        } else {
            this.integrity = 1.0F;
        }

        this.seed = p_332447_.getLong("seed");
        this.updateBlockState();
    }

    private void updateBlockState() {
        if (this.level != null) {
            BlockPos blockpos = this.getBlockPos();
            BlockState blockstate = this.level.getBlockState(blockpos);
            if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
                this.level.setBlock(blockpos, blockstate.setValue(StructureBlock.MODE, this.mode), 2);
            }
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p_327713_) {
        return this.saveCustomOnly(p_327713_);
    }

    public boolean usedBy(Player p_59854_) {
        if (!p_59854_.canUseGameMasterBlocks()) {
            return false;
        } else {
            if (p_59854_.getCommandSenderWorld().isClientSide) {
                p_59854_.openStructureBlock(this);
            }

            return true;
        }
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String p_59869_) {
        this.setStructureName(StringUtil.isNullOrEmpty(p_59869_) ? null : ResourceLocation.tryParse(p_59869_));
    }

    public void setStructureName(@Nullable ResourceLocation p_59875_) {
        this.structureName = p_59875_;
    }

    public void createdBy(LivingEntity p_59852_) {
        this.author = p_59852_.getName().getString();
    }

    public BlockPos getStructurePos() {
        return this.structurePos;
    }

    public void setStructurePos(BlockPos p_59886_) {
        this.structurePos = p_59886_;
    }

    public Vec3i getStructureSize() {
        return this.structureSize;
    }

    public void setStructureSize(Vec3i p_155798_) {
        this.structureSize = p_155798_;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror p_59882_) {
        this.mirror = p_59882_;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public void setRotation(Rotation p_59884_) {
        this.rotation = p_59884_;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String p_59888_) {
        this.metaData = p_59888_;
    }

    public StructureMode getMode() {
        return this.mode;
    }

    public void setMode(StructureMode p_59861_) {
        this.mode = p_59861_;
        BlockState blockstate = this.level.getBlockState(this.getBlockPos());
        if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(this.getBlockPos(), blockstate.setValue(StructureBlock.MODE, p_59861_), 2);
        }
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public void setIgnoreEntities(boolean p_59877_) {
        this.ignoreEntities = p_59877_;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float p_59839_) {
        this.integrity = p_59839_;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long p_59841_) {
        this.seed = p_59841_;
    }

    public boolean detectSize() {
        if (this.mode != StructureMode.SAVE) {
            return false;
        } else {
            BlockPos blockpos = this.getBlockPos();
            int i = 80;
            BlockPos blockpos1 = new BlockPos(blockpos.getX() - 80, this.level.getMinY(), blockpos.getZ() - 80);
            BlockPos blockpos2 = new BlockPos(blockpos.getX() + 80, this.level.getMaxY(), blockpos.getZ() + 80);
            Stream<BlockPos> stream = this.getRelatedCorners(blockpos1, blockpos2);
            return calculateEnclosingBoundingBox(blockpos, stream)
                .filter(
                    p_155790_ -> {
                        int j = p_155790_.maxX() - p_155790_.minX();
                        int k = p_155790_.maxY() - p_155790_.minY();
                        int l = p_155790_.maxZ() - p_155790_.minZ();
                        if (j > 1 && k > 1 && l > 1) {
                            this.structurePos = new BlockPos(
                                p_155790_.minX() - blockpos.getX() + 1,
                                p_155790_.minY() - blockpos.getY() + 1,
                                p_155790_.minZ() - blockpos.getZ() + 1
                            );
                            this.structureSize = new Vec3i(j - 1, k - 1, l - 1);
                            this.setChanged();
                            BlockState blockstate = this.level.getBlockState(blockpos);
                            this.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                            return true;
                        } else {
                            return false;
                        }
                    }
                )
                .isPresent();
        }
    }

    private Stream<BlockPos> getRelatedCorners(BlockPos p_155792_, BlockPos p_155793_) {
        return BlockPos.betweenClosedStream(p_155792_, p_155793_)
            .filter(p_272561_ -> this.level.getBlockState(p_272561_).is(Blocks.STRUCTURE_BLOCK))
            .map(this.level::getBlockEntity)
            .filter(p_155802_ -> p_155802_ instanceof StructureBlockEntity)
            .map(p_155785_ -> (StructureBlockEntity)p_155785_)
            .filter(p_155787_ -> p_155787_.mode == StructureMode.CORNER && Objects.equals(this.structureName, p_155787_.structureName))
            .map(BlockEntity::getBlockPos);
    }

    private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos p_155795_, Stream<BlockPos> p_155796_) {
        Iterator<BlockPos> iterator = p_155796_.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        } else {
            BlockPos blockpos = iterator.next();
            BoundingBox boundingbox = new BoundingBox(blockpos);
            if (iterator.hasNext()) {
                iterator.forEachRemaining(boundingbox::encapsulate);
            } else {
                boundingbox.encapsulate(p_155795_);
            }

            return Optional.of(boundingbox);
        }
    }

    public boolean saveStructure() {
        return this.mode != StructureMode.SAVE ? false : this.saveStructure(true);
    }

    public boolean saveStructure(boolean p_59890_) {
        if (this.structureName == null) {
            return false;
        } else {
            BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
            ServerLevel serverlevel = (ServerLevel)this.level;
            StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

            StructureTemplate structuretemplate;
            try {
                structuretemplate = structuretemplatemanager.getOrCreate(this.structureName);
            } catch (ResourceLocationException resourcelocationexception1) {
                return false;
            }

            structuretemplate.fillFromWorld(this.level, blockpos, this.structureSize, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
            structuretemplate.setAuthor(this.author);
            if (p_59890_) {
                try {
                    return structuretemplatemanager.save(this.structureName);
                } catch (ResourceLocationException resourcelocationexception) {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    public static RandomSource createRandom(long p_222889_) {
        return p_222889_ == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(p_222889_);
    }

    public boolean placeStructureIfSameSize(ServerLevel p_310062_) {
        if (this.mode == StructureMode.LOAD && this.structureName != null) {
            StructureTemplate structuretemplate = p_310062_.getStructureManager().get(this.structureName).orElse(null);
            if (structuretemplate == null) {
                return false;
            } else if (structuretemplate.getSize().equals(this.structureSize)) {
                this.placeStructure(p_310062_, structuretemplate);
                return true;
            } else {
                this.loadStructureInfo(structuretemplate);
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean loadStructureInfo(ServerLevel p_312602_) {
        StructureTemplate structuretemplate = this.getStructureTemplate(p_312602_);
        if (structuretemplate == null) {
            return false;
        } else {
            this.loadStructureInfo(structuretemplate);
            return true;
        }
    }

    private void loadStructureInfo(StructureTemplate p_311753_) {
        this.author = !StringUtil.isNullOrEmpty(p_311753_.getAuthor()) ? p_311753_.getAuthor() : "";
        this.structureSize = p_311753_.getSize();
        this.setChanged();
    }

    public void placeStructure(ServerLevel p_312292_) {
        StructureTemplate structuretemplate = this.getStructureTemplate(p_312292_);
        if (structuretemplate != null) {
            this.placeStructure(p_312292_, structuretemplate);
        }
    }

    @Nullable
    private StructureTemplate getStructureTemplate(ServerLevel p_310290_) {
        return this.structureName == null ? null : p_310290_.getStructureManager().get(this.structureName).orElse(null);
    }

    private void placeStructure(ServerLevel p_311121_, StructureTemplate p_312324_) {
        this.loadStructureInfo(p_312324_);
        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings().setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities);
        if (this.integrity < 1.0F) {
            structureplacesettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0F, 1.0F))).setRandom(createRandom(this.seed));
        }

        BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
        p_312324_.placeInWorld(p_311121_, blockpos, blockpos, structureplacesettings, createRandom(this.seed), 2);
    }

    public void unloadStructure() {
        if (this.structureName != null) {
            ServerLevel serverlevel = (ServerLevel)this.level;
            StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();
            structuretemplatemanager.remove(this.structureName);
        }
    }

    public boolean isStructureLoadable() {
        if (this.mode == StructureMode.LOAD && !this.level.isClientSide && this.structureName != null) {
            ServerLevel serverlevel = (ServerLevel)this.level;
            StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

            try {
                return structuretemplatemanager.get(this.structureName).isPresent();
            } catch (ResourceLocationException resourcelocationexception) {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean p_59894_) {
        this.powered = p_59894_;
    }

    public boolean getShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean p_59897_) {
        this.showAir = p_59897_;
    }

    public boolean getShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean p_59899_) {
        this.showBoundingBox = p_59899_;
    }

    public static enum UpdateType {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA;
    }
}