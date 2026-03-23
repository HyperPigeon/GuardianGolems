package net.hyper_pigeon.guardian_golems;

import net.hyper_pigeon.guardian_golems.listener.GolemListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class GuardianGolems extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new GolemListener(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
