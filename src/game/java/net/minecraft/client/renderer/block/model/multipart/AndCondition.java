package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Streams;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AndCondition implements Condition {
    public static final String TOKEN = "AND";
    private final Iterable<? extends Condition> conditions;

    public AndCondition(Iterable<? extends Condition> p_111910_) {
        this.conditions = p_111910_;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> p_111921_) {
        return Util.allOf(Streams.stream(this.conditions).map(p_111916_ -> p_111916_.getPredicate(p_111921_)).toList());
    }
}