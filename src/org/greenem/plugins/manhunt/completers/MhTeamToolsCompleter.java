package org.greenem.plugins.manhunt.completers;

import static org.greenem.plugins.manhunt.data.References.Hunters;
import static org.greenem.plugins.manhunt.data.References.Speedrunners;
import static org.greenem.plugins.manhunt.util.SimpleLogger.log;
import static org.greenem.plugins.manhunt.util.SimpleLogger.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.greenem.plugins.manhunt.compass.TeamsManager;
import org.greenem.plugins.manhunt.completers.util.CompleterUtils;
import org.greenem.plugins.manhunt.data.Lines;
import org.greenem.plugins.manhunt.models.ManhuntTeam;
import org.greenem.plugins.manhunt.util.FormattedMessages;
import org.greenem.plugins.manhunt.util.FormattedMessages.MessageType;
import org.javatuples.Pair;
import org.javatuples.Triplet;

public class MhTeamToolsCompleter implements TabCompleter {
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
	
	private static boolean matchesPattern(AtomicReference<Matcher> matcher, String text, Pattern pattern) {
		matcher.set(pattern.matcher(text));
		return matcher.get().matches();
	}
	
	private static Pair<Boolean, Boolean> stringToBooleanAndError(String text) {
		String tl = text.toLowerCase();
		if(tl.equals("true")) return new Pair<Boolean, Boolean>(true, false);
		else if(tl.equals("false")) return new Pair<Boolean, Boolean>(false, false);
		else return new Pair<Boolean, Boolean>(false, true);
	}
	
	private static ManhuntTeam checkTeamOption(CommandSender sender, String teamName) {
		ManhuntTeam team = TeamsManager.getTeamByName(teamName);
//		Pair<Boolean, Boolean> optionAndError = stringToBooleanAndError(optionName);
		if(team == null) {
			FormattedMessages.sendColoredMessage(sender, MessageType.ISSUE, "Such team doesn't exist!");
		}
//		else if (optionAndError.getValue1()) {
//			sender.sendMessage("Choose true/false value!");
//		}
		
		return team;
	}
	
	private static Pair<ManhuntTeam, ManhuntTeam> checkTeamsOptions(CommandSender sender, String teamName, String targetTeamName) {
		ManhuntTeam team = TeamsManager.getTeamByName(teamName);
		ManhuntTeam targetTeam = TeamsManager.getTeamByName(targetTeamName);
		
		if(team == null) {
			sender.sendMessage("Such team doesn't exist!");
		}
		else if(targetTeam == null) {
			sender.sendMessage("Target team doesn't exist!");
		}
		
		return new Pair<ManhuntTeam, ManhuntTeam>(team, targetTeam);
	}
	
	private static String removeLastWord(String text) {
		String[] words = text.trim().split("\\s");
		String[] otherParts = new String[words.length-1];
		for(int i = 0; i > otherParts.length; i++) {
			otherParts[i] = words[i];
		}
		return String.join(" ", otherParts);
	}
	
	private static String anythingExceptQuotes = "[^\"]";
	private static String regexNameWQuotesSuppport = "((?:\"" + anythingExceptQuotes + "*\")|(?:" + anythingExceptQuotes + "*))"; // "((?:\".+\")|(?:\\S+))";
	private static String regexNameWQuotesSuppportButExcluded = "(?:(?:\"(" + anythingExceptQuotes + "+)\")|(\\S+))";
	private static String regexTrueOrFalse = "((?:true)|(?:false))";
	private static String regexNOTTrueOrFalse = "\\S+(?!true)(?!false)\\S+";
	
	private static Pattern patternCreateTeamStart = Pattern.compile("create_team\\s*");
	private static Pattern patternCreateTeamDone = Pattern.compile("create_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	private static Pattern patternDeleteTeamStart = Pattern.compile("delete_team\\s*");
	private static Pattern patternDeleteTeamDone = Pattern.compile("delete_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	
	private static Pattern patternConfigureTeamStart = Pattern.compile("configure_team\\s*");
//	private static Pattern patternConfigureTeamInPrgr = Pattern.compile("configure_team\\s+\\S+\\s*");
	private static Pattern patternConfigureTeamWNameStart = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+\\s*");
//	private static Pattern patternConfigureTeamWNameInPrgr = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+\\S+\\s*");
	private static Pattern patternConfigureTeamTrackingTrueFalseStart = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:can_use_tracking)|(?:can_be_tracked))\\s*");
//	private static Pattern patternConfigureTeamTrackingTrueFalseInPrgr = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:can_use_tracking)|(?:can_be_tracked))\\s+\\S+\\s*");
	private static Pattern patternConfigureTeamTrackOrUntrackTeamStart = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:track_team)|(?:untrack_team))\\s*");
