package net.minecraft.advancements;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import javax.annotation.Nullable;

public class AdvancementNode {
    private final AdvancementHolder holder;
    @Nullable
    private final AdvancementNode parent;
    private final Set<AdvancementNode> children = new ReferenceOpenHashSet<>();

    @VisibleForTesting
    public AdvancementNode(AdvancementHolder p_300583_, @Nullable AdvancementNode p_299774_) {
        this.holder = p_300583_;
        this.parent = p_299774_;
    }

    public Advancement advancement() {
        return this.holder.value();
    }

    public AdvancementHolder holder() {
        return this.holder;
    }

    @Nullable
    public AdvancementNode parent() {
        return this.parent;
    }

    public AdvancementNode root() {
        return getRoot(this);
    }

    public static AdvancementNode getRoot(AdvancementNode p_300357_) {
        AdvancementNode advancementnode = p_300357_;

        while (true) {
            AdvancementNode advancementnode1 = advancementnode.parent();
            if (advancementnode1 == null) {
                return advancementnode;
            }

            advancementnode = advancementnode1;
        }
    }

    public Iterable<AdvancementNode> children() {
        return this.children;
    }

    @VisibleForTesting
    public void addChild(AdvancementNode p_298204_) {
        this.children.add(p_298204_);
    }

    @Override
    public boolean equals(Object p_297253_) {
        if (this == p_297253_) {
            return true;
        } else {
            if (p_297253_ instanceof AdvancementNode advancementnode && this.holder.equals(advancementnode.holder)) {
                return true;
            }

            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.holder.hashCode();
    }

    @Override
    public String toString() {
        return this.holder.id().toString();
    }
}