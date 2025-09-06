package de.thojo0.moreheadsounds;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;

import de.thojo0.moreheadsounds.CommandUtils.Failed;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class App extends JavaPlugin {
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

                        case "add":
                            break;

                        case "remove":
                            break;

                        case "show":
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
                    Player player = utils.checkPlayer();
                    if (args.length == 1) {
                        runGetHash(player);
                        break;
                    }
                    if (args.length < 4) {
                        utils.fail("argument.pos3d.incomplete");
                    }
                    try {
                        runGetHash(sender, player.getWorld().getBlockAt(Integer.parseInt(args[1]),
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
                        autoComplete.add("add");
                        autoComplete.add("remove");
                        autoComplete.add("show");
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