package de.thojo0.moreheadsounds;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TranslatableComponent;

public final class CommandUtils {
    public final static class Failed extends Exception {
    }

    private final CommandSender sender;
    private final Command command;

    public CommandUtils(CommandSender sender, Command command) {
        this.sender = sender;
        this.command = command;
    }

    public void fail() throws Failed {
        fail("commands.help.failed");
    }

    public void fail(String translate, Object... with) throws Failed {
        sender.spigot().sendMessage(new ComponentBuilder(App.customPrefixFail)
                .append(new TranslatableComponent(translate)).color(ChatColor.RED)
                .build());
        throw new Failed();
    }

    public void checkPermission(String permission) throws Failed {
        if (!sender.hasPermission(command.getName() + "." + permission)) {
            fail();
        }
    }

    public Player checkPlayer() throws Failed {
        if (!(sender instanceof Player)) {
            fail("permissions.requires.player");
        }
        return (Player) sender;
    }

    public void checkArgs(String[] args, int minArgs, String label) throws Failed {
        if (args.length < minArgs) {
            sender.sendMessage(App.customPrefixFail + "You need to type something after /" + label);
            throw new Failed();
        }
    }
}
