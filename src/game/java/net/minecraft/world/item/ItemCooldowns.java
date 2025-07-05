package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldowns {
    private final Map<ResourceLocation, ItemCooldowns.CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(ItemStack p_369547_) {
        return this.getCooldownPercent(p_369547_, 0.0F) > 0.0F;
    }

    public float getCooldownPercent(ItemStack p_366950_, float p_41523_) {
        ResourceLocation resourcelocation = this.getCooldownGroup(p_366950_);
        ItemCooldowns.CooldownInstance itemcooldowns$cooldowninstance = this.cooldowns.get(resourcelocation);
        if (itemcooldowns$cooldowninstance != null) {
            float f = (float)(itemcooldowns$cooldowninstance.endTime - itemcooldowns$cooldowninstance.startTime);
            float f1 = (float)itemcooldowns$cooldowninstance.endTime - ((float)this.tickCount + p_41523_);
            return Mth.clamp(f1 / f, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        this.tickCount++;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Entry<ResourceLocation, ItemCooldowns.CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<ResourceLocation, ItemCooldowns.CooldownInstance> entry = iterator.next();
                if (entry.getValue().endTime <= this.tickCount) {
                    iterator.remove();
                    this.onCooldownEnded(entry.getKey());
                }
            }
        }
    }

    public ResourceLocation getCooldownGroup(ItemStack p_361933_) {
        UseCooldown usecooldown = p_361933_.get(DataComponents.USE_COOLDOWN);
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(p_361933_.getItem());
        return usecooldown == null ? resourcelocation : usecooldown.cooldownGroup().orElse(resourcelocation);
    }

    public void addCooldown(ItemStack p_366379_, int p_367584_) {
        this.addCooldown(this.getCooldownGroup(p_366379_), p_367584_);
    }

    public void addCooldown(ResourceLocation p_369799_, int p_41526_) {
        this.cooldowns.put(p_369799_, new ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + p_41526_));
        this.onCooldownStarted(p_369799_, p_41526_);
    }

    public void removeCooldown(ResourceLocation p_361396_) {
        this.cooldowns.remove(p_361396_);
        this.onCooldownEnded(p_361396_);
    }

    protected void onCooldownStarted(ResourceLocation p_361466_, int p_41530_) {
    }

    protected void onCooldownEnded(ResourceLocation p_370122_) {
    }

    static record CooldownInstance(int startTime, int endTime) {
    }
}