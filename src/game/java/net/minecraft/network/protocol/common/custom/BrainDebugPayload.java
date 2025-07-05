package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BrainDebugPayload(BrainDebugPayload.BrainDump brainDump) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, BrainDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(
        BrainDebugPayload::write, BrainDebugPayload::new
    );
    public static final CustomPacketPayload.Type<BrainDebugPayload> TYPE = CustomPacketPayload.createType("debug/brain");

    private BrainDebugPayload(FriendlyByteBuf p_299558_) {
        this(new BrainDebugPayload.BrainDump(p_299558_));
    }

    private void write(FriendlyByteBuf p_300305_) {
        this.brainDump.write(p_300305_);
    }

    @Override
    public CustomPacketPayload.Type<BrainDebugPayload> type() {
        return TYPE;
    }

    public static record BrainDump(
        UUID uuid,
        int id,
        String name,
        String profession,
        int xp,
        float health,
        float maxHealth,
        Vec3 pos,
        String inventory,
        @Nullable Path path,
        boolean wantsGolem,
        int angerLevel,
        List<String> activities,
        List<String> behaviors,
        List<String> memories,
        List<String> gossips,
        Set<BlockPos> pois,
        Set<BlockPos> potentialPois
    ) {
        public BrainDump(FriendlyByteBuf p_298042_) {
            this(
                p_298042_.readUUID(),
                p_298042_.readInt(),
                p_298042_.readUtf(),
                p_298042_.readUtf(),
                p_298042_.readInt(),
                p_298042_.readFloat(),
                p_298042_.readFloat(),
                p_298042_.readVec3(),
                p_298042_.readUtf(),
                p_298042_.readNullable(Path::createFromStream),
                p_298042_.readBoolean(),
                p_298042_.readInt(),
                p_298042_.readList(FriendlyByteBuf::readUtf),
                p_298042_.readList(FriendlyByteBuf::readUtf),
                p_298042_.readList(FriendlyByteBuf::readUtf),
                p_298042_.readList(FriendlyByteBuf::readUtf),
                p_298042_.readCollection(HashSet::new, BlockPos.STREAM_CODEC),
                p_298042_.readCollection(HashSet::new, BlockPos.STREAM_CODEC)
            );
        }

        public void write(FriendlyByteBuf p_299431_) {
            p_299431_.writeUUID(this.uuid);
            p_299431_.writeInt(this.id);
            p_299431_.writeUtf(this.name);
            p_299431_.writeUtf(this.profession);
            p_299431_.writeInt(this.xp);
            p_299431_.writeFloat(this.health);
            p_299431_.writeFloat(this.maxHealth);
            p_299431_.writeVec3(this.pos);
            p_299431_.writeUtf(this.inventory);
            p_299431_.writeNullable(this.path, (p_297936_, p_301045_) -> p_301045_.writeToStream(p_297936_));
            p_299431_.writeBoolean(this.wantsGolem);
            p_299431_.writeInt(this.angerLevel);
            p_299431_.writeCollection(this.activities, FriendlyByteBuf::writeUtf);
            p_299431_.writeCollection(this.behaviors, FriendlyByteBuf::writeUtf);
            p_299431_.writeCollection(this.memories, FriendlyByteBuf::writeUtf);
            p_299431_.writeCollection(this.gossips, FriendlyByteBuf::writeUtf);
            p_299431_.writeCollection(this.pois, BlockPos.STREAM_CODEC);
            p_299431_.writeCollection(this.potentialPois, BlockPos.STREAM_CODEC);
        }

        public boolean hasPoi(BlockPos p_301193_) {
            return this.pois.contains(p_301193_);
        }

        public boolean hasPotentialPoi(BlockPos p_300342_) {
            return this.potentialPois.contains(p_300342_);
        }
    }
}