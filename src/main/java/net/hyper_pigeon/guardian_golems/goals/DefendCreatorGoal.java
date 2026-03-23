package net.hyper_pigeon.guardian_golems.goals;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import net.hyper_pigeon.guardian_golems.GuardianGolems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

public class DefendCreatorGoal implements Goal<Golem>, Listener {

    public static final GoalKey<Golem> REFERENCE_KEY = GoalKey.of(Golem.class,
            new NamespacedKey("guardian_golems", "defend_creator"));
    private final GoalKey<Golem> key;
    private final Golem golem;
    private LivingEntity target;
    private final NamespacedKey creatorKey;

    private final double maxTargetDistance = 16.0F;

    public DefendCreatorGoal(Golem golem, NamespacedKey creatorKey) {
        this.golem = golem;
        this.creatorKey = creatorKey;
        this.key = GoalKey.of(Golem.class,
                new NamespacedKey("guardian_golems", "defend_creator"));
    }

    @Override
    public boolean shouldActivate() {
        return true;
    }

    @Override
    public boolean shouldStayActive() {
        return shouldActivate();
    }

    @Override
    public void start() {
        Bukkit.getPluginManager().registerEvents(this, GuardianGolems.getPlugin(GuardianGolems.class));
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void tick() {
        if (target != null) {
            if (!target.isValid() || target.isDead() || target.getLocation().distance(golem.getLocation()) > maxTargetDistance) {
                setTarget(null);
            }
        }
    }

    @Override
    public GoalKey<Golem> getKey() {
        return key;
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.TARGET);
    }

    public Player getCreator() {
        PersistentDataContainer data = golem.getPersistentDataContainer();
        UUID creatorId = UUID.fromString(Objects.requireNonNull(data.get(creatorKey, PersistentDataType.STRING)));
        return Bukkit.getPlayer(creatorId);
    }

    public boolean isValidTarget(LivingEntity target) {
        if (!target.isValid() || target.equals(getCreator())) return false;
        if (target instanceof Player p)
            return !p.equals(getCreator()) && p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR;
        else if (target instanceof Golem g) {
            Goal<Golem> goal = Bukkit.getMobGoals().getGoal(g, key);
            if (goal instanceof DefendCreatorGoal defendCreatorGoal) {
                return !defendCreatorGoal.getCreator().equals(getCreator());
            }
        }
        return true;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
        golem.setTarget(this.target);
    }

    @EventHandler
    public void entityDamagedByEntity(EntityDamageByEntityEvent event) {
        if(target != null) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!victim.equals(getCreator())) return;
        if(!(event.getDamager() instanceof LivingEntity attacker)) return;
        if(!attacker.equals(getCreator()) && !attacker.equals(golem) && isValidTarget(attacker)) setTarget(attacker);
    }
}