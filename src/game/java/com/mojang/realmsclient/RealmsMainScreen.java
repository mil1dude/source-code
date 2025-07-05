package com.mojang.realmsclient;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.client.Ping;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RegionPingResult;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import com.mojang.realmsclient.gui.RealmsServerList;
import com.mojang.realmsclient.gui.screens.AddRealmPopupScreen;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsCreateRealmScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.gui.screens.RealmsPendingInvitesScreen;
import com.mojang.realmsclient.gui.screens.RealmsPopups;
import com.mojang.realmsclient.gui.task.DataFetcher;
import com.mojang.realmsclient.util.RealmsPersistence;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientActivePlayersTooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsMainScreen extends RealmsScreen {
    static final ResourceLocation INFO_SPRITE = ResourceLocation.withDefaultNamespace("icon/info");
    static final ResourceLocation NEW_REALM_SPRITE = ResourceLocation.withDefaultNamespace("icon/new_realm");
    static final ResourceLocation EXPIRED_SPRITE = ResourceLocation.withDefaultNamespace("realm_status/expired");
    static final ResourceLocation EXPIRES_SOON_SPRITE = ResourceLocation.withDefaultNamespace("realm_status/expires_soon");
    static final ResourceLocation OPEN_SPRITE = ResourceLocation.withDefaultNamespace("realm_status/open");
    static final ResourceLocation CLOSED_SPRITE = ResourceLocation.withDefaultNamespace("realm_status/closed");
    private static final ResourceLocation INVITE_SPRITE = ResourceLocation.withDefaultNamespace("icon/invite");
    private static final ResourceLocation NEWS_SPRITE = ResourceLocation.withDefaultNamespace("icon/news");
    public static final ResourceLocation HARDCORE_MODE_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation LOGO_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/title/realms.png");
    private static final ResourceLocation NO_REALMS_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/realms/no_realms.png");
    private static final Component TITLE = Component.translatable("menu.online");
    private static final Component LOADING_TEXT = Component.translatable("mco.selectServer.loading");
    static final Component SERVER_UNITIALIZED_TEXT = Component.translatable("mco.selectServer.uninitialized");
    static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredList");
    private static final Component SUBSCRIPTION_RENEW_TEXT = Component.translatable("mco.selectServer.expiredRenew");
    static final Component TRIAL_EXPIRED_TEXT = Component.translatable("mco.selectServer.expiredTrial");
    private static final Component PLAY_TEXT = Component.translatable("mco.selectServer.play");
    private static final Component LEAVE_SERVER_TEXT = Component.translatable("mco.selectServer.leave");
    private static final Component CONFIGURE_SERVER_TEXT = Component.translatable("mco.selectServer.configure");
    static final Component SERVER_EXPIRED_TOOLTIP = Component.translatable("mco.selectServer.expired");
    static final Component SERVER_EXPIRES_SOON_TOOLTIP = Component.translatable("mco.selectServer.expires.soon");
    static final Component SERVER_EXPIRES_IN_DAY_TOOLTIP = Component.translatable("mco.selectServer.expires.day");
    static final Component SERVER_OPEN_TOOLTIP = Component.translatable("mco.selectServer.open");
    static final Component SERVER_CLOSED_TOOLTIP = Component.translatable("mco.selectServer.closed");
    static final Component UNITIALIZED_WORLD_NARRATION = Component.translatable("gui.narrate.button", SERVER_UNITIALIZED_TEXT);
    private static final Component NO_REALMS_TEXT = Component.translatable("mco.selectServer.noRealms");
    private static final Component NO_PENDING_INVITES = Component.translatable("mco.invites.nopending");
    private static final Component PENDING_INVITES = Component.translatable("mco.invites.pending");
    private static final Component INCOMPATIBLE_POPUP_TITLE = Component.translatable("mco.compatibility.incompatible.popup.title");
    private static final Component INCOMPATIBLE_RELEASE_TYPE_POPUP_MESSAGE = Component.translatable("mco.compatibility.incompatible.releaseType.popup.message");
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_COLUMNS = 3;
    private static final int BUTTON_SPACING = 4;
    private static final int CONTENT_WIDTH = 308;
    private static final int LOGO_WIDTH = 128;
    private static final int LOGO_HEIGHT = 34;
    private static final int LOGO_TEXTURE_WIDTH = 128;
    private static final int LOGO_TEXTURE_HEIGHT = 64;
    private static final int LOGO_PADDING = 5;
    private static final int HEADER_HEIGHT = 44;
    private static final int FOOTER_PADDING = 11;
    private static final int NEW_REALM_SPRITE_WIDTH = 40;
    private static final int NEW_REALM_SPRITE_HEIGHT = 20;
    private static final int ENTRY_WIDTH = 216;
    private static final int ITEM_HEIGHT = 36;
    private static final boolean SNAPSHOT = !SharedConstants.getCurrentVersion().isStable();
    private static boolean snapshotToggle = SNAPSHOT;
    private final CompletableFuture<RealmsAvailability.Result> availability = RealmsAvailability.get();
    @Nullable
    private DataFetcher.Subscription dataSubscription;
    private final Set<UUID> handledSeenNotifications = new HashSet<>();
    private static boolean regionsPinged;
    private final RateLimiter inviteNarrationLimiter;
    private final Screen lastScreen;
    private Button playButton;
    private Button backButton;
    private Button renewButton;
    private Button configureButton;
    private Button leaveButton;
    RealmsMainScreen.RealmSelectionList realmSelectionList;
    RealmsServerList serverList;
    List<RealmsServer> availableSnapshotServers = List.of();
    RealmsServerPlayerLists onlinePlayersPerRealm = new RealmsServerPlayerLists();
    private volatile boolean trialsAvailable;
    @Nullable
    private volatile String newsLink;
    long lastClickTime;
    final List<RealmsNotification> notifications = new ArrayList<>();
    private Button addRealmButton;
    private RealmsMainScreen.NotificationButton pendingInvitesButton;
    private RealmsMainScreen.NotificationButton newsButton;
    private RealmsMainScreen.LayoutState activeLayoutState;
    @Nullable
    private HeaderAndFooterLayout layout;

    public RealmsMainScreen(Screen p_86315_) {
        super(TITLE);
        this.lastScreen = p_86315_;
        this.inviteNarrationLimiter = RateLimiter.create(0.016666668F);
    }

    @Override
    public void init() {
        this.serverList = new RealmsServerList(this.minecraft);
        this.realmSelectionList = new RealmsMainScreen.RealmSelectionList();
        Component component = Component.translatable("mco.invites.title");
        this.pendingInvitesButton = new RealmsMainScreen.NotificationButton(
            component, INVITE_SPRITE, p_296029_ -> this.minecraft.setScreen(new RealmsPendingInvitesScreen(this, component))
        );
        Component component1 = Component.translatable("mco.news");
        this.newsButton = new RealmsMainScreen.NotificationButton(component1, NEWS_SPRITE, p_296035_ -> {
            String s = this.newsLink;
            if (s != null) {
                ConfirmLinkScreen.confirmLinkNow(this, s);
                if (this.newsButton.notificationCount() != 0) {
                    RealmsPersistence.RealmsPersistenceData realmspersistence$realmspersistencedata = RealmsPersistence.readFile();
                    realmspersistence$realmspersistencedata.hasUnreadNews = false;
                    RealmsPersistence.writeFile(realmspersistence$realmspersistencedata);
                    this.newsButton.setNotificationCount(0);
                }
            }
        });
        this.newsButton.setTooltip(Tooltip.create(component1));
        this.playButton = Button.builder(PLAY_TEXT, p_86659_ -> play(this.getSelectedServer(), this)).width(100).build();
        this.configureButton = Button.builder(CONFIGURE_SERVER_TEXT, p_86672_ -> this.configureClicked(this.getSelectedServer())).width(100).build();
        this.renewButton = Button.builder(SUBSCRIPTION_RENEW_TEXT, p_86622_ -> this.onRenew(this.getSelectedServer())).width(100).build();
        this.leaveButton = Button.builder(LEAVE_SERVER_TEXT, p_86679_ -> this.leaveClicked(this.getSelectedServer())).width(100).build();
        this.addRealmButton = Button.builder(Component.translatable("mco.selectServer.purchase"), p_296032_ -> this.openTrialAvailablePopup()).size(100, 20).build();
        this.backButton = Button.builder(CommonComponents.GUI_BACK, p_325094_ -> this.onClose()).width(100).build();
        if (RealmsClient.ENVIRONMENT == RealmsClient.Environment.STAGE) {
            this.addRenderableWidget(
                CycleButton.booleanBuilder(Component.literal("Snapshot"), Component.literal("Release"))
                    .create(5, 5, 100, 20, Component.literal("Realm"), (p_308035_, p_308036_) -> {
                        snapshotToggle = p_308036_;
                        this.availableSnapshotServers = List.of();
                        this.debugRefreshDataFetchers();
                    })
            );
        }

        this.updateLayout(RealmsMainScreen.LayoutState.LOADING);
        this.updateButtonStates();
        this.availability.thenAcceptAsync(p_296034_ -> {
            Screen screen = p_296034_.createErrorScreen(this.lastScreen);
            if (screen == null) {
                this.dataSubscription = this.initDataFetcher(this.minecraft.realmsDataFetcher());
            } else {
                this.minecraft.setScreen(screen);
            }
        }, this.screenExecutor);
    }

    public static boolean isSnapshot() {
        return SNAPSHOT && snapshotToggle;
    }

    @Override
    protected void repositionElements() {
        if (this.layout != null) {
            this.realmSelectionList.updateSize(this.width, this.layout);
            this.layout.arrangeElements();
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private void updateLayout() {
        if (this.serverList.isEmpty() && this.availableSnapshotServers.isEmpty() && this.notifications.isEmpty()) {
            this.updateLayout(RealmsMainScreen.LayoutState.NO_REALMS);
        } else {
            this.updateLayout(RealmsMainScreen.LayoutState.LIST);
        }
    }

    private void updateLayout(RealmsMainScreen.LayoutState p_297284_) {
        if (this.activeLayoutState != p_297284_) {
            if (this.layout != null) {
                this.layout.visitWidgets(p_325098_ -> this.removeWidget(p_325098_));
            }

            this.layout = this.createLayout(p_297284_);
            this.activeLayoutState = p_297284_;
            this.layout.visitWidgets(p_325096_ -> {
                AbstractWidget abstractwidget = this.addRenderableWidget(p_325096_);
            });
            this.repositionElements();
        }
    }

    private HeaderAndFooterLayout createLayout(RealmsMainScreen.LayoutState p_299759_) {
        HeaderAndFooterLayout headerandfooterlayout = new HeaderAndFooterLayout(this);
        headerandfooterlayout.setHeaderHeight(44);
        headerandfooterlayout.addToHeader(this.createHeader());
        Layout layout = this.createFooter(p_299759_);
        layout.arrangeElements();
        headerandfooterlayout.setFooterHeight(layout.getHeight() + 22);
        headerandfooterlayout.addToFooter(layout);
        switch (p_299759_) {
            case LOADING:
                headerandfooterlayout.addToContents(new LoadingDotsWidget(this.font, LOADING_TEXT));
                break;
            case NO_REALMS:
                headerandfooterlayout.addToContents(this.createNoRealmsContent());
                break;
            case LIST:
                headerandfooterlayout.addToContents(this.realmSelectionList);
        }

        return headerandfooterlayout;
    }

    private Layout createHeader() {
        int i = 90;
        LinearLayout linearlayout = LinearLayout.horizontal().spacing(4);
        linearlayout.defaultCellSetting().alignVerticallyMiddle();
        linearlayout.addChild(this.pendingInvitesButton);
        linearlayout.addChild(this.newsButton);
        LinearLayout linearlayout1 = LinearLayout.horizontal();
        linearlayout1.defaultCellSetting().alignVerticallyMiddle();
        linearlayout1.addChild(SpacerElement.width(90));
        linearlayout1.addChild(ImageWidget.texture(128, 34, LOGO_LOCATION, 128, 64), LayoutSettings::alignHorizontallyCenter);
        linearlayout1.addChild(new FrameLayout(90, 44)).addChild(linearlayout, LayoutSettings::alignHorizontallyRight);
        return linearlayout1;
    }

    private Layout createFooter(RealmsMainScreen.LayoutState p_299205_) {
        GridLayout gridlayout = new GridLayout().spacing(4);
        GridLayout.RowHelper gridlayout$rowhelper = gridlayout.createRowHelper(3);
        if (p_299205_ == RealmsMainScreen.LayoutState.LIST) {
            gridlayout$rowhelper.addChild(this.playButton);
            gridlayout$rowhelper.addChild(this.configureButton);
            gridlayout$rowhelper.addChild(this.renewButton);
            gridlayout$rowhelper.addChild(this.leaveButton);
        }

        gridlayout$rowhelper.addChild(this.addRealmButton);
        gridlayout$rowhelper.addChild(this.backButton);
        return gridlayout;
    }

    private LinearLayout createNoRealmsContent() {
        LinearLayout linearlayout = LinearLayout.vertical().spacing(8);
        linearlayout.defaultCellSetting().alignHorizontallyCenter();
        linearlayout.addChild(ImageWidget.texture(130, 64, NO_REALMS_LOCATION, 130, 64));
        FocusableTextWidget focusabletextwidget = new FocusableTextWidget(308, NO_REALMS_TEXT, this.font, false, 4);
        linearlayout.addChild(focusabletextwidget);
        return linearlayout;
    }

    void updateButtonStates() {
        RealmsServer realmsserver = this.getSelectedServer();
        this.addRealmButton.active = this.activeLayoutState != RealmsMainScreen.LayoutState.LOADING;
        this.playButton.active = realmsserver != null && this.shouldPlayButtonBeActive(realmsserver);
        this.renewButton.active = realmsserver != null && this.shouldRenewButtonBeActive(realmsserver);
        this.leaveButton.active = realmsserver != null && this.shouldLeaveButtonBeActive(realmsserver);
        this.configureButton.active = realmsserver != null && this.shouldConfigureButtonBeActive(realmsserver);
    }

    boolean shouldPlayButtonBeActive(RealmsServer p_86563_) {
        boolean flag = !p_86563_.expired && p_86563_.state == RealmsServer.State.OPEN;
        return flag && (p_86563_.isCompatible() || p_86563_.needsUpgrade() || isSelfOwnedServer(p_86563_));
    }

    private boolean shouldRenewButtonBeActive(RealmsServer p_86595_) {
        return p_86595_.expired && isSelfOwnedServer(p_86595_);
    }

    private boolean shouldConfigureButtonBeActive(RealmsServer p_86620_) {
        return isSelfOwnedServer(p_86620_) && p_86620_.state != RealmsServer.State.UNINITIALIZED;
    }

    private boolean shouldLeaveButtonBeActive(RealmsServer p_86645_) {
        return !isSelfOwnedServer(p_86645_);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.dataSubscription != null) {
            this.dataSubscription.tick();
        }
    }

    public static void refreshPendingInvites() {
        Minecraft.getInstance().realmsDataFetcher().pendingInvitesTask.reset();
    }

    public static void refreshServerList() {
        Minecraft.getInstance().realmsDataFetcher().serverListUpdateTask.reset();
    }

    private void debugRefreshDataFetchers() {
        for (DataFetcher.Task<?> task : this.minecraft.realmsDataFetcher().getTasks()) {
            task.reset();
        }
    }

    private DataFetcher.Subscription initDataFetcher(RealmsDataFetcher p_238836_) {
        DataFetcher.Subscription datafetcher$subscription = p_238836_.dataFetcher.createSubscription();
        datafetcher$subscription.subscribe(p_238836_.serverListUpdateTask, p_308037_ -> {
            this.serverList.updateServersList(p_308037_.serverList());
            this.availableSnapshotServers = p_308037_.availableSnapshotServers();
            this.refreshListAndLayout();
            boolean flag = false;

            for (RealmsServer realmsserver : this.serverList) {
                if (this.isSelfOwnedNonExpiredServer(realmsserver)) {
                    flag = true;
                }
            }

            if (!regionsPinged && flag) {
                regionsPinged = true;
                this.pingRegions();
            }
        });
        callRealmsClient(RealmsClient::getNotifications, p_274622_ -> {
            this.notifications.clear();
            this.notifications.addAll(p_274622_);

            for (RealmsNotification realmsnotification : p_274622_) {
                if (realmsnotification instanceof RealmsNotification.InfoPopup realmsnotification$infopopup) {
                    PopupScreen popupscreen = realmsnotification$infopopup.buildScreen(this, this::dismissNotification);
                    if (popupscreen != null) {
                        this.minecraft.setScreen(popupscreen);
                        this.markNotificationsAsSeen(List.of(realmsnotification));
                        break;
                    }
                }
            }

            if (!this.notifications.isEmpty() && this.activeLayoutState != RealmsMainScreen.LayoutState.LOADING) {
                this.refreshListAndLayout();
            }
        });
        datafetcher$subscription.subscribe(p_238836_.pendingInvitesTask, p_296027_ -> {
            this.pendingInvitesButton.setNotificationCount(p_296027_);
            this.pendingInvitesButton.setTooltip(p_296027_ == 0 ? Tooltip.create(NO_PENDING_INVITES) : Tooltip.create(PENDING_INVITES));
            if (p_296027_ > 0 && this.inviteNarrationLimiter.tryAcquire(1)) {
                this.minecraft.getNarrator().sayNow(Component.translatable("mco.configure.world.invite.narration", p_296027_));
            }
        });
        datafetcher$subscription.subscribe(p_238836_.trialAvailabilityTask, p_296031_ -> this.trialsAvailable = p_296031_);
        datafetcher$subscription.subscribe(p_238836_.onlinePlayersTask, p_340705_ -> this.onlinePlayersPerRealm = p_340705_);
        datafetcher$subscription.subscribe(p_238836_.newsTask, p_296037_ -> {
            p_238836_.newsManager.updateUnreadNews(p_296037_);
            this.newsLink = p_238836_.newsManager.newsLink();
            this.newsButton.setNotificationCount(p_238836_.newsManager.hasUnreadNews() ? Integer.MAX_VALUE : 0);
        });
        return datafetcher$subscription;
    }

    void markNotificationsAsSeen(Collection<RealmsNotification> p_311351_) {
        List<UUID> list = new ArrayList<>(p_311351_.size());

        for (RealmsNotification realmsnotification : p_311351_) {
            if (!realmsnotification.seen() && !this.handledSeenNotifications.contains(realmsnotification.uuid())) {
                list.add(realmsnotification.uuid());
            }
        }

        if (!list.isEmpty()) {
            callRealmsClient(p_274625_ -> {
                p_274625_.notificationsSeen(list);
                return null;
            }, p_274630_ -> this.handledSeenNotifications.addAll(list));
        }
    }

    private static <T> void callRealmsClient(RealmsMainScreen.RealmsCall<T> p_275561_, Consumer<T> p_275686_) {
        Minecraft minecraft = Minecraft.getInstance();
        CompletableFuture.<T>supplyAsync(() -> {
            try {
                return p_275561_.request(RealmsClient.create(minecraft));
            } catch (RealmsServiceException realmsserviceexception) {
                throw new RuntimeException(realmsserviceexception);
            }
        }).thenAcceptAsync(p_275686_, minecraft).exceptionally(p_274626_ -> {
            LOGGER.error("Failed to execute call to Realms Service", p_274626_);
            return null;
        });
    }

    private void refreshListAndLayout() {
        this.realmSelectionList.refreshEntries(this, this.getSelectedServer());
        this.updateLayout();
        this.updateButtonStates();
    }

    private void pingRegions() {
        new Thread(() -> {
            List<RegionPingResult> list = Ping.pingAllRegions();
            RealmsClient realmsclient = RealmsClient.create();
            PingResult pingresult = new PingResult();
            pingresult.pingResults = list;
            pingresult.realmIds = this.getOwnedNonExpiredRealmIds();

            try {
                realmsclient.sendPingResults(pingresult);
            } catch (Throwable throwable) {
                LOGGER.warn("Could not send ping result to Realms: ", throwable);
            }
        }).start();
    }

    private List<Long> getOwnedNonExpiredRealmIds() {
        List<Long> list = Lists.newArrayList();

        for (RealmsServer realmsserver : this.serverList) {
            if (this.isSelfOwnedNonExpiredServer(realmsserver)) {
                list.add(realmsserver.id);
            }
        }

        return list;
    }

    private void onRenew(@Nullable RealmsServer p_193500_) {
        if (p_193500_ != null) {
            String s = CommonLinks.extendRealms(p_193500_.remoteSubscriptionId, this.minecraft.getUser().getProfileId(), p_193500_.expiredTrial);
            this.minecraft.keyboardHandler.setClipboard(s);
            Util.getPlatform().openUri(s);
        }
    }

    private void configureClicked(@Nullable RealmsServer p_86657_) {
        if (p_86657_ != null && this.minecraft.isLocalPlayer(p_86657_.ownerUUID)) {
            this.minecraft.setScreen(new RealmsConfigureWorldScreen(this, p_86657_.id));
        }
    }

    private void leaveClicked(@Nullable RealmsServer p_86670_) {
        if (p_86670_ != null && !this.minecraft.isLocalPlayer(p_86670_.ownerUUID)) {
            Component component = Component.translatable("mco.configure.world.leave.question.line1");
            this.minecraft.setScreen(RealmsPopups.infoPopupScreen(this, component, p_340701_ -> this.leaveServer(p_86670_)));
        }
    }

    @Nullable
    private RealmsServer getSelectedServer() {
        return this.realmSelectionList.getSelected() instanceof RealmsMainScreen.ServerEntry realmsmainscreen$serverentry ? realmsmainscreen$serverentry.getServer() : null;
    }

    private void leaveServer(final RealmsServer p_193495_) {
        (new Thread("Realms-leave-server") {
                @Override
                public void run() {
                    try {
                        RealmsClient realmsclient = RealmsClient.create();
                        realmsclient.uninviteMyselfFrom(p_193495_.id);
                        RealmsMainScreen.this.minecraft.execute(RealmsMainScreen::refreshServerList);
                    } catch (RealmsServiceException realmsserviceexception) {
                        RealmsMainScreen.LOGGER.error("Couldn't configure world", (Throwable)realmsserviceexception);
                        RealmsMainScreen.this.minecraft
                            .execute(() -> RealmsMainScreen.this.minecraft.setScreen(new RealmsGenericErrorScreen(realmsserviceexception, RealmsMainScreen.this)));
                    }
                }
            })
            .start();
        this.minecraft.setScreen(this);
    }

    void dismissNotification(UUID p_275349_) {
        callRealmsClient(p_274628_ -> {
            p_274628_.notificationsDismiss(List.of(p_275349_));
            return null;
        }, p_274632_ -> {
            this.notifications.removeIf(p_274621_ -> p_274621_.dismissable() && p_275349_.equals(p_274621_.uuid()));
            this.refreshListAndLayout();
        });
    }

    public void resetScreen() {
        this.realmSelectionList.setSelected(null);
        refreshServerList();
    }

    @Override
    public Component getNarrationMessage() {
        return (Component)(switch (this.activeLayoutState) {
            case LOADING -> CommonComponents.joinForNarration(super.getNarrationMessage(), LOADING_TEXT);
            case NO_REALMS -> CommonComponents.joinForNarration(super.getNarrationMessage(), NO_REALMS_TEXT);
            case LIST -> super.getNarrationMessage();
        });
    }

    @Override
    public void render(GuiGraphics p_282736_, int p_283347_, int p_282480_, float p_283485_) {
        super.render(p_282736_, p_283347_, p_282480_, p_283485_);
        if (isSnapshot()) {
            p_282736_.drawString(this.font, "Minecraft " + SharedConstants.getCurrentVersion().getName(), 2, this.height - 10, -1);
        }

        if (this.trialsAvailable && this.addRealmButton.active) {
            AddRealmPopupScreen.renderDiamond(p_282736_, this.addRealmButton);
        }

        switch (RealmsClient.ENVIRONMENT) {
            case STAGE:
                this.renderEnvironment(p_282736_, "STAGE!", -256);
                break;
            case LOCAL:
                this.renderEnvironment(p_282736_, "LOCAL!", 8388479);
        }
    }

    private void openTrialAvailablePopup() {
        this.minecraft.setScreen(new AddRealmPopupScreen(this, this.trialsAvailable));
    }

    public static void play(@Nullable RealmsServer p_86516_, Screen p_86517_) {
        play(p_86516_, p_86517_, false);
    }

    public static void play(@Nullable RealmsServer p_312669_, Screen p_310591_, boolean p_309776_) {
        if (p_312669_ != null) {
            if (!isSnapshot() || p_309776_ || p_312669_.isMinigameActive()) {
                Minecraft.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(p_310591_, new GetServerDetailsTask(p_310591_, p_312669_)));
                return;
            }

            switch (p_312669_.compatibility) {
                case COMPATIBLE:
                    Minecraft.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(p_310591_, new GetServerDetailsTask(p_310591_, p_312669_)));
                    break;
                case UNVERIFIABLE:
                    confirmToPlay(
                        p_312669_,
                        p_310591_,
                        Component.translatable("mco.compatibility.unverifiable.title").withColor(-171),
                        Component.translatable("mco.compatibility.unverifiable.message"),
                        CommonComponents.GUI_CONTINUE
                    );
                    break;
                case NEEDS_DOWNGRADE:
                    confirmToPlay(
                        p_312669_,
                        p_310591_,
                        Component.translatable("selectWorld.backupQuestion.downgrade").withColor(-2142128),
                        Component.translatable(
                            "mco.compatibility.downgrade.description",
                            Component.literal(p_312669_.activeVersion).withColor(-171),
                            Component.literal(SharedConstants.getCurrentVersion().getName()).withColor(-171)
                        ),
                        Component.translatable("mco.compatibility.downgrade")
                    );
                    break;
                case NEEDS_UPGRADE:
                    upgradeRealmAndPlay(p_312669_, p_310591_);
                    break;
                case INCOMPATIBLE:
                    Minecraft.getInstance()
                        .setScreen(
                            new PopupScreen.Builder(p_310591_, INCOMPATIBLE_POPUP_TITLE)
                                .setMessage(
                                    Component.translatable(
                                        "mco.compatibility.incompatible.series.popup.message",
                                        Component.literal(p_312669_.activeVersion).withColor(-171),
                                        Component.literal(SharedConstants.getCurrentVersion().getName()).withColor(-171)
                                    )
                                )
                                .addButton(CommonComponents.GUI_BACK, PopupScreen::onClose)
                                .build()
                        );
                    break;
                case RELEASE_TYPE_INCOMPATIBLE:
                    Minecraft.getInstance()
                        .setScreen(
                            new PopupScreen.Builder(p_310591_, INCOMPATIBLE_POPUP_TITLE)
                                .setMessage(INCOMPATIBLE_RELEASE_TYPE_POPUP_MESSAGE)
                                .addButton(CommonComponents.GUI_BACK, PopupScreen::onClose)
                                .build()
                        );
            }
        }
    }

    private static void confirmToPlay(RealmsServer p_311893_, Screen p_310296_, Component p_309987_, Component p_309434_, Component p_311253_) {
        Minecraft.getInstance().setScreen(new PopupScreen.Builder(p_310296_, p_309987_).setMessage(p_309434_).addButton(p_311253_, p_340704_ -> {
            Minecraft.getInstance().setScreen(new RealmsLongRunningMcoTaskScreen(p_310296_, new GetServerDetailsTask(p_310296_, p_311893_)));
            refreshServerList();
        }).addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose).build());
    }

    private static void upgradeRealmAndPlay(RealmsServer p_343214_, Screen p_343698_) {
        Component component = Component.translatable("mco.compatibility.upgrade.title").withColor(-171);
        Component component1 = Component.translatable("mco.compatibility.upgrade");
        Component component2 = Component.literal(p_343214_.activeVersion).withColor(-171);
        Component component3 = Component.literal(SharedConstants.getCurrentVersion().getName()).withColor(-171);
        Component component4 = isSelfOwnedServer(p_343214_)
            ? Component.translatable("mco.compatibility.upgrade.description", component2, component3)
            : Component.translatable("mco.compatibility.upgrade.friend.description", component2, component3);
        confirmToPlay(p_343214_, p_343698_, component, component4, component1);
    }

    public static Component getVersionComponent(String p_312049_, boolean p_312280_) {
        return getVersionComponent(p_312049_, p_312280_ ? -8355712 : -2142128);
    }

    public static Component getVersionComponent(String p_311695_, int p_311083_) {
        return (Component)(StringUtils.isBlank(p_311695_) ? CommonComponents.EMPTY : Component.literal(p_311695_).withColor(p_311083_));
    }

    public static Component getGameModeComponent(int p_364136_, boolean p_361479_) {
        return (Component)(p_361479_ ? Component.translatable("gameMode.hardcore").withColor(-65536) : GameType.byId(p_364136_).getLongDisplayName());
    }

    static boolean isSelfOwnedServer(RealmsServer p_86684_) {
        return Minecraft.getInstance().isLocalPlayer(p_86684_.ownerUUID);
    }

    private boolean isSelfOwnedNonExpiredServer(RealmsServer p_86689_) {
        return isSelfOwnedServer(p_86689_) && !p_86689_.expired;
    }

    private void renderEnvironment(GuiGraphics p_298843_, String p_299597_, int p_300122_) {
        p_298843_.pose().pushPose();
        p_298843_.pose().translate((float)(this.width / 2 - 25), 20.0F, 0.0F);
        p_298843_.pose().mulPose(Axis.ZP.rotationDegrees(-20.0F));
        p_298843_.pose().scale(1.5F, 1.5F, 1.5F);
        p_298843_.drawString(this.font, p_299597_, 0, 0, p_300122_);
        p_298843_.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    class AvailableSnapshotEntry extends RealmsMainScreen.Entry {
        private static final Component START_SNAPSHOT_REALM = Component.translatable("mco.snapshot.start");
        private static final int TEXT_PADDING = 5;
        private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();
        private final RealmsServer parent;

        public AvailableSnapshotEntry(final RealmsServer p_311559_) {
            this.parent = p_311559_;
            this.tooltip.set(Tooltip.create(Component.translatable("mco.snapshot.tooltip")));
        }

        @Override
        public void render(
            GuiGraphics p_310547_,
            int p_310078_,
            int p_309934_,
            int p_311127_,
            int p_310500_,
            int p_311639_,
            int p_311442_,
            int p_309408_,
            boolean p_312327_,
            float p_309422_
        ) {
            p_310547_.blitSprite(RenderType::guiTextured, RealmsMainScreen.NEW_REALM_SPRITE, p_311127_ - 5, p_309934_ + p_311639_ / 2 - 10, 40, 20);
            int i = p_309934_ + p_311639_ / 2 - 9 / 2;
            p_310547_.drawString(RealmsMainScreen.this.font, START_SNAPSHOT_REALM, p_311127_ + 40 - 2, i - 5, 8388479);
            p_310547_.drawString(
                RealmsMainScreen.this.font,
                Component.translatable("mco.snapshot.description", Objects.requireNonNullElse(this.parent.name, "unknown server")),
                p_311127_ + 40 - 2,
                i + 5,
                -8355712
            );
            this.tooltip.refreshTooltipForNextRenderPass(p_312327_, this.isFocused(), new ScreenRectangle(p_311127_, p_309934_, p_310500_, p_311639_));
        }

        @Override
        public boolean mouseClicked(double p_310312_, double p_309519_, int p_313156_) {
            this.addSnapshotRealm();
            return true;
        }

        @Override
        public boolean keyPressed(int p_309531_, int p_310526_, int p_312670_) {
            if (CommonInputs.selected(p_309531_)) {
                this.addSnapshotRealm();
                return false;
            } else {
                return super.keyPressed(p_309531_, p_310526_, p_312670_);
            }
        }

        private void addSnapshotRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.this.minecraft
                .setScreen(
                    new PopupScreen.Builder(RealmsMainScreen.this, Component.translatable("mco.snapshot.createSnapshotPopup.title"))
                        .setMessage(Component.translatable("mco.snapshot.createSnapshotPopup.text"))
                        .addButton(
                            Component.translatable("mco.selectServer.create"),
                            p_357548_ -> RealmsMainScreen.this.minecraft.setScreen(new RealmsCreateRealmScreen(RealmsMainScreen.this, this.parent, true))
                        )
                        .addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose)
                        .build()
                );
        }

        @Override
        public Component getNarration() {
            return Component.translatable(
                "gui.narrate.button",
                CommonComponents.joinForNarration(
                    START_SNAPSHOT_REALM, Component.translatable("mco.snapshot.description", Objects.requireNonNullElse(this.parent.name, "unknown server"))
                )
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ButtonEntry extends RealmsMainScreen.Entry {
        private final Button button;

        public ButtonEntry(final Button p_275726_) {
            this.button = p_275726_;
        }

        @Override
        public boolean mouseClicked(double p_275240_, double p_275616_, int p_275528_) {
            this.button.mouseClicked(p_275240_, p_275616_, p_275528_);
            return super.mouseClicked(p_275240_, p_275616_, p_275528_);
        }

        @Override
        public boolean keyPressed(int p_275630_, int p_275328_, int p_275519_) {
            return this.button.keyPressed(p_275630_, p_275328_, p_275519_) ? true : super.keyPressed(p_275630_, p_275328_, p_275519_);
        }

        @Override
        public void render(
            GuiGraphics p_283542_,
            int p_282029_,
            int p_281480_,
            int p_281377_,
            int p_283160_,
            int p_281920_,
            int p_283267_,
            int p_281282_,
            boolean p_281269_,
            float p_282372_
        ) {
            this.button.setPosition(RealmsMainScreen.this.width / 2 - 75, p_281480_ + 4);
            this.button.render(p_283542_, p_283267_, p_281282_, p_282372_);
        }

        @Override
        public void setFocused(boolean p_311570_) {
            super.setFocused(p_311570_);
            this.button.setFocused(p_311570_);
        }

        @Override
        public Component getNarration() {
            return this.button.getMessage();
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class CrossButton extends ImageButton {
        private static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("widget/cross_button"), ResourceLocation.withDefaultNamespace("widget/cross_button_highlighted")
        );

        protected CrossButton(Button.OnPress p_275420_, Component p_275193_) {
            super(0, 0, 14, 14, SPRITES, p_275420_);
            this.setTooltip(Tooltip.create(p_275193_));
        }
    }

    @OnlyIn(Dist.CLIENT)
    class EmptyEntry extends RealmsMainScreen.Entry {
        @Override
        public void render(
            GuiGraphics p_301870_,
            int p_301858_,
            int p_301868_,
            int p_301866_,
            int p_301860_,
            int p_301859_,
            int p_301864_,
            int p_301865_,
            boolean p_301869_,
            float p_301861_
        ) {
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    abstract class Entry extends ObjectSelectionList.Entry<RealmsMainScreen.Entry> {
        protected static final int STATUS_LIGHT_WIDTH = 10;
        private static final int STATUS_LIGHT_HEIGHT = 28;
        protected static final int PADDING_X = 7;
        protected static final int PADDING_Y = 2;

        protected void renderStatusLights(RealmsServer p_312488_, GuiGraphics p_310620_, int p_309999_, int p_309772_, int p_310609_, int p_312927_) {
            int i = p_309999_ - 10 - 7;
            int j = p_309772_ + 2;
            if (p_312488_.expired) {
                this.drawRealmStatus(p_310620_, i, j, p_310609_, p_312927_, RealmsMainScreen.EXPIRED_SPRITE, () -> RealmsMainScreen.SERVER_EXPIRED_TOOLTIP);
            } else if (p_312488_.state == RealmsServer.State.CLOSED) {
                this.drawRealmStatus(p_310620_, i, j, p_310609_, p_312927_, RealmsMainScreen.CLOSED_SPRITE, () -> RealmsMainScreen.SERVER_CLOSED_TOOLTIP);
            } else if (RealmsMainScreen.isSelfOwnedServer(p_312488_) && p_312488_.daysLeft < 7) {
                this.drawRealmStatus(
                    p_310620_,
                    i,
                    j,
                    p_310609_,
                    p_312927_,
                    RealmsMainScreen.EXPIRES_SOON_SPRITE,
                    () -> {
                        if (p_312488_.daysLeft <= 0) {
                            return RealmsMainScreen.SERVER_EXPIRES_SOON_TOOLTIP;
                        } else {
                            return (Component)(p_312488_.daysLeft == 1
                                ? RealmsMainScreen.SERVER_EXPIRES_IN_DAY_TOOLTIP
                                : Component.translatable("mco.selectServer.expires.days", p_312488_.daysLeft));
                        }
                    }
                );
            } else if (p_312488_.state == RealmsServer.State.OPEN) {
                this.drawRealmStatus(p_310620_, i, j, p_310609_, p_312927_, RealmsMainScreen.OPEN_SPRITE, () -> RealmsMainScreen.SERVER_OPEN_TOOLTIP);
            }
        }

        private void drawRealmStatus(
            GuiGraphics p_312727_, int p_311004_, int p_311259_, int p_310947_, int p_311421_, ResourceLocation p_313063_, Supplier<Component> p_312584_
        ) {
            p_312727_.blitSprite(RenderType::guiTextured, p_313063_, p_311004_, p_311259_, 10, 28);
            if (RealmsMainScreen.this.realmSelectionList.isMouseOver((double)p_310947_, (double)p_311421_)
                && p_310947_ >= p_311004_
                && p_310947_ <= p_311004_ + 10
                && p_311421_ >= p_311259_
                && p_311421_ <= p_311259_ + 28) {
                RealmsMainScreen.this.setTooltipForNextRenderPass(p_312584_.get());
            }
        }

        protected void renderThirdLine(GuiGraphics p_309875_, int p_309431_, int p_312885_, RealmsServer p_311246_) {
            int i = this.textX(p_312885_);
            int j = this.firstLineY(p_309431_);
            int k = this.thirdLineY(j);
            if (!RealmsMainScreen.isSelfOwnedServer(p_311246_)) {
                p_309875_.drawString(RealmsMainScreen.this.font, p_311246_.owner, i, this.thirdLineY(j), -8355712);
            } else if (p_311246_.expired) {
                Component component = p_311246_.expiredTrial ? RealmsMainScreen.TRIAL_EXPIRED_TEXT : RealmsMainScreen.SUBSCRIPTION_EXPIRED_TEXT;
                p_309875_.drawString(RealmsMainScreen.this.font, component, i, k, -2142128);
            }
        }

        protected void renderClampedString(GuiGraphics p_311967_, @Nullable String p_310470_, int p_311349_, int p_310646_, int p_312217_, int p_310447_) {
            if (p_310470_ != null) {
                int i = p_312217_ - p_311349_;
                if (RealmsMainScreen.this.font.width(p_310470_) > i) {
                    String s = RealmsMainScreen.this.font.plainSubstrByWidth(p_310470_, i - RealmsMainScreen.this.font.width("... "));
                    p_311967_.drawString(RealmsMainScreen.this.font, s + "...", p_311349_, p_310646_, p_310447_);
                } else {
                    p_311967_.drawString(RealmsMainScreen.this.font, p_310470_, p_311349_, p_310646_, p_310447_);
                }
            }
        }

        protected int versionTextX(int p_312234_, int p_313052_, Component p_311065_) {
            return p_312234_ + p_313052_ - RealmsMainScreen.this.font.width(p_311065_) - 20;
        }

        protected int gameModeTextX(int p_361673_, int p_369353_, Component p_362134_) {
            return p_361673_ + p_369353_ - RealmsMainScreen.this.font.width(p_362134_) - 20;
        }

        protected int renderGameMode(RealmsServer p_363303_, GuiGraphics p_363659_, int p_362703_, int p_363524_, int p_362657_) {
            boolean flag = p_363303_.isHardcore;
            int i = p_363303_.gameMode;
            int j = p_362703_;
            if (GameType.isValidId(i)) {
                Component component = RealmsMainScreen.getGameModeComponent(i, flag);
                j = this.gameModeTextX(p_362703_, p_363524_, component);
                p_363659_.drawString(RealmsMainScreen.this.font, component, j, this.secondLineY(p_362657_), -8355712);
            }

            if (flag) {
                j -= 10;
                p_363659_.blitSprite(RenderType::guiTextured, RealmsMainScreen.HARDCORE_MODE_SPRITE, j, this.secondLineY(p_362657_), 8, 8);
            }

            return j;
        }

        protected int firstLineY(int p_311005_) {
            return p_311005_ + 1;
        }

        protected int lineHeight() {
            return 2 + 9;
        }

        protected int textX(int p_312460_) {
            return p_312460_ + 36 + 2;
        }

        protected int secondLineY(int p_309933_) {
            return p_309933_ + this.lineHeight();
        }

        protected int thirdLineY(int p_310502_) {
            return p_310502_ + this.lineHeight() * 2;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum LayoutState {
        LOADING,
        NO_REALMS,
        LIST;
    }

    @OnlyIn(Dist.CLIENT)
    static class NotificationButton extends SpriteIconButton.CenteredIcon {
        private static final ResourceLocation[] NOTIFICATION_ICONS = new ResourceLocation[]{
            ResourceLocation.withDefaultNamespace("notification/1"),
            ResourceLocation.withDefaultNamespace("notification/2"),
            ResourceLocation.withDefaultNamespace("notification/3"),
            ResourceLocation.withDefaultNamespace("notification/4"),
            ResourceLocation.withDefaultNamespace("notification/5"),
            ResourceLocation.withDefaultNamespace("notification/more")
        };
        private static final int UNKNOWN_COUNT = Integer.MAX_VALUE;
        private static final int SIZE = 20;
        private static final int SPRITE_SIZE = 14;
        private int notificationCount;

        public NotificationButton(Component p_299660_, ResourceLocation p_298832_, Button.OnPress p_297337_) {
            super(20, 20, p_299660_, 14, 14, p_298832_, p_297337_, null);
        }

        int notificationCount() {
            return this.notificationCount;
        }

        public void setNotificationCount(int p_300462_) {
            this.notificationCount = p_300462_;
        }

        @Override
        public void renderWidget(GuiGraphics p_301337_, int p_300699_, int p_300272_, float p_300587_) {
            super.renderWidget(p_301337_, p_300699_, p_300272_, p_300587_);
            if (this.active && this.notificationCount != 0) {
                this.drawNotificationCounter(p_301337_);
            }
        }

        private void drawNotificationCounter(GuiGraphics p_301365_) {
            p_301365_.blitSprite(
                RenderType::guiTextured, NOTIFICATION_ICONS[Math.min(this.notificationCount, 6) - 1], this.getX() + this.getWidth() - 5, this.getY() - 3, 8, 8
            );
        }
    }

    @OnlyIn(Dist.CLIENT)
    class NotificationMessageEntry extends RealmsMainScreen.Entry {
        private static final int SIDE_MARGINS = 40;
        private static final int OUTLINE_COLOR = -12303292;
        private final Component text;
        private final int frameItemHeight;
        private final List<AbstractWidget> children = new ArrayList<>();
        @Nullable
        private final RealmsMainScreen.CrossButton dismissButton;
        private final MultiLineTextWidget textWidget;
        private final GridLayout gridLayout;
        private final FrameLayout textFrame;
        private int lastEntryWidth = -1;

        public NotificationMessageEntry(final Component p_275215_, final int p_301862_, final RealmsNotification p_275494_) {
            this.text = p_275215_;
            this.frameItemHeight = p_301862_;
            this.gridLayout = new GridLayout();
            int i = 7;
            this.gridLayout.addChild(ImageWidget.sprite(20, 20, RealmsMainScreen.INFO_SPRITE), 0, 0, this.gridLayout.newCellSettings().padding(7, 7, 0, 0));
            this.gridLayout.addChild(SpacerElement.width(40), 0, 0);
            this.textFrame = this.gridLayout.addChild(new FrameLayout(0, 9 * 3 * (p_301862_ - 1)), 0, 1, this.gridLayout.newCellSettings().paddingTop(7));
            this.textWidget = this.textFrame
                .addChild(
                    new MultiLineTextWidget(p_275215_, RealmsMainScreen.this.font).setCentered(true), this.textFrame.newChildLayoutSettings().alignHorizontallyCenter().alignVerticallyTop()
                );
            this.gridLayout.addChild(SpacerElement.width(40), 0, 2);
            if (p_275494_.dismissable()) {
                this.dismissButton = this.gridLayout
                    .addChild(
                        new RealmsMainScreen.CrossButton(
                            p_275478_ -> RealmsMainScreen.this.dismissNotification(p_275494_.uuid()), Component.translatable("mco.notification.dismiss")
                        ),
                        0,
                        2,
                        this.gridLayout.newCellSettings().alignHorizontallyRight().padding(0, 7, 7, 0)
                    );
            } else {
                this.dismissButton = null;
            }

            this.gridLayout.visitWidgets(this.children::add);
        }

        @Override
        public boolean keyPressed(int p_275646_, int p_275453_, int p_275621_) {
            return this.dismissButton != null && this.dismissButton.keyPressed(p_275646_, p_275453_, p_275621_) ? true : super.keyPressed(p_275646_, p_275453_, p_275621_);
        }

        private void updateEntryWidth(int p_275670_) {
            if (this.lastEntryWidth != p_275670_) {
                this.refreshLayout(p_275670_);
                this.lastEntryWidth = p_275670_;
            }
        }

        private void refreshLayout(int p_275267_) {
            int i = p_275267_ - 80;
            this.textFrame.setMinWidth(i);
            this.textWidget.setMaxWidth(i);
            this.gridLayout.arrangeElements();
        }

        @Override
        public void renderBack(
            GuiGraphics p_281374_,
            int p_282622_,
            int p_283656_,
            int p_281830_,
            int p_281651_,
            int p_283685_,
            int p_281784_,
            int p_282510_,
            boolean p_283146_,
            float p_283324_
        ) {
            super.renderBack(p_281374_, p_282622_, p_283656_, p_281830_, p_281651_, p_283685_, p_281784_, p_282510_, p_283146_, p_283324_);
            p_281374_.renderOutline(p_281830_ - 2, p_283656_ - 2, p_281651_, 36 * this.frameItemHeight - 2, -12303292);
        }

        @Override
        public void render(
            GuiGraphics p_281768_,
            int p_275375_,
            int p_275358_,
            int p_275447_,
            int p_275694_,
            int p_275477_,
            int p_275710_,
            int p_275677_,
            boolean p_275542_,
            float p_275323_
        ) {
            this.gridLayout.setPosition(p_275447_, p_275358_);
            this.updateEntryWidth(p_275694_ - 4);
            this.children.forEach(p_280688_ -> p_280688_.render(p_281768_, p_275710_, p_275677_, p_275323_));
        }

        @Override
        public boolean mouseClicked(double p_275209_, double p_275338_, int p_275560_) {
            if (this.dismissButton != null) {
                this.dismissButton.mouseClicked(p_275209_, p_275338_, p_275560_);
            }

            return super.mouseClicked(p_275209_, p_275338_, p_275560_);
        }

        @Override
        public Component getNarration() {
            return this.text;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class ParentEntry extends RealmsMainScreen.Entry {
        private final RealmsServer server;
        private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

        public ParentEntry(final RealmsServer p_311143_) {
            this.server = p_311143_;
            if (!p_311143_.expired) {
                this.tooltip.set(Tooltip.create(Component.translatable("mco.snapshot.parent.tooltip")));
            }
        }

        @Override
        public void render(
            GuiGraphics p_312282_,
            int p_310045_,
            int p_311515_,
            int p_311448_,
            int p_310278_,
            int p_312055_,
            int p_311895_,
            int p_310535_,
            boolean p_312546_,
            float p_313200_
        ) {
            int i = this.textX(p_311448_);
            int j = this.firstLineY(p_311515_);
            RealmsUtil.renderPlayerFace(p_312282_, p_311448_, p_311515_, 32, this.server.ownerUUID);
            Component component = RealmsMainScreen.getVersionComponent(this.server.activeVersion, -8355712);
            int k = this.versionTextX(p_311448_, p_310278_, component);
            this.renderClampedString(p_312282_, this.server.getName(), i, j, k, -8355712);
            if (component != CommonComponents.EMPTY) {
                p_312282_.drawString(RealmsMainScreen.this.font, component, k, j, -8355712);
            }

            int l = p_311448_;
            if (!this.server.isMinigameActive()) {
                l = this.renderGameMode(this.server, p_312282_, p_311448_, p_310278_, j);
            }

            this.renderClampedString(p_312282_, this.server.getDescription(), i, this.secondLineY(j), l, -8355712);
            this.renderThirdLine(p_312282_, p_311515_, p_311448_, this.server);
            this.renderStatusLights(this.server, p_312282_, p_311448_ + p_310278_, p_311515_, p_311895_, p_310535_);
            this.tooltip.refreshTooltipForNextRenderPass(p_312546_, this.isFocused(), new ScreenRectangle(p_311448_, p_311515_, p_310278_, p_312055_));
        }

        @Override
        public Component getNarration() {
            return Component.literal(Objects.requireNonNullElse(this.server.name, "unknown server"));
        }
    }

    @OnlyIn(Dist.CLIENT)
    class RealmSelectionList extends ObjectSelectionList<RealmsMainScreen.Entry> {
        public RealmSelectionList() {
            super(Minecraft.getInstance(), RealmsMainScreen.this.width, RealmsMainScreen.this.height, 0, 36);
        }

        public void setSelected(@Nullable RealmsMainScreen.Entry p_86849_) {
            super.setSelected(p_86849_);
            RealmsMainScreen.this.updateButtonStates();
        }

        @Override
        public int getRowWidth() {
            return 300;
        }

        void refreshEntries(RealmsMainScreen p_369757_, @Nullable RealmsServer p_362542_) {
            this.clearEntries();

            for (RealmsNotification realmsnotification : RealmsMainScreen.this.notifications) {
                if (realmsnotification instanceof RealmsNotification.VisitUrl realmsnotification$visiturl) {
                    this.addEntriesForNotification(realmsnotification$visiturl, p_369757_);
                    RealmsMainScreen.this.markNotificationsAsSeen(List.of(realmsnotification));
                    break;
                }
            }

            this.refreshServerEntries(p_362542_);
        }

        private void refreshServerEntries(@Nullable RealmsServer p_365290_) {
            for (RealmsServer realmsserver : RealmsMainScreen.this.availableSnapshotServers) {
                this.addEntry(RealmsMainScreen.this.new AvailableSnapshotEntry(realmsserver));
            }

            for (RealmsServer realmsserver1 : RealmsMainScreen.this.serverList) {
                RealmsMainScreen.Entry realmsmainscreen$entry;
                if (RealmsMainScreen.isSnapshot() && !realmsserver1.isSnapshotRealm()) {
                    if (realmsserver1.state == RealmsServer.State.UNINITIALIZED) {
                        continue;
                    }

                    realmsmainscreen$entry = RealmsMainScreen.this.new ParentEntry(realmsserver1);
                } else {
                    realmsmainscreen$entry = RealmsMainScreen.this.new ServerEntry(realmsserver1);
                }

                this.addEntry(realmsmainscreen$entry);
                if (p_365290_ != null && p_365290_.id == realmsserver1.id) {
                    this.setSelected(realmsmainscreen$entry);
                }
            }
        }

        private void addEntriesForNotification(RealmsNotification.VisitUrl p_365197_, RealmsMainScreen p_363091_) {
            Component component = p_365197_.getMessage();
            int i = RealmsMainScreen.this.font.wordWrapHeight(component, 216);
            int j = Mth.positiveCeilDiv(i + 7, 36) - 1;
            this.addEntry(RealmsMainScreen.this.new NotificationMessageEntry(component, j + 2, p_365197_));

            for (int k = 0; k < j; k++) {
                this.addEntry(RealmsMainScreen.this.new EmptyEntry());
            }

            this.addEntry(RealmsMainScreen.this.new ButtonEntry(p_365197_.buildOpenLinkButton(p_363091_)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    interface RealmsCall<T> {
        T request(RealmsClient p_275639_) throws RealmsServiceException;
    }

    @OnlyIn(Dist.CLIENT)
    class ServerEntry extends RealmsMainScreen.Entry {
        private static final Component ONLINE_PLAYERS_TOOLTIP_HEADER = Component.translatable("mco.onlinePlayers");
        private static final int PLAYERS_ONLINE_SPRITE_SIZE = 9;
        private static final int SKIN_HEAD_LARGE_WIDTH = 36;
        private final RealmsServer serverData;
        private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

        public ServerEntry(final RealmsServer p_86856_) {
            this.serverData = p_86856_;
            boolean flag = RealmsMainScreen.isSelfOwnedServer(p_86856_);
            if (RealmsMainScreen.isSnapshot() && flag && p_86856_.isSnapshotRealm()) {
                this.tooltip.set(Tooltip.create(Component.translatable("mco.snapshot.paired", p_86856_.parentWorldName)));
            } else if (!flag && p_86856_.needsDowngrade()) {
                this.tooltip.set(Tooltip.create(Component.translatable("mco.snapshot.friendsRealm.downgrade", p_86856_.activeVersion)));
            }
        }

        @Override
        public void render(
            GuiGraphics p_283093_,
            int p_281645_,
            int p_283047_,
            int p_283525_,
            int p_282321_,
            int p_282391_,
            int p_281913_,
            int p_282475_,
            boolean p_282378_,
            float p_282843_
        ) {
            if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                p_283093_.blitSprite(RenderType::guiTextured, RealmsMainScreen.NEW_REALM_SPRITE, p_283525_ - 5, p_283047_ + p_282391_ / 2 - 10, 40, 20);
                int i = p_283047_ + p_282391_ / 2 - 9 / 2;
                p_283093_.drawString(RealmsMainScreen.this.font, RealmsMainScreen.SERVER_UNITIALIZED_TEXT, p_283525_ + 40 - 2, i, 8388479);
            } else {
                this.renderStatusLights(this.serverData, p_283093_, p_283525_ + 36, p_283047_, p_281913_, p_282475_);
                RealmsUtil.renderPlayerFace(p_283093_, p_283525_, p_283047_, 32, this.serverData.ownerUUID);
                this.renderFirstLine(p_283093_, p_283047_, p_283525_, p_282321_);
                this.renderSecondLine(p_283093_, p_283047_, p_283525_, p_282321_);
                this.renderThirdLine(p_283093_, p_283047_, p_283525_, this.serverData);
                boolean flag = this.renderOnlinePlayers(p_283093_, p_283047_, p_283525_, p_282321_, p_282391_, p_281913_, p_282475_);
                this.renderStatusLights(this.serverData, p_283093_, p_283525_ + p_282321_, p_283047_, p_281913_, p_282475_);
                if (!flag) {
                    this.tooltip.refreshTooltipForNextRenderPass(p_282378_, this.isFocused(), new ScreenRectangle(p_283525_, p_283047_, p_282321_, p_282391_));
                }
            }
        }

        private void renderFirstLine(GuiGraphics p_311326_, int p_311522_, int p_312647_, int p_310935_) {
            int i = this.textX(p_312647_);
            int j = this.firstLineY(p_311522_);
            Component component = RealmsMainScreen.getVersionComponent(this.serverData.activeVersion, this.serverData.isCompatible());
            int k = this.versionTextX(p_312647_, p_310935_, component);
            this.renderClampedString(p_311326_, this.serverData.getName(), i, j, k, -1);
            if (component != CommonComponents.EMPTY && !this.serverData.isMinigameActive()) {
                p_311326_.drawString(RealmsMainScreen.this.font, component, k, j, -8355712);
            }
        }

        private void renderSecondLine(GuiGraphics p_311635_, int p_311064_, int p_310167_, int p_368548_) {
            int i = this.textX(p_310167_);
            int j = this.firstLineY(p_311064_);
            int k = this.secondLineY(j);
            String s = this.serverData.getMinigameName();
            boolean flag = this.serverData.isMinigameActive();
            if (flag && s != null) {
                Component component = Component.literal(s).withStyle(ChatFormatting.GRAY);
                p_311635_.drawString(RealmsMainScreen.this.font, Component.translatable("mco.selectServer.minigameName", component).withColor(-171), i, k, -1);
            } else {
                int l = this.renderGameMode(this.serverData, p_311635_, p_310167_, p_368548_, j);
                this.renderClampedString(p_311635_, this.serverData.getDescription(), i, this.secondLineY(j), l, -8355712);
            }
        }

        private boolean renderOnlinePlayers(GuiGraphics p_343952_, int p_344863_, int p_344728_, int p_342937_, int p_345144_, int p_344520_, int p_342333_) {
            List<ProfileResult> list = RealmsMainScreen.this.onlinePlayersPerRealm.getProfileResultsFor(this.serverData.id);
            if (!list.isEmpty()) {
                int i = p_344728_ + p_342937_ - 21;
                int j = p_344863_ + p_345144_ - 9 - 2;
                int k = i;

                for (int l = 0; l < list.size(); l++) {
                    k -= 9 + (l == 0 ? 0 : 3);
                    PlayerFaceRenderer.draw(p_343952_, Minecraft.getInstance().getSkinManager().getInsecureSkin(list.get(l).profile()), k, j, 9);
                }

                if (p_344520_ >= k && p_344520_ <= i && p_342333_ >= j && p_342333_ <= j + 9) {
                    p_343952_.renderTooltip(
                        RealmsMainScreen.this.font,
                        List.of(ONLINE_PLAYERS_TOOLTIP_HEADER),
                        Optional.of(new ClientActivePlayersTooltip.ActivePlayersTooltip(list)),
                        p_344520_,
                        p_342333_
                    );
                    return true;
                }
            }

            return false;
        }

        private void playRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsMainScreen.play(this.serverData, RealmsMainScreen.this);
        }

        private void createUnitializedRealm() {
            RealmsMainScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            RealmsCreateRealmScreen realmscreaterealmscreen = new RealmsCreateRealmScreen(RealmsMainScreen.this, this.serverData, this.serverData.isSnapshotRealm());
            RealmsMainScreen.this.minecraft.setScreen(realmscreaterealmscreen);
        }

        @Override
        public boolean mouseClicked(double p_86858_, double p_86859_, int p_86860_) {
            if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                this.createUnitializedRealm();
            } else if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
                if (Util.getMillis() - RealmsMainScreen.this.lastClickTime < 250L && this.isFocused()) {
                    this.playRealm();
                }

                RealmsMainScreen.this.lastClickTime = Util.getMillis();
            }

            return true;
        }

        @Override
        public boolean keyPressed(int p_279120_, int p_279121_, int p_279296_) {
            if (CommonInputs.selected(p_279120_)) {
                if (this.serverData.state == RealmsServer.State.UNINITIALIZED) {
                    this.createUnitializedRealm();
                    return true;
                }

                if (RealmsMainScreen.this.shouldPlayButtonBeActive(this.serverData)) {
                    this.playRealm();
                    return true;
                }
            }

            return super.keyPressed(p_279120_, p_279121_, p_279296_);
        }

        @Override
        public Component getNarration() {
            return (Component)(this.serverData.state == RealmsServer.State.UNINITIALIZED
                ? RealmsMainScreen.UNITIALIZED_WORLD_NARRATION
                : Component.translatable("narrator.select", Objects.requireNonNullElse(this.serverData.name, "unknown server")));
        }

        public RealmsServer getServer() {
            return this.serverData;
        }
    }
}