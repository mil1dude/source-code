package net.minecraft.client.resources.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class MissingBlockModel {
    private static final String NAME = "missing";
    private static final String TEXTURE_SLOT = "missingno";
    public static final ResourceLocation LOCATION = ResourceLocation.withDefaultNamespace("builtin/missing");
    public static final ModelResourceLocation VARIANT = new ModelResourceLocation(LOCATION, "missing");

    public static UnbakedModel missingModel() {
        BlockFaceUV blockfaceuv = new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0);
        Map<Direction, BlockElementFace> map = new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            map.put(direction, new BlockElementFace(direction, -1, "missingno", blockfaceuv));
        }

        BlockElement blockelement = new BlockElement(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(16.0F, 16.0F, 16.0F), map);
        return new BlockModel(
            null,
            List.of(blockelement),
            new TextureSlots.Data.Builder()
                .addReference("particle", "missingno")
                .addTexture("missingno", new Material(TextureAtlas.LOCATION_BLOCKS, MissingTextureAtlasSprite.getLocation()))
                .build(),
            null,
            null,
            ItemTransforms.NO_TRANSFORMS
        );
    }
}