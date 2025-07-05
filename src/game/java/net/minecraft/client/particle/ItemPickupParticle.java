package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ItemPickupParticle extends Particle {
    private static final int LIFE_TIME = 3;
    private final Entity itemEntity;
    private final Entity target;
    private int life;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private double targetX;
    private double targetY;
    private double targetZ;
    private double targetXOld;
    private double targetYOld;
    private double targetZOld;

    public ItemPickupParticle(EntityRenderDispatcher p_107023_, ClientLevel p_107025_, Entity p_107026_, Entity p_107027_) {
        this(p_107023_, p_107025_, p_107026_, p_107027_, p_107026_.getDeltaMovement());
    }

    private ItemPickupParticle(EntityRenderDispatcher p_107029_, ClientLevel p_107031_, Entity p_107032_, Entity p_107033_, Vec3 p_107034_) {
        super(p_107031_, p_107032_.getX(), p_107032_.getY(), p_107032_.getZ(), p_107034_.x, p_107034_.y, p_107034_.z);
        this.itemEntity = this.getSafeCopy(p_107032_);
        this.target = p_107033_;
        this.entityRenderDispatcher = p_107029_;
        this.updatePosition();
        this.saveOldPosition();
    }

    private Entity getSafeCopy(Entity p_107037_) {
        return (Entity)(!(p_107037_ instanceof ItemEntity) ? p_107037_ : ((ItemEntity)p_107037_).copy());
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void renderCustom(PoseStack p_375620_, MultiBufferSource p_377939_, Camera p_376327_, float p_377781_) {
        float f = ((float)this.life + p_377781_) / 3.0F;
        f *= f;
        double d0 = Mth.lerp((double)p_377781_, this.targetXOld, this.targetX);
        double d1 = Mth.lerp((double)p_377781_, this.targetYOld, this.targetY);
        double d2 = Mth.lerp((double)p_377781_, this.targetZOld, this.targetZ);
        double d3 = Mth.lerp((double)f, this.itemEntity.getX(), d0);
        double d4 = Mth.lerp((double)f, this.itemEntity.getY(), d1);
        double d5 = Mth.lerp((double)f, this.itemEntity.getZ(), d2);
        Vec3 vec3 = p_376327_.getPosition();
        this.entityRenderDispatcher
            .render(
                this.itemEntity,
                d3 - vec3.x(),
                d4 - vec3.y(),
                d5 - vec3.z(),
                p_377781_,
                new PoseStack(),
                p_377939_,
                this.entityRenderDispatcher.getPackedLightCoords(this.itemEntity, p_377781_)
            );
    }

    @Override
    public void render(VertexConsumer p_107039_, Camera p_107040_, float p_107041_) {
    }

    @Override
    public void tick() {
        this.life++;
        if (this.life == 3) {
            this.remove();
        }

        this.saveOldPosition();
        this.updatePosition();
    }

    private void updatePosition() {
        this.targetX = this.target.getX();
        this.targetY = (this.target.getY() + this.target.getEyeY()) / 2.0;
        this.targetZ = this.target.getZ();
    }

    private void saveOldPosition() {
        this.targetXOld = this.targetX;
        this.targetYOld = this.targetY;
        this.targetZOld = this.targetZ;
    }
}