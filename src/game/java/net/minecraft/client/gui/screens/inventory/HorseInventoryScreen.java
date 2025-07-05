package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final ResourceLocation CHEST_SLOTS_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/chest_slots");
    private static final ResourceLocation HORSE_INVENTORY_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/horse.png");
    private final AbstractHorse horse;
    private final int inventoryColumns;
    private float xMouse;
    private float yMouse;

    public HorseInventoryScreen(HorseInventoryMenu p_98817_, Inventory p_98818_, AbstractHorse p_98819_, int p_342509_) {
        super(p_98817_, p_98818_, p_98819_.getDisplayName());
        this.horse = p_98819_;
        this.inventoryColumns = p_342509_;
    }

    @Override
    protected void renderBg(GuiGraphics p_282553_, float p_282998_, int p_282929_, int p_283133_) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        p_282553_.blit(RenderType::guiTextured, HORSE_INVENTORY_LOCATION, i, j, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        if (this.inventoryColumns > 0) {
            p_282553_.blitSprite(RenderType::guiTextured, CHEST_SLOTS_SPRITE, 90, 54, 0, 0, i + 79, j + 17, this.inventoryColumns * 18, 54);
        }

        if (this.horse.isSaddleable()) {
            this.drawSlot(p_282553_, i + 7, j + 35 - 18);
        }

        if (this.horse.canUseSlot(EquipmentSlot.BODY)) {
            this.drawSlot(p_282553_, i + 7, j + 35);
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(p_282553_, i + 26, j + 18, i + 78, j + 70, 17, 0.25F, this.xMouse, this.yMouse, this.horse);
    }

    private void drawSlot(GuiGraphics p_377302_, int p_375725_, int p_375686_) {
        p_377302_.blitSprite(RenderType::guiTextured, SLOT_SPRITE, p_375725_, p_375686_, 18, 18);
    }

    @Override
    public void render(GuiGraphics p_281697_, int p_282103_, int p_283529_, float p_283079_) {
        this.xMouse = (float)p_282103_;
        this.yMouse = (float)p_283529_;
        super.render(p_281697_, p_282103_, p_283529_, p_283079_);
        this.renderTooltip(p_281697_, p_282103_, p_283529_);
    }
}