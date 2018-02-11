package me.nikl.commandcenter;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;

/**
 * Created by nikl on 04.02.18.
 */
public class CommandCenter extends JavaPlugin {
    public static final boolean DEBUG = true;
    private FileConfiguration configuration;
    private OverrideCommandMap overrideCommandMap;

    @Override
    public void onEnable(){
        if (!reload()) {
            getLogger().severe(" Problem while loading CommandCenter! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // clear the commands after all plugins were enabled to get rid of NPEs from other plugins
        new BukkitRunnable() {
            @Override
            public void run() {
                clearCommands();
            }
        }.runTask(this);
    }

    public boolean reload() {
        if(!reloadConfiguration()){
            getLogger().severe(" Failed to load config file!");
            return false;
        }
        if(overrideCommandMap != null) overrideCommandMap.reload();
        return true;
    }

    private boolean reloadConfiguration() {
        File con = new File(this.getDataFolder().toString() + File.separatorChar + "config.yml");
        if(!con.exists()){
            this.saveResource("config.yml", false);
        }
        try {
            this.configuration = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(con), "UTF-8"));
        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onDisable(){

    }

    private void clearCommands() {
        Field commandMap;
        try {
            commandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            Field pluginManager = Bukkit.getServer().getClass().getDeclaredField("pluginManager");
            Field modifiersField = Field.class.getDeclaredField("modifiers");

            modifiersField.setAccessible(true);
            modifiersField.setInt(commandMap, commandMap.getModifiers() & ~Modifier.FINAL);
            modifiersField.setInt(pluginManager, pluginManager.getModifiers() & ~Modifier.FINAL);
            commandMap.setAccessible(true);
            pluginManager.setAccessible(true);

            Collection<Command> knownCommands = ((SimpleCommandMap)commandMap.get(Bukkit.getServer())).getCommands();
            overrideCommandMap = new OverrideCommandMap(Bukkit.getServer());
            for(Command command : knownCommands){
                overrideCommandMap.register("", command);
            }
            commandMap.set(Bukkit.getServer(), overrideCommandMap);

            SimplePluginManager simpleManager = (SimplePluginManager)pluginManager.get(Bukkit.getServer());
            Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
            f.setAccessible(true);
            f.set(simpleManager, overrideCommandMap);
            pluginManager.set(Bukkit.getServer(), simpleManager);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig(){
        return this.configuration;
    }

    OverrideCommandMap getOverrideCommandMap(){
        return this.overrideCommandMap;
    }

    public void debug(String message){
        if(DEBUG) getLogger().info(message);
    }
}
