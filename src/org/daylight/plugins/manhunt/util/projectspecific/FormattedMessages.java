package org.daylight.plugins.manhunt.util.projectspecific;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;

public class FormattedMessages {
	public enum MessageType {
		NORMAL,
		ISSUE, // user's fault
		ERROR, // code error
		WARNING;
	};
	
	private static String baseColor = ChatColor.RESET + "" + ChatColor.WHITE; // + "" + ChatColor.BOLD;
	private static String errorColor = ChatColor.RESET + "" + ChatColor.RED;
	private static String warningColor = ChatColor.RESET + "" + ChatColor.YELLOW;
	private static String highlightColor = ChatColor.BOLD + ""; // + ChatColor.ITALIC;
	
	private static String playerColor = ChatColor.GRAY + "" + ChatColor.BOLD;
	
	private static String peacefulTeamColor = ChatColor.GREEN + "";
	private static String agressiveTeamColor = ChatColor.RED + "";
	private static String miscTeamColor = ChatColor.AQUA + "";
	private static String weirdTeamColor = ChatColor.GRAY + "";
	
	public static String playerJoinedTeamMessage(String nickname) {
		return baseColor + getPlayerNickname(nickname) + " has joined the team";
	}
	
	public static String playerLeftTeamMessage(String nickname) {
		return baseColor + getPlayerNickname(nickname) + " has left the team";
	}
	
	public static String youJoinedTeamMessage(String nickname, String teamName, ManhuntTeam team) {
		return baseColor + "You have joined the " + getColoredTeamName(team) + " team";
	}
	
	public static String youLeftTeamMessage(String nickname, String teamName, ManhuntTeam team) {
		return baseColor + "You have left the " + getColoredTeamName(team) + " team";
	}
	
	public static String youWereAddedToTeamMessage(String nickname, String teamName, ManhuntTeam team, CommandSender whoDidThat) {
		return baseColor + " You have beed added to the " + getColoredTeamName(team) + " team by " + getColoredSenderName(whoDidThat);
	}
	
	public static String youWereRemovedFromTeamMessage(String nickname, String teamName, ManhuntTeam team, CommandSender whoDidThat) {
		return baseColor + " You have been removed from the " + getColoredTeamName(team) + " team by " + getColoredSenderName(whoDidThat);
	}
	
	public static String someoneIsAlreadyTeamMessage(String nickname) {
		return baseColor + getPlayerNickname(nickname) + " is already in the team!";
	}
	
	public static String youAreAlreadyTeamMessage() {
		return baseColor + "You are already in the team!";
	}
	
	public static String cantFindPlayerMessage(String nickname) {
		return baseColor + "Can't find player " + getPlayerNickname(nickname) + "!";
	}
	
	public static String noTargetsAvaliableMessage() {
		return baseColor + "No targets are avaliable for the action!";
	}
	
	public static String youAreNotInATeamYetMessage() {
		return "You are not in a team yet!";
	}
	
	public static String someoneIsNotInATeamYetMessage(String nickname) {
		return baseColor + getPlayerNickname(nickname) + " is not in a team yet!";
	}
	
	public static String cantHoldCompassHereMessage() {
		return baseColor + ChatColor.RED + "You can't hold the compass in the nether and the end, so it won't update until you stop";
	}
	
	public static String getColoredTeamName(ManhuntTeam team) {
		return getTeamColor(team) + team.getName() + baseColor;
	}
	
	public static String getColoredTeamName(String teamName) {
		ManhuntTeam team = TeamManager.getTeamByName(teamName);
		if(team == null) return null;
		return getColoredTeamName(team);
	}
	
	public static String getPlayerNickname(String nickname) {
		return playerColor + nickname + baseColor;
	}
	
	public static void sendColoredMessage(CommandSender sender, MessageType messageType, String text) {
		String messageColor = getMessageColor(messageType);
		sender.sendMessage(messageColor + text.replace("%rst", messageColor).replace("%hlt", highlightColor));
	}
	
	public static String getColoredSenderName(CommandSender sender) {
		if(sender instanceof ConsoleCommandSender) return playerColor + "[CONSOLE]" + baseColor;
		if(sender instanceof Player) return playerColor + sender.getName() + baseColor;
		return playerColor + "Non-player: " + sender.getName() + baseColor;
	}
	
	public static String getTeamColor(ManhuntTeam team) {
		return getTeamColor(team, ChatColor.BOLD + "");
	}
	
	public static String getTeamColor(ManhuntTeam team, String style) {
		String actualStyle = ChatColor.BOLD + "";
		if(style != null) actualStyle = style;
		if(team.canBeTracked() && !team.canUseTracking()) return peacefulTeamColor + actualStyle;
		else if(team.canBeTracked() && team.canUseTracking()) return miscTeamColor + actualStyle;
		else if(!team.canBeTracked() && team.canUseTracking()) return agressiveTeamColor + actualStyle;
		else return weirdTeamColor + actualStyle;
	}
	
	private static String getMessageColor(MessageType type) {
		switch (type) {
		case NORMAL: {
			return baseColor;
		}
		case WARNING: {
			return warningColor;
		}
		case ISSUE: {
			return errorColor;
		}
		case ERROR: {
			return errorColor;
		}
		default:
			throw new IllegalArgumentException("Unexpected value in getMessageColor: " + type);
		}
	}
}
