package net.minecraft.client.gui.screens;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateFlatWorldScreen extends Screen {
    private static final Component TITLE = Component.translatable("createWorld.customize.flat.title");
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 64);
    protected final CreateWorldScreen parent;
    private final Consumer<FlatLevelGeneratorSettings> applySettings;
    FlatLevelGeneratorSettings generator;
    @Nullable
    private CreateFlatWorldScreen.DetailsList list;
    @Nullable
    private Button deleteLayerButton;

    public CreateFlatWorldScreen(CreateWorldScreen p_95822_, Consumer<FlatLevelGeneratorSettings> p_95823_, FlatLevelGeneratorSettings p_95824_) {
        super(TITLE);
        this.parent = p_95822_;
        this.applySettings = p_95823_;
        this.generator = p_95824_;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.generator;
    }

    public void setConfig(FlatLevelGeneratorSettings p_95826_) {
        this.generator = p_95826_;
        if (this.list != null) {
            this.list.resetRows();
            this.updateButtonValidity();
        }
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(this.title, this.font);
        this.list = this.layout.addToContents(new CreateFlatWorldScreen.DetailsList());
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.vertical().spacing(4));
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        LinearLayout linearlayout1 = linearlayout.addChild(LinearLayout.horizontal().spacing(8));
        LinearLayout linearlayout2 = linearlayout.addChild(LinearLayout.horizontal().spacing(8));
        this.deleteLayerButton = linearlayout1.addChild(Button.builder(Component.translatable("createWorld.customize.flat.removeLayer"), p_95845_ -> {
            if (this.hasValidSelection()) {
                List<FlatLayerInfo> list = this.generator.getLayersInfo();
                int i = this.list.children().indexOf(this.list.getSelected());
                int j = list.size() - i - 1;
                list.remove(j);
                this.list.setSelected(list.isEmpty() ? null : this.list.children().get(Math.min(i, list.size() - 1)));
                this.generator.updateLayers();
                this.list.resetRows();
                this.updateButtonValidity();
            }
        }).build());
        linearlayout1.addChild(Button.builder(Component.translatable("createWorld.customize.presets"), p_280790_ -> {
            this.minecraft.setScreen(new PresetFlatWorldScreen(this));
            this.generator.updateLayers();
            this.updateButtonValidity();
        }).build());
        linearlayout2.addChild(Button.builder(CommonComponents.GUI_DONE, p_374574_ -> {
            this.applySettings.accept(this.generator);
            this.onClose();
            this.generator.updateLayers();
        }).build());
        linearlayout2.addChild(Button.builder(CommonComponents.GUI_CANCEL, p_374573_ -> {
            this.onClose();
            this.generator.updateLayers();
        }).build());
        this.generator.updateLayers();
        this.updateButtonValidity();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.list != null) {
            this.list.updateSize(this.width, this.layout);
        }

        this.layout.arrangeElements();
    }

    void updateButtonValidity() {
        if (this.deleteLayerButton != null) {
            this.deleteLayerButton.active = this.hasValidSelection();
        }
    }

    private boolean hasValidSelection() {
        return this.list != null && this.list.getSelected() != null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @OnlyIn(Dist.CLIENT)
    class DetailsList extends ObjectSelectionList<CreateFlatWorldScreen.DetailsList.Entry> {
        private static final Component LAYER_MATERIAL_TITLE = Component.translatable("createWorld.customize.flat.tile").withStyle(ChatFormatting.UNDERLINE);
        private static final Component HEIGHT_TITLE = Component.translatable("createWorld.customize.flat.height").withStyle(ChatFormatting.UNDERLINE);

        public DetailsList() {
            super(CreateFlatWorldScreen.this.minecraft, CreateFlatWorldScreen.this.width, CreateFlatWorldScreen.this.height - 103, 43, 24, (int)(9.0 * 1.5));

            for (int i = 0; i < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); i++) {
                this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
            }
        }

        public void setSelected(@Nullable CreateFlatWorldScreen.DetailsList.Entry p_95855_) {
            super.setSelected(p_95855_);
            CreateFlatWorldScreen.this.updateButtonValidity();
        }

        public void resetRows() {
            int i = this.children().indexOf(this.getSelected());
            this.clearEntries();

            for (int j = 0; j < CreateFlatWorldScreen.this.generator.getLayersInfo().size(); j++) {
                this.addEntry(new CreateFlatWorldScreen.DetailsList.Entry());
            }

            List<CreateFlatWorldScreen.DetailsList.Entry> list = this.children();
            if (i >= 0 && i < list.size()) {
                this.setSelected(list.get(i));
            }
        }

        @Override
        protected void renderHeader(GuiGraphics p_378163_, int p_377446_, int p_378143_) {
            p_378163_.drawString(CreateFlatWorldScreen.this.font, LAYER_MATERIAL_TITLE, p_377446_, p_378143_, -1);
            p_378163_.drawString(
                CreateFlatWorldScreen.this.font,
                HEIGHT_TITLE,
                p_377446_ + this.getRowWidth() - CreateFlatWorldScreen.this.font.width(HEIGHT_TITLE) - 8,
                p_378143_,
                -1
            );
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ObjectSelectionList.Entry<CreateFlatWorldScreen.DetailsList.Entry> {
            @Override
            public void render(
                GuiGraphics p_281319_,
                int p_281943_,
                int p_283629_,
                int p_283315_,
                int p_282974_,
                int p_281870_,
                int p_283341_,
                int p_281639_,
                boolean p_282715_,
                float p_281937_
            ) {
                FlatLayerInfo flatlayerinfo = CreateFlatWorldScreen.this.generator
                    .getLayersInfo()
                    .get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - p_281943_ - 1);
                BlockState blockstate = flatlayerinfo.getBlockState();
                ItemStack itemstack = this.getDisplayItem(blockstate);
                this.blitSlot(p_281319_, p_283315_, p_283629_, itemstack);
                int i = p_283629_ + p_281870_ / 2 - 9 / 2;
                p_281319_.drawString(CreateFlatWorldScreen.this.font, itemstack.getHoverName(), p_283315_ + 18 + 5, i, -1);
                Component component;
                if (p_281943_ == 0) {
                    component = Component.translatable("createWorld.customize.flat.layer.top", flatlayerinfo.getHeight());
                } else if (p_281943_ == CreateFlatWorldScreen.this.generator.getLayersInfo().size() - 1) {
                    component = Component.translatable("createWorld.customize.flat.layer.bottom", flatlayerinfo.getHeight());
                } else {
                    component = Component.translatable("createWorld.customize.flat.layer", flatlayerinfo.getHeight());
                }

                p_281319_.drawString(
                    CreateFlatWorldScreen.this.font, component, p_283315_ + p_282974_ - CreateFlatWorldScreen.this.font.width(component) - 8, i, -1
                );
            }

            private ItemStack getDisplayItem(BlockState p_169294_) {
                Item item = p_169294_.getBlock().asItem();
                if (item == Items.AIR) {
                    if (p_169294_.is(Blocks.WATER)) {
                        item = Items.WATER_BUCKET;
                    } else if (p_169294_.is(Blocks.LAVA)) {
                        item = Items.LAVA_BUCKET;
                    }
                }

                return new ItemStack(item);
            }

            @Override
            public Component getNarration() {
                FlatLayerInfo flatlayerinfo = CreateFlatWorldScreen.this.generator
                    .getLayersInfo()
                    .get(CreateFlatWorldScreen.this.generator.getLayersInfo().size() - DetailsList.this.children().indexOf(this) - 1);
                ItemStack itemstack = this.getDisplayItem(flatlayerinfo.getBlockState());
                return (Component)(!itemstack.isEmpty() ? Component.translatable("narrator.select", itemstack.getHoverName()) : CommonComponents.EMPTY);
            }

            @Override
            public boolean mouseClicked(double p_95868_, double p_95869_, int p_95870_) {
                DetailsList.this.setSelected(this);
                return super.mouseClicked(p_95868_, p_95869_, p_95870_);
            }

            private void blitSlot(GuiGraphics p_281733_, int p_282373_, int p_282844_, ItemStack p_281263_) {
                this.blitSlotBg(p_281733_, p_282373_ + 1, p_282844_ + 1);
                if (!p_281263_.isEmpty()) {
                    p_281733_.renderFakeItem(p_281263_, p_282373_ + 2, p_282844_ + 2);
                }
            }

            private void blitSlotBg(GuiGraphics p_282271_, int p_281324_, int p_283171_) {
                p_282271_.blitSprite(RenderType::guiTextured, CreateFlatWorldScreen.SLOT_SPRITE, p_281324_, p_283171_, 18, 18);
            }
        }
    }
}