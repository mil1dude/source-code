package net.minecraft.client;

import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientBootstrap {
    private static volatile boolean isBootstrapped;

    public static void bootstrap() {
        if (!isBootstrapped) {
            isBootstrapped = true;
            ItemModels.bootstrap();
            SpecialModelRenderers.bootstrap();
            ItemTintSources.bootstrap();
            SelectItemModelProperties.bootstrap();
            ConditionalItemModelProperties.bootstrap();
            RangeSelectItemModelProperties.bootstrap();
        }
    }
}