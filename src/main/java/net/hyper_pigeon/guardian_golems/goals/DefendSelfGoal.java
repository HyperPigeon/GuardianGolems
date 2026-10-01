package net.hyper_pigeon.guardian_golems.goals;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.EnumSet;


public class DefendSelfGoal extends AbstractDefendGoal {

    public static final GoalKey<Golem> REFERENCE_KEY = GoalKey.of(Golem.class,
            new NamespacedKey("guardian_golems", "defend_self"));

    public DefendSelfGoal(Golem golem, NamespacedKey creatorKey) {
        super(golem, creatorKey);
    }

    @Override
    public GoalKey<Golem> getKey() {
        return REFERENCE_KEY;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        // Deliberately flagless: DefendCreatorGoal is always active and holds GoalType.TARGET, so a
        // second TARGET goal would be locked out of ever running. This goal only listens for damage
        // events and hands the golem a target, so it needs no goal flags of its own.
        return EnumSet.noneOf(GoalType.class);
    }

    @EventHandler
    public void entityDamagedByEntity(EntityDamageByEntityEvent event) {
        if (getTarget() != null) return;
        if (!event.getEntity().equals(golem)) return;
        LivingEntity attacker = resolveAttacker(event.getDamager());
        if (attacker == null || isCreator(attacker)) return;
        if (isValidTarget(attacker)) setTarget(attacker);
    }
}
