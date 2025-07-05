package net.minecraft.world.entity.ai.behavior;

import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

public class MeleeAttack {
    public static <T extends Mob> OneShot<T> create(int p_259758_) {
        return create(p_358993_ -> true, p_259758_);
    }

    public static <T extends Mob> OneShot<T> create(Predicate<T> p_365972_, int p_361186_) {
        return BehaviorBuilder.create(
            p_358992_ -> p_358992_.group(
                        p_358992_.registered(MemoryModuleType.LOOK_TARGET),
                        p_358992_.present(MemoryModuleType.ATTACK_TARGET),
                        p_358992_.absent(MemoryModuleType.ATTACK_COOLING_DOWN),
                        p_358992_.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                    )
                    .apply(
                        p_358992_,
                        (p_358997_, p_358998_, p_358999_, p_359000_) -> (p_359008_, p_359009_, p_359010_) -> {
                                LivingEntity livingentity = p_358992_.get(p_358998_);
                                if (p_365972_.test(p_359009_)
                                    && !isHoldingUsableProjectileWeapon(p_359009_)
                                    && p_359009_.isWithinMeleeAttackRange(livingentity)
                                    && p_358992_.<NearestVisibleLivingEntities>get(p_359000_).contains(livingentity)) {
                                    p_358997_.set(new EntityTracker(livingentity, true));
                                    p_359009_.swing(InteractionHand.MAIN_HAND);
                                    p_359009_.doHurtTarget(p_359008_, livingentity);
                                    p_358999_.setWithExpiry(true, (long)p_361186_);
                                    return true;
                                } else {
                                    return false;
                                }
                            }
                    )
        );
    }

    private static boolean isHoldingUsableProjectileWeapon(Mob p_23528_) {
        return p_23528_.isHolding(p_147697_ -> {
            Item item = p_147697_.getItem();
            return item instanceof ProjectileWeaponItem && p_23528_.canFireProjectileWeapon((ProjectileWeaponItem)item);
        });
    }
}