package net.minecraft.client.gui.screens.recipebook;

import java.util.List;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookTabButton extends StateSwitchingButton {
    private static final WidgetSprites SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("recipe_book/tab"), ResourceLocation.withDefaultNamespace("recipe_book/tab_selected")
    );
    private final RecipeBookComponent.TabInfo tabInfo;
    private static final float ANIMATION_TIME = 15.0F;
    private float animationTime;

    public RecipeBookTabButton(RecipeBookComponent.TabInfo p_368060_) {
        super(0, 0, 35, 27, false);
        this.tabInfo = p_368060_;
        this.initTextureValues(SPRITES);
    }

    public void startAnimation(ClientRecipeBook p_370091_, boolean p_361650_) {
        RecipeCollection.CraftableStatus recipecollection$craftablestatus = p_361650_
            ? RecipeCollection.CraftableStatus.CRAFTABLE
            : RecipeCollection.CraftableStatus.ANY;

        for (RecipeCollection recipecollection : p_370091_.getCollection(this.tabInfo.category())) {
            for (RecipeDisplayEntry recipedisplayentry : recipecollection.getSelectedRecipes(recipecollection$craftablestatus)) {
                if (p_370091_.willHighlight(recipedisplayentry.id())) {
                    this.animationTime = 15.0F;
                    return;
                }
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics p_283195_, int p_283508_, int p_281788_, float p_283269_) {
        if (this.sprites != null) {
            if (this.animationTime > 0.0F) {
                float f = 1.0F + 0.1F * (float)Math.sin((double)(this.animationTime / 15.0F * (float) Math.PI));
                p_283195_.pose().pushPose();
                p_283195_.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12), 0.0F);
                p_283195_.pose().scale(1.0F, f, 1.0F);
                p_283195_.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)), 0.0F);
            }

            ResourceLocation resourcelocation = this.sprites.get(true, this.isStateTriggered);
            int i = this.getX();
            if (this.isStateTriggered) {
                i -= 2;
            }

            p_283195_.blitSprite(RenderType::guiTextured, resourcelocation, i, this.getY(), this.width, this.height);
            this.renderIcon(p_283195_);
            if (this.animationTime > 0.0F) {
                p_283195_.pose().popPose();
                this.animationTime -= p_283269_;
            }
        }
    }

    private void renderIcon(GuiGraphics p_281802_) {
        int i = this.isStateTriggered ? -2 : 0;
        if (this.tabInfo.secondaryIcon().isPresent()) {
            p_281802_.renderFakeItem(this.tabInfo.primaryIcon(), this.getX() + 3 + i, this.getY() + 5);
            p_281802_.renderFakeItem(this.tabInfo.secondaryIcon().get(), this.getX() + 14 + i, this.getY() + 5);
        } else {
            p_281802_.renderFakeItem(this.tabInfo.primaryIcon(), this.getX() + 9 + i, this.getY() + 5);
        }
    }

    public ExtendedRecipeBookCategory getCategory() {
        return this.tabInfo.category();
    }

    public boolean updateVisibility(ClientRecipeBook p_100450_) {
        List<RecipeCollection> list = p_100450_.getCollection(this.tabInfo.category());
        this.visible = false;

        for (RecipeCollection recipecollection : list) {
            if (recipecollection.hasAnySelected()) {
                this.visible = true;
                break;
            }
        }

        return this.visible;
    }
}