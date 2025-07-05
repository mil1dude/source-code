package net.minecraft.client.resources;

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobEffectTextureManager extends TextureAtlasHolder {
    public MobEffectTextureManager(TextureManager p_118730_) {
        super(p_118730_, ResourceLocation.withDefaultNamespace("textures/atlas/mob_effects.png"), ResourceLocation.withDefaultNamespace("mob_effects"));
    }

    public TextureAtlasSprite get(Holder<MobEffect> p_332238_) {
        return this.getSprite(p_332238_.unwrapKey().map(ResourceKey::location).orElseGet(MissingTextureAtlasSprite::getLocation));
    }
}