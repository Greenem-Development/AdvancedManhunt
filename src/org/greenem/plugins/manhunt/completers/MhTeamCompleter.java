package org.greenem.plugins.manhunt.completers;

import static org.greenem.plugins.manhunt.data.References.Hunters;
import static org.greenem.plugins.manhunt.data.References.Speedrunners;
import static org.greenem.plugins.manhunt.util.SimpleLogger.log;
import static org.greenem.plugins.manhunt.util.SimpleLogger.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.greenem.plugins.manhunt.compass.TeamsManager;
import org.greenem.plugins.manhunt.models.ManhuntTeam;

public class MhTeamCompleter implements TabCompleter {
	public static boolean check(String s1, String s2) {
		if(s1 == s2) return true;
		if(s1 == null || s2 == null) return false;
		return s1.equals(s2);
	}
	
	public static String groupAndFirstWord(Matcher matcher, int groupId) {
		String res = matcher.group(groupId);
		if(res != null) {
			String[] parts = res.split(" ");
			if (parts.length == 0) return null;
			return parts[0];
		}
		return null;
	}
	
	public static String groupAndTrimAndFirstWord(Matcher matcher, int groupId) {
		String res = matcher.group(groupId);
		if(res != null) {
			String[] parts = res.trim().split(" ");
			if (parts.length == 0) return null;
			return parts[0];
		}
		return null;
	}
	
	public static String groupAndTrim(Matcher matcher, int groupId) {
		String res = matcher.group(groupId);
		if(res != null) res = res.trim();
		return res;
	}
	
	public static class MhTeamArgs {
		private static Pattern pattern = Pattern.compile("((self)|(someone)((\\severyone|\\sremaining|\\sspecific(\\s\\S+)?)?)?)?(\\sjoin|\\sleave)?(\\s\\S+)?\\s*");
		
		public String selfOrSomeone = null;
		public String typeOfSomeone = null;
		public String targetName = null;
		public String actionType = null;
		public String teamName = null;
		
		public boolean stopSuggestions = false;
		
		public MhTeamArgs(String[] args) {
			String s = String.join(" ", args);
			Matcher matcher = pattern.matcher(s);
			if(!matcher.find()) return;
			
			selfOrSomeone = groupAndFirstWord(matcher, 1);
			typeOfSomeone = groupAndTrimAndFirstWord(matcher, 5);
			targetName = groupAndTrim(matcher, 6);
			actionType = groupAndTrim(matcher, 7);
			teamName = groupAndTrim(matcher, 8);
			
			if(teamName != null && s.endsWith(" ")) {
				stopSuggestions = true;
				return;
			}
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> arguments = new ArrayList<>();
		
		if(cmd.getName().equalsIgnoreCase("mhteam")) {
			MhTeamArgs specificArgs = new MhTeamArgs(args);
			String selfOrSomeone = specificArgs.selfOrSomeone;
			String typeOfSomeone = specificArgs.typeOfSomeone;
			String targetName = specificArgs.targetName;
			String actionType = specificArgs.actionType;
			
//			log(args.length + " | 1:" + selfOrSomeone + " 2:" + typeOfSomeone + " 3:" + targetName + " 4:" + actionType + " 5:" + specificArgs.teamName + " " + specificArgs.stopSuggestions);
			
			if(specificArgs.stopSuggestions) return arguments;
			
			if(selfOrSomeone == null) {
				arguments.add("self");
				arguments.add("someone");
				return arguments;
			}
			
			if(check(selfOrSomeone, "someone") && typeOfSomeone == null && targetName == null) {
				arguments.add("specific");
				arguments.add("remaining");
				arguments.add("everyone");
				return arguments;
			}
			
			if(check(selfOrSomeone, "someone") && check(typeOfSomeone, "specific") && targetName == null) {
				for(Player p : Bukkit.getOnlinePlayers()) {
					arguments.add(p.getName());
				}
				return arguments;
			}
			
			if((targetName != null || check(selfOrSomeone, "self") || check(typeOfSomeone, "remaining") || check(typeOfSomeone, "everyone")) && (actionType == null || actionType.isBlank())) {
				arguments.add("join");
				arguments.add("leave");
				return arguments;
			}
			
			if(actionType != null && !(check(actionType, "leave"))) {
				for(Map.Entry<String, ManhuntTeam> team : TeamsManager.getAllTeams().entrySet()) {
					arguments.add(team.getKey());
				}
				return arguments;
			}
		}
		return arguments;
	}
}
