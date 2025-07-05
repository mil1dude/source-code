package net.minecraft.world.entity.raid;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

public class Raids extends SavedData {
    private static final String RAID_FILE_ID = "raids";
    private final Map<Integer, Raid> raidMap = Maps.newHashMap();
    private final ServerLevel level;
    private int nextAvailableID;
    private int tick;

    public static SavedData.Factory<Raids> factory(ServerLevel p_300199_) {
        return new SavedData.Factory<>(() -> new Raids(p_300199_), (p_296865_, p_334354_) -> load(p_300199_, p_296865_), DataFixTypes.SAVED_DATA_RAIDS);
    }

    public Raids(ServerLevel p_37956_) {
        this.level = p_37956_;
        this.nextAvailableID = 1;
        this.setDirty();
    }

    public Raid get(int p_37959_) {
        return this.raidMap.get(p_37959_);
    }

    public void tick() {
        this.tick++;
        Iterator<Raid> iterator = this.raidMap.values().iterator();

        while (iterator.hasNext()) {
            Raid raid = iterator.next();
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
                raid.stop();
            }

            if (raid.isStopped()) {
                iterator.remove();
                this.setDirty();
            } else {
                raid.tick();
            }
        }

        if (this.tick % 200 == 0) {
            this.setDirty();
        }

        DebugPackets.sendRaids(this.level, this.raidMap.values());
    }

    public static boolean canJoinRaid(Raider p_37966_, Raid p_37967_) {
        return p_37966_ != null && p_37967_ != null && p_37967_.getLevel() != null
            ? p_37966_.isAlive() && p_37966_.canJoinRaid() && p_37966_.getNoActionTime() <= 2400 && p_37966_.level().dimensionType() == p_37967_.getLevel().dimensionType()
            : false;
    }

    @Nullable
    public Raid createOrExtendRaid(ServerPlayer p_37964_, BlockPos p_336355_) {
        if (p_37964_.isSpectator()) {
            return null;
        } else if (this.level.getGameRules().getBoolean(GameRules.RULE_DISABLE_RAIDS)) {
            return null;
        } else {
            DimensionType dimensiontype = p_37964_.level().dimensionType();
            if (!dimensiontype.hasRaids()) {
                return null;
            } else {
                List<PoiRecord> list = this.level
                    .getPoiManager()
                    .getInRange(p_219845_ -> p_219845_.is(PoiTypeTags.VILLAGE), p_336355_, 64, PoiManager.Occupancy.IS_OCCUPIED)
                    .toList();
                int i = 0;
                Vec3 vec3 = Vec3.ZERO;

                for (PoiRecord poirecord : list) {
                    BlockPos blockpos = poirecord.getPos();
                    vec3 = vec3.add((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
                    i++;
                }

                BlockPos blockpos1;
                if (i > 0) {
                    vec3 = vec3.scale(1.0 / (double)i);
                    blockpos1 = BlockPos.containing(vec3);
                } else {
                    blockpos1 = p_336355_;
                }

                Raid raid = this.getOrCreateRaid(p_37964_.serverLevel(), blockpos1);
                if (!raid.isStarted() && !this.raidMap.containsKey(raid.getId())) {
                    this.raidMap.put(raid.getId(), raid);
                }

                if (!raid.isStarted() || raid.getRaidOmenLevel() < raid.getMaxRaidOmenLevel()) {
                    raid.absorbRaidOmen(p_37964_);
                }

                this.setDirty();
                return raid;
            }
        }
    }

    private Raid getOrCreateRaid(ServerLevel p_37961_, BlockPos p_37962_) {
        Raid raid = p_37961_.getRaidAt(p_37962_);
        return raid != null ? raid : new Raid(this.getUniqueId(), p_37961_, p_37962_);
    }

    public static Raids load(ServerLevel p_150236_, CompoundTag p_150237_) {
        Raids raids = new Raids(p_150236_);
        raids.nextAvailableID = p_150237_.getInt("NextAvailableID");
        raids.tick = p_150237_.getInt("Tick");
        ListTag listtag = p_150237_.getList("Raids", 10);

        for (int i = 0; i < listtag.size(); i++) {
            CompoundTag compoundtag = listtag.getCompound(i);
            Raid raid = new Raid(p_150236_, compoundtag);
            raids.raidMap.put(raid.getId(), raid);
        }

        return raids;
    }

    @Override
    public CompoundTag save(CompoundTag p_37976_, HolderLookup.Provider p_335569_) {
        p_37976_.putInt("NextAvailableID", this.nextAvailableID);
        p_37976_.putInt("Tick", this.tick);
        ListTag listtag = new ListTag();

        for (Raid raid : this.raidMap.values()) {
            CompoundTag compoundtag = new CompoundTag();
            raid.save(compoundtag);
            listtag.add(compoundtag);
        }

        p_37976_.put("Raids", listtag);
        return p_37976_;
    }

    public static String getFileId(Holder<DimensionType> p_211597_) {
        return p_211597_.is(BuiltinDimensionTypes.END) ? "raids_end" : "raids";
    }

    private int getUniqueId() {
        return ++this.nextAvailableID;
    }

    @Nullable
    public Raid getNearbyRaid(BlockPos p_37971_, int p_37972_) {
        Raid raid = null;
        double d0 = (double)p_37972_;

        for (Raid raid1 : this.raidMap.values()) {
            double d1 = raid1.getCenter().distSqr(p_37971_);
            if (raid1.isActive() && d1 < d0) {
                raid = raid1;
                d0 = d1;
            }
        }

        return raid;
    }
}