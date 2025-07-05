package net.minecraft.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyMapping implements Comparable<KeyMapping> {
    private static final Map<String, KeyMapping> ALL = Maps.newHashMap();
    private static final Map<InputConstants.Key, KeyMapping> MAP = Maps.newHashMap();
    private static final Set<String> CATEGORIES = Sets.newHashSet();
    public static final String CATEGORY_MOVEMENT = "key.categories.movement";
    public static final String CATEGORY_MISC = "key.categories.misc";
    public static final String CATEGORY_MULTIPLAYER = "key.categories.multiplayer";
    public static final String CATEGORY_GAMEPLAY = "key.categories.gameplay";
    public static final String CATEGORY_INVENTORY = "key.categories.inventory";
    public static final String CATEGORY_INTERFACE = "key.categories.ui";
    public static final String CATEGORY_CREATIVE = "key.categories.creative";
    private static final Map<String, Integer> CATEGORY_SORT_ORDER = Util.make(Maps.newHashMap(), p_90845_ -> {
        p_90845_.put("key.categories.movement", 1);
        p_90845_.put("key.categories.gameplay", 2);
        p_90845_.put("key.categories.inventory", 3);
        p_90845_.put("key.categories.creative", 4);
        p_90845_.put("key.categories.multiplayer", 5);
        p_90845_.put("key.categories.ui", 6);
        p_90845_.put("key.categories.misc", 7);
    });
    private final String name;
    private final InputConstants.Key defaultKey;
    private final String category;
    private InputConstants.Key key;
    private boolean isDown;
    private int clickCount;

    public static void click(InputConstants.Key p_90836_) {
        KeyMapping keymapping = MAP.get(p_90836_);
        if (keymapping != null) {
            keymapping.clickCount++;
        }
    }

    public static void set(InputConstants.Key p_90838_, boolean p_90839_) {
        KeyMapping keymapping = MAP.get(p_90838_);
        if (keymapping != null) {
            keymapping.setDown(p_90839_);
        }
    }

    public static void setAll() {
        for (KeyMapping keymapping : ALL.values()) {
            if (keymapping.key.getType() == InputConstants.Type.KEYSYM && keymapping.key.getValue() != InputConstants.UNKNOWN.getValue()) {
                keymapping.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keymapping.key.getValue()));
            }
        }
    }

    public static void releaseAll() {
        for (KeyMapping keymapping : ALL.values()) {
            keymapping.release();
        }
    }

    public static void resetToggleKeys() {
        for (KeyMapping keymapping : ALL.values()) {
            if (keymapping instanceof ToggleKeyMapping togglekeymapping) {
                togglekeymapping.reset();
            }
        }
    }

    public static void resetMapping() {
        MAP.clear();

        for (KeyMapping keymapping : ALL.values()) {
            MAP.put(keymapping.key, keymapping);
        }
    }

    public KeyMapping(String p_90821_, int p_90822_, String p_90823_) {
        this(p_90821_, InputConstants.Type.KEYSYM, p_90822_, p_90823_);
    }

    public KeyMapping(String p_90825_, InputConstants.Type p_90826_, int p_90827_, String p_90828_) {
        this.name = p_90825_;
        this.key = p_90826_.getOrCreate(p_90827_);
        this.defaultKey = this.key;
        this.category = p_90828_;
        ALL.put(p_90825_, this);
        MAP.put(this.key, this);
        CATEGORIES.add(p_90828_);
    }

    public boolean isDown() {
        return this.isDown;
    }

    public String getCategory() {
        return this.category;
    }

    public boolean consumeClick() {
        if (this.clickCount == 0) {
            return false;
        } else {
            this.clickCount--;
            return true;
        }
    }

    private void release() {
        this.clickCount = 0;
        this.setDown(false);
    }

    public String getName() {
        return this.name;
    }

    public InputConstants.Key getDefaultKey() {
        return this.defaultKey;
    }

    public void setKey(InputConstants.Key p_90849_) {
        this.key = p_90849_;
    }

    public int compareTo(KeyMapping p_90841_) {
        return this.category.equals(p_90841_.category)
            ? I18n.get(this.name).compareTo(I18n.get(p_90841_.name))
            : CATEGORY_SORT_ORDER.get(this.category).compareTo(CATEGORY_SORT_ORDER.get(p_90841_.category));
    }

    public static Supplier<Component> createNameSupplier(String p_90843_) {
        KeyMapping keymapping = ALL.get(p_90843_);
        return keymapping == null ? () -> Component.translatable(p_90843_) : keymapping::getTranslatedKeyMessage;
    }

    public boolean same(KeyMapping p_90851_) {
        return this.key.equals(p_90851_.key);
    }

    public boolean isUnbound() {
        return this.key.equals(InputConstants.UNKNOWN);
    }

    public boolean matches(int p_90833_, int p_90834_) {
        return p_90833_ == InputConstants.UNKNOWN.getValue()
            ? this.key.getType() == InputConstants.Type.SCANCODE && this.key.getValue() == p_90834_
            : this.key.getType() == InputConstants.Type.KEYSYM && this.key.getValue() == p_90833_;
    }

    public boolean matchesMouse(int p_90831_) {
        return this.key.getType() == InputConstants.Type.MOUSE && this.key.getValue() == p_90831_;
    }

    public Component getTranslatedKeyMessage() {
        return this.key.getDisplayName();
    }

    public boolean isDefault() {
        return this.key.equals(this.defaultKey);
    }

    public String saveString() {
        return this.key.getName();
    }

    public void setDown(boolean p_90846_) {
        this.isDown = p_90846_;
    }

    @Nullable
    public static KeyMapping get(String p_378660_) {
        return ALL.get(p_378660_);
    }
}