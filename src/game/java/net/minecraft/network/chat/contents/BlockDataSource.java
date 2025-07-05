package net.minecraft.network.chat.contents;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public record BlockDataSource(String posPattern, @Nullable Coordinates compiledPos) implements DataSource {
    public static final MapCodec<BlockDataSource> SUB_CODEC = RecordCodecBuilder.mapCodec(
        p_309816_ -> p_309816_.group(Codec.STRING.fieldOf("block").forGetter(BlockDataSource::posPattern)).apply(p_309816_, BlockDataSource::new)
    );
    public static final DataSource.Type<BlockDataSource> TYPE = new DataSource.Type<>(SUB_CODEC, "block");

    public BlockDataSource(String p_237312_) {
        this(p_237312_, compilePos(p_237312_));
    }

    @Nullable
    private static Coordinates compilePos(String p_237318_) {
        try {
            return BlockPosArgument.blockPos().parse(new StringReader(p_237318_));
        } catch (CommandSyntaxException commandsyntaxexception) {
            return null;
        }
    }

    @Override
    public Stream<CompoundTag> getData(CommandSourceStack p_237323_) {
        if (this.compiledPos != null) {
            ServerLevel serverlevel = p_237323_.getLevel();
            BlockPos blockpos = this.compiledPos.getBlockPos(p_237323_);
            if (serverlevel.isLoaded(blockpos)) {
                BlockEntity blockentity = serverlevel.getBlockEntity(blockpos);
                if (blockentity != null) {
                    return Stream.of(blockentity.saveWithFullMetadata(p_237323_.registryAccess()));
                }
            }
        }

        return Stream.empty();
    }

    @Override
    public DataSource.Type<?> type() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "block=" + this.posPattern;
    }

    @Override
    public boolean equals(Object p_237321_) {
        if (this == p_237321_) {
            return true;
        } else {
            if (p_237321_ instanceof BlockDataSource blockdatasource && this.posPattern.equals(blockdatasource.posPattern)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.posPattern.hashCode();
    }
}