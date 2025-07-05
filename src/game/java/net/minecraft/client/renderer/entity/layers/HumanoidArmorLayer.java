package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> extends RenderLayer<S, M> {
    private final A innerModel;
    private final A outerModel;
    private final A innerModelBaby;
    private final A outerModelBaby;
    private final EquipmentLayerRenderer equipmentRenderer;

    public HumanoidArmorLayer(RenderLayerParent<S, M> p_267286_, A p_267110_, A p_267150_, EquipmentLayerRenderer p_369441_) {
        this(p_267286_, p_267110_, p_267150_, p_267110_, p_267150_, p_369441_);
    }

    public HumanoidArmorLayer(RenderLayerParent<S, M> p_364333_, A p_368953_, A p_369993_, A p_363840_, A p_363634_, EquipmentLayerRenderer p_361027_) {
        super(p_364333_);
        this.innerModel = p_368953_;
        this.outerModel = p_369993_;
        this.innerModelBaby = p_363840_;
        this.outerModelBaby = p_363634_;
        this.equipmentRenderer = p_361027_;
    }

    public static boolean shouldRender(ItemStack p_362744_, EquipmentSlot p_366990_) {
        Equippable equippable = p_362744_.get(DataComponents.EQUIPPABLE);
        return equippable != null && shouldRender(equippable, p_366990_);
    }

    private static boolean shouldRender(Equippable p_369539_, EquipmentSlot p_369578_) {
        return p_369539_.assetId().isPresent() && p_369539_.slot() == p_369578_;
    }

    public void render(PoseStack p_117085_, MultiBufferSource p_117086_, int p_117087_, S p_364101_, float p_117089_, float p_117090_) {
        this.renderArmorPiece(p_117085_, p_117086_, p_364101_.chestEquipment, EquipmentSlot.CHEST, p_117087_, this.getArmorModel(p_364101_, EquipmentSlot.CHEST));
        this.renderArmorPiece(p_117085_, p_117086_, p_364101_.legsEquipment, EquipmentSlot.LEGS, p_117087_, this.getArmorModel(p_364101_, EquipmentSlot.LEGS));
        this.renderArmorPiece(p_117085_, p_117086_, p_364101_.feetEquipment, EquipmentSlot.FEET, p_117087_, this.getArmorModel(p_364101_, EquipmentSlot.FEET));
        this.renderArmorPiece(p_117085_, p_117086_, p_364101_.headEquipment, EquipmentSlot.HEAD, p_117087_, this.getArmorModel(p_364101_, EquipmentSlot.HEAD));
    }

    private void renderArmorPiece(PoseStack p_117119_, MultiBufferSource p_117120_, ItemStack p_366444_, EquipmentSlot p_117122_, int p_117123_, A p_117124_) {
        Equippable equippable = p_366444_.get(DataComponents.EQUIPPABLE);
        if (equippable != null && shouldRender(equippable, p_117122_)) {
            this.getParentModel().copyPropertiesTo(p_117124_);
            this.setPartVisibility(p_117124_, p_117122_);
            EquipmentClientInfo.LayerType equipmentclientinfo$layertype = this.usesInnerModel(p_117122_)
                ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS
                : EquipmentClientInfo.LayerType.HUMANOID;
            this.equipmentRenderer
                .renderLayers(equipmentclientinfo$layertype, equippable.assetId().orElseThrow(), p_117124_, p_366444_, p_117119_, p_117120_, p_117123_);
        }
    }

    protected void setPartVisibility(A p_117126_, EquipmentSlot p_117127_) {
        p_117126_.setAllVisible(false);
        switch (p_117127_) {
            case HEAD:
                p_117126_.head.visible = true;
                p_117126_.hat.visible = true;
                break;
            case CHEST:
                p_117126_.body.visible = true;
                p_117126_.rightArm.visible = true;
                p_117126_.leftArm.visible = true;
                break;
            case LEGS:
                p_117126_.body.visible = true;
                p_117126_.rightLeg.visible = true;
                p_117126_.leftLeg.visible = true;
                break;
            case FEET:
                p_117126_.rightLeg.visible = true;
                p_117126_.leftLeg.visible = true;
        }
    }

    private A getArmorModel(S p_363587_, EquipmentSlot p_117079_) {
        if (this.usesInnerModel(p_117079_)) {
            return p_363587_.isBaby ? this.innerModelBaby : this.innerModel;
        } else {
            return p_363587_.isBaby ? this.outerModelBaby : this.outerModel;
        }
    }

    private boolean usesInnerModel(EquipmentSlot p_117129_) {
        return p_117129_ == EquipmentSlot.LEGS;
    }
}