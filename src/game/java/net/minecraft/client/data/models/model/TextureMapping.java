package net.minecraft.client.data.models.model;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureMapping {
    private final Map<TextureSlot, ResourceLocation> slots = Maps.newHashMap();
    private final Set<TextureSlot> forcedSlots = Sets.newHashSet();

    public TextureMapping put(TextureSlot p_375469_, ResourceLocation p_376844_) {
        this.slots.put(p_375469_, p_376844_);
        return this;
    }

    public TextureMapping putForced(TextureSlot p_375726_, ResourceLocation p_377057_) {
        this.slots.put(p_375726_, p_377057_);
        this.forcedSlots.add(p_375726_);
        return this;
    }

    public Stream<TextureSlot> getForced() {
        return this.forcedSlots.stream();
    }

    public TextureMapping copySlot(TextureSlot p_377785_, TextureSlot p_378687_) {
        this.slots.put(p_378687_, this.slots.get(p_377785_));
        return this;
    }

    public TextureMapping copyForced(TextureSlot p_377866_, TextureSlot p_376740_) {
        this.slots.put(p_376740_, this.slots.get(p_377866_));
        this.forcedSlots.add(p_376740_);
        return this;
    }

    public ResourceLocation get(TextureSlot p_377192_) {
        for (TextureSlot textureslot = p_377192_; textureslot != null; textureslot = textureslot.getParent()) {
            ResourceLocation resourcelocation = this.slots.get(textureslot);
            if (resourcelocation != null) {
                return resourcelocation;
            }
        }

        throw new IllegalStateException("Can't find texture for slot " + p_377192_);
    }

    public TextureMapping copyAndUpdate(TextureSlot p_376401_, ResourceLocation p_378265_) {
        TextureMapping texturemapping = new TextureMapping();
        texturemapping.slots.putAll(this.slots);
        texturemapping.forcedSlots.addAll(this.forcedSlots);
        texturemapping.put(p_376401_, p_378265_);
        return texturemapping;
    }

    public static TextureMapping cube(Block p_378382_) {
        ResourceLocation resourcelocation = getBlockTexture(p_378382_);
        return cube(resourcelocation);
    }

    public static TextureMapping defaultTexture(Block p_378584_) {
        ResourceLocation resourcelocation = getBlockTexture(p_378584_);
        return defaultTexture(resourcelocation);
    }

    public static TextureMapping defaultTexture(ResourceLocation p_377815_) {
        return new TextureMapping().put(TextureSlot.TEXTURE, p_377815_);
    }

    public static TextureMapping cube(ResourceLocation p_377582_) {
        return new TextureMapping().put(TextureSlot.ALL, p_377582_);
    }

    public static TextureMapping cross(Block p_377984_) {
        return singleSlot(TextureSlot.CROSS, getBlockTexture(p_377984_));
    }

    public static TextureMapping side(Block p_375557_) {
        return singleSlot(TextureSlot.SIDE, getBlockTexture(p_375557_));
    }

    public static TextureMapping crossEmissive(Block p_375522_) {
        return new TextureMapping().put(TextureSlot.CROSS, getBlockTexture(p_375522_)).put(TextureSlot.CROSS_EMISSIVE, getBlockTexture(p_375522_, "_emissive"));
    }

    public static TextureMapping cross(ResourceLocation p_378193_) {
        return singleSlot(TextureSlot.CROSS, p_378193_);
    }

    public static TextureMapping plant(Block p_377826_) {
        return singleSlot(TextureSlot.PLANT, getBlockTexture(p_377826_));
    }

    public static TextureMapping plantEmissive(Block p_377330_) {
        return new TextureMapping().put(TextureSlot.PLANT, getBlockTexture(p_377330_)).put(TextureSlot.CROSS_EMISSIVE, getBlockTexture(p_377330_, "_emissive"));
    }

    public static TextureMapping plant(ResourceLocation p_376991_) {
        return singleSlot(TextureSlot.PLANT, p_376991_);
    }

    public static TextureMapping rail(Block p_378377_) {
        return singleSlot(TextureSlot.RAIL, getBlockTexture(p_378377_));
    }

    public static TextureMapping rail(ResourceLocation p_377142_) {
        return singleSlot(TextureSlot.RAIL, p_377142_);
    }

    public static TextureMapping wool(Block p_377999_) {
        return singleSlot(TextureSlot.WOOL, getBlockTexture(p_377999_));
    }

    public static TextureMapping flowerbed(Block p_376315_) {
        return new TextureMapping().put(TextureSlot.FLOWERBED, getBlockTexture(p_376315_)).put(TextureSlot.STEM, getBlockTexture(p_376315_, "_stem"));
    }

    public static TextureMapping wool(ResourceLocation p_378747_) {
        return singleSlot(TextureSlot.WOOL, p_378747_);
    }

    public static TextureMapping stem(Block p_377484_) {
        return singleSlot(TextureSlot.STEM, getBlockTexture(p_377484_));
    }

    public static TextureMapping attachedStem(Block p_375438_, Block p_377349_) {
        return new TextureMapping().put(TextureSlot.STEM, getBlockTexture(p_375438_)).put(TextureSlot.UPPER_STEM, getBlockTexture(p_377349_));
    }

    public static TextureMapping pattern(Block p_378069_) {
        return singleSlot(TextureSlot.PATTERN, getBlockTexture(p_378069_));
    }

    public static TextureMapping fan(Block p_377538_) {
        return singleSlot(TextureSlot.FAN, getBlockTexture(p_377538_));
    }

    public static TextureMapping crop(ResourceLocation p_376695_) {
        return singleSlot(TextureSlot.CROP, p_376695_);
    }

    public static TextureMapping pane(Block p_377816_, Block p_376781_) {
        return new TextureMapping().put(TextureSlot.PANE, getBlockTexture(p_377816_)).put(TextureSlot.EDGE, getBlockTexture(p_376781_, "_top"));
    }

    public static TextureMapping singleSlot(TextureSlot p_375607_, ResourceLocation p_378030_) {
        return new TextureMapping().put(p_375607_, p_378030_);
    }

    public static TextureMapping column(Block p_377667_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_377667_, "_side"))
            .put(TextureSlot.END, getBlockTexture(p_377667_, "_top"));
    }

    public static TextureMapping cubeTop(Block p_378763_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_378763_, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(p_378763_, "_top"));
    }

    public static TextureMapping pottedAzalea(Block p_376369_) {
        return new TextureMapping()
            .put(TextureSlot.PLANT, getBlockTexture(p_376369_, "_plant"))
            .put(TextureSlot.SIDE, getBlockTexture(p_376369_, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(p_376369_, "_top"));
    }

    public static TextureMapping logColumn(Block p_378279_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_378279_))
            .put(TextureSlot.END, getBlockTexture(p_378279_, "_top"))
            .put(TextureSlot.PARTICLE, getBlockTexture(p_378279_));
    }

    public static TextureMapping column(ResourceLocation p_375631_, ResourceLocation p_376410_) {
        return new TextureMapping().put(TextureSlot.SIDE, p_375631_).put(TextureSlot.END, p_376410_);
    }

    public static TextureMapping fence(Block p_378554_) {
        return new TextureMapping()
            .put(TextureSlot.TEXTURE, getBlockTexture(p_378554_))
            .put(TextureSlot.SIDE, getBlockTexture(p_378554_, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(p_378554_, "_top"));
    }

    public static TextureMapping customParticle(Block p_377912_) {
        return new TextureMapping().put(TextureSlot.TEXTURE, getBlockTexture(p_377912_)).put(TextureSlot.PARTICLE, getBlockTexture(p_377912_, "_particle"));
    }

    public static TextureMapping cubeBottomTop(Block p_376324_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_376324_, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(p_376324_, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(p_376324_, "_bottom"));
    }

    public static TextureMapping cubeBottomTopWithWall(Block p_377028_) {
        ResourceLocation resourcelocation = getBlockTexture(p_377028_);
        return new TextureMapping()
            .put(TextureSlot.WALL, resourcelocation)
            .put(TextureSlot.SIDE, resourcelocation)
            .put(TextureSlot.TOP, getBlockTexture(p_377028_, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(p_377028_, "_bottom"));
    }

    public static TextureMapping columnWithWall(Block p_377232_) {
        ResourceLocation resourcelocation = getBlockTexture(p_377232_);
        return new TextureMapping()
            .put(TextureSlot.TEXTURE, resourcelocation)
            .put(TextureSlot.WALL, resourcelocation)
            .put(TextureSlot.SIDE, resourcelocation)
            .put(TextureSlot.END, getBlockTexture(p_377232_, "_top"));
    }

    public static TextureMapping door(ResourceLocation p_375948_, ResourceLocation p_376042_) {
        return new TextureMapping().put(TextureSlot.TOP, p_375948_).put(TextureSlot.BOTTOM, p_376042_);
    }

    public static TextureMapping door(Block p_377626_) {
        return new TextureMapping()
            .put(TextureSlot.TOP, getBlockTexture(p_377626_, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(p_377626_, "_bottom"));
    }

    public static TextureMapping particle(Block p_375573_) {
        return new TextureMapping().put(TextureSlot.PARTICLE, getBlockTexture(p_375573_));
    }

    public static TextureMapping particle(ResourceLocation p_376691_) {
        return new TextureMapping().put(TextureSlot.PARTICLE, p_376691_);
    }

    public static TextureMapping fire0(Block p_375396_) {
        return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(p_375396_, "_0"));
    }

    public static TextureMapping fire1(Block p_378277_) {
        return new TextureMapping().put(TextureSlot.FIRE, getBlockTexture(p_378277_, "_1"));
    }

    public static TextureMapping lantern(Block p_378649_) {
        return new TextureMapping().put(TextureSlot.LANTERN, getBlockTexture(p_378649_));
    }

    public static TextureMapping torch(Block p_378663_) {
        return new TextureMapping().put(TextureSlot.TORCH, getBlockTexture(p_378663_));
    }

    public static TextureMapping torch(ResourceLocation p_378365_) {
        return new TextureMapping().put(TextureSlot.TORCH, p_378365_);
    }

    public static TextureMapping trialSpawner(Block p_376710_, String p_377856_, String p_375601_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_376710_, p_377856_))
            .put(TextureSlot.TOP, getBlockTexture(p_376710_, p_375601_))
            .put(TextureSlot.BOTTOM, getBlockTexture(p_376710_, "_bottom"));
    }

    public static TextureMapping vault(Block p_376379_, String p_376806_, String p_377906_, String p_377742_, String p_376382_) {
        return new TextureMapping()
            .put(TextureSlot.FRONT, getBlockTexture(p_376379_, p_376806_))
            .put(TextureSlot.SIDE, getBlockTexture(p_376379_, p_377906_))
            .put(TextureSlot.TOP, getBlockTexture(p_376379_, p_377742_))
            .put(TextureSlot.BOTTOM, getBlockTexture(p_376379_, p_376382_));
    }

    public static TextureMapping particleFromItem(Item p_376776_) {
        return new TextureMapping().put(TextureSlot.PARTICLE, getItemTexture(p_376776_));
    }

    public static TextureMapping commandBlock(Block p_378327_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_378327_, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(p_378327_, "_front"))
            .put(TextureSlot.BACK, getBlockTexture(p_378327_, "_back"));
    }

    public static TextureMapping orientableCube(Block p_378234_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_378234_, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(p_378234_, "_front"))
            .put(TextureSlot.TOP, getBlockTexture(p_378234_, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(p_378234_, "_bottom"));
    }

    public static TextureMapping orientableCubeOnlyTop(Block p_378414_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_378414_, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(p_378414_, "_front"))
            .put(TextureSlot.TOP, getBlockTexture(p_378414_, "_top"));
    }

    public static TextureMapping orientableCubeSameEnds(Block p_377990_) {
        return new TextureMapping()
            .put(TextureSlot.SIDE, getBlockTexture(p_377990_, "_side"))
            .put(TextureSlot.FRONT, getBlockTexture(p_377990_, "_front"))
            .put(TextureSlot.END, getBlockTexture(p_377990_, "_end"));
    }

    public static TextureMapping top(Block p_376969_) {
        return new TextureMapping().put(TextureSlot.TOP, getBlockTexture(p_376969_, "_top"));
    }

    public static TextureMapping craftingTable(Block p_378375_, Block p_375901_) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(p_378375_, "_front"))
            .put(TextureSlot.DOWN, getBlockTexture(p_375901_))
            .put(TextureSlot.UP, getBlockTexture(p_378375_, "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(p_378375_, "_front"))
            .put(TextureSlot.EAST, getBlockTexture(p_378375_, "_side"))
            .put(TextureSlot.SOUTH, getBlockTexture(p_378375_, "_side"))
            .put(TextureSlot.WEST, getBlockTexture(p_378375_, "_front"));
    }

    public static TextureMapping fletchingTable(Block p_378723_, Block p_376734_) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(p_378723_, "_front"))
            .put(TextureSlot.DOWN, getBlockTexture(p_376734_))
            .put(TextureSlot.UP, getBlockTexture(p_378723_, "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(p_378723_, "_front"))
            .put(TextureSlot.SOUTH, getBlockTexture(p_378723_, "_front"))
            .put(TextureSlot.EAST, getBlockTexture(p_378723_, "_side"))
            .put(TextureSlot.WEST, getBlockTexture(p_378723_, "_side"));
    }

    public static TextureMapping snifferEgg(String p_375695_) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SNIFFER_EGG, p_375695_ + "_north"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SNIFFER_EGG, p_375695_ + "_bottom"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.SNIFFER_EGG, p_375695_ + "_top"))
            .put(TextureSlot.NORTH, getBlockTexture(Blocks.SNIFFER_EGG, p_375695_ + "_north"))
            .put(TextureSlot.SOUTH, getBlockTexture(Blocks.SNIFFER_EGG, p_375695_ + "_south"))
            .put(TextureSlot.EAST, getBlockTexture(Blocks.SNIFFER_EGG, p_375695_ + "_east"))
            .put(TextureSlot.WEST, getBlockTexture(Blocks.SNIFFER_EGG, p_375695_ + "_west"));
    }

    public static TextureMapping campfire(Block p_377895_) {
        return new TextureMapping()
            .put(TextureSlot.LIT_LOG, getBlockTexture(p_377895_, "_log_lit"))
            .put(TextureSlot.FIRE, getBlockTexture(p_377895_, "_fire"));
    }

    public static TextureMapping candleCake(Block p_378344_, boolean p_376676_) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAKE, "_side"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAKE, "_bottom"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.CAKE, "_top"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.CAKE, "_side"))
            .put(TextureSlot.CANDLE, getBlockTexture(p_378344_, p_376676_ ? "_lit" : ""));
    }

    public static TextureMapping cauldron(ResourceLocation p_377720_) {
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.CAULDRON, "_side"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.CAULDRON, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.CAULDRON, "_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.CAULDRON, "_bottom"))
            .put(TextureSlot.INSIDE, getBlockTexture(Blocks.CAULDRON, "_inner"))
            .put(TextureSlot.CONTENT, p_377720_);
    }

    public static TextureMapping sculkShrieker(boolean p_377001_) {
        String s = p_377001_ ? "_can_summon" : "";
        return new TextureMapping()
            .put(TextureSlot.PARTICLE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"))
            .put(TextureSlot.SIDE, getBlockTexture(Blocks.SCULK_SHRIEKER, "_side"))
            .put(TextureSlot.TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, "_top"))
            .put(TextureSlot.INNER_TOP, getBlockTexture(Blocks.SCULK_SHRIEKER, s + "_inner_top"))
            .put(TextureSlot.BOTTOM, getBlockTexture(Blocks.SCULK_SHRIEKER, "_bottom"));
    }

    public static TextureMapping layer0(Item p_378728_) {
        return new TextureMapping().put(TextureSlot.LAYER0, getItemTexture(p_378728_));
    }

    public static TextureMapping layer0(Block p_376340_) {
        return new TextureMapping().put(TextureSlot.LAYER0, getBlockTexture(p_376340_));
    }

    public static TextureMapping layer0(ResourceLocation p_378501_) {
        return new TextureMapping().put(TextureSlot.LAYER0, p_378501_);
    }

    public static TextureMapping layered(ResourceLocation p_376621_, ResourceLocation p_377249_) {
        return new TextureMapping().put(TextureSlot.LAYER0, p_376621_).put(TextureSlot.LAYER1, p_377249_);
    }

    public static TextureMapping layered(ResourceLocation p_378512_, ResourceLocation p_377525_, ResourceLocation p_378610_) {
        return new TextureMapping()
            .put(TextureSlot.LAYER0, p_378512_)
            .put(TextureSlot.LAYER1, p_377525_)
            .put(TextureSlot.LAYER2, p_378610_);
    }

    public static ResourceLocation getBlockTexture(Block p_378828_) {
        ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(p_378828_);
        return resourcelocation.withPrefix("block/");
    }

    public static ResourceLocation getBlockTexture(Block p_376909_, String p_378602_) {
        ResourceLocation resourcelocation = BuiltInRegistries.BLOCK.getKey(p_376909_);
        return resourcelocation.withPath(p_377089_ -> "block/" + p_377089_ + p_378602_);
    }

    public static ResourceLocation getItemTexture(Item p_376204_) {
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(p_376204_);
        return resourcelocation.withPrefix("item/");
    }

    public static ResourceLocation getItemTexture(Item p_376530_, String p_375596_) {
        ResourceLocation resourcelocation = BuiltInRegistries.ITEM.getKey(p_376530_);
        return resourcelocation.withPath(p_377151_ -> "item/" + p_377151_ + p_375596_);
    }
}