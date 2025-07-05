package net.minecraft.client.resources;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DefaultPlayerSkin {
    private static final PlayerSkin[] DEFAULT_SKINS = new PlayerSkin[]{
        create("textures/entity/player/slim/alex.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/ari.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/efe.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/kai.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/makena.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/noor.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/steve.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/sunny.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/slim/zuri.png", PlayerSkin.Model.SLIM),
        create("textures/entity/player/wide/alex.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/ari.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/efe.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/kai.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/makena.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/noor.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/steve.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/sunny.png", PlayerSkin.Model.WIDE),
        create("textures/entity/player/wide/zuri.png", PlayerSkin.Model.WIDE)
    };

    public static ResourceLocation getDefaultTexture() {
        return getDefaultSkin().texture();
    }

    public static PlayerSkin getDefaultSkin() {
        return DEFAULT_SKINS[6];
    }

    public static PlayerSkin get(UUID p_297375_) {
        return DEFAULT_SKINS[Math.floorMod(p_297375_.hashCode(), DEFAULT_SKINS.length)];
    }

    public static PlayerSkin get(GameProfile p_298897_) {
        return get(p_298897_.getId());
    }

    private static PlayerSkin create(String p_298713_, PlayerSkin.Model p_297716_) {
        return new PlayerSkin(ResourceLocation.withDefaultNamespace(p_298713_), null, null, null, p_297716_, true);
    }
}