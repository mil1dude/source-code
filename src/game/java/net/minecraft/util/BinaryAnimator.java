package net.minecraft.util;

public class BinaryAnimator {
    private final int animationLength;
    private final BinaryAnimator.EasingFunction easingFunction;
    private int ticks;
    private int ticksOld;

    public BinaryAnimator(int p_368328_, BinaryAnimator.EasingFunction p_370148_) {
        this.animationLength = p_368328_;
        this.easingFunction = p_370148_;
    }

    public BinaryAnimator(int p_365144_) {
        this(p_365144_, p_364253_ -> p_364253_);
    }

    public void tick(boolean p_364056_) {
        this.ticksOld = this.ticks;
        if (p_364056_) {
            if (this.ticks < this.animationLength) {
                this.ticks++;
            }
        } else if (this.ticks > 0) {
            this.ticks--;
        }
    }

    public float getFactor(float p_364595_) {
        float f = Mth.lerp(p_364595_, (float)this.ticksOld, (float)this.ticks) / (float)this.animationLength;
        return this.easingFunction.apply(f);
    }

    public interface EasingFunction {
        float apply(float p_363249_);
    }
}