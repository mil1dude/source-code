package net.minecraft.client.renderer.entity.state;

import javax.annotation.Nullable;
import net.minecraft.world.entity.Display;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextDisplayEntityRenderState extends DisplayEntityRenderState {
    @Nullable
    public Display.TextDisplay.TextRenderState textRenderState;
    @Nullable
    public Display.TextDisplay.CachedInfo cachedInfo;

    @Override
    public boolean hasSubState() {
        return this.textRenderState != null;
    }
}