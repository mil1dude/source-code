package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlayerSkinWidget extends AbstractWidget {
    private static final float MODEL_OFFSET = 0.0625F;
    private static final float MODEL_HEIGHT = 2.125F;
    private static final float Z_OFFSET = 100.0F;
    private static final float ROTATION_SENSITIVITY = 2.5F;
    private static final float DEFAULT_ROTATION_X = -5.0F;
    private static final float DEFAULT_ROTATION_Y = 30.0F;
    private static final float ROTATION_X_LIMIT = 50.0F;
    private final PlayerSkinWidget.Model model;
    private final Supplier<PlayerSkin> skin;
    private float rotationX = -5.0F;
    private float rotationY = 30.0F;

    public PlayerSkinWidget(int p_299990_, int p_297411_, EntityModelSet p_298438_, Supplier<PlayerSkin> p_299497_) {
        super(0, 0, p_299990_, p_297411_, CommonComponents.EMPTY);
        this.model = PlayerSkinWidget.Model.bake(p_298438_);
        this.skin = p_299497_;
    }

    @Override
    protected void renderWidget(GuiGraphics p_298610_, int p_299860_, int p_299420_, float p_300463_) {
        p_298610_.pose().pushPose();
        p_298610_.pose().translate((float)this.getX() + (float)this.getWidth() / 2.0F, (float)(this.getY() + this.getHeight()), 100.0F);
        float f = (float)this.getHeight() / 2.125F;
        p_298610_.pose().scale(f, f, f);
        p_298610_.pose().translate(0.0F, -0.0625F, 0.0F);
        p_298610_.pose().rotateAround(Axis.XP.rotationDegrees(this.rotationX), 0.0F, -1.0625F, 0.0F);
        p_298610_.pose().mulPose(Axis.YP.rotationDegrees(this.rotationY));
        p_298610_.flush();
        Lighting.setupForEntityInInventory(Axis.XP.rotationDegrees(this.rotationX));
        this.model.render(p_298610_, this.skin.get());
        p_298610_.flush();
        Lighting.setupFor3DItems();
        p_298610_.pose().popPose();
    }

    @Override
    protected void onDrag(double p_301243_, double p_297441_, double p_301242_, double p_297777_) {
        this.rotationX = Mth.clamp(this.rotationX - (float)p_297777_ * 2.5F, -50.0F, 50.0F);
        this.rotationY += (float)p_301242_ * 2.5F;
    }

    @Override
    public void playDownSound(SoundManager p_299795_) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_298811_) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Nullable
    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent p_300388_) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    static record Model(PlayerModel wideModel, PlayerModel slimModel) {
        public static PlayerSkinWidget.Model bake(EntityModelSet p_300414_) {
            PlayerModel playermodel = new PlayerModel(p_300414_.bakeLayer(ModelLayers.PLAYER), false);
            PlayerModel playermodel1 = new PlayerModel(p_300414_.bakeLayer(ModelLayers.PLAYER_SLIM), true);
            return new PlayerSkinWidget.Model(playermodel, playermodel1);
        }

        public void render(GuiGraphics p_299673_, PlayerSkin p_297884_) {
            p_299673_.pose().pushPose();
            p_299673_.pose().scale(1.0F, 1.0F, -1.0F);
            p_299673_.pose().translate(0.0F, -1.501F, 0.0F);
            PlayerModel playermodel = p_297884_.model() == PlayerSkin.Model.SLIM ? this.slimModel : this.wideModel;
            RenderType rendertype = playermodel.renderType(p_297884_.texture());
            p_299673_.drawSpecial(p_366660_ -> playermodel.renderToBuffer(p_299673_.pose(), p_366660_.getBuffer(rendertype), 15728880, OverlayTexture.NO_OVERLAY));
            p_299673_.pose().popPose();
        }
    }
}