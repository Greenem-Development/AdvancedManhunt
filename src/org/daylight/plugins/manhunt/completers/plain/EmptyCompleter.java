package org.daylight.plugins.manhunt.completers.plain;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class EmptyCompleter implements TabCompleter {
	private int amountOfArgs;
	
	public EmptyCompleter() {
		this(0);
	}
	
	public EmptyCompleter(int amountOfArgs) {
		super();
		this.amountOfArgs = amountOfArgs;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(args.length == amountOfArgs + 1) return new ArrayList<>();
		return null;
	}
	
}
