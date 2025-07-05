package net.minecraft.world.level.block.entity.trialspawner;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public interface PlayerDetector {
    PlayerDetector NO_CREATIVE_PLAYERS = (p_327335_, p_327336_, p_327337_, p_327338_, p_327339_) -> p_327336_.getPlayers(
                p_327335_, p_375336_ -> p_375336_.blockPosition().closerThan(p_327337_, p_327338_) && !p_375336_.isCreative() && !p_375336_.isSpectator()
            )
            .stream()
            .filter(p_360514_ -> !p_327339_ || inLineOfSight(p_327335_, p_327337_.getCenter(), p_360514_.getEyePosition()))
            .map(Entity::getUUID)
            .toList();
    PlayerDetector INCLUDING_CREATIVE_PLAYERS = (p_327353_, p_327354_, p_327355_, p_327356_, p_327357_) -> p_327354_.getPlayers(
                p_327353_, p_375339_ -> p_375339_.blockPosition().closerThan(p_327355_, p_327356_) && !p_375339_.isSpectator()
            )
            .stream()
            .filter(p_360510_ -> !p_327357_ || inLineOfSight(p_327353_, p_327355_.getCenter(), p_360510_.getEyePosition()))
            .map(Entity::getUUID)
            .toList();
    PlayerDetector SHEEP = (p_327340_, p_327341_, p_327342_, p_327343_, p_327344_) -> {
        AABB aabb = new AABB(p_327342_).inflate(p_327343_);
        return p_327341_.getEntities(p_327340_, EntityType.SHEEP, aabb, LivingEntity::isAlive)
            .stream()
            .filter(p_360506_ -> !p_327344_ || inLineOfSight(p_327340_, p_327342_.getCenter(), p_360506_.getEyePosition()))
            .map(Entity::getUUID)
            .toList();
    };

    List<UUID> detect(ServerLevel p_309619_, PlayerDetector.EntitySelector p_330942_, BlockPos p_311426_, double p_331401_, boolean p_334733_);

    private static boolean inLineOfSight(Level p_332656_, Vec3 p_329624_, Vec3 p_332220_) {
        BlockHitResult blockhitresult = p_332656_.clip(
            new ClipContext(p_332220_, p_329624_, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty())
        );
        return blockhitresult.getBlockPos().equals(BlockPos.containing(p_329624_)) || blockhitresult.getType() == HitResult.Type.MISS;
    }

    public interface EntitySelector {
        PlayerDetector.EntitySelector SELECT_FROM_LEVEL = new PlayerDetector.EntitySelector() {
            @Override
            public List<ServerPlayer> getPlayers(ServerLevel p_336224_, Predicate<? super Player> p_335897_) {
                return p_336224_.getPlayers(p_335897_);
            }

            @Override
            public <T extends Entity> List<T> getEntities(
                ServerLevel p_327999_, EntityTypeTest<Entity, T> p_332093_, AABB p_333456_, Predicate<? super T> p_335480_
            ) {
                return p_327999_.getEntities(p_332093_, p_333456_, p_335480_);
            }
        };

        List<? extends Player> getPlayers(ServerLevel p_332311_, Predicate<? super Player> p_333154_);

        <T extends Entity> List<T> getEntities(ServerLevel p_334481_, EntityTypeTest<Entity, T> p_330467_, AABB p_330961_, Predicate<? super T> p_332077_);

        static PlayerDetector.EntitySelector onlySelectPlayer(Player p_329336_) {
            return onlySelectPlayers(List.of(p_329336_));
        }

        static PlayerDetector.EntitySelector onlySelectPlayers(final List<Player> p_329097_) {
            return new PlayerDetector.EntitySelector() {
                @Override
                public List<Player> getPlayers(ServerLevel p_332526_, Predicate<? super Player> p_329353_) {
                    return p_329097_.stream().filter(p_329353_).toList();
                }

                @Override
                public <T extends Entity> List<T> getEntities(
                    ServerLevel p_330015_, EntityTypeTest<Entity, T> p_329558_, AABB p_328059_, Predicate<? super T> p_334090_
                ) {
                    return p_329097_.stream().map(p_329558_::tryCast).filter(Objects::nonNull).filter(p_334090_).toList();
                }
            };
        }
    }
}