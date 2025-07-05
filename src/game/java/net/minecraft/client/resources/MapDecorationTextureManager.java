package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MapDecorationTextureManager extends TextureAtlasHolder {
    public MapDecorationTextureManager(TextureManager p_331993_) {
        super(p_331993_, ResourceLocation.withDefaultNamespace("textures/atlas/map_decorations.png"), ResourceLocation.withDefaultNamespace("map_decorations"));
    }

    public TextureAtlasSprite get(MapDecoration p_330308_) {
        return this.getSprite(p_330308_.getSpriteLocation());
    }
}