package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;

public interface Portal {
    default int getPortalTransitionTime(ServerLevel p_345098_, Entity p_345452_) {
        return 0;
    }

    @Nullable
    TeleportTransition getPortalDestination(ServerLevel p_343424_, Entity p_344569_, BlockPos p_345365_);

    default Portal.Transition getLocalTransition() {
        return Portal.Transition.NONE;
    }

    public static enum Transition {
        CONFUSION,
        NONE;
    }
}