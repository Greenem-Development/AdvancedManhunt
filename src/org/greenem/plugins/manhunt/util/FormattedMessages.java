package org.greenem.plugins.manhunt.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.greenem.plugins.manhunt.models.ManhuntTeam;

public class FormattedMessages {
	private static String baseColor = ChatColor.WHITE + "" + ChatColor.BOLD;
	private static String playerColor = ChatColor.GRAY + "" + ChatColor.BOLD;
	
	private static String peacefulTeamColor = ChatColor.GREEN + "" + ChatColor.BOLD;
	private static String agressiveTeamColor = ChatColor.RED + "" + ChatColor.BOLD;
	private static String miscTeamColor = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD;
	private static String weirdTeamColor = ChatColor.GRAY + "" + ChatColor.BOLD;
	
	public static String playerJoinedTeamMessage(String nickname) {
		return baseColor + getPlayerNickname(nickname) + " has joined the team";
	}
	
	public static String playerLeftTeamMessage(String nickname) {
		return baseColor + getPlayerNickname(nickname) + " has left the team";
	}
	
	public static String youJoinedTeamMessage(String nickname, String teamName, ManhuntTeam team) {
		return baseColor + "You have joined the " + getDecoTeamName(teamName, team) + " team";
	}
	
	public static String youLeftTeamMessage(String nickname, String teamName, ManhuntTeam team) {
		return baseColor + "You have left the " + getDecoTeamName(teamName, team) + " team";
	}
	
	public static String youWereAddedToTeamMessage(String nickname, String teamName, ManhuntTeam team, CommandSender whoDidThat) {
		return baseColor + " You have beed added to the " + getDecoTeamName(teamName, team) + " team by " + getDecoSenderName(whoDidThat);
	}
	
	public static String youWereRemovedFromTeamMessage(String nickname, String teamName, ManhuntTeam team, CommandSender whoDidThat) {
		return baseColor + " You have been removed from the " + getDecoTeamName(teamName, team) + " team by " + getDecoSenderName(whoDidThat);
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
	
	public static String getDecoTeamName(String teamName, ManhuntTeam team) {
		return getTeamColor(team) + teamName + baseColor;
	}
	
	public static String getPlayerNickname(String nickname) {
		return playerColor + nickname + baseColor;
	}
	
	public static String getDecoSenderName(CommandSender sender) {
		if(sender instanceof ConsoleCommandSender) return playerColor + "[CONSOLE]" + baseColor;
		if(sender instanceof Player) return playerColor + sender.getName() + baseColor;
		return playerColor + "Non-player: " + sender.getName() + baseColor;
	}
	
	public static String getTeamColor(ManhuntTeam team) {
		if(team.canBeTracked() && !team.canUseTracking()) return peacefulTeamColor;
		else if(team.canBeTracked() && team.canUseTracking()) return miscTeamColor;
		else if(!team.canBeTracked() && team.canUseTracking()) return agressiveTeamColor;
		else return weirdTeamColor;
	}
}
