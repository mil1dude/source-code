package com.mojang.realmsclient.client;

import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvite;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsNotification;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.ServerActivityList;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.UploadTokenCache;
import com.mojang.util.UndashedUuid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsClient {
    public static final RealmsClient.Environment ENVIRONMENT = Optional.ofNullable(System.getenv("realms.environment"))
        .or(() -> Optional.ofNullable(System.getProperty("realms.environment")))
        .flatMap(RealmsClient.Environment::byName)
        .orElse(RealmsClient.Environment.PRODUCTION);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String sessionId;
    private final String username;
    private final Minecraft minecraft;
    private static final String WORLDS_RESOURCE_PATH = "worlds";
    private static final String INVITES_RESOURCE_PATH = "invites";
    private static final String MCO_RESOURCE_PATH = "mco";
    private static final String SUBSCRIPTION_RESOURCE = "subscriptions";
    private static final String ACTIVITIES_RESOURCE = "activities";
    private static final String OPS_RESOURCE = "ops";
    private static final String REGIONS_RESOURCE = "regions/ping/stat";
    private static final String TRIALS_RESOURCE = "trial";
    private static final String NOTIFICATIONS_RESOURCE = "notifications";
    private static final String PATH_LIST_ALL_REALMS = "/listUserWorldsOfType/any";
    private static final String PATH_CREATE_SNAPSHOT_REALM = "/$PARENT_WORLD_ID/createPrereleaseRealm";
    private static final String PATH_SNAPSHOT_ELIGIBLE_REALMS = "/listPrereleaseEligibleWorlds";
    private static final String PATH_INITIALIZE = "/$WORLD_ID/initialize";
    private static final String PATH_GET_ACTIVTIES = "/$WORLD_ID";
    private static final String PATH_GET_LIVESTATS = "/liveplayerlist";
    private static final String PATH_GET_SUBSCRIPTION = "/$WORLD_ID";
    private static final String PATH_OP = "/$WORLD_ID/$PROFILE_UUID";
    private static final String PATH_PUT_INTO_MINIGAMES_MODE = "/minigames/$MINIGAME_ID/$WORLD_ID";
    private static final String PATH_AVAILABLE = "/available";
    private static final String PATH_TEMPLATES = "/templates/$WORLD_TYPE";
    private static final String PATH_WORLD_JOIN = "/v1/$ID/join/pc";
    private static final String PATH_WORLD_GET = "/$ID";
    private static final String PATH_WORLD_INVITES = "/$WORLD_ID";
    private static final String PATH_WORLD_UNINVITE = "/$WORLD_ID/invite/$UUID";
    private static final String PATH_PENDING_INVITES_COUNT = "/count/pending";
    private static final String PATH_PENDING_INVITES = "/pending";
    private static final String PATH_ACCEPT_INVITE = "/accept/$INVITATION_ID";
    private static final String PATH_REJECT_INVITE = "/reject/$INVITATION_ID";
    private static final String PATH_UNINVITE_MYSELF = "/$WORLD_ID";
    private static final String PATH_WORLD_UPDATE = "/$WORLD_ID";
    private static final String PATH_SLOT = "/$WORLD_ID/slot/$SLOT_ID";
    private static final String PATH_WORLD_OPEN = "/$WORLD_ID/open";
    private static final String PATH_WORLD_CLOSE = "/$WORLD_ID/close";
    private static final String PATH_WORLD_RESET = "/$WORLD_ID/reset";
    private static final String PATH_DELETE_WORLD = "/$WORLD_ID";
    private static final String PATH_WORLD_BACKUPS = "/$WORLD_ID/backups";
    private static final String PATH_WORLD_DOWNLOAD = "/$WORLD_ID/slot/$SLOT_ID/download";
    private static final String PATH_WORLD_UPLOAD = "/$WORLD_ID/backups/upload";
    private static final String PATH_CLIENT_COMPATIBLE = "/client/compatible";
    private static final String PATH_TOS_AGREED = "/tos/agreed";
    private static final String PATH_NEWS = "/v1/news";
    private static final String PATH_MARK_NOTIFICATIONS_SEEN = "/seen";
    private static final String PATH_DISMISS_NOTIFICATIONS = "/dismiss";
    private static final GuardedSerializer GSON = new GuardedSerializer();

    public static RealmsClient create() {
        Minecraft minecraft = Minecraft.getInstance();
        return create(minecraft);
    }

    public static RealmsClient create(Minecraft p_239152_) {
        String s = p_239152_.getUser().getName();
        String s1 = p_239152_.getUser().getSessionId();
        return new RealmsClient(s1, s, p_239152_);
    }

    public RealmsClient(String p_87166_, String p_87167_, Minecraft p_87168_) {
        this.sessionId = p_87166_;
        this.username = p_87167_;
        this.minecraft = p_87168_;
        RealmsClientConfig.setProxy(p_87168_.getProxy());
    }

    public RealmsServerList listRealms() throws RealmsServiceException {
        String s = this.url("worlds");
        if (RealmsMainScreen.isSnapshot()) {
            s = s + "/listUserWorldsOfType/any";
        }

        String s1 = this.execute(Request.get(s));
        return RealmsServerList.parse(s1);
    }

    public List<RealmsServer> listSnapshotEligibleRealms() throws RealmsServiceException {
        String s = this.url("worlds/listPrereleaseEligibleWorlds");
        String s1 = this.execute(Request.get(s));
        return RealmsServerList.parse(s1).servers;
    }

    public RealmsServer createSnapshotRealm(Long p_310421_) throws RealmsServiceException {
        String s = String.valueOf(p_310421_);
        String s1 = this.url("worlds" + "/$PARENT_WORLD_ID/createPrereleaseRealm".replace("$PARENT_WORLD_ID", s));
        return RealmsServer.parse(this.execute(Request.post(s1, s)));
    }

    public List<RealmsNotification> getNotifications() throws RealmsServiceException {
        String s = this.url("notifications");
        String s1 = this.execute(Request.get(s));
        return RealmsNotification.parseList(s1);
    }

    private static JsonArray uuidListToJsonArray(List<UUID> p_275393_) {
        JsonArray jsonarray = new JsonArray();

        for (UUID uuid : p_275393_) {
            if (uuid != null) {
                jsonarray.add(uuid.toString());
            }
        }

        return jsonarray;
    }

    public void notificationsSeen(List<UUID> p_275212_) throws RealmsServiceException {
        String s = this.url("notifications/seen");
        this.execute(Request.post(s, GSON.toJson(uuidListToJsonArray(p_275212_))));
    }

    public void notificationsDismiss(List<UUID> p_275407_) throws RealmsServiceException {
        String s = this.url("notifications/dismiss");
        this.execute(Request.post(s, GSON.toJson(uuidListToJsonArray(p_275407_))));
    }

    public RealmsServer getOwnRealm(long p_87175_) throws RealmsServiceException {
        String s = this.url("worlds" + "/$ID".replace("$ID", String.valueOf(p_87175_)));
        String s1 = this.execute(Request.get(s));
        return RealmsServer.parse(s1);
    }

    public ServerActivityList getActivity(long p_167279_) throws RealmsServiceException {
        String s = this.url("activities" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(p_167279_)));
        String s1 = this.execute(Request.get(s));
        return ServerActivityList.parse(s1);
    }

    public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
        String s = this.url("activities/liveplayerlist");
        String s1 = this.execute(Request.get(s));
        return RealmsServerPlayerLists.parse(s1);
    }

    public RealmsServerAddress join(long p_87208_) throws RealmsServiceException {
        String s = this.url("worlds" + "/v1/$ID/join/pc".replace("$ID", p_87208_ + ""));
        String s1 = this.execute(Request.get(s, 5000, 30000));
        return RealmsServerAddress.parse(s1);
    }

    public void initializeRealm(long p_87192_, String p_87193_, String p_87194_) throws RealmsServiceException {
        RealmsDescriptionDto realmsdescriptiondto = new RealmsDescriptionDto(p_87193_, p_87194_);
        String s = this.url("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(p_87192_)));
        String s1 = GSON.toJson(realmsdescriptiondto);
        this.execute(Request.post(s, s1, 5000, 10000));
    }

    public boolean hasParentalConsent() throws RealmsServiceException {
        String s = this.url("mco/available");
        String s1 = this.execute(Request.get(s));
        return Boolean.parseBoolean(s1);
    }

    public RealmsClient.CompatibleVersionResponse clientCompatible() throws RealmsServiceException {
        String s = this.url("mco/client/compatible");
        String s1 = this.execute(Request.get(s));

        try {
            return RealmsClient.CompatibleVersionResponse.valueOf(s1);
        } catch (IllegalArgumentException illegalargumentexception) {
            throw new RealmsServiceException(RealmsError.CustomError.unknownCompatibilityResponse(s1));
        }
    }

    public void uninvite(long p_87184_, UUID p_300114_) throws RealmsServiceException {
        String s = this.url(
            "invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(p_87184_)).replace("$UUID", UndashedUuid.toString(p_300114_))
        );
        this.execute(Request.delete(s));
    }

    public void uninviteMyselfFrom(long p_87223_) throws RealmsServiceException {
        String s = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(p_87223_)));
        this.execute(Request.delete(s));
    }

    public RealmsServer invite(long p_87213_, String p_87214_) throws RealmsServiceException {
        PlayerInfo playerinfo = new PlayerInfo();
        playerinfo.setName(p_87214_);
        String s = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(p_87213_)));
        String s1 = this.execute(Request.post(s, GSON.toJson(playerinfo)));
        return RealmsServer.parse(s1);
    }

    public BackupList backupsFor(long p_87231_) throws RealmsServiceException {
        String s = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(p_87231_)));
        String s1 = this.execute(Request.get(s));
        return BackupList.parse(s1);
    }

    public void update(long p_87216_, String p_87217_, String p_87218_) throws RealmsServiceException {
        RealmsDescriptionDto realmsdescriptiondto = new RealmsDescriptionDto(p_87217_, p_87218_);
        String s = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(p_87216_)));
        this.execute(Request.post(s, GSON.toJson(realmsdescriptiondto)));
    }

    public void updateSlot(long p_87180_, int p_87181_, RealmsWorldOptions p_87182_) throws RealmsServiceException {
        String s = this.url(
            "worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(p_87180_)).replace("$SLOT_ID", String.valueOf(p_87181_))
        );
        String s1 = p_87182_.toJson();
        this.execute(Request.post(s, s1));
    }

    public boolean switchSlot(long p_87177_, int p_87178_) throws RealmsServiceException {
        String s = this.url(
            "worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(p_87177_)).replace("$SLOT_ID", String.valueOf(p_87178_))
        );
        String s1 = this.execute(Request.put(s, ""));
        return Boolean.valueOf(s1);
    }

    public void restoreWorld(long p_87225_, String p_87226_) throws RealmsServiceException {
        String s = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(p_87225_)), "backupId=" + p_87226_);
        this.execute(Request.put(s, "", 40000, 600000));
    }

    public WorldTemplatePaginatedList fetchWorldTemplates(int p_87171_, int p_87172_, RealmsServer.WorldType p_87173_) throws RealmsServiceException {
        String s = this.url(
            "worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", p_87173_.toString()),
            String.format(Locale.ROOT, "page=%d&pageSize=%d", p_87171_, p_87172_)
        );
        String s1 = this.execute(Request.get(s));
        return WorldTemplatePaginatedList.parse(s1);
    }

    public Boolean putIntoMinigameMode(long p_87233_, String p_87234_) throws RealmsServiceException {
        String s = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", p_87234_).replace("$WORLD_ID", String.valueOf(p_87233_));
        String s1 = this.url("worlds" + s);
        return Boolean.valueOf(this.execute(Request.put(s1, "")));
    }

    public Ops op(long p_87239_, UUID p_297634_) throws RealmsServiceException {
        String s = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(p_87239_)).replace("$PROFILE_UUID", UndashedUuid.toString(p_297634_));
        String s1 = this.url("ops" + s);
        return Ops.parse(this.execute(Request.post(s1, "")));
    }

    public Ops deop(long p_87245_, UUID p_298989_) throws RealmsServiceException {
        String s = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(p_87245_)).replace("$PROFILE_UUID", UndashedUuid.toString(p_298989_));
        String s1 = this.url("ops" + s);
        return Ops.parse(this.execute(Request.delete(s1)));
    }

    public Boolean open(long p_87237_) throws RealmsServiceException {
        String s = this.url("worlds" + "/$WORLD_ID/open".replace("$WORLD_ID", String.valueOf(p_87237_)));
        String s1 = this.execute(Request.put(s, ""));
        return Boolean.valueOf(s1);
    }

    public Boolean close(long p_87243_) throws RealmsServiceException {
        String s = this.url("worlds" + "/$WORLD_ID/close".replace("$WORLD_ID", String.valueOf(p_87243_)));
        String s1 = this.execute(Request.put(s, ""));
        return Boolean.valueOf(s1);
    }

    public Boolean resetWorldWithTemplate(long p_87251_, String p_87252_) throws RealmsServiceException {
        RealmsWorldResetDto realmsworldresetdto = new RealmsWorldResetDto(null, Long.valueOf(p_87252_), -1, false, Set.of());
        String s = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(p_87251_)));
        String s1 = this.execute(Request.post(s, GSON.toJson(realmsworldresetdto), 30000, 80000));
        return Boolean.valueOf(s1);
    }

    public Subscription subscriptionFor(long p_87249_) throws RealmsServiceException {
        String s = this.url("subscriptions" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(p_87249_)));
        String s1 = this.execute(Request.get(s));
        return Subscription.parse(s1);
    }

    public int pendingInvitesCount() throws RealmsServiceException {
        return this.pendingInvites().pendingInvites.size();
    }

    public PendingInvitesList pendingInvites() throws RealmsServiceException {
        String s = this.url("invites/pending");
        String s1 = this.execute(Request.get(s));
        PendingInvitesList pendinginviteslist = PendingInvitesList.parse(s1);
        pendinginviteslist.pendingInvites.removeIf(this::isBlocked);
        return pendinginviteslist;
    }

    private boolean isBlocked(PendingInvite p_87198_) {
        return this.minecraft.getPlayerSocialManager().isBlocked(p_87198_.realmOwnerUuid);
    }

    public void acceptInvitation(String p_87202_) throws RealmsServiceException {
        String s = this.url("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", p_87202_));
        this.execute(Request.put(s, ""));
    }

    public WorldDownload requestDownloadInfo(long p_87210_, int p_87211_) throws RealmsServiceException {
        String s = this.url(
            "worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(p_87210_)).replace("$SLOT_ID", String.valueOf(p_87211_))
        );
        String s1 = this.execute(Request.get(s));
        return WorldDownload.parse(s1);
    }

    @Nullable
    public UploadInfo requestUploadInfo(long p_87257_) throws RealmsServiceException {
        String s = this.url("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(p_87257_)));
        String s1 = UploadTokenCache.get(p_87257_);
        UploadInfo uploadinfo = UploadInfo.parse(this.execute(Request.put(s, UploadInfo.createRequest(s1))));
        if (uploadinfo != null) {
            UploadTokenCache.put(p_87257_, uploadinfo.getToken());
        }

        return uploadinfo;
    }

    public void rejectInvitation(String p_87220_) throws RealmsServiceException {
        String s = this.url("invites" + "/reject/$INVITATION_ID".replace("$INVITATION_ID", p_87220_));
        this.execute(Request.put(s, ""));
    }

    public void agreeToTos() throws RealmsServiceException {
        String s = this.url("mco/tos/agreed");
        this.execute(Request.post(s, ""));
    }

    public RealmsNews getNews() throws RealmsServiceException {
        String s = this.url("mco/v1/news");
        String s1 = this.execute(Request.get(s, 5000, 10000));
        return RealmsNews.parse(s1);
    }

    public void sendPingResults(PingResult p_87200_) throws RealmsServiceException {
        String s = this.url("regions/ping/stat");
        this.execute(Request.post(s, GSON.toJson(p_87200_)));
    }

    public Boolean trialAvailable() throws RealmsServiceException {
        String s = this.url("trial");
        String s1 = this.execute(Request.get(s));
        return Boolean.valueOf(s1);
    }

    public void deleteRealm(long p_87255_) throws RealmsServiceException {
        String s = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(p_87255_)));
        this.execute(Request.delete(s));
    }

    private String url(String p_87228_) {
        return this.url(p_87228_, null);
    }

    private String url(String p_87204_, @Nullable String p_87205_) {
        try {
            return new URI(ENVIRONMENT.protocol, ENVIRONMENT.baseUrl, "/" + p_87204_, p_87205_, null).toASCIIString();
        } catch (URISyntaxException urisyntaxexception) {
            throw new IllegalArgumentException(p_87204_, urisyntaxexception);
        }
    }

    private String execute(Request<?> p_87196_) throws RealmsServiceException {
        p_87196_.cookie("sid", this.sessionId);
        p_87196_.cookie("user", this.username);
        p_87196_.cookie("version", SharedConstants.getCurrentVersion().getName());
        p_87196_.addSnapshotHeader(RealmsMainScreen.isSnapshot());

        try {
            int i = p_87196_.responseCode();
            if (i != 503 && i != 277) {
                String s = p_87196_.text();
                if (i >= 200 && i < 300) {
                    return s;
                } else if (i == 401) {
                    String s1 = p_87196_.getHeader("WWW-Authenticate");
                    LOGGER.info("Could not authorize you against Realms server: {}", s1);
                    throw new RealmsServiceException(new RealmsError.AuthenticationError(s1));
                } else {
                    RealmsError realmserror = RealmsError.parse(i, s);
                    throw new RealmsServiceException(realmserror);
                }
            } else {
                int j = p_87196_.getRetryAfterHeader();
                throw new RetryCallException(j, i);
            }
        } catch (RealmsHttpException realmshttpexception) {
            throw new RealmsServiceException(RealmsError.CustomError.connectivityError(realmshttpexception));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum CompatibleVersionResponse {
        COMPATIBLE,
        OUTDATED,
        OTHER;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Environment {
        PRODUCTION("pc.realms.minecraft.net", "https"),
        STAGE("pc-stage.realms.minecraft.net", "https"),
        LOCAL("localhost:8080", "http");

        public final String baseUrl;
        public final String protocol;

        private Environment(final String p_87286_, final String p_87287_) {
            this.baseUrl = p_87286_;
            this.protocol = p_87287_;
        }

        public static Optional<RealmsClient.Environment> byName(String p_289688_) {
            String s = p_289688_.toLowerCase(Locale.ROOT);

            return switch (s) {
                case "production" -> Optional.of(PRODUCTION);
                case "local" -> Optional.of(LOCAL);
                case "stage", "staging" -> Optional.of(STAGE);
                default -> Optional.empty();
            };
        }
    }
}