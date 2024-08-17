package org.greenem.plugins.manhunt.compass;

import static org.greenem.plugins.manhunt.util.SimpleLogger.log;
import static org.greenem.plugins.manhunt.util.SimpleLogger.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.greenem.plugins.manhunt.completers.MhTeamCompleter;
import org.greenem.plugins.manhunt.models.ManhuntPlayer;
import org.greenem.plugins.manhunt.models.ManhuntTeam;
import org.greenem.plugins.manhunt.util.FormattedMessages;

public class TeamsManager {
	private static Map<String, ManhuntTeam> teams = new HashMap<>();
	
	public static Map<String, ManhuntTeam> getAllTeams() {
		return teams;
	}
	
	public static void addTeam(String name, ManhuntTeam team) {
		if(teams.containsKey(name)) throw new IllegalArgumentException("Such team already exists");
		teams.put(name, team);
	}
	
	public static void deleteTeam(String name) {
		if(!teams.containsKey(name)) throw new IllegalArgumentException("Such team doesn't exist");
		teams.remove(name);
	}
	
	public static void deleteTeam(ManhuntTeam team) {
		if(!teams.containsValue(team)) throw new IllegalArgumentException("Such team doesn't exist");
		for(Map.Entry<String, ManhuntTeam> t : teams.entrySet()) {
			if(t.getValue() == team) {
				teams.remove(t.getKey());
				return;
			}
		}
	}
	
	public static boolean teamExists(String name) {
		return teams.containsKey(name);
	}
	
	public static ManhuntTeam getTeamByName(String name) {
		return teams.get(name);
	}
	
	public static ManhuntTeam getTeamByPlayerNickname(String nickname) {
		for(Map.Entry<String, ManhuntTeam> team : teams.entrySet()) {
			if(team.getValue().getPlayer(nickname) != null) return team.getValue();
		}
		return null;
	}
	
	public static String getTeamNameByPlayerNickname(String nickname) {
		for(Map.Entry<String, ManhuntTeam> team : teams.entrySet()) {
			if(team.getValue().getPlayer(nickname) != null) return team.getKey();
		}
		return null;
	}
	
	public static ManhuntPlayer getPlayer(String nickname) {
		for(Map.Entry<String, ManhuntTeam> team : teams.entrySet()) {
			ManhuntPlayer p = team.getValue().getPlayer(nickname);
			if(p != null) return p;
		}
		return null;
	}
	
	public static void clearTeams() {
		teams.clear();
	}
	
	public static void resetTeams() {
		clearTeams();
		addTeam("hunters", new ManhuntTeam(true, false));
		addTeam("speedrunners", new ManhuntTeam(false, true));
		getTeamByName("hunters").addTeamToTrack(getTeamByName("speedrunners"));
	}
	
	// Returns: -1: already in that team
	public static int addToTeamAndRemFromOtherAndNotify(String nickname, String teamName, CommandSender sender) {
		if(!teamExists(teamName)) throw new IllegalArgumentException("The team doesn't exist!");
		Player p = Bukkit.getPlayerExact(nickname);
		ManhuntTeam team = getTeamByName(teamName);
		if(team.getPlayer(nickname) != null) return -1;
		
		internalRemFromOtherAndNotify(nickname, getTeamByName(teamName), sender);
		team.addPlayer(new ManhuntPlayer(nickname));
		if(team.canBeTracked()) CompassUpdating.updateOnMovement(p, p.getLocation());
		if(team.canUseTracking()) {
			CompassUtils.giveCompass(p);
			CompassUpdating.updateBecauseATrackerAppeared(p);
		}
		
		team.sendMessage(FormattedMessages.playerJoinedTeamMessage(nickname), nickname);
		if(sender == p) p.sendMessage(FormattedMessages.youJoinedTeamMessage(nickname, teamName, team));
		else p.sendMessage(FormattedMessages.youWereAddedToTeamMessage(nickname, teamName, team, sender));
		return 0;
	}
	
	private static void internalRemFromOtherAndNotify(String nickname, ManhuntTeam team, CommandSender sender) {
		for(Map.Entry<String, ManhuntTeam> t : teams.entrySet()) {
			if(t.getValue() != team) {
				t.getValue().removePlayer(nickname);
				t.getValue().sendMessage(FormattedMessages.playerLeftTeamMessage(nickname), nickname);
			}
		}
	}
	
	public static void removeFromTeamAndNotify(String nickname, CommandSender sender) {
		Player p = Bukkit.getPlayerExact(nickname);
		ManhuntTeam team = TeamsManager.getTeamByPlayerNickname(nickname);
		log(team);
		if(team == null) {
			if(sender == p) sender.sendMessage(FormattedMessages.youAreNotInATeamYetMessage());
			else sender.sendMessage(FormattedMessages.someoneIsNotInATeamYetMessage(nickname));
			return;
		}
		String teamName = TeamsManager.getTeamNameByPlayerNickname(nickname);
		team.removePlayer(nickname);
		team.sendMessage(FormattedMessages.playerLeftTeamMessage(nickname), nickname);
		
		if(sender == p) p.sendMessage(FormattedMessages.youLeftTeamMessage(nickname, teamName, team));
		else p.sendMessage(FormattedMessages.youWereRemovedFromTeamMessage(nickname, teamName, team, sender));
		CompassUtils.removeAllCompasses(p);
	}
	
	public static List<String> getAllPlayers() {
		List<String> players = new ArrayList<>();
		for(Player p : Bukkit.getOnlinePlayers()) {
			players.add(p.getName());
		}
		return players;
	}
	
	public static List<String> getRemainingPlayers(boolean toAdd) {
		List<String> players = new ArrayList<>();
		for(Player p : Bukkit.getOnlinePlayers()) {
			boolean isInATeam = TeamsManager.getPlayer(p.getName()) != null;
			if(toAdd && !isInATeam || !toAdd && isInATeam) players.add(p.getName());
		}
		return players;
	}
}
