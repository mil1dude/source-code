package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GoalSelectorDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST = 160;
    private final Minecraft minecraft;
    private final Int2ObjectMap<GoalSelectorDebugRenderer.EntityGoalInfo> goalSelectors = new Int2ObjectOpenHashMap<>();

    @Override
    public void clear() {
        this.goalSelectors.clear();
    }

    public void addGoalSelector(int p_113549_, BlockPos p_299054_, List<GoalDebugPayload.DebugGoal> p_113550_) {
        this.goalSelectors.put(p_113549_, new GoalSelectorDebugRenderer.EntityGoalInfo(p_299054_, p_113550_));
    }

    public void removeGoalSelector(int p_173889_) {
        this.goalSelectors.remove(p_173889_);
    }

    public GoalSelectorDebugRenderer(Minecraft p_113546_) {
        this.minecraft = p_113546_;
    }

    @Override
    public void render(PoseStack p_113552_, MultiBufferSource p_113553_, double p_113554_, double p_113555_, double p_113556_) {
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        BlockPos blockpos = BlockPos.containing(camera.getPosition().x, 0.0, camera.getPosition().z);

        for (GoalSelectorDebugRenderer.EntityGoalInfo goalselectordebugrenderer$entitygoalinfo : this.goalSelectors.values()) {
            BlockPos blockpos1 = goalselectordebugrenderer$entitygoalinfo.entityPos;
            if (blockpos.closerThan(blockpos1, 160.0)) {
                for (int i = 0; i < goalselectordebugrenderer$entitygoalinfo.goals.size(); i++) {
                    GoalDebugPayload.DebugGoal goaldebugpayload$debuggoal = goalselectordebugrenderer$entitygoalinfo.goals.get(i);
                    double d0 = (double)blockpos1.getX() + 0.5;
                    double d1 = (double)blockpos1.getY() + 2.0 + (double)i * 0.25;
                    double d2 = (double)blockpos1.getZ() + 0.5;
                    int j = goaldebugpayload$debuggoal.isRunning() ? -16711936 : -3355444;
                    DebugRenderer.renderFloatingText(p_113552_, p_113553_, goaldebugpayload$debuggoal.name(), d0, d1, d2, j);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record EntityGoalInfo(BlockPos entityPos, List<GoalDebugPayload.DebugGoal> goals) {
    }
}