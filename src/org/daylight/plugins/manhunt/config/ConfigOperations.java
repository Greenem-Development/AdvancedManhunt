package org.daylight.plugins.manhunt.config;

import static org.daylight.plugins.manhunt.data.References.pluginConfigDirectory;
import static org.daylight.plugins.manhunt.data.References.serverDirectory;
import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.daylight.plugins.manhunt.data.Lines;
import org.daylight.plugins.manhunt.data.References;
import org.daylight.plugins.manhunt.versionCompatibility.ConfigCompatibility;

public class ConfigOperations {
	public static void updateAndLoadConfig() {
		ConfigCompatibility.updateConfig();
		loadConfig();
	}
	
	public static void loadConfig() {
		File f = new File(pluginConfigDirectory, Lines.mainConfigName);
		@SuppressWarnings("unused")
		boolean fileExisted = f.exists(); // could improve
		
		f.getParentFile().mkdirs();
		YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
		YamlConfiguration cOld = YamlConfiguration.loadConfiguration(f);
		
		String currentVersion = "1.7";
		String configVersion = null;
		
		ConfigData.speedrunnerDelayMilliseconds = (int) loadKey(c, "settings.game.delay-for-speedrunner-on-start-in-milliseconds",
				ConfigData.speedrunnerDelayMilliseconds);
		ConfigData.clearItemsOnStart = (boolean) loadKey(c, "settings.game.on-start.clear-items",
				ConfigData.clearItemsOnStart);
		ConfigData.healOnStart = (boolean) loadKey(c, "settings.game.on-start.heal-players",
				ConfigData.healOnStart);
		
		configVersion = (String) loadKey(c, "internal.config-version", null);
		
		if(configVersion != null) {
//			if(configVersion.equals("1.7")) updateConfig_1_7_to_1_8(c); // in the future
		} else {
			loadKey(c, "internal.config-version", currentVersion);
		}
		
		saveConfigIfHasChanges(c, cOld, f);
	}
	
	/*public static Object getDefaultValue(String key) {
		switch (key) {
			case "settings.world-resetting.launch-server-script-name": return "Run.bat";
			case "settings.commands.allow-gm-command-for-ops": return true;
			case "settings.commands.allow-heal-command-for-ops": return true;
			case "settings.world-resetting.require-confirmation-for-world-resetting": return false;
			case "settings.world-resetting.use-halt-for-instant-server-stopping": return false;
			case "settings.game.delay-for-speedrunner-on-start-in-milliseconds": return 2000;
			case "settings.warnings.disable-no-existing-start-file-warning": return false;
			case "settings.game.clear-items-on-start": return true;
			case "settings.world-resetting.allow-ops-use-world-resetting": return true;
			default:
				return "UNDEFINED_CONFIG_VALUE";
		}
	}*/
	
	private static Object loadKey(YamlConfiguration c, String currentKey, Object defaultValue) {
		return loadKey(c, currentKey, defaultValue, null, null);
	}
	
	private static Object loadKey(YamlConfiguration c, String currentKey, Object defaultValue, Runnable ifExists, Runnable ifNotExists) {
		try {
			Object currentValue = c.get(currentKey);
//			Object def = null;
			Object returnValue = null;
			if (currentValue == null) {
//				def = getDefaultValue(currentKey);
				c.set(currentKey, defaultValue);
				returnValue = defaultValue;
				if(ifExists!=null) {
					ifExists.run();
				}
			} else {
				try {
					returnValue = currentValue;
					if(ifNotExists!=null) {
						ifNotExists.run();
					}
				} catch (ClassCastException e) {
					logCastEx(currentKey);
				}
			}
//			if(currentKey=="settings.world-resetting.launch-server-script-name") {
//				checkStartScript((String) returnValue);
//			}
			return returnValue;
		} catch (Throwable t) {
			log("Error while reading \"" + currentKey + "\" from the config");
			return defaultValue;
		}
	}
	
	/***
	 * Returnes true if found changes
	 * **/
	public static boolean saveConfigIfHasChanges(YamlConfiguration newConfig, YamlConfiguration oldConfig, File f) {
		boolean hasChanges = false;
	    for(String key : newConfig.getKeys(true)) { // missing keys added by user
	        if(!oldConfig.contains(key)) {
	        	hasChanges = true;
	        }
	    }
	    for(String key : oldConfig.getKeys(true)) {
	        if(!newConfig.contains(key)) {
	        	hasChanges = true;
	        }
	    }
	    
		if (hasChanges) {
			try {
				newConfig.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return hasChanges;
	}
	
	/*private static void checkStartScript(String runFileString) {
		if(ConfigData.disableNoExistingStartFileWarning) return;
		if(!new File(serverDirectory, runFileString).exists()) {
			String noExistingStartFileWarning = ChatColor.RED + "You haven't specified your run script file name in the config! "
					+ "(for world reseting, but other things are fine)";
			log(noExistingStartFileWarning);
		}
	}*/
	
	public static void logCastEx(String currentKey) {
		log("Wrong value type for \"" + currentKey + "\" in the config");
	}
}
