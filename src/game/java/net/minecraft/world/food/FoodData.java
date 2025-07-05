package net.minecraft.world.food;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

public class FoodData {
    private int foodLevel = 20;
    private float saturationLevel = 5.0F;
    private float exhaustionLevel;
    private int tickTimer;

    private void add(int p_335153_, float p_332105_) {
        this.foodLevel = Mth.clamp(p_335153_ + this.foodLevel, 0, 20);
        this.saturationLevel = Mth.clamp(p_332105_ + this.saturationLevel, 0.0F, (float)this.foodLevel);
    }

    public void eat(int p_38708_, float p_38709_) {
        this.add(p_38708_, FoodConstants.saturationByModifier(p_38708_, p_38709_));
    }

    public void eat(FoodProperties p_345472_) {
        this.add(p_345472_.nutrition(), p_345472_.saturation());
    }

    public void tick(ServerPlayer p_369401_) {
        ServerLevel serverlevel = p_369401_.serverLevel();
        Difficulty difficulty = serverlevel.getDifficulty();
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean flag = serverlevel.getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        if (flag && this.saturationLevel > 0.0F && p_369401_.isHurt() && this.foodLevel >= 20) {
            this.tickTimer++;
            if (this.tickTimer >= 10) {
                float f = Math.min(this.saturationLevel, 6.0F);
                p_369401_.heal(f / 6.0F);
                this.addExhaustion(f);
                this.tickTimer = 0;
            }
        } else if (flag && this.foodLevel >= 18 && p_369401_.isHurt()) {
            this.tickTimer++;
            if (this.tickTimer >= 80) {
                p_369401_.heal(1.0F);
                this.addExhaustion(6.0F);
                this.tickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            this.tickTimer++;
            if (this.tickTimer >= 80) {
                if (p_369401_.getHealth() > 10.0F || difficulty == Difficulty.HARD || p_369401_.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
                    p_369401_.hurtServer(serverlevel, p_369401_.damageSources().starve(), 1.0F);
                }

                this.tickTimer = 0;
            }
        } else {
            this.tickTimer = 0;
        }
    }

    public void readAdditionalSaveData(CompoundTag p_38716_) {
        if (p_38716_.contains("foodLevel", 99)) {
            this.foodLevel = p_38716_.getInt("foodLevel");
            this.tickTimer = p_38716_.getInt("foodTickTimer");
            this.saturationLevel = p_38716_.getFloat("foodSaturationLevel");
            this.exhaustionLevel = p_38716_.getFloat("foodExhaustionLevel");
        }
    }

    public void addAdditionalSaveData(CompoundTag p_38720_) {
        p_38720_.putInt("foodLevel", this.foodLevel);
        p_38720_.putInt("foodTickTimer", this.tickTimer);
        p_38720_.putFloat("foodSaturationLevel", this.saturationLevel);
        p_38720_.putFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    public int getFoodLevel() {
        return this.foodLevel;
    }

    public boolean needsFood() {
        return this.foodLevel < 20;
    }

    public void addExhaustion(float p_38704_) {
        this.exhaustionLevel = Math.min(this.exhaustionLevel + p_38704_, 40.0F);
    }

    public float getSaturationLevel() {
        return this.saturationLevel;
    }

    public void setFoodLevel(int p_38706_) {
        this.foodLevel = p_38706_;
    }

    public void setSaturation(float p_38718_) {
        this.saturationLevel = p_38718_;
    }
}