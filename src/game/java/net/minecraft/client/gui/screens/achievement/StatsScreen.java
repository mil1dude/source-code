package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StatsScreen extends Screen {
    private static final Component TITLE = Component.translatable("gui.stats");
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    static final ResourceLocation HEADER_SPRITE = ResourceLocation.withDefaultNamespace("statistics/header");
    static final ResourceLocation SORT_UP_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_up");
    static final ResourceLocation SORT_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_down");
    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    static final Component NO_VALUE_DISPLAY = Component.translatable("stats.none");
    private static final Component GENERAL_BUTTON = Component.translatable("stat.generalButton");
    private static final Component ITEMS_BUTTON = Component.translatable("stat.itemsButton");
    private static final Component MOBS_BUTTON = Component.translatable("stat.mobsButton");
    protected final Screen lastScreen;
    private static final int LIST_WIDTH = 280;
    private static final int PADDING = 5;
    private static final int FOOTER_HEIGHT = 58;
    private HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 58);
    @Nullable
    private StatsScreen.GeneralStatisticsList statsList;
    @Nullable
    StatsScreen.ItemStatisticsList itemStatsList;
    @Nullable
    private StatsScreen.MobsStatisticsList mobsStatsList;
    final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    private boolean isLoading = true;

    public StatsScreen(Screen p_96906_, StatsCounter p_96907_) {
        super(TITLE);
        this.lastScreen = p_96906_;
        this.stats = p_96907_;
    }

    @Override
    protected void init() {
        this.layout.addToContents(new LoadingDotsWidget(this.font, PENDING_TEXT));
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public void initLists() {
        this.statsList = new StatsScreen.GeneralStatisticsList(this.minecraft);
        this.itemStatsList = new StatsScreen.ItemStatisticsList(this.minecraft);
        this.mobsStatsList = new StatsScreen.MobsStatisticsList(this.minecraft);
    }

    public void initButtons() {
        HeaderAndFooterLayout headerandfooterlayout = new HeaderAndFooterLayout(this, 33, 58);
        headerandfooterlayout.addTitleHeader(TITLE, this.font);
        LinearLayout linearlayout = headerandfooterlayout.addToFooter(LinearLayout.vertical()).spacing(5);
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        LinearLayout linearlayout1 = linearlayout.addChild(LinearLayout.horizontal()).spacing(5);
        linearlayout1.addChild(Button.builder(GENERAL_BUTTON, p_96963_ -> this.setActiveList(this.statsList)).width(120).build());
        Button button = linearlayout1.addChild(Button.builder(ITEMS_BUTTON, p_96959_ -> this.setActiveList(this.itemStatsList)).width(120).build());
        Button button1 = linearlayout1.addChild(Button.builder(MOBS_BUTTON, p_96949_ -> this.setActiveList(this.mobsStatsList)).width(120).build());
        linearlayout.addChild(Button.builder(CommonComponents.GUI_DONE, p_325372_ -> this.onClose()).width(200).build());
        if (this.itemStatsList != null && this.itemStatsList.children().isEmpty()) {
            button.active = false;
        }

        if (this.mobsStatsList != null && this.mobsStatsList.children().isEmpty()) {
            button1.active = false;
        }

        this.layout = headerandfooterlayout;
        this.layout.visitWidgets(p_325374_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_325374_);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.activeList != null) {
            this.activeList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    public void onStatsUpdated() {
        if (this.isLoading) {
            this.initLists();
            this.setActiveList(this.statsList);
            this.initButtons();
            this.setInitialFocus();
            this.isLoading = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return !this.isLoading;
    }

    public void setActiveList(@Nullable ObjectSelectionList<?> p_96925_) {
        if (this.activeList != null) {
            this.removeWidget(this.activeList);
        }

        if (p_96925_ != null) {
            this.addRenderableWidget(p_96925_);
            this.activeList = p_96925_;
            this.repositionElements();
        }
    }

    static String getTranslationKey(Stat<ResourceLocation> p_96947_) {
        return "stat." + p_96947_.getValue().toString().replace(':', '.');
    }

    @OnlyIn(Dist.CLIENT)
    class GeneralStatisticsList extends ObjectSelectionList<StatsScreen.GeneralStatisticsList.Entry> {
        public GeneralStatisticsList(final Minecraft p_96995_) {
            super(p_96995_, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 14);
            ObjectArrayList<Stat<ResourceLocation>> objectarraylist = new ObjectArrayList<>(Stats.CUSTOM.iterator());
            objectarraylist.sort(Comparator.comparing(p_96997_ -> I18n.get(StatsScreen.getTranslationKey((Stat<ResourceLocation>)p_96997_))));

            for (Stat<ResourceLocation> stat : objectarraylist) {
                this.addEntry(new StatsScreen.GeneralStatisticsList.Entry(stat));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @OnlyIn(Dist.CLIENT)
        class Entry extends ObjectSelectionList.Entry<StatsScreen.GeneralStatisticsList.Entry> {
            private final Stat<ResourceLocation> stat;
            private final Component statDisplay;

            Entry(final Stat<ResourceLocation> p_97005_) {
                this.stat = p_97005_;
                this.statDisplay = Component.translatable(StatsScreen.getTranslationKey(p_97005_));
            }

            private String getValueText() {
                return this.stat.format(StatsScreen.this.stats.getValue(this.stat));
            }

            @Override
            public void render(
                GuiGraphics p_283043_,
                int p_97012_,
                int p_97013_,
                int p_97014_,
                int p_97015_,
                int p_97016_,
                int p_97017_,
                int p_97018_,
                boolean p_97019_,
                float p_97020_
            ) {
                int i = p_97013_ + p_97016_ / 2 - 9 / 2;
                int j = p_97012_ % 2 == 0 ? -1 : -4539718;
                p_283043_.drawString(StatsScreen.this.font, this.statDisplay, p_97014_ + 2, i, j);
                String s = this.getValueText();
                p_283043_.drawString(StatsScreen.this.font, s, p_97014_ + p_97015_ - StatsScreen.this.font.width(s) - 4, i, j);
            }

            @Override
            public Component getNarration() {
                return Component.translatable(
                    "narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText())
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ItemStatisticsList extends ObjectSelectionList<StatsScreen.ItemStatisticsList.ItemRow> {
        private static final int SLOT_BG_SIZE = 18;
        private static final int SLOT_STAT_HEIGHT = 22;
        private static final int SLOT_BG_Y = 1;
        private static final int SORT_NONE = 0;
        private static final int SORT_DOWN = -1;
        private static final int SORT_UP = 1;
        private final ResourceLocation[] iconSprites = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("statistics/block_mined"),
            ResourceLocation.withDefaultNamespace("statistics/item_broken"),
            ResourceLocation.withDefaultNamespace("statistics/item_crafted"),
            ResourceLocation.withDefaultNamespace("statistics/item_used"),
            ResourceLocation.withDefaultNamespace("statistics/item_picked_up"),
            ResourceLocation.withDefaultNamespace("statistics/item_dropped")
        };
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        protected final Comparator<StatsScreen.ItemStatisticsList.ItemRow> itemStatSorter = new StatsScreen.ItemStatisticsList.ItemRowComparator();
        @Nullable
        protected StatType<?> sortColumn;
        protected int headerPressed = -1;
        protected int sortOrder;

        public ItemStatisticsList(final Minecraft p_97032_) {
            super(p_97032_, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 22, 22);
            this.blockColumns = Lists.newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList(Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED);
            Set<Item> set = Sets.newIdentityHashSet();

            for (Item item : BuiltInRegistries.ITEM) {
                boolean flag = false;

                for (StatType<Item> stattype : this.itemColumns) {
                    if (stattype.contains(item) && StatsScreen.this.stats.getValue(stattype.get(item)) > 0) {
                        flag = true;
                    }
                }

                if (flag) {
                    set.add(item);
                }
            }

            for (Block block : BuiltInRegistries.BLOCK) {
                boolean flag1 = false;

                for (StatType<Block> stattype1 : this.blockColumns) {
                    if (stattype1.contains(block) && StatsScreen.this.stats.getValue(stattype1.get(block)) > 0) {
                        flag1 = true;
                    }
                }

                if (flag1) {
                    set.add(block.asItem());
                }
            }

            set.remove(Items.AIR);

            for (Item item1 : set) {
                this.addEntry(new StatsScreen.ItemStatisticsList.ItemRow(item1));
            }
        }

        int getColumnX(int p_329609_) {
            return 75 + 40 * p_329609_;
        }

        @Override
        protected void renderHeader(GuiGraphics p_282214_, int p_97050_, int p_97051_) {
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }

            for (int i = 0; i < this.iconSprites.length; i++) {
                ResourceLocation resourcelocation = this.headerPressed == i ? StatsScreen.SLOT_SPRITE : StatsScreen.HEADER_SPRITE;
                p_282214_.blitSprite(RenderType::guiTextured, resourcelocation, p_97050_ + this.getColumnX(i) - 18, p_97051_ + 1, 18, 18);
            }

            if (this.sortColumn != null) {
                int j = this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                ResourceLocation resourcelocation1 = this.sortOrder == 1 ? StatsScreen.SORT_UP_SPRITE : StatsScreen.SORT_DOWN_SPRITE;
                p_282214_.blitSprite(RenderType::guiTextured, resourcelocation1, p_97050_ + j, p_97051_ + 1, 18, 18);
            }

            for (int k = 0; k < this.iconSprites.length; k++) {
                int l = this.headerPressed == k ? 1 : 0;
                p_282214_.blitSprite(RenderType::guiTextured, this.iconSprites[k], p_97050_ + this.getColumnX(k) - 18 + l, p_97051_ + 1 + l, 18, 18);
            }
        }

        @Override
        public boolean mouseClicked(double p_378023_, double p_375495_, int p_376505_) {
            boolean flag = super.mouseClicked(p_378023_, p_375495_, p_376505_);
            return !flag
                    && this.clickedHeader(
                        (int)(p_378023_ - ((double)this.getX() + (double)this.width / 2.0 - (double)this.getRowWidth() / 2.0)),
                        (int)(p_375495_ - (double)this.getY()) + (int)this.scrollAmount() - 4
                    )
                ? true
                : flag;
        }

        protected boolean clickedHeader(int p_97036_, int p_97037_) {
            this.headerPressed = -1;

            for (int i = 0; i < this.iconSprites.length; i++) {
                int j = p_97036_ - this.getColumnX(i);
                if (j >= -36 && j <= 0) {
                    this.headerPressed = i;
                    break;
                }
            }

            if (this.headerPressed >= 0) {
                this.sortByColumn(this.getColumn(this.headerPressed));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        private StatType<?> getColumn(int p_97034_) {
            return p_97034_ < this.blockColumns.size() ? this.blockColumns.get(p_97034_) : this.itemColumns.get(p_97034_ - this.blockColumns.size());
        }

        private int getColumnIndex(StatType<?> p_97059_) {
            int i = this.blockColumns.indexOf(p_97059_);
            if (i >= 0) {
                return i;
            } else {
                int j = this.itemColumns.indexOf(p_97059_);
                return j >= 0 ? j + this.blockColumns.size() : -1;
            }
        }

        @Override
        protected void renderDecorations(GuiGraphics p_283203_, int p_97046_, int p_97047_) {
            if (p_97047_ >= this.getY() && p_97047_ <= this.getBottom()) {
                StatsScreen.ItemStatisticsList.ItemRow statsscreen$itemstatisticslist$itemrow = this.getHovered();
                int i = this.getRowLeft();
                if (statsscreen$itemstatisticslist$itemrow != null) {
                    if (p_97046_ < i || p_97046_ > i + 18) {
                        return;
                    }

                    Item item = statsscreen$itemstatisticslist$itemrow.getItem();
                    p_283203_.renderTooltip(StatsScreen.this.font, item.getName(), p_97046_, p_97047_, item.components().get(DataComponents.TOOLTIP_STYLE));
                } else {
                    Component component = null;
                    int j = p_97046_ - i;

                    for (int k = 0; k < this.iconSprites.length; k++) {
                        int l = this.getColumnX(k);
                        if (j >= l - 18 && j <= l) {
                            component = this.getColumn(k).getDisplayName();
                            break;
                        }
                    }

                    if (component != null) {
                        p_283203_.renderTooltip(StatsScreen.this.font, component, p_97046_, p_97047_);
                    }
                }
            }
        }

        protected void sortByColumn(StatType<?> p_97039_) {
            if (p_97039_ != this.sortColumn) {
                this.sortColumn = p_97039_;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }

            this.children().sort(this.itemStatSorter);
        }

        @OnlyIn(Dist.CLIENT)
        class ItemRow extends ObjectSelectionList.Entry<StatsScreen.ItemStatisticsList.ItemRow> {
            private final Item item;

            ItemRow(final Item p_169517_) {
                this.item = p_169517_;
            }

            public Item getItem() {
                return this.item;
            }

            @Override
            public void render(
                GuiGraphics p_283614_,
                int p_97082_,
                int p_97083_,
                int p_97084_,
                int p_97085_,
                int p_97086_,
                int p_97087_,
                int p_97088_,
                boolean p_97089_,
                float p_97090_
            ) {
                p_283614_.blitSprite(RenderType::guiTextured, StatsScreen.SLOT_SPRITE, p_97084_, p_97083_, 18, 18);
                p_283614_.renderFakeItem(this.item.getDefaultInstance(), p_97084_ + 1, p_97083_ + 1);
                if (StatsScreen.this.itemStatsList != null) {
                    for (int i = 0; i < StatsScreen.this.itemStatsList.blockColumns.size(); i++) {
                        Stat<Block> stat;
                        if (this.item instanceof BlockItem blockitem) {
                            stat = StatsScreen.this.itemStatsList.blockColumns.get(i).get(blockitem.getBlock());
                        } else {
                            stat = null;
                        }

                        this.renderStat(p_283614_, stat, p_97084_ + ItemStatisticsList.this.getColumnX(i), p_97083_ + p_97086_ / 2 - 9 / 2, p_97082_ % 2 == 0);
                    }

                    for (int j = 0; j < StatsScreen.this.itemStatsList.itemColumns.size(); j++) {
                        this.renderStat(
                            p_283614_,
                            StatsScreen.this.itemStatsList.itemColumns.get(j).get(this.item),
                            p_97084_ + ItemStatisticsList.this.getColumnX(j + StatsScreen.this.itemStatsList.blockColumns.size()),
                            p_97083_ + p_97086_ / 2 - 9 / 2,
                            p_97082_ % 2 == 0
                        );
                    }
                }
            }

            protected void renderStat(GuiGraphics p_282544_, @Nullable Stat<?> p_97093_, int p_97094_, int p_97095_, boolean p_97096_) {
                Component component = (Component)(p_97093_ == null
                    ? StatsScreen.NO_VALUE_DISPLAY
                    : Component.literal(p_97093_.format(StatsScreen.this.stats.getValue(p_97093_))));
                p_282544_.drawString(
                    StatsScreen.this.font, component, p_97094_ - StatsScreen.this.font.width(component), p_97095_, p_97096_ ? -1 : -4539718
                );
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.item.getName());
            }
        }

        @OnlyIn(Dist.CLIENT)
        class ItemRowComparator implements Comparator<StatsScreen.ItemStatisticsList.ItemRow> {
            public int compare(StatsScreen.ItemStatisticsList.ItemRow p_169524_, StatsScreen.ItemStatisticsList.ItemRow p_169525_) {
                Item item = p_169524_.getItem();
                Item item1 = p_169525_.getItem();
                int i;
                int j;
                if (ItemStatisticsList.this.sortColumn == null) {
                    i = 0;
                    j = 0;
                } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                    StatType<Block> stattype = (StatType<Block>)ItemStatisticsList.this.sortColumn;
                    i = item instanceof BlockItem ? StatsScreen.this.stats.getValue(stattype, ((BlockItem)item).getBlock()) : -1;
                    j = item1 instanceof BlockItem ? StatsScreen.this.stats.getValue(stattype, ((BlockItem)item1).getBlock()) : -1;
                } else {
                    StatType<Item> stattype1 = (StatType<Item>)ItemStatisticsList.this.sortColumn;
                    i = StatsScreen.this.stats.getValue(stattype1, item);
                    j = StatsScreen.this.stats.getValue(stattype1, item1);
                }

                return i == j
                    ? ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item1))
                    : ItemStatisticsList.this.sortOrder * Integer.compare(i, j);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    class MobsStatisticsList extends ObjectSelectionList<StatsScreen.MobsStatisticsList.MobRow> {
        public MobsStatisticsList(final Minecraft p_97100_) {
            super(p_97100_, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 9 * 4);

            for (EntityType<?> entitytype : BuiltInRegistries.ENTITY_TYPE) {
                if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entitytype)) > 0
                    || StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entitytype)) > 0) {
                    this.addEntry(new StatsScreen.MobsStatisticsList.MobRow(entitytype));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        @OnlyIn(Dist.CLIENT)
        class MobRow extends ObjectSelectionList.Entry<StatsScreen.MobsStatisticsList.MobRow> {
            private final Component mobName;
            private final Component kills;
            private final Component killedBy;
            private final boolean hasKills;
            private final boolean wasKilledBy;

            public MobRow(final EntityType<?> p_97112_) {
                this.mobName = p_97112_.getDescription();
                int i = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(p_97112_));
                if (i == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", i, this.mobName);
                    this.hasKills = true;
                }

                int j = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(p_97112_));
                if (j == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, j);
                    this.wasKilledBy = true;
                }
            }

            @Override
            public void render(
                GuiGraphics p_283265_,
                int p_97115_,
                int p_97116_,
                int p_97117_,
                int p_97118_,
                int p_97119_,
                int p_97120_,
                int p_97121_,
                boolean p_97122_,
                float p_97123_
            ) {
                p_283265_.drawString(StatsScreen.this.font, this.mobName, p_97117_ + 2, p_97116_ + 1, -1);
                p_283265_.drawString(StatsScreen.this.font, this.kills, p_97117_ + 2 + 10, p_97116_ + 1 + 9, this.hasKills ? -4539718 : -8355712);
                p_283265_.drawString(StatsScreen.this.font, this.killedBy, p_97117_ + 2 + 10, p_97116_ + 1 + 9 * 2, this.wasKilledBy ? -4539718 : -8355712);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }
        }
    }
}