package com.mojang.realmsclient.util;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.Date;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsUtil {
    private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
    private static final int MINUTES = 60;
    private static final int HOURS = 3600;
    private static final int DAYS = 86400;

    public static Component convertToAgePresentation(long p_287679_) {
        if (p_287679_ < 0L) {
            return RIGHT_NOW;
        } else {
            long i = p_287679_ / 1000L;
            if (i < 60L) {
                return Component.translatable("mco.time.secondsAgo", i);
            } else if (i < 3600L) {
                long l = i / 60L;
                return Component.translatable("mco.time.minutesAgo", l);
            } else if (i < 86400L) {
                long k = i / 3600L;
                return Component.translatable("mco.time.hoursAgo", k);
            } else {
                long j = i / 86400L;
                return Component.translatable("mco.time.daysAgo", j);
            }
        }
    }

    public static Component convertToAgePresentationFromInstant(Date p_287698_) {
        return convertToAgePresentation(System.currentTimeMillis() - p_287698_.getTime());
    }

    public static void renderPlayerFace(GuiGraphics p_281255_, int p_281818_, int p_281791_, int p_282088_, UUID p_298294_) {
        Minecraft minecraft = Minecraft.getInstance();
        ProfileResult profileresult = minecraft.getMinecraftSessionService().fetchProfile(p_298294_, false);
        PlayerSkin playerskin = profileresult != null ? minecraft.getSkinManager().getInsecureSkin(profileresult.profile()) : DefaultPlayerSkin.get(p_298294_);
        PlayerFaceRenderer.draw(p_281255_, playerskin, p_281818_, p_281791_, p_282088_);
    }
}