package net.minecraft.client.renderer.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RabbitRenderer extends AgeableMobRenderer<Rabbit, RabbitRenderState, RabbitModel> {
    private static final ResourceLocation RABBIT_BROWN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/brown.png");
    private static final ResourceLocation RABBIT_WHITE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/white.png");
    private static final ResourceLocation RABBIT_BLACK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/black.png");
    private static final ResourceLocation RABBIT_GOLD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/gold.png");
    private static final ResourceLocation RABBIT_SALT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/salt.png");
    private static final ResourceLocation RABBIT_WHITE_SPLOTCHED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/white_splotched.png");
    private static final ResourceLocation RABBIT_TOAST_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/toast.png");
    private static final ResourceLocation RABBIT_EVIL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/caerbannog.png");

    public RabbitRenderer(EntityRendererProvider.Context p_174360_) {
        super(p_174360_, new RabbitModel(p_174360_.bakeLayer(ModelLayers.RABBIT)), new RabbitModel(p_174360_.bakeLayer(ModelLayers.RABBIT_BABY)), 0.3F);
    }

    public ResourceLocation getTextureLocation(RabbitRenderState p_362493_) {
        if (p_362493_.isToast) {
            return RABBIT_TOAST_LOCATION;
        } else {
            return switch (p_362493_.variant) {
                case BROWN -> RABBIT_BROWN_LOCATION;
                case WHITE -> RABBIT_WHITE_LOCATION;
                case BLACK -> RABBIT_BLACK_LOCATION;
                case GOLD -> RABBIT_GOLD_LOCATION;
                case SALT -> RABBIT_SALT_LOCATION;
                case WHITE_SPLOTCHED -> RABBIT_WHITE_SPLOTCHED_LOCATION;
                case EVIL -> RABBIT_EVIL_LOCATION;
            };
        }
    }

    public RabbitRenderState createRenderState() {
        return new RabbitRenderState();
    }

    public void extractRenderState(Rabbit p_363089_, RabbitRenderState p_365846_, float p_365965_) {
        super.extractRenderState(p_363089_, p_365846_, p_365965_);
        p_365846_.jumpCompletion = p_363089_.getJumpCompletion(p_365965_);
        p_365846_.isToast = "Toast".equals(ChatFormatting.stripFormatting(p_363089_.getName().getString()));
        p_365846_.variant = p_363089_.getVariant();
    }
}