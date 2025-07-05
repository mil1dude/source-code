package net.minecraft.world.level.block.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public abstract class ContainerOpenersCounter {
    private static final int CHECK_TICK_DELAY = 5;
    private int openCount;
    private double maxInteractionRange;

    protected abstract void onOpen(Level p_155460_, BlockPos p_155461_, BlockState p_155462_);

    protected abstract void onClose(Level p_155473_, BlockPos p_155474_, BlockState p_155475_);

    protected abstract void openerCountChanged(Level p_155463_, BlockPos p_155464_, BlockState p_155465_, int p_155466_, int p_155467_);

    protected abstract boolean isOwnContainer(Player p_155451_);

    public void incrementOpeners(Player p_155453_, Level p_155454_, BlockPos p_155455_, BlockState p_155456_) {
        int i = this.openCount++;
        if (i == 0) {
            this.onOpen(p_155454_, p_155455_, p_155456_);
            p_155454_.gameEvent(p_155453_, GameEvent.CONTAINER_OPEN, p_155455_);
            scheduleRecheck(p_155454_, p_155455_, p_155456_);
        }

        this.openerCountChanged(p_155454_, p_155455_, p_155456_, i, this.openCount);
        this.maxInteractionRange = Math.max(p_155453_.blockInteractionRange(), this.maxInteractionRange);
    }

    public void decrementOpeners(Player p_155469_, Level p_155470_, BlockPos p_155471_, BlockState p_155472_) {
        int i = this.openCount--;
        if (this.openCount == 0) {
            this.onClose(p_155470_, p_155471_, p_155472_);
            p_155470_.gameEvent(p_155469_, GameEvent.CONTAINER_CLOSE, p_155471_);
            this.maxInteractionRange = 0.0;
        }

        this.openerCountChanged(p_155470_, p_155471_, p_155472_, i, this.openCount);
    }

    private List<Player> getPlayersWithContainerOpen(Level p_333723_, BlockPos p_334135_) {
        double d0 = this.maxInteractionRange + 4.0;
        AABB aabb = new AABB(p_334135_).inflate(d0);
        return p_333723_.getEntities(EntityTypeTest.forClass(Player.class), aabb, this::isOwnContainer);
    }

    public void recheckOpeners(Level p_155477_, BlockPos p_155478_, BlockState p_155479_) {
        List<Player> list = this.getPlayersWithContainerOpen(p_155477_, p_155478_);
        this.maxInteractionRange = 0.0;

        for (Player player : list) {
            this.maxInteractionRange = Math.max(player.blockInteractionRange(), this.maxInteractionRange);
        }

        int i = list.size();
        int j = this.openCount;
        if (j != i) {
            boolean flag = i != 0;
            boolean flag1 = j != 0;
            if (flag && !flag1) {
                this.onOpen(p_155477_, p_155478_, p_155479_);
                p_155477_.gameEvent(null, GameEvent.CONTAINER_OPEN, p_155478_);
            } else if (!flag) {
                this.onClose(p_155477_, p_155478_, p_155479_);
                p_155477_.gameEvent(null, GameEvent.CONTAINER_CLOSE, p_155478_);
            }

            this.openCount = i;
        }

        this.openerCountChanged(p_155477_, p_155478_, p_155479_, j, i);
        if (i > 0) {
            scheduleRecheck(p_155477_, p_155478_, p_155479_);
        }
    }

    public int getOpenerCount() {
        return this.openCount;
    }

    private static void scheduleRecheck(Level p_155481_, BlockPos p_155482_, BlockState p_155483_) {
        p_155481_.scheduleTick(p_155482_, p_155483_.getBlock(), 5);
    }
}