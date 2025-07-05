package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;

public record StorageValue(ResourceLocation storage, NbtPathArgument.NbtPath path) implements NumberProvider {
    public static final MapCodec<StorageValue> CODEC = RecordCodecBuilder.mapCodec(
        p_330652_ -> p_330652_.group(
                    ResourceLocation.CODEC.fieldOf("storage").forGetter(StorageValue::storage),
                    NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(StorageValue::path)
                )
                .apply(p_330652_, StorageValue::new)
    );

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.STORAGE;
    }

    private Optional<NumericTag> getNumericTag(LootContext p_329012_) {
        CompoundTag compoundtag = p_329012_.getLevel().getServer().getCommandStorage().get(this.storage);

        try {
            List<Tag> list = this.path.get(compoundtag);
            if (list.size() == 1 && list.get(0) instanceof NumericTag numerictag) {
                return Optional.of(numerictag);
            }
        } catch (CommandSyntaxException commandsyntaxexception) {
        }

        return Optional.empty();
    }

    @Override
    public float getFloat(LootContext p_334554_) {
        return this.getNumericTag(p_334554_).map(NumericTag::getAsFloat).orElse(0.0F);
    }

    @Override
    public int getInt(LootContext p_329755_) {
        return this.getNumericTag(p_329755_).map(NumericTag::getAsInt).orElse(0);
    }
}