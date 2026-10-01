package net.hyper_pigeon.guardian_golems.goals;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.EnumSet;

public class DefendCreatorGoal extends AbstractDefendGoal {

    public static final GoalKey<Golem> REFERENCE_KEY = GoalKey.of(Golem.class,
            new NamespacedKey("guardian_golems", "defend_creator"));

    public DefendCreatorGoal(Golem golem, NamespacedKey creatorKey) {
        super(golem, creatorKey);
    }

    @Override
    public GoalKey<Golem> getKey() {
        return REFERENCE_KEY;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }

    @EventHandler
    public void entityDamagedByEntity(EntityDamageByEntityEvent event) {
        if (getTarget() != null) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!isCreator(victim)) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!attacker.equals(golem) && isValidTarget(attacker)) setTarget(attacker);
    }
}
