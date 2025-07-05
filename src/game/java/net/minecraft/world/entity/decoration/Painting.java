package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Painting extends HangingEntity implements VariantHolder<Holder<PaintingVariant>> {
    private static final EntityDataAccessor<Holder<PaintingVariant>> DATA_PAINTING_VARIANT_ID = SynchedEntityData.defineId(Painting.class, EntityDataSerializers.PAINTING_VARIANT);
    public static final MapCodec<Holder<PaintingVariant>> VARIANT_MAP_CODEC = PaintingVariant.CODEC.fieldOf("variant");
    public static final Codec<Holder<PaintingVariant>> VARIANT_CODEC = VARIANT_MAP_CODEC.codec();
    public static final float DEPTH = 0.0625F;

    public Painting(EntityType<? extends Painting> p_31904_, Level p_31905_) {
        super(p_31904_, p_31905_);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder p_334800_) {
        p_334800_.define(DATA_PAINTING_VARIANT_ID, this.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getAny().orElseThrow());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_218896_) {
        if (DATA_PAINTING_VARIANT_ID.equals(p_218896_)) {
            this.recalculateBoundingBox();
        }
    }

    public void setVariant(Holder<PaintingVariant> p_218892_) {
        this.entityData.set(DATA_PAINTING_VARIANT_ID, p_218892_);
    }

    public Holder<PaintingVariant> getVariant() {
        return this.entityData.get(DATA_PAINTING_VARIANT_ID);
    }

    public static Optional<Painting> create(Level p_218888_, BlockPos p_218889_, Direction p_218890_) {
        Painting painting = new Painting(p_218888_, p_218889_);
        List<Holder<PaintingVariant>> list = new ArrayList<>();
        p_218888_.registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(list::add);
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            painting.setDirection(p_218890_);
            list.removeIf(p_359233_ -> {
                painting.setVariant((Holder<PaintingVariant>)p_359233_);
                return !painting.survives();
            });
            if (list.isEmpty()) {
                return Optional.empty();
            } else {
                int i = list.stream().mapToInt(Painting::variantArea).max().orElse(0);
                list.removeIf(p_218883_ -> variantArea((Holder<PaintingVariant>)p_218883_) < i);
                Optional<Holder<PaintingVariant>> optional = Util.getRandomSafe(list, painting.random);
                if (optional.isEmpty()) {
                    return Optional.empty();
                } else {
                    painting.setVariant(optional.get());
                    painting.setDirection(p_218890_);
                    return Optional.of(painting);
                }
            }
        }
    }

    private static int variantArea(Holder<PaintingVariant> p_218899_) {
        return p_218899_.value().area();
    }

    private Painting(Level p_218874_, BlockPos p_218875_) {
        super(EntityType.PAINTING, p_218874_, p_218875_);
    }

    public Painting(Level p_218877_, BlockPos p_218878_, Direction p_218879_, Holder<PaintingVariant> p_218880_) {
        this(p_218877_, p_218878_);
        this.setVariant(p_218880_);
        this.setDirection(p_218879_);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag p_31935_) {
        VARIANT_CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), this.getVariant()).ifSuccess(p_327008_ -> p_31935_.merge((CompoundTag)p_327008_));
        p_31935_.putByte("facing", (byte)this.direction.get2DDataValue());
        super.addAdditionalSaveData(p_31935_);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag p_31927_) {
        VARIANT_CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), p_31927_).ifSuccess(this::setVariant);
        this.direction = Direction.from2DDataValue(p_31927_.getByte("facing"));
        super.readAdditionalSaveData(p_31927_);
        this.setDirection(this.direction);
    }

    @Override
    protected AABB calculateBoundingBox(BlockPos p_344620_, Direction p_345489_) {
        float f = 0.46875F;
        Vec3 vec3 = Vec3.atCenterOf(p_344620_).relative(p_345489_, -0.46875);
        PaintingVariant paintingvariant = this.getVariant().value();
        double d0 = this.offsetForPaintingSize(paintingvariant.width());
        double d1 = this.offsetForPaintingSize(paintingvariant.height());
        Direction direction = p_345489_.getCounterClockWise();
        Vec3 vec31 = vec3.relative(direction, d0).relative(Direction.UP, d1);
        Direction.Axis direction$axis = p_345489_.getAxis();
        double d2 = direction$axis == Direction.Axis.X ? 0.0625 : (double)paintingvariant.width();
        double d3 = (double)paintingvariant.height();
        double d4 = direction$axis == Direction.Axis.Z ? 0.0625 : (double)paintingvariant.width();
        return AABB.ofSize(vec31, d2, d3, d4);
    }

    private double offsetForPaintingSize(int p_344506_) {
        return p_344506_ % 2 == 0 ? 0.5 : 0.0;
    }

    @Override
    public void dropItem(ServerLevel p_364635_, @Nullable Entity p_31925_) {
        if (p_364635_.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
            if (p_31925_ instanceof Player player && player.hasInfiniteMaterials()) {
                return;
            }

            this.spawnAtLocation(p_364635_, Items.PAINTING);
        }
    }

    @Override
    public void playPlacementSound() {
        this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
    }

    @Override
    public void moveTo(double p_31929_, double p_31930_, double p_31931_, float p_31932_, float p_31933_) {
        this.setPos(p_31929_, p_31930_, p_31931_);
    }

    @Override
    public void lerpTo(double p_31917_, double p_31918_, double p_31919_, float p_31920_, float p_31921_, int p_31922_) {
        this.setPos(p_31917_, p_31918_, p_31919_);
    }

    @Override
    public Vec3 trackingPosition() {
        return Vec3.atLowerCornerOf(this.pos);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity p_345195_) {
        return new ClientboundAddEntityPacket(this, this.direction.get3DDataValue(), this.getPos());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket p_218894_) {
        super.recreateFromPacket(p_218894_);
        this.setDirection(Direction.from3DDataValue(p_218894_.getData()));
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.PAINTING);
    }
}