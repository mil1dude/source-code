package net.minecraft.server.packs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.InclusiveRange;

public record OverlayMetadataSection(List<OverlayMetadataSection.OverlayEntry> overlays) {
    private static final Pattern DIR_VALIDATOR = Pattern.compile("[-_a-zA-Z0-9.]+");
    private static final Codec<OverlayMetadataSection> CODEC = RecordCodecBuilder.create(
        p_297746_ -> p_297746_.group(OverlayMetadataSection.OverlayEntry.CODEC.listOf().fieldOf("entries").forGetter(OverlayMetadataSection::overlays))
                .apply(p_297746_, OverlayMetadataSection::new)
    );
    public static final MetadataSectionType<OverlayMetadataSection> TYPE = new MetadataSectionType<>("overlays", CODEC);

    private static DataResult<String> validateOverlayDir(String p_301366_) {
        return !DIR_VALIDATOR.matcher(p_301366_).matches() ? DataResult.error(() -> p_301366_ + " is not accepted directory name") : DataResult.success(p_301366_);
    }

    public List<String> overlaysForVersion(int p_299136_) {
        return this.overlays.stream().filter(p_301078_ -> p_301078_.isApplicable(p_299136_)).map(OverlayMetadataSection.OverlayEntry::overlay).toList();
    }

    public static record OverlayEntry(InclusiveRange<Integer> format, String overlay) {
        static final Codec<OverlayMetadataSection.OverlayEntry> CODEC = RecordCodecBuilder.create(
            p_326461_ -> p_326461_.group(
                        InclusiveRange.codec(Codec.INT).fieldOf("formats").forGetter(OverlayMetadataSection.OverlayEntry::format),
                        Codec.STRING.validate(OverlayMetadataSection::validateOverlayDir).fieldOf("directory").forGetter(OverlayMetadataSection.OverlayEntry::overlay)
                    )
                    .apply(p_326461_, OverlayMetadataSection.OverlayEntry::new)
        );

        public boolean isApplicable(int p_300875_) {
            return this.format.isValueInRange(p_300875_);
        }
    }
}