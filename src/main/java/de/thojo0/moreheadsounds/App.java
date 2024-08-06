package de.thojo0.moreheadsounds;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class App extends JavaPlugin {
    public String customPrefix = "§a[§e" + getName() + "§a] ";
    public String customPrefixFail = "§c[§7" + getName() + "§c] ";

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
            sender.sendMessage(customPrefixFail + "You need to type something after /" + label);

            return true;
        }
        switch (args[0]) {
            case "reload":
                if (!sender.hasPermission(command.getName() + ".reload")) {
                    sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                            .append(new TranslatableComponent("commands.help.failed")).color(ChatColor.RED)
                            .build());
                    return true;
                }
                runReload(sender);
                break;

            case "clearItem":
                if (!sender.hasPermission(command.getName() + ".clearItem")) {
                    sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                            .append(new TranslatableComponent("commands.help.failed")).color(ChatColor.RED)
                            .build());
                    return true;
                }
                if (player == null) {
                    sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                            .append(new TranslatableComponent("permissions.requires.player")).color(ChatColor.RED)
                            .build());
                    return true;
                }
                runClearItem(player);
                break;

            case "getHash":
                if (!sender.hasPermission(command.getName() + ".getHash")) {
                    sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                            .append(new TranslatableComponent("commands.help.failed")).color(ChatColor.RED)
                            .build());
                    return true;
                }
                if (player == null) {
                    sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                            .append(new TranslatableComponent("permissions.requires.player")).color(ChatColor.RED)
                            .build());
                    return true;
                }
                if (args.length == 1) {
                    runGetHash(player);
                    break;
                }
                if (args.length < 4) {
                    player.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                            .append(new TranslatableComponent("argument.pos3d.incomplete")).color(ChatColor.RED)
                            .build());
                    return true;
                }
                try {
                    runGetHash(sender, player.getWorld().getBlockAt(Integer.parseInt(args[1]),
                            Integer.parseInt(args[2]), Integer.parseInt(args[3])));
                } catch (NumberFormatException e) {
                    player.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                            .append(new TranslatableComponent("parsing.int.expected")).color(ChatColor.RED)
                            .build());
                    return true;
                }
                break;

            default:
                sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail)
                        .append(new TranslatableComponent("commands.help.failed")).color(ChatColor.RED)
                        .build());
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;

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
        if (args.length > 1) {
            if (args[0].equals("getHash") && player != null && sender.hasPermission(command.getName() + ".getHash")) {
                Block block = player.getTargetBlockExact(5);
                if (block != null) {
                    switch (args.length) {
                        case 2:
                            autoComplete.add(block.getX() + "");
                            autoComplete.add(block.getX() + " " + block.getY());
                            autoComplete.add(block.getX() + " " + block.getY() + " " + block.getZ());
                            break;
                        case 3:
                            autoComplete.add(block.getY() + "");
                            autoComplete.add(block.getY() + " " + block.getZ());
                            break;
                        case 4:
                            autoComplete.add(block.getZ() + "");
                            break;
                    }
                }
            }
        }
        return autoComplete;
    }

    public void runReload(CommandSender sender) {
        reloadConfig();
        eventListener.reloadConfig(getConfig());
        sender.sendMessage(customPrefix + "Configuration reloaded!");
    }

    public void runClearItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a player head
        if (item.getType() != Material.PLAYER_HEAD) {
            player.spigot().sendMessage(new ComponentBuilder(customPrefixFail + "You arn't holding a ")
                    .append(new TranslatableComponent(Material.PLAYER_HEAD.getItemTranslationKey())).build());
            return;
        }
        // Get the metadata for the player head
        SkullMeta metaData = (SkullMeta) item.getItemMeta();
        // remove the sound from the metadata
        metaData.setNoteBlockSound(null);
        item.setItemMeta(metaData);

        player.sendMessage(customPrefix + "Sound cleared from item!");
    }

    public void runGetHash(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if the item is a player head
        if (item.getType() != Material.PLAYER_HEAD) {
            player.spigot().sendMessage(new ComponentBuilder(customPrefixFail + "You arn't holding a ")
                    .append(new TranslatableComponent(Material.PLAYER_HEAD.getItemTranslationKey())).build());
            return;
        }
        // Get the metadata for the player head
        SkullMeta metaData = (SkullMeta) item.getItemMeta();
        PlayerProfile owner = metaData.getOwnerProfile();
        // Check owner exist
        if (owner == null) {
            player.sendMessage(customPrefixFail + "Coudn't find any texture hash!");
            return;
        }
        runGetHash(player, owner);
    }

    public void runGetHash(CommandSender sender, Block block) {
        // Check if the block above is a player head
        if (block.getType() != Material.PLAYER_HEAD) {
            sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail + "Target block isn't a ")
                    .append(new TranslatableComponent(Material.PLAYER_HEAD.getItemTranslationKey())).build());
            return;
        }
        // Get the skull data and texture hash for the player head
        Skull skull = (Skull) block.getState();
        PlayerProfile owner = skull.getOwnerProfile();
        // Check owner exist
        if (owner == null) {
            sender.sendMessage(customPrefixFail + "Coudn't find any texture hash!");
            return;
        }
        runGetHash(sender, owner);
    }

    public void runGetHash(CommandSender sender, PlayerProfile owner) {
        String textureHash = EventListener.getTextureHash(owner);
        sender.spigot().sendMessage(new ComponentBuilder(customPrefix + "Hash: §r").append(textureHash)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text(new TranslatableComponent("chat.copy.click"))))
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, textureHash)).build());
    }
}