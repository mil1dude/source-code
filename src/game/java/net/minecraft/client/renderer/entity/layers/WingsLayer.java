package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WingsLayer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    private final ElytraModel elytraModel;
    private final ElytraModel elytraBabyModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public WingsLayer(RenderLayerParent<S, M> p_366720_, EntityModelSet p_369504_, EquipmentLayerRenderer p_361718_) {
        super(p_366720_);
        this.elytraModel = new ElytraModel(p_369504_.bakeLayer(ModelLayers.ELYTRA));
        this.elytraBabyModel = new ElytraModel(p_369504_.bakeLayer(ModelLayers.ELYTRA_BABY));
        this.equipmentRenderer = p_361718_;
    }

    public void render(PoseStack p_362037_, MultiBufferSource p_368252_, int p_364275_, S p_368470_, float p_368401_, float p_362513_) {
        ItemStack itemstack = p_368470_.chestEquipment;
        Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            ResourceLocation resourcelocation = getPlayerElytraTexture(p_368470_);
            ElytraModel elytramodel = p_368470_.isBaby ? this.elytraBabyModel : this.elytraModel;
            p_362037_.pushPose();
            p_362037_.translate(0.0F, 0.0F, 0.125F);
            elytramodel.setupAnim(p_368470_);
            this.equipmentRenderer
                .renderLayers(
                    EquipmentClientInfo.LayerType.WINGS,
                    equippable.assetId().get(),
                    elytramodel,
                    itemstack,
                    p_362037_,
                    p_368252_,
                    p_364275_,
                    resourcelocation
                );
            p_362037_.popPose();
        }
    }

    @Nullable
    private static ResourceLocation getPlayerElytraTexture(HumanoidRenderState p_364125_) {
        if (p_364125_ instanceof PlayerRenderState playerrenderstate) {
            PlayerSkin playerskin = playerrenderstate.skin;
            if (playerskin.elytraTexture() != null) {
                return playerskin.elytraTexture();
            }

            if (playerskin.capeTexture() != null && playerrenderstate.showCape) {
                return playerskin.capeTexture();
            }
        }

        return null;
    }
}