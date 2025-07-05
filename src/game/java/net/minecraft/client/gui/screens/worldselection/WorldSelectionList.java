package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.Entry> {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    static final ResourceLocation ERROR_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/error_highlighted");
    static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("world_list/error");
    static final ResourceLocation MARKED_JOIN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/marked_join_highlighted");
    static final ResourceLocation MARKED_JOIN_SPRITE = ResourceLocation.withDefaultNamespace("world_list/marked_join");
    static final ResourceLocation WARNING_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/warning_highlighted");
    static final ResourceLocation WARNING_SPRITE = ResourceLocation.withDefaultNamespace("world_list/warning");
    static final ResourceLocation JOIN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/join_highlighted");
    static final ResourceLocation JOIN_SPRITE = ResourceLocation.withDefaultNamespace("world_list/join");
    static final Logger LOGGER = LogUtils.getLogger();
    static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
    static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
    static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
    static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
    static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
    static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
    static final Component INCOMPATIBLE_VERSION_TOOLTIP = Component.translatable("selectWorld.incompatible.tooltip").withStyle(ChatFormatting.RED);
    static final Component WORLD_EXPERIMENTAL = Component.translatable("selectWorld.experimental");
    private final SelectWorldScreen screen;
    private CompletableFuture<List<LevelSummary>> pendingLevels;
    @Nullable
    private List<LevelSummary> currentlyDisplayedLevels;
    private String filter;
    private final WorldSelectionList.LoadingHeader loadingHeader;

    public WorldSelectionList(
        SelectWorldScreen p_239540_,
        Minecraft p_239541_,
        int p_239542_,
        int p_239543_,
        int p_239544_,
        int p_239545_,
        String p_239547_,
        @Nullable WorldSelectionList p_239548_
    ) {
        super(p_239541_, p_239542_, p_239543_, p_239544_, p_239545_);
        this.screen = p_239540_;
        this.loadingHeader = new WorldSelectionList.LoadingHeader(p_239541_);
        this.filter = p_239547_;
        if (p_239548_ != null) {
            this.pendingLevels = p_239548_.pendingLevels;
        } else {
            this.pendingLevels = this.loadLevels();
        }

        this.handleNewLevels(this.pollLevelsIgnoreErrors());
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(WorldSelectionList.Entry::close);
        super.clearEntries();
    }

    @Nullable
    private List<LevelSummary> pollLevelsIgnoreErrors() {
        try {
            return this.pendingLevels.getNow(null);
        } catch (CancellationException | CompletionException completionexception) {
            return null;
        }
    }

    void reloadWorldList() {
        this.pendingLevels = this.loadLevels();
    }

    @Override
    public boolean keyPressed(int p_289017_, int p_288966_, int p_289020_) {
        if (CommonInputs.selected(p_289017_)) {
            Optional<WorldSelectionList.WorldListEntry> optional = this.getSelectedOpt();
            if (optional.isPresent()) {
                if (optional.get().canJoin()) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    optional.get().joinWorld();
                }

                return true;
            }
        }

        return super.keyPressed(p_289017_, p_288966_, p_289020_);
    }

    @Override
    public void renderWidget(GuiGraphics p_310403_, int p_312182_, int p_312151_, float p_311062_) {
        List<LevelSummary> list = this.pollLevelsIgnoreErrors();
        if (list != this.currentlyDisplayedLevels) {
            this.handleNewLevels(list);
        }

        super.renderWidget(p_310403_, p_312182_, p_312151_, p_311062_);
    }

    private void handleNewLevels(@Nullable List<LevelSummary> p_239665_) {
        if (p_239665_ == null) {
            this.fillLoadingLevels();
        } else {
            this.fillLevels(this.filter, p_239665_);
        }

        this.currentlyDisplayedLevels = p_239665_;
    }

    public void updateFilter(String p_239901_) {
        if (this.currentlyDisplayedLevels != null && !p_239901_.equals(this.filter)) {
            this.fillLevels(p_239901_, this.currentlyDisplayedLevels);
        }

        this.filter = p_239901_;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorageSource.LevelCandidates levelstoragesource$levelcandidates;
        try {
            levelstoragesource$levelcandidates = this.minecraft.getLevelSource().findLevelCandidates();
        } catch (LevelStorageException levelstorageexception) {
            LOGGER.error("Couldn't load level list", (Throwable)levelstorageexception);
            this.handleLevelLoadFailure(levelstorageexception.getMessageComponent());
            return CompletableFuture.completedFuture(List.of());
        }

        if (levelstoragesource$levelcandidates.isEmpty()) {
            CreateWorldScreen.openFresh(this.minecraft, null);
            return CompletableFuture.completedFuture(List.of());
        } else {
            return this.minecraft.getLevelSource().loadLevelSummaries(levelstoragesource$levelcandidates).exceptionally(p_233202_ -> {
                this.minecraft.delayCrash(CrashReport.forThrowable(p_233202_, "Couldn't load level list"));
                return List.of();
            });
        }
    }

    private void fillLevels(String p_233199_, List<LevelSummary> p_233200_) {
        this.clearEntries();
        p_233199_ = p_233199_.toLowerCase(Locale.ROOT);

        for (LevelSummary levelsummary : p_233200_) {
            if (this.filterAccepts(p_233199_, levelsummary)) {
                this.addEntry(new WorldSelectionList.WorldListEntry(this, levelsummary));
            }
        }

        this.notifyListUpdated();
    }

    private boolean filterAccepts(String p_233196_, LevelSummary p_233197_) {
        return p_233197_.getLevelName().toLowerCase(Locale.ROOT).contains(p_233196_) || p_233197_.getLevelId().toLowerCase(Locale.ROOT).contains(p_233196_);
    }

    private void fillLoadingLevels() {
        this.clearEntries();
        this.addEntry(this.loadingHeader);
        this.notifyListUpdated();
    }

    private void notifyListUpdated() {
        this.refreshScrollAmount();
        this.screen.triggerImmediateNarration(true);
    }

    private void handleLevelLoadFailure(Component p_233212_) {
        this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), p_233212_));
    }

    @Override
    public int getRowWidth() {
        return 270;
    }

    public void setSelected(@Nullable WorldSelectionList.Entry p_233190_) {
        super.setSelected(p_233190_);
        this.screen
            .updateButtonStatus(
                p_233190_ instanceof WorldSelectionList.WorldListEntry worldselectionlist$worldlistentry ? worldselectionlist$worldlistentry.summary : null
            );
    }

    public Optional<WorldSelectionList.WorldListEntry> getSelectedOpt() {
        WorldSelectionList.Entry worldselectionlist$entry = this.getSelected();
        return worldselectionlist$entry instanceof WorldSelectionList.WorldListEntry worldselectionlist$worldlistentry
            ? Optional.of(worldselectionlist$worldlistentry)
            : Optional.empty();
    }

    public SelectWorldScreen getScreen() {
        return this.screen;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput p_313204_) {
        if (this.children().contains(this.loadingHeader)) {
            this.loadingHeader.updateNarration(p_313204_);
        } else {
            super.updateWidgetNarration(p_313204_);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ObjectSelectionList.Entry<WorldSelectionList.Entry> implements AutoCloseable {
        @Override
        public void close() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LoadingHeader extends WorldSelectionList.Entry {
        private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
        private final Minecraft minecraft;

        public LoadingHeader(Minecraft p_233222_) {
            this.minecraft = p_233222_;
        }

        @Override
        public void render(
            GuiGraphics p_282319_,
            int p_283207_,
            int p_281352_,
            int p_283332_,
            int p_282400_,
            int p_282912_,
            int p_282760_,
            int p_281344_,
            boolean p_283655_,
            float p_283696_
        ) {
            int i = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
            int j = p_281352_ + (p_282912_ - 9) / 2;
            p_282319_.drawString(this.minecraft.font, LOADING_LABEL, i, j, -1);
            String s = LoadingDotsText.get(Util.getMillis());
            int k = (this.minecraft.screen.width - this.minecraft.font.width(s)) / 2;
            int l = j + 9;
            p_282319_.drawString(this.minecraft.font, s, k, l, -8355712);
        }

        @Override
        public Component getNarration() {
            return LOADING_LABEL;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public final class WorldListEntry extends WorldSelectionList.Entry {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private final Minecraft minecraft;
        private final SelectWorldScreen screen;
        final LevelSummary summary;
        private final FaviconTexture icon;
        @Nullable
        private Path iconFile;
        private long lastClickTime;

        public WorldListEntry(final WorldSelectionList p_101702_, final LevelSummary p_101703_) {
            this.minecraft = p_101702_.minecraft;
            this.screen = p_101702_.getScreen();
            this.summary = p_101703_;
            this.icon = FaviconTexture.forWorld(this.minecraft.getTextureManager(), p_101703_.getLevelId());
            this.iconFile = p_101703_.getIcon();
            this.validateIconFile();
            this.loadIcon();
        }

        private void validateIconFile() {
            if (this.iconFile != null) {
                try {
                    BasicFileAttributes basicfileattributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    if (basicfileattributes.isSymbolicLink()) {
                        List<ForbiddenSymlinkInfo> list = this.minecraft.directoryValidator().validateSymlink(this.iconFile);
                        if (!list.isEmpty()) {
                            WorldSelectionList.LOGGER.warn("{}", ContentValidationException.getMessage(this.iconFile, list));
                            this.iconFile = null;
                        } else {
                            basicfileattributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class);
                        }
                    }

                    if (!basicfileattributes.isRegularFile()) {
                        this.iconFile = null;
                    }
                } catch (NoSuchFileException nosuchfileexception) {
                    this.iconFile = null;
                } catch (IOException ioexception) {
                    WorldSelectionList.LOGGER.error("could not validate symlink", (Throwable)ioexception);
                    this.iconFile = null;
                }
            }
        }

        @Override
        public Component getNarration() {
            Component component = Component.translatable(
                "narrator.select.world_info", this.summary.getLevelName(), Component.translationArg(new Date(this.summary.getLastPlayed())), this.summary.getInfo()
            );
            if (this.summary.isLocked()) {
                component = CommonComponents.joinForNarration(component, WorldSelectionList.WORLD_LOCKED_TOOLTIP);
            }

            if (this.summary.isExperimental()) {
                component = CommonComponents.joinForNarration(component, WorldSelectionList.WORLD_EXPERIMENTAL);
            }

            return Component.translatable("narrator.select", component);
        }

        @Override
        public void render(
            GuiGraphics p_281612_,
            int p_281353_,
            int p_283181_,
            int p_282820_,
            int p_282420_,
            int p_281855_,
            int p_283204_,
            int p_283025_,
            boolean p_283396_,
            float p_282938_
        ) {
            String s = this.summary.getLevelName();
            String s1 = this.summary.getLevelId();
            long i = this.summary.getLastPlayed();
            if (i != -1L) {
                s1 = s1 + " (" + WorldSelectionList.DATE_FORMAT.format(Instant.ofEpochMilli(i)) + ")";
            }

            if (StringUtils.isEmpty(s)) {
                s = I18n.get("selectWorld.world") + " " + (p_281353_ + 1);
            }

            Component component = this.summary.getInfo();
            p_281612_.drawString(this.minecraft.font, s, p_282820_ + 32 + 3, p_283181_ + 1, -1);
            p_281612_.drawString(this.minecraft.font, s1, p_282820_ + 32 + 3, p_283181_ + 9 + 3, -8355712);
            p_281612_.drawString(this.minecraft.font, component, p_282820_ + 32 + 3, p_283181_ + 9 + 9 + 3, -8355712);
            p_281612_.blit(RenderType::guiTextured, this.icon.textureLocation(), p_282820_, p_283181_, 0.0F, 0.0F, 32, 32, 32, 32);
            if (this.minecraft.options.touchscreen().get() || p_283396_) {
                p_281612_.fill(p_282820_, p_283181_, p_282820_ + 32, p_283181_ + 32, -1601138544);
                int j = p_283204_ - p_282820_;
                boolean flag = j < 32;
                ResourceLocation resourcelocation = flag ? WorldSelectionList.JOIN_HIGHLIGHTED_SPRITE : WorldSelectionList.JOIN_SPRITE;
                ResourceLocation resourcelocation1 = flag ? WorldSelectionList.WARNING_HIGHLIGHTED_SPRITE : WorldSelectionList.WARNING_SPRITE;
                ResourceLocation resourcelocation2 = flag ? WorldSelectionList.ERROR_HIGHLIGHTED_SPRITE : WorldSelectionList.ERROR_SPRITE;
                ResourceLocation resourcelocation3 = flag ? WorldSelectionList.MARKED_JOIN_HIGHLIGHTED_SPRITE : WorldSelectionList.MARKED_JOIN_SPRITE;
                if (this.summary instanceof LevelSummary.SymlinkLevelSummary || this.summary instanceof LevelSummary.CorruptedLevelSummary) {
                    p_281612_.blitSprite(RenderType::guiTextured, resourcelocation2, p_282820_, p_283181_, 32, 32);
                    p_281612_.blitSprite(RenderType::guiTextured, resourcelocation3, p_282820_, p_283181_, 32, 32);
                    return;
                }

                if (this.summary.isLocked()) {
                    p_281612_.blitSprite(RenderType::guiTextured, resourcelocation2, p_282820_, p_283181_, 32, 32);
                    if (flag) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
                    }
                } else if (this.summary.requiresManualConversion()) {
                    p_281612_.blitSprite(RenderType::guiTextured, resourcelocation2, p_282820_, p_283181_, 32, 32);
                    if (flag) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_REQUIRES_CONVERSION, 175));
                    }
                } else if (!this.summary.isCompatible()) {
                    p_281612_.blitSprite(RenderType::guiTextured, resourcelocation2, p_282820_, p_283181_, 32, 32);
                    if (flag) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.INCOMPATIBLE_VERSION_TOOLTIP, 175));
                    }
                } else if (this.summary.shouldBackup()) {
                    p_281612_.blitSprite(RenderType::guiTextured, resourcelocation3, p_282820_, p_283181_, 32, 32);
                    if (this.summary.isDowngrade()) {
                        p_281612_.blitSprite(RenderType::guiTextured, resourcelocation2, p_282820_, p_283181_, 32, 32);
                        if (flag) {
                            this.screen.setTooltipForNextRenderPass(ImmutableList.of(WorldSelectionList.FROM_NEWER_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.FROM_NEWER_TOOLTIP_2.getVisualOrderText()));
                        }
                    } else if (!SharedConstants.getCurrentVersion().isStable()) {
                        p_281612_.blitSprite(RenderType::guiTextured, resourcelocation1, p_282820_, p_283181_, 32, 32);
                        if (flag) {
                            this.screen.setTooltipForNextRenderPass(ImmutableList.of(WorldSelectionList.SNAPSHOT_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.SNAPSHOT_TOOLTIP_2.getVisualOrderText()));
                        }
                    }
                } else {
                    p_281612_.blitSprite(RenderType::guiTextured, resourcelocation, p_282820_, p_283181_, 32, 32);
                }
            }
        }

        @Override
        public boolean mouseClicked(double p_101706_, double p_101707_, int p_101708_) {
            if (!this.summary.primaryActionActive()) {
                return true;
            } else {
                WorldSelectionList.this.setSelected((WorldSelectionList.Entry)this);
                if (!(p_101706_ - (double)WorldSelectionList.this.getRowLeft() <= 32.0) && Util.getMillis() - this.lastClickTime >= 250L) {
                    this.lastClickTime = Util.getMillis();
                    return super.mouseClicked(p_101706_, p_101707_, p_101708_);
                } else {
                    if (this.canJoin()) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.joinWorld();
                    }

                    return true;
                }
            }
        }

        public boolean canJoin() {
            return this.summary.primaryActionActive();
        }

        public void joinWorld() {
            if (this.summary.primaryActionActive()) {
                if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
                    this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                } else {
                    this.minecraft.createWorldOpenFlows().openWorld(this.summary.getLevelId(), () -> {
                        WorldSelectionList.this.reloadWorldList();
                        this.minecraft.setScreen(this.screen);
                    });
                }
            }
        }

        public void deleteWorld() {
            this.minecraft
                .setScreen(
                    new ConfirmScreen(
                        p_170322_ -> {
                            if (p_170322_) {
                                this.minecraft.setScreen(new ProgressScreen(true));
                                this.doDeleteWorld();
                            }

                            this.minecraft.setScreen(this.screen);
                        },
                        Component.translatable("selectWorld.deleteQuestion"),
                        Component.translatable("selectWorld.deleteWarning", this.summary.getLevelName()),
                        Component.translatable("selectWorld.deleteButton"),
                        CommonComponents.GUI_CANCEL
                    )
                );
        }

        public void doDeleteWorld() {
            LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
            String s = this.summary.getLevelId();

            try (LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = levelstoragesource.createAccess(s)) {
                levelstoragesource$levelstorageaccess.deleteLevel();
            } catch (IOException ioexception) {
                SystemToast.onWorldDeleteFailure(this.minecraft, s);
                WorldSelectionList.LOGGER.error("Failed to delete world {}", s, ioexception);
            }

            WorldSelectionList.this.reloadWorldList();
        }

        public void editWorld() {
            this.queueLoadScreen();
            String s = this.summary.getLevelId();

            LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess;
            try {
                levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().validateAndCreateAccess(s);
            } catch (IOException ioexception1) {
                SystemToast.onWorldAccessFailure(this.minecraft, s);
                WorldSelectionList.LOGGER.error("Failed to access level {}", s, ioexception1);
                WorldSelectionList.this.reloadWorldList();
                return;
            } catch (ContentValidationException contentvalidationexception) {
                WorldSelectionList.LOGGER.warn("{}", contentvalidationexception.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                return;
            }

            EditWorldScreen editworldscreen;
            try {
                editworldscreen = EditWorldScreen.create(this.minecraft, levelstoragesource$levelstorageaccess, p_308276_ -> {
                    levelstoragesource$levelstorageaccess.safeClose();
                    if (p_308276_) {
                        WorldSelectionList.this.reloadWorldList();
                    }

                    this.minecraft.setScreen(this.screen);
                });
            } catch (NbtException | ReportedNbtException | IOException ioexception) {
                levelstoragesource$levelstorageaccess.safeClose();
                SystemToast.onWorldAccessFailure(this.minecraft, s);
                WorldSelectionList.LOGGER.error("Failed to load world data {}", s, ioexception);
                WorldSelectionList.this.reloadWorldList();
                return;
            }

            this.minecraft.setScreen(editworldscreen);
        }

        public void recreateWorld() {
            this.queueLoadScreen();

            try (LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().validateAndCreateAccess(this.summary.getLevelId())) {
                Pair<LevelSettings, WorldCreationContext> pair = this.minecraft.createWorldOpenFlows().recreateWorldData(levelstoragesource$levelstorageaccess);
                LevelSettings levelsettings = pair.getFirst();
                WorldCreationContext worldcreationcontext = pair.getSecond();
                Path path = CreateWorldScreen.createTempDataPackDirFromExistingWorld(levelstoragesource$levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
                worldcreationcontext.validate();
                if (worldcreationcontext.options().isOldCustomizedWorld()) {
                    this.minecraft
                        .setScreen(
                            new ConfirmScreen(
                                p_275882_ -> this.minecraft
                                        .setScreen(
                                            (Screen)(p_275882_
                                                ? CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelsettings, worldcreationcontext, path)
                                                : this.screen)
                                        ),
                                Component.translatable("selectWorld.recreate.customized.title"),
                                Component.translatable("selectWorld.recreate.customized.text"),
                                CommonComponents.GUI_PROCEED,
                                CommonComponents.GUI_CANCEL
                            )
                        );
                } else {
                    this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelsettings, worldcreationcontext, path));
                }
            } catch (ContentValidationException contentvalidationexception) {
                WorldSelectionList.LOGGER.warn("{}", contentvalidationexception.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
            } catch (Exception exception) {
                WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)exception);
                this.minecraft
                    .setScreen(
                        new AlertScreen(
                            () -> this.minecraft.setScreen(this.screen),
                            Component.translatable("selectWorld.recreate.error.title"),
                            Component.translatable("selectWorld.recreate.error.text")
                        )
                    );
            }
        }

        private void queueLoadScreen() {
            this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
        }

        private void loadIcon() {
            boolean flag = this.iconFile != null && Files.isRegularFile(this.iconFile);
            if (flag) {
                try (InputStream inputstream = Files.newInputStream(this.iconFile)) {
                    this.icon.upload(NativeImage.read(inputstream));
                } catch (Throwable throwable) {
                    WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), throwable);
                    this.iconFile = null;
                }
            } else {
                this.icon.clear();
            }
        }

        @Override
        public void close() {
            this.icon.close();
        }

        public String getLevelName() {
            return this.summary.getLevelName();
        }
    }
}