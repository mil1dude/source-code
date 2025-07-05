package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RecipeBookComponent<T extends RecipeBookMenu> implements Renderable, GuiEventListener, NarratableEntry {
    public static final WidgetSprites RECIPE_BUTTON_SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("recipe_book/button"), ResourceLocation.withDefaultNamespace("recipe_book/button_highlighted")
    );
    protected static final ResourceLocation RECIPE_BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    private static final Component SEARCH_HINT = Component.translatable("gui.recipebook.search_hint")
        .withStyle(ChatFormatting.ITALIC)
        .withStyle(ChatFormatting.GRAY);
    public static final int IMAGE_WIDTH = 147;
    public static final int IMAGE_HEIGHT = 166;
    private static final int OFFSET_X_POSITION = 86;
    private static final int BORDER_WIDTH = 8;
    private static final Component ALL_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.toggleRecipes.all");
    private static final int TICKS_TO_SWAP_SLOT = 30;
    private int xOffset;
    private int width;
    private int height;
    private float time;
    @Nullable
    private RecipeDisplayId lastPlacedRecipe;
    private final GhostSlots ghostSlots;
    private final List<RecipeBookTabButton> tabButtons = Lists.newArrayList();
    @Nullable
    private RecipeBookTabButton selectedTab;
    protected StateSwitchingButton filterButton;
    protected final T menu;
    protected Minecraft minecraft;
    @Nullable
    private EditBox searchBox;
    private String lastSearch = "";
    private final List<RecipeBookComponent.TabInfo> tabInfos;
    private ClientRecipeBook book;
    private final RecipeBookPage recipeBookPage;
    @Nullable
    private RecipeDisplayId lastRecipe;
    @Nullable
    private RecipeCollection lastRecipeCollection;
    private final StackedItemContents stackedContents = new StackedItemContents();
    private int timesInventoryChanged;
    private boolean ignoreTextInput;
    private boolean visible;
    private boolean widthTooNarrow;
    @Nullable
    private ScreenRectangle magnifierIconPlacement;

    public RecipeBookComponent(T p_365668_, List<RecipeBookComponent.TabInfo> p_366055_) {
        this.menu = p_365668_;
        this.tabInfos = p_366055_;
        SlotSelectTime slotselecttime = () -> Mth.floor(this.time / 30.0F);
        this.ghostSlots = new GhostSlots(slotselecttime);
        this.recipeBookPage = new RecipeBookPage(this, slotselecttime, p_365668_ instanceof AbstractFurnaceMenu);
    }

    public void init(int p_100310_, int p_100311_, Minecraft p_100312_, boolean p_100313_) {
        this.minecraft = p_100312_;
        this.width = p_100310_;
        this.height = p_100311_;
        this.widthTooNarrow = p_100313_;
        this.book = p_100312_.player.getRecipeBook();
        this.timesInventoryChanged = p_100312_.player.getInventory().getTimesChanged();
        this.visible = this.isVisibleAccordingToBookData();
        if (this.visible) {
            this.initVisuals();
        }
    }

    private void initVisuals() {
        boolean flag = this.isFiltering();
        this.xOffset = this.widthTooNarrow ? 0 : 86;
        int i = this.getXOrigin();
        int j = this.getYOrigin();
        this.stackedContents.clear();
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        String s = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.minecraft.font, i + 25, j + 13, 81, 9 + 5, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(16777215);
        this.searchBox.setValue(s);
        this.searchBox.setHint(SEARCH_HINT);
        this.magnifierIconPlacement = ScreenRectangle.of(
            ScreenAxis.HORIZONTAL, i + 8, this.searchBox.getY(), this.searchBox.getX() - this.getXOrigin(), this.searchBox.getHeight()
        );
        this.recipeBookPage.init(this.minecraft, i, j);
        this.filterButton = new StateSwitchingButton(i + 110, j + 12, 26, 16, flag);
        this.updateFilterButtonTooltip();
        this.initFilterButtonTextures();
        this.tabButtons.clear();

        for (RecipeBookComponent.TabInfo recipebookcomponent$tabinfo : this.tabInfos) {
            this.tabButtons.add(new RecipeBookTabButton(recipebookcomponent$tabinfo));
        }

        if (this.selectedTab != null) {
            this.selectedTab = this.tabButtons.stream().filter(p_357691_ -> p_357691_.getCategory().equals(this.selectedTab.getCategory())).findFirst().orElse(null);
        }

        if (this.selectedTab == null) {
            this.selectedTab = this.tabButtons.get(0);
        }

        this.selectedTab.setStateTriggered(true);
        this.selectMatchingRecipes();
        this.updateTabs(flag);
        this.updateCollections(false, flag);
    }

    private int getYOrigin() {
        return (this.height - 166) / 2;
    }

    private int getXOrigin() {
        return (this.width - 147) / 2 - this.xOffset;
    }

    private void updateFilterButtonTooltip() {
        this.filterButton.setTooltip(this.filterButton.isStateTriggered() ? Tooltip.create(this.getRecipeFilterName()) : Tooltip.create(ALL_RECIPES_TOOLTIP));
    }

    protected abstract void initFilterButtonTextures();

    public int updateScreenPosition(int p_181402_, int p_181403_) {
        int i;
        if (this.isVisible() && !this.widthTooNarrow) {
            i = 177 + (p_181402_ - p_181403_ - 200) / 2;
        } else {
            i = (p_181402_ - p_181403_) / 2;
        }

        return i;
    }

    public void toggleVisibility() {
        this.setVisible(!this.isVisible());
    }

    public boolean isVisible() {
        return this.visible;
    }

    private boolean isVisibleAccordingToBookData() {
        return this.book.isOpen(this.menu.getRecipeBookType());
    }

    protected void setVisible(boolean p_100370_) {
        if (p_100370_) {
            this.initVisuals();
        }

        this.visible = p_100370_;
        this.book.setOpen(this.menu.getRecipeBookType(), p_100370_);
        if (!p_100370_) {
            this.recipeBookPage.setInvisible();
        }

        this.sendUpdateSettings();
    }

    protected abstract boolean isCraftingSlot(Slot p_369976_);

    public void slotClicked(@Nullable Slot p_100315_) {
        if (p_100315_ != null && this.isCraftingSlot(p_100315_)) {
            this.lastPlacedRecipe = null;
            this.ghostSlots.clear();
            if (this.isVisible()) {
                this.updateStackedContents();
            }
        }
    }

    private void selectMatchingRecipes() {
        for (RecipeBookComponent.TabInfo recipebookcomponent$tabinfo : this.tabInfos) {
            for (RecipeCollection recipecollection : this.book.getCollection(recipebookcomponent$tabinfo.category())) {
                this.selectMatchingRecipes(recipecollection, this.stackedContents);
            }
        }
    }

    protected abstract void selectMatchingRecipes(RecipeCollection p_362260_, StackedItemContents p_368403_);

    private void updateCollections(boolean p_100383_, boolean p_363367_) {
        List<RecipeCollection> list = this.book.getCollection(this.selectedTab.getCategory());
        List<RecipeCollection> list1 = Lists.newArrayList(list);
        list1.removeIf(p_357690_ -> !p_357690_.hasAnySelected());
        String s = this.searchBox.getValue();
        if (!s.isEmpty()) {
            ClientPacketListener clientpacketlistener = this.minecraft.getConnection();
            if (clientpacketlistener != null) {
                ObjectSet<RecipeCollection> objectset = new ObjectLinkedOpenHashSet<>(
                    clientpacketlistener.searchTrees().recipes().search(s.toLowerCase(Locale.ROOT))
                );
                list1.removeIf(p_301525_ -> !objectset.contains(p_301525_));
            }
        }

        if (p_363367_) {
            list1.removeIf(p_100331_ -> !p_100331_.hasCraftable());
        }

        this.recipeBookPage.updateCollections(list1, p_100383_, p_363367_);
    }

    private void updateTabs(boolean p_361603_) {
        int i = (this.width - 147) / 2 - this.xOffset - 30;
        int j = (this.height - 166) / 2 + 3;
        int k = 27;
        int l = 0;

        for (RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
            ExtendedRecipeBookCategory extendedrecipebookcategory = recipebooktabbutton.getCategory();
            if (extendedrecipebookcategory instanceof SearchRecipeBookCategory) {
                recipebooktabbutton.visible = true;
                recipebooktabbutton.setPosition(i, j + 27 * l++);
            } else if (recipebooktabbutton.updateVisibility(this.book)) {
                recipebooktabbutton.setPosition(i, j + 27 * l++);
                recipebooktabbutton.startAnimation(this.book, p_361603_);
            }
        }
    }

    public void tick() {
        boolean flag = this.isVisibleAccordingToBookData();
        if (this.isVisible() != flag) {
            this.setVisible(flag);
        }

        if (this.isVisible()) {
            if (this.timesInventoryChanged != this.minecraft.player.getInventory().getTimesChanged()) {
                this.updateStackedContents();
                this.timesInventoryChanged = this.minecraft.player.getInventory().getTimesChanged();
            }
        }
    }

    private void updateStackedContents() {
        this.stackedContents.clear();
        this.minecraft.player.getInventory().fillStackedContents(this.stackedContents);
        this.menu.fillCraftSlotsStackedContents(this.stackedContents);
        this.selectMatchingRecipes();
        this.updateCollections(false, this.isFiltering());
    }

    private boolean isFiltering() {
        return this.book.isFiltering(this.menu.getRecipeBookType());
    }

    @Override
    public void render(GuiGraphics p_283597_, int p_282668_, int p_283506_, float p_282813_) {
        if (this.isVisible()) {
            if (!Screen.hasControlDown()) {
                this.time += p_282813_;
            }

            p_283597_.pose().pushPose();
            p_283597_.pose().translate(0.0F, 0.0F, 100.0F);
            int i = this.getXOrigin();
            int j = this.getYOrigin();
            p_283597_.blit(RenderType::guiTextured, RECIPE_BOOK_LOCATION, i, j, 1.0F, 1.0F, 147, 166, 256, 256);
            this.searchBox.render(p_283597_, p_282668_, p_283506_, p_282813_);

            for (RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                recipebooktabbutton.render(p_283597_, p_282668_, p_283506_, p_282813_);
            }

            this.filterButton.render(p_283597_, p_282668_, p_283506_, p_282813_);
            this.recipeBookPage.render(p_283597_, i, j, p_282668_, p_283506_, p_282813_);
            p_283597_.pose().popPose();
        }
    }

    public void renderTooltip(GuiGraphics p_281740_, int p_281520_, int p_282050_, @Nullable Slot p_369188_) {
        if (this.isVisible()) {
            this.recipeBookPage.renderTooltip(p_281740_, p_281520_, p_282050_);
            this.ghostSlots.renderTooltip(p_281740_, this.minecraft, p_281520_, p_282050_, p_369188_);
        }
    }

    protected abstract Component getRecipeFilterName();

    public void renderGhostRecipe(GuiGraphics p_283634_, boolean p_283495_) {
        this.ghostSlots.render(p_283634_, this.minecraft, p_283495_);
    }

    @Override
    public boolean mouseClicked(double p_100294_, double p_100295_, int p_100296_) {
        if (this.isVisible() && !this.minecraft.player.isSpectator()) {
            if (this.recipeBookPage.mouseClicked(p_100294_, p_100295_, p_100296_, this.getXOrigin(), this.getYOrigin(), 147, 166)) {
                RecipeDisplayId recipedisplayid = this.recipeBookPage.getLastClickedRecipe();
                RecipeCollection recipecollection = this.recipeBookPage.getLastClickedRecipeCollection();
                if (recipedisplayid != null && recipecollection != null) {
                    if (!this.tryPlaceRecipe(recipecollection, recipedisplayid)) {
                        return false;
                    }

                    this.lastRecipeCollection = recipecollection;
                    this.lastRecipe = recipedisplayid;
                    if (!this.isOffsetNextToMainGUI()) {
                        this.setVisible(false);
                    }
                }

                return true;
            } else {
                if (this.searchBox != null) {
                    boolean flag = this.magnifierIconPlacement != null && this.magnifierIconPlacement.containsPoint(Mth.floor(p_100294_), Mth.floor(p_100295_));
                    if (flag || this.searchBox.mouseClicked(p_100294_, p_100295_, p_100296_)) {
                        this.searchBox.setFocused(true);
                        return true;
                    }

                    this.searchBox.setFocused(false);
                }

                if (this.filterButton.mouseClicked(p_100294_, p_100295_, p_100296_)) {
                    boolean flag1 = this.toggleFiltering();
                    this.filterButton.setStateTriggered(flag1);
                    this.updateFilterButtonTooltip();
                    this.sendUpdateSettings();
                    this.updateCollections(false, flag1);
                    return true;
                } else {
                    for (RecipeBookTabButton recipebooktabbutton : this.tabButtons) {
                        if (recipebooktabbutton.mouseClicked(p_100294_, p_100295_, p_100296_)) {
                            if (this.selectedTab != recipebooktabbutton) {
                                if (this.selectedTab != null) {
                                    this.selectedTab.setStateTriggered(false);
                                }

                                this.selectedTab = recipebooktabbutton;
                                this.selectedTab.setStateTriggered(true);
                                this.updateCollections(true, this.isFiltering());
                            }

                            return true;
                        }
                    }

                    return false;
                }
            }
        } else {
            return false;
        }
    }

    private boolean tryPlaceRecipe(RecipeCollection p_369480_, RecipeDisplayId p_365779_) {
        if (!p_369480_.isCraftable(p_365779_) && p_365779_.equals(this.lastPlacedRecipe)) {
            return false;
        } else {
            this.lastPlacedRecipe = p_365779_;
            this.ghostSlots.clear();
            this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, p_365779_, Screen.hasShiftDown());
            return true;
        }
    }

    private boolean toggleFiltering() {
        RecipeBookType recipebooktype = this.menu.getRecipeBookType();
        boolean flag = !this.book.isFiltering(recipebooktype);
        this.book.setFiltering(recipebooktype, flag);
        return flag;
    }

    public boolean hasClickedOutside(double p_100298_, double p_100299_, int p_100300_, int p_100301_, int p_100302_, int p_100303_, int p_100304_) {
        if (!this.isVisible()) {
            return true;
        } else {
            boolean flag = p_100298_ < (double)p_100300_
                || p_100299_ < (double)p_100301_
                || p_100298_ >= (double)(p_100300_ + p_100302_)
                || p_100299_ >= (double)(p_100301_ + p_100303_);
            boolean flag1 = (double)(p_100300_ - 147) < p_100298_
                && p_100298_ < (double)p_100300_
                && (double)p_100301_ < p_100299_
                && p_100299_ < (double)(p_100301_ + p_100303_);
            return flag && !flag1 && !this.selectedTab.isHoveredOrFocused();
        }
    }

    @Override
    public boolean keyPressed(int p_100306_, int p_100307_, int p_100308_) {
        this.ignoreTextInput = false;
        if (!this.isVisible() || this.minecraft.player.isSpectator()) {
            return false;
        } else if (p_100306_ == 256 && !this.isOffsetNextToMainGUI()) {
            this.setVisible(false);
            return true;
        } else if (this.searchBox.keyPressed(p_100306_, p_100307_, p_100308_)) {
            this.checkSearchStringUpdate();
            return true;
        } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && p_100306_ != 256) {
            return true;
        } else if (this.minecraft.options.keyChat.matches(p_100306_, p_100307_) && !this.searchBox.isFocused()) {
            this.ignoreTextInput = true;
            this.searchBox.setFocused(true);
            return true;
        } else if (CommonInputs.selected(p_100306_) && this.lastRecipeCollection != null && this.lastRecipe != null) {
            AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
            return this.tryPlaceRecipe(this.lastRecipeCollection, this.lastRecipe);
        } else {
            return false;
        }
    }

    @Override
    public boolean keyReleased(int p_100356_, int p_100357_, int p_100358_) {
        this.ignoreTextInput = false;
        return GuiEventListener.super.keyReleased(p_100356_, p_100357_, p_100358_);
    }

    @Override
    public boolean charTyped(char p_100291_, int p_100292_) {
        if (this.ignoreTextInput) {
            return false;
        } else if (!this.isVisible() || this.minecraft.player.isSpectator()) {
            return false;
        } else if (this.searchBox.charTyped(p_100291_, p_100292_)) {
            this.checkSearchStringUpdate();
            return true;
        } else {
            return GuiEventListener.super.charTyped(p_100291_, p_100292_);
        }
    }

    @Override
    public boolean isMouseOver(double p_100353_, double p_100354_) {
        return false;
    }

    @Override
    public void setFocused(boolean p_265089_) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    private void checkSearchStringUpdate() {
        String s = this.searchBox.getValue().toLowerCase(Locale.ROOT);
        this.pirateSpeechForThePeople(s);
        if (!s.equals(this.lastSearch)) {
            this.updateCollections(false, this.isFiltering());
            this.lastSearch = s;
        }
    }

    private void pirateSpeechForThePeople(String p_100336_) {
        if ("excitedze".equals(p_100336_)) {
            LanguageManager languagemanager = this.minecraft.getLanguageManager();
            String s = "en_pt";
            LanguageInfo languageinfo = languagemanager.getLanguage("en_pt");
            if (languageinfo == null || languagemanager.getSelected().equals("en_pt")) {
                return;
            }

            languagemanager.setSelected("en_pt");
            this.minecraft.options.languageCode = "en_pt";
            this.minecraft.reloadResourcePacks();
            this.minecraft.options.save();
        }
    }

    private boolean isOffsetNextToMainGUI() {
        return this.xOffset == 86;
    }

    public void recipesUpdated() {
        this.selectMatchingRecipes();
        this.updateTabs(this.isFiltering());
        if (this.isVisible()) {
            this.updateCollections(false, this.isFiltering());
        }
    }

    public void recipeShown(RecipeDisplayId p_363874_) {
        this.minecraft.player.removeRecipeHighlight(p_363874_);
    }

    public void fillGhostRecipe(RecipeDisplay p_365238_) {
        this.ghostSlots.clear();
        ContextMap contextmap = SlotDisplayContext.fromLevel(Objects.requireNonNull(this.minecraft.level));
        this.fillGhostRecipe(this.ghostSlots, p_365238_, contextmap);
    }

    protected abstract void fillGhostRecipe(GhostSlots p_369382_, RecipeDisplay p_365034_, ContextMap p_367648_);

    protected void sendUpdateSettings() {
        if (this.minecraft.getConnection() != null) {
            RecipeBookType recipebooktype = this.menu.getRecipeBookType();
            boolean flag = this.book.getBookSettings().isOpen(recipebooktype);
            boolean flag1 = this.book.getBookSettings().isFiltering(recipebooktype);
            this.minecraft.getConnection().send(new ServerboundRecipeBookChangeSettingsPacket(recipebooktype, flag, flag1));
        }
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        return this.visible ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput p_170046_) {
        List<NarratableEntry> list = Lists.newArrayList();
        this.recipeBookPage.listButtons(p_170049_ -> {
            if (p_170049_.isActive()) {
                list.add(p_170049_);
            }
        });
        list.add(this.searchBox);
        list.add(this.filterButton);
        list.addAll(this.tabButtons);
        Screen.NarratableSearchResult screen$narratablesearchresult = Screen.findNarratableWidget(list, null);
        if (screen$narratablesearchresult != null) {
            screen$narratablesearchresult.entry.updateNarration(p_170046_.nest());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static record TabInfo(ItemStack primaryIcon, Optional<ItemStack> secondaryIcon, ExtendedRecipeBookCategory category) {
        public TabInfo(SearchRecipeBookCategory p_362286_) {
            this(new ItemStack(Items.COMPASS), Optional.empty(), p_362286_);
        }

        public TabInfo(Item p_367894_, RecipeBookCategory p_361028_) {
            this(new ItemStack(p_367894_), Optional.empty(), p_361028_);
        }

        public TabInfo(Item p_370216_, Item p_361025_, RecipeBookCategory p_366775_) {
            this(new ItemStack(p_370216_), Optional.of(new ItemStack(p_361025_)), p_366775_);
        }
    }
}