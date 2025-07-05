package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import org.slf4j.Logger;

@FunctionalInterface
public interface SignedMessageValidator {
    Logger LOGGER = LogUtils.getLogger();
    SignedMessageValidator ACCEPT_UNSIGNED = PlayerChatMessage::removeSignature;
    SignedMessageValidator REJECT_ALL = p_308576_ -> {
        LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", p_308576_.sender());
        return null;
    };

    @Nullable
    PlayerChatMessage updateAndValidate(PlayerChatMessage p_251036_);

    public static class KeyBased implements SignedMessageValidator {
        private final SignatureValidator validator;
        private final BooleanSupplier expired;
        @Nullable
        private PlayerChatMessage lastMessage;
        private boolean isChainValid = true;

        public KeyBased(SignatureValidator p_241517_, BooleanSupplier p_300664_) {
            this.validator = p_241517_;
            this.expired = p_300664_;
        }

        private boolean validateChain(PlayerChatMessage p_250412_) {
            if (p_250412_.equals(this.lastMessage)) {
                return true;
            } else if (this.lastMessage != null && !p_250412_.link().isDescendantOf(this.lastMessage.link())) {
                LOGGER.error(
                    "Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}",
                    p_250412_.sender(),
                    this.lastMessage.link().index(),
                    this.lastMessage.link().sessionId(),
                    p_250412_.link().index(),
                    p_250412_.link().sessionId()
                );
                return false;
            } else {
                return true;
            }
        }

        private boolean validate(PlayerChatMessage p_297346_) {
            if (this.expired.getAsBoolean()) {
                LOGGER.error("Received message from player with expired profile public key: {}", p_297346_);
                return false;
            } else if (!p_297346_.verify(this.validator)) {
                LOGGER.error("Received message with invalid signature from {}", p_297346_.sender());
                return false;
            } else {
                return this.validateChain(p_297346_);
            }
        }

        @Nullable
        @Override
        public PlayerChatMessage updateAndValidate(PlayerChatMessage p_251182_) {
            this.isChainValid = this.isChainValid && this.validate(p_251182_);
            if (!this.isChainValid) {
                return null;
            } else {
                this.lastMessage = p_251182_;
                return p_251182_;
            }
        }
    }
}