package org.daylight.plugins.manhunt.commands.combined;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.daylight.plugins.manhunt.completers.util.CompleterUtils;
import org.daylight.plugins.manhunt.data.Lines;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages.MessageType;
import org.javatuples.Pair;

public class MhTeamToolsExecutorCompleter implements TabCompleter {
	private static boolean matchesPattern(AtomicReference<Matcher> matcher, String text, Pattern pattern) {
		matcher.set(pattern.matcher(text));
		return matcher.get().matches();
	}
	
	private static ManhuntTeam checkTeamOption(CommandSender sender, String teamName) {
		ManhuntTeam team = TeamManager.getTeamByName(teamName);
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
		ManhuntTeam team = TeamManager.getTeamByName(teamName);
		ManhuntTeam targetTeam = TeamManager.getTeamByName(targetTeamName);
		
		if(team == null) {
			sender.sendMessage("Such team doesn't exist!");
		}
		else if(targetTeam == null) {
			sender.sendMessage("Target team doesn't exist!");
		}
		
		return new Pair<ManhuntTeam, ManhuntTeam>(team, targetTeam);
	}
	
	private static String anythingExceptQuotes = "[^\"]";
	private static String regexNameWQuotesSuppport = "((?:\"" + anythingExceptQuotes + "*\")|(?:" + anythingExceptQuotes + "*))"; // "((?:\".+\")|(?:\\S+))";
	private static String regexNameWQuotesSuppportButExcluded = "(?:(?:\"(" + anythingExceptQuotes + "+)\")|(\\S+))";
	private static String regexTrueOrFalse = "((?:true)|(?:false))";
	private static String regexNOTTrueOrFalse = "\\S+(?!true)(?!false)\\S*"; // was \\S+ there
	
	private static Pattern patternCreateTeamStart = Pattern.compile("create_team\\s*");
	private static Pattern patternCreateTeamDone = Pattern.compile("create_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	private static Pattern patternDeleteTeamStart = Pattern.compile("delete_team\\s*");
	private static Pattern patternDeleteTeamDone = Pattern.compile("delete_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	
	private static Pattern patternConfigureTeamStart = Pattern.compile("configure_team\\s*");

	private static Pattern patternConfigureTeamWNameStart = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+\\s*");
	private static Pattern patternConfigureTeamTrackingTrueFalseStart = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:can_use_tracking)|(?:can_be_tracked))\\s*");
	private static Pattern patternConfigureTeamTrackOrUntrackTeamStart = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:track_team)|(?:untrack_team))\\s*");
	
	private static Pattern patternConfigureTeamCanUseTrackingDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+can_use_tracking\\s+" + regexTrueOrFalse + "\\s*");
	private static Pattern patternConfigureTeamCanBeTrackedDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+can_be_tracked\\s+" + regexTrueOrFalse + "\\s*");
	private static Pattern patternConfigureTeamTrackTeamDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+track_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	private static Pattern patternConfigureTeamUntrackTeamDone = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+untrack_team\\s+" + regexNameWQuotesSuppport + "\\s*");
	private static Pattern patternConfigureTeamTrackingList = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+tracking_list\\s*");
	
	private static Pattern patternBooleanWrongConfigureTeam = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+(?:can_use_tracking|can_be_tracked)\\s+" + regexNOTTrueOrFalse + "\\s*");
	
	private static Pattern patternEmpty = Pattern.compile("\\s*");
	
