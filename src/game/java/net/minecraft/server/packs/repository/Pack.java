package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final Pack.ResourcesSupplier resources;
    private final Pack.Metadata metadata;
    private final PackSelectionConfig selectionConfig;

    @Nullable
    public static Pack readMetaAndCreate(PackLocationInfo p_333251_, Pack.ResourcesSupplier p_252210_, PackType p_250595_, PackSelectionConfig p_334202_) {
        int i = SharedConstants.getCurrentVersion().getPackVersion(p_250595_);
        Pack.Metadata pack$metadata = readPackMetadata(p_333251_, p_252210_, i);
        return pack$metadata != null ? new Pack(p_333251_, p_252210_, pack$metadata, p_334202_) : null;
    }

    public Pack(PackLocationInfo p_330003_, Pack.ResourcesSupplier p_249377_, Pack.Metadata p_330761_, PackSelectionConfig p_334769_) {
        this.location = p_330003_;
        this.resources = p_249377_;
        this.metadata = p_330761_;
        this.selectionConfig = p_334769_;
    }

    @Nullable
    public static Pack.Metadata readPackMetadata(PackLocationInfo p_330799_, Pack.ResourcesSupplier p_331172_, int p_333544_) {
        try {
            Pack.Metadata pack$metadata;
            try (PackResources packresources = p_331172_.openPrimary(p_330799_)) {
                PackMetadataSection packmetadatasection = packresources.getMetadataSection(PackMetadataSection.TYPE);
                if (packmetadatasection == null) {
                    LOGGER.warn("Missing metadata in pack {}", p_330799_.id());
                    return null;
                }

                FeatureFlagsMetadataSection featureflagsmetadatasection = packresources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
                FeatureFlagSet featureflagset = featureflagsmetadatasection != null ? featureflagsmetadatasection.flags() : FeatureFlagSet.of();
                InclusiveRange<Integer> inclusiverange = getDeclaredPackVersions(p_330799_.id(), packmetadatasection);
                PackCompatibility packcompatibility = PackCompatibility.forVersion(inclusiverange, p_333544_);
                OverlayMetadataSection overlaymetadatasection = packresources.getMetadataSection(OverlayMetadataSection.TYPE);
                List<String> list = overlaymetadatasection != null ? overlaymetadatasection.overlaysForVersion(p_333544_) : List.of();
                pack$metadata = new Pack.Metadata(packmetadatasection.description(), packcompatibility, featureflagset, list);
            }

            return pack$metadata;
        } catch (Exception exception) {
            LOGGER.warn("Failed to read pack {} metadata", p_330799_.id(), exception);
            return null;
        }
    }

    private static InclusiveRange<Integer> getDeclaredPackVersions(String p_299045_, PackMetadataSection p_298414_) {
        int i = p_298414_.packFormat();
        if (p_298414_.supportedFormats().isEmpty()) {
            return new InclusiveRange<>(i);
        } else {
            InclusiveRange<Integer> inclusiverange = p_298414_.supportedFormats().get();
            if (!inclusiverange.isValueInRange(i)) {
                LOGGER.warn("Pack {} declared support for versions {} but declared main format is {}, defaulting to {}", p_299045_, inclusiverange, i, i);
                return new InclusiveRange<>(i);
            } else {
                return inclusiverange;
            }
        }
    }

    public PackLocationInfo location() {
        return this.location;
    }

    public Component getTitle() {
        return this.location.title();
    }

    public Component getDescription() {
        return this.metadata.description();
    }

    public Component getChatLink(boolean p_10438_) {
        return this.location.createChatLink(p_10438_, this.metadata.description);
    }

    public PackCompatibility getCompatibility() {
        return this.metadata.compatibility();
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.metadata.requestedFeatures();
    }

    public PackResources open() {
        return this.resources.openFull(this.location, this.metadata);
    }

    public String getId() {
        return this.location.id();
    }

    public PackSelectionConfig selectionConfig() {
        return this.selectionConfig;
    }

    public boolean isRequired() {
        return this.selectionConfig.required();
    }

    public boolean isFixedPosition() {
        return this.selectionConfig.fixedPosition();
    }

    public Pack.Position getDefaultPosition() {
        return this.selectionConfig.defaultPosition();
    }

    public PackSource getPackSource() {
        return this.location.source();
    }

    @Override
    public boolean equals(Object p_10448_) {
        if (this == p_10448_) {
            return true;
        } else {
            return !(p_10448_ instanceof Pack pack) ? false : this.location.equals(pack.location);
        }
    }

    @Override
    public int hashCode() {
        return this.location.hashCode();
    }

    public static record Metadata(Component description, PackCompatibility compatibility, FeatureFlagSet requestedFeatures, List<String> overlays) {
    }

    public static enum Position {
        TOP,
        BOTTOM;

        public <T> int insert(List<T> p_10471_, T p_10472_, Function<T, PackSelectionConfig> p_10473_, boolean p_10474_) {
            Pack.Position pack$position = p_10474_ ? this.opposite() : this;
            if (pack$position == BOTTOM) {
                int j;
                for (j = 0; j < p_10471_.size(); j++) {
                    PackSelectionConfig packselectionconfig1 = p_10473_.apply(p_10471_.get(j));
                    if (!packselectionconfig1.fixedPosition() || packselectionconfig1.defaultPosition() != this) {
                        break;
                    }
                }

                p_10471_.add(j, p_10472_);
                return j;
            } else {
                int i;
                for (i = p_10471_.size() - 1; i >= 0; i--) {
                    PackSelectionConfig packselectionconfig = p_10473_.apply(p_10471_.get(i));
                    if (!packselectionconfig.fixedPosition() || packselectionconfig.defaultPosition() != this) {
                        break;
                    }
                }

                p_10471_.add(i + 1, p_10472_);
                return i + 1;
            }
        }

        public Pack.Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }

    public interface ResourcesSupplier {
        PackResources openPrimary(PackLocationInfo p_332103_);

        PackResources openFull(PackLocationInfo p_330351_, Pack.Metadata p_333429_);
    }
}