package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TitleScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component TITLE = Component.translatable("narrator.screen.title");
    private static final Component COPYRIGHT_TEXT = Component.translatable("title.credits");
    private static final String DEMO_LEVEL_ID = "Demo_World";
    private static final float FADE_IN_TIME = 2000.0F;
    @Nullable
    private SplashRenderer splash;
    private Button resetDemoButton;
    @Nullable
    private RealmsNotificationsScreen realmsNotificationsScreen;
    private float panoramaFade = 1.0F;
    private boolean fading;
    private long fadeInStart;
    private final LogoRenderer logoRenderer;

    public TitleScreen() {
        this(false);
    }

    public TitleScreen(boolean p_96733_) {
        this(p_96733_, null);
    }

    public TitleScreen(boolean p_265779_, @Nullable LogoRenderer p_265067_) {
        super(TITLE);
        this.fading = p_265779_;
        this.logoRenderer = Objects.requireNonNullElseGet(p_265067_, () -> new LogoRenderer(false));
    }

    private boolean realmsNotificationsEnabled() {
        return this.realmsNotificationsScreen != null;
    }

    @Override
    public void tick() {
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.tick();
        }
    }

    public static void registerTextures(TextureManager p_378459_) {
        p_378459_.registerForNextReload(LogoRenderer.MINECRAFT_LOGO);
        p_378459_.registerForNextReload(LogoRenderer.MINECRAFT_EDITION);
        p_378459_.registerForNextReload(PanoramaRenderer.PANORAMA_OVERLAY);
        CUBE_MAP.registerTextures(p_378459_);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected void init() {
        if (this.splash == null) {
            this.splash = this.minecraft.getSplashManager().getSplash();
        }

        int i = this.font.width(COPYRIGHT_TEXT);
        int j = this.width - i - 2;
        int k = 24;
        int l = this.height / 4 + 48;
        if (this.minecraft.isDemo()) {
            l = this.createDemoMenuOptions(l, 24);
        } else {
            l = this.createNormalMenuOptions(l, 24);
        }

        l = this.createTestWorldButton(l, 24);
        SpriteIconButton spriteiconbutton = this.addRenderableWidget(
            CommonButtons.language(
                20, p_340809_ -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true
            )
        );
        int i1 = this.width / 2 - 124;
        l += 36;
        spriteiconbutton.setPosition(i1, l);
        this.addRenderableWidget(
            Button.builder(Component.translatable("menu.options"), p_340808_ -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)))
                .bounds(this.width / 2 - 100, l, 98, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("menu.quit"), p_280831_ -> this.minecraft.stop()).bounds(this.width / 2 + 2, l, 98, 20).build()
        );
        SpriteIconButton spriteiconbutton1 = this.addRenderableWidget(
            CommonButtons.accessibility(20, p_340810_ -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), true)
        );
        spriteiconbutton1.setPosition(this.width / 2 + 104, l);
        this.addRenderableWidget(
            new PlainTextButton(
                j, this.height - 10, i, 10, COPYRIGHT_TEXT, p_280834_ -> this.minecraft.setScreen(new CreditsAndAttributionScreen(this)), this.font
            )
        );
        if (this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }

        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }
    }

    private int createTestWorldButton(int p_368793_, int p_361481_) {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            this.addRenderableWidget(
                Button.builder(Component.literal("Create Test World"), p_357674_ -> CreateWorldScreen.testWorld(this.minecraft, this))
                    .bounds(this.width / 2 - 100, p_368793_ += p_361481_, 200, 20)
                    .build()
            );
        }

        return p_368793_;
    }

    private int createNormalMenuOptions(int p_96764_, int p_96765_) {
        this.addRenderableWidget(
            Button.builder(Component.translatable("menu.singleplayer"), p_280832_ -> this.minecraft.setScreen(new SelectWorldScreen(this)))
                .bounds(this.width / 2 - 100, p_96764_, 200, 20)
                .build()
        );
        Component component = this.getMultiplayerDisabledReason();
        boolean flag = component == null;
        Tooltip tooltip = component != null ? Tooltip.create(component) : null;
        int i;
        this.addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"), p_280833_ -> {
            Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
            this.minecraft.setScreen(screen);
        }).bounds(this.width / 2 - 100, i = p_96764_ + p_96765_, 200, 20).tooltip(tooltip).build()).active = flag;
        this.addRenderableWidget(
                Button.builder(Component.translatable("menu.online"), p_325369_ -> this.minecraft.setScreen(new RealmsMainScreen(this)))
                    .bounds(this.width / 2 - 100, p_96764_ = i + p_96765_, 200, 20)
                    .tooltip(tooltip)
                    .build()
            )
            .active = flag;
        return p_96764_;
    }

    @Nullable
    private Component getMultiplayerDisabledReason() {
        if (this.minecraft.allowsMultiplayer()) {
            return null;
        } else if (this.minecraft.isNameBanned()) {
            return Component.translatable("title.multiplayer.disabled.banned.name");
        } else {
            BanDetails bandetails = this.minecraft.multiplayerBan();
            if (bandetails != null) {
                return bandetails.expires() != null
                    ? Component.translatable("title.multiplayer.disabled.banned.temporary")
                    : Component.translatable("title.multiplayer.disabled.banned.permanent");
            } else {
                return Component.translatable("title.multiplayer.disabled");
            }
        }
    }

    private int createDemoMenuOptions(int p_96773_, int p_96774_) {
        boolean flag = this.checkDemoWorldPresence();
        this.addRenderableWidget(Button.builder(Component.translatable("menu.playdemo"), p_325371_ -> {
            if (flag) {
                this.minecraft.createWorldOpenFlows().openWorld("Demo_World", () -> this.minecraft.setScreen(this));
            } else {
                this.minecraft.createWorldOpenFlows().createFreshLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions, this);
            }
        }).bounds(this.width / 2 - 100, p_96773_, 200, 20).build());
        int i;
        this.resetDemoButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("menu.resetdemo"),
                    p_308197_ -> {
                        LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();

                        try (LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = levelstoragesource.createAccess("Demo_World")) {
                            if (levelstoragesource$levelstorageaccess.hasWorldData()) {
                                this.minecraft
                                    .setScreen(
                                        new ConfirmScreen(
                                            this::confirmDemo,
                                            Component.translatable("selectWorld.deleteQuestion"),
                                            Component.translatable("selectWorld.deleteWarning", MinecraftServer.DEMO_SETTINGS.levelName()),
                                            Component.translatable("selectWorld.deleteButton"),
                                            CommonComponents.GUI_CANCEL
                                        )
                                    );
                            }
                        } catch (IOException ioexception) {
                            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
                            LOGGER.warn("Failed to access demo world", (Throwable)ioexception);
                        }
                    }
                )
                .bounds(this.width / 2 - 100, i = p_96773_ + p_96774_, 200, 20)
                .build()
        );
        this.resetDemoButton.active = flag;
        return i;
    }

    private boolean checkDemoWorldPresence() {
        try {
            boolean flag;
            try (LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().createAccess("Demo_World")) {
                flag = levelstoragesource$levelstorageaccess.hasWorldData();
            }

            return flag;
        } catch (IOException ioexception) {
            SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
            LOGGER.warn("Failed to read demo world data", (Throwable)ioexception);
            return false;
        }
    }

    @Override
    public void render(GuiGraphics p_282860_, int p_281753_, int p_283539_, float p_282628_) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }

        float f = 1.0F;
        if (this.fading) {
            float f1 = (float)(Util.getMillis() - this.fadeInStart) / 2000.0F;
            if (f1 > 1.0F) {
                this.fading = false;
                this.panoramaFade = 1.0F;
            } else {
                f1 = Mth.clamp(f1, 0.0F, 1.0F);
                f = Mth.clampedMap(f1, 0.5F, 1.0F, 0.0F, 1.0F);
                this.panoramaFade = Mth.clampedMap(f1, 0.0F, 0.5F, 0.0F, 1.0F);
            }

            this.fadeWidgets(f);
        }

        this.renderPanorama(p_282860_, p_282628_);
        int i = Mth.ceil(f * 255.0F) << 24;
        if ((i & -67108864) != 0) {
            super.render(p_282860_, p_281753_, p_283539_, p_282628_);
            this.logoRenderer.renderLogo(p_282860_, this.width, f);
            if (this.splash != null && !this.minecraft.options.hideSplashTexts().get()) {
                this.splash.render(p_282860_, this.width, this.font, i);
            }

            String s = "Minecraft " + SharedConstants.getCurrentVersion().getName();
            if (this.minecraft.isDemo()) {
                s = s + " Demo";
            } else {
                s = s + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
            }

            if (Minecraft.checkModStatus().shouldReportAsModified()) {
                s = s + I18n.get("menu.modded");
            }

            p_282860_.drawString(this.font, s, 2, this.height - 10, 16777215 | i);
            if (this.realmsNotificationsEnabled() && f >= 1.0F) {
                this.realmsNotificationsScreen.render(p_282860_, p_281753_, p_283539_, p_282628_);
            }
        }
    }

    private void fadeWidgets(float p_335005_) {
        for (GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener instanceof AbstractWidget abstractwidget) {
                abstractwidget.setAlpha(p_335005_);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics p_301363_, int p_300303_, int p_299762_, float p_300311_) {
    }

    @Override
    protected void renderPanorama(GuiGraphics p_335595_, float p_331154_) {
        PANORAMA.render(p_335595_, this.width, this.height, this.panoramaFade, p_331154_);
    }

    @Override
    public boolean mouseClicked(double p_96735_, double p_96736_, int p_96737_) {
        return super.mouseClicked(p_96735_, p_96736_, p_96737_) ? true : this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(p_96735_, p_96736_, p_96737_);
    }

    @Override
    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }
    }

    @Override
    public void added() {
        super.added();
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.added();
        }
    }

    private void confirmDemo(boolean p_96778_) {
        if (p_96778_) {
            try (LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.minecraft.getLevelSource().createAccess("Demo_World")) {
                levelstoragesource$levelstorageaccess.deleteLevel();
            } catch (IOException ioexception) {
                SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
                LOGGER.warn("Failed to delete demo world", (Throwable)ioexception);
            }
        }

        this.minecraft.setScreen(this);
    }
}