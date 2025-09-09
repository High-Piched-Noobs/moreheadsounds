package de.thojo0.moreheadsounds;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import de.thojo0.moreheadsounds.CommandUtils.Failed;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class App extends JavaPlugin {
    private static final int ITEMS_PER_PAGE = 15;
    public static String customPrefix;
    public static String customPrefixFail;

    {
        customPrefix = "§a[§e" + getName() + "§a] ";
        customPrefixFail = "§c[§7" + getName() + "§c] ";
    }

    private final EventListener eventListener = new EventListener(getConfig());

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(eventListener, this);
        getLogger().info(getName() + " activated!");
    }

    @Override
    public void onDisable() {
        getLogger().info(getName() + " deactivated!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final CommandUtils utils = new CommandUtils(sender, command);
        try {
            utils.checkArgs(args, 1, label);
            switch (args[0].toLowerCase()) {
                case "config":
                    utils.checkPermission("config");
                    utils.checkArgs(args, 2, label + " config");
                    switch (args[1].toLowerCase()) {
                        case "reload":
                            runReload(sender);
                            break;
                        case "list":
                            if (args.length > 2) {
                                try {
                                    int page = Integer.parseInt(args[2]);
                                    runList(sender, page);
                                } catch (NumberFormatException e) {
                                    if (args[2].length() >= 32) {
                                        giveHead(utils.checkPlayer(), args[2]);
                                    } else {
                                        utils.fail("parsing.int.expected");
                                    }
                                }
                            } else {
                                runList(sender, 1);
                            }
                            break;
                        case "include":
                            utils.checkArgs(args, 3, label + " config include <key>");
                            runAdd(utils.checkPlayer(), args[2], "include");
                            break;
                        case "exclude":
                            utils.checkArgs(args, 3, label + " config exclude <key>");
                            runAdd(utils.checkPlayer(), args[2], "exclude");
                            break;
                        case "remove":
                            utils.checkArgs(args, 3, label + " config remove <key>");
                            runRemove(utils.checkPlayer(), args[2]);
                            break;
                        case "show":
                            runShow(utils.checkPlayer());
                            break;
                        default:
                            utils.fail();
                    }
                    break;

                case "clearitem":
                    utils.checkPermission("clearItem");
                    runClearItem(utils.checkPlayer());
                    break;

                case "gethash":
                    utils.checkPermission("getHash");
                    Player p = utils.checkPlayer();
                    if (args.length == 1) {
                        runGetHash(p);
                        break;
                    }
                    if (args.length < 4) {
                        utils.fail("argument.pos3d.incomplete");
                    }
                    try {
                        runGetHash(sender, p.getWorld().getBlockAt(Integer.parseInt(args[1]),
                                Integer.parseInt(args[2]), Integer.parseInt(args[3])));
                    } catch (NumberFormatException e) {
                        utils.fail("parsing.int.expected");
                    }
                    break;

                default:
                    utils.fail();
            }
        } catch (Failed e) {
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player player = null;
        if (sender instanceof Player)
            player = (Player) sender;

        List<String> autoComplete = new LinkedList<String>();
        if (args.length == 1) {
            if (sender.hasPermission(command.getName() + ".config")) {
                autoComplete.add("config");
            }
            if (sender.hasPermission(command.getName() + ".clearItem")) {
                autoComplete.add("clearItem");
            }
            if (sender.hasPermission(command.getName() + ".getHash")) {
                autoComplete.add("getHash");
            }
        }
        if (args.length > 1) {
            switch (args[0].toLowerCase()) {
                case "config":
                    if (!sender.hasPermission(command.getName() + ".config"))
                        break;
                    if (args.length == 2) {
                        autoComplete.add("reload");
                        autoComplete.add("include");
                        autoComplete.add("exclude");
                        autoComplete.add("remove");
                        autoComplete.add("show");
                        autoComplete.add("list");
                    } else if (args.length == 3) {
                        switch (args[1].toLowerCase()) {
                            case "include":
                            case "exclude":
                                String currentInput = args[2].toLowerCase();
                                Set<String> suggestions = new HashSet<>();
                                for (Sound sound : Sound.values()) {
                                    String key = sound.getKey().getKey();
                                    if (key.startsWith(currentInput)) {
                                        int nextDot = key.indexOf('.', currentInput.length());
                                        if (nextDot >= 0) {
                                            suggestions.add(key.substring(0, nextDot + 1));
                                        } else {
                                            suggestions.add(key);
                                        }
                                    }
                                }
                                autoComplete.addAll(suggestions);
                                break;
                            case "remove":
                                if (player != null) {
                                    String hash = getHashFromHeldItem(player);
                                    if (hash != null) {
                                        ConfigurationSection section = getConfig().getConfigurationSection(hash);
                                        if (section != null) {
                                            autoComplete.addAll(section.getStringList("include"));
                                            autoComplete.addAll(section.getStringList("exclude"));
                                        }
                                    }
                                }
                                break;
                        }
                    }
                    break;

                case "gethash":
                    if (player != null && sender.hasPermission(command.getName() + ".getHash")) {
                        addTargetBlockToAutoComplete(autoComplete, player, args.length - 2);
                    }
                    break;
            }
        }
        return autoComplete;
    }

    private ItemStack getHeldPlayerHead(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.PLAYER_HEAD) {
            player.spigot().sendMessage(new ComponentBuilder(customPrefixFail + "You arn't holding a ")
                    .append(new TranslatableComponent(Material.PLAYER_HEAD.getItemTranslationKey())).build());
            return null;
        }
        return item;
    }

    private String getHashFromHeldItem(Player player) {
        ItemStack item = getHeldPlayerHead(player);
        if (item == null) {
            return null;
        }
        SkullMeta metaData = (SkullMeta) item.getItemMeta();
        PlayerProfile owner = metaData.getOwnerProfile();
        if (owner == null) {
            player.sendMessage(customPrefixFail + "Coudn't find any texture hash on the held item!");
            return null;
        }
        return EventListener.getTextureHash(owner);
    }

    private static void addTargetBlockToAutoComplete(List<String> autoComplete, Player player, int onArg) {
        Block block = player.getTargetBlockExact(5);
        if (block == null)
            return;
        switch (onArg) {
            case 0:
                autoComplete.add(block.getX() + "");
                autoComplete.add(block.getX() + " " + block.getY());
                autoComplete.add(block.getX() + " " + block.getY() + " " + block.getZ());
                break;
            case 1:
                autoComplete.add(block.getY() + "");
                autoComplete.add(block.getY() + " " + block.getZ());
                break;
            case 2:
                autoComplete.add(block.getZ() + "");
                break;
        }
    }

    public void runReload(CommandSender sender) {
        reloadConfig();
        eventListener.reloadConfig(getConfig());
        sender.sendMessage(customPrefix + "Configuration reloaded!");
    }

    public void runClearItem(Player player) {
        ItemStack item = getHeldPlayerHead(player);
        if (item == null) {
            return;
        }
        SkullMeta metaData = (SkullMeta) item.getItemMeta();
        metaData.setNoteBlockSound(null);
        item.setItemMeta(metaData);

        player.sendMessage(customPrefix + "Sound cleared from item!");
    }

    public void runGetHash(Player player) {
        String textureHash = getHashFromHeldItem(player);
        if (textureHash != null) {
            sendHashMessage(player, textureHash);
        }
    }

    public void runGetHash(CommandSender sender, Block block) {
        if (block.getType() != Material.PLAYER_HEAD) {
            sender.spigot().sendMessage(new ComponentBuilder(customPrefixFail + "Target block isn't a ")
                    .append(new TranslatableComponent(Material.PLAYER_HEAD.getItemTranslationKey())).build());
            return;
        }
        Skull skull = (Skull) block.getState();
        PlayerProfile owner = skull.getOwnerProfile();
        if (owner == null) {
            sender.sendMessage(customPrefixFail + "Coudn't find any texture hash!");
            return;
        }
        sendHashMessage(sender, EventListener.getTextureHash(owner));
    }

    private void sendHashMessage(CommandSender sender, String textureHash) {
        sender.spigot().sendMessage(new ComponentBuilder(customPrefix + "Hash: §r").append(textureHash)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text(new TranslatableComponent("chat.copy.click"))))
                .event(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, textureHash)).build());
    }

    public void runAdd(Player player, String keyString, String listName) {
        String hash = getHashFromHeldItem(player);
        if (hash == null) {
            return;
        }

        if (NamespacedKey.fromString(keyString) == null) {
            player.sendMessage(customPrefixFail + "Invalid NamespacedKey: " + keyString);
            return;
        }
        List<String> list = getConfig().getStringList(hash + "." + listName);
        if (list.contains(keyString)) {
            player.sendMessage(
                    customPrefixFail + "The key '" + keyString + "' is already in the " + listName + " list.");
            return;
        }
        list.add(keyString);
        getConfig().set(hash + "." + listName, list);
        saveConfig();
        runReload(player);
        player.sendMessage(
                customPrefix + "Added '" + keyString + "' to the " + listName + " list for hash '" + hash + "'");
    }

    public void runRemove(Player player, String keyString) {
        String hash = getHashFromHeldItem(player);
        if (hash == null) {
            return;
        }

        boolean changed = false;
        List<String> includeList = getConfig().getStringList(hash + ".include");
        if (includeList.remove(keyString)) {
            getConfig().set(hash + ".include", includeList);
            player.sendMessage(customPrefix + "Removed '" + keyString + "' from include list for hash '" + hash + "'");
            changed = true;
        }

        List<String> excludeList = getConfig().getStringList(hash + ".exclude");
        if (excludeList.remove(keyString)) {
            getConfig().set(hash + ".exclude", excludeList);
            player.sendMessage(customPrefix + "Removed '" + keyString + "' from exclude list for hash '" + hash + "'");
            changed = true;
        }

        if (changed) {
            saveConfig();
            runReload(player);
        } else {
            player.sendMessage(customPrefixFail + "The key '" + keyString + "' was not found for hash '" + hash + "'");
        }
    }

    public void runShow(Player player) {
        String hash = getHashFromHeldItem(player);
        if (hash == null) {
            return;
        }

        player.sendMessage(customPrefix + "Configuration for hash '" + hash + "'");
        List<String> includeList = getConfig().getStringList(hash + ".include");
        player.sendMessage("  §aInclude list:");
        if (includeList.isEmpty()) {
            player.sendMessage("    §7(empty)");
        } else {
            for (String key : includeList) {
                player.sendMessage("    §8- §r" + key);
            }
        }
        List<String> excludeList = getConfig().getStringList(hash + ".exclude");
        player.sendMessage("  §cExclude list:");
        if (excludeList.isEmpty()) {
            player.sendMessage("    §7(empty)");
        } else {
            for (String key : excludeList) {
                player.sendMessage("    §8- §r" + key);
            }
        }
    }

    public void runList(CommandSender sender, int page) {
        List<String> hashes = new ArrayList<>(getConfig().getKeys(false));
        Collections.sort(hashes);

        if (hashes.isEmpty()) {
            sender.sendMessage(customPrefix + "No hashes configured.");
            return;
        }

        int totalPages = (int) Math.ceil((double) hashes.size() / ITEMS_PER_PAGE);

        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        sender.sendMessage(customPrefix + "Available Hashes (Page " + page + "/" + totalPages + "):");

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, hashes.size());

        for (int i = startIndex; i < endIndex; i++) {
            String hash = hashes.get(i);
            String command = "/moreheadsounds config list " + hash;

            sender.spigot().sendMessage(new ComponentBuilder("  §8- §r" + hash.substring(0, 40) + "...")
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text("Click to get head with hash: " + hash)))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                    .create());
        }

        ComponentBuilder nav = new ComponentBuilder();
        if (page > 1) {
            nav.append("§a[<<< Previous]")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/moreheadsounds config list " + (page - 1)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Go to page " + (page - 1))));
        }
        if (page > 1 && page < totalPages) {
            nav.append(" ");
        }
        if (page < totalPages) {
            nav.append("§a[Next >>>]")
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/moreheadsounds config list " + (page + 1)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Go to page " + (page + 1))));
        }
        if (nav.getParts().size() > 0) {
            sender.spigot().sendMessage(nav.create());
        }
    }

    private void giveHead(Player player, String hash) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        PlayerProfile profile = Bukkit.createPlayerProfile(null, hash.substring(0, 16));
        PlayerTextures textures = profile.getTextures();
        try {
            textures.setSkin(URI.create("http://textures.minecraft.net/texture/" + hash).toURL());
        } catch (MalformedURLException e) {
            player.sendMessage(customPrefixFail + "Invalid hash format.");
            return;
        }
        profile.setTextures(textures);
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);

        player.getInventory().addItem(head);
        player.sendMessage(customPrefix + "You received a head.");
    }
}
