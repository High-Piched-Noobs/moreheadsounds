package de.thojo0.moreheadsounds;

import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new EventListener(getConfig()), this);
        getLogger().info("MoreHeadSounds activated!");
    }
    @Override
    public void onDisable() {
        getLogger().info("MoreHeadSounds deactivated!");
    }
}