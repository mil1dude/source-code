package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class AttributeMap {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<Holder<Attribute>, AttributeInstance> attributes = new Object2ObjectOpenHashMap<>();
    private final Set<AttributeInstance> attributesToSync = new ObjectOpenHashSet<>();
    private final Set<AttributeInstance> attributesToUpdate = new ObjectOpenHashSet<>();
    private final AttributeSupplier supplier;

    public AttributeMap(AttributeSupplier p_22144_) {
        this.supplier = p_22144_;
    }

    private void onAttributeModified(AttributeInstance p_22158_) {
        this.attributesToUpdate.add(p_22158_);
        if (p_22158_.getAttribute().value().isClientSyncable()) {
            this.attributesToSync.add(p_22158_);
        }
    }

    public Set<AttributeInstance> getAttributesToSync() {
        return this.attributesToSync;
    }

    public Set<AttributeInstance> getAttributesToUpdate() {
        return this.attributesToUpdate;
    }

    public Collection<AttributeInstance> getSyncableAttributes() {
        return this.attributes.values().stream().filter(p_326797_ -> p_326797_.getAttribute().value().isClientSyncable()).collect(Collectors.toList());
    }

    @Nullable
    public AttributeInstance getInstance(Holder<Attribute> p_250010_) {
        return this.attributes.computeIfAbsent(p_250010_, p_326793_ -> this.supplier.createInstance(this::onAttributeModified, (Holder<Attribute>)p_326793_));
    }

    public boolean hasAttribute(Holder<Attribute> p_248893_) {
        return this.attributes.get(p_248893_) != null || this.supplier.hasAttribute(p_248893_);
    }

    public boolean hasModifier(Holder<Attribute> p_250299_, ResourceLocation p_343661_) {
        AttributeInstance attributeinstance = this.attributes.get(p_250299_);
        return attributeinstance != null ? attributeinstance.getModifier(p_343661_) != null : this.supplier.hasModifier(p_250299_, p_343661_);
    }

    public double getValue(Holder<Attribute> p_328238_) {
        AttributeInstance attributeinstance = this.attributes.get(p_328238_);
        return attributeinstance != null ? attributeinstance.getValue() : this.supplier.getValue(p_328238_);
    }

    public double getBaseValue(Holder<Attribute> p_329417_) {
        AttributeInstance attributeinstance = this.attributes.get(p_329417_);
        return attributeinstance != null ? attributeinstance.getBaseValue() : this.supplier.getBaseValue(p_329417_);
    }

    public double getModifierValue(Holder<Attribute> p_251534_, ResourceLocation p_343636_) {
        AttributeInstance attributeinstance = this.attributes.get(p_251534_);
        return attributeinstance != null ? attributeinstance.getModifier(p_343636_).amount() : this.supplier.getModifierValue(p_251534_, p_343636_);
    }

    public void addTransientAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> p_342579_) {
        p_342579_.forEach((p_341286_, p_341287_) -> {
            AttributeInstance attributeinstance = this.getInstance((Holder<Attribute>)p_341286_);
            if (attributeinstance != null) {
                attributeinstance.removeModifier(p_341287_.id());
                attributeinstance.addTransientModifier(p_341287_);
            }
        });
    }

    public void removeAttributeModifiers(Multimap<Holder<Attribute>, AttributeModifier> p_342034_) {
        p_342034_.asMap().forEach((p_341283_, p_341284_) -> {
            AttributeInstance attributeinstance = this.attributes.get(p_341283_);
            if (attributeinstance != null) {
                p_341284_.forEach(p_341289_ -> attributeinstance.removeModifier(p_341289_.id()));
            }
        });
    }

    public void assignAllValues(AttributeMap p_22160_) {
        p_22160_.attributes.values().forEach(p_326796_ -> {
            AttributeInstance attributeinstance = this.getInstance(p_326796_.getAttribute());
            if (attributeinstance != null) {
                attributeinstance.replaceFrom(p_326796_);
            }
        });
    }

    public void assignBaseValues(AttributeMap p_344183_) {
        p_344183_.attributes.values().forEach(p_341285_ -> {
            AttributeInstance attributeinstance = this.getInstance(p_341285_.getAttribute());
            if (attributeinstance != null) {
                attributeinstance.setBaseValue(p_341285_.getBaseValue());
            }
        });
    }

    public void assignPermanentModifiers(AttributeMap p_365307_) {
        p_365307_.attributes.values().forEach(p_358913_ -> {
            AttributeInstance attributeinstance = this.getInstance(p_358913_.getAttribute());
            if (attributeinstance != null) {
                attributeinstance.addPermanentModifiers(p_358913_.getPermanentModifiers());
            }
        });
    }

    public boolean resetBaseValue(Holder<Attribute> p_377122_) {
        if (!this.supplier.hasAttribute(p_377122_)) {
            return false;
        } else {
            AttributeInstance attributeinstance = this.attributes.get(p_377122_);
            if (attributeinstance != null) {
                attributeinstance.setBaseValue(this.supplier.getBaseValue(p_377122_));
            }

            return true;
        }
    }

    public ListTag save() {
        ListTag listtag = new ListTag();

        for (AttributeInstance attributeinstance : this.attributes.values()) {
            listtag.add(attributeinstance.save());
        }

        return listtag;
    }

    public void load(ListTag p_22169_) {
        for (int i = 0; i < p_22169_.size(); i++) {
            CompoundTag compoundtag = p_22169_.getCompound(i);
            String s = compoundtag.getString("id");
            ResourceLocation resourcelocation = ResourceLocation.tryParse(s);
            if (resourcelocation != null) {
                Util.ifElse(BuiltInRegistries.ATTRIBUTE.get(resourcelocation), p_326795_ -> {
                    AttributeInstance attributeinstance = this.getInstance(p_326795_);
                    if (attributeinstance != null) {
                        attributeinstance.load(compoundtag);
                    }
                }, () -> LOGGER.warn("Ignoring unknown attribute '{}'", resourcelocation));
            } else {
                LOGGER.warn("Ignoring malformed attribute '{}'", s);
            }
        }
    }
}