package net.minecraft.util;

public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;

    public boolean toBoolean(boolean p_361597_) {
        return switch (this) {
            case TRUE -> true;
            case FALSE -> false;
            default -> p_361597_;
        };
    }
}