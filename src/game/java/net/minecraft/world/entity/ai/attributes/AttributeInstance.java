package net.minecraft.world.entity.ai.attributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class AttributeInstance {
    private static final String BASE_FIELD = "base";
    private static final String MODIFIERS_FIELD = "modifiers";
    public static final String ID_FIELD = "id";
    private final Holder<Attribute> attribute;
    private final Map<AttributeModifier.Operation, Map<ResourceLocation, AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<ResourceLocation, AttributeModifier> modifierById = new Object2ObjectArrayMap<>();
    private final Map<ResourceLocation, AttributeModifier> permanentModifiers = new Object2ObjectArrayMap<>();
    private double baseValue;
    private boolean dirty = true;
    private double cachedValue;
    private final Consumer<AttributeInstance> onDirty;

    public AttributeInstance(Holder<Attribute> p_335359_, Consumer<AttributeInstance> p_22098_) {
        this.attribute = p_335359_;
        this.onDirty = p_22098_;
        this.baseValue = p_335359_.value().getDefaultValue();
    }

    public Holder<Attribute> getAttribute() {
        return this.attribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double p_22101_) {
        if (p_22101_ != this.baseValue) {
            this.baseValue = p_22101_;
            this.setDirty();
        }
    }

    @VisibleForTesting
    Map<ResourceLocation, AttributeModifier> getModifiers(AttributeModifier.Operation p_22105_) {
        return this.modifiersByOperation.computeIfAbsent(p_22105_, p_326790_ -> new Object2ObjectOpenHashMap<>());
    }

    public Set<AttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }

    public Set<AttributeModifier> getPermanentModifiers() {
        return ImmutableSet.copyOf(this.permanentModifiers.values());
    }

    @Nullable
    public AttributeModifier getModifier(ResourceLocation p_344264_) {
        return this.modifierById.get(p_344264_);
    }

    public boolean hasModifier(ResourceLocation p_344370_) {
        return this.modifierById.get(p_344370_) != null;
    }

    private void addModifier(AttributeModifier p_22134_) {
        AttributeModifier attributemodifier = this.modifierById.putIfAbsent(p_22134_.id(), p_22134_);
        if (attributemodifier != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            this.getModifiers(p_22134_.operation()).put(p_22134_.id(), p_22134_);
            this.setDirty();
        }
    }

    public void addOrUpdateTransientModifier(AttributeModifier p_327789_) {
        AttributeModifier attributemodifier = this.modifierById.put(p_327789_.id(), p_327789_);
        if (p_327789_ != attributemodifier) {
            this.getModifiers(p_327789_.operation()).put(p_327789_.id(), p_327789_);
            this.setDirty();
        }
    }

    public void addTransientModifier(AttributeModifier p_22119_) {
        this.addModifier(p_22119_);
    }

    public void addOrReplacePermanentModifier(AttributeModifier p_343885_) {
        this.removeModifier(p_343885_.id());
        this.addModifier(p_343885_);
        this.permanentModifiers.put(p_343885_.id(), p_343885_);
    }

    public void addPermanentModifier(AttributeModifier p_22126_) {
        this.addModifier(p_22126_);
        this.permanentModifiers.put(p_22126_.id(), p_22126_);
    }

    public void addPermanentModifiers(Collection<AttributeModifier> p_366375_) {
        for (AttributeModifier attributemodifier : p_366375_) {
            this.addPermanentModifier(attributemodifier);
        }
    }

    protected void setDirty() {
        this.dirty = true;
        this.onDirty.accept(this);
    }

    public void removeModifier(AttributeModifier p_22131_) {
        this.removeModifier(p_22131_.id());
    }

    public boolean removeModifier(ResourceLocation p_344753_) {
        AttributeModifier attributemodifier = this.modifierById.remove(p_344753_);
        if (attributemodifier == null) {
            return false;
        } else {
            this.getModifiers(attributemodifier.operation()).remove(p_344753_);
            this.permanentModifiers.remove(p_344753_);
            this.setDirty();
            return true;
        }
    }

    public void removeModifiers() {
        for (AttributeModifier attributemodifier : this.getModifiers()) {
            this.removeModifier(attributemodifier);
        }
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }

        return this.cachedValue;
    }

    private double calculateValue() {
        double d0 = this.getBaseValue();

        for (AttributeModifier attributemodifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
            d0 += attributemodifier.amount();
        }

        double d1 = d0;

        for (AttributeModifier attributemodifier1 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            d1 += d0 * attributemodifier1.amount();
        }

        for (AttributeModifier attributemodifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            d1 *= 1.0 + attributemodifier2.amount();
        }

        return this.attribute.value().sanitizeValue(d1);
    }

    private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation p_22117_) {
        return this.modifiersByOperation.getOrDefault(p_22117_, Map.of()).values();
    }

    public void replaceFrom(AttributeInstance p_22103_) {
        this.baseValue = p_22103_.baseValue;
        this.modifierById.clear();
        this.modifierById.putAll(p_22103_.modifierById);
        this.permanentModifiers.clear();
        this.permanentModifiers.putAll(p_22103_.permanentModifiers);
        this.modifiersByOperation.clear();
        p_22103_.modifiersByOperation
            .forEach((p_326791_, p_326792_) -> this.getModifiers(p_326791_).putAll((Map<? extends ResourceLocation, ? extends AttributeModifier>)p_326792_));
        this.setDirty();
    }

    public CompoundTag save() {
        CompoundTag compoundtag = new CompoundTag();
        ResourceKey<Attribute> resourcekey = this.attribute
            .unwrapKey()
            .orElseThrow(() -> new IllegalStateException("Tried to serialize unregistered attribute"));
        compoundtag.putString("id", resourcekey.location().toString());
        compoundtag.putDouble("base", this.baseValue);
        if (!this.permanentModifiers.isEmpty()) {
            ListTag listtag = new ListTag();

            for (AttributeModifier attributemodifier : this.permanentModifiers.values()) {
                listtag.add(attributemodifier.save());
            }

            compoundtag.put("modifiers", listtag);
        }

        return compoundtag;
    }

    public void load(CompoundTag p_22114_) {
        this.baseValue = p_22114_.getDouble("base");
        if (p_22114_.contains("modifiers", 9)) {
            ListTag listtag = p_22114_.getList("modifiers", 10);

            for (int i = 0; i < listtag.size(); i++) {
                AttributeModifier attributemodifier = AttributeModifier.load(listtag.getCompound(i));
                if (attributemodifier != null) {
                    this.modifierById.put(attributemodifier.id(), attributemodifier);
                    this.getModifiers(attributemodifier.operation()).put(attributemodifier.id(), attributemodifier);
                    this.permanentModifiers.put(attributemodifier.id(), attributemodifier);
                }
            }
        }

        this.setDirty();
    }
}