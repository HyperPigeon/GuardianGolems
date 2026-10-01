package net.hyper_pigeon.guardian_golems.goals;

import com.destroystokyo.paper.entity.ai.Goal;
import net.hyper_pigeon.guardian_golems.GuardianGolems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public abstract class AbstractDefendGoal implements Goal<Golem>, Listener {

    protected final Golem golem;
    protected final NamespacedKey creatorKey;
    private LivingEntity target;

    private final double maxTargetDistance = 16.0F;

    protected AbstractDefendGoal(Golem golem, NamespacedKey creatorKey) {
        this.golem = golem;
        this.creatorKey = creatorKey;
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
        if (target == null) return;
        if (!target.equals(golem.getTarget())) {
            target = null;
            return;
        }
        if (!target.isValid() || target.isDead()
                || !target.getWorld().equals(golem.getWorld())
                || target.getLocation().distance(golem.getLocation()) > maxTargetDistance) {
            setTarget(null);
        }
    }

    public LivingEntity getTarget() {
        return target;
    }

    public void setTarget(LivingEntity target) {
        this.target = target;
        golem.setTarget(this.target);
    }

    public Player getCreator() {
        UUID creatorId = getCreatorId(golem);
        return creatorId == null ? null : Bukkit.getPlayer(creatorId);
    }

    protected UUID getCreatorId(Golem golem) {
        PersistentDataContainer data = golem.getPersistentDataContainer();
        String creatorId = data.get(creatorKey, PersistentDataType.STRING);
        return creatorId == null ? null : UUID.fromString(creatorId);
    }

    protected boolean isCreator(Entity entity) {
        return entity != null && entity.getUniqueId().equals(getCreatorId(golem));
    }

    protected LivingEntity resolveAttacker(Entity damager) {
        if (damager instanceof LivingEntity livingEntity) return livingEntity;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof LivingEntity shooter) {
            return shooter;
        }
        return null;
    }

    public boolean isValidTarget(LivingEntity target) {
        if (!target.isValid() || target.equals(golem) || isCreator(target)) return false;
        if (target instanceof Player p)
            return p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR;
        else if (target instanceof Golem g) {
            UUID otherCreatorId = getCreatorId(g);
            return otherCreatorId == null || !otherCreatorId.equals(getCreatorId(golem));
        }
        return true;
    }
}
