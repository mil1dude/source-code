package net.minecraft.client.renderer.item.properties.numeric;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class NeedleDirectionHelper {
    private final boolean wobble;

    protected NeedleDirectionHelper(boolean p_376496_) {
        this.wobble = p_376496_;
    }

    public float get(ItemStack p_377286_, @Nullable ClientLevel p_378790_, @Nullable LivingEntity p_375514_, int p_375400_) {
        Entity entity = (Entity)(p_375514_ != null ? p_375514_ : p_377286_.getEntityRepresentation());
        if (entity == null) {
            return 0.0F;
        } else {
            if (p_378790_ == null && entity.level() instanceof ClientLevel clientlevel) {
                p_378790_ = clientlevel;
            }

            return p_378790_ == null ? 0.0F : this.calculate(p_377286_, p_378790_, p_375400_, entity);
        }
    }

    protected abstract float calculate(ItemStack p_378355_, ClientLevel p_377407_, int p_377397_, Entity p_377026_);

    protected boolean wobble() {
        return this.wobble;
    }

    protected NeedleDirectionHelper.Wobbler newWobbler(float p_376684_) {
        return this.wobble ? standardWobbler(p_376684_) : nonWobbler();
    }

    public static NeedleDirectionHelper.Wobbler standardWobbler(final float p_376618_) {
        return new NeedleDirectionHelper.Wobbler() {
            private float rotation;
            private float deltaRotation;
            private long lastUpdateTick;

            @Override
            public float rotation() {
                return this.rotation;
            }

            @Override
            public boolean shouldUpdate(long p_378605_) {
                return this.lastUpdateTick != p_378605_;
            }

            @Override
            public void update(long p_378620_, float p_377269_) {
                this.lastUpdateTick = p_378620_;
                float f = Mth.positiveModulo(p_377269_ - this.rotation + 0.5F, 1.0F) - 0.5F;
                this.deltaRotation += f * 0.1F;
                this.deltaRotation = this.deltaRotation * p_376618_;
                this.rotation = Mth.positiveModulo(this.rotation + this.deltaRotation, 1.0F);
            }
        };
    }

    public static NeedleDirectionHelper.Wobbler nonWobbler() {
        return new NeedleDirectionHelper.Wobbler() {
            private float targetValue;

            @Override
            public float rotation() {
                return this.targetValue;
            }

            @Override
            public boolean shouldUpdate(long p_377058_) {
                return true;
            }

            @Override
            public void update(long p_377475_, float p_376108_) {
                this.targetValue = p_376108_;
            }
        };
    }

    @OnlyIn(Dist.CLIENT)
    public interface Wobbler {
        float rotation();

        boolean shouldUpdate(long p_378139_);

        void update(long p_377073_, float p_376320_);
    }
}