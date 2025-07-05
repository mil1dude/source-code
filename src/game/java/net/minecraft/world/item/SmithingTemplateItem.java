package net.minecraft.world.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SmithingTemplateItem extends Item {
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final Component INGREDIENTS_TITLE = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.ingredients")))
        .withStyle(TITLE_FORMAT);
    private static final Component APPLIES_TO_TITLE = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.applies_to")))
        .withStyle(TITLE_FORMAT);
    private static final Component SMITHING_TEMPLATE_SUFFIX = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template")))
        .withStyle(TITLE_FORMAT);
    private static final Component ARMOR_TRIM_APPLIES_TO = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.applies_to"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component ARMOR_TRIM_INGREDIENTS = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.ingredients"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component ARMOR_TRIM_BASE_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.base_slot_description"))
    );
    private static final Component ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.armor_trim.additions_slot_description"))
    );
    private static final Component NETHERITE_UPGRADE_APPLIES_TO = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.applies_to"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component NETHERITE_UPGRADE_INGREDIENTS = Component.translatable(
            Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.ingredients"))
        )
        .withStyle(DESCRIPTION_FORMAT);
    private static final Component NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.base_slot_description"))
    );
    private static final Component NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
        Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.additions_slot_description"))
    );
    private static final ResourceLocation EMPTY_SLOT_HELMET = ResourceLocation.withDefaultNamespace("container/slot/helmet");
    private static final ResourceLocation EMPTY_SLOT_CHESTPLATE = ResourceLocation.withDefaultNamespace("container/slot/chestplate");
    private static final ResourceLocation EMPTY_SLOT_LEGGINGS = ResourceLocation.withDefaultNamespace("container/slot/leggings");
    private static final ResourceLocation EMPTY_SLOT_BOOTS = ResourceLocation.withDefaultNamespace("container/slot/boots");
    private static final ResourceLocation EMPTY_SLOT_HOE = ResourceLocation.withDefaultNamespace("container/slot/hoe");
    private static final ResourceLocation EMPTY_SLOT_AXE = ResourceLocation.withDefaultNamespace("container/slot/axe");
    private static final ResourceLocation EMPTY_SLOT_SWORD = ResourceLocation.withDefaultNamespace("container/slot/sword");
    private static final ResourceLocation EMPTY_SLOT_SHOVEL = ResourceLocation.withDefaultNamespace("container/slot/shovel");
    private static final ResourceLocation EMPTY_SLOT_PICKAXE = ResourceLocation.withDefaultNamespace("container/slot/pickaxe");
    private static final ResourceLocation EMPTY_SLOT_INGOT = ResourceLocation.withDefaultNamespace("container/slot/ingot");
    private static final ResourceLocation EMPTY_SLOT_REDSTONE_DUST = ResourceLocation.withDefaultNamespace("container/slot/redstone_dust");
    private static final ResourceLocation EMPTY_SLOT_QUARTZ = ResourceLocation.withDefaultNamespace("container/slot/quartz");
    private static final ResourceLocation EMPTY_SLOT_EMERALD = ResourceLocation.withDefaultNamespace("container/slot/emerald");
    private static final ResourceLocation EMPTY_SLOT_DIAMOND = ResourceLocation.withDefaultNamespace("container/slot/diamond");
    private static final ResourceLocation EMPTY_SLOT_LAPIS_LAZULI = ResourceLocation.withDefaultNamespace("container/slot/lapis_lazuli");
    private static final ResourceLocation EMPTY_SLOT_AMETHYST_SHARD = ResourceLocation.withDefaultNamespace("container/slot/amethyst_shard");
    private final Component appliesTo;
    private final Component ingredients;
    private final Component baseSlotDescription;
    private final Component additionsSlotDescription;
    private final List<ResourceLocation> baseSlotEmptyIcons;
    private final List<ResourceLocation> additionalSlotEmptyIcons;

    public SmithingTemplateItem(
        Component p_266834_,
        Component p_267043_,
        Component p_267048_,
        Component p_267278_,
        List<ResourceLocation> p_266755_,
        List<ResourceLocation> p_267060_,
        Item.Properties p_362295_
    ) {
        super(p_362295_);
        this.appliesTo = p_266834_;
        this.ingredients = p_267043_;
        this.baseSlotDescription = p_267048_;
        this.additionsSlotDescription = p_267278_;
        this.baseSlotEmptyIcons = p_266755_;
        this.additionalSlotEmptyIcons = p_267060_;
    }

    public static SmithingTemplateItem createArmorTrimTemplate(Item.Properties p_366947_) {
        return new SmithingTemplateItem(ARMOR_TRIM_APPLIES_TO, ARMOR_TRIM_INGREDIENTS, ARMOR_TRIM_BASE_SLOT_DESCRIPTION, ARMOR_TRIM_ADDITIONS_SLOT_DESCRIPTION, createTrimmableArmorIconList(), createTrimmableMaterialIconList(), p_366947_);
    }

    public static SmithingTemplateItem createNetheriteUpgradeTemplate(Item.Properties p_368215_) {
        return new SmithingTemplateItem(NETHERITE_UPGRADE_APPLIES_TO, NETHERITE_UPGRADE_INGREDIENTS, NETHERITE_UPGRADE_BASE_SLOT_DESCRIPTION, NETHERITE_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createNetheriteUpgradeIconList(), createNetheriteUpgradeMaterialList(), p_368215_);
    }

    private static List<ResourceLocation> createTrimmableArmorIconList() {
        return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_BOOTS);
    }

    private static List<ResourceLocation> createTrimmableMaterialIconList() {
        return List.of(EMPTY_SLOT_INGOT, EMPTY_SLOT_REDSTONE_DUST, EMPTY_SLOT_LAPIS_LAZULI, EMPTY_SLOT_QUARTZ, EMPTY_SLOT_DIAMOND, EMPTY_SLOT_EMERALD, EMPTY_SLOT_AMETHYST_SHARD);
    }

    private static List<ResourceLocation> createNetheriteUpgradeIconList() {
        return List.of(EMPTY_SLOT_HELMET, EMPTY_SLOT_SWORD, EMPTY_SLOT_CHESTPLATE, EMPTY_SLOT_PICKAXE, EMPTY_SLOT_LEGGINGS, EMPTY_SLOT_AXE, EMPTY_SLOT_BOOTS, EMPTY_SLOT_HOE, EMPTY_SLOT_SHOVEL);
    }

    private static List<ResourceLocation> createNetheriteUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_INGOT);
    }

    @Override
    public void appendHoverText(ItemStack p_267313_, Item.TooltipContext p_331023_, List<Component> p_266820_, TooltipFlag p_266857_) {
        super.appendHoverText(p_267313_, p_331023_, p_266820_, p_266857_);
        p_266820_.add(SMITHING_TEMPLATE_SUFFIX);
        p_266820_.add(CommonComponents.EMPTY);
        p_266820_.add(APPLIES_TO_TITLE);
        p_266820_.add(CommonComponents.space().append(this.appliesTo));
        p_266820_.add(INGREDIENTS_TITLE);
        p_266820_.add(CommonComponents.space().append(this.ingredients));
    }

    public Component getBaseSlotDescription() {
        return this.baseSlotDescription;
    }

    public Component getAdditionSlotDescription() {
        return this.additionsSlotDescription;
    }

    public List<ResourceLocation> getBaseSlotEmptyIcons() {
        return this.baseSlotEmptyIcons;
    }

    public List<ResourceLocation> getAdditionalSlotEmptyIcons() {
        return this.additionalSlotEmptyIcons;
    }
}