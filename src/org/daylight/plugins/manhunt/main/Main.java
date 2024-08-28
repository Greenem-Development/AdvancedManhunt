package org.daylight.plugins.manhunt.main;

import static org.daylight.plugins.manhunt.data.References.configsDirectory;
import static org.daylight.plugins.manhunt.data.References.pluginConfigDirectory;
import static org.daylight.plugins.manhunt.data.References.serverDirectory;
import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;
import static org.daylight.plugins.manhunt.util.Stash.isSenderAPlayer;
import static org.daylight.plugins.manhunt.util.Stash.isSenderAPlayerOrConsole;

import java.io.File;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.daylight.plugins.manhunt.commands.ManhuntCommands;
import org.daylight.plugins.manhunt.completers.MhProblemsCompleter;
import org.daylight.plugins.manhunt.completers.MhTeamCompleter;
import org.daylight.plugins.manhunt.completers.MhTeamToolsCompleter;
import org.daylight.plugins.manhunt.completers.plain.EmptyCompleterNoArgs;
import org.daylight.plugins.manhunt.config.ConfigOperations;
import org.daylight.plugins.manhunt.config.ConfigWatcher;
import org.daylight.plugins.manhunt.coreDependent.AuthorsAdvancedVer;
import org.daylight.plugins.manhunt.coreDependent.AuthorsBaiscVer;
import org.daylight.plugins.manhunt.data.References;
import org.daylight.plugins.manhunt.events.FrozenEvents;
import org.daylight.plugins.manhunt.events.ManhuntEvents;
import org.daylight.plugins.manhunt.teams.TeamManager;
import org.daylight.plugins.manhunt.util.OsDependent;
import org.daylight.plugins.manhunt.util.SimpleLogger;
import org.daylight.plugins.manhunt.util.projectspecific.DebugModeSwitch;

import de.tr7zw.changeme.nbtapi.NBT;

public class Main extends JavaPlugin implements Listener {
	public static Plugin plugin;

	private static Main instance;
	public static Main getInstance() { // not synchronized, but that's ok // what???
		return instance;
	}
	
	@Override
	public void onEnable() {
		if(!preloadNbtApi()) return;
		initEverything();
		finishInitialization();
		instance = this;
	}
	
	@Override
	public void onDisable() {
		log("Advanced Manhunt plugin has been disabled");
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player p = null;
//		String name = sender.getName();
		try {
			p = (Player) sender;
		} catch (ClassCastException e) {}
		
		if (cmd.getName().equalsIgnoreCase("mhw-about")) {
			try {
				AuthorsAdvancedVer.authors(sender);
			} catch (NoClassDefFoundError e) {
				AuthorsBaiscVer.authors(sender);
			}
		}
//		else if (cmd.getName().equalsIgnoreCase("test")) {
//			if(!isSenderAPlayer(sender)) return true;
//			ManhuntCommands.testCommand(p);
//		}
		else if (cmd.getName().equalsIgnoreCase("mhteam")) {
			if(!isSenderAPlayerOrConsole(sender)) return true;
			ManhuntCommands.mhTeamCommand(sender, args);
		}
		else if (cmd.getName().equalsIgnoreCase("mhteams")) {
			if(!isSenderAPlayerOrConsole(sender)) return true;
			ManhuntCommands.mhTeamToolsCommand(sender, args);
		}
		else if(cmd.getName().equalsIgnoreCase("mhissues")) {
			if(!isSenderAPlayer(sender)) return true;
			ManhuntCommands.problemsCommand(p, args);
		}
		else if(cmd.getName().equalsIgnoreCase("mhgame")) {
			if(!isSenderAPlayerOrConsole(sender)) return true;
			ManhuntCommands.mhGameCommand(sender, args);
		}
		return true; //false???
	}
	
	private void initEverything() {
		// Main info
		plugin = this;
		References.os = OsDependent.getSystemType();
		
		// Files
		initFiles();
		DebugModeSwitch.init();
		
		// Config
		ConfigOperations.updateAndLoadConfig();
		startConfigWatcher();
		
		// Bukkit API
		registerListeners();
		registerCommands();
		
		// Plugin Functionality
		TeamManager.resetTeams();
	}
	
	private void registerListeners() {
		Bukkit.getPluginManager().registerEvents(this, this);
		Bukkit.getPluginManager().registerEvents(new FrozenEvents(), this);
		Bukkit.getPluginManager().registerEvents(new ManhuntEvents(), this);
	}
	
	private void registerCommands() {
//		getCommand("mhgame").setTabCompleter(new MhStartCompleter());
		getCommand("mhw-about").setTabCompleter(new EmptyCompleterNoArgs());
		getCommand("mhteam").setTabCompleter(new MhTeamCompleter());
		getCommand("mhteams").setTabCompleter(new MhTeamToolsCompleter());
		getCommand("mhissues").setTabCompleter(new MhProblemsCompleter());
	}
	
	private void initFiles() {
		serverDirectory = Bukkit.getWorldContainer();
		configsDirectory = new File(serverDirectory, "plugins/configs");
		pluginConfigDirectory = new File(configsDirectory, "AdvancedManhunt");
		pluginConfigDirectory.mkdirs();
	}
	
	private void startConfigWatcher() {
		References.configWatcher = new ConfigWatcher();
		References.configWatcher.start();
	}
	
	private void finishInitialization() {
		log(ChatColor.GREEN + "Advanced Manhunt" + ChatColor.RESET + " plugin has been enabled!");
		if(SimpleLogger.debugOutputEnabled) debug(ChatColor.AQUA + "Debug mode is enabled");
	}
	
	private boolean preloadNbtApi() {
		if (!NBT.preloadApi()) {
	        getLogger().warning("NBT-API wasn't initialized properly, disabling the plugin");
	        getPluginLoader().disablePlugin(this);
	        return false;
	    }
		return true;
	}
	
	// TODO learn to send action bars
}