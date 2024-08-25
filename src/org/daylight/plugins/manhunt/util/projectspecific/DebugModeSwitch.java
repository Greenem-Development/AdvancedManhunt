package org.daylight.plugins.manhunt.util.projectspecific;

import java.io.File;

import org.daylight.plugins.manhunt.commands.ExtraCommands;
import org.daylight.plugins.manhunt.data.References;
import org.daylight.plugins.manhunt.util.SimpleLogger;

public class DebugModeSwitch {
	public static void init() {
		File f = new File(References.pluginConfigDirectory, "enableDebug.dev");
		if(!f.exists()) {
			SimpleLogger.debugOutputEnabled = false;
			ExtraCommands.logCommandInfo = false;
		}
	}
}
