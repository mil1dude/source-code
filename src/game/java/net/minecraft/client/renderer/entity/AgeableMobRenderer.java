package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@Deprecated
@OnlyIn(Dist.CLIENT)
public abstract class AgeableMobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends MobRenderer<T, S, M> {
    private final M adultModel;
    private final M babyModel;

    public AgeableMobRenderer(EntityRendererProvider.Context p_367262_, M p_369985_, M p_363518_, float p_363526_) {
        super(p_367262_, p_369985_, p_363526_);
        this.adultModel = p_369985_;
        this.babyModel = p_363518_;
    }

    @Override
    public void render(S p_363116_, PoseStack p_363662_, MultiBufferSource p_366693_, int p_363199_) {
        this.model = p_363116_.isBaby ? this.babyModel : this.adultModel;
        super.render(p_363116_, p_363662_, p_366693_, p_363199_);
    }
}