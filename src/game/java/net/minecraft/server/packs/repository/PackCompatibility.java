package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.InclusiveRange;

public enum PackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Component description;
    private final Component confirmation;

    private PackCompatibility(final String p_10488_) {
        this.description = Component.translatable("pack.incompatible." + p_10488_).withStyle(ChatFormatting.GRAY);
        this.confirmation = Component.translatable("pack.incompatible.confirm." + p_10488_);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static PackCompatibility forVersion(InclusiveRange<Integer> p_300208_, int p_297718_) {
        if (p_300208_.maxInclusive() < p_297718_) {
            return TOO_OLD;
        } else {
            return p_297718_ < p_300208_.minInclusive() ? TOO_NEW : COMPATIBLE;
        }
    }

    public Component getDescription() {
        return this.description;
    }

    public Component getConfirmation() {
        return this.confirmation;
    }
}