package net.minecraft.network.chat.contents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public class KeybindContents implements ComponentContents {
    public static final MapCodec<KeybindContents> CODEC = RecordCodecBuilder.mapCodec(
        p_310396_ -> p_310396_.group(Codec.STRING.fieldOf("keybind").forGetter(p_309709_ -> p_309709_.name)).apply(p_310396_, KeybindContents::new)
    );
    public static final ComponentContents.Type<KeybindContents> TYPE = new ComponentContents.Type<>(CODEC, "keybind");
    private final String name;
    @Nullable
    private Supplier<Component> nameResolver;

    public KeybindContents(String p_237347_) {
        this.name = p_237347_;
    }

    private Component getNestedComponent() {
        if (this.nameResolver == null) {
            this.nameResolver = KeybindResolver.keyResolver.apply(this.name);
        }

        return this.nameResolver.get();
    }

    @Override
    public <T> Optional<T> visit(FormattedText.ContentConsumer<T> p_237350_) {
        return this.getNestedComponent().visit(p_237350_);
    }

    @Override
    public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> p_237352_, Style p_237353_) {
        return this.getNestedComponent().visit(p_237352_, p_237353_);
    }

    @Override
    public boolean equals(Object p_237356_) {
        if (this == p_237356_) {
            return true;
        } else {
            if (p_237356_ instanceof KeybindContents keybindcontents && this.name.equals(keybindcontents.name)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return "keybind{" + this.name + "}";
    }

    public String getName() {
        return this.name;
    }

    @Override
    public ComponentContents.Type<?> type() {
        return TYPE;
    }
}