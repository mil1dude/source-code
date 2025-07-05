package net.minecraft.client.gui.screens.advancements;

import net.minecraft.advancements.AdvancementType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum AdvancementWidgetType {
    OBTAINED(
        ResourceLocation.withDefaultNamespace("advancements/box_obtained"),
        ResourceLocation.withDefaultNamespace("advancements/task_frame_obtained"),
        ResourceLocation.withDefaultNamespace("advancements/challenge_frame_obtained"),
        ResourceLocation.withDefaultNamespace("advancements/goal_frame_obtained")
    ),
    UNOBTAINED(
        ResourceLocation.withDefaultNamespace("advancements/box_unobtained"),
        ResourceLocation.withDefaultNamespace("advancements/task_frame_unobtained"),
        ResourceLocation.withDefaultNamespace("advancements/challenge_frame_unobtained"),
        ResourceLocation.withDefaultNamespace("advancements/goal_frame_unobtained")
    );

    private final ResourceLocation boxSprite;
    private final ResourceLocation taskFrameSprite;
    private final ResourceLocation challengeFrameSprite;
    private final ResourceLocation goalFrameSprite;

    private AdvancementWidgetType(
        final ResourceLocation p_300112_, final ResourceLocation p_300140_, final ResourceLocation p_299008_, final ResourceLocation p_301311_
    ) {
        this.boxSprite = p_300112_;
        this.taskFrameSprite = p_300140_;
        this.challengeFrameSprite = p_299008_;
        this.goalFrameSprite = p_301311_;
    }

    public ResourceLocation boxSprite() {
        return this.boxSprite;
    }

    public ResourceLocation frameSprite(AdvancementType p_311711_) {
        return switch (p_311711_) {
            case TASK -> this.taskFrameSprite;
            case CHALLENGE -> this.challengeFrameSprite;
            case GOAL -> this.goalFrameSprite;
        };
    }
}