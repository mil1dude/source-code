package net.minecraft.network.chat.numbers;

import net.minecraft.network.chat.MutableComponent;

public interface NumberFormat {
    MutableComponent format(int p_312462_);

    NumberFormatType<? extends NumberFormat> type();
}