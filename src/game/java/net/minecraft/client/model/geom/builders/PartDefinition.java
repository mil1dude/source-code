package net.minecraft.client.model.geom.builders;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PartDefinition {
    private final List<CubeDefinition> cubes;
    private final PartPose partPose;
    private final Map<String, PartDefinition> children = Maps.newHashMap();

    PartDefinition(List<CubeDefinition> p_171581_, PartPose p_171582_) {
        this.cubes = p_171581_;
        this.partPose = p_171582_;
    }

    public PartDefinition addOrReplaceChild(String p_171600_, CubeListBuilder p_171601_, PartPose p_171602_) {
        PartDefinition partdefinition = new PartDefinition(p_171601_.getCubes(), p_171602_);
        return this.addOrReplaceChild(p_171600_, partdefinition);
    }

    public PartDefinition addOrReplaceChild(String p_366821_, PartDefinition p_363546_) {
        PartDefinition partdefinition = this.children.put(p_366821_, p_363546_);
        if (partdefinition != null) {
            p_363546_.children.putAll(partdefinition.children);
        }

        return p_363546_;
    }

    public PartDefinition clearChild(String p_363088_) {
        return this.addOrReplaceChild(p_363088_, CubeListBuilder.create(), PartPose.ZERO);
    }

    public ModelPart bake(int p_171584_, int p_171585_) {
        Object2ObjectArrayMap<String, ModelPart> object2objectarraymap = this.children
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    p_171593_ -> p_171593_.getValue().bake(p_171584_, p_171585_),
                    (p_171595_, p_171596_) -> p_171595_,
                    Object2ObjectArrayMap::new
                )
            );
        List<ModelPart.Cube> list = this.cubes.stream().map(p_171589_ -> p_171589_.bake(p_171584_, p_171585_)).toList();
        ModelPart modelpart = new ModelPart(list, object2objectarraymap);
        modelpart.setInitialPose(this.partPose);
        modelpart.loadPose(this.partPose);
        return modelpart;
    }

    public PartDefinition getChild(String p_171598_) {
        return this.children.get(p_171598_);
    }

    public Set<Entry<String, PartDefinition>> getChildren() {
        return this.children.entrySet();
    }

    public PartDefinition transformed(UnaryOperator<PartPose> p_367495_) {
        PartDefinition partdefinition = new PartDefinition(this.cubes, p_367495_.apply(this.partPose));
        partdefinition.children.putAll(this.children);
        return partdefinition;
    }
}