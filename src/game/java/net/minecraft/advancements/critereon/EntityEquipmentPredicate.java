package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;

public record EntityEquipmentPredicate(
    Optional<ItemPredicate> head,
    Optional<ItemPredicate> chest,
    Optional<ItemPredicate> legs,
    Optional<ItemPredicate> feet,
    Optional<ItemPredicate> body,
    Optional<ItemPredicate> mainhand,
    Optional<ItemPredicate> offhand
) {
    public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(
        p_325209_ -> p_325209_.group(
                    ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head),
                    ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest),
                    ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs),
                    ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet),
                    ItemPredicate.CODEC.optionalFieldOf("body").forGetter(EntityEquipmentPredicate::body),
                    ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand),
                    ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand)
                )
                .apply(p_325209_, EntityEquipmentPredicate::new)
    );

    public static EntityEquipmentPredicate captainPredicate(HolderGetter<Item> p_330893_, HolderGetter<BannerPattern> p_366823_) {
        return EntityEquipmentPredicate.Builder.equipment()
            .head(
                ItemPredicate.Builder.item()
                    .of(p_330893_, Items.WHITE_BANNER)
                    .hasComponents(DataComponentPredicate.someOf(Raid.getOminousBannerInstance(p_366823_).getComponents(), DataComponents.BANNER_PATTERNS, DataComponents.ITEM_NAME))
            )
            .build();
    }

    public boolean matches(@Nullable Entity p_32194_) {
        if (p_32194_ instanceof LivingEntity livingentity) {
            if (this.head.isPresent() && !this.head.get().test(livingentity.getItemBySlot(EquipmentSlot.HEAD))) {
                return false;
            } else if (this.chest.isPresent() && !this.chest.get().test(livingentity.getItemBySlot(EquipmentSlot.CHEST))) {
                return false;
            } else if (this.legs.isPresent() && !this.legs.get().test(livingentity.getItemBySlot(EquipmentSlot.LEGS))) {
                return false;
            } else if (this.feet.isPresent() && !this.feet.get().test(livingentity.getItemBySlot(EquipmentSlot.FEET))) {
                return false;
            } else if (this.body.isPresent() && !this.body.get().test(livingentity.getItemBySlot(EquipmentSlot.BODY))) {
                return false;
            } else {
                return this.mainhand.isPresent() && !this.mainhand.get().test(livingentity.getItemBySlot(EquipmentSlot.MAINHAND))
                    ? false
                    : !this.offhand.isPresent() || this.offhand.get().test(livingentity.getItemBySlot(EquipmentSlot.OFFHAND));
            }
        } else {
            return false;
        }
    }

    public static class Builder {
        private Optional<ItemPredicate> head = Optional.empty();
        private Optional<ItemPredicate> chest = Optional.empty();
        private Optional<ItemPredicate> legs = Optional.empty();
        private Optional<ItemPredicate> feet = Optional.empty();
        private Optional<ItemPredicate> body = Optional.empty();
        private Optional<ItemPredicate> mainhand = Optional.empty();
        private Optional<ItemPredicate> offhand = Optional.empty();

        public static EntityEquipmentPredicate.Builder equipment() {
            return new EntityEquipmentPredicate.Builder();
        }

        public EntityEquipmentPredicate.Builder head(ItemPredicate.Builder p_298717_) {
            this.head = Optional.of(p_298717_.build());
            return this;
        }

        public EntityEquipmentPredicate.Builder chest(ItemPredicate.Builder p_301149_) {
            this.chest = Optional.of(p_301149_.build());
            return this;
        }

        public EntityEquipmentPredicate.Builder legs(ItemPredicate.Builder p_300588_) {
            this.legs = Optional.of(p_300588_.build());
            return this;
        }

        public EntityEquipmentPredicate.Builder feet(ItemPredicate.Builder p_301104_) {
            this.feet = Optional.of(p_301104_.build());
            return this;
        }

        public EntityEquipmentPredicate.Builder body(ItemPredicate.Builder p_330741_) {
            this.body = Optional.of(p_330741_.build());
            return this;
        }

        public EntityEquipmentPredicate.Builder mainhand(ItemPredicate.Builder p_299261_) {
            this.mainhand = Optional.of(p_299261_.build());
            return this;
        }

        public EntityEquipmentPredicate.Builder offhand(ItemPredicate.Builder p_297723_) {
            this.offhand = Optional.of(p_297723_.build());
            return this;
        }

        public EntityEquipmentPredicate build() {
            return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.body, this.mainhand, this.offhand);
        }
    }
}