package org.daylight.plugins.manhunt.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MhCompassCompleter implements TabCompleter {
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		if(cmd.getName().equalsIgnoreCase("mhcompass")) {
			if(args.length==1) {
				arguments.add("get_usual");
				arguments.add("own_team");
				return arguments;
			}
			return arguments;
		}
		return null;
	}
}
