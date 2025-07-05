package net.minecraft.client.renderer.texture.atlas;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class SpriteSourceList {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter ATLAS_INFO_CONVERTER = new FileToIdConverter("atlases", ".json");
    private final List<SpriteSource> sources;

    private SpriteSourceList(List<SpriteSource> p_297576_) {
        this.sources = p_297576_;
    }

    public List<Function<SpriteResourceLoader, SpriteContents>> list(ResourceManager p_298985_) {
        final Map<ResourceLocation, SpriteSource.SpriteSupplier> map = new HashMap<>();
        SpriteSource.Output spritesource$output = new SpriteSource.Output() {
            @Override
            public void add(ResourceLocation p_297663_, SpriteSource.SpriteSupplier p_299416_) {
                SpriteSource.SpriteSupplier spritesource$spritesupplier = map.put(p_297663_, p_299416_);
                if (spritesource$spritesupplier != null) {
                    spritesource$spritesupplier.discard();
                }
            }

            @Override
            public void removeAll(Predicate<ResourceLocation> p_299726_) {
                Iterator<Entry<ResourceLocation, SpriteSource.SpriteSupplier>> iterator = map.entrySet().iterator();

                while (iterator.hasNext()) {
                    Entry<ResourceLocation, SpriteSource.SpriteSupplier> entry = iterator.next();
                    if (p_299726_.test(entry.getKey())) {
                        entry.getValue().discard();
                        iterator.remove();
                    }
                }
            }
        };
        this.sources.forEach(p_299872_ -> p_299872_.run(p_298985_, spritesource$output));
        Builder<Function<SpriteResourceLoader, SpriteContents>> builder = ImmutableList.builder();
        builder.add(p_299121_ -> MissingTextureAtlasSprite.create());
        builder.addAll(map.values());
        return builder.build();
    }

    public static SpriteSourceList load(ResourceManager p_300689_, ResourceLocation p_300572_) {
        ResourceLocation resourcelocation = ATLAS_INFO_CONVERTER.idToFile(p_300572_);
        List<SpriteSource> list = new ArrayList<>();

        for (Resource resource : p_300689_.getResourceStack(resourcelocation)) {
            try (BufferedReader bufferedreader = resource.openAsReader()) {
                Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(bufferedreader));
                list.addAll(SpriteSources.FILE_CODEC.parse(dynamic).getOrThrow());
            } catch (Exception exception) {
                LOGGER.error("Failed to parse atlas definition {} in pack {}", resourcelocation, resource.sourcePackId(), exception);
            }
        }

        return new SpriteSourceList(list);
    }
}