package org.daylight.plugins.manhunt.commands;

import static org.daylight.plugins.manhunt.completers.MhTeamCompleter.check;
import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;
import static org.daylight.plugins.manhunt.util.Stash.isSenderAPlayerOrConsole;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.daylight.plugins.manhunt.compass.CompassUtils;
import org.daylight.plugins.manhunt.completers.MhTeamCompleter;
import org.daylight.plugins.manhunt.completers.MhTeamToolsCompleter;
import org.daylight.plugins.manhunt.completers.MhTeamCompleter.MhTeamArgs;
import org.daylight.plugins.manhunt.config.ConfigOperations;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;
import org.daylight.plugins.manhunt.util.StandartUtils;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages;

public class ManhuntCommands {
	public static void problemsCommand(Player p, String[] args) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(p.getName());
		if(ownTeam == null) {
			p.sendMessage(ChatColor.RED + "You are not in a team!");
			return;
		}
		if(!ownTeam.canUseTracking()) {
			p.sendMessage(ChatColor.RED + "You can't use the compass!");
			return;
		}
		if(args.length==1) {
			if(args[0].equals("get_compass")) {
				if(p.getInventory().contains(Material.COMPASS)) {
					p.sendMessage(ChatColor.RED + "You already have a compass!");
					return;
				}
				CompassUtils.giveCompass(p);
				p.sendMessage(ChatColor.GREEN + "You've been given a compass");
			}
		}
		else {
			p.sendMessage(ChatColor.RED + "Wrong arguments");
		}
	}
	
	public static void mhTeamCommand(CommandSender sender, String[] args) {
		MhTeamArgs specificArgs = new MhTeamCompleter.MhTeamArgs(args);
//		log(args.length + " | 1:" + specificArgs.selfOrSomeone + " 2:" + specificArgs.typeOfSomeone + " 3:" + specificArgs.targetName + " 4:" + specificArgs.actionType + " 5:" + specificArgs.teamName);
		boolean valid = specificArgs.actionType != null && ((specificArgs.actionType.equals("join") && (specificArgs.teamName != null || specificArgs.teamName.trim().isEmpty())) || specificArgs.actionType.equals("leave")) && (check(specificArgs.selfOrSomeone, "self") || (check(specificArgs.selfOrSomeone, "someone") && specificArgs.typeOfSomeone != null));
		if(!valid) {
			sender.sendMessage("Incorrect command usage!");
			return;
		}
		
		boolean toAdd = specificArgs.actionType.equals("join");
		List<String> playerNames = null;
		if(check(specificArgs.selfOrSomeone, "self") && sender instanceof Player) {
			playerNames = new ArrayList<>();
			playerNames.add(sender.getName());
		} else if(check(specificArgs.typeOfSomeone, "everyone")) {
			playerNames = TeamManager.getAllPlayers();
		} else if(check(specificArgs.typeOfSomeone, "remaining")) {
			playerNames = TeamManager.getRemainingPlayers(toAdd);
		} else if(!(sender instanceof Player)) {
			sender.sendMessage("You can't apply this action on such target!");
			return;
		}
		
		if(playerNames != null) {
			if(playerNames.size() == 0) {
				sender.sendMessage(FormattedMessages.noTargetsAvaliableMessage());
				return;
			}
			for(String p : playerNames) {
				addOrRemovePlayerFromTeam(sender, p, specificArgs.teamName, toAdd);
			}
		}
	}
	
	private static void addOrRemovePlayerFromTeam(CommandSender sender, String nickname, String teamName, boolean toAdd) {
		Player p = Bukkit.getPlayerExact(nickname);
		if(p == null) {
			sender.sendMessage(FormattedMessages.cantFindPlayerMessage(nickname));
			return;
		}
		
		if(!toAdd || teamName != null && TeamManager.teamExists(teamName)) {
			if(toAdd) {
				int result = TeamManager.addToTeamAndRemFromOtherAndNotify(nickname, teamName, sender);
				if(result == -1) {
					if(sender == p) sender.sendMessage(FormattedMessages.youAreAlreadyTeamMessage());
					else sender.sendMessage(FormattedMessages.someoneIsAlreadyTeamMessage(p.getName()));
				}
			} else {
				TeamManager.removeFromTeamAndNotify(nickname, sender);
			}
		}
	}
	
	public static void mhTeamToolsCommand(CommandSender sender, String[] args) {
		MhTeamToolsCompleter.processMhToolsArgs(sender, String.join(" ", args), false);
	}
	
	public static void mhGameCommand(CommandSender sender, String[] args) {
		if(args.length<1) {
			sender.sendMessage(ChatColor.RED + "Insert the delay in seconds. /mhstart <seconds>");
			return;
		}
		if(args.length==1) {
			if(StandartUtils.isInteger(args[0])) {
//				int totalPlayers = Speedrunners.size()+Hunters.size();
//				if(totalPlayers==0) {
//					sender.sendMessage(ChatColor.RED + "You didn't add a single hunter or speedrunner!");
//					return;
//				}
//				if(Speedrunners.size()==0) {
//					sender.sendMessage(ChatColor.RED + "You didn't add a single speedrunner!");
//					return;
//				}
//				if(Hunters.size()==0) {
//					sender.sendMessage(ChatColor.YELLOW + "You are starting the game against NO HUNTERS"); //. If that's a mistake, do /mhstop // add later
//				}
				ManhuntCommandActions.mhStartGame(Integer.parseInt(args[0]));
			}
			else {
				sender.sendMessage(ChatColor.RED + "Use a number as the argument");
				return;
			}
		}
		if(args.length>1) {
			sender.sendMessage(ChatColor.RED + "Incorrect arguments. /mhstart <seconds>");
			return;
		}
	}
}