//	private static Pattern patternConfigureTeamInPrgr = Pattern.compile("configure_team\\s+\\S+\\s*");
//	private static Pattern patternConfigureTeamWNameInPrgr = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+\\S+\\s*");
//	private static Pattern patternConfigureTeamTrackingTrueFalseInPrgr = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:can_use_tracking)|(?:can_be_tracked))\\s+\\S+\\s*");
//	private static Pattern patternConfigureTeamTrackOrUntrackTeamInPrgr = Pattern.compile("configure_team\\s+" + regexNameWQuotesSuppport + "\\s+((?:track_team)|(?:untrack_team))\\s+\\S+\\s*");
	
	private static final List<String> nothing = new ArrayList<>();
	
	public static void executeMhToolsCommand(CommandSender sender, String s) {
		List<String> arguments = new ArrayList<>();
//		String sl = s.toLowerCase();
		AtomicReference<Matcher> matcher = new AtomicReference<>();
		
		if(matchesPattern(matcher, s, patternCreateTeamDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			if(TeamManager.teamExists(teamName)) {
				FormattedMessages.sendColoredMessage(sender, MessageType.ISSUE, "Such team already exists!");
				return;
			}
			ManhuntTeam newTeam = new ManhuntTeam(teamName).setCanUseTracking(false).setCanBeTracked(true);
			TeamManager.createTeam(newTeam);
			FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Created team " + FormattedMessages.getColoredTeamName(newTeam));
			return ;
		}
		
		if(matchesPattern(matcher, s, patternDeleteTeamDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam theTeam = checkTeamOption(sender, teamName);
			if(theTeam == null) return;
			String teamText = FormattedMessages.getColoredTeamName(theTeam);
			TeamManager.deleteTeam(teamName);
			FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Deleted team " + teamText);
			return;
		}
		
		// True or false
		
		if(matchesPattern(matcher, s, patternConfigureTeamCanUseTrackingDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam team = checkTeamOption(sender, teamName);
			if(team != null) {
				team.setCanUseTracking(Boolean.parseBoolean(matcher.get().group(2)));
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Have set \"can use tracking\" to %hlt" + matcher.get().group(2).toLowerCase() + "%rst for " + FormattedMessages.getColoredTeamName(teamName));
			}
			return;
		}
		
		if(matchesPattern(matcher, s, patternConfigureTeamCanBeTrackedDone)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam team = checkTeamOption(sender, teamName);
			if(team != null) {
				team.setCanBeTracked(Boolean.parseBoolean(matcher.get().group(2)));
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Have set \"can be tracked\" to %hlt" + matcher.get().group(2).toLowerCase() + "%rst for " + FormattedMessages.getColoredTeamName(teamName));
			}
			return;
		}
		
		// negative
		
		if(matchesPattern(matcher, s, patternBooleanWrongConfigureTeam)) {
			FormattedMessages.sendColoredMessage(sender, MessageType.ISSUE, "Choose %hlt\"true\"%rst or %hlt\"false\"%rst option!"); // "Choose " + ChatColor.BOLD + "\"true\"" + ChatColor.RESET + ChatColor.RED + " or " + ChatColor.BOLD + "\"false\"" + ChatColor.RESET + ChatColor.RED + " option!");
			return;
		}
		
		// Tracking targets
		
		if(matchesPattern(matcher, s, patternConfigureTeamTrackTeamDone)) {
			String team1Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			String team2Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(2));
			Pair<ManhuntTeam, ManhuntTeam> teams = checkTeamsOptions(sender, team1Name, team2Name);
			if(teams.getValue0() != null && teams.getValue1() != null) {
				teams.getValue0().addTeamToTrack(teams.getValue1());
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Team " + FormattedMessages.getColoredTeamName(team1Name) + "%rst is now tracking team " + FormattedMessages.getColoredTeamName(team2Name));
			}
			return;
		}
		
		if(matchesPattern(matcher, s, patternConfigureTeamUntrackTeamDone)) {
			String team1Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			String team2Name = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(2));
			Pair<ManhuntTeam, ManhuntTeam> teams = checkTeamsOptions(sender, team1Name, team2Name);
			if(teams.getValue0() != null && teams.getValue1() != null) {
				teams.getValue0().addTeamToTrack(teams.getValue1());
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, "Team " + FormattedMessages.getColoredTeamName(team1Name) + "%rst is now not tracking team " + FormattedMessages.getColoredTeamName(team2Name) + "%rst anymore");
			}
			return;
		}
		
		if(matchesPattern(matcher, s, patternConfigureTeamTrackingList)) {
			String teamName = CompleterUtils.formatRemoveNameQuotes(matcher.get().group(1));
			ManhuntTeam team = checkTeamOption(sender, teamName);
			if(team != null) {
				String text = "---- Team " + FormattedMessages.getColoredTeamName(teamName) + " ----\n";
				
				if(team.getListTrackingOtherTeams().size() > 0) text += "\n%hltTracking:%rst";
				for(ManhuntTeam otherTeam : team.getListTrackingOtherTeams()) {
					text += "\n%rst- " + FormattedMessages.getColoredTeamName(otherTeam.getName());
				}
				
				if(team.getListBeingTrackedByTeams().size() > 0) text += "\n%hltTracked by:%rst";
				for(ManhuntTeam otherTeam : team.getListBeingTrackedByTeams()) {
					text += "\n%rst- " + FormattedMessages.getColoredTeamName(otherTeam.getName());
				}
				
				if(team.getListTrackingOtherTeams().size() == 0 && team.getListBeingTrackedByTeams().size() == 0) {
					text += "\n%rstNot tracking any team and not being tracked";
				}
				
				FormattedMessages.sendColoredMessage(sender, MessageType.NORMAL, text);
			}
			return;
		}
		
		sender.sendMessage(Lines.genericWrongCommandUsage);
	}
	
	public static List<String> suggestMhToolsCommand(CommandSender sender, String s) {
		List<String> arguments = new ArrayList<>();
		AtomicReference<Matcher> matcher = new AtomicReference<>();
		
		// ---------------- create / delete a team ----------------
		
		if(matchesPattern(matcher, s, patternCreateTeamStart)) {
			return arguments;
		}
		
		if(matchesPattern(matcher, s, patternDeleteTeamStart)) {
			return TeamManager.getAllTeamsNames();
		}
		
		// configure
		
		if(matchesPattern(matcher, s, patternConfigureTeamStart)) { // || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamInPrgr)
			return TeamManager.getAllTeamsNames();
		}
		
		if(matchesPattern(matcher, s, patternConfigureTeamWNameStart)) { // || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamWNameInPrgr)
			arguments.add("can_use_tracking");
			arguments.add("can_be_tracked");
			arguments.add("track_team");
			arguments.add("untrack_team");
			arguments.add("tracking_list");
			return arguments;
		}
		
		if(matchesPattern(matcher, s, patternConfigureTeamTrackingTrueFalseStart)) { //  || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamTrackingTrueFalseInPrgr)
			arguments.add("true");
			arguments.add("false");
			return arguments;
		}
		
		if(matchesPattern(matcher, s, patternConfigureTeamTrackOrUntrackTeamStart)) { // || matchesPattern(matcher2, removeLastWord(s), patternConfigureTeamTrackOrUntrackTeamInPrgr)
			return TeamManager.getAllTeamsNames();
		}
		
		// empty
		
		if(matchesPattern(matcher, s, patternEmpty)) {
			arguments.add("create_team");
			arguments.add("delete_team");
			arguments.add("configure_team");
			return arguments;
		}
		
		return arguments;
	}
	
	public static void main2(String[] args, int a) {
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
		
		MhTeamToolsExecutorCompleter instance = new MhTeamToolsExecutorCompleter();
		TeamManager.createTeam(new ManhuntTeam("a a"));
		TeamManager.createTeam(new ManhuntTeam("b b"));
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
//			debug("Checking for: " + tryWith);
			List<String> options = suggestMhToolsCommand(sender, tryWith);
			if(options == null) return null;
			
//			debug("Removed: " + userInput);
			return CompleterUtils.applyFilteredSuggestions(options, userInput);
		} else return CompleterUtils.applyFilteredSuggestions(suggestMhToolsCommand(sender, s), null);
	}
}
