package de.thojo0.moreheadsounds;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin {
    public String customPrefix = "§a[§e" + getName() + "§a]";
    public String customPrefixFail = "§c[§7" + getName() + "§c]";

    private final EventListener eventListener = new EventListener(getConfig());

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(eventListener, this);
        PluginCommand command = getCommand(getName().toLowerCase());
        command.setExecutor(this);
        command.setTabCompleter(this);
        getLogger().info(getName() + " activated!");
    }

    @Override
    public void onDisable() {
        getLogger().info(getName() + " deactivated!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(customPrefixFail + " You need to type something after /" + label + "\n");
            return true;
        }
        switch (args[0]) {
            case "reload":
                if (!sender.hasPermission(command.getName() + ".reload")) {
                    sender.sendMessage(customPrefixFail + " You don't have permission to perform that command!");
                    return true;
                }
                reloadConfig();
                eventListener.reloadConfig(getConfig());
                sender.sendMessage(customPrefix + " Configuration reloaded!");
                return true;
        }
        return super.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        LinkedList<String> autoComplete = new LinkedList<String>();
        if (args.length == 1 && (sender.hasPermission(command.getName() + ".reload"))) {
            autoComplete.add("reload");
        }
        return autoComplete;
    }
}