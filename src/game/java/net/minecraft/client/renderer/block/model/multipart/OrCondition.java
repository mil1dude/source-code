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
public class OrCondition implements Condition {
    public static final String TOKEN = "OR";
    private final Iterable<? extends Condition> conditions;

    public OrCondition(Iterable<? extends Condition> p_112003_) {
        this.conditions = p_112003_;
    }

    @Override
    public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> p_112014_) {
        return Util.anyOf(Streams.stream(this.conditions).map(p_112009_ -> p_112009_.getPredicate(p_112014_)).toList());
    }
}