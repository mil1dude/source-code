package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconScreen extends AbstractContainerScreen<BeaconMenu> {
    private static final ResourceLocation BEACON_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/beacon.png");
    static final ResourceLocation BUTTON_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_disabled");
    static final ResourceLocation BUTTON_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_selected");
    static final ResourceLocation BUTTON_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button_highlighted");
    static final ResourceLocation BUTTON_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/button");
    static final ResourceLocation CONFIRM_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/confirm");
    static final ResourceLocation CANCEL_SPRITE = ResourceLocation.withDefaultNamespace("container/beacon/cancel");
    private static final Component PRIMARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.primary");
    private static final Component SECONDARY_EFFECT_LABEL = Component.translatable("block.minecraft.beacon.secondary");
    private final List<BeaconScreen.BeaconButton> beaconButtons = Lists.newArrayList();
    @Nullable
    Holder<MobEffect> primary;
    @Nullable
    Holder<MobEffect> secondary;

    public BeaconScreen(final BeaconMenu p_97912_, Inventory p_97913_, Component p_97914_) {
        super(p_97912_, p_97913_, p_97914_);
        this.imageWidth = 230;
        this.imageHeight = 219;
        p_97912_.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu p_97973_, int p_97974_, ItemStack p_97975_) {
            }

            @Override
            public void dataChanged(AbstractContainerMenu p_169628_, int p_169629_, int p_169630_) {
                BeaconScreen.this.primary = p_97912_.getPrimaryEffect();
                BeaconScreen.this.secondary = p_97912_.getSecondaryEffect();
            }
        });
    }

    private <T extends AbstractWidget & BeaconScreen.BeaconButton> void addBeaconButton(T p_169617_) {
        this.addRenderableWidget(p_169617_);
        this.beaconButtons.add(p_169617_);
    }

    @Override
    protected void init() {
        super.init();
        this.beaconButtons.clear();
        this.addBeaconButton(new BeaconScreen.BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
        this.addBeaconButton(new BeaconScreen.BeaconCancelButton(this.leftPos + 190, this.topPos + 107));

        for (int i = 0; i <= 2; i++) {
            int j = BeaconBlockEntity.BEACON_EFFECTS.get(i).size();
            int k = j * 22 + (j - 1) * 2;

            for (int l = 0; l < j; l++) {
                Holder<MobEffect> holder = BeaconBlockEntity.BEACON_EFFECTS.get(i).get(l);
                BeaconScreen.BeaconPowerButton beaconscreen$beaconpowerbutton = new BeaconScreen.BeaconPowerButton(
                    this.leftPos + 76 + l * 24 - k / 2, this.topPos + 22 + i * 25, holder, true, i
                );
                beaconscreen$beaconpowerbutton.active = false;
                this.addBeaconButton(beaconscreen$beaconpowerbutton);
            }
        }

        int i1 = 3;
        int j1 = BeaconBlockEntity.BEACON_EFFECTS.get(3).size() + 1;
        int k1 = j1 * 22 + (j1 - 1) * 2;

        for (int l1 = 0; l1 < j1 - 1; l1++) {
            Holder<MobEffect> holder2 = BeaconBlockEntity.BEACON_EFFECTS.get(3).get(l1);
            BeaconScreen.BeaconPowerButton beaconscreen$beaconpowerbutton2 = new BeaconScreen.BeaconPowerButton(
                this.leftPos + 167 + l1 * 24 - k1 / 2, this.topPos + 47, holder2, false, 3
            );
            beaconscreen$beaconpowerbutton2.active = false;
            this.addBeaconButton(beaconscreen$beaconpowerbutton2);
        }

        Holder<MobEffect> holder1 = BeaconBlockEntity.BEACON_EFFECTS.get(0).get(0);
        BeaconScreen.BeaconPowerButton beaconscreen$beaconpowerbutton1 = new BeaconScreen.BeaconUpgradePowerButton(
            this.leftPos + 167 + (j1 - 1) * 24 - k1 / 2, this.topPos + 47, holder1
        );
        beaconscreen$beaconpowerbutton1.visible = false;
        this.addBeaconButton(beaconscreen$beaconpowerbutton1);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updateButtons();
    }

    void updateButtons() {
        int i = this.menu.getLevels();
        this.beaconButtons.forEach(p_169615_ -> p_169615_.updateStatus(i));
    }

    @Override
    protected void renderLabels(GuiGraphics p_283369_, int p_282699_, int p_281296_) {
        p_283369_.drawCenteredString(this.font, PRIMARY_EFFECT_LABEL, 62, 10, 14737632);
        p_283369_.drawCenteredString(this.font, SECONDARY_EFFECT_LABEL, 169, 10, 14737632);
    }

    @Override
    protected void renderBg(GuiGraphics p_282454_, float p_282185_, int p_282362_, int p_282987_) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        p_282454_.blit(RenderType::guiTextured, BEACON_LOCATION, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        p_282454_.pose().pushPose();
        p_282454_.pose().translate(0.0F, 0.0F, 100.0F);
        p_282454_.renderItem(new ItemStack(Items.NETHERITE_INGOT), i + 20, j + 109);
        p_282454_.renderItem(new ItemStack(Items.EMERALD), i + 41, j + 109);
        p_282454_.renderItem(new ItemStack(Items.DIAMOND), i + 41 + 22, j + 109);
        p_282454_.renderItem(new ItemStack(Items.GOLD_INGOT), i + 42 + 44, j + 109);
        p_282454_.renderItem(new ItemStack(Items.IRON_INGOT), i + 42 + 66, j + 109);
        p_282454_.pose().popPose();
    }

    @Override
    public void render(GuiGraphics p_283062_, int p_282876_, int p_282015_, float p_281395_) {
        super.render(p_283062_, p_282876_, p_282015_, p_281395_);
        this.renderTooltip(p_283062_, p_282876_, p_282015_);
    }

    @OnlyIn(Dist.CLIENT)
    interface BeaconButton {
        void updateStatus(int p_169631_);
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconCancelButton extends BeaconScreen.BeaconSpriteScreenButton {
        public BeaconCancelButton(final int p_97982_, final int p_97983_) {
            super(p_97982_, p_97983_, BeaconScreen.CANCEL_SPRITE, CommonComponents.GUI_CANCEL);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int p_169636_) {
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconConfirmButton extends BeaconScreen.BeaconSpriteScreenButton {
        public BeaconConfirmButton(final int p_97992_, final int p_97993_) {
            super(p_97992_, p_97993_, BeaconScreen.CONFIRM_SPRITE, CommonComponents.GUI_DONE);
        }

        @Override
        public void onPress() {
            BeaconScreen.this.minecraft
                .getConnection()
                .send(new ServerboundSetBeaconPacket(Optional.ofNullable(BeaconScreen.this.primary), Optional.ofNullable(BeaconScreen.this.secondary)));
            BeaconScreen.this.minecraft.player.closeContainer();
        }

        @Override
        public void updateStatus(int p_169638_) {
            this.active = BeaconScreen.this.menu.hasPayment() && BeaconScreen.this.primary != null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconPowerButton extends BeaconScreen.BeaconScreenButton {
        private final boolean isPrimary;
        protected final int tier;
        private Holder<MobEffect> effect;
        private TextureAtlasSprite sprite;

        public BeaconPowerButton(final int p_169642_, final int p_169643_, final Holder<MobEffect> p_336384_, final boolean p_169645_, final int p_169646_) {
            super(p_169642_, p_169643_);
            this.isPrimary = p_169645_;
            this.tier = p_169646_;
            this.setEffect(p_336384_);
        }

        protected void setEffect(Holder<MobEffect> p_329569_) {
            this.effect = p_329569_;
            this.sprite = Minecraft.getInstance().getMobEffectTextures().get(p_329569_);
            this.setTooltip(Tooltip.create(this.createEffectDescription(p_329569_), null));
        }

        protected MutableComponent createEffectDescription(Holder<MobEffect> p_331976_) {
            return Component.translatable(p_331976_.value().getDescriptionId());
        }

        @Override
        public void onPress() {
            if (!this.isSelected()) {
                if (this.isPrimary) {
                    BeaconScreen.this.primary = this.effect;
                } else {
                    BeaconScreen.this.secondary = this.effect;
                }

                BeaconScreen.this.updateButtons();
            }
        }

        @Override
        protected void renderIcon(GuiGraphics p_282265_) {
            p_282265_.blitSprite(RenderType::guiTextured, this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);
        }

        @Override
        public void updateStatus(int p_169648_) {
            this.active = this.tier < p_169648_;
            this.setSelected(this.effect.equals(this.isPrimary ? BeaconScreen.this.primary : BeaconScreen.this.secondary));
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            return this.createEffectDescription(this.effect);
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class BeaconScreenButton extends AbstractButton implements BeaconScreen.BeaconButton {
        private boolean selected;

        protected BeaconScreenButton(int p_98022_, int p_98023_) {
            super(p_98022_, p_98023_, 22, 22, CommonComponents.EMPTY);
        }

        protected BeaconScreenButton(int p_169654_, int p_169655_, Component p_169656_) {
            super(p_169654_, p_169655_, 22, 22, p_169656_);
        }

        @Override
        public void renderWidget(GuiGraphics p_281837_, int p_281780_, int p_283603_, float p_283562_) {
            ResourceLocation resourcelocation;
            if (!this.active) {
                resourcelocation = BeaconScreen.BUTTON_DISABLED_SPRITE;
            } else if (this.selected) {
                resourcelocation = BeaconScreen.BUTTON_SELECTED_SPRITE;
            } else if (this.isHoveredOrFocused()) {
                resourcelocation = BeaconScreen.BUTTON_HIGHLIGHTED_SPRITE;
            } else {
                resourcelocation = BeaconScreen.BUTTON_SPRITE;
            }

            p_281837_.blitSprite(RenderType::guiTextured, resourcelocation, this.getX(), this.getY(), this.width, this.height);
            this.renderIcon(p_281837_);
        }

        protected abstract void renderIcon(GuiGraphics p_283292_);

        public boolean isSelected() {
            return this.selected;
        }

        public void setSelected(boolean p_98032_) {
            this.selected = p_98032_;
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput p_259705_) {
            this.defaultButtonNarrationText(p_259705_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract static class BeaconSpriteScreenButton extends BeaconScreen.BeaconScreenButton {
        private final ResourceLocation sprite;

        protected BeaconSpriteScreenButton(int p_169663_, int p_169664_, ResourceLocation p_299425_, Component p_169667_) {
            super(p_169663_, p_169664_, p_169667_);
            this.sprite = p_299425_;
        }

        @Override
        protected void renderIcon(GuiGraphics p_283624_) {
            p_283624_.blitSprite(RenderType::guiTextured, this.sprite, this.getX() + 2, this.getY() + 2, 18, 18);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BeaconUpgradePowerButton extends BeaconScreen.BeaconPowerButton {
        public BeaconUpgradePowerButton(final int p_169675_, final int p_169676_, final Holder<MobEffect> p_330320_) {
            super(p_169675_, p_169676_, p_330320_, false, 3);
        }

        @Override
        protected MutableComponent createEffectDescription(Holder<MobEffect> p_328605_) {
            return Component.translatable(p_328605_.value().getDescriptionId()).append(" II");
        }

        @Override
        public void updateStatus(int p_169679_) {
            if (BeaconScreen.this.primary != null) {
                this.visible = true;
                this.setEffect(BeaconScreen.this.primary);
                super.updateStatus(p_169679_);
            } else {
                this.visible = false;
            }
        }
    }
}