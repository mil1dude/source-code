package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.util.task.LongRunningTask;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsLongRunningMcoTickTaskScreen extends RealmsLongRunningMcoTaskScreen {
    private final LongRunningTask task;

    public RealmsLongRunningMcoTickTaskScreen(Screen p_298922_, LongRunningTask p_298079_) {
        super(p_298922_, p_298079_);
        this.task = p_298079_;
    }

    @Override
    public void tick() {
        super.tick();
        this.task.tick();
    }

    @Override
    protected void cancel() {
        this.task.abortTask();
        super.cancel();
    }
}