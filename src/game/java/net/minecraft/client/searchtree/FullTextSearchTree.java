package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FullTextSearchTree<T> extends IdSearchTree<T> {
    private final SearchTree<T> plainTextSearchTree;

    public FullTextSearchTree(Function<T, Stream<String>> p_235155_, Function<T, Stream<ResourceLocation>> p_235156_, List<T> p_235157_) {
        super(p_235156_, p_235157_);
        this.plainTextSearchTree = SearchTree.plainText(p_235157_, p_235155_);
    }

    @Override
    protected List<T> searchPlainText(String p_235160_) {
        return this.plainTextSearchTree.search(p_235160_);
    }

    @Override
    protected List<T> searchResourceLocation(String p_235162_, String p_235163_) {
        List<T> list = this.resourceLocationSearchTree.searchNamespace(p_235162_);
        List<T> list1 = this.resourceLocationSearchTree.searchPath(p_235163_);
        List<T> list2 = this.plainTextSearchTree.search(p_235163_);
        Iterator<T> iterator = new MergingUniqueIterator<>(list1.iterator(), list2.iterator(), this.additionOrder);
        return ImmutableList.copyOf(new IntersectionIterator<>(list.iterator(), iterator, this.additionOrder));
    }
}