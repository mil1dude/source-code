package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EffectsInInventory {
    private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE = ResourceLocation.withDefaultNamespace("container/inventory/effect_background_small");
    private final AbstractContainerScreen<?> screen;
    private final Minecraft minecraft;

    public EffectsInInventory(AbstractContainerScreen<?> p_367800_) {
        this.screen = p_367800_;
        this.minecraft = Minecraft.getInstance();
    }

    public void render(GuiGraphics p_361248_, int p_363009_, int p_366978_, float p_361526_) {
        this.renderEffects(p_361248_, p_363009_, p_366978_);
    }

    public boolean canSeeEffects() {
        int i = this.screen.leftPos + this.screen.imageWidth + 2;
        int j = this.screen.width - i;
        return j >= 32;
    }

    private void renderEffects(GuiGraphics p_362146_, int p_370153_, int p_365612_) {
        int i = this.screen.leftPos + this.screen.imageWidth + 2;
        int j = this.screen.width - i;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty() && j >= 32) {
            boolean flag = j >= 120;
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = Ordering.natural().sortedCopy(collection);
            this.renderBackgrounds(p_362146_, i, k, iterable, flag);
            this.renderIcons(p_362146_, i, k, iterable, flag);
            if (flag) {
                this.renderLabels(p_362146_, i, k, iterable);
            } else if (p_370153_ >= i && p_370153_ <= i + 33) {
                int l = this.screen.topPos;
                MobEffectInstance mobeffectinstance = null;

                for (MobEffectInstance mobeffectinstance1 : iterable) {
                    if (p_365612_ >= l && p_365612_ <= l + k) {
                        mobeffectinstance = mobeffectinstance1;
                    }

                    l += k;
                }

                if (mobeffectinstance != null) {
                    List<Component> list = List.of(
                        this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate())
                    );
                    p_362146_.renderTooltip(this.screen.getFont(), list, Optional.empty(), p_370153_, p_365612_);
                }
            }
        }
    }

    private void renderBackgrounds(GuiGraphics p_363087_, int p_362702_, int p_362968_, Iterable<MobEffectInstance> p_366617_, boolean p_366522_) {
        int i = this.screen.topPos;

        for (MobEffectInstance mobeffectinstance : p_366617_) {
            if (p_366522_) {
                p_363087_.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_LARGE_SPRITE, p_362702_, i, 120, 32);
            } else {
                p_363087_.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_SMALL_SPRITE, p_362702_, i, 32, 32);
            }

            i += p_362968_;
        }
    }

    private void renderIcons(GuiGraphics p_367085_, int p_367644_, int p_367522_, Iterable<MobEffectInstance> p_361981_, boolean p_368681_) {
        MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
        int i = this.screen.topPos;

        for (MobEffectInstance mobeffectinstance : p_361981_) {
            Holder<MobEffect> holder = mobeffectinstance.getEffect();
            TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(holder);
            p_367085_.blitSprite(RenderType::guiTextured, textureatlassprite, p_367644_ + (p_368681_ ? 6 : 7), i + 7, 18, 18);
            i += p_367522_;
        }
    }

    private void renderLabels(GuiGraphics p_361851_, int p_367468_, int p_365556_, Iterable<MobEffectInstance> p_365480_) {
        int i = this.screen.topPos;

        for (MobEffectInstance mobeffectinstance : p_365480_) {
            Component component = this.getEffectName(mobeffectinstance);
            p_361851_.drawString(this.screen.getFont(), component, p_367468_ + 10 + 18, i + 6, 16777215);
            Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, this.minecraft.level.tickRateManager().tickrate());
            p_361851_.drawString(this.screen.getFont(), component1, p_367468_ + 10 + 18, i + 6 + 10, 8355711);
            i += p_365556_;
        }
    }

    private Component getEffectName(MobEffectInstance p_368169_) {
        MutableComponent mutablecomponent = p_368169_.getEffect().value().getDisplayName().copy();
        if (p_368169_.getAmplifier() >= 1 && p_368169_.getAmplifier() <= 9) {
            mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (p_368169_.getAmplifier() + 1)));
        }

        return mutablecomponent;
    }
}