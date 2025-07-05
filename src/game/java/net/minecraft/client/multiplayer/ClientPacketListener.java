package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.DebugQueryHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DemoIntroScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerReconfigScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.debug.BrainDebugRenderer;
import net.minecraft.client.renderer.debug.VillageSectionsDebugRenderer;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.client.resources.sounds.BeeAggressiveSoundInstance;
import net.minecraft.client.resources.sounds.BeeFlyingSoundInstance;
import net.minecraft.client.resources.sounds.BeeSoundInstance;
import net.minecraft.client.resources.sounds.GuardianAttackSoundInstance;
import net.minecraft.client.resources.sounds.MinecartSoundInstance;
import net.minecraft.client.resources.sounds.SnifferSoundInstance;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.LocalChatSession;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.custom.BeeDebugPayload;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.GameEventDebugPayload;
import net.minecraft.network.protocol.common.custom.GameEventListenerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestAddMarkerDebugPayload;
import net.minecraft.network.protocol.common.custom.GameTestClearMarkersDebugPayload;
import net.minecraft.network.protocol.common.custom.GoalDebugPayload;
import net.minecraft.network.protocol.common.custom.HiveDebugPayload;
import net.minecraft.network.protocol.common.custom.NeighborUpdatesDebugPayload;
import net.minecraft.network.protocol.common.custom.PathfindingDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiAddedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiRemovedDebugPayload;
import net.minecraft.network.protocol.common.custom.PoiTicketCountDebugPayload;
import net.minecraft.network.protocol.common.custom.RaidsDebugPayload;
import net.minecraft.network.protocol.common.custom.RedstoneWireOrientationsDebugPayload;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.network.protocol.common.custom.VillageSectionsDebugPayload;
import net.minecraft.network.protocol.common.custom.WorldGenAttemptDebugPayload;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundDebugSamplePacket;
import net.minecraft.network.protocol.game.ClientboundDeleteChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveMinecartPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundProjectilePowerPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetSimulationDistancePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import net.minecraft.network.protocol.game.ClientboundTickingStepPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.tags.TagNetworkSerialization;
import net.minecraft.util.Crypt;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.SelectableRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientPacketListener extends ClientCommonPacketListenerImpl implements ClientGamePacketListener, TickablePacketListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component UNSECURE_SERVER_TOAST_TITLE = Component.translatable("multiplayer.unsecureserver.toast.title");
    private static final Component UNSERURE_SERVER_TOAST = Component.translatable("multiplayer.unsecureserver.toast");
    private static final Component INVALID_PACKET = Component.translatable("multiplayer.disconnect.invalid_packet");
    private static final Component RECONFIGURE_SCREEN_MESSAGE = Component.translatable("connect.reconfiguring");
    private static final int PENDING_OFFSET_THRESHOLD = 64;
    public static final int TELEPORT_INTERPOLATION_THRESHOLD = 64;
    private final GameProfile localGameProfile;
    private ClientLevel level;
    private ClientLevel.ClientLevelData levelData;
    private final Map<UUID, PlayerInfo> playerInfoMap = Maps.newHashMap();
    private final Set<PlayerInfo> listedPlayers = new ReferenceOpenHashSet<>();
    private final ClientAdvancements advancements;
    private final ClientSuggestionProvider suggestionsProvider;
    private final DebugQueryHandler debugQueryHandler = new DebugQueryHandler(this);
    private int serverChunkRadius = 3;
    private int serverSimulationDistance = 3;
    private final RandomSource random = RandomSource.createThreadSafe();
    private CommandDispatcher<SharedSuggestionProvider> commands = new CommandDispatcher<>();
    private ClientRecipeContainer recipes = new ClientRecipeContainer(Map.of(), SelectableRecipe.SingleInputSet.empty());
    private final UUID id = UUID.randomUUID();
    private Set<ResourceKey<Level>> levels;
    private final RegistryAccess.Frozen registryAccess;
    private final FeatureFlagSet enabledFeatures;
    private final PotionBrewing potionBrewing;
    private FuelValues fuelValues;
    private OptionalInt removedPlayerVehicleId = OptionalInt.empty();
    @Nullable
    private LocalChatSession chatSession;
    private SignedMessageChain.Encoder signedMessageEncoder = SignedMessageChain.Encoder.UNSIGNED;
    private LastSeenMessagesTracker lastSeenMessages = new LastSeenMessagesTracker(20);
    private MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
    @Nullable
    private CompletableFuture<Optional<ProfileKeyPair>> keyPairFuture;
    @Nullable
    private ClientInformation remoteClientInformation;
    private final ChunkBatchSizeCalculator chunkBatchSizeCalculator = new ChunkBatchSizeCalculator();
    private final PingDebugMonitor pingDebugMonitor;
    private final DebugSampleSubscriber debugSampleSubscriber;
    @Nullable
    private LevelLoadStatusManager levelLoadStatusManager;
    private boolean serverEnforcesSecureChat;
    private boolean seenInsecureChatWarning = false;
    private volatile boolean closed;
    private final Scoreboard scoreboard = new Scoreboard();
    private final SessionSearchTrees searchTrees = new SessionSearchTrees();

    public ClientPacketListener(Minecraft p_253924_, Connection p_253614_, CommonListenerCookie p_298329_) {
        super(p_253924_, p_253614_, p_298329_);
        this.localGameProfile = p_298329_.localGameProfile();
        this.registryAccess = p_298329_.receivedRegistries();
        this.enabledFeatures = p_298329_.enabledFeatures();
        this.advancements = new ClientAdvancements(p_253924_, this.telemetryManager);
        this.suggestionsProvider = new ClientSuggestionProvider(this, p_253924_);
        this.pingDebugMonitor = new PingDebugMonitor(this, p_253924_.getDebugOverlay().getPingLogger());
        this.debugSampleSubscriber = new DebugSampleSubscriber(this, p_253924_.getDebugOverlay());
        if (p_298329_.chatState() != null) {
            p_253924_.gui.getChat().restoreState(p_298329_.chatState());
        }

        this.potionBrewing = PotionBrewing.bootstrap(this.enabledFeatures);
        this.fuelValues = FuelValues.vanillaBurnTimes(p_298329_.receivedRegistries(), this.enabledFeatures);
    }

    public ClientSuggestionProvider getSuggestionsProvider() {
        return this.suggestionsProvider;
    }

    public void close() {
        this.closed = true;
        this.clearLevel();
        this.telemetryManager.onDisconnect();
    }

    public void clearLevel() {
        this.level = null;
        this.levelLoadStatusManager = null;
    }

    public RecipeAccess recipes() {
        return this.recipes;
    }

    @Override
    public void handleLogin(ClientboundLoginPacket p_105030_) {
        PacketUtils.ensureRunningOnSameThread(p_105030_, this, this.minecraft);
        this.minecraft.gameMode = new MultiPlayerGameMode(this.minecraft, this);
        CommonPlayerSpawnInfo commonplayerspawninfo = p_105030_.commonPlayerSpawnInfo();
        List<ResourceKey<Level>> list = Lists.newArrayList(p_105030_.levels());
        Collections.shuffle(list);
        this.levels = Sets.newLinkedHashSet(list);
        ResourceKey<Level> resourcekey = commonplayerspawninfo.dimension();
        Holder<DimensionType> holder = commonplayerspawninfo.dimensionType();
        this.serverChunkRadius = p_105030_.chunkRadius();
        this.serverSimulationDistance = p_105030_.simulationDistance();
        boolean flag = commonplayerspawninfo.isDebug();
        boolean flag1 = commonplayerspawninfo.isFlat();
        int i = commonplayerspawninfo.seaLevel();
        ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(Difficulty.NORMAL, p_105030_.hardcore(), flag1);
        this.levelData = clientlevel$clientleveldata;
        this.level = new ClientLevel(
            this,
            clientlevel$clientleveldata,
            resourcekey,
            holder,
            this.serverChunkRadius,
            this.serverSimulationDistance,
            this.minecraft.levelRenderer,
            flag,
            commonplayerspawninfo.seed(),
            i
        );
        this.minecraft.setLevel(this.level, ReceivingLevelScreen.Reason.OTHER);
        if (this.minecraft.player == null) {
            this.minecraft.player = this.minecraft.gameMode.createPlayer(this.level, new StatsCounter(), new ClientRecipeBook());
            this.minecraft.player.setYRot(-180.0F);
            if (this.minecraft.getSingleplayerServer() != null) {
                this.minecraft.getSingleplayerServer().setUUID(this.minecraft.player.getUUID());
            }
        }

        this.minecraft.debugRenderer.clear();
        this.minecraft.player.resetPos();
        this.minecraft.player.setId(p_105030_.playerId());
        this.level.addEntity(this.minecraft.player);
        this.minecraft.player.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(this.minecraft.player);
        this.minecraft.cameraEntity = this.minecraft.player;
        this.startWaitingForNewLevel(this.minecraft.player, this.level, ReceivingLevelScreen.Reason.OTHER);
        this.minecraft.player.setReducedDebugInfo(p_105030_.reducedDebugInfo());
        this.minecraft.player.setShowDeathScreen(p_105030_.showDeathScreen());
        this.minecraft.player.setDoLimitedCrafting(p_105030_.doLimitedCrafting());
        this.minecraft.player.setLastDeathLocation(commonplayerspawninfo.lastDeathLocation());
        this.minecraft.player.setPortalCooldown(commonplayerspawninfo.portalCooldown());
        this.minecraft.gameMode.setLocalMode(commonplayerspawninfo.gameType(), commonplayerspawninfo.previousGameType());
        this.minecraft.options.setServerRenderDistance(p_105030_.chunkRadius());
        this.chatSession = null;
        this.lastSeenMessages = new LastSeenMessagesTracker(20);
        this.messageSignatureCache = MessageSignatureCache.createDefault();
        if (this.connection.isEncrypted()) {
            this.prepareKeyPair();
        }

        this.telemetryManager.onPlayerInfoReceived(commonplayerspawninfo.gameType(), p_105030_.hardcore());
        this.minecraft.quickPlayLog().log(this.minecraft);
        this.serverEnforcesSecureChat = p_105030_.enforcesSecureChat();
        if (this.serverData != null && !this.seenInsecureChatWarning && !this.enforcesSecureChat()) {
            SystemToast systemtoast = SystemToast.multiline(this.minecraft, SystemToast.SystemToastId.UNSECURE_SERVER_WARNING, UNSECURE_SERVER_TOAST_TITLE, UNSERURE_SERVER_TOAST);
            this.minecraft.getToastManager().addToast(systemtoast);
            this.seenInsecureChatWarning = true;
        }
    }

    @Override
    public void handleAddEntity(ClientboundAddEntityPacket p_104958_) {
        PacketUtils.ensureRunningOnSameThread(p_104958_, this, this.minecraft);
        if (this.removedPlayerVehicleId.isPresent() && this.removedPlayerVehicleId.getAsInt() == p_104958_.getId()) {
            this.removedPlayerVehicleId = OptionalInt.empty();
        }

        Entity entity = this.createEntityFromPacket(p_104958_);
        if (entity != null) {
            entity.recreateFromPacket(p_104958_);
            this.level.addEntity(entity);
            this.postAddEntitySoundInstance(entity);
        } else {
            LOGGER.warn("Skipping Entity with id {}", p_104958_.getType());
        }
    }

    @Nullable
    private Entity createEntityFromPacket(ClientboundAddEntityPacket p_301611_) {
        EntityType<?> entitytype = p_301611_.getType();
        if (entitytype == EntityType.PLAYER) {
            PlayerInfo playerinfo = this.getPlayerInfo(p_301611_.getUUID());
            if (playerinfo == null) {
                LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", p_301611_.getUUID());
                return null;
            } else {
                return new RemotePlayer(this.level, playerinfo.getProfile());
            }
        } else {
            return entitytype.create(this.level, EntitySpawnReason.LOAD);
        }
    }

    private void postAddEntitySoundInstance(Entity p_233664_) {
        if (p_233664_ instanceof AbstractMinecart abstractminecart) {
            this.minecraft.getSoundManager().play(new MinecartSoundInstance(abstractminecart));
        } else if (p_233664_ instanceof Bee bee) {
            boolean flag = bee.isAngry();
            BeeSoundInstance beesoundinstance;
            if (flag) {
                beesoundinstance = new BeeAggressiveSoundInstance(bee);
            } else {
                beesoundinstance = new BeeFlyingSoundInstance(bee);
            }

            this.minecraft.getSoundManager().queueTickingSound(beesoundinstance);
        }
    }

    @Override
    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket p_104960_) {
        PacketUtils.ensureRunningOnSameThread(p_104960_, this, this.minecraft);
        double d0 = p_104960_.getX();
        double d1 = p_104960_.getY();
        double d2 = p_104960_.getZ();
        Entity entity = new ExperienceOrb(this.level, d0, d1, d2, p_104960_.getValue());
        entity.syncPacketPositionCodec(d0, d1, d2);
        entity.setYRot(0.0F);
        entity.setXRot(0.0F);
        entity.setId(p_104960_.getId());
        this.level.addEntity(entity);
    }

    @Override
    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket p_105092_) {
        PacketUtils.ensureRunningOnSameThread(p_105092_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105092_.getId());
        if (entity != null) {
            entity.lerpMotion(p_105092_.getXa(), p_105092_.getYa(), p_105092_.getZa());
        }
    }

    @Override
    public void handleSetEntityData(ClientboundSetEntityDataPacket p_105088_) {
        PacketUtils.ensureRunningOnSameThread(p_105088_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105088_.id());
        if (entity != null) {
            entity.getEntityData().assignValues(p_105088_.packedItems());
        }
    }

    @Override
    public void handleEntityPositionSync(ClientboundEntityPositionSyncPacket p_364334_) {
        PacketUtils.ensureRunningOnSameThread(p_364334_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_364334_.id());
        if (entity != null) {
            Vec3 vec3 = p_364334_.values().position();
            entity.getPositionCodec().setBase(vec3);
            if (!entity.isControlledByLocalInstance()) {
                float f = p_364334_.values().yRot();
                float f1 = p_364334_.values().xRot();
                boolean flag = entity.position().distanceToSqr(vec3) > 4096.0;
                if (this.level.isTickingEntity(entity) && !flag) {
                    entity.lerpTo(vec3.x, vec3.y, vec3.z, f, f1, 3);
                } else {
                    entity.moveTo(vec3.x, vec3.y, vec3.z, f, f1);
                    if (entity.hasIndirectPassenger(this.minecraft.player)) {
                        entity.positionRider(this.minecraft.player);
                        this.minecraft.player.setOldPosAndRot();
                    }
                }

                entity.setOnGround(p_364334_.onGround());
            }
        }
    }

    @Override
    public void handleTeleportEntity(ClientboundTeleportEntityPacket p_105124_) {
        PacketUtils.ensureRunningOnSameThread(p_105124_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105124_.id());
        if (entity == null) {
            if (this.removedPlayerVehicleId.isPresent() && this.removedPlayerVehicleId.getAsInt() == p_105124_.id()) {
                LOGGER.debug(
                    "Trying to teleport entity with id {}, that was formerly player vehicle, applying teleport to player instead", p_105124_.id()
                );
                setValuesFromPositionPacket(p_105124_.change(), p_105124_.relatives(), this.minecraft.player, false);
                this.connection
                    .send(
                        new ServerboundMovePlayerPacket.PosRot(
                            this.minecraft.player.getX(),
                            this.minecraft.player.getY(),
                            this.minecraft.player.getZ(),
                            this.minecraft.player.getYRot(),
                            this.minecraft.player.getXRot(),
                            false,
                            false
                        )
                    );
            }
        } else {
            boolean flag = p_105124_.relatives().contains(Relative.X)
                || p_105124_.relatives().contains(Relative.Y)
                || p_105124_.relatives().contains(Relative.Z);
            boolean flag1 = this.level.isTickingEntity(entity) || !entity.isControlledByLocalInstance() || flag;
            boolean flag2 = setValuesFromPositionPacket(p_105124_.change(), p_105124_.relatives(), entity, flag1);
            entity.setOnGround(p_105124_.onGround());
            if (!flag2 && entity.hasIndirectPassenger(this.minecraft.player)) {
                entity.positionRider(this.minecraft.player);
                this.minecraft.player.setOldPosAndRot();
                if (entity.isControlledByOrIsLocalPlayer()) {
                    this.connection.send(ServerboundMoveVehiclePacket.fromEntity(entity));
                }
            }
        }
    }

    @Override
    public void handleTickingState(ClientboundTickingStatePacket p_311347_) {
        PacketUtils.ensureRunningOnSameThread(p_311347_, this, this.minecraft);
        if (this.minecraft.level != null) {
            TickRateManager tickratemanager = this.minecraft.level.tickRateManager();
            tickratemanager.setTickRate(p_311347_.tickRate());
            tickratemanager.setFrozen(p_311347_.isFrozen());
        }
    }

    @Override
    public void handleTickingStep(ClientboundTickingStepPacket p_309537_) {
        PacketUtils.ensureRunningOnSameThread(p_309537_, this, this.minecraft);
        if (this.minecraft.level != null) {
            TickRateManager tickratemanager = this.minecraft.level.tickRateManager();
            tickratemanager.setFrozenTicksToRun(p_309537_.tickSteps());
        }
    }

    @Override
    public void handleSetHeldSlot(ClientboundSetHeldSlotPacket p_365551_) {
        PacketUtils.ensureRunningOnSameThread(p_365551_, this, this.minecraft);
        if (Inventory.isHotbarSlot(p_365551_.slot())) {
            this.minecraft.player.getInventory().selected = p_365551_.slot();
        }
    }

    @Override
    public void handleMoveEntity(ClientboundMoveEntityPacket p_105036_) {
        PacketUtils.ensureRunningOnSameThread(p_105036_, this, this.minecraft);
        Entity entity = p_105036_.getEntity(this.level);
        if (entity != null) {
            if (entity.isControlledByLocalInstance()) {
                VecDeltaCodec vecdeltacodec1 = entity.getPositionCodec();
                Vec3 vec31 = vecdeltacodec1.decode((long)p_105036_.getXa(), (long)p_105036_.getYa(), (long)p_105036_.getZa());
                vecdeltacodec1.setBase(vec31);
            } else {
                if (p_105036_.hasPosition()) {
                    VecDeltaCodec vecdeltacodec = entity.getPositionCodec();
                    Vec3 vec3 = vecdeltacodec.decode((long)p_105036_.getXa(), (long)p_105036_.getYa(), (long)p_105036_.getZa());
                    vecdeltacodec.setBase(vec3);
                    float f = p_105036_.hasRotation() ? p_105036_.getyRot() : entity.lerpTargetYRot();
                    float f1 = p_105036_.hasRotation() ? p_105036_.getxRot() : entity.lerpTargetXRot();
                    entity.lerpTo(vec3.x(), vec3.y(), vec3.z(), f, f1, 3);
                } else if (p_105036_.hasRotation()) {
                    entity.lerpTo(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ(), p_105036_.getyRot(), p_105036_.getxRot(), 3);
                }

                entity.setOnGround(p_105036_.isOnGround());
            }
        }
    }

    @Override
    public void handleMinecartAlongTrack(ClientboundMoveMinecartPacket p_364082_) {
        PacketUtils.ensureRunningOnSameThread(p_364082_, this, this.minecraft);
        Entity entity = p_364082_.getEntity(this.level);
        if (entity instanceof AbstractMinecart abstractminecart) {
            if (!entity.isControlledByLocalInstance() && abstractminecart.getBehavior() instanceof NewMinecartBehavior newminecartbehavior) {
                newminecartbehavior.lerpSteps.addAll(p_364082_.lerpSteps());
            }
        }
    }

    @Override
    public void handleRotateMob(ClientboundRotateHeadPacket p_105068_) {
        PacketUtils.ensureRunningOnSameThread(p_105068_, this, this.minecraft);
        Entity entity = p_105068_.getEntity(this.level);
        if (entity != null) {
            entity.lerpHeadTo(p_105068_.getYHeadRot(), 3);
        }
    }

    @Override
    public void handleRemoveEntities(ClientboundRemoveEntitiesPacket p_182633_) {
        PacketUtils.ensureRunningOnSameThread(p_182633_, this, this.minecraft);
        p_182633_.getEntityIds().forEach((int p_357779_) -> {
            Entity entity = this.level.getEntity(p_357779_);
            if (entity != null) {
                if (entity.hasIndirectPassenger(this.minecraft.player)) {
                    LOGGER.debug("Remove entity {}:{} that has player as passenger", entity.getType(), p_357779_);
                    this.removedPlayerVehicleId = OptionalInt.of(p_357779_);
                }

                this.level.removeEntity(p_357779_, Entity.RemovalReason.DISCARDED);
            }
        });
    }

    @Override
    public void handleMovePlayer(ClientboundPlayerPositionPacket p_105056_) {
        PacketUtils.ensureRunningOnSameThread(p_105056_, this, this.minecraft);
        Player player = this.minecraft.player;
        if (!player.isPassenger()) {
            setValuesFromPositionPacket(p_105056_.change(), p_105056_.relatives(), player, false);
        }

        this.connection.send(new ServerboundAcceptTeleportationPacket(p_105056_.id()));
        this.connection
            .send(
                new ServerboundMovePlayerPacket.PosRot(
                    player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), false, false
                )
            );
    }

    private static boolean setValuesFromPositionPacket(PositionMoveRotation p_361901_, Set<Relative> p_362559_, Entity p_368395_, boolean p_366293_) {
        PositionMoveRotation positionmoverotation = PositionMoveRotation.ofEntityUsingLerpTarget(p_368395_);
        PositionMoveRotation positionmoverotation1 = PositionMoveRotation.calculateAbsolute(positionmoverotation, p_361901_, p_362559_);
        boolean flag = positionmoverotation.position().distanceToSqr(positionmoverotation1.position()) > 4096.0;
        if (p_366293_ && !flag) {
            p_368395_.lerpTo(
                positionmoverotation1.position().x(),
                positionmoverotation1.position().y(),
                positionmoverotation1.position().z(),
                positionmoverotation1.yRot(),
                positionmoverotation1.xRot(),
                3
            );
            p_368395_.setDeltaMovement(positionmoverotation1.deltaMovement());
            return true;
        } else {
            p_368395_.setPos(positionmoverotation1.position());
            p_368395_.setDeltaMovement(positionmoverotation1.deltaMovement());
            p_368395_.setYRot(positionmoverotation1.yRot());
            p_368395_.setXRot(positionmoverotation1.xRot());
            PositionMoveRotation positionmoverotation2 = new PositionMoveRotation(p_368395_.oldPosition(), Vec3.ZERO, p_368395_.yRotO, p_368395_.xRotO);
            PositionMoveRotation positionmoverotation3 = PositionMoveRotation.calculateAbsolute(positionmoverotation2, p_361901_, p_362559_);
            p_368395_.setOldPosAndRot(positionmoverotation3.position(), positionmoverotation3.yRot(), positionmoverotation3.xRot());
            return false;
        }
    }

    @Override
    public void handleRotatePlayer(ClientboundPlayerRotationPacket p_367721_) {
        PacketUtils.ensureRunningOnSameThread(p_367721_, this, this.minecraft);
        Player player = this.minecraft.player;
        player.setYRot(p_367721_.yRot());
        player.setXRot(p_367721_.xRot());
        player.setOldRot();
        this.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), false, false));
    }

    @Override
    public void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket p_105070_) {
        PacketUtils.ensureRunningOnSameThread(p_105070_, this, this.minecraft);
        p_105070_.runUpdates((p_284633_, p_284634_) -> this.level.setServerVerifiedBlockState(p_284633_, p_284634_, 19));
    }

    @Override
    public void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket p_194241_) {
        PacketUtils.ensureRunningOnSameThread(p_194241_, this, this.minecraft);
        int i = p_194241_.getX();
        int j = p_194241_.getZ();
        this.updateLevelChunk(i, j, p_194241_.getChunkData());
        ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = p_194241_.getLightData();
        this.level.queueLightUpdate(() -> {
            this.applyLightData(i, j, clientboundlightupdatepacketdata, false);
            LevelChunk levelchunk = this.level.getChunkSource().getChunk(i, j, false);
            if (levelchunk != null) {
                this.enableChunkLight(levelchunk, i, j);
                this.minecraft.levelRenderer.onChunkReadyToRender(levelchunk.getPos());
            }
        });
    }

    @Override
    public void handleChunksBiomes(ClientboundChunksBiomesPacket p_275437_) {
        PacketUtils.ensureRunningOnSameThread(p_275437_, this, this.minecraft);

        for (ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata : p_275437_.chunkBiomeData()) {
            this.level
                .getChunkSource()
                .replaceBiomes(
                    clientboundchunksbiomespacket$chunkbiomedata.pos().x,
                    clientboundchunksbiomespacket$chunkbiomedata.pos().z,
                    clientboundchunksbiomespacket$chunkbiomedata.getReadBuffer()
                );
        }

        for (ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata1 : p_275437_.chunkBiomeData()) {
            this.level
                .onChunkLoaded(
                    new ChunkPos(
                        clientboundchunksbiomespacket$chunkbiomedata1.pos().x, clientboundchunksbiomespacket$chunkbiomedata1.pos().z
                    )
                );
        }

        for (ClientboundChunksBiomesPacket.ChunkBiomeData clientboundchunksbiomespacket$chunkbiomedata2 : p_275437_.chunkBiomeData()) {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    for (int k = this.level.getMinSectionY(); k <= this.level.getMaxSectionY(); k++) {
                        this.minecraft
                            .levelRenderer
                            .setSectionDirty(
                                clientboundchunksbiomespacket$chunkbiomedata2.pos().x + i,
                                k,
                                clientboundchunksbiomespacket$chunkbiomedata2.pos().z + j
                            );
                    }
                }
            }
        }
    }

    private void updateLevelChunk(int p_194199_, int p_194200_, ClientboundLevelChunkPacketData p_194201_) {
        this.level.getChunkSource().replaceWithPacketData(p_194199_, p_194200_, p_194201_.getReadBuffer(), p_194201_.getHeightmaps(), p_194201_.getBlockEntitiesTagsConsumer(p_194199_, p_194200_));
    }

    private void enableChunkLight(LevelChunk p_194213_, int p_194214_, int p_194215_) {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        LevelChunkSection[] alevelchunksection = p_194213_.getSections();
        ChunkPos chunkpos = p_194213_.getPos();

        for (int i = 0; i < alevelchunksection.length; i++) {
            LevelChunkSection levelchunksection = alevelchunksection[i];
            int j = this.level.getSectionYFromSectionIndex(i);
            levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), levelchunksection.hasOnlyAir());
        }

        this.level.setSectionRangeDirty(p_194214_ - 1, this.level.getMinSectionY(), p_194215_ - 1, p_194214_ + 1, this.level.getMaxSectionY(), p_194215_ + 1);
    }

    @Override
    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket p_105014_) {
        PacketUtils.ensureRunningOnSameThread(p_105014_, this, this.minecraft);
        this.level.getChunkSource().drop(p_105014_.pos());
        this.queueLightRemoval(p_105014_);
    }

    private void queueLightRemoval(ClientboundForgetLevelChunkPacket p_194253_) {
        ChunkPos chunkpos = p_194253_.pos();
        this.level.queueLightUpdate(() -> {
            LevelLightEngine levellightengine = this.level.getLightEngine();
            levellightengine.setLightEnabled(chunkpos, false);

            for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); i++) {
                SectionPos sectionpos = SectionPos.of(chunkpos, i);
                levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, null);
                levellightengine.queueSectionData(LightLayer.SKY, sectionpos, null);
            }

            for (int j = this.level.getMinSectionY(); j <= this.level.getMaxSectionY(); j++) {
                levellightengine.updateSectionStatus(SectionPos.of(chunkpos, j), true);
            }
        });
    }

    @Override
    public void handleBlockUpdate(ClientboundBlockUpdatePacket p_104980_) {
        PacketUtils.ensureRunningOnSameThread(p_104980_, this, this.minecraft);
        this.level.setServerVerifiedBlockState(p_104980_.getPos(), p_104980_.getBlockState(), 19);
    }

    @Override
    public void handleConfigurationStart(ClientboundStartConfigurationPacket p_298839_) {
        PacketUtils.ensureRunningOnSameThread(p_298839_, this, this.minecraft);
        this.minecraft.getChatListener().clearQueue();
        this.sendChatAcknowledgement();
        ChatComponent.State chatcomponent$state = this.minecraft.gui.getChat().storeState();
        this.minecraft.clearClientLevel(new ServerReconfigScreen(RECONFIGURE_SCREEN_MESSAGE, this.connection));
        this.connection
            .setupInboundProtocol(
                ConfigurationProtocols.CLIENTBOUND,
                new ClientConfigurationPacketListenerImpl(
                    this.minecraft,
                    this.connection,
                    new CommonListenerCookie(
                        this.localGameProfile,
                        this.telemetryManager,
                        this.registryAccess,
                        this.enabledFeatures,
                        this.serverBrand,
                        this.serverData,
                        this.postDisconnectScreen,
                        this.serverCookies,
                        chatcomponent$state,
                        this.customReportDetails,
                        this.serverLinks
                    )
                )
            );
        this.send(ServerboundConfigurationAcknowledgedPacket.INSTANCE);
        this.connection.setupOutboundProtocol(ConfigurationProtocols.SERVERBOUND);
    }

    @Override
    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket p_105122_) {
        PacketUtils.ensureRunningOnSameThread(p_105122_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105122_.getItemId());
        LivingEntity livingentity = (LivingEntity)this.level.getEntity(p_105122_.getPlayerId());
        if (livingentity == null) {
            livingentity = this.minecraft.player;
        }

        if (entity != null) {
            if (entity instanceof ExperienceOrb) {
                this.level
                    .playLocalSound(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        SoundSource.PLAYERS,
                        0.1F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.35F + 0.9F,
                        false
                    );
            } else {
                this.level
                    .playLocalSound(
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.2F,
                        (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F,
                        false
                    );
            }

            this.minecraft.particleEngine.add(new ItemPickupParticle(this.minecraft.getEntityRenderDispatcher(), this.level, entity, livingentity));
            if (entity instanceof ItemEntity itementity) {
                ItemStack itemstack = itementity.getItem();
                if (!itemstack.isEmpty()) {
                    itemstack.shrink(p_105122_.getAmount());
                }

                if (itemstack.isEmpty()) {
                    this.level.removeEntity(p_105122_.getItemId(), Entity.RemovalReason.DISCARDED);
                }
            } else if (!(entity instanceof ExperienceOrb)) {
                this.level.removeEntity(p_105122_.getItemId(), Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void handleSystemChat(ClientboundSystemChatPacket p_233708_) {
        PacketUtils.ensureRunningOnSameThread(p_233708_, this, this.minecraft);
        this.minecraft.getChatListener().handleSystemMessage(p_233708_.content(), p_233708_.overlay());
    }

    @Override
    public void handlePlayerChat(ClientboundPlayerChatPacket p_233702_) {
        PacketUtils.ensureRunningOnSameThread(p_233702_, this, this.minecraft);
        Optional<SignedMessageBody> optional = p_233702_.body().unpack(this.messageSignatureCache);
        if (optional.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET);
        } else {
            this.messageSignatureCache.push(optional.get(), p_233702_.signature());
            UUID uuid = p_233702_.sender();
            PlayerInfo playerinfo = this.getPlayerInfo(uuid);
            if (playerinfo == null) {
                LOGGER.error("Received player chat packet for unknown player with ID: {}", uuid);
                this.minecraft.getChatListener().handleChatMessageError(uuid, p_233702_.chatType());
            } else {
                RemoteChatSession remotechatsession = playerinfo.getChatSession();
                SignedMessageLink signedmessagelink;
                if (remotechatsession != null) {
                    signedmessagelink = new SignedMessageLink(p_233702_.index(), uuid, remotechatsession.sessionId());
                } else {
                    signedmessagelink = SignedMessageLink.unsigned(uuid);
                }

                PlayerChatMessage playerchatmessage = new PlayerChatMessage(
                    signedmessagelink, p_233702_.signature(), optional.get(), p_233702_.unsignedContent(), p_233702_.filterMask()
                );
                playerchatmessage = playerinfo.getMessageValidator().updateAndValidate(playerchatmessage);
                if (playerchatmessage != null) {
                    this.minecraft.getChatListener().handlePlayerChatMessage(playerchatmessage, playerinfo.getProfile(), p_233702_.chatType());
                } else {
                    this.minecraft.getChatListener().handleChatMessageError(uuid, p_233702_.chatType());
                }
            }
        }
    }

    @Override
    public void handleDisguisedChat(ClientboundDisguisedChatPacket p_251920_) {
        PacketUtils.ensureRunningOnSameThread(p_251920_, this, this.minecraft);
        this.minecraft.getChatListener().handleDisguisedChatMessage(p_251920_.message(), p_251920_.chatType());
    }

    @Override
    public void handleDeleteChat(ClientboundDeleteChatPacket p_241325_) {
        PacketUtils.ensureRunningOnSameThread(p_241325_, this, this.minecraft);
        Optional<MessageSignature> optional = p_241325_.messageSignature().unpack(this.messageSignatureCache);
        if (optional.isEmpty()) {
            this.connection.disconnect(INVALID_PACKET);
        } else {
            this.lastSeenMessages.ignorePending(optional.get());
            if (!this.minecraft.getChatListener().removeFromDelayedMessageQueue(optional.get())) {
                this.minecraft.gui.getChat().deleteMessage(optional.get());
            }
        }
    }

    @Override
    public void handleAnimate(ClientboundAnimatePacket p_104968_) {
        PacketUtils.ensureRunningOnSameThread(p_104968_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_104968_.getId());
        if (entity != null) {
            if (p_104968_.getAction() == 0) {
                LivingEntity livingentity = (LivingEntity)entity;
                livingentity.swing(InteractionHand.MAIN_HAND);
            } else if (p_104968_.getAction() == 3) {
                LivingEntity livingentity1 = (LivingEntity)entity;
                livingentity1.swing(InteractionHand.OFF_HAND);
            } else if (p_104968_.getAction() == 2) {
                Player player = (Player)entity;
                player.stopSleepInBed(false, false);
            } else if (p_104968_.getAction() == 4) {
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.CRIT);
            } else if (p_104968_.getAction() == 5) {
                this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.ENCHANTED_HIT);
            }
        }
    }

    @Override
    public void handleHurtAnimation(ClientboundHurtAnimationPacket p_265581_) {
        PacketUtils.ensureRunningOnSameThread(p_265581_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_265581_.id());
        if (entity != null) {
            entity.animateHurt(p_265581_.yaw());
        }
    }

    @Override
    public void handleSetTime(ClientboundSetTimePacket p_105108_) {
        PacketUtils.ensureRunningOnSameThread(p_105108_, this, this.minecraft);
        this.level.setTimeFromServer(p_105108_.gameTime(), p_105108_.dayTime(), p_105108_.tickDayTime());
        this.telemetryManager.setTime(p_105108_.gameTime());
    }

    @Override
    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket p_105084_) {
        PacketUtils.ensureRunningOnSameThread(p_105084_, this, this.minecraft);
        this.minecraft.level.setDefaultSpawnPos(p_105084_.getPos(), p_105084_.getAngle());
    }

    @Override
    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket p_105102_) {
        PacketUtils.ensureRunningOnSameThread(p_105102_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105102_.getVehicle());
        if (entity == null) {
            LOGGER.warn("Received passengers for unknown entity");
        } else {
            boolean flag = entity.hasIndirectPassenger(this.minecraft.player);
            entity.ejectPassengers();

            for (int i : p_105102_.getPassengers()) {
                Entity entity1 = this.level.getEntity(i);
                if (entity1 != null) {
                    entity1.startRiding(entity, true);
                    if (entity1 == this.minecraft.player) {
                        this.removedPlayerVehicleId = OptionalInt.empty();
                        if (!flag) {
                            if (entity instanceof AbstractBoat) {
                                this.minecraft.player.yRotO = entity.getYRot();
                                this.minecraft.player.setYRot(entity.getYRot());
                                this.minecraft.player.setYHeadRot(entity.getYRot());
                            }

                            Component component = Component.translatable("mount.onboard", this.minecraft.options.keyShift.getTranslatedKeyMessage());
                            this.minecraft.gui.setOverlayMessage(component, false);
                            this.minecraft.getNarrator().sayNow(component);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket p_105090_) {
        PacketUtils.ensureRunningOnSameThread(p_105090_, this, this.minecraft);
        if (this.level.getEntity(p_105090_.getSourceId()) instanceof Leashable leashable) {
            leashable.setDelayedLeashHolderId(p_105090_.getDestId());
        }
    }

    private static ItemStack findTotem(Player p_104928_) {
        for (InteractionHand interactionhand : InteractionHand.values()) {
            ItemStack itemstack = p_104928_.getItemInHand(interactionhand);
            if (itemstack.has(DataComponents.DEATH_PROTECTION)) {
                return itemstack;
            }
        }

        return new ItemStack(Items.TOTEM_OF_UNDYING);
    }

    @Override
    public void handleEntityEvent(ClientboundEntityEventPacket p_105010_) {
        PacketUtils.ensureRunningOnSameThread(p_105010_, this, this.minecraft);
        Entity entity = p_105010_.getEntity(this.level);
        if (entity != null) {
            switch (p_105010_.getEventId()) {
                case 21:
                    this.minecraft.getSoundManager().play(new GuardianAttackSoundInstance((Guardian)entity));
                    break;
                case 35:
                    int i = 40;
                    this.minecraft.particleEngine.createTrackingEmitter(entity, ParticleTypes.TOTEM_OF_UNDYING, 30);
                    this.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0F, 1.0F, false);
                    if (entity == this.minecraft.player) {
                        this.minecraft.gameRenderer.displayItemActivation(findTotem(this.minecraft.player));
                    }
                    break;
                case 63:
                    this.minecraft.getSoundManager().play(new SnifferSoundInstance((Sniffer)entity));
                    break;
                default:
                    entity.handleEntityEvent(p_105010_.getEventId());
            }
        }
    }

    @Override
    public void handleDamageEvent(ClientboundDamageEventPacket p_270800_) {
        PacketUtils.ensureRunningOnSameThread(p_270800_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_270800_.entityId());
        if (entity != null) {
            entity.handleDamageEvent(p_270800_.getSource(this.level));
        }
    }

    @Override
    public void handleSetHealth(ClientboundSetHealthPacket p_105098_) {
        PacketUtils.ensureRunningOnSameThread(p_105098_, this, this.minecraft);
        this.minecraft.player.hurtTo(p_105098_.getHealth());
        this.minecraft.player.getFoodData().setFoodLevel(p_105098_.getFood());
        this.minecraft.player.getFoodData().setSaturation(p_105098_.getSaturation());
    }

    @Override
    public void handleSetExperience(ClientboundSetExperiencePacket p_105096_) {
        PacketUtils.ensureRunningOnSameThread(p_105096_, this, this.minecraft);
        this.minecraft.player.setExperienceValues(p_105096_.getExperienceProgress(), p_105096_.getTotalExperience(), p_105096_.getExperienceLevel());
    }

    @Override
    public void handleRespawn(ClientboundRespawnPacket p_105066_) {
        PacketUtils.ensureRunningOnSameThread(p_105066_, this, this.minecraft);
        CommonPlayerSpawnInfo commonplayerspawninfo = p_105066_.commonPlayerSpawnInfo();
        ResourceKey<Level> resourcekey = commonplayerspawninfo.dimension();
        Holder<DimensionType> holder = commonplayerspawninfo.dimensionType();
        LocalPlayer localplayer = this.minecraft.player;
        ResourceKey<Level> resourcekey1 = localplayer.level().dimension();
        boolean flag = resourcekey != resourcekey1;
        ReceivingLevelScreen.Reason receivinglevelscreen$reason = this.determineLevelLoadingReason(localplayer.isDeadOrDying(), resourcekey, resourcekey1);
        if (flag) {
            Map<MapId, MapItemSavedData> map = this.level.getAllMapData();
            boolean flag1 = commonplayerspawninfo.isDebug();
            boolean flag2 = commonplayerspawninfo.isFlat();
            int i = commonplayerspawninfo.seaLevel();
            ClientLevel.ClientLevelData clientlevel$clientleveldata = new ClientLevel.ClientLevelData(this.levelData.getDifficulty(), this.levelData.isHardcore(), flag2);
            this.levelData = clientlevel$clientleveldata;
            this.level = new ClientLevel(
                this,
                clientlevel$clientleveldata,
                resourcekey,
                holder,
                this.serverChunkRadius,
                this.serverSimulationDistance,
                this.minecraft.levelRenderer,
                flag1,
                commonplayerspawninfo.seed(),
                i
            );
            this.level.addMapData(map);
            this.minecraft.setLevel(this.level, receivinglevelscreen$reason);
        }

        this.minecraft.cameraEntity = null;
        if (localplayer.hasContainerOpen()) {
            localplayer.closeContainer();
        }

        LocalPlayer localplayer1;
        if (p_105066_.shouldKeep((byte)2)) {
            localplayer1 = this.minecraft
                .gameMode
                .createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook(), localplayer.isShiftKeyDown(), localplayer.isSprinting());
        } else {
            localplayer1 = this.minecraft.gameMode.createPlayer(this.level, localplayer.getStats(), localplayer.getRecipeBook());
        }

        this.startWaitingForNewLevel(localplayer1, this.level, receivinglevelscreen$reason);
        localplayer1.setId(localplayer.getId());
        this.minecraft.player = localplayer1;
        if (flag) {
            this.minecraft.getMusicManager().stopPlaying();
        }

        this.minecraft.cameraEntity = localplayer1;
        if (p_105066_.shouldKeep((byte)2)) {
            List<SynchedEntityData.DataValue<?>> list = localplayer.getEntityData().getNonDefaultValues();
            if (list != null) {
                localplayer1.getEntityData().assignValues(list);
            }

            localplayer1.setDeltaMovement(localplayer.getDeltaMovement());
            localplayer1.setYRot(localplayer.getYRot());
            localplayer1.setXRot(localplayer.getXRot());
        } else {
            localplayer1.resetPos();
            localplayer1.setYRot(-180.0F);
        }

        if (p_105066_.shouldKeep((byte)1)) {
            localplayer1.getAttributes().assignAllValues(localplayer.getAttributes());
        } else {
            localplayer1.getAttributes().assignBaseValues(localplayer.getAttributes());
        }

        this.level.addEntity(localplayer1);
        localplayer1.input = new KeyboardInput(this.minecraft.options);
        this.minecraft.gameMode.adjustPlayer(localplayer1);
        localplayer1.setReducedDebugInfo(localplayer.isReducedDebugInfo());
        localplayer1.setShowDeathScreen(localplayer.shouldShowDeathScreen());
        localplayer1.setLastDeathLocation(commonplayerspawninfo.lastDeathLocation());
        localplayer1.setPortalCooldown(commonplayerspawninfo.portalCooldown());
        localplayer1.spinningEffectIntensity = localplayer.spinningEffectIntensity;
        localplayer1.oSpinningEffectIntensity = localplayer.oSpinningEffectIntensity;
        if (this.minecraft.screen instanceof DeathScreen || this.minecraft.screen instanceof DeathScreen.TitleConfirmScreen) {
            this.minecraft.setScreen(null);
        }

        this.minecraft.gameMode.setLocalMode(commonplayerspawninfo.gameType(), commonplayerspawninfo.previousGameType());
    }

    private ReceivingLevelScreen.Reason determineLevelLoadingReason(boolean p_327777_, ResourceKey<Level> p_333661_, ResourceKey<Level> p_327689_) {
        ReceivingLevelScreen.Reason receivinglevelscreen$reason = ReceivingLevelScreen.Reason.OTHER;
        if (!p_327777_) {
            if (p_333661_ == Level.NETHER || p_327689_ == Level.NETHER) {
                receivinglevelscreen$reason = ReceivingLevelScreen.Reason.NETHER_PORTAL;
            } else if (p_333661_ == Level.END || p_327689_ == Level.END) {
                receivinglevelscreen$reason = ReceivingLevelScreen.Reason.END_PORTAL;
            }
        }

        return receivinglevelscreen$reason;
    }

    @Override
    public void handleExplosion(ClientboundExplodePacket p_105012_) {
        PacketUtils.ensureRunningOnSameThread(p_105012_, this, this.minecraft);
        Vec3 vec3 = p_105012_.center();
        this.minecraft
            .level
            .playLocalSound(
                vec3.x(),
                vec3.y(),
                vec3.z(),
                p_105012_.explosionSound().value(),
                SoundSource.BLOCKS,
                4.0F,
                (1.0F + (this.minecraft.level.random.nextFloat() - this.minecraft.level.random.nextFloat()) * 0.2F) * 0.7F,
                false
            );
        this.minecraft.level.addParticle(p_105012_.explosionParticle(), vec3.x(), vec3.y(), vec3.z(), 1.0, 0.0, 0.0);
        p_105012_.playerKnockback().ifPresent(this.minecraft.player::addDeltaMovement);
    }

    @Override
    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket p_105018_) {
        PacketUtils.ensureRunningOnSameThread(p_105018_, this, this.minecraft);
        if (this.level.getEntity(p_105018_.getEntityId()) instanceof AbstractHorse abstracthorse) {
            LocalPlayer localplayer = this.minecraft.player;
            int i = p_105018_.getInventoryColumns();
            SimpleContainer simplecontainer = new SimpleContainer(AbstractHorse.getInventorySize(i));
            HorseInventoryMenu horseinventorymenu = new HorseInventoryMenu(p_105018_.getContainerId(), localplayer.getInventory(), simplecontainer, abstracthorse, i);
            localplayer.containerMenu = horseinventorymenu;
            this.minecraft.setScreen(new HorseInventoryScreen(horseinventorymenu, localplayer.getInventory(), abstracthorse, i));
        }
    }

    @Override
    public void handleOpenScreen(ClientboundOpenScreenPacket p_105042_) {
        PacketUtils.ensureRunningOnSameThread(p_105042_, this, this.minecraft);
        MenuScreens.create(p_105042_.getType(), this.minecraft, p_105042_.getContainerId(), p_105042_.getTitle());
    }

    @Override
    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket p_105000_) {
        PacketUtils.ensureRunningOnSameThread(p_105000_, this, this.minecraft);
        Player player = this.minecraft.player;
        ItemStack itemstack = p_105000_.getItem();
        int i = p_105000_.getSlot();
        this.minecraft.getTutorial().onGetItem(itemstack);
        boolean flag;
        if (this.minecraft.screen instanceof CreativeModeInventoryScreen creativemodeinventoryscreen) {
            flag = !creativemodeinventoryscreen.isInventoryOpen();
        } else {
            flag = false;
        }

        if (p_105000_.getContainerId() == 0) {
            if (InventoryMenu.isHotbarSlot(i) && !itemstack.isEmpty()) {
                ItemStack itemstack1 = player.inventoryMenu.getSlot(i).getItem();
                if (itemstack1.isEmpty() || itemstack1.getCount() < itemstack.getCount()) {
                    itemstack.setPopTime(5);
                }
            }

            player.inventoryMenu.setItem(i, p_105000_.getStateId(), itemstack);
        } else if (p_105000_.getContainerId() == player.containerMenu.containerId && (p_105000_.getContainerId() != 0 || !flag)) {
            player.containerMenu.setItem(i, p_105000_.getStateId(), itemstack);
        }

        if (this.minecraft.screen instanceof CreativeModeInventoryScreen) {
            player.inventoryMenu.setRemoteSlot(i, itemstack);
            player.inventoryMenu.broadcastChanges();
        }
    }

    @Override
    public void handleSetCursorItem(ClientboundSetCursorItemPacket p_369171_) {
        PacketUtils.ensureRunningOnSameThread(p_369171_, this, this.minecraft);
        this.minecraft.getTutorial().onGetItem(p_369171_.contents());
        if (!(this.minecraft.screen instanceof CreativeModeInventoryScreen)) {
            this.minecraft.player.containerMenu.setCarried(p_369171_.contents());
        }
    }

    @Override
    public void handleSetPlayerInventory(ClientboundSetPlayerInventoryPacket p_368912_) {
        PacketUtils.ensureRunningOnSameThread(p_368912_, this, this.minecraft);
        this.minecraft.getTutorial().onGetItem(p_368912_.contents());
        this.minecraft.player.getInventory().setItem(p_368912_.slot(), p_368912_.contents());
    }

    @Override
    public void handleContainerContent(ClientboundContainerSetContentPacket p_104996_) {
        PacketUtils.ensureRunningOnSameThread(p_104996_, this, this.minecraft);
        Player player = this.minecraft.player;
        if (p_104996_.getContainerId() == 0) {
            player.inventoryMenu.initializeContents(p_104996_.getStateId(), p_104996_.getItems(), p_104996_.getCarriedItem());
        } else if (p_104996_.getContainerId() == player.containerMenu.containerId) {
            player.containerMenu.initializeContents(p_104996_.getStateId(), p_104996_.getItems(), p_104996_.getCarriedItem());
        }
    }

    @Override
    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket p_105044_) {
        PacketUtils.ensureRunningOnSameThread(p_105044_, this, this.minecraft);
        BlockPos blockpos = p_105044_.getPos();
        if (this.level.getBlockEntity(blockpos) instanceof SignBlockEntity signblockentity) {
            this.minecraft.player.openTextEdit(signblockentity, p_105044_.isFrontText());
        } else {
            LOGGER.warn("Ignoring openTextEdit on an invalid entity: {} at pos {}", this.level.getBlockEntity(blockpos), blockpos);
        }
    }

    @Override
    public void handleBlockEntityData(ClientboundBlockEntityDataPacket p_104976_) {
        PacketUtils.ensureRunningOnSameThread(p_104976_, this, this.minecraft);
        BlockPos blockpos = p_104976_.getPos();
        this.minecraft.level.getBlockEntity(blockpos, p_104976_.getType()).ifPresent(p_325478_ -> {
            CompoundTag compoundtag = p_104976_.getTag();
            if (!compoundtag.isEmpty()) {
                p_325478_.loadWithComponents(compoundtag, this.registryAccess);
            }

            if (p_325478_ instanceof CommandBlockEntity && this.minecraft.screen instanceof CommandBlockEditScreen) {
                ((CommandBlockEditScreen)this.minecraft.screen).updateGui();
            }
        });
    }

    @Override
    public void handleContainerSetData(ClientboundContainerSetDataPacket p_104998_) {
        PacketUtils.ensureRunningOnSameThread(p_104998_, this, this.minecraft);
        Player player = this.minecraft.player;
        if (player.containerMenu != null && player.containerMenu.containerId == p_104998_.getContainerId()) {
            player.containerMenu.setData(p_104998_.getId(), p_104998_.getValue());
        }
    }

    @Override
    public void handleSetEquipment(ClientboundSetEquipmentPacket p_105094_) {
        PacketUtils.ensureRunningOnSameThread(p_105094_, this, this.minecraft);
        if (this.level.getEntity(p_105094_.getEntity()) instanceof LivingEntity livingentity) {
            p_105094_.getSlots().forEach(p_325480_ -> livingentity.setItemSlot(p_325480_.getFirst(), p_325480_.getSecond()));
        }
    }

    @Override
    public void handleContainerClose(ClientboundContainerClosePacket p_104994_) {
        PacketUtils.ensureRunningOnSameThread(p_104994_, this, this.minecraft);
        this.minecraft.player.clientSideCloseContainer();
    }

    @Override
    public void handleBlockEvent(ClientboundBlockEventPacket p_104978_) {
        PacketUtils.ensureRunningOnSameThread(p_104978_, this, this.minecraft);
        this.minecraft.level.blockEvent(p_104978_.getPos(), p_104978_.getBlock(), p_104978_.getB0(), p_104978_.getB1());
    }

    @Override
    public void handleBlockDestruction(ClientboundBlockDestructionPacket p_104974_) {
        PacketUtils.ensureRunningOnSameThread(p_104974_, this, this.minecraft);
        this.minecraft.level.destroyBlockProgress(p_104974_.getId(), p_104974_.getPos(), p_104974_.getProgress());
    }

    @Override
    public void handleGameEvent(ClientboundGameEventPacket p_105016_) {
        PacketUtils.ensureRunningOnSameThread(p_105016_, this, this.minecraft);
        Player player = this.minecraft.player;
        ClientboundGameEventPacket.Type clientboundgameeventpacket$type = p_105016_.getEvent();
        float f = p_105016_.getParam();
        int i = Mth.floor(f + 0.5F);
        if (clientboundgameeventpacket$type == ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE) {
            player.displayClientMessage(Component.translatable("block.minecraft.spawn.not_valid"), false);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.START_RAINING) {
            this.level.getLevelData().setRaining(true);
            this.level.setRainLevel(0.0F);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.STOP_RAINING) {
            this.level.getLevelData().setRaining(false);
            this.level.setRainLevel(1.0F);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.CHANGE_GAME_MODE) {
            this.minecraft.gameMode.setLocalMode(GameType.byId(i));
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.WIN_GAME) {
            this.minecraft.setScreen(new WinScreen(true, () -> {
                this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                this.minecraft.setScreen(null);
            }));
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.DEMO_EVENT) {
            Options options = this.minecraft.options;
            if (f == 0.0F) {
                this.minecraft.setScreen(new DemoIntroScreen());
            } else if (f == 101.0F) {
                this.minecraft
                    .gui
                    .getChat()
                    .addMessage(
                        Component.translatable(
                            "demo.help.movement",
                            options.keyUp.getTranslatedKeyMessage(),
                            options.keyLeft.getTranslatedKeyMessage(),
                            options.keyDown.getTranslatedKeyMessage(),
                            options.keyRight.getTranslatedKeyMessage()
                        )
                    );
            } else if (f == 102.0F) {
                this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.jump", options.keyJump.getTranslatedKeyMessage()));
            } else if (f == 103.0F) {
                this.minecraft.gui.getChat().addMessage(Component.translatable("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
            } else if (f == 104.0F) {
                this.minecraft.gui.getChat().addMessage(Component.translatable("demo.day.6", options.keyScreenshot.getTranslatedKeyMessage()));
            }
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.ARROW_HIT_PLAYER) {
            this.level.playSound(player, player.getX(), player.getEyeY(), player.getZ(), SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 0.45F);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.RAIN_LEVEL_CHANGE) {
            this.level.setRainLevel(f);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.THUNDER_LEVEL_CHANGE) {
            this.level.setThunderLevel(f);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.PUFFER_FISH_STING) {
            this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.PUFFER_FISH_STING, SoundSource.NEUTRAL, 1.0F, 1.0F);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.GUARDIAN_ELDER_EFFECT) {
            this.level.addParticle(ParticleTypes.ELDER_GUARDIAN, player.getX(), player.getY(), player.getZ(), 0.0, 0.0, 0.0);
            if (i == 1) {
                this.level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.IMMEDIATE_RESPAWN) {
            this.minecraft.player.setShowDeathScreen(f == 0.0F);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.LIMITED_CRAFTING) {
            this.minecraft.player.setDoLimitedCrafting(f == 1.0F);
        } else if (clientboundgameeventpacket$type == ClientboundGameEventPacket.LEVEL_CHUNKS_LOAD_START && this.levelLoadStatusManager != null) {
            this.levelLoadStatusManager.loadingPacketsReceived();
        }
    }

    private void startWaitingForNewLevel(LocalPlayer p_309620_, ClientLevel p_310146_, ReceivingLevelScreen.Reason p_328346_) {
        this.levelLoadStatusManager = new LevelLoadStatusManager(p_309620_, p_310146_, this.minecraft.levelRenderer);
        this.minecraft.setScreen(new ReceivingLevelScreen(this.levelLoadStatusManager::levelReady, p_328346_));
    }

    @Override
    public void handleMapItemData(ClientboundMapItemDataPacket p_105032_) {
        PacketUtils.ensureRunningOnSameThread(p_105032_, this, this.minecraft);
        MapId mapid = p_105032_.mapId();
        MapItemSavedData mapitemsaveddata = this.minecraft.level.getMapData(mapid);
        if (mapitemsaveddata == null) {
            mapitemsaveddata = MapItemSavedData.createForClient(p_105032_.scale(), p_105032_.locked(), this.minecraft.level.dimension());
            this.minecraft.level.overrideMapData(mapid, mapitemsaveddata);
        }

        p_105032_.applyToMap(mapitemsaveddata);
        this.minecraft.getMapTextureManager().update(mapid, mapitemsaveddata);
    }

    @Override
    public void handleLevelEvent(ClientboundLevelEventPacket p_105024_) {
        PacketUtils.ensureRunningOnSameThread(p_105024_, this, this.minecraft);
        if (p_105024_.isGlobalEvent()) {
            this.minecraft.level.globalLevelEvent(p_105024_.getType(), p_105024_.getPos(), p_105024_.getData());
        } else {
            this.minecraft.level.levelEvent(p_105024_.getType(), p_105024_.getPos(), p_105024_.getData());
        }
    }

    @Override
    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket p_105126_) {
        PacketUtils.ensureRunningOnSameThread(p_105126_, this, this.minecraft);
        this.advancements.update(p_105126_);
    }

    @Override
    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket p_105072_) {
        PacketUtils.ensureRunningOnSameThread(p_105072_, this, this.minecraft);
        ResourceLocation resourcelocation = p_105072_.getTab();
        if (resourcelocation == null) {
            this.advancements.setSelectedTab(null, false);
        } else {
            AdvancementHolder advancementholder = this.advancements.get(resourcelocation);
            this.advancements.setSelectedTab(advancementholder, false);
        }
    }

    @Override
    public void handleCommands(ClientboundCommandsPacket p_104990_) {
        PacketUtils.ensureRunningOnSameThread(p_104990_, this, this.minecraft);
        this.commands = new CommandDispatcher<>(p_104990_.getRoot(CommandBuildContext.simple(this.registryAccess, this.enabledFeatures)));
    }

    @Override
    public void handleStopSoundEvent(ClientboundStopSoundPacket p_105116_) {
        PacketUtils.ensureRunningOnSameThread(p_105116_, this, this.minecraft);
        this.minecraft.getSoundManager().stop(p_105116_.getName(), p_105116_.getSource());
    }

    @Override
    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket p_104988_) {
        PacketUtils.ensureRunningOnSameThread(p_104988_, this, this.minecraft);
        this.suggestionsProvider.completeCustomSuggestions(p_104988_.id(), p_104988_.toSuggestions());
    }

    @Override
    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket p_105132_) {
        PacketUtils.ensureRunningOnSameThread(p_105132_, this, this.minecraft);
        this.recipes = new ClientRecipeContainer(p_105132_.itemSets(), p_105132_.stonecutterRecipes());
    }

    @Override
    public void handleLookAt(ClientboundPlayerLookAtPacket p_105054_) {
        PacketUtils.ensureRunningOnSameThread(p_105054_, this, this.minecraft);
        Vec3 vec3 = p_105054_.getPosition(this.level);
        if (vec3 != null) {
            this.minecraft.player.lookAt(p_105054_.getFromAnchor(), vec3);
        }
    }

    @Override
    public void handleTagQueryPacket(ClientboundTagQueryPacket p_105120_) {
        PacketUtils.ensureRunningOnSameThread(p_105120_, this, this.minecraft);
        if (!this.debugQueryHandler.handleResponse(p_105120_.getTransactionId(), p_105120_.getTag())) {
            LOGGER.debug("Got unhandled response to tag query {}", p_105120_.getTransactionId());
        }
    }

    @Override
    public void handleAwardStats(ClientboundAwardStatsPacket p_104970_) {
        PacketUtils.ensureRunningOnSameThread(p_104970_, this, this.minecraft);

        for (Entry<Stat<?>> entry : p_104970_.stats().object2IntEntrySet()) {
            Stat<?> stat = entry.getKey();
            int i = entry.getIntValue();
            this.minecraft.player.getStats().setValue(this.minecraft.player, stat, i);
        }

        if (this.minecraft.screen instanceof StatsScreen statsscreen) {
            statsscreen.onStatsUpdated();
        }
    }

    @Override
    public void handleRecipeBookAdd(ClientboundRecipeBookAddPacket p_365432_) {
        PacketUtils.ensureRunningOnSameThread(p_365432_, this, this.minecraft);
        ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
        if (p_365432_.replace()) {
            clientrecipebook.clear();
        }

        for (ClientboundRecipeBookAddPacket.Entry clientboundrecipebookaddpacket$entry : p_365432_.entries()) {
            clientrecipebook.add(clientboundrecipebookaddpacket$entry.contents());
            if (clientboundrecipebookaddpacket$entry.highlight()) {
                clientrecipebook.addHighlight(clientboundrecipebookaddpacket$entry.contents().id());
            }

            if (clientboundrecipebookaddpacket$entry.notification()) {
                RecipeToast.addOrUpdate(this.minecraft.getToastManager(), clientboundrecipebookaddpacket$entry.contents().display());
            }
        }

        this.refreshRecipeBook(clientrecipebook);
    }

    @Override
    public void handleRecipeBookRemove(ClientboundRecipeBookRemovePacket p_364792_) {
        PacketUtils.ensureRunningOnSameThread(p_364792_, this, this.minecraft);
        ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();

        for (RecipeDisplayId recipedisplayid : p_364792_.recipes()) {
            clientrecipebook.remove(recipedisplayid);
        }

        this.refreshRecipeBook(clientrecipebook);
    }

    @Override
    public void handleRecipeBookSettings(ClientboundRecipeBookSettingsPacket p_365706_) {
        PacketUtils.ensureRunningOnSameThread(p_365706_, this, this.minecraft);
        ClientRecipeBook clientrecipebook = this.minecraft.player.getRecipeBook();
        clientrecipebook.setBookSettings(p_365706_.bookSettings());
        this.refreshRecipeBook(clientrecipebook);
    }

    private void refreshRecipeBook(ClientRecipeBook p_364029_) {
        p_364029_.rebuildCollections();
        this.searchTrees.updateRecipes(p_364029_, this.level);
        if (this.minecraft.screen instanceof RecipeUpdateListener recipeupdatelistener) {
            recipeupdatelistener.recipesUpdated();
        }
    }

    @Override
    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket p_105130_) {
        PacketUtils.ensureRunningOnSameThread(p_105130_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105130_.getEntityId());
        if (entity instanceof LivingEntity) {
            Holder<MobEffect> holder = p_105130_.getEffect();
            MobEffectInstance mobeffectinstance = new MobEffectInstance(
                holder, p_105130_.getEffectDurationTicks(), p_105130_.getEffectAmplifier(), p_105130_.isEffectAmbient(), p_105130_.isEffectVisible(), p_105130_.effectShowsIcon(), null
            );
            if (!p_105130_.shouldBlend()) {
                mobeffectinstance.skipBlending();
            }

            ((LivingEntity)entity).forceAddEffect(mobeffectinstance, null);
        }
    }

    private <T> Registry.PendingTags<T> updateTags(ResourceKey<? extends Registry<? extends T>> p_367809_, TagNetworkSerialization.NetworkPayload p_369174_) {
        Registry<T> registry = this.registryAccess.lookupOrThrow(p_367809_);
        return registry.prepareTagReload(p_369174_.resolve(registry));
    }

    @Override
    public void handleUpdateTags(ClientboundUpdateTagsPacket p_298004_) {
        PacketUtils.ensureRunningOnSameThread(p_298004_, this, this.minecraft);
        List<Registry.PendingTags<?>> list = new ArrayList<>(p_298004_.getTags().size());
        boolean flag = this.connection.isMemoryConnection();
        p_298004_.getTags().forEach((p_357782_, p_357783_) -> {
            if (!flag || RegistrySynchronization.isNetworkable((ResourceKey<? extends Registry<?>>)p_357782_)) {
                list.add(this.updateTags((ResourceKey<? extends Registry<?>>)p_357782_, p_357783_));
            }
        });
        list.forEach(Registry.PendingTags::apply);
        this.fuelValues = FuelValues.vanillaBurnTimes(this.registryAccess, this.enabledFeatures);
        List<ItemStack> list1 = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
        this.searchTrees.updateCreativeTags(list1);
    }

    @Override
    public void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket p_171771_) {
    }

    @Override
    public void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket p_171773_) {
    }

    @Override
    public void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket p_171775_) {
        PacketUtils.ensureRunningOnSameThread(p_171775_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_171775_.playerId());
        if (entity == this.minecraft.player) {
            if (this.minecraft.player.shouldShowDeathScreen()) {
                this.minecraft.setScreen(new DeathScreen(p_171775_.message(), this.level.getLevelData().isHardcore()));
            } else {
                this.minecraft.player.respawn();
            }
        }
    }

    @Override
    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket p_104984_) {
        PacketUtils.ensureRunningOnSameThread(p_104984_, this, this.minecraft);
        this.levelData.setDifficulty(p_104984_.getDifficulty());
        this.levelData.setDifficultyLocked(p_104984_.isLocked());
    }

    @Override
    public void handleSetCamera(ClientboundSetCameraPacket p_105076_) {
        PacketUtils.ensureRunningOnSameThread(p_105076_, this, this.minecraft);
        Entity entity = p_105076_.getEntity(this.level);
        if (entity != null) {
            this.minecraft.setCameraEntity(entity);
        }
    }

    @Override
    public void handleInitializeBorder(ClientboundInitializeBorderPacket p_171767_) {
        PacketUtils.ensureRunningOnSameThread(p_171767_, this, this.minecraft);
        WorldBorder worldborder = this.level.getWorldBorder();
        worldborder.setCenter(p_171767_.getNewCenterX(), p_171767_.getNewCenterZ());
        long i = p_171767_.getLerpTime();
        if (i > 0L) {
            worldborder.lerpSizeBetween(p_171767_.getOldSize(), p_171767_.getNewSize(), i);
        } else {
            worldborder.setSize(p_171767_.getNewSize());
        }

        worldborder.setAbsoluteMaxSize(p_171767_.getNewAbsoluteMaxSize());
        worldborder.setWarningBlocks(p_171767_.getWarningBlocks());
        worldborder.setWarningTime(p_171767_.getWarningTime());
    }

    @Override
    public void handleSetBorderCenter(ClientboundSetBorderCenterPacket p_171781_) {
        PacketUtils.ensureRunningOnSameThread(p_171781_, this, this.minecraft);
        this.level.getWorldBorder().setCenter(p_171781_.getNewCenterX(), p_171781_.getNewCenterZ());
    }

    @Override
    public void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket p_171783_) {
        PacketUtils.ensureRunningOnSameThread(p_171783_, this, this.minecraft);
        this.level.getWorldBorder().lerpSizeBetween(p_171783_.getOldSize(), p_171783_.getNewSize(), p_171783_.getLerpTime());
    }

    @Override
    public void handleSetBorderSize(ClientboundSetBorderSizePacket p_171785_) {
        PacketUtils.ensureRunningOnSameThread(p_171785_, this, this.minecraft);
        this.level.getWorldBorder().setSize(p_171785_.getSize());
    }

    @Override
    public void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket p_171789_) {
        PacketUtils.ensureRunningOnSameThread(p_171789_, this, this.minecraft);
        this.level.getWorldBorder().setWarningBlocks(p_171789_.getWarningBlocks());
    }

    @Override
    public void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket p_171787_) {
        PacketUtils.ensureRunningOnSameThread(p_171787_, this, this.minecraft);
        this.level.getWorldBorder().setWarningTime(p_171787_.getWarningDelay());
    }

    @Override
    public void handleTitlesClear(ClientboundClearTitlesPacket p_171765_) {
        PacketUtils.ensureRunningOnSameThread(p_171765_, this, this.minecraft);
        this.minecraft.gui.clearTitles();
        if (p_171765_.shouldResetTimes()) {
            this.minecraft.gui.resetTitleTimes();
        }
    }

    @Override
    public void handleServerData(ClientboundServerDataPacket p_233704_) {
        PacketUtils.ensureRunningOnSameThread(p_233704_, this, this.minecraft);
        if (this.serverData != null) {
            this.serverData.motd = p_233704_.motd();
            p_233704_.iconBytes().map(ServerData::validateIcon).ifPresent(this.serverData::setIconBytes);
            ServerList.saveSingleServer(this.serverData);
        }
    }

    @Override
    public void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket p_240832_) {
        PacketUtils.ensureRunningOnSameThread(p_240832_, this, this.minecraft);
        this.suggestionsProvider.modifyCustomCompletions(p_240832_.action(), p_240832_.entries());
    }

    @Override
    public void setActionBarText(ClientboundSetActionBarTextPacket p_171779_) {
        PacketUtils.ensureRunningOnSameThread(p_171779_, this, this.minecraft);
        this.minecraft.gui.setOverlayMessage(p_171779_.text(), false);
    }

    @Override
    public void setTitleText(ClientboundSetTitleTextPacket p_171793_) {
        PacketUtils.ensureRunningOnSameThread(p_171793_, this, this.minecraft);
        this.minecraft.gui.setTitle(p_171793_.text());
    }

    @Override
    public void setSubtitleText(ClientboundSetSubtitleTextPacket p_171791_) {
        PacketUtils.ensureRunningOnSameThread(p_171791_, this, this.minecraft);
        this.minecraft.gui.setSubtitle(p_171791_.text());
    }

    @Override
    public void setTitlesAnimation(ClientboundSetTitlesAnimationPacket p_171795_) {
        PacketUtils.ensureRunningOnSameThread(p_171795_, this, this.minecraft);
        this.minecraft.gui.setTimes(p_171795_.getFadeIn(), p_171795_.getStay(), p_171795_.getFadeOut());
    }

    @Override
    public void handleTabListCustomisation(ClientboundTabListPacket p_105118_) {
        PacketUtils.ensureRunningOnSameThread(p_105118_, this, this.minecraft);
        this.minecraft.gui.getTabList().setHeader(p_105118_.header().getString().isEmpty() ? null : p_105118_.header());
        this.minecraft.gui.getTabList().setFooter(p_105118_.footer().getString().isEmpty() ? null : p_105118_.footer());
    }

    @Override
    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket p_105062_) {
        PacketUtils.ensureRunningOnSameThread(p_105062_, this, this.minecraft);
        if (p_105062_.getEntity(this.level) instanceof LivingEntity livingentity) {
            livingentity.removeEffectNoUpdate(p_105062_.effect());
        }
    }

    @Override
    public void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket p_248731_) {
        PacketUtils.ensureRunningOnSameThread(p_248731_, this, this.minecraft);

        for (UUID uuid : p_248731_.profileIds()) {
            this.minecraft.getPlayerSocialManager().removePlayer(uuid);
            PlayerInfo playerinfo = this.playerInfoMap.remove(uuid);
            if (playerinfo != null) {
                this.listedPlayers.remove(playerinfo);
            }
        }
    }

    @Override
    public void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket p_250115_) {
        PacketUtils.ensureRunningOnSameThread(p_250115_, this, this.minecraft);

        for (ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket$entry : p_250115_.newEntries()) {
            PlayerInfo playerinfo = new PlayerInfo(Objects.requireNonNull(clientboundplayerinfoupdatepacket$entry.profile()), this.enforcesSecureChat());
            if (this.playerInfoMap.putIfAbsent(clientboundplayerinfoupdatepacket$entry.profileId(), playerinfo) == null) {
                this.minecraft.getPlayerSocialManager().addPlayer(playerinfo);
            }
        }

        for (ClientboundPlayerInfoUpdatePacket.Entry clientboundplayerinfoupdatepacket$entry1 : p_250115_.entries()) {
            PlayerInfo playerinfo1 = this.playerInfoMap.get(clientboundplayerinfoupdatepacket$entry1.profileId());
            if (playerinfo1 == null) {
                LOGGER.warn(
                    "Ignoring player info update for unknown player {} ({})", clientboundplayerinfoupdatepacket$entry1.profileId(), p_250115_.actions()
                );
            } else {
                for (ClientboundPlayerInfoUpdatePacket.Action clientboundplayerinfoupdatepacket$action : p_250115_.actions()) {
                    this.applyPlayerInfoUpdate(clientboundplayerinfoupdatepacket$action, clientboundplayerinfoupdatepacket$entry1, playerinfo1);
                }
            }
        }
    }

    private void applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action p_248954_, ClientboundPlayerInfoUpdatePacket.Entry p_251310_, PlayerInfo p_251146_) {
        switch (p_248954_) {
            case INITIALIZE_CHAT:
                this.initializeChatSession(p_251310_, p_251146_);
                break;
            case UPDATE_GAME_MODE:
                if (p_251146_.getGameMode() != p_251310_.gameMode()
                    && this.minecraft.player != null
                    && this.minecraft.player.getUUID().equals(p_251310_.profileId())) {
                    this.minecraft.player.onGameModeChanged(p_251310_.gameMode());
                }

                p_251146_.setGameMode(p_251310_.gameMode());
                break;
            case UPDATE_LISTED:
                if (p_251310_.listed()) {
                    this.listedPlayers.add(p_251146_);
                } else {
                    this.listedPlayers.remove(p_251146_);
                }
                break;
            case UPDATE_LATENCY:
                p_251146_.setLatency(p_251310_.latency());
                break;
            case UPDATE_DISPLAY_NAME:
                p_251146_.setTabListDisplayName(p_251310_.displayName());
                break;
            case UPDATE_HAT:
                p_251146_.setShowHat(p_251310_.showHat());
                break;
            case UPDATE_LIST_ORDER:
                p_251146_.setTabListOrder(p_251310_.listOrder());
        }
    }

    private void initializeChatSession(ClientboundPlayerInfoUpdatePacket.Entry p_248806_, PlayerInfo p_251136_) {
        GameProfile gameprofile = p_251136_.getProfile();
        SignatureValidator signaturevalidator = this.minecraft.getProfileKeySignatureValidator();
        if (signaturevalidator == null) {
            LOGGER.warn("Ignoring chat session from {} due to missing Services public key", gameprofile.getName());
            p_251136_.clearChatSession(this.enforcesSecureChat());
        } else {
            RemoteChatSession.Data remotechatsession$data = p_248806_.chatSession();
            if (remotechatsession$data != null) {
                try {
                    RemoteChatSession remotechatsession = remotechatsession$data.validate(gameprofile, signaturevalidator);
                    p_251136_.setChatSession(remotechatsession);
                } catch (ProfilePublicKey.ValidationException profilepublickey$validationexception) {
                    LOGGER.error("Failed to validate profile key for player: '{}'", gameprofile.getName(), profilepublickey$validationexception);
                    p_251136_.clearChatSession(this.enforcesSecureChat());
                }
            } else {
                p_251136_.clearChatSession(this.enforcesSecureChat());
            }
        }
    }

    private boolean enforcesSecureChat() {
        return this.minecraft.canValidateProfileKeys() && this.serverEnforcesSecureChat;
    }

    @Override
    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket p_105048_) {
        PacketUtils.ensureRunningOnSameThread(p_105048_, this, this.minecraft);
        Player player = this.minecraft.player;
        player.getAbilities().flying = p_105048_.isFlying();
        player.getAbilities().instabuild = p_105048_.canInstabuild();
        player.getAbilities().invulnerable = p_105048_.isInvulnerable();
        player.getAbilities().mayfly = p_105048_.canFly();
        player.getAbilities().setFlyingSpeed(p_105048_.getFlyingSpeed());
        player.getAbilities().setWalkingSpeed(p_105048_.getWalkingSpeed());
    }

    @Override
    public void handleSoundEvent(ClientboundSoundPacket p_105114_) {
        PacketUtils.ensureRunningOnSameThread(p_105114_, this, this.minecraft);
        this.minecraft
            .level
            .playSeededSound(
                this.minecraft.player,
                p_105114_.getX(),
                p_105114_.getY(),
                p_105114_.getZ(),
                p_105114_.getSound(),
                p_105114_.getSource(),
                p_105114_.getVolume(),
                p_105114_.getPitch(),
                p_105114_.getSeed()
            );
    }

    @Override
    public void handleSoundEntityEvent(ClientboundSoundEntityPacket p_105112_) {
        PacketUtils.ensureRunningOnSameThread(p_105112_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105112_.getId());
        if (entity != null) {
            this.minecraft
                .level
                .playSeededSound(
                    this.minecraft.player,
                    entity,
                    p_105112_.getSound(),
                    p_105112_.getSource(),
                    p_105112_.getVolume(),
                    p_105112_.getPitch(),
                    p_105112_.getSeed()
                );
        }
    }

    @Override
    public void handleBossUpdate(ClientboundBossEventPacket p_104982_) {
        PacketUtils.ensureRunningOnSameThread(p_104982_, this, this.minecraft);
        this.minecraft.gui.getBossOverlay().update(p_104982_);
    }

    @Override
    public void handleItemCooldown(ClientboundCooldownPacket p_105002_) {
        PacketUtils.ensureRunningOnSameThread(p_105002_, this, this.minecraft);
        if (p_105002_.duration() == 0) {
            this.minecraft.player.getCooldowns().removeCooldown(p_105002_.cooldownGroup());
        } else {
            this.minecraft.player.getCooldowns().addCooldown(p_105002_.cooldownGroup(), p_105002_.duration());
        }
    }

    @Override
    public void handleMoveVehicle(ClientboundMoveVehiclePacket p_105038_) {
        PacketUtils.ensureRunningOnSameThread(p_105038_, this, this.minecraft);
        Entity entity = this.minecraft.player.getRootVehicle();
        if (entity != this.minecraft.player && entity.isControlledByLocalInstance()) {
            Vec3 vec3 = p_105038_.position();
            Vec3 vec31 = new Vec3(entity.lerpTargetX(), entity.lerpTargetY(), entity.lerpTargetZ());
            if (vec3.distanceTo(vec31) > 1.0E-5F) {
                entity.cancelLerp();
                entity.absMoveTo(vec3.x(), vec3.y(), vec3.z(), p_105038_.yRot(), p_105038_.xRot());
            }

            this.connection.send(ServerboundMoveVehiclePacket.fromEntity(entity));
        }
    }

    @Override
    public void handleOpenBook(ClientboundOpenBookPacket p_105040_) {
        PacketUtils.ensureRunningOnSameThread(p_105040_, this, this.minecraft);
        ItemStack itemstack = this.minecraft.player.getItemInHand(p_105040_.getHand());
        BookViewScreen.BookAccess bookviewscreen$bookaccess = BookViewScreen.BookAccess.fromItem(itemstack);
        if (bookviewscreen$bookaccess != null) {
            this.minecraft.setScreen(new BookViewScreen(bookviewscreen$bookaccess));
        }
    }

    @Override
    public void handleCustomPayload(CustomPacketPayload p_300286_) {
        if (p_300286_ instanceof PathfindingDebugPayload pathfindingdebugpayload) {
            this.minecraft
                .debugRenderer
                .pathfindingRenderer
                .addPath(pathfindingdebugpayload.entityId(), pathfindingdebugpayload.path(), pathfindingdebugpayload.maxNodeDistance());
        } else if (p_300286_ instanceof NeighborUpdatesDebugPayload neighborupdatesdebugpayload) {
            this.minecraft.debugRenderer.neighborsUpdateRenderer.addUpdate(neighborupdatesdebugpayload.time(), neighborupdatesdebugpayload.pos());
        } else if (p_300286_ instanceof RedstoneWireOrientationsDebugPayload redstonewireorientationsdebugpayload) {
            this.minecraft.debugRenderer.redstoneWireOrientationsRenderer.addWireOrientations(redstonewireorientationsdebugpayload);
        } else if (p_300286_ instanceof StructuresDebugPayload structuresdebugpayload) {
            this.minecraft
                .debugRenderer
                .structureRenderer
                .addBoundingBox(structuresdebugpayload.mainBB(), structuresdebugpayload.pieces(), structuresdebugpayload.dimension());
        } else if (p_300286_ instanceof WorldGenAttemptDebugPayload worldgenattemptdebugpayload) {
            ((WorldGenAttemptRenderer)this.minecraft.debugRenderer.worldGenAttemptRenderer)
                .addPos(
                    worldgenattemptdebugpayload.pos(),
                    worldgenattemptdebugpayload.scale(),
                    worldgenattemptdebugpayload.red(),
                    worldgenattemptdebugpayload.green(),
                    worldgenattemptdebugpayload.blue(),
                    worldgenattemptdebugpayload.alpha()
                );
        } else if (p_300286_ instanceof PoiTicketCountDebugPayload poiticketcountdebugpayload) {
            this.minecraft.debugRenderer.brainDebugRenderer.setFreeTicketCount(poiticketcountdebugpayload.pos(), poiticketcountdebugpayload.freeTicketCount());
        } else if (p_300286_ instanceof PoiAddedDebugPayload poiaddeddebugpayload) {
            BrainDebugRenderer.PoiInfo braindebugrenderer$poiinfo = new BrainDebugRenderer.PoiInfo(
                poiaddeddebugpayload.pos(), poiaddeddebugpayload.poiType(), poiaddeddebugpayload.freeTicketCount()
            );
            this.minecraft.debugRenderer.brainDebugRenderer.addPoi(braindebugrenderer$poiinfo);
        } else if (p_300286_ instanceof PoiRemovedDebugPayload poiremoveddebugpayload) {
            this.minecraft.debugRenderer.brainDebugRenderer.removePoi(poiremoveddebugpayload.pos());
        } else if (p_300286_ instanceof VillageSectionsDebugPayload villagesectionsdebugpayload) {
            VillageSectionsDebugRenderer villagesectionsdebugrenderer = this.minecraft.debugRenderer.villageSectionsDebugRenderer;
            villagesectionsdebugpayload.villageChunks().forEach(villagesectionsdebugrenderer::setVillageSection);
            villagesectionsdebugpayload.notVillageChunks().forEach(villagesectionsdebugrenderer::setNotVillageSection);
        } else if (p_300286_ instanceof GoalDebugPayload goaldebugpayload) {
            this.minecraft.debugRenderer.goalSelectorRenderer.addGoalSelector(goaldebugpayload.entityId(), goaldebugpayload.pos(), goaldebugpayload.goals());
        } else if (p_300286_ instanceof BrainDebugPayload braindebugpayload) {
            this.minecraft.debugRenderer.brainDebugRenderer.addOrUpdateBrainDump(braindebugpayload.brainDump());
        } else if (p_300286_ instanceof BeeDebugPayload beedebugpayload) {
            this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateBeeInfo(beedebugpayload.beeInfo());
        } else if (p_300286_ instanceof HiveDebugPayload hivedebugpayload) {
            this.minecraft.debugRenderer.beeDebugRenderer.addOrUpdateHiveInfo(hivedebugpayload.hiveInfo(), this.level.getGameTime());
        } else if (p_300286_ instanceof GameTestAddMarkerDebugPayload gametestaddmarkerdebugpayload) {
            this.minecraft
                .debugRenderer
                .gameTestDebugRenderer
                .addMarker(
                    gametestaddmarkerdebugpayload.pos(),
                    gametestaddmarkerdebugpayload.color(),
                    gametestaddmarkerdebugpayload.text(),
                    gametestaddmarkerdebugpayload.durationMs()
                );
        } else if (p_300286_ instanceof GameTestClearMarkersDebugPayload) {
            this.minecraft.debugRenderer.gameTestDebugRenderer.clear();
        } else if (p_300286_ instanceof RaidsDebugPayload raidsdebugpayload) {
            this.minecraft.debugRenderer.raidDebugRenderer.setRaidCenters(raidsdebugpayload.raidCenters());
        } else if (p_300286_ instanceof GameEventDebugPayload gameeventdebugpayload) {
            this.minecraft.debugRenderer.gameEventListenerRenderer.trackGameEvent(gameeventdebugpayload.gameEventType(), gameeventdebugpayload.pos());
        } else if (p_300286_ instanceof GameEventListenerDebugPayload gameeventlistenerdebugpayload) {
            this.minecraft.debugRenderer.gameEventListenerRenderer.trackListener(gameeventlistenerdebugpayload.listenerPos(), gameeventlistenerdebugpayload.listenerRange());
        } else if (p_300286_ instanceof BreezeDebugPayload breezedebugpayload) {
            this.minecraft.debugRenderer.breezeDebugRenderer.add(breezedebugpayload.breezeInfo());
        } else {
            this.handleUnknownCustomPayload(p_300286_);
        }
    }

    private void handleUnknownCustomPayload(CustomPacketPayload p_301051_) {
        LOGGER.warn("Unknown custom packet payload: {}", p_301051_.type().id());
    }

    @Override
    public void handleAddObjective(ClientboundSetObjectivePacket p_105100_) {
        PacketUtils.ensureRunningOnSameThread(p_105100_, this, this.minecraft);
        String s = p_105100_.getObjectiveName();
        if (p_105100_.getMethod() == 0) {
            this.scoreboard.addObjective(s, ObjectiveCriteria.DUMMY, p_105100_.getDisplayName(), p_105100_.getRenderType(), false, p_105100_.getNumberFormat().orElse(null));
        } else {
            Objective objective = this.scoreboard.getObjective(s);
            if (objective != null) {
                if (p_105100_.getMethod() == 1) {
                    this.scoreboard.removeObjective(objective);
                } else if (p_105100_.getMethod() == 2) {
                    objective.setRenderType(p_105100_.getRenderType());
                    objective.setDisplayName(p_105100_.getDisplayName());
                    objective.setNumberFormat(p_105100_.getNumberFormat().orElse(null));
                }
            }
        }
    }

    @Override
    public void handleSetScore(ClientboundSetScorePacket p_105106_) {
        PacketUtils.ensureRunningOnSameThread(p_105106_, this, this.minecraft);
        String s = p_105106_.objectiveName();
        ScoreHolder scoreholder = ScoreHolder.forNameOnly(p_105106_.owner());
        Objective objective = this.scoreboard.getObjective(s);
        if (objective != null) {
            ScoreAccess scoreaccess = this.scoreboard.getOrCreatePlayerScore(scoreholder, objective, true);
            scoreaccess.set(p_105106_.score());
            scoreaccess.display(p_105106_.display().orElse(null));
            scoreaccess.numberFormatOverride(p_105106_.numberFormat().orElse(null));
        } else {
            LOGGER.warn("Received packet for unknown scoreboard objective: {}", s);
        }
    }

    @Override
    public void handleResetScore(ClientboundResetScorePacket p_312811_) {
        PacketUtils.ensureRunningOnSameThread(p_312811_, this, this.minecraft);
        String s = p_312811_.objectiveName();
        ScoreHolder scoreholder = ScoreHolder.forNameOnly(p_312811_.owner());
        if (s == null) {
            this.scoreboard.resetAllPlayerScores(scoreholder);
        } else {
            Objective objective = this.scoreboard.getObjective(s);
            if (objective != null) {
                this.scoreboard.resetSinglePlayerScore(scoreholder, objective);
            } else {
                LOGGER.warn("Received packet for unknown scoreboard objective: {}", s);
            }
        }
    }

    @Override
    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket p_105086_) {
        PacketUtils.ensureRunningOnSameThread(p_105086_, this, this.minecraft);
        String s = p_105086_.getObjectiveName();
        Objective objective = s == null ? null : this.scoreboard.getObjective(s);
        this.scoreboard.setDisplayObjective(p_105086_.getSlot(), objective);
    }

    @Override
    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket p_105104_) {
        PacketUtils.ensureRunningOnSameThread(p_105104_, this, this.minecraft);
        ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action = p_105104_.getTeamAction();
        PlayerTeam playerteam;
        if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.ADD) {
            playerteam = this.scoreboard.addPlayerTeam(p_105104_.getName());
        } else {
            playerteam = this.scoreboard.getPlayerTeam(p_105104_.getName());
            if (playerteam == null) {
                LOGGER.warn(
                    "Received packet for unknown team {}: team action: {}, player action: {}",
                    p_105104_.getName(),
                    p_105104_.getTeamAction(),
                    p_105104_.getPlayerAction()
                );
                return;
            }
        }

        Optional<ClientboundSetPlayerTeamPacket.Parameters> optional = p_105104_.getParameters();
        optional.ifPresent(p_233670_ -> {
            playerteam.setDisplayName(p_233670_.getDisplayName());
            playerteam.setColor(p_233670_.getColor());
            playerteam.unpackOptions(p_233670_.getOptions());
            Team.Visibility team$visibility = Team.Visibility.byName(p_233670_.getNametagVisibility());
            if (team$visibility != null) {
                playerteam.setNameTagVisibility(team$visibility);
            }

            Team.CollisionRule team$collisionrule = Team.CollisionRule.byName(p_233670_.getCollisionRule());
            if (team$collisionrule != null) {
                playerteam.setCollisionRule(team$collisionrule);
            }

            playerteam.setPlayerPrefix(p_233670_.getPlayerPrefix());
            playerteam.setPlayerSuffix(p_233670_.getPlayerSuffix());
        });
        ClientboundSetPlayerTeamPacket.Action clientboundsetplayerteampacket$action1 = p_105104_.getPlayerAction();
        if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.ADD) {
            for (String s : p_105104_.getPlayers()) {
                this.scoreboard.addPlayerToTeam(s, playerteam);
            }
        } else if (clientboundsetplayerteampacket$action1 == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            for (String s1 : p_105104_.getPlayers()) {
                this.scoreboard.removePlayerFromTeam(s1, playerteam);
            }
        }

        if (clientboundsetplayerteampacket$action == ClientboundSetPlayerTeamPacket.Action.REMOVE) {
            this.scoreboard.removePlayerTeam(playerteam);
        }
    }

    @Override
    public void handleParticleEvent(ClientboundLevelParticlesPacket p_105026_) {
        PacketUtils.ensureRunningOnSameThread(p_105026_, this, this.minecraft);
        if (p_105026_.getCount() == 0) {
            double d0 = (double)(p_105026_.getMaxSpeed() * p_105026_.getXDist());
            double d2 = (double)(p_105026_.getMaxSpeed() * p_105026_.getYDist());
            double d4 = (double)(p_105026_.getMaxSpeed() * p_105026_.getZDist());

            try {
                this.level
                    .addParticle(
                        p_105026_.getParticle(),
                        p_105026_.isOverrideLimiter(),
                        p_105026_.alwaysShow(),
                        p_105026_.getX(),
                        p_105026_.getY(),
                        p_105026_.getZ(),
                        d0,
                        d2,
                        d4
                    );
            } catch (Throwable throwable1) {
                LOGGER.warn("Could not spawn particle effect {}", p_105026_.getParticle());
            }
        } else {
            for (int i = 0; i < p_105026_.getCount(); i++) {
                double d1 = this.random.nextGaussian() * (double)p_105026_.getXDist();
                double d3 = this.random.nextGaussian() * (double)p_105026_.getYDist();
                double d5 = this.random.nextGaussian() * (double)p_105026_.getZDist();
                double d6 = this.random.nextGaussian() * (double)p_105026_.getMaxSpeed();
                double d7 = this.random.nextGaussian() * (double)p_105026_.getMaxSpeed();
                double d8 = this.random.nextGaussian() * (double)p_105026_.getMaxSpeed();

                try {
                    this.level
                        .addParticle(
                            p_105026_.getParticle(),
                            p_105026_.isOverrideLimiter(),
                            p_105026_.alwaysShow(),
                            p_105026_.getX() + d1,
                            p_105026_.getY() + d3,
                            p_105026_.getZ() + d5,
                            d6,
                            d7,
                            d8
                        );
                } catch (Throwable throwable) {
                    LOGGER.warn("Could not spawn particle effect {}", p_105026_.getParticle());
                    return;
                }
            }
        }
    }

    @Override
    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket p_105128_) {
        PacketUtils.ensureRunningOnSameThread(p_105128_, this, this.minecraft);
        Entity entity = this.level.getEntity(p_105128_.getEntityId());
        if (entity != null) {
            if (!(entity instanceof LivingEntity)) {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            } else {
                AttributeMap attributemap = ((LivingEntity)entity).getAttributes();

                for (ClientboundUpdateAttributesPacket.AttributeSnapshot clientboundupdateattributespacket$attributesnapshot : p_105128_.getValues()) {
                    AttributeInstance attributeinstance = attributemap.getInstance(clientboundupdateattributespacket$attributesnapshot.attribute());
                    if (attributeinstance == null) {
                        LOGGER.warn(
                            "Entity {} does not have attribute {}", entity, clientboundupdateattributespacket$attributesnapshot.attribute().getRegisteredName()
                        );
                    } else {
                        attributeinstance.setBaseValue(clientboundupdateattributespacket$attributesnapshot.base());
                        attributeinstance.removeModifiers();

                        for (AttributeModifier attributemodifier : clientboundupdateattributespacket$attributesnapshot.modifiers()) {
                            attributeinstance.addTransientModifier(attributemodifier);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket p_105046_) {
        PacketUtils.ensureRunningOnSameThread(p_105046_, this, this.minecraft);
        AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;
        if (abstractcontainermenu.containerId == p_105046_.containerId()) {
            if (this.minecraft.screen instanceof RecipeUpdateListener recipeupdatelistener) {
                recipeupdatelistener.fillGhostRecipe(p_105046_.recipeDisplay());
            }
        }
    }

    @Override
    public void handleLightUpdatePacket(ClientboundLightUpdatePacket p_194243_) {
        PacketUtils.ensureRunningOnSameThread(p_194243_, this, this.minecraft);
        int i = p_194243_.getX();
        int j = p_194243_.getZ();
        ClientboundLightUpdatePacketData clientboundlightupdatepacketdata = p_194243_.getLightData();
        this.level.queueLightUpdate(() -> this.applyLightData(i, j, clientboundlightupdatepacketdata, true));
    }

    private void applyLightData(int p_194249_, int p_194250_, ClientboundLightUpdatePacketData p_194251_, boolean p_365523_) {
        LevelLightEngine levellightengine = this.level.getChunkSource().getLightEngine();
        BitSet bitset = p_194251_.getSkyYMask();
        BitSet bitset1 = p_194251_.getEmptySkyYMask();
        Iterator<byte[]> iterator = p_194251_.getSkyUpdates().iterator();
        this.readSectionList(p_194249_, p_194250_, levellightengine, LightLayer.SKY, bitset, bitset1, iterator, p_365523_);
        BitSet bitset2 = p_194251_.getBlockYMask();
        BitSet bitset3 = p_194251_.getEmptyBlockYMask();
        Iterator<byte[]> iterator1 = p_194251_.getBlockUpdates().iterator();
        this.readSectionList(p_194249_, p_194250_, levellightengine, LightLayer.BLOCK, bitset2, bitset3, iterator1, p_365523_);
        levellightengine.setLightEnabled(new ChunkPos(p_194249_, p_194250_), true);
    }

    @Override
    public void handleMerchantOffers(ClientboundMerchantOffersPacket p_105034_) {
        PacketUtils.ensureRunningOnSameThread(p_105034_, this, this.minecraft);
        AbstractContainerMenu abstractcontainermenu = this.minecraft.player.containerMenu;
        if (p_105034_.getContainerId() == abstractcontainermenu.containerId && abstractcontainermenu instanceof MerchantMenu merchantmenu) {
            merchantmenu.setOffers(p_105034_.getOffers());
            merchantmenu.setXp(p_105034_.getVillagerXp());
            merchantmenu.setMerchantLevel(p_105034_.getVillagerLevel());
            merchantmenu.setShowProgressBar(p_105034_.showProgress());
            merchantmenu.setCanRestock(p_105034_.canRestock());
        }
    }

    @Override
    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket p_105082_) {
        PacketUtils.ensureRunningOnSameThread(p_105082_, this, this.minecraft);
        this.serverChunkRadius = p_105082_.getRadius();
        this.minecraft.options.setServerRenderDistance(this.serverChunkRadius);
        this.level.getChunkSource().updateViewRadius(p_105082_.getRadius());
    }

    @Override
    public void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket p_194245_) {
        PacketUtils.ensureRunningOnSameThread(p_194245_, this, this.minecraft);
        this.serverSimulationDistance = p_194245_.simulationDistance();
        this.level.setServerSimulationDistance(this.serverSimulationDistance);
    }

    @Override
    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket p_105080_) {
        PacketUtils.ensureRunningOnSameThread(p_105080_, this, this.minecraft);
        this.level.getChunkSource().updateViewCenter(p_105080_.getX(), p_105080_.getZ());
    }

    @Override
    public void handleBlockChangedAck(ClientboundBlockChangedAckPacket p_233698_) {
        PacketUtils.ensureRunningOnSameThread(p_233698_, this, this.minecraft);
        this.level.handleBlockChangedAck(p_233698_.sequence());
    }

    @Override
    public void handleBundlePacket(ClientboundBundlePacket p_265195_) {
        PacketUtils.ensureRunningOnSameThread(p_265195_, this, this.minecraft);

        for (Packet<? super ClientGamePacketListener> packet : p_265195_.subPackets()) {
            packet.handle(this);
        }
    }

    @Override
    public void handleProjectilePowerPacket(ClientboundProjectilePowerPacket p_330827_) {
        PacketUtils.ensureRunningOnSameThread(p_330827_, this, this.minecraft);
        if (this.level.getEntity(p_330827_.getId()) instanceof AbstractHurtingProjectile abstracthurtingprojectile) {
            abstracthurtingprojectile.accelerationPower = p_330827_.getAccelerationPower();
        }
    }

    @Override
    public void handleChunkBatchStart(ClientboundChunkBatchStartPacket p_297740_) {
        this.chunkBatchSizeCalculator.onBatchStart();
    }

    @Override
    public void handleChunkBatchFinished(ClientboundChunkBatchFinishedPacket p_300262_) {
        this.chunkBatchSizeCalculator.onBatchFinished(p_300262_.batchSize());
        this.send(new ServerboundChunkBatchReceivedPacket(this.chunkBatchSizeCalculator.getDesiredChunksPerTick()));
    }

    @Override
    public void handleDebugSample(ClientboundDebugSamplePacket p_333240_) {
        this.minecraft.getDebugOverlay().logRemoteSample(p_333240_.sample(), p_333240_.debugSampleType());
    }

    @Override
    public void handlePongResponse(ClientboundPongResponsePacket p_329147_) {
        this.pingDebugMonitor.onPongReceived(p_329147_);
    }

    private void readSectionList(
        int p_171735_,
        int p_171736_,
        LevelLightEngine p_171737_,
        LightLayer p_171738_,
        BitSet p_171739_,
        BitSet p_171740_,
        Iterator<byte[]> p_171741_,
        boolean p_369857_
    ) {
        for (int i = 0; i < p_171737_.getLightSectionCount(); i++) {
            int j = p_171737_.getMinLightSection() + i;
            boolean flag = p_171739_.get(i);
            boolean flag1 = p_171740_.get(i);
            if (flag || flag1) {
                p_171737_.queueSectionData(
                    p_171738_, SectionPos.of(p_171735_, j, p_171736_), flag ? new DataLayer((byte[])p_171741_.next().clone()) : new DataLayer()
                );
                if (p_369857_) {
                    this.level.setSectionDirtyWithNeighbors(p_171735_, j, p_171736_);
                }
            }
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected() && !this.closed;
    }

    public Collection<PlayerInfo> getListedOnlinePlayers() {
        return this.listedPlayers;
    }

    public Collection<PlayerInfo> getOnlinePlayers() {
        return this.playerInfoMap.values();
    }

    public Collection<UUID> getOnlinePlayerIds() {
        return this.playerInfoMap.keySet();
    }

    @Nullable
    public PlayerInfo getPlayerInfo(UUID p_104950_) {
        return this.playerInfoMap.get(p_104950_);
    }

    @Nullable
    public PlayerInfo getPlayerInfo(String p_104939_) {
        for (PlayerInfo playerinfo : this.playerInfoMap.values()) {
            if (playerinfo.getProfile().getName().equals(p_104939_)) {
                return playerinfo;
            }
        }

        return null;
    }

    public GameProfile getLocalGameProfile() {
        return this.localGameProfile;
    }

    public ClientAdvancements getAdvancements() {
        return this.advancements;
    }

    public CommandDispatcher<SharedSuggestionProvider> getCommands() {
        return this.commands;
    }

    public ClientLevel getLevel() {
        return this.level;
    }

    public DebugQueryHandler getDebugQueryHandler() {
        return this.debugQueryHandler;
    }

    public UUID getId() {
        return this.id;
    }

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
    }

    public RegistryAccess.Frozen registryAccess() {
        return this.registryAccess;
    }

    public void markMessageAsProcessed(PlayerChatMessage p_242356_, boolean p_242455_) {
        MessageSignature messagesignature = p_242356_.signature();
        if (messagesignature != null && this.lastSeenMessages.addPending(messagesignature, p_242455_) && this.lastSeenMessages.offset() > 64) {
            this.sendChatAcknowledgement();
        }
    }

    private void sendChatAcknowledgement() {
        int i = this.lastSeenMessages.getAndClearOffset();
        if (i > 0) {
            this.send(new ServerboundChatAckPacket(i));
        }
    }

    public void sendChat(String p_249888_) {
        Instant instant = Instant.now();
        long i = Crypt.SaltSupplier.getLong();
        LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
        MessageSignature messagesignature = this.signedMessageEncoder.pack(new SignedMessageBody(p_249888_, instant, i, lastseenmessagestracker$update.lastSeen()));
        this.send(new ServerboundChatPacket(p_249888_, instant, i, messagesignature, lastseenmessagestracker$update.update()));
    }

    public void sendCommand(String p_250092_) {
        SignableCommand<SharedSuggestionProvider> signablecommand = SignableCommand.of(this.parseCommand(p_250092_));
        if (signablecommand.arguments().isEmpty()) {
            this.send(new ServerboundChatCommandPacket(p_250092_));
        } else {
            Instant instant = Instant.now();
            long i = Crypt.SaltSupplier.getLong();
            LastSeenMessagesTracker.Update lastseenmessagestracker$update = this.lastSeenMessages.generateAndApplyUpdate();
            ArgumentSignatures argumentsignatures = ArgumentSignatures.signCommand(signablecommand, p_247875_ -> {
                SignedMessageBody signedmessagebody = new SignedMessageBody(p_247875_, instant, i, lastseenmessagestracker$update.lastSeen());
                return this.signedMessageEncoder.pack(signedmessagebody);
            });
            this.send(new ServerboundChatCommandSignedPacket(p_250092_, instant, i, argumentsignatures, lastseenmessagestracker$update.update()));
        }
    }

    public boolean sendUnsignedCommand(String p_251509_) {
        if (!SignableCommand.hasSignableArguments(this.parseCommand(p_251509_))) {
            this.send(new ServerboundChatCommandPacket(p_251509_));
            return true;
        } else {
            return false;
        }
    }

    private ParseResults<SharedSuggestionProvider> parseCommand(String p_249982_) {
        return this.commands.parse(p_249982_, this.suggestionsProvider);
    }

    public void broadcastClientInformation(ClientInformation p_368886_) {
        if (!p_368886_.equals(this.remoteClientInformation)) {
            this.send(new ServerboundClientInformationPacket(p_368886_));
            this.remoteClientInformation = p_368886_;
        }
    }

    @Override
    public void tick() {
        if (this.chatSession != null && this.minecraft.getProfileKeyPairManager().shouldRefreshKeyPair()) {
            this.prepareKeyPair();
        }

        if (this.keyPairFuture != null && this.keyPairFuture.isDone()) {
            this.keyPairFuture.join().ifPresent(this::setKeyPair);
            this.keyPairFuture = null;
        }

        this.sendDeferredPackets();
        if (this.minecraft.getDebugOverlay().showNetworkCharts()) {
            this.pingDebugMonitor.tick();
        }

        this.debugSampleSubscriber.tick();
        this.telemetryManager.tick();
        if (this.levelLoadStatusManager != null) {
            this.levelLoadStatusManager.tick();
            if (this.levelLoadStatusManager.levelReady() && !this.minecraft.player.hasClientLoaded()) {
                this.connection.send(new ServerboundPlayerLoadedPacket());
                this.minecraft.player.setClientLoaded(true);
            }
        }
    }

    public void prepareKeyPair() {
        this.keyPairFuture = this.minecraft.getProfileKeyPairManager().prepareKeyPair();
    }

    private void setKeyPair(ProfileKeyPair p_261475_) {
        if (this.minecraft.isLocalPlayer(this.localGameProfile.getId())) {
            if (this.chatSession == null || !this.chatSession.keyPair().equals(p_261475_)) {
                this.chatSession = LocalChatSession.create(p_261475_);
                this.signedMessageEncoder = this.chatSession.createMessageEncoder(this.localGameProfile.getId());
                this.send(new ServerboundChatSessionUpdatePacket(this.chatSession.asRemote().asData()));
            }
        }
    }

    @Nullable
    public ServerData getServerData() {
        return this.serverData;
    }

    public FeatureFlagSet enabledFeatures() {
        return this.enabledFeatures;
    }

    public boolean isFeatureEnabled(FeatureFlagSet p_250605_) {
        return p_250605_.isSubsetOf(this.enabledFeatures());
    }

    public Scoreboard scoreboard() {
        return this.scoreboard;
    }

    public PotionBrewing potionBrewing() {
        return this.potionBrewing;
    }

    public FuelValues fuelValues() {
        return this.fuelValues;
    }

    public void updateSearchTrees() {
        this.searchTrees.rebuildAfterLanguageChange();
    }

    public SessionSearchTrees searchTrees() {
        return this.searchTrees;
    }

    public ServerLinks serverLinks() {
        return this.serverLinks;
    }
}
