package net.minecraft.client.gui.screens.advancements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
enum AdvancementTabType {
    ABOVE(
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_above_left_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_above_middle_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_above_right_selected")
        ),
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_above_left"),
            ResourceLocation.withDefaultNamespace("advancements/tab_above_middle"),
            ResourceLocation.withDefaultNamespace("advancements/tab_above_right")
        ),
        28,
        32,
        8
    ),
    BELOW(
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_below_left_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_below_middle_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_below_right_selected")
        ),
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_below_left"),
            ResourceLocation.withDefaultNamespace("advancements/tab_below_middle"),
            ResourceLocation.withDefaultNamespace("advancements/tab_below_right")
        ),
        28,
        32,
        8
    ),
    LEFT(
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_left_top_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_left_middle_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_left_bottom_selected")
        ),
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_left_top"),
            ResourceLocation.withDefaultNamespace("advancements/tab_left_middle"),
            ResourceLocation.withDefaultNamespace("advancements/tab_left_bottom")
        ),
        32,
        28,
        5
    ),
    RIGHT(
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_right_top_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_right_middle_selected"),
            ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom_selected")
        ),
        new AdvancementTabType.Sprites(
            ResourceLocation.withDefaultNamespace("advancements/tab_right_top"),
            ResourceLocation.withDefaultNamespace("advancements/tab_right_middle"),
            ResourceLocation.withDefaultNamespace("advancements/tab_right_bottom")
        ),
        32,
        28,
        5
    );

    private final AdvancementTabType.Sprites selectedSprites;
    private final AdvancementTabType.Sprites unselectedSprites;
    private final int width;
    private final int height;
    private final int max;

    private AdvancementTabType(
        final AdvancementTabType.Sprites p_300993_, final AdvancementTabType.Sprites p_299630_, final int p_97205_, final int p_97206_, final int p_97207_
    ) {
        this.selectedSprites = p_300993_;
        this.unselectedSprites = p_299630_;
        this.width = p_97205_;
        this.height = p_97206_;
        this.max = p_97207_;
    }

    public int getMax() {
        return this.max;
    }

    public void draw(GuiGraphics p_283216_, int p_282432_, int p_283617_, boolean p_282320_, int p_281898_) {
        AdvancementTabType.Sprites advancementtabtype$sprites = p_282320_ ? this.selectedSprites : this.unselectedSprites;
        ResourceLocation resourcelocation;
        if (p_281898_ == 0) {
            resourcelocation = advancementtabtype$sprites.first();
        } else if (p_281898_ == this.max - 1) {
            resourcelocation = advancementtabtype$sprites.last();
        } else {
            resourcelocation = advancementtabtype$sprites.middle();
        }

        p_283216_.blitSprite(
            RenderType::guiTextured, resourcelocation, p_282432_ + this.getX(p_281898_), p_283617_ + this.getY(p_281898_), this.width, this.height
        );
    }

    public void drawIcon(GuiGraphics p_281370_, int p_283209_, int p_282807_, int p_282968_, ItemStack p_283383_) {
        int i = p_283209_ + this.getX(p_282968_);
        int j = p_282807_ + this.getY(p_282968_);
        switch (this) {
            case ABOVE:
                i += 6;
                j += 9;
                break;
            case BELOW:
                i += 6;
                j += 6;
                break;
            case LEFT:
                i += 10;
                j += 5;
                break;
            case RIGHT:
                i += 6;
                j += 5;
        }

        p_281370_.renderFakeItem(p_283383_, i, j);
    }

    public int getX(int p_97212_) {
        switch (this) {
            case ABOVE:
                return (this.width + 4) * p_97212_;
            case BELOW:
                return (this.width + 4) * p_97212_;
            case LEFT:
                return -this.width + 4;
            case RIGHT:
                return 248;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public int getY(int p_97233_) {
        switch (this) {
            case ABOVE:
                return -this.height + 4;
            case BELOW:
                return 136;
            case LEFT:
                return this.height * p_97233_;
            case RIGHT:
                return this.height * p_97233_;
            default:
                throw new UnsupportedOperationException("Don't know what this tab type is!" + this);
        }
    }

    public boolean isMouseOver(int p_97214_, int p_97215_, int p_97216_, double p_97217_, double p_97218_) {
        int i = p_97214_ + this.getX(p_97216_);
        int j = p_97215_ + this.getY(p_97216_);
        return p_97217_ > (double)i && p_97217_ < (double)(i + this.width) && p_97218_ > (double)j && p_97218_ < (double)(j + this.height);
    }

    @OnlyIn(Dist.CLIENT)
    static record Sprites(ResourceLocation first, ResourceLocation middle, ResourceLocation last) {
    }
}