package net.minecraft.world.entity.player;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum ChatVisiblity implements OptionEnum {
    FULL(0, "options.chat.visibility.full"),
    SYSTEM(1, "options.chat.visibility.system"),
    HIDDEN(2, "options.chat.visibility.hidden");

    private static final IntFunction<ChatVisiblity> BY_ID = ByIdMap.continuous(ChatVisiblity::getId, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    private final int id;
    private final String key;

    private ChatVisiblity(final int p_35963_, final String p_35964_) {
        this.id = p_35963_;
        this.key = p_35964_;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public static ChatVisiblity byId(int p_35967_) {
        return BY_ID.apply(p_35967_);
    }
}