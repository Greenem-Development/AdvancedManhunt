package org.daylight.plugins.manhunt.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class MhProblemsCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		//sender.sendMessage(commandLabel);
		if(cmd.getName().equalsIgnoreCase("mhissues")) {
			if(args.length==1) {
				arguments.add("get_compass");
				return arguments;
			}
			if(args.length==2) {
				return null;
			}
			return arguments;
		}
		return null;
	}
	
}
