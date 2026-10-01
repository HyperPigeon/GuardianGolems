package net.hyper_pigeon.guardian_golems.listener;

import com.destroystokyo.paper.entity.ai.GoalKey;
import net.hyper_pigeon.guardian_golems.goals.DefendCreatorGoal;
import net.hyper_pigeon.guardian_golems.goals.DefendSelfGoal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

public class GolemListener implements Listener {


    private final ConcurrentHashMap<Location, String> pumpkinNames = new ConcurrentHashMap<>();
    private final NamespacedKey creatorKey;
    public GolemListener(Plugin plugin) {
        this.creatorKey = new NamespacedKey(plugin, "creator");
    }

    @EventHandler
    public void onPumpkinPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.CARVED_PUMPKIN) return;
        if (!item.hasItemMeta()) return;
        Component displayName = item.getItemMeta().displayName();
        if(displayName != null) {
            String nameAsPlainText = PlainTextComponentSerializer.plainText().serialize(displayName);
            pumpkinNames.put(event.getBlockPlaced().getLocation(), nameAsPlainText);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if(reason == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM || reason == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN) {
            Golem golem = (Golem) event.getEntity();
            Location loc = golem.getLocation();
            for (Location pumpkinLoc : pumpkinNames.keySet()) {
                if (!pumpkinLoc.getWorld().equals(loc.getWorld())) continue;
                if (pumpkinLoc.distanceSquared(loc) <= 6.25) {
                    String playerName = pumpkinNames.remove(pumpkinLoc);
                    Player creator = Bukkit.getPlayerExact(playerName);
                    if (creator == null) return;
                    golem.getPersistentDataContainer().set(creatorKey, PersistentDataType.STRING, creator.getUniqueId().toString());
                    Bukkit.getMobGoals().addGoal(golem, 0, new DefendCreatorGoal(golem, creatorKey));
                    Bukkit.getMobGoals().addGoal(golem, 1, new DefendSelfGoal(golem, creatorKey));
                }
            }
        }
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        event.getEntities().forEach(entity -> {
            if (!(entity instanceof Golem golem)) return;

            String creator = golem.getPersistentDataContainer()
                    .get(creatorKey, PersistentDataType.STRING);

            if (creator != null) {
                if (!hasGoal(golem, DefendCreatorGoal.REFERENCE_KEY)) {
                    Bukkit.getMobGoals().addGoal(golem, 0, new DefendCreatorGoal(golem, creatorKey));
                }
                if (!hasGoal(golem, DefendSelfGoal.REFERENCE_KEY)) {
                    Bukkit.getMobGoals().addGoal(golem, 1, new DefendSelfGoal(golem, creatorKey));
                }
            }
        });
    }

    private boolean hasGoal(Golem golem, GoalKey<Golem> key) {
        return !Bukkit.getMobGoals().getGoals(golem, key).isEmpty();
    }
}
