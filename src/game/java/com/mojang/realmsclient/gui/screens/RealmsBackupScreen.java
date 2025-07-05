package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.DownloadTask;
import com.mojang.realmsclient.util.task.RestoreTask;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("mco.configure.world.backup");
    static final Component RESTORE_TOOLTIP = Component.translatable("mco.backup.button.restore");
    static final Component HAS_CHANGES_TOOLTIP = Component.translatable("mco.backup.changes.tooltip");
    private static final Component NO_BACKUPS_LABEL = Component.translatable("mco.backup.nobackups");
    private static final Component DOWNLOAD_LATEST = Component.translatable("mco.backup.button.download");
    private static final String UPLOADED_KEY = "uploaded";
    private static final int PADDING = 8;
    final RealmsConfigureWorldScreen lastScreen;
    List<Backup> backups = Collections.emptyList();
    @Nullable
    RealmsBackupScreen.BackupObjectSelectionList backupList;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final int slotId;
    @Nullable
    Button downloadButton;
    final RealmsServer serverData;
    boolean noBackups = false;

    public RealmsBackupScreen(RealmsConfigureWorldScreen p_88126_, RealmsServer p_88127_, int p_88128_) {
        super(TITLE);
        this.lastScreen = p_88126_;
        this.serverData = p_88127_;
        this.slotId = p_88128_;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.backupList = this.layout.addToContents(new RealmsBackupScreen.BackupObjectSelectionList());
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.downloadButton = linearlayout.addChild(Button.builder(DOWNLOAD_LATEST, p_88185_ -> this.downloadClicked()).build());
        this.downloadButton.active = false;
        linearlayout.addChild(Button.builder(CommonComponents.GUI_BACK, p_325106_ -> this.onClose()).build());
        this.layout.visitWidgets(p_325105_ -> {
            AbstractWidget abstractwidget = this.addRenderableWidget(p_325105_);
        });
        this.repositionElements();
        this.fetchRealmsBackups();
    }

    @Override
    public void render(GuiGraphics p_283405_, int p_282020_, int p_282404_, float p_281280_) {
        super.render(p_283405_, p_282020_, p_282404_, p_281280_);
        if (this.noBackups && this.backupList != null) {
            p_283405_.drawString(
                this.font,
                NO_BACKUPS_LABEL,
                this.width / 2 - this.font.width(NO_BACKUPS_LABEL) / 2,
                this.backupList.getY() + this.backupList.getHeight() / 2 - 9 / 2,
                -1
            );
        }
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.backupList != null) {
            this.backupList.updateSize(this.width, this.layout);
        }
    }

    private void fetchRealmsBackups() {
        (new Thread("Realms-fetch-backups") {
                @Override
                public void run() {
                    RealmsClient realmsclient = RealmsClient.create();

                    try {
                        List<Backup> list = realmsclient.backupsFor(RealmsBackupScreen.this.serverData.id).backups;
                        RealmsBackupScreen.this.minecraft
                            .execute(
                                () -> {
                                    RealmsBackupScreen.this.backups = list;
                                    RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                                    if (!RealmsBackupScreen.this.noBackups && RealmsBackupScreen.this.downloadButton != null) {
                                        RealmsBackupScreen.this.downloadButton.active = true;
                                    }

                                    if (RealmsBackupScreen.this.backupList != null) {
                                        RealmsBackupScreen.this.backupList
                                            .replaceEntries(
                                                RealmsBackupScreen.this.backups
                                                    .stream()
                                                    .map(p_357551_ -> RealmsBackupScreen.this.new Entry(p_357551_))
                                                    .toList()
                                            );
                                    }
                                }
                            );
                    } catch (RealmsServiceException realmsserviceexception) {
                        RealmsBackupScreen.LOGGER.error("Couldn't request backups", (Throwable)realmsserviceexception);
                    }
                }
            })
            .start();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private void downloadClicked() {
        this.minecraft
            .setScreen(
                RealmsPopups.infoPopupScreen(
                    this,
                    Component.translatable("mco.configure.world.restore.download.question.line1"),
                    p_357549_ -> this.minecraft
                            .setScreen(
                                new RealmsLongRunningMcoTaskScreen(
                                    this.lastScreen.getNewScreen(),
                                    new DownloadTask(
                                        this.serverData.id,
                                        this.slotId,
                                        Objects.requireNonNullElse(this.serverData.name, "")
                                            + " ("
                                            + this.serverData.slots.get(this.serverData.activeSlot).getSlotName(this.serverData.activeSlot)
                                            + ")",
                                        this
                                    )
                                )
                            )
                )
            );
    }

    @OnlyIn(Dist.CLIENT)
    class BackupObjectSelectionList extends ContainerObjectSelectionList<RealmsBackupScreen.Entry> {
        private static final int ITEM_HEIGHT = 36;

        public BackupObjectSelectionList() {
            super(
                Minecraft.getInstance(),
                RealmsBackupScreen.this.width,
                RealmsBackupScreen.this.layout.getContentHeight(),
                RealmsBackupScreen.this.layout.getHeaderHeight(),
                36
            );
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ContainerObjectSelectionList.Entry<RealmsBackupScreen.Entry> {
        private static final int Y_PADDING = 2;
        private final Backup backup;
        @Nullable
        private Button restoreButton;
        @Nullable
        private Button changesButton;
        private final List<AbstractWidget> children = new ArrayList<>();

        public Entry(final Backup p_88250_) {
            this.backup = p_88250_;
            this.populateChangeList(p_88250_);
            if (!p_88250_.changeList.isEmpty()) {
                this.changesButton = Button.builder(
                        RealmsBackupScreen.HAS_CHANGES_TOOLTIP,
                        p_340707_ -> RealmsBackupScreen.this.minecraft.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, this.backup))
                    )
                    .width(8 + RealmsBackupScreen.this.font.width(RealmsBackupScreen.HAS_CHANGES_TOOLTIP))
                    .createNarration(p_325109_ -> CommonComponents.joinForNarration(Component.translatable("mco.backup.narration", this.getShortBackupDate()), p_325109_.get()))
                    .build();
                this.children.add(this.changesButton);
            }

            if (!RealmsBackupScreen.this.serverData.expired) {
                this.restoreButton = Button.builder(RealmsBackupScreen.RESTORE_TOOLTIP, p_325108_ -> this.restoreClicked())
                    .width(8 + RealmsBackupScreen.this.font.width(RealmsBackupScreen.HAS_CHANGES_TOOLTIP))
                    .createNarration(p_325111_ -> CommonComponents.joinForNarration(Component.translatable("mco.backup.narration", this.getShortBackupDate()), p_325111_.get()))
                    .build();
                this.children.add(this.restoreButton);
            }
        }

        private void populateChangeList(Backup p_279365_) {
            int i = RealmsBackupScreen.this.backups.indexOf(p_279365_);
            if (i != RealmsBackupScreen.this.backups.size() - 1) {
                Backup backup = RealmsBackupScreen.this.backups.get(i + 1);

                for (String s : p_279365_.metadata.keySet()) {
                    if (!s.contains("uploaded") && backup.metadata.containsKey(s)) {
                        if (!p_279365_.metadata.get(s).equals(backup.metadata.get(s))) {
                            this.addToChangeList(s);
                        }
                    } else {
                        this.addToChangeList(s);
                    }
                }
            }
        }

        private void addToChangeList(String p_279195_) {
            if (p_279195_.contains("uploaded")) {
                String s = DateFormat.getDateTimeInstance(3, 3).format(this.backup.lastModifiedDate);
                this.backup.changeList.put(p_279195_, s);
                this.backup.setUploadedVersion(true);
            } else {
                this.backup.changeList.put(p_279195_, this.backup.metadata.get(p_279195_));
            }
        }

        private String getShortBackupDate() {
            return DateFormat.getDateTimeInstance(3, 3).format(this.backup.lastModifiedDate);
        }

        private void restoreClicked() {
            Component component = RealmsUtil.convertToAgePresentationFromInstant(this.backup.lastModifiedDate);
            Component component1 = Component.translatable("mco.configure.world.restore.question.line1", this.getShortBackupDate(), component);
            RealmsBackupScreen.this.minecraft
                .setScreen(
                    RealmsPopups.warningPopupScreen(
                        RealmsBackupScreen.this,
                        component1,
                        p_340708_ -> RealmsBackupScreen.this.minecraft
                                .setScreen(
                                    new RealmsLongRunningMcoTaskScreen(
                                        RealmsBackupScreen.this.lastScreen.getNewScreen(),
                                        new RestoreTask(this.backup, RealmsBackupScreen.this.serverData.id, RealmsBackupScreen.this.lastScreen)
                                    )
                                )
                    )
                );
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        @Override
        public void render(
            GuiGraphics p_281408_,
            int p_281974_,
            int p_282495_,
            int p_282463_,
            int p_281562_,
            int p_282782_,
            int p_281638_,
            int p_283190_,
            boolean p_283105_,
            float p_282066_
        ) {
            int i = p_282495_ + p_282782_ / 2;
            int j = i - 9 - 2;
            int k = i + 2;
            int l = this.backup.isUploadedVersion() ? -8388737 : -1;
            p_281408_.drawString(
                RealmsBackupScreen.this.font, Component.translatable("mco.backup.entry", RealmsUtil.convertToAgePresentationFromInstant(this.backup.lastModifiedDate)), p_282463_, j, l
            );
            p_281408_.drawString(RealmsBackupScreen.this.font, this.getMediumDatePresentation(this.backup.lastModifiedDate), p_282463_, k, 5000268);
            int i1 = 0;
            int j1 = p_282495_ + p_282782_ / 2 - 10;
            if (this.restoreButton != null) {
                i1 += this.restoreButton.getWidth() + 8;
                this.restoreButton.setX(p_282463_ + p_281562_ - i1);
                this.restoreButton.setY(j1);
                this.restoreButton.render(p_281408_, p_281638_, p_283190_, p_282066_);
            }

            if (this.changesButton != null) {
                i1 += this.changesButton.getWidth() + 8;
                this.changesButton.setX(p_282463_ + p_281562_ - i1);
                this.changesButton.setY(j1);
                this.changesButton.render(p_281408_, p_281638_, p_283190_, p_282066_);
            }
        }

        private String getMediumDatePresentation(Date p_88276_) {
            return DateFormat.getDateTimeInstance(3, 3).format(p_88276_);
        }
    }
}