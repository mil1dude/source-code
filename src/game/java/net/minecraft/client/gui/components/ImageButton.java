package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ImageButton extends Button {
    protected final WidgetSprites sprites;

    public ImageButton(int p_94230_, int p_94231_, int p_94232_, int p_94233_, WidgetSprites p_299641_, Button.OnPress p_94240_) {
        this(p_94230_, p_94231_, p_94232_, p_94233_, p_299641_, p_94240_, CommonComponents.EMPTY);
    }

    public ImageButton(int p_94256_, int p_94257_, int p_94258_, int p_94259_, WidgetSprites p_298753_, Button.OnPress p_94266_, Component p_94267_) {
        super(p_94256_, p_94257_, p_94258_, p_94259_, p_94267_, p_94266_, DEFAULT_NARRATION);
        this.sprites = p_298753_;
    }

    public ImageButton(int p_169011_, int p_169012_, WidgetSprites p_298218_, Button.OnPress p_169018_, Component p_297655_) {
        this(0, 0, p_169011_, p_169012_, p_298218_, p_169018_, p_297655_);
    }

    @Override
    public void renderWidget(GuiGraphics p_283502_, int p_281473_, int p_283021_, float p_282518_) {
        ResourceLocation resourcelocation = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
        p_283502_.blitSprite(RenderType::guiTextured, resourcelocation, this.getX(), this.getY(), this.width, this.height);
    }
}