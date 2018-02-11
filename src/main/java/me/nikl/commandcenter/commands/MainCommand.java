package me.nikl.commandcenter.commands;

import me.nikl.commandcenter.CommandCenter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by nikl on 09.02.18.
 */
public class MainCommand implements CommandExecutor {
    private CommandCenter commandCenter;

    public MainCommand(CommandCenter commandCenter){
        this.commandCenter = commandCenter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("commandcenter.admin")){
            sender.sendMessage(color("[&6CommandCenter&r] You have no permission!"));
            return true;
        }

        if(args.length == 0){
            sendHelpMessage(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")){
            commandCenter.reload();
            return true;
        }
        sendHelpMessage(sender);
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(color("[&6CommandCenter&r] Do &1/commandcenter reload &rto reload the plugin configuration."));
    }

    private String color(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