//	private static Pattern patternConfigureTeamTrackOrUntrackTeamInPrgr = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:track_team)|(?:untrack_team))\\s+\\S+\\s*");
	
	private static Pattern patternConfigureTeamCanUseTrackingDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+can_use_tracking\\s+" + regexTrueOrFalse + "\\s*");
	private static Pattern patternConfigureTeamCanBeTrackedDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+can_be_tracked\\s+" + regexTrueOrFalse + "\\s*");
	private static Pattern patternConfigureTeamTrackTeamDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+track_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	private static Pattern patternConfigureTeamUntrackTeamDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+untrack_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	private static Pattern patternConfigureTeamTrackingList = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+tracking_list\\s*");
	
	private static Pattern patternBooleanWrongConfigureTeam = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+(?:can_use_tracking|can_be_tracked)\\s+" + regexNOTTrueOrFalse + "\\s*");
	
	private static Pattern patternEmpty = Pattern.compile("\\s*");
	
	private static final List<String> nothing = new ArrayList<>();
	
	public static List<String> processMhToolsArgs(CommandSender sender, String s, boolean returnSuggestionsInstead) {
		List<String> arguments = new ArrayList<>();
//		String sl = s.toLowerCase();
		AtomicReference<Matcher> matcher = new AtomicReference<>();
		AtomicReference<Matcher> matcher2 = new AtomicReference<>();
		
		// ---------------- create / delete a team ----------------
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternCreateTeamDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			if(TeamsManager.teamExists(teamName)) {
				FormattedMessages.sendColoredMessage(sender, MessageType.ISSUE, "Such team already exists!");
				return nothing;
			}
			ManhuntTeam newTeam = new ManhuntTeam(teamName).setCanUseTracking(false).setCanBeTracked(true);
			TeamsManager.createTeam(newTeam);
			FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Created team " + FormattedMessages.getDecoTeamName(newTeam));
			return nothing;
		}
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternDeleteTeamDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam theTeam = checkTeamOption(sender, teamName);
			if(theTeam == null) return nothing;
			String teamText = FormattedMessages.getDecoTeamName(theTeam);
			TeamsManager.deleteTeam(teamName);
			FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Deleted team " + teamText);
			return nothing;
		}
		
		if(returnSuggestionsInstead && matchesPattern(matcher, s, patternCreateTeamStart)) {
			return arguments;
		}
		
		if(returnSuggestionsInstead && matchesPattern(matcher, s, patternDeleteTeamStart)) {
			return TeamsManager.getAllTeamsNames();
		}
		
		// configure
		
		if(returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamStart)) { // || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamInPrgr)
			return TeamsManager.getAllTeamsNames();
		}
		
		if(returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamWNameStart)) { // || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamWNameInPrgr)
			arguments.add("can_use_tracking");
			arguments.add("can_be_tracked");
			arguments.add("track_team");
			arguments.add("untrack_team");
			arguments.add("tracking_list");
			return arguments;
		}
		
		if(returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamTrackingTrueFalseStart)) { //  || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamTrackingTrueFalseInPrgr)
			arguments.add("true");
			arguments.add("false");
			return arguments;
		}
		
		if(returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamTrackOrUntrackTeamStart)) { // || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamTrackOrUntrackTeamInPrgr)
			return TeamsManager.getAllTeamsNames();
		}
		
		//               --------------------------------- EXECUTORS ------------------------------------ 
		
		// True or false
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamCanUseTrackingDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam team = checkTeamOption(sender, teamName);
			if(team != null) {
				team.setCanUseTracking(Boolean.parseBoolean(matcher.get().group(2)));
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Have set \"can use tracking\" to %hlt" + matcher.get().group(2).toLowerCase() + "%rst for " + FormattedMessages.getDecoTeamName(teamName));
			}
			return nothing;
		}
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamCanBeTrackedDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam team = checkTeamOption(sender, teamName);
			if(team != null) {
				team.setCanBeTracked(Boolean.parseBoolean(matcher.get().group(2)));
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Have set \"can be tracked\" to %hlt" + matcher.get().group(2).toLowerCase() + "%rst for " + FormattedMessages.getDecoTeamName(teamName));
			}
			return nothing;
		}
		
		// negative
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternBooleanWrongConfigureTeam)) {
			FormattedMessages.sendColoredMessage(sender, MessageType.ISSUE, "Choose %hlt\"true\"%rst or %hlt\"false\"%rst option!"); // "Choose " + ChatColor.BOLD + "\"true\"" + ChatColor.RESET + ChatColor.RED + " or " + ChatColor.BOLD + "\"false\"" + ChatColor.RESET + ChatColor.RED + " option!");
			return nothing;
		}
		
		// Tracking targets
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamTrackTeamDone)) {
			String team1Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			String team2Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(2));
			Pair<ManhuntTeam, ManhuntTeam> teams = checkTeamsOptions(sender, team1Name, team2Name);
			if(teams.getValue0() != null && teams.getValue1() != null) {
				teams.getValue0().addTeamToTrack(teams.getValue1());
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Team " + FormattedMessages.getDecoTeamName(team1Name) + "%rst is now tracking team " + FormattedMessages.getDecoTeamName(team2Name));
			}
			return nothing;
		}
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamUntrackTeamDone)) {
			String team1Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			String team2Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(2));
			Pair<ManhuntTeam, ManhuntTeam> teams = checkTeamsOptions(sender, team1Name, team2Name);
			if(teams.getValue0() != null && teams.getValue1() != null) {
				teams.getValue0().addTeamToTrack(teams.getValue1());
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Team " + FormattedMessages.getDecoTeamName(team1Name) + "%rst is now not tracking team " + FormattedMessages.getDecoTeamName(team2Name) + "%rst anymore");
			}
			return nothing;
		}
		
		if(!returnSuggestionsInstead && matchesPattern(matcher, s, patternConfigureTeamTrackingList)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam team = checkTeamOption(sender, teamName);
			if(team != null) {
				String text = "---- Team " + FormattedMessages.getDecoTeamName(teamName) + " ----\n";
				
				if(team.getListTrackingOtherTeams().size() > 0) text += "\n%hltTracking:%rst";
				for(ManhuntTeam otherTeam : team.getListTrackingOtherTeams()) {
					text += "\n%rst- " + FormattedMessages.getDecoTeamName(otherTeam.getName());
				}
				
				if(team.getListBeingTrackedByTeams().size() > 0) text += "\n%hltTracked by:%rst";
				for(ManhuntTeam otherTeam : team.getListBeingTrackedByTeams()) {
					text += "\n%rst- " + FormattedMessages.getDecoTeamName(otherTeam.getName());
				}
				
				if(team.getListTrackingOtherTeams().size() == 0 && team.getListBeingTrackedByTeams().size() == 0) {
					text += "\n%rstNot tracking any team and not being tracked";
				}
				
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, text);
			}
			return nothing;
		}
		
		// empty
		
		if(returnSuggestionsInstead && matchesPattern(matcher, s, patternEmpty)) {
			arguments.add("create_team");
			arguments.add("delete_team");
			arguments.add("configure_team");
			return arguments;
		}
		
		if(!returnSuggestionsInstead) {
			sender.sendMessage(Lines.genericWrongCommandUsage);
		}
		return arguments;
	}
	
	public static class MhTeamToolsArgs { // old
		Pattern pattern1 = Pattern.compile("((create)|(modify)((\\severyone|\\sremaining|\\sspecific(\\s\\S+)?)?)?)?(\\sjoin|\\sleave)?(\\s\\S+)?\\s*");
		Pattern patternCreateTeam = Pattern.compile("create_team(?:\\s(\\S+))?\\s*");
		Pattern patternCreateTeam1 = Pattern.compile("configure_team(?:\\s(\\S+))?(?:\\s((?:can_use_tracking)|(?:can_be_tracked)|(?:can_choose_target_team)))?\\s*");
	}
	
	class TestCommandImpl extends Command {
		protected TestCommandImpl(String name) {
			super(name);
		}

		@Override
		public boolean execute(CommandSender sender, String commandLabel, String[] args) {
			return false;
		}
	}
	
	public static void main(String[] args) {
//		Pattern p = Pattern.compile("(?:(?:\"(.+)\")|(\\S+))");
//		System.out.println(p.matcher("\"aaa\"").group(1));
		
//		String input = "\"example text\"";
//        Pattern pattern = Pattern.compile("^(\"(.*?)\"|([^\"].*?[^\"\\s]))$");
//        Matcher matcher = pattern.matcher(input);
//        String result = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
//        System.out.println(result);
        
//        String test = "(?:(?:\"(.+)\")|(\\S+))"; // 2 groups, prob working
		
		String str = "configure_team \"a a\" can_be_tracked taa";
		System.out.println(patternBooleanWrongConfigureTeam.matcher(str).matches());
		
		MhTeamToolsCompleter instance = new MhTeamToolsCompleter();
		TeamsManager.createTeam(new ManhuntTeam("a a"));
		TeamsManager.createTeam(new ManhuntTeam("b b"));
		System.out.println(instance.onTabComplete(null, null, null, new String[] {str}));
		
//		Pattern lastBrackededWord = Pattern.compile("(?:[^\"])*(\"[^\"]*\"?)$");
//		System.out.println("After quotes: " + CompleterUtils.extractAfterQuotes(str));
//		System.out.println(lastBrackededWord.matcher(str).matches());
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		AtomicReference<Matcher> matcher = new AtomicReference<>();
		String s = String.join(" ", args);
		
//		debug(s);
		
		if(!matchesPattern(matcher, s, patternEmpty) && CompleterUtils.stillAtTheWordOrBrackets(s)) {
//			String tryWith = CompleterUtils.removeWordAtTheEnd(s).trim() + " ";
			String userInput = CompleterUtils.getWordAtTheEnd(s);
			String tryWith = s.substring(0, s.length() - userInput.length()).trim() + " ";
			debug("Checking for: " + tryWith);
			List<String> options = processMhToolsArgs(sender, tryWith, true);
			if(options == null) return null;
			
//			debug("Removed: " + userInput);
			return CompleterUtils.applyFilteredSuggestions(options, userInput);
		} else return CompleterUtils.applyFilteredSuggestions(processMhToolsArgs(sender, s, true), null);
	}
}
