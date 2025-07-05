package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DynamicLoot extends LootPoolSingletonContainer {
    public static final MapCodec<DynamicLoot> CODEC = RecordCodecBuilder.mapCodec(
        p_297024_ -> p_297024_.group(ResourceLocation.CODEC.fieldOf("name").forGetter(p_297018_ -> p_297018_.name))
                .and(singletonFields(p_297024_))
                .apply(p_297024_, DynamicLoot::new)
    );
    private final ResourceLocation name;

    private DynamicLoot(ResourceLocation p_79465_, int p_79466_, int p_79467_, List<LootItemCondition> p_297929_, List<LootItemFunction> p_299695_) {
        super(p_79466_, p_79467_, p_297929_, p_299695_);
        this.name = p_79465_;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.DYNAMIC;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> p_79481_, LootContext p_79482_) {
        p_79482_.addDynamicDrops(this.name, p_79481_);
    }

    public static LootPoolSingletonContainer.Builder<?> dynamicEntry(ResourceLocation p_79484_) {
        return simpleBuilder((p_297020_, p_297021_, p_297022_, p_297023_) -> new DynamicLoot(p_79484_, p_297020_, p_297021_, p_297022_, p_297023_));
    }
}