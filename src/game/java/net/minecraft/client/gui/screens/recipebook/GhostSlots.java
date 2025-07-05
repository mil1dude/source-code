package net.minecraft.client.gui.screens.recipebook;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhostSlots {
    private final Reference2ObjectMap<Slot, GhostSlots.GhostSlot> ingredients = new Reference2ObjectArrayMap<>();
    private final SlotSelectTime slotSelectTime;

    public GhostSlots(SlotSelectTime p_365573_) {
        this.slotSelectTime = p_365573_;
    }

    public void clear() {
        this.ingredients.clear();
    }

    private void setSlot(Slot p_361864_, ContextMap p_369307_, SlotDisplay p_367189_, boolean p_365725_) {
        List<ItemStack> list = p_367189_.resolveForStacks(p_369307_);
        if (!list.isEmpty()) {
            this.ingredients.put(p_361864_, new GhostSlots.GhostSlot(list, p_365725_));
        }
    }

    protected void setInput(Slot p_367759_, ContextMap p_367544_, SlotDisplay p_368203_) {
        this.setSlot(p_367759_, p_367544_, p_368203_, false);
    }

    protected void setResult(Slot p_364854_, ContextMap p_369632_, SlotDisplay p_369669_) {
        this.setSlot(p_364854_, p_369632_, p_369669_, true);
    }

    public void render(GuiGraphics p_367836_, Minecraft p_369819_, boolean p_361725_) {
        this.ingredients.forEach((p_365858_, p_367422_) -> {
            int i = p_365858_.x;
            int j = p_365858_.y;
            if (p_367422_.isResultSlot && p_361725_) {
                p_367836_.fill(i - 4, j - 4, i + 20, j + 20, 822018048);
            } else {
                p_367836_.fill(i, j, i + 16, j + 16, 822018048);
            }

            ItemStack itemstack = p_367422_.getItem(this.slotSelectTime.currentIndex());
            p_367836_.renderFakeItem(itemstack, i, j);
            p_367836_.fill(RenderType.guiGhostRecipeOverlay(), i, j, i + 16, j + 16, 822083583);
            if (p_367422_.isResultSlot) {
                p_367836_.renderItemDecorations(p_369819_.font, itemstack, i, j);
            }
        });
    }

    public void renderTooltip(GuiGraphics p_362742_, Minecraft p_367763_, int p_363042_, int p_368767_, @Nullable Slot p_366140_) {
        if (p_366140_ != null) {
            GhostSlots.GhostSlot ghostslots$ghostslot = this.ingredients.get(p_366140_);
            if (ghostslots$ghostslot != null) {
                ItemStack itemstack = ghostslots$ghostslot.getItem(this.slotSelectTime.currentIndex());
                p_362742_.renderComponentTooltip(
                    p_367763_.font, Screen.getTooltipFromItem(p_367763_, itemstack), p_363042_, p_368767_, itemstack.get(DataComponents.TOOLTIP_STYLE)
                );
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record GhostSlot(List<ItemStack> items, boolean isResultSlot) {
        public ItemStack getItem(int p_367346_) {
            int i = this.items.size();
            return i == 0 ? ItemStack.EMPTY : this.items.get(p_367346_ % i);
        }
    }
}