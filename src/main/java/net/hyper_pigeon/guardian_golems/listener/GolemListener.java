package net.hyper_pigeon.guardian_golems.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.UUID;
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
                }
            }
        }
    }

//    @EventHandler // FIXME: Unused block could be entirely removed
//    public void onGolemAdd(EntityAddToWorldEvent event) {
//        if (!(event.getEntity() instanceof IronGolem golem)) return;
//
//        Location loc = golem.getLocation();
//        for (Location pumpkinLoc : pumpkinNames.keySet()) {
//            if (!pumpkinLoc.getWorld().equals(loc.getWorld())) continue;
//            if (pumpkinLoc.distanceSquared(loc) <= 9) {
//                String playerName = pumpkinNames.remove(pumpkinLoc);
//                Player creator = Bukkit.getPlayerExact(playerName);
//                if (creator == null) return;
//                golem.getPersistentDataContainer().set(creatorKey, PersistentDataType.STRING, creator.getUniqueId().toString());
//            }
//        }
//    }

    // Iron Golem will protect creator when they are attacked or assist them when they fight.
    @EventHandler
    public void onCreatorFight(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Entity attacker = event.getDamager();

        if (!(attacker instanceof LivingEntity livingAttacker)) return;

        UUID victimId = victim.getUniqueId();

        for (IronGolem golem : victim.getWorld().getEntitiesByClass(IronGolem.class)) {
            PersistentDataContainer data = golem.getPersistentDataContainer();

            if ( !data.has(creatorKey, PersistentDataType.STRING)) continue;

            UUID creatorId = UUID.fromString(Objects.requireNonNull(data.get(creatorKey, PersistentDataType.STRING)));

            boolean attackerIsCreator = attacker.getUniqueId().equals(creatorId); // FIXME: unused variable?

            if(creatorId.equals(victimId)) // FIXME: if with no block?

            if(attacker.getUniqueId().equals(creatorId) && !creatorId.equals(victimId)) {
                golem.setTarget(victim);
            }

            if (!creatorId.equals(victimId)) continue;

            golem.setTarget(livingAttacker);
        }
    }
}
