package net.minecraft.world.level.saveddata.maps;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

public class MapFrame {
    private final BlockPos pos;
    private final int rotation;
    private final int entityId;

    public MapFrame(BlockPos p_77866_, int p_77867_, int p_77868_) {
        this.pos = p_77866_;
        this.rotation = p_77867_;
        this.entityId = p_77868_;
    }

    @Nullable
    public static MapFrame load(CompoundTag p_77873_) {
        Optional<BlockPos> optional = NbtUtils.readBlockPos(p_77873_, "pos");
        if (optional.isEmpty()) {
            return null;
        } else {
            int i = p_77873_.getInt("rotation");
            int j = p_77873_.getInt("entity_id");
            return new MapFrame(optional.get(), i, j);
        }
    }

    public CompoundTag save() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.put("pos", NbtUtils.writeBlockPos(this.pos));
        compoundtag.putInt("rotation", this.rotation);
        compoundtag.putInt("entity_id", this.entityId);
        return compoundtag;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getRotation() {
        return this.rotation;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public String getId() {
        return frameId(this.pos);
    }

    public static String frameId(BlockPos p_77871_) {
        return "frame-" + p_77871_.getX() + "," + p_77871_.getY() + "," + p_77871_.getZ();
    }
}