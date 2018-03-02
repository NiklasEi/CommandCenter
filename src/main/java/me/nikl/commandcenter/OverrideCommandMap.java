package me.nikl.commandcenter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class OverrideCommandMap extends SimpleCommandMap{
    private CommandCenter commandCenter;
    private List<String> whiteListCommands;
    private List<String> blackListCommands;
    private boolean removeAllCommandsExceptWhiteList;
    private boolean tabCompleteEnabled;

    public OverrideCommandMap(Server server){
        super(server);
    }

    @Override
    public boolean register(String fallbackPrefix, Command command){
        return register(command.getName(), fallbackPrefix, command);
    }

    private void setUp() {
        commandCenter = (CommandCenter) Bukkit.getPluginManager().getPlugin("CommandCenter");
        whiteListCommands = commandCenter.getConfig().getStringList("whiteList");
        if (whiteListCommands == null) whiteListCommands = new ArrayList<>();
        whiteListCommands.addAll(Arrays.asList("stop"));
        blackListCommands = commandCenter.getConfig().getStringList("blackList");
        if (blackListCommands == null) blackListCommands = new ArrayList<>();
        removeAllCommandsExceptWhiteList = commandCenter.getConfig().getBoolean("removeAllCommandsExceptWhiteList", true);
        tabCompleteEnabled = commandCenter.getConfig().getBoolean("tabComplete.enable", true);
    }

    @Override
    public boolean register(String cmdName, String fallbackPrefix, Command command) {
        if(!allowRegistration(cmdName)){
            commandCenter.debug(" deny registration of: " + command.getLabel() + "   name: " + command.getName());
            return false;
        }
        //commandCenter.debug(" -> register: " + command.getName());
        command = manipulateCommandPreRegistration(command);
        return super.register(cmdName, fallbackPrefix, command);
    }

    private Command manipulateCommandPreRegistration(Command command) {
        if(command instanceof PluginCommand){
            //commandCenter.debug(" in plugin command");
            command.setPermissionMessage(" Testing custom perm message");
        } else {
            //commandCenter.debug(command.getName() + " is of class " + command.getClass().getSimpleName());
        }
        return command;
    }

    private boolean allowRegistration(String cmdName) {
        if(whiteListCommands == null) setUp();
        if(removeAllCommandsExceptWhiteList){
            return whiteListCommands.contains(cmdName);
        } else {
            return !blackListCommands.contains(cmdName);
        }
    }

    @Override
    public boolean dispatch(CommandSender sender, String commandLine) throws CommandException{
        commandCenter.debug(" dispatching: " + commandLine);
        return super.dispatch(sender, commandLine);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine, Location location) {
        commandCenter.debug(" tab completing " + cmdLine);
        return super.tabComplete(sender, cmdLine, location);
    }

    public void reload() {
        setUp();
        HashSet<Command> knownCommands = new HashSet<>(getCommands());
        clearCommands();
        for (Command command : knownCommands){
            register("", command);
        }
    }
}