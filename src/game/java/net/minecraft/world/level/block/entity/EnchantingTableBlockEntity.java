package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EnchantingTableBlockEntity extends BlockEntity implements Nameable {
    public int time;
    public float flip;
    public float oFlip;
    public float flipT;
    public float flipA;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;
    private static final RandomSource RANDOM = RandomSource.create();
    @Nullable
    private Component name;

    public EnchantingTableBlockEntity(BlockPos p_329912_, BlockState p_331662_) {
        super(BlockEntityType.ENCHANTING_TABLE, p_329912_, p_331662_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_329203_, HolderLookup.Provider p_335261_) {
        super.saveAdditional(p_329203_, p_335261_);
        if (this.hasCustomName()) {
            p_329203_.putString("CustomName", Component.Serializer.toJson(this.name, p_335261_));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag p_333729_, HolderLookup.Provider p_333480_) {
        super.loadAdditional(p_333729_, p_333480_);
        if (p_333729_.contains("CustomName", 8)) {
            this.name = parseCustomNameSafe(p_333729_.getString("CustomName"), p_333480_);
        }
    }

    public static void bookAnimationTick(Level p_334676_, BlockPos p_332815_, BlockState p_332072_, EnchantingTableBlockEntity p_333258_) {
        p_333258_.oOpen = p_333258_.open;
        p_333258_.oRot = p_333258_.rot;
        Player player = p_334676_.getNearestPlayer(
            (double)p_332815_.getX() + 0.5, (double)p_332815_.getY() + 0.5, (double)p_332815_.getZ() + 0.5, 3.0, false
        );
        if (player != null) {
            double d0 = player.getX() - ((double)p_332815_.getX() + 0.5);
            double d1 = player.getZ() - ((double)p_332815_.getZ() + 0.5);
            p_333258_.tRot = (float)Mth.atan2(d1, d0);
            p_333258_.open += 0.1F;
            if (p_333258_.open < 0.5F || RANDOM.nextInt(40) == 0) {
                float f1 = p_333258_.flipT;

                do {
                    p_333258_.flipT = p_333258_.flipT + (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f1 == p_333258_.flipT);
            }
        } else {
            p_333258_.tRot += 0.02F;
            p_333258_.open -= 0.1F;
        }

        while (p_333258_.rot >= (float) Math.PI) {
            p_333258_.rot -= (float) (Math.PI * 2);
        }

        while (p_333258_.rot < (float) -Math.PI) {
            p_333258_.rot += (float) (Math.PI * 2);
        }

        while (p_333258_.tRot >= (float) Math.PI) {
            p_333258_.tRot -= (float) (Math.PI * 2);
        }

        while (p_333258_.tRot < (float) -Math.PI) {
            p_333258_.tRot += (float) (Math.PI * 2);
        }

        float f2 = p_333258_.tRot - p_333258_.rot;

        while (f2 >= (float) Math.PI) {
            f2 -= (float) (Math.PI * 2);
        }

        while (f2 < (float) -Math.PI) {
            f2 += (float) (Math.PI * 2);
        }

        p_333258_.rot += f2 * 0.4F;
        p_333258_.open = Mth.clamp(p_333258_.open, 0.0F, 1.0F);
        p_333258_.time++;
        p_333258_.oFlip = p_333258_.flip;
        float f = (p_333258_.flipT - p_333258_.flip) * 0.4F;
        float f3 = 0.2F;
        f = Mth.clamp(f, -0.2F, 0.2F);
        p_333258_.flipA = p_333258_.flipA + (f - p_333258_.flipA) * 0.9F;
        p_333258_.flip = p_333258_.flip + p_333258_.flipA;
    }

    @Override
    public Component getName() {
        return (Component)(this.name != null ? this.name : Component.translatable("container.enchant"));
    }

    public void setCustomName(@Nullable Component p_330108_) {
        this.name = p_330108_;
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return this.name;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput p_333936_) {
        super.applyImplicitComponents(p_333936_);
        this.name = p_333936_.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder p_334287_) {
        super.collectImplicitComponents(p_334287_);
        p_334287_.set(DataComponents.CUSTOM_NAME, this.name);
    }

    @Override
    public void removeComponentsFromTag(CompoundTag p_330630_) {
        p_330630_.remove("CustomName");
    }
}