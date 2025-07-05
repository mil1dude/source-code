package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerLikeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.client.resources.metadata.animation.VillagerMetadataSection;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerProfessionLayer<S extends LivingEntityRenderState & VillagerDataHolderRenderState, M extends EntityModel<S> & VillagerLikeModel>
    extends RenderLayer<S, M> {
    private static final Int2ObjectMap<ResourceLocation> LEVEL_LOCATIONS = Util.make(new Int2ObjectOpenHashMap<>(), p_340946_ -> {
        p_340946_.put(1, ResourceLocation.withDefaultNamespace("stone"));
        p_340946_.put(2, ResourceLocation.withDefaultNamespace("iron"));
        p_340946_.put(3, ResourceLocation.withDefaultNamespace("gold"));
        p_340946_.put(4, ResourceLocation.withDefaultNamespace("emerald"));
        p_340946_.put(5, ResourceLocation.withDefaultNamespace("diamond"));
    });
    private final Object2ObjectMap<VillagerType, VillagerMetadataSection.Hat> typeHatCache = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<VillagerProfession, VillagerMetadataSection.Hat> professionHatCache = new Object2ObjectOpenHashMap<>();
    private final ResourceManager resourceManager;
    private final String path;

    public VillagerProfessionLayer(RenderLayerParent<S, M> p_174550_, ResourceManager p_174551_, String p_174552_) {
        super(p_174550_);
        this.resourceManager = p_174551_;
        this.path = p_174552_;
    }

    public void render(PoseStack p_117646_, MultiBufferSource p_117647_, int p_117648_, S p_369199_, float p_117650_, float p_117651_) {
        if (!p_369199_.isInvisible) {
            VillagerData villagerdata = p_369199_.getVillagerData();
            VillagerType villagertype = villagerdata.getType();
            VillagerProfession villagerprofession = villagerdata.getProfession();
            VillagerMetadataSection.Hat villagermetadatasection$hat = this.getHatData(this.typeHatCache, "type", BuiltInRegistries.VILLAGER_TYPE, villagertype);
            VillagerMetadataSection.Hat villagermetadatasection$hat1 = this.getHatData(
                this.professionHatCache, "profession", BuiltInRegistries.VILLAGER_PROFESSION, villagerprofession
            );
            M m = this.getParentModel();
            m.hatVisible(
                villagermetadatasection$hat1 == VillagerMetadataSection.Hat.NONE
                    || villagermetadatasection$hat1 == VillagerMetadataSection.Hat.PARTIAL && villagermetadatasection$hat != VillagerMetadataSection.Hat.FULL
            );
            ResourceLocation resourcelocation = this.getResourceLocation("type", BuiltInRegistries.VILLAGER_TYPE.getKey(villagertype));
            renderColoredCutoutModel(m, resourcelocation, p_117646_, p_117647_, p_117648_, p_369199_, -1);
            m.hatVisible(true);
            if (villagerprofession != VillagerProfession.NONE && !p_369199_.isBaby) {
                ResourceLocation resourcelocation1 = this.getResourceLocation("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(villagerprofession));
                renderColoredCutoutModel(m, resourcelocation1, p_117646_, p_117647_, p_117648_, p_369199_, -1);
                if (villagerprofession != VillagerProfession.NITWIT) {
                    ResourceLocation resourcelocation2 = this.getResourceLocation(
                        "profession_level", LEVEL_LOCATIONS.get(Mth.clamp(villagerdata.getLevel(), 1, LEVEL_LOCATIONS.size()))
                    );
                    renderColoredCutoutModel(m, resourcelocation2, p_117646_, p_117647_, p_117648_, p_369199_, -1);
                }
            }
        }
    }

    private ResourceLocation getResourceLocation(String p_117669_, ResourceLocation p_117670_) {
        return p_117670_.withPath(p_247944_ -> "textures/entity/" + this.path + "/" + p_117669_ + "/" + p_247944_ + ".png");
    }

    public <K> VillagerMetadataSection.Hat getHatData(
        Object2ObjectMap<K, VillagerMetadataSection.Hat> p_117659_, String p_117660_, DefaultedRegistry<K> p_117661_, K p_117662_
    ) {
        return p_117659_.computeIfAbsent(
            p_117662_, p_374663_ -> this.resourceManager.getResource(this.getResourceLocation(p_117660_, p_117661_.getKey(p_117662_))).flatMap(p_374659_ -> {
                    try {
                        return p_374659_.metadata().getSection(VillagerMetadataSection.TYPE).map(VillagerMetadataSection::hat);
                    } catch (IOException ioexception) {
                        return Optional.empty();
                    }
                }).orElse(VillagerMetadataSection.Hat.NONE)
        );
    }
}