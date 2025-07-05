package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public record SerializableChunkData(
    Registry<Biome> biomeRegistry,
    ChunkPos chunkPos,
    int minSectionY,
    long lastUpdateTime,
    long inhabitedTime,
    ChunkStatus chunkStatus,
    @Nullable BlendingData.Packed blendingData,
    @Nullable BelowZeroRetrogen belowZeroRetrogen,
    UpgradeData upgradeData,
    @Nullable long[] carvingMask,
    Map<Heightmap.Types, long[]> heightmaps,
    ChunkAccess.PackedTicks packedTicks,
    ShortList[] postProcessingSections,
    boolean lightCorrect,
    List<SerializableChunkData.SectionData> sectionData,
    List<CompoundTag> entities,
    List<CompoundTag> blockEntities,
    CompoundTag structureData
) {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
        Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    @Nullable
    public static SerializableChunkData parse(LevelHeightAccessor p_366637_, RegistryAccess p_364474_, CompoundTag p_368975_) {
        if (!p_368975_.contains("Status", 8)) {
            return null;
        } else {
            ChunkPos chunkpos = new ChunkPos(p_368975_.getInt("xPos"), p_368975_.getInt("zPos"));
            long i = p_368975_.getLong("LastUpdate");
            long j = p_368975_.getLong("InhabitedTime");
            ChunkStatus chunkstatus = ChunkStatus.byName(p_368975_.getString("Status"));
            UpgradeData upgradedata = p_368975_.contains("UpgradeData", 10)
                ? new UpgradeData(p_368975_.getCompound("UpgradeData"), p_366637_)
                : UpgradeData.EMPTY;
            boolean flag = p_368975_.getBoolean("isLightOn");
            BlendingData.Packed blendingdata$packed;
            if (p_368975_.contains("blending_data", 10)) {
                blendingdata$packed = BlendingData.Packed.CODEC
                    .parse(NbtOps.INSTANCE, p_368975_.getCompound("blending_data"))
                    .resultOrPartial(LOGGER::error)
                    .orElse(null);
            } else {
                blendingdata$packed = null;
            }

            BelowZeroRetrogen belowzeroretrogen;
            if (p_368975_.contains("below_zero_retrogen", 10)) {
                belowzeroretrogen = BelowZeroRetrogen.CODEC
                    .parse(NbtOps.INSTANCE, p_368975_.getCompound("below_zero_retrogen"))
                    .resultOrPartial(LOGGER::error)
                    .orElse(null);
            } else {
                belowzeroretrogen = null;
            }

            long[] along;
            if (p_368975_.contains("carving_mask", 12)) {
                along = p_368975_.getLongArray("carving_mask");
            } else {
                along = null;
            }

            CompoundTag compoundtag = p_368975_.getCompound("Heightmaps");
            Map<Heightmap.Types, long[]> map = new EnumMap<>(Heightmap.Types.class);

            for (Heightmap.Types heightmap$types : chunkstatus.heightmapsAfter()) {
                String s = heightmap$types.getSerializationKey();
                if (compoundtag.contains(s, 12)) {
                    map.put(heightmap$types, compoundtag.getLongArray(s));
                }
            }

            List<SavedTick<Block>> list1 = SavedTick.loadTickList(
                p_368975_.getList("block_ticks", 10), p_367354_ -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(p_367354_)), chunkpos
            );
            List<SavedTick<Fluid>> list2 = SavedTick.loadTickList(
                p_368975_.getList("fluid_ticks", 10), p_369392_ -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(p_369392_)), chunkpos
            );
            ChunkAccess.PackedTicks chunkaccess$packedticks = new ChunkAccess.PackedTicks(list1, list2);
            ListTag listtag = p_368975_.getList("PostProcessing", 9);
            ShortList[] ashortlist = new ShortList[listtag.size()];

            for (int k = 0; k < listtag.size(); k++) {
                ListTag listtag1 = listtag.getList(k);
                ShortList shortlist = new ShortArrayList(listtag1.size());

                for (int l = 0; l < listtag1.size(); l++) {
                    shortlist.add(listtag1.getShort(l));
                }

                ashortlist[k] = shortlist;
            }

            List<CompoundTag> list3 = Lists.transform(p_368975_.getList("entities", 10), p_364930_ -> (CompoundTag)p_364930_);
            List<CompoundTag> list4 = Lists.transform(p_368975_.getList("block_entities", 10), p_370018_ -> (CompoundTag)p_370018_);
            CompoundTag compoundtag2 = p_368975_.getCompound("structures");
            ListTag listtag2 = p_368975_.getList("sections", 10);
            List<SerializableChunkData.SectionData> list = new ArrayList<>(listtag2.size());
            Registry<Biome> registry = p_364474_.lookupOrThrow(Registries.BIOME);
            Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(registry);

            for (int i1 = 0; i1 < listtag2.size(); i1++) {
                CompoundTag compoundtag1 = listtag2.getCompound(i1);
                int j1 = compoundtag1.getByte("Y");
                LevelChunkSection levelchunksection;
                if (j1 >= p_366637_.getMinSectionY() && j1 <= p_366637_.getMaxSectionY()) {
                    PalettedContainer<BlockState> palettedcontainer;
                    if (compoundtag1.contains("block_states", 10)) {
                        palettedcontainer = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, compoundtag1.getCompound("block_states"))
                            .promotePartial(p_362514_ -> logErrors(chunkpos, j1, p_362514_))
                            .getOrThrow(SerializableChunkData.ChunkReadException::new);
                    } else {
                        palettedcontainer = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                    }

                    PalettedContainerRO<Holder<Biome>> palettedcontainerro;
                    if (compoundtag1.contains("biomes", 10)) {
                        palettedcontainerro = codec.parse(NbtOps.INSTANCE, compoundtag1.getCompound("biomes"))
                            .promotePartial(p_362842_ -> logErrors(chunkpos, j1, p_362842_))
                            .getOrThrow(SerializableChunkData.ChunkReadException::new);
                    } else {
                        palettedcontainerro = new PalettedContainer<>(
                            registry.asHolderIdMap(), registry.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES
                        );
                    }

                    levelchunksection = new LevelChunkSection(palettedcontainer, palettedcontainerro);
                } else {
                    levelchunksection = null;
                }

                DataLayer datalayer = compoundtag1.contains("BlockLight", 7) ? new DataLayer(compoundtag1.getByteArray("BlockLight")) : null;
                DataLayer datalayer1 = compoundtag1.contains("SkyLight", 7) ? new DataLayer(compoundtag1.getByteArray("SkyLight")) : null;
                list.add(new SerializableChunkData.SectionData(j1, levelchunksection, datalayer, datalayer1));
            }

            return new SerializableChunkData(
                registry,
                chunkpos,
                p_366637_.getMinSectionY(),
                i,
                j,
                chunkstatus,
                blendingdata$packed,
                belowzeroretrogen,
                upgradedata,
                along,
                map,
                chunkaccess$packedticks,
                ashortlist,
                flag,
                list,
                list3,
                list4,
                compoundtag2
            );
        }
    }

    public ProtoChunk read(ServerLevel p_368634_, PoiManager p_362734_, RegionStorageInfo p_366907_, ChunkPos p_363624_) {
        if (!Objects.equals(p_363624_, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", p_363624_, p_363624_, this.chunkPos);
            p_368634_.getServer().reportMisplacedChunk(this.chunkPos, p_363624_, p_366907_);
        }

        int i = p_368634_.getSectionsCount();
        LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
        boolean flag = p_368634_.dimensionType().hasSkyLight();
        ChunkSource chunksource = p_368634_.getChunkSource();
        LevelLightEngine levellightengine = chunksource.getLightEngine();
        Registry<Biome> registry = p_368634_.registryAccess().lookupOrThrow(Registries.BIOME);
        boolean flag1 = false;

        for (SerializableChunkData.SectionData serializablechunkdata$sectiondata : this.sectionData) {
            SectionPos sectionpos = SectionPos.of(p_363624_, serializablechunkdata$sectiondata.y);
            if (serializablechunkdata$sectiondata.chunkSection != null) {
                alevelchunksection[p_368634_.getSectionIndexFromSectionY(serializablechunkdata$sectiondata.y)] = serializablechunkdata$sectiondata.chunkSection;
                p_362734_.checkConsistencyWithBlocks(sectionpos, serializablechunkdata$sectiondata.chunkSection);
            }

            boolean flag2 = serializablechunkdata$sectiondata.blockLight != null;
            boolean flag3 = flag && serializablechunkdata$sectiondata.skyLight != null;
            if (flag2 || flag3) {
                if (!flag1) {
                    levellightengine.retainData(p_363624_, true);
                    flag1 = true;
                }

                if (flag2) {
                    levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, serializablechunkdata$sectiondata.blockLight);
                }

                if (flag3) {
                    levellightengine.queueSectionData(LightLayer.SKY, sectionpos, serializablechunkdata$sectiondata.skyLight);
                }
            }
        }

        ChunkType chunktype = this.chunkStatus.getChunkType();
        ChunkAccess chunkaccess;
        if (chunktype == ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> levelchunkticks = new LevelChunkTicks<>(this.packedTicks.blocks());
            LevelChunkTicks<Fluid> levelchunkticks1 = new LevelChunkTicks<>(this.packedTicks.fluids());
            chunkaccess = new LevelChunk(
                p_368634_.getLevel(),
                p_363624_,
                this.upgradeData,
                levelchunkticks,
                levelchunkticks1,
                this.inhabitedTime,
                alevelchunksection,
                postLoadChunk(p_368634_, this.entities, this.blockEntities),
                BlendingData.unpack(this.blendingData)
            );
        } else {
            ProtoChunkTicks<Block> protochunkticks = ProtoChunkTicks.load(this.packedTicks.blocks());
            ProtoChunkTicks<Fluid> protochunkticks1 = ProtoChunkTicks.load(this.packedTicks.fluids());
            ProtoChunk protochunk1 = new ProtoChunk(
                p_363624_, this.upgradeData, alevelchunksection, protochunkticks, protochunkticks1, p_368634_, registry, BlendingData.unpack(this.blendingData)
            );
            chunkaccess = protochunk1;
            protochunk1.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protochunk1.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }

            protochunk1.setPersistedStatus(this.chunkStatus);
            if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                protochunk1.setLightEngine(levellightengine);
            }
        }

        chunkaccess.setLightCorrect(this.lightCorrect);
        EnumSet<Heightmap.Types> enumset = EnumSet.noneOf(Heightmap.Types.class);

        for (Heightmap.Types heightmap$types : chunkaccess.getPersistedStatus().heightmapsAfter()) {
            long[] along = this.heightmaps.get(heightmap$types);
            if (along != null) {
                chunkaccess.setHeightmap(heightmap$types, along);
            } else {
                enumset.add(heightmap$types);
            }
        }

        Heightmap.primeHeightmaps(chunkaccess, enumset);
        chunkaccess.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(p_368634_), this.structureData, p_368634_.getSeed()));
        chunkaccess.setAllReferences(unpackStructureReferences(p_368634_.registryAccess(), p_363624_, this.structureData));

        for (int j = 0; j < this.postProcessingSections.length; j++) {
            chunkaccess.addPackedPostProcess(this.postProcessingSections[j], j);
        }

        if (chunktype == ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)chunkaccess, false);
        } else {
            ProtoChunk protochunk = (ProtoChunk)chunkaccess;

            for (CompoundTag compoundtag : this.entities) {
                protochunk.addEntity(compoundtag);
            }

            for (CompoundTag compoundtag1 : this.blockEntities) {
                protochunk.setBlockEntityNbt(compoundtag1);
            }

            if (this.carvingMask != null) {
                protochunk.setCarvingMask(new CarvingMask(this.carvingMask, chunkaccess.getMinY()));
            }

            return protochunk;
        }
    }

    private static void logErrors(ChunkPos p_362005_, int p_366847_, String p_369695_) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", p_362005_.x, p_366847_, p_362005_.z, p_369695_);
    }

    private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> p_368397_) {
        return PalettedContainer.codecRO(
            p_368397_.asHolderIdMap(), p_368397_.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, p_368397_.getOrThrow(Biomes.PLAINS)
        );
    }

    public static SerializableChunkData copyOf(ServerLevel p_369088_, ChunkAccess p_363062_) {
        if (!p_363062_.canBeSerialized()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + p_363062_);
        } else {
            ChunkPos chunkpos = p_363062_.getPos();
            List<SerializableChunkData.SectionData> list = new ArrayList<>();
            LevelChunkSection[] alevelchunksection = p_363062_.getSections();
            LevelLightEngine levellightengine = p_369088_.getChunkSource().getLightEngine();

            for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); i++) {
                int j = p_363062_.getSectionIndexFromSectionY(i);
                boolean flag = j >= 0 && j < alevelchunksection.length;
                DataLayer datalayer = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
                DataLayer datalayer1 = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
                DataLayer datalayer2 = datalayer != null && !datalayer.isEmpty() ? datalayer.copy() : null;
                DataLayer datalayer3 = datalayer1 != null && !datalayer1.isEmpty() ? datalayer1.copy() : null;
                if (flag || datalayer2 != null || datalayer3 != null) {
                    LevelChunkSection levelchunksection = flag ? alevelchunksection[j].copy() : null;
                    list.add(new SerializableChunkData.SectionData(i, levelchunksection, datalayer2, datalayer3));
                }
            }

            List<CompoundTag> list1 = new ArrayList<>(p_363062_.getBlockEntitiesPos().size());

            for (BlockPos blockpos : p_363062_.getBlockEntitiesPos()) {
                CompoundTag compoundtag = p_363062_.getBlockEntityNbtForSaving(blockpos, p_369088_.registryAccess());
                if (compoundtag != null) {
                    list1.add(compoundtag);
                }
            }

            List<CompoundTag> list2 = new ArrayList<>();
            long[] along = null;
            if (p_363062_.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                ProtoChunk protochunk = (ProtoChunk)p_363062_;
                list2.addAll(protochunk.getEntities());
                CarvingMask carvingmask = protochunk.getCarvingMask();
                if (carvingmask != null) {
                    along = carvingmask.toArray();
                }
            }

            Map<Heightmap.Types, long[]> map = new EnumMap<>(Heightmap.Types.class);

            for (Entry<Heightmap.Types, Heightmap> entry : p_363062_.getHeightmaps()) {
                if (p_363062_.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) {
                    long[] along1 = entry.getValue().getRawData();
                    map.put(entry.getKey(), (long[])along1.clone());
                }
            }

            ChunkAccess.PackedTicks chunkaccess$packedticks = p_363062_.getTicksForSerialization(p_369088_.getGameTime());
            ShortList[] ashortlist = Arrays.stream(p_363062_.getPostProcessing())
                .map(p_366782_ -> p_366782_ != null ? new ShortArrayList(p_366782_) : null)
                .toArray(ShortList[]::new);
            CompoundTag compoundtag1 = packStructureData(StructurePieceSerializationContext.fromLevel(p_369088_), chunkpos, p_363062_.getAllStarts(), p_363062_.getAllReferences());
            return new SerializableChunkData(
                p_369088_.registryAccess().lookupOrThrow(Registries.BIOME),
                chunkpos,
                p_363062_.getMinSectionY(),
                p_369088_.getGameTime(),
                p_363062_.getInhabitedTime(),
                p_363062_.getPersistedStatus(),
                Optionull.map(p_363062_.getBlendingData(), BlendingData::pack),
                p_363062_.getBelowZeroRetrogen(),
                p_363062_.getUpgradeData().copy(),
                along,
                map,
                chunkaccess$packedticks,
                ashortlist,
                p_363062_.isLightCorrect(),
                list,
                list2,
                list1,
                compoundtag1
            );
        }
    }

    public CompoundTag write() {
        CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundtag.putInt("xPos", this.chunkPos.x);
        compoundtag.putInt("yPos", this.minSectionY);
        compoundtag.putInt("zPos", this.chunkPos.z);
        compoundtag.putLong("LastUpdate", this.lastUpdateTime);
        compoundtag.putLong("InhabitedTime", this.inhabitedTime);
        compoundtag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        if (this.blendingData != null) {
            BlendingData.Packed.CODEC
                .encodeStart(NbtOps.INSTANCE, this.blendingData)
                .resultOrPartial(LOGGER::error)
                .ifPresent(p_365550_ -> compoundtag.put("blending_data", p_365550_));
        }

        if (this.belowZeroRetrogen != null) {
            BelowZeroRetrogen.CODEC
                .encodeStart(NbtOps.INSTANCE, this.belowZeroRetrogen)
                .resultOrPartial(LOGGER::error)
                .ifPresent(p_366866_ -> compoundtag.put("below_zero_retrogen", p_366866_));
        }

        if (!this.upgradeData.isEmpty()) {
            compoundtag.put("UpgradeData", this.upgradeData.write());
        }

        ListTag listtag = new ListTag();
        Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(this.biomeRegistry);

        for (SerializableChunkData.SectionData serializablechunkdata$sectiondata : this.sectionData) {
            CompoundTag compoundtag1 = new CompoundTag();
            LevelChunkSection levelchunksection = serializablechunkdata$sectiondata.chunkSection;
            if (levelchunksection != null) {
                compoundtag1.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, levelchunksection.getStates()).getOrThrow());
                compoundtag1.put("biomes", codec.encodeStart(NbtOps.INSTANCE, levelchunksection.getBiomes()).getOrThrow());
            }

            if (serializablechunkdata$sectiondata.blockLight != null) {
                compoundtag1.putByteArray("BlockLight", serializablechunkdata$sectiondata.blockLight.getData());
            }

            if (serializablechunkdata$sectiondata.skyLight != null) {
                compoundtag1.putByteArray("SkyLight", serializablechunkdata$sectiondata.skyLight.getData());
            }

            if (!compoundtag1.isEmpty()) {
                compoundtag1.putByte("Y", (byte)serializablechunkdata$sectiondata.y);
                listtag.add(compoundtag1);
            }
        }

        compoundtag.put("sections", listtag);
        if (this.lightCorrect) {
            compoundtag.putBoolean("isLightOn", true);
        }

        ListTag listtag1 = new ListTag();
        listtag1.addAll(this.blockEntities);
        compoundtag.put("block_entities", listtag1);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            ListTag listtag2 = new ListTag();
            listtag2.addAll(this.entities);
            compoundtag.put("entities", listtag2);
            if (this.carvingMask != null) {
                compoundtag.putLongArray("carving_mask", this.carvingMask);
            }
        }

        saveTicks(compoundtag, this.packedTicks);
        compoundtag.put("PostProcessing", packOffsets(this.postProcessingSections));
        CompoundTag compoundtag2 = new CompoundTag();
        this.heightmaps.forEach((p_369025_, p_369618_) -> compoundtag2.put(p_369025_.getSerializationKey(), new LongArrayTag(p_369618_)));
        compoundtag.put("Heightmaps", compoundtag2);
        compoundtag.put("structures", this.structureData);
        return compoundtag;
    }

    private static void saveTicks(CompoundTag p_366243_, ChunkAccess.PackedTicks p_367613_) {
        ListTag listtag = new ListTag();

        for (SavedTick<Block> savedtick : p_367613_.blocks()) {
            listtag.add(savedtick.save(p_367401_ -> BuiltInRegistries.BLOCK.getKey(p_367401_).toString()));
        }

        p_366243_.put("block_ticks", listtag);
        ListTag listtag1 = new ListTag();

        for (SavedTick<Fluid> savedtick1 : p_367613_.fluids()) {
            listtag1.add(savedtick1.save(p_368053_ -> BuiltInRegistries.FLUID.getKey(p_368053_).toString()));
        }

        p_366243_.put("fluid_ticks", listtag1);
    }

    public static ChunkType getChunkTypeFromTag(@Nullable CompoundTag p_362607_) {
        return p_362607_ != null ? ChunkStatus.byName(p_362607_.getString("Status")).getChunkType() : ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel p_367726_, List<CompoundTag> p_368624_, List<CompoundTag> p_369871_) {
        return p_368624_.isEmpty() && p_369871_.isEmpty() ? null : p_361871_ -> {
            if (!p_368624_.isEmpty()) {
                p_367726_.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(p_368624_, p_367726_, EntitySpawnReason.LOAD));
            }

            for (CompoundTag compoundtag : p_369871_) {
                boolean flag = compoundtag.getBoolean("keepPacked");
                if (flag) {
                    p_361871_.setBlockEntityNbt(compoundtag);
                } else {
                    BlockPos blockpos = BlockEntity.getPosFromTag(compoundtag);
                    BlockEntity blockentity = BlockEntity.loadStatic(blockpos, p_361871_.getBlockState(blockpos), compoundtag, p_367726_.registryAccess());
                    if (blockentity != null) {
                        p_361871_.setBlockEntity(blockentity);
                    }
                }
            }
        };
    }

    private static CompoundTag packStructureData(
        StructurePieceSerializationContext p_365342_, ChunkPos p_366115_, Map<Structure, StructureStart> p_361842_, Map<Structure, LongSet> p_369653_
    ) {
        CompoundTag compoundtag = new CompoundTag();
        CompoundTag compoundtag1 = new CompoundTag();
        Registry<Structure> registry = p_365342_.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (Entry<Structure, StructureStart> entry : p_361842_.entrySet()) {
            ResourceLocation resourcelocation = registry.getKey(entry.getKey());
            compoundtag1.put(resourcelocation.toString(), entry.getValue().createTag(p_365342_, p_366115_));
        }

        compoundtag.put("starts", compoundtag1);
        CompoundTag compoundtag2 = new CompoundTag();

        for (Entry<Structure, LongSet> entry1 : p_369653_.entrySet()) {
            if (!entry1.getValue().isEmpty()) {
                ResourceLocation resourcelocation1 = registry.getKey(entry1.getKey());
                compoundtag2.put(resourcelocation1.toString(), new LongArrayTag(entry1.getValue()));
            }
        }

        compoundtag.put("References", compoundtag2);
        return compoundtag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext p_368168_, CompoundTag p_361005_, long p_364111_) {
        Map<Structure, StructureStart> map = Maps.newHashMap();
        Registry<Structure> registry = p_368168_.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundtag = p_361005_.getCompound("starts");

        for (String s : compoundtag.getAllKeys()) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
            Structure structure = registry.getValue(resourcelocation);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", resourcelocation);
            } else {
                StructureStart structurestart = StructureStart.loadStaticStart(p_368168_, compoundtag.getCompound(s), p_364111_);
                if (structurestart != null) {
                    map.put(structure, structurestart);
                }
            }
        }

        return map;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess p_360899_, ChunkPos p_366437_, CompoundTag p_368599_) {
        Map<Structure, LongSet> map = Maps.newHashMap();
        Registry<Structure> registry = p_360899_.lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundtag = p_368599_.getCompound("References");

        for (String s : compoundtag.getAllKeys()) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
            Structure structure = registry.getValue(resourcelocation);
            if (structure == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", resourcelocation, p_366437_);
            } else {
                long[] along = compoundtag.getLongArray(s);
                if (along.length != 0) {
                    map.put(structure, new LongOpenHashSet(Arrays.stream(along).filter(p_365743_ -> {
                        ChunkPos chunkpos = new ChunkPos(p_365743_);
                        if (chunkpos.getChessboardDistance(p_366437_) > 8) {
                            LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", resourcelocation, chunkpos, p_366437_);
                            return false;
                        } else {
                            return true;
                        }
                    }).toArray()));
                }
            }
        }

        return map;
    }

    private static ListTag packOffsets(ShortList[] p_365024_) {
        ListTag listtag = new ListTag();

        for (ShortList shortlist : p_365024_) {
            ListTag listtag1 = new ListTag();
            if (shortlist != null) {
                for (int i = 0; i < shortlist.size(); i++) {
                    listtag1.add(ShortTag.valueOf(shortlist.getShort(i)));
                }
            }

            listtag.add(listtag1);
        }

        return listtag;
    }

    public static class ChunkReadException extends NbtException {
        public ChunkReadException(String p_364016_) {
            super(p_364016_);
        }
    }

    public static record SectionData(int y, @Nullable LevelChunkSection chunkSection, @Nullable DataLayer blockLight, @Nullable DataLayer skyLight) {
    }
}