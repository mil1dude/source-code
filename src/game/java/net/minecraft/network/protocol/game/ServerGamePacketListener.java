package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.ping.ServerPingPacketListener;

public interface ServerGamePacketListener extends ServerPingPacketListener, ServerCommonPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void handleAnimate(ServerboundSwingPacket p_133781_);

    void handleChat(ServerboundChatPacket p_133743_);

    void handleChatCommand(ServerboundChatCommandPacket p_237920_);

    void handleSignedChatCommand(ServerboundChatCommandSignedPacket p_332349_);

    void handleChatAck(ServerboundChatAckPacket p_242214_);

    void handleClientCommand(ServerboundClientCommandPacket p_133744_);

    void handleContainerButtonClick(ServerboundContainerButtonClickPacket p_133748_);

    void handleContainerClick(ServerboundContainerClickPacket p_133749_);

    void handlePlaceRecipe(ServerboundPlaceRecipePacket p_133762_);

    void handleContainerClose(ServerboundContainerClosePacket p_133750_);

    void handleInteract(ServerboundInteractPacket p_133754_);

    void handleMovePlayer(ServerboundMovePlayerPacket p_133758_);

    void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket p_133763_);

    void handlePlayerAction(ServerboundPlayerActionPacket p_133764_);

    void handlePlayerCommand(ServerboundPlayerCommandPacket p_133765_);

    void handlePlayerInput(ServerboundPlayerInputPacket p_133766_);

    void handleSetCarriedItem(ServerboundSetCarriedItemPacket p_133774_);

    void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket p_133777_);

    void handleSignUpdate(ServerboundSignUpdatePacket p_133780_);

    void handleUseItemOn(ServerboundUseItemOnPacket p_133783_);

    void handleUseItem(ServerboundUseItemPacket p_133784_);

    void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket p_133782_);

    void handlePaddleBoat(ServerboundPaddleBoatPacket p_133760_);

    void handleMoveVehicle(ServerboundMoveVehiclePacket p_133759_);

    void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket p_133740_);

    void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket p_377981_);

    void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket p_133768_);

    void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket p_367622_);

    void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket p_133767_);

    void handleSeenAdvancements(ServerboundSeenAdvancementsPacket p_133771_);

    void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket p_133746_);

    void handleSetCommandBlock(ServerboundSetCommandBlockPacket p_133775_);

    void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket p_133776_);

    void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket p_377338_);

    void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket p_375772_);

    void handleRenameItem(ServerboundRenameItemPacket p_133769_);

    void handleSetBeaconPacket(ServerboundSetBeaconPacket p_133773_);

    void handleSetStructureBlock(ServerboundSetStructureBlockPacket p_133779_);

    void handleSelectTrade(ServerboundSelectTradePacket p_133772_);

    void handleEditBook(ServerboundEditBookPacket p_133752_);

    void handleEntityTagQuery(ServerboundEntityTagQueryPacket p_333332_);

    void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket p_311148_);

    void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket p_330143_);

    void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket p_133778_);

    void handleJigsawGenerate(ServerboundJigsawGeneratePacket p_133755_);

    void handleChangeDifficulty(ServerboundChangeDifficultyPacket p_133742_);

    void handleLockDifficulty(ServerboundLockDifficultyPacket p_133757_);

    void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket p_254226_);

    void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket p_298498_);

    void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket p_297801_);

    void handleDebugSampleSubscription(ServerboundDebugSampleSubscriptionPacket p_335173_);

    void handleClientTickEnd(ServerboundClientTickEndPacket p_368528_);
}