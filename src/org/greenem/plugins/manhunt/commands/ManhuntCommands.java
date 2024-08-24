package org.greenem.plugins.manhunt.commands;

import static org.greenem.plugins.manhunt.data.References.Hunters;
import static org.greenem.plugins.manhunt.data.References.HuntersPortalFrom;
import static org.greenem.plugins.manhunt.data.References.HuntersPortalTo;
import static org.greenem.plugins.manhunt.data.References.PortalEndTo;
import static org.greenem.plugins.manhunt.data.References.PortalFrom;
import static org.greenem.plugins.manhunt.data.References.PortalTo;
import static org.greenem.plugins.manhunt.data.References.Speedrunners;
import static org.greenem.plugins.manhunt.util.Stash.isSenderAPlayerOrConsole;
import static org.greenem.plugins.manhunt.completers.MhTeamCompleter.check;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import static org.greenem.plugins.manhunt.util.SimpleLogger.log;
import static org.greenem.plugins.manhunt.util.SimpleLogger.debug;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.util.Vector;
import org.greenem.plugins.manhunt.compass.CompassUpdating;
import org.greenem.plugins.manhunt.compass.CompassUtils;
import org.greenem.plugins.manhunt.compass.TeamsManager;
import org.greenem.plugins.manhunt.completers.MhTeamCompleter;
import org.greenem.plugins.manhunt.completers.MhTeamCompleter.MhTeamArgs;
import org.greenem.plugins.manhunt.completers.MhTeamToolsCompleter;
import org.greenem.plugins.manhunt.config.ConfigData;
import org.greenem.plugins.manhunt.config.ConfigOperations;
import org.greenem.plugins.manhunt.models.ManhuntPlayer;
import org.greenem.plugins.manhunt.models.ManhuntTeam;
import org.greenem.plugins.manhunt.util.FormattedMessages;
import org.greenem.plugins.manhunt.util.StandartUtils;
import org.greenem.plugins.manhunt.util.Stash;
import org.greenem.plugins.manhunt.worldManagement.WorldResetting;

import net.md_5.bungee.api.chat.TextComponent;

public class ManhuntCommands {
//	private static String messageNoPlayerSpecified = ChatColor.RED + "You should specify the player to apply the result to";
	private static String msgWrongUsage = ChatColor.RED + "Wrong command usage";
	private static String msgSpecifyPlayer = ChatColor.RED + "You should specify the player to apply the action to!";
	private static String msgShouldntArgs = ChatColor.RED + "You shouldn't put any extra arguments there!";
	private static String msgCantSelfConsole = ChatColor.RED + "You can't apply this to yourself as the console!";
	
	public static void hunterCommand(CommandSender sender, String[] args) {
		AbstractCommand ac = new AbstractCommand();
		ac.sender = sender;
		ac.type = CommandActionType.MAKE_HUNTER;
		if(args.length==2) {
			ac.targetName = args[1];
			if(args[0].equals("add")) ac.additionalType = AdditionalCommandActionType.OTHER_PLAYER;
			else if(args[0].equals("remove")) {
				ac.additionalType = AdditionalCommandActionType.OTHER_PLAYER;
				ac.type = CommandActionType.MAKE_NOROLE;
			} else if(args[0].equals("remove-all") || args[0].equals("add-remaining")) {
				sender.sendMessage(msgShouldntArgs);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Unknown argument \"" + args[0] +"\"");
				return;
			}
		} else if(args.length==1) {
			if(args[0].equals("add-remaining")) ac.additionalType = AdditionalCommandActionType.REMAINING_PLAYERS;
			else if(args[0].equals("remove-all")) {
				ac.additionalType = AdditionalCommandActionType.ALL_PLAYERS;
				ac.removeFrom = "h";
				ac.type = CommandActionType.MAKE_NOROLE;
			} else if(args[0].equals("remove") || args[0].equals("add")) {
				sender.sendMessage(msgSpecifyPlayer);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Unknown argument \"" + args[0] +"\"");
				//log(ChatColor.RED + "Unknown command structure, bug");
				return;
			}
		} else if(args.length==0) {
			if(sender instanceof ConsoleCommandSender) {
				sender.sendMessage(msgCantSelfConsole);
				return;
			}
			ac.additionalType = AdditionalCommandActionType.SELF;
		} else {
			sender.sendMessage(msgWrongUsage);
			return;
		}
		
		ac.completeInitialisation();
		UniversalCommands.universalCommand(ac, null);
		
//		if(true) return;
//		
//		Player p = (Player) sender;
//		String pName = p.getName();
//		if(Speedrunners.contains(pName)) {
////			p.sendMessage(ChatColor.RED + "You are a speedrunner. Use /norole first");
////			return true;
//			ManhuntCommandActions.doNoRole(p, pName);
//		}
//		if(!p.getWorld().getEnvironment().equals(Environment.NORMAL)) {
//			p.sendMessage(ChatColor.RED + "You must be in the overworld to use this command. (There would be problems with missing portal location. Later this will be avaliable)");
//			return;
//		}
//		if(!Hunters.contains(pName)) {
//			Hunters.add(pName);
//			p.getInventory().remove(Material.COMPASS);
//			Stash.AddItemStack(p, new ItemStack(Material.COMPASS), 8);
//			CompassUpdating.findTarget();
//			p.sendMessage(ChatColor.WHITE + "Now you are a " + ChatColor.RESET + "" + ChatColor.RED + "" + ChatColor.BOLD + "hunter");
//		}
//		else {
//			p.sendMessage(ChatColor.RED + "You are already a hunter");
//		}
	}
	
	public static void speedrunnerCommand(CommandSender sender, String[] args) {
		AbstractCommand ac = new AbstractCommand();
		ac.sender = sender;
		ac.type = CommandActionType.MAKE_SPEEDRUNNER;
		if(args.length==2) {
			ac.targetName = args[1];
			if(args[0].equals("add")) ac.additionalType = AdditionalCommandActionType.OTHER_PLAYER;
			else if(args[0].equals("remove")) {
				ac.additionalType = AdditionalCommandActionType.OTHER_PLAYER;
				ac.type = CommandActionType.MAKE_NOROLE;
			} else if(args[0].equals("remove-all") || args[0].equals("add-remaining")) {
				sender.sendMessage(msgShouldntArgs);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Unknown argument \"" + args[0] +"\"");
				return;
			}
		} else if(args.length==1) {
			if(args[0].equals("add-remaining")) ac.additionalType = AdditionalCommandActionType.REMAINING_PLAYERS;
			else if(args[0].equals("remove-all")) {
				ac.additionalType = AdditionalCommandActionType.ALL_PLAYERS;
				ac.removeFrom = "s";
				ac.type = CommandActionType.MAKE_NOROLE;
			} else if(args[0].equals("remove") || args[0].equals("add")) {
				sender.sendMessage(msgSpecifyPlayer);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Unknown argument \"" + args[0] +"\"");
				//log(ChatColor.RED + "Unknown command structure, bug");
				return;
			}
		} else if(args.length==0) {
			if(sender instanceof ConsoleCommandSender) {
				sender.sendMessage(msgCantSelfConsole);
				return;
			}
			ac.additionalType = AdditionalCommandActionType.SELF;
		} else {
			sender.sendMessage(msgWrongUsage);
			return;
		}
		
		ac.completeInitialisation();
		UniversalCommands.universalCommand(ac, null);
		
//		if(true) return;
//		
//		Player p = (Player) sender;
//		String pName = p.getName();
//		if(Hunters.contains(pName)) {
////			p.sendMessage(ChatColor.RED + "You are a hunter. Use /norole");
////			return true;
//			ManhuntCommandActions.doNoRole(p, pName);
//		}
//		if(!p.getWorld().getEnvironment().equals(Environment.NORMAL)) {
//			p.sendMessage(ChatColor.RED + "You must be in the overworld to use this command. (There would be problems with missing portal location. Later this will be avaliable)");
//			return;
//		}
//		if(!Speedrunners.contains(pName)) {
//			Speedrunners.add(pName);
//			p.getInventory().remove(Material.COMPASS);
//			p.sendMessage(ChatColor.WHITE + "Now you are a " + ChatColor.RESET + "" + ChatColor.GREEN + "" + ChatColor.BOLD + "speedrunner");
//		}
//		else {
//			p.sendMessage(ChatColor.RED + "You are already a speedrunner");
//		}
	}
	
	@SuppressWarnings("unused")
	public static void noRoleCommand(CommandSender sender, String[] args) {
		AbstractCommand ac = new AbstractCommand();
		ac.sender = sender;
		ac.type = CommandActionType.MAKE_NOROLE;
		if(args.length==2) {
			ac.targetName = args[1];
			if(args[0].equals("apply")) ac.additionalType = AdditionalCommandActionType.OTHER_PLAYER;
			else if(args[0].equals("apply-to-remaining")) {
				sender.sendMessage(msgShouldntArgs);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Unknown argument \"" + args[0] +"\"");
				return;
			}
		} else if(args.length==1) {
			if(args[0].equals("apply-to-remaining")) {
				ac.additionalType = AdditionalCommandActionType.REMAINING_PLAYERS;
				ac.removeFrom = "";
			} else if(args[0].equals("apply")) {
				sender.sendMessage(msgSpecifyPlayer);
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "Unknown argument \"" + args[0] +"\"");
				//log(ChatColor.RED + "Unknown command structure, bug");
//				else log(ChatColor.RED + "Unknown command structure, bug");
				return;
			}
		} else if(args.length==0) {
			ac.additionalType = AdditionalCommandActionType.SELF;
			if(sender instanceof ConsoleCommandSender) {
				sender.sendMessage(msgCantSelfConsole);
				return;
			}
		} else {
			sender.sendMessage(msgWrongUsage);
			return;
		}
		ac.completeInitialisation();
		
		UniversalCommands.universalCommand(ac, null);
		
		if(true) return;
		
		Player p = (Player) sender;
		String pName = p.getName();
		ManhuntCommandActions.doNoRole(p, pName);
		p.sendMessage(ChatColor.GOLD + "You have removed your role");
	}
	
	public static void startGameCommand(CommandSender sender, String[] args) {
		if(args.length<1) {
			sender.sendMessage(ChatColor.RED + "Insert the delay in seconds. /mhstart <seconds>");
			return;
		}
		if(args.length==1) {
			if(StandartUtils.isInteger(args[0])) {
				int totalPlayers = Speedrunners.size()+Hunters.size();
				if(totalPlayers==0) {
					sender.sendMessage(ChatColor.RED + "You didn't add a single hunter or speedrunner!");
					return;
				}
				if(Speedrunners.size()==0) {
					sender.sendMessage(ChatColor.RED + "You didn't add a single speedrunner!");
					return;
				}
				if(Hunters.size()==0) {
					sender.sendMessage(ChatColor.YELLOW + "You are starting the game against NO HUNTERS"); //. If that's a mistake, do /mhstop // add later
				}
				ManhuntCommandActions.mhStart(Integer.parseInt(args[0]));
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
	
	public static void stopGameCommand(Player p, String[] args) {
		p.sendMessage(ChatColor.RED + "This command is not coded yet");
	}
	
	public static void reloadConfigCommand(CommandSender sender) {
		ConfigOperations.updateAndLoadConfig(); // maybe just load
		// and maybe check for user-added fields and delete them or something like that (maybe change something else too)
		sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the config");
	}
	
	public static void resetWorldCommand(CommandSender sender, String[] args) {
		WorldResetting.resetWorld(sender, args);
	}
	
	public static void authors(CommandSender sender) {
//		String discordLinkText = "a";
		String curseforgeLinkText = "https://www.curseforge.com/minecraft/bukkit-plugins/advanced-manhunt";
		String line = "-------------------------------------------------------------------------------";
		String endLine = "\n" + ChatColor.RED + line;
		
		String part1 = ChatColor.RED + line + ChatColor.RESET + "\n";
		part1 += ChatColor.WHITE + "" + ChatColor.BOLD + "Info about Advanced Manhunt plugin" + ChatColor.RESET + "\n";
		part1 += ChatColor.WHITE + "Author: " + ChatColor.RED + "" + ChatColor.BOLD + "Greenem" + ChatColor.RESET + "\n";
		part1 += ChatColor.GRAY + "Social links: \n" + ChatColor.RESET;
		part1 += ChatColor.WHITE + "Discord server: " + ChatColor.RESET;
		
//		String discordPartText = ChatColor.GRAY + "" + ChatColor.BOLD + discordLinkText;
		
		String part2 = ChatColor.GRAY + "\n" + "Download links: " + ChatColor.RESET + "\n";
		part2 += ChatColor.WHITE + "CurseForge: " + ChatColor.RESET;
		
		String curseforgePartText = ChatColor.GRAY + "" + ChatColor.BOLD + curseforgeLinkText;
		
//		sender.sendMessage(part1 + discordPartText + part2 + curseforgePartText + endLine);
		sender.sendMessage(part1 + part2 + curseforgePartText + endLine);

		//		clickable.setHoverEvent(new HoverEvent()); // to do later, show that player's name
	}
	
	public static void testCommand(Player p) {
//		setSpinningCompassMode(p);
//		Location l = p.getLocation().clone();
//		l.add(new Vector(5, 0, 0));
		
//		ItemStack compassItem = p.getInventory().getItemInMainHand();
//		CompassMeta meta = (CompassMeta) compassItem.getItemMeta();
//		meta.setLodestoneTracked(true);
//		meta.setLodestone(l);
//		compassItem.setItemMeta(meta);
		
//		setSpinningLodestoneCompass(p, p.getInventory().getItemInMainHand());
//		setUsualCompass(p.getInventory().getItemInMainHand());
//		setCompassToSpinningLodestone(p, p.getInventory().getItemInMainHand());
//		setUsualCompass(p.getInventory().getItemInMainHand());
//		setCompassToNotFound(p, p.getInventory().getItemInMainHand());
		
		CompassUtils.setTarget(p, p.getLocation().clone().add(new Vector(5, 0, 0)));
	}
	
	public static void problemsCommand(Player p, String[] args) {
		ManhuntTeam ownTeam = TeamsManager.getTeamByPlayerNickname(p.getName());
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
			playerNames = TeamsManager.getAllPlayers();
		} else if(check(specificArgs.typeOfSomeone, "remaining")) {
			playerNames = TeamsManager.getRemainingPlayers(toAdd);
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
		
		if(!toAdd || teamName != null && TeamsManager.teamExists(teamName)) {
			if(toAdd) {
				int result = TeamsManager.addToTeamAndRemFromOtherAndNotify(nickname, teamName, sender);
				if(result == -1) {
					if(sender == p) sender.sendMessage(FormattedMessages.youAreAlreadyTeamMessage());
					else sender.sendMessage(FormattedMessages.someoneIsAlreadyTeamMessage(p.getName()));
				}
			} else {
				TeamsManager.removeFromTeamAndNotify(nickname, sender);
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
				int totalPlayers = Speedrunners.size()+Hunters.size();
				if(totalPlayers==0) {
					sender.sendMessage(ChatColor.RED + "You didn't add a single hunter or speedrunner!");
					return;
				}
				if(Speedrunners.size()==0) {
					sender.sendMessage(ChatColor.RED + "You didn't add a single speedrunner!");
					return;
				}
				if(Hunters.size()==0) {
					sender.sendMessage(ChatColor.YELLOW + "You are starting the game against NO HUNTERS"); //. If that's a mistake, do /mhstop // add later
				}
				ManhuntCommandActions.mhStart(Integer.parseInt(args[0]));
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
