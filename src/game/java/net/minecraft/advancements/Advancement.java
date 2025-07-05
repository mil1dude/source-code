package net.minecraft.advancements;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record Advancement(
    Optional<ResourceLocation> parent,
    Optional<DisplayInfo> display,
    AdvancementRewards rewards,
    Map<String, Criterion<?>> criteria,
    AdvancementRequirements requirements,
    boolean sendsTelemetryEvent,
    Optional<Component> name
) {
    private static final Codec<Map<String, Criterion<?>>> CRITERIA_CODEC = Codec.unboundedMap(Codec.STRING, Criterion.CODEC)
        .validate(p_308091_ -> p_308091_.isEmpty() ? DataResult.error(() -> "Advancement criteria cannot be empty") : DataResult.success(p_308091_));
    public static final Codec<Advancement> CODEC = RecordCodecBuilder.<Advancement>create(
            p_325179_ -> p_325179_.group(
                        ResourceLocation.CODEC.optionalFieldOf("parent").forGetter(Advancement::parent),
                        DisplayInfo.CODEC.optionalFieldOf("display").forGetter(Advancement::display),
                        AdvancementRewards.CODEC.optionalFieldOf("rewards", AdvancementRewards.EMPTY).forGetter(Advancement::rewards),
                        CRITERIA_CODEC.fieldOf("criteria").forGetter(Advancement::criteria),
                        AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter(p_308099_ -> Optional.of(p_308099_.requirements())),
                        Codec.BOOL.optionalFieldOf("sends_telemetry_event", Boolean.valueOf(false)).forGetter(Advancement::sendsTelemetryEvent)
                    )
                    .apply(p_325179_, (p_308085_, p_308086_, p_308087_, p_308088_, p_308089_, p_308090_) -> {
                        AdvancementRequirements advancementrequirements = p_308089_.orElseGet(() -> AdvancementRequirements.allOf(p_308088_.keySet()));
                        return new Advancement(p_308085_, p_308086_, p_308087_, p_308088_, advancementrequirements, p_308090_);
                    })
        )
        .validate(Advancement::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, Advancement> STREAM_CODEC = StreamCodec.ofMember(Advancement::write, Advancement::read);

    public Advancement(
        Optional<ResourceLocation> p_299284_,
        Optional<DisplayInfo> p_301017_,
        AdvancementRewards p_286389_,
        Map<String, Criterion<?>> p_286635_,
        AdvancementRequirements p_300504_,
        boolean p_286478_
    ) {
        this(p_299284_, p_301017_, p_286389_, Map.copyOf(p_286635_), p_300504_, p_286478_, p_301017_.map(Advancement::decorateName));
    }

    private static DataResult<Advancement> validate(Advancement p_312373_) {
        return p_312373_.requirements().validate(p_312373_.criteria().keySet()).map(p_308094_ -> p_312373_);
    }

    private static Component decorateName(DisplayInfo p_300038_) {
        Component component = p_300038_.getTitle();
        ChatFormatting chatformatting = p_300038_.getType().getChatColor();
        Component component1 = ComponentUtils.mergeStyles(component.copy(), Style.EMPTY.withColor(chatformatting))
            .append("\n")
            .append(p_300038_.getDescription());
        Component component2 = component.copy().withStyle(p_138316_ -> p_138316_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, component1)));
        return ComponentUtils.wrapInSquareBrackets(component2).withStyle(chatformatting);
    }

    public static Component name(AdvancementHolder p_297556_) {
        return p_297556_.value().name().orElseGet(() -> Component.literal(p_297556_.id().toString()));
    }

    private void write(RegistryFriendlyByteBuf p_328062_) {
        p_328062_.writeOptional(this.parent, FriendlyByteBuf::writeResourceLocation);
        DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(p_328062_, this.display);
        this.requirements.write(p_328062_);
        p_328062_.writeBoolean(this.sendsTelemetryEvent);
    }

    private static Advancement read(RegistryFriendlyByteBuf p_328348_) {
        return new Advancement(
            p_328348_.readOptional(FriendlyByteBuf::readResourceLocation),
            (Optional<DisplayInfo>)DisplayInfo.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(p_328348_),
            AdvancementRewards.EMPTY,
            Map.of(),
            new AdvancementRequirements(p_328348_),
            p_328348_.readBoolean()
        );
    }

    public boolean isRoot() {
        return this.parent.isEmpty();
    }

    public void validate(ProblemReporter p_310503_, HolderGetter.Provider p_335087_) {
        this.criteria.forEach((p_325177_, p_325178_) -> {
            CriterionValidator criterionvalidator = new CriterionValidator(p_310503_.forChild(p_325177_), p_335087_);
            p_325178_.triggerInstance().validate(criterionvalidator);
        });
    }

    public static class Builder {
        private Optional<ResourceLocation> parent = Optional.empty();
        private Optional<DisplayInfo> display = Optional.empty();
        private AdvancementRewards rewards = AdvancementRewards.EMPTY;
        private final ImmutableMap.Builder<String, Criterion<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private boolean sendsTelemetryEvent;

        public static Advancement.Builder advancement() {
            return new Advancement.Builder().sendsTelemetryEvent();
        }

        public static Advancement.Builder recipeAdvancement() {
            return new Advancement.Builder();
        }

        public Advancement.Builder parent(AdvancementHolder p_300513_) {
            this.parent = Optional.of(p_300513_.id());
            return this;
        }

        @Deprecated(
            forRemoval = true
        )
        public Advancement.Builder parent(ResourceLocation p_138397_) {
            this.parent = Optional.of(p_138397_);
            return this;
        }

        public Advancement.Builder display(
            ItemStack p_138363_,
            Component p_138364_,
            Component p_138365_,
            @Nullable ResourceLocation p_138366_,
            AdvancementType p_310090_,
            boolean p_138368_,
            boolean p_138369_,
            boolean p_138370_
        ) {
            return this.display(new DisplayInfo(p_138363_, p_138364_, p_138365_, Optional.ofNullable(p_138366_), p_310090_, p_138368_, p_138369_, p_138370_));
        }

        public Advancement.Builder display(
            ItemLike p_138372_,
            Component p_138373_,
            Component p_138374_,
            @Nullable ResourceLocation p_138375_,
            AdvancementType p_309840_,
            boolean p_138377_,
            boolean p_138378_,
            boolean p_138379_
        ) {
            return this.display(
                new DisplayInfo(
                    new ItemStack(p_138372_.asItem()), p_138373_, p_138374_, Optional.ofNullable(p_138375_), p_309840_, p_138377_, p_138378_, p_138379_
                )
            );
        }

        public Advancement.Builder display(DisplayInfo p_138359_) {
            this.display = Optional.of(p_138359_);
            return this;
        }

        public Advancement.Builder rewards(AdvancementRewards.Builder p_138355_) {
            return this.rewards(p_138355_.build());
        }

        public Advancement.Builder rewards(AdvancementRewards p_138357_) {
            this.rewards = p_138357_;
            return this;
        }

        public Advancement.Builder addCriterion(String p_138384_, Criterion<?> p_138385_) {
            this.criteria.put(p_138384_, p_138385_);
            return this;
        }

        public Advancement.Builder requirements(AdvancementRequirements.Strategy p_298091_) {
            this.requirementsStrategy = p_298091_;
            return this;
        }

        public Advancement.Builder requirements(AdvancementRequirements p_300756_) {
            this.requirements = Optional.of(p_300756_);
            return this;
        }

        public Advancement.Builder sendsTelemetryEvent() {
            this.sendsTelemetryEvent = true;
            return this;
        }

        public AdvancementHolder build(ResourceLocation p_138404_) {
            Map<String, Criterion<?>> map = this.criteria.buildOrThrow();
            AdvancementRequirements advancementrequirements = this.requirements.orElseGet(() -> this.requirementsStrategy.create(map.keySet()));
            return new AdvancementHolder(
                p_138404_, new Advancement(this.parent, this.display, this.rewards, map, advancementrequirements, this.sendsTelemetryEvent)
            );
        }

        public AdvancementHolder save(Consumer<AdvancementHolder> p_138390_, String p_138391_) {
            AdvancementHolder advancementholder = this.build(ResourceLocation.parse(p_138391_));
            p_138390_.accept(advancementholder);
            return advancementholder;
        }
    }
}