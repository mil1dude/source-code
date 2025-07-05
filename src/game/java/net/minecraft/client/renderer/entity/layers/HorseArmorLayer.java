package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseArmorLayer extends RenderLayer<HorseRenderState, HorseModel> {
    private final HorseModel adultModel;
    private final HorseModel babyModel;
    private final EquipmentLayerRenderer equipmentRenderer;

    public HorseArmorLayer(RenderLayerParent<HorseRenderState, HorseModel> p_174496_, EntityModelSet p_174497_, EquipmentLayerRenderer p_367517_) {
        super(p_174496_);
        this.equipmentRenderer = p_367517_;
        this.adultModel = new HorseModel(p_174497_.bakeLayer(ModelLayers.HORSE_ARMOR));
        this.babyModel = new HorseModel(p_174497_.bakeLayer(ModelLayers.HORSE_BABY_ARMOR));
    }

    public void render(PoseStack p_117021_, MultiBufferSource p_117022_, int p_117023_, HorseRenderState p_363397_, float p_117025_, float p_117026_) {
        ItemStack itemstack = p_363397_.bodyArmorItem;
        Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && !equippable.assetId().isEmpty()) {
            HorseModel horsemodel = p_363397_.isBaby ? this.babyModel : this.adultModel;
            horsemodel.setupAnim(p_363397_);
            this.equipmentRenderer
                .renderLayers(EquipmentClientInfo.LayerType.HORSE_BODY, equippable.assetId().get(), horsemodel, itemstack, p_117021_, p_117022_, p_117023_);
        }
    }
}