package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum ChatTrustLevel implements StringRepresentable {
    SECURE("secure"),
    MODIFIED("modified"),
    NOT_SECURE("not_secure");

    public static final Codec<ChatTrustLevel> CODEC = StringRepresentable.fromEnum(ChatTrustLevel::values);
    private final String serializedName;

    private ChatTrustLevel(final String p_254190_) {
        this.serializedName = p_254190_;
    }

    public static ChatTrustLevel evaluate(PlayerChatMessage p_248663_, Component p_248544_, Instant p_252024_) {
        if (!p_248663_.hasSignature() || p_248663_.hasExpiredClient(p_252024_)) {
            return NOT_SECURE;
        } else {
            return isModified(p_248663_, p_248544_) ? MODIFIED : SECURE;
        }
    }

    private static boolean isModified(PlayerChatMessage p_252093_, Component p_250811_) {
        if (!p_250811_.getString().contains(p_252093_.signedContent())) {
            return true;
        } else {
            Component component = p_252093_.unsignedContent();
            return component == null ? false : containsModifiedStyle(component);
        }
    }

    private static boolean containsModifiedStyle(Component p_251011_) {
        return p_251011_.<Boolean>visit((p_251711_, p_250844_) -> isModifiedStyle(p_251711_) ? Optional.of(true) : Optional.empty(), Style.EMPTY).orElse(false);
    }

    private static boolean isModifiedStyle(Style p_251347_) {
        return !p_251347_.getFont().equals(Style.DEFAULT_FONT);
    }

    public boolean isNotSecure() {
        return this == NOT_SECURE;
    }

    @Nullable
    public GuiMessageTag createTag(PlayerChatMessage p_240632_) {
        return switch (this) {
            case MODIFIED -> GuiMessageTag.chatModified(p_240632_.signedContent());
            case NOT_SECURE -> GuiMessageTag.chatNotSecure();
            default -> null;
        };
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}