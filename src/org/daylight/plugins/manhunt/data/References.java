package org.daylight.plugins.manhunt.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.daylight.plugins.manhunt.config.ConfigWatcher;
import org.daylight.plugins.manhunt.util.OsDependent.OsType;

public class References {
	public static ConfigWatcher configWatcher;
	
	public static File serverDirectory;
	public static File configsDirectory;
	public static File pluginConfigDirectory;
	
	public static String runFileString = "asdgsdfgsd";
	public static String javaString = "java"; //"\"C:\\Program Files\\Java\\jdk-17.0.1\\bin\\java\"";
	public static OsType os;
//	public static String launchServerFileName;
}
