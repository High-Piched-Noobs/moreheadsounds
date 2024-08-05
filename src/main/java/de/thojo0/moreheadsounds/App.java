package de.thojo0.moreheadsounds;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

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
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;

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
                runReload(sender);
                break;

            case "clearItem":
                if (player == null) {
                    sender.sendMessage(customPrefixFail + " You can only this command as player!");
                    return true;
                }
                if (!sender.hasPermission(command.getName() + ".clearItem")) {
                    sender.sendMessage(customPrefixFail + " You don't have permission to perform that command!");
                    return true;
                }
                runClearItem(player);
                break;

            case "getHash":
                if (player == null) {
                    sender.sendMessage(customPrefixFail + " You can only this command as player!");
                    return true;
                }
                if (!sender.hasPermission(command.getName() + ".getHash")) {
                    sender.sendMessage(customPrefixFail + " You don't have permission to perform that command!");
                    return true;
                }
                runGetHash(player);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        LinkedList<String> autoComplete = new LinkedList<String>();
        if (args.length == 1) {
            if (sender.hasPermission(command.getName() + ".reload")) {
                autoComplete.add("reload");
            }
            if (sender.hasPermission(command.getName() + ".clearItem")) {
                autoComplete.add("clearItem");
            }
            if (sender.hasPermission(command.getName() + ".getHash")) {
                autoComplete.add("getHash");
            }
        }
        return autoComplete;
    }

    public void runReload(CommandSender sender) {
        reloadConfig();
        eventListener.reloadConfig(getConfig());
        sender.sendMessage(customPrefix + " Configuration reloaded!");
    }

    public void runClearItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a player head
        if (item.getType() != Material.PLAYER_HEAD) {
            player.spigot().sendMessage(new ComponentBuilder(customPrefixFail + " You arn't holding a ")
                    .append(new TranslatableComponent(Material.PLAYER_HEAD.getItemTranslationKey())).build());
            return;
        }
        // Get the metadata for the player head
        SkullMeta metaData = (SkullMeta) item.getItemMeta();
        // remove the sound from the metadata
        metaData.setNoteBlockSound(null);
        item.setItemMeta(metaData);

        player.sendMessage(customPrefix + " Sound cleared from item!");
    }

    public void runGetHash(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a player head
        if (item.getType() != Material.PLAYER_HEAD) {
            player.spigot().sendMessage(new ComponentBuilder(customPrefixFail + " You arn't holding a ")
                    .append(new TranslatableComponent(Material.PLAYER_HEAD.getItemTranslationKey())).build());
            return;
        }
        // Get the metadata for the player head
        SkullMeta metaData = (SkullMeta) item.getItemMeta();
        PlayerProfile owner = metaData.getOwnerProfile();
        // Check owner exist
        if (owner == null) {
            player.sendMessage(customPrefixFail + " Coudn't find any texture hash!");
            return;
        }

        String textureHash = EventListener.getTextureHash(owner);

        player.spigot().sendMessage(new ComponentBuilder(customPrefix + " Hash: §r").append(textureHash)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to copy to clipboard")))
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, textureHash)).build());
    }
}