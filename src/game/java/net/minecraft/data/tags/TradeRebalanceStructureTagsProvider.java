package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

public class TradeRebalanceStructureTagsProvider extends TagsProvider<Structure> {
    public TradeRebalanceStructureTagsProvider(PackOutput p_301607_, CompletableFuture<HolderLookup.Provider> p_301609_) {
        super(p_301607_, Registries.STRUCTURE, p_301609_);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_301604_) {
        this.tag(StructureTags.ON_SAVANNA_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_SAVANNA);
        this.tag(StructureTags.ON_DESERT_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_DESERT);
        this.tag(StructureTags.ON_PLAINS_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_PLAINS);
        this.tag(StructureTags.ON_TAIGA_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_TAIGA);
        this.tag(StructureTags.ON_SNOWY_VILLAGE_MAPS).add(BuiltinStructures.VILLAGE_SNOWY);
        this.tag(StructureTags.ON_SWAMP_EXPLORER_MAPS).add(BuiltinStructures.SWAMP_HUT);
        this.tag(StructureTags.ON_JUNGLE_EXPLORER_MAPS).add(BuiltinStructures.JUNGLE_TEMPLE);
    }
}