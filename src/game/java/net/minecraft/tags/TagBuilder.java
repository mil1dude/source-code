package net.minecraft.tags;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class TagBuilder {
    private final List<TagEntry> entries = new ArrayList<>();

    public static TagBuilder create() {
        return new TagBuilder();
    }

    public List<TagEntry> build() {
        return List.copyOf(this.entries);
    }

    public TagBuilder add(TagEntry p_215903_) {
        this.entries.add(p_215903_);
        return this;
    }

    public TagBuilder addElement(ResourceLocation p_215901_) {
        return this.add(TagEntry.element(p_215901_));
    }

    public TagBuilder addOptionalElement(ResourceLocation p_215906_) {
        return this.add(TagEntry.optionalElement(p_215906_));
    }

    public TagBuilder addTag(ResourceLocation p_215908_) {
        return this.add(TagEntry.tag(p_215908_));
    }

    public TagBuilder addOptionalTag(ResourceLocation p_215910_) {
        return this.add(TagEntry.optionalTag(p_215910_));
    }
}