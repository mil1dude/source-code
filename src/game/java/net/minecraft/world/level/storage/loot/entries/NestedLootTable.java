package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class NestedLootTable extends LootPoolSingletonContainer {
    public static final MapCodec<NestedLootTable> CODEC = RecordCodecBuilder.mapCodec(
        p_331721_ -> p_331721_.group(
                    Codec.either(ResourceKey.codec(Registries.LOOT_TABLE), LootTable.DIRECT_CODEC).fieldOf("value").forGetter(p_331624_ -> p_331624_.contents)
                )
                .and(singletonFields(p_331721_))
                .apply(p_331721_, NestedLootTable::new)
    );
    private final Either<ResourceKey<LootTable>, LootTable> contents;

    private NestedLootTable(
        Either<ResourceKey<LootTable>, LootTable> p_335218_, int p_332597_, int p_330218_, List<LootItemCondition> p_335913_, List<LootItemFunction> p_331388_
    ) {
        super(p_332597_, p_330218_, p_335913_, p_331388_);
        this.contents = p_335218_;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.LOOT_TABLE;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> p_329435_, LootContext p_332786_) {
        this.contents
            .map(
                p_360669_ -> p_332786_.getResolver().get((ResourceKey<LootTable>)p_360669_).map(Holder::value).orElse(LootTable.EMPTY),
                p_328175_ -> (LootTable)p_328175_
            )
            .getRandomItemsRaw(p_332786_, p_329435_);
    }

    @Override
    public void validate(ValidationContext p_331194_) {
        Optional<ResourceKey<LootTable>> optional = this.contents.left();
        if (optional.isPresent()) {
            ResourceKey<LootTable> resourcekey = optional.get();
            if (!p_331194_.allowsReferences()) {
                p_331194_.reportProblem("Uses reference to " + resourcekey.location() + ", but references are not allowed");
                return;
            }

            if (p_331194_.hasVisitedElement(resourcekey)) {
                p_331194_.reportProblem("Table " + resourcekey.location() + " is recursively called");
                return;
            }
        }

        super.validate(p_331194_);
        this.contents
            .ifLeft(
                p_360667_ -> p_331194_.resolver()
                        .get((ResourceKey<LootTable>)p_360667_)
                        .ifPresentOrElse(
                            p_329102_ -> p_329102_.value().validate(p_331194_.enterElement("->{" + p_360667_.location() + "}", (ResourceKey<?>)p_360667_)),
                            () -> p_331194_.reportProblem("Unknown loot table called " + p_360667_.location())
                        )
            )
            .ifRight(p_333644_ -> p_333644_.validate(p_331194_.forChild("->{inline}")));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableReference(ResourceKey<LootTable> p_332425_) {
        return simpleBuilder((p_331287_, p_328654_, p_335079_, p_330542_) -> new NestedLootTable(Either.left(p_332425_), p_331287_, p_328654_, p_335079_, p_330542_));
    }

    public static LootPoolSingletonContainer.Builder<?> inlineLootTable(LootTable p_336216_) {
        return simpleBuilder(
            (p_327921_, p_332453_, p_332156_, p_328257_) -> new NestedLootTable(Either.right(p_336216_), p_327921_, p_332453_, p_332156_, p_328257_)
        );
    }
}