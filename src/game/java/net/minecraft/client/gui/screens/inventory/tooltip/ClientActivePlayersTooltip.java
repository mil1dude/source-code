package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientActivePlayersTooltip implements ClientTooltipComponent {
    private static final int SKIN_SIZE = 10;
    private static final int PADDING = 2;
    private final List<ProfileResult> activePlayers;

    public ClientActivePlayersTooltip(ClientActivePlayersTooltip.ActivePlayersTooltip p_344514_) {
        this.activePlayers = p_344514_.profiles();
    }

    @Override
    public int getHeight(Font p_367830_) {
        return this.activePlayers.size() * 12 + 2;
    }

    @Override
    public int getWidth(Font p_345139_) {
        int i = 0;

        for (ProfileResult profileresult : this.activePlayers) {
            int j = p_345139_.width(profileresult.profile().getName());
            if (j > i) {
                i = j;
            }
        }

        return i + 10 + 6;
    }

    @Override
    public void renderImage(Font p_342274_, int p_345290_, int p_342557_, int p_361924_, int p_360967_, GuiGraphics p_345309_) {
        for (int i = 0; i < this.activePlayers.size(); i++) {
            ProfileResult profileresult = this.activePlayers.get(i);
            int j = p_342557_ + 2 + i * 12;
            PlayerFaceRenderer.draw(p_345309_, Minecraft.getInstance().getSkinManager().getInsecureSkin(profileresult.profile()), p_345290_ + 2, j, 10);
            p_345309_.drawString(p_342274_, profileresult.profile().getName(), p_345290_ + 10 + 4, j + 2, -1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record ActivePlayersTooltip(List<ProfileResult> profiles) implements TooltipComponent {
    }
}