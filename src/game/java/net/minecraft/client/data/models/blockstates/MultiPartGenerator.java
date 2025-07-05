package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MultiPartGenerator implements BlockStateGenerator {
    private final Block block;
    private final List<MultiPartGenerator.Entry> parts = Lists.newArrayList();

    private MultiPartGenerator(Block p_376910_) {
        this.block = p_376910_;
    }

    @Override
    public Block getBlock() {
        return this.block;
    }

    public static MultiPartGenerator multiPart(Block p_376179_) {
        return new MultiPartGenerator(p_376179_);
    }

    public MultiPartGenerator with(List<Variant> p_378732_) {
        this.parts.add(new MultiPartGenerator.Entry(p_378732_));
        return this;
    }

    public MultiPartGenerator with(Variant p_378158_) {
        return this.with(ImmutableList.of(p_378158_));
    }

    public MultiPartGenerator with(Condition p_377379_, List<Variant> p_378775_) {
        this.parts.add(new MultiPartGenerator.ConditionalEntry(p_377379_, p_378775_));
        return this;
    }

    public MultiPartGenerator with(Condition p_375962_, Variant... p_375846_) {
        return this.with(p_375962_, ImmutableList.copyOf(p_375846_));
    }

    public MultiPartGenerator with(Condition p_375886_, Variant p_376034_) {
        return this.with(p_375886_, ImmutableList.of(p_376034_));
    }

    public JsonElement get() {
        StateDefinition<Block, BlockState> statedefinition = this.block.getStateDefinition();
        this.parts.forEach(p_376013_ -> p_376013_.validate(statedefinition));
        JsonArray jsonarray = new JsonArray();
        this.parts.stream().map(MultiPartGenerator.Entry::get).forEach(jsonarray::add);
        JsonObject jsonobject = new JsonObject();
        jsonobject.add("multipart", jsonarray);
        return jsonobject;
    }

    @OnlyIn(Dist.CLIENT)
    static class ConditionalEntry extends MultiPartGenerator.Entry {
        private final Condition condition;

        ConditionalEntry(Condition p_376079_, List<Variant> p_375924_) {
            super(p_375924_);
            this.condition = p_376079_;
        }

        @Override
        public void validate(StateDefinition<?, ?> p_376140_) {
            this.condition.validate(p_376140_);
        }

        @Override
        public void decorate(JsonObject p_376267_) {
            p_376267_.add("when", this.condition.get());
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Entry implements Supplier<JsonElement> {
        private final List<Variant> variants;

        Entry(List<Variant> p_375774_) {
            this.variants = p_375774_;
        }

        public void validate(StateDefinition<?, ?> p_377245_) {
        }

        public void decorate(JsonObject p_376672_) {
        }

        public JsonElement get() {
            JsonObject jsonobject = new JsonObject();
            this.decorate(jsonobject);
            jsonobject.add("apply", Variant.convertList(this.variants));
            return jsonobject;
        }
    }
}