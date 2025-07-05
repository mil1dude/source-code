package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.ping.ClientPongPacketListener;

public interface ClientGamePacketListener extends ClientPongPacketListener, ClientCommonPacketListener {
    @Override
    default ConnectionProtocol protocol() {
        return ConnectionProtocol.PLAY;
    }

    void handleAddEntity(ClientboundAddEntityPacket p_131367_);

    void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket p_131368_);

    void handleAddObjective(ClientboundSetObjectivePacket p_131438_);

    void handleAnimate(ClientboundAnimatePacket p_131372_);

    void handleHurtAnimation(ClientboundHurtAnimationPacket p_265165_);

    void handleAwardStats(ClientboundAwardStatsPacket p_131373_);

    void handleRecipeBookAdd(ClientboundRecipeBookAddPacket p_364790_);

    void handleRecipeBookRemove(ClientboundRecipeBookRemovePacket p_363804_);

    void handleRecipeBookSettings(ClientboundRecipeBookSettingsPacket p_363431_);

    void handleBlockDestruction(ClientboundBlockDestructionPacket p_131375_);

    void handleOpenSignEditor(ClientboundOpenSignEditorPacket p_131410_);

    void handleBlockEntityData(ClientboundBlockEntityDataPacket p_131376_);

    void handleBlockEvent(ClientboundBlockEventPacket p_131377_);

    void handleBlockUpdate(ClientboundBlockUpdatePacket p_131378_);

    void handleSystemChat(ClientboundSystemChatPacket p_237543_);

    void handlePlayerChat(ClientboundPlayerChatPacket p_237540_);

    void handleDisguisedChat(ClientboundDisguisedChatPacket p_251057_);

    void handleDeleteChat(ClientboundDeleteChatPacket p_241462_);

    void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket p_131423_);

    void handleMapItemData(ClientboundMapItemDataPacket p_131404_);

    void handleContainerClose(ClientboundContainerClosePacket p_131385_);

    void handleContainerContent(ClientboundContainerSetContentPacket p_131386_);

    void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket p_131397_);

    void handleContainerSetData(ClientboundContainerSetDataPacket p_131387_);

    void handleContainerSetSlot(ClientboundContainerSetSlotPacket p_131388_);

    void handleEntityEvent(ClientboundEntityEventPacket p_131393_);

    void handleEntityLinkPacket(ClientboundSetEntityLinkPacket p_131433_);

    void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket p_131439_);

    void handleExplosion(ClientboundExplodePacket p_131394_);

    void handleGameEvent(ClientboundGameEventPacket p_131396_);

    void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket p_195622_);

    void handleChunksBiomes(ClientboundChunksBiomesPacket p_275451_);

    void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket p_131395_);

    void handleLevelEvent(ClientboundLevelEventPacket p_131400_);

    void handleLogin(ClientboundLoginPacket p_131403_);

    void handleMoveEntity(ClientboundMoveEntityPacket p_131406_);

    void handleMinecartAlongTrack(ClientboundMoveMinecartPacket p_361369_);

    void handleMovePlayer(ClientboundPlayerPositionPacket p_131416_);

    void handleRotatePlayer(ClientboundPlayerRotationPacket p_365080_);

    void handleParticleEvent(ClientboundLevelParticlesPacket p_131401_);

    void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket p_131412_);

    void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket p_252308_);

    void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket p_248573_);

    void handleRemoveEntities(ClientboundRemoveEntitiesPacket p_182700_);

    void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket p_131419_);

    void handleRespawn(ClientboundRespawnPacket p_131421_);

    void handleRotateMob(ClientboundRotateHeadPacket p_131422_);

    void handleSetHeldSlot(ClientboundSetHeldSlotPacket p_364003_);

    void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket p_131431_);

    void handleSetEntityData(ClientboundSetEntityDataPacket p_131432_);

    void handleSetEntityMotion(ClientboundSetEntityMotionPacket p_131434_);

    void handleSetEquipment(ClientboundSetEquipmentPacket p_131435_);

    void handleSetExperience(ClientboundSetExperiencePacket p_131436_);

    void handleSetHealth(ClientboundSetHealthPacket p_131437_);

    void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket p_131440_);

    void handleSetScore(ClientboundSetScorePacket p_131441_);

    void handleResetScore(ClientboundResetScorePacket p_310831_);

    void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket p_131430_);

    void handleSetTime(ClientboundSetTimePacket p_131442_);

    void handleSoundEvent(ClientboundSoundPacket p_131445_);

    void handleSoundEntityEvent(ClientboundSoundEntityPacket p_131444_);

    void handleTakeItemEntity(ClientboundTakeItemEntityPacket p_131449_);

    void handleEntityPositionSync(ClientboundEntityPositionSyncPacket p_363417_);

    void handleTeleportEntity(ClientboundTeleportEntityPacket p_131450_);

    void handleTickingState(ClientboundTickingStatePacket p_309939_);

    void handleTickingStep(ClientboundTickingStepPacket p_312343_);

    void handleUpdateAttributes(ClientboundUpdateAttributesPacket p_131452_);

    void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket p_131453_);

    void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket p_178546_);

    void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket p_178547_);

    void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket p_178548_);

    void handleChangeDifficulty(ClientboundChangeDifficultyPacket p_131380_);

    void handleSetCamera(ClientboundSetCameraPacket p_131426_);

    void handleInitializeBorder(ClientboundInitializeBorderPacket p_178544_);

    void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket p_178552_);

    void handleSetBorderSize(ClientboundSetBorderSizePacket p_178553_);

    void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket p_178554_);

    void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket p_178555_);

    void handleSetBorderCenter(ClientboundSetBorderCenterPacket p_178551_);

    void handleTabListCustomisation(ClientboundTabListPacket p_131447_);

    void handleBossUpdate(ClientboundBossEventPacket p_131379_);

    void handleItemCooldown(ClientboundCooldownPacket p_131389_);

    void handleMoveVehicle(ClientboundMoveVehiclePacket p_131407_);

    void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket p_131451_);

    void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket p_131424_);

    void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket p_131411_);

    void handleCommands(ClientboundCommandsPacket p_131383_);

    void handleStopSoundEvent(ClientboundStopSoundPacket p_131446_);

    void handleCommandSuggestions(ClientboundCommandSuggestionsPacket p_131382_);

    void handleUpdateRecipes(ClientboundUpdateRecipesPacket p_131454_);

    void handleLookAt(ClientboundPlayerLookAtPacket p_131415_);

    void handleTagQueryPacket(ClientboundTagQueryPacket p_131448_);

    void handleLightUpdatePacket(ClientboundLightUpdatePacket p_195623_);

    void handleOpenBook(ClientboundOpenBookPacket p_131408_);

    void handleOpenScreen(ClientboundOpenScreenPacket p_131409_);

    void handleMerchantOffers(ClientboundMerchantOffersPacket p_131405_);

    void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket p_131429_);

    void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket p_195624_);

    void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket p_131428_);

    void handleBlockChangedAck(ClientboundBlockChangedAckPacket p_237538_);

    void setActionBarText(ClientboundSetActionBarTextPacket p_178550_);

    void setSubtitleText(ClientboundSetSubtitleTextPacket p_178556_);

    void setTitleText(ClientboundSetTitleTextPacket p_178557_);

    void setTitlesAnimation(ClientboundSetTitlesAnimationPacket p_178558_);

    void handleTitlesClear(ClientboundClearTitlesPacket p_178543_);

    void handleServerData(ClientboundServerDataPacket p_237541_);

    void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket p_240770_);

    void handleBundlePacket(ClientboundBundlePacket p_265211_);

    void handleDamageEvent(ClientboundDamageEventPacket p_270900_);

    void handleConfigurationStart(ClientboundStartConfigurationPacket p_298772_);

    void handleChunkBatchStart(ClientboundChunkBatchStartPacket p_298767_);

    void handleChunkBatchFinished(ClientboundChunkBatchFinishedPacket p_297668_);

    void handleDebugSample(ClientboundDebugSamplePacket p_328537_);

    void handleProjectilePowerPacket(ClientboundProjectilePowerPacket p_330129_);

    void handleSetCursorItem(ClientboundSetCursorItemPacket p_362172_);

    void handleSetPlayerInventory(ClientboundSetPlayerInventoryPacket p_365703_);
}