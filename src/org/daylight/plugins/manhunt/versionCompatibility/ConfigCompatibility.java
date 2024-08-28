package org.daylight.plugins.manhunt.versionCompatibility;

import static org.daylight.plugins.manhunt.data.References.runFileString;
import static org.daylight.plugins.manhunt.data.References.serverDirectory;
import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.daylight.plugins.manhunt.config.ConfigOperations;
import org.daylight.plugins.manhunt.data.Lines;
import org.daylight.plugins.manhunt.data.References;

public class ConfigCompatibility {
	public static void updateConfig() {
		File f = getConfigFile();
		f.getParentFile().mkdirs();
		YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
		YamlConfiguration cOld = YamlConfiguration.loadConfiguration(f);
		
//		updateConfigFrom1_3To1_4(c);
		
		ConfigOperations.saveConfigIfHasChanges(c, cOld, f);
	}
	
	/**
	 * Example
	 */
	private static void updateConfigFrom1_3To1_4(YamlConfiguration c) {
		String newPrefixString = "settings.";
		String currentKey;
		
		currentKey = "launch-server-script-name";
		moveValue(c, currentKey, newPrefixString + "world-resetting." + currentKey);
		currentKey = "allow-gm-command-for-ops";
		moveValue(c, currentKey, newPrefixString + ".commands." + currentKey);
		currentKey = "allow-heal-command-for-ops";
		moveValue(c, currentKey, newPrefixString + ".commands." + currentKey);
	}
	
	private static File getConfigFile() {
		return new File(References.pluginConfigDirectory, Lines.mainConfigName);
	}
	
	private static void moveValue(YamlConfiguration c, String oldKey, String newKey) {
		if(c.get(newKey)==null && c.get(oldKey)!=null) {
			Object currentValue = null;
			try {
				currentValue = c.get(oldKey);
			} catch (ClassCastException e) {
				
			}
//			if (currentValue!=null) {
				c.set(newKey, currentValue);
//			} else {
//				c.set(newKey, ConfigOperations.getDefaultValue(newKey));
//			}
		}
		c.set(oldKey, null);
	}
}
