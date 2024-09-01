package org.daylight.plugins.manhunt.teams;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.daylight.plugins.manhunt.compass.CompassUpdating;
import org.daylight.plugins.manhunt.compass.CompassUtils;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems.MainCompassType;

public class TeamManager {
	private static List<ManhuntTeam> teams = new ArrayList<>();
	
	public static List<ManhuntTeam> getAllTeams() {
		return teams;
	}
	
	public static List<String> getAllTeamsNames() {
		List<String> results = new ArrayList<>();
		for(ManhuntTeam team : getAllTeams()) {
			results.add(team.getName());
		}
		return results;
	}
	
	public static void createTeam(ManhuntTeam team) {
		if(teamExists(team.getName())) return; // throw new IllegalArgumentException("Such team already exists");
		teams.add(team);
	}
	
	public static void deleteTeam(String name) {
//		if(!teamExists(name)) throw new IllegalArgumentException("Such team doesn't exist");
		deleteTeam(getTeamByName(name));
	}
	
	public static void deleteTeam(ManhuntTeam team) {
		if(team == null) return;
//		if(!teams.contains(team)) throw new IllegalArgumentException("Such team doesn't exist");
		teams.remove(team);
	}
	
	public static boolean teamExists(String name) {
		return getTeamByName(name) != null;
	}
	
	public static ManhuntTeam getTeamByName(String name) {
		for(ManhuntTeam t : teams) {
			if(t.getName().equalsIgnoreCase(name)) return t;
		}
		return null;
	}
	
	public static ManhuntTeam getTeamByPlayerNickname(String nickname) {
		for(ManhuntTeam team : teams) {
			if(team.getPlayer(nickname) != null) return team;
		}
		return null;
	}
	
	public static String getTeamNameByPlayerNickname(String nickname) {
		ManhuntTeam team = getTeamByPlayerNickname(nickname);
		return team != null ? team.getName() : null;
	}
	
	public static ManhuntPlayer getPlayer(Player p) {
		return getPlayer(p.getName());
	}
	
	public static ManhuntPlayer getPlayer(String nickname) {
		for(ManhuntTeam team : teams) {
			ManhuntPlayer p = team.getPlayer(nickname);
			if(p != null) return p;
		}
		return null;
	}
	
	public static void clearTeams() {
		teams.clear();
	}
	
	public static void resetTeams() {
		clearTeams();
		createTeam(new ManhuntTeam("hunters").setCanUseTracking(true).setCanBeTracked(false).setFrozenOnStart(true));
		createTeam(new ManhuntTeam("speedrunners").setCanUseTracking(false).setCanBeTracked(true).setFrozenOnStart(false));
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
		if(team.canBeTracked()) CompassUpdating.updateOnTargetMovement(p, p.getLocation());
		if(team.canUseTracking()) {
			CompassUtils.giveCompass(p, MainCompassType.TRACKED_TEAMS);
			CompassUpdating.updateFromTrackerRequest(p); // comment later on stable
		}
		
		team.sendMessage(FormattedMessages.playerJoinedTeamMessage(nickname), nickname);
		if(sender == p) p.sendMessage(FormattedMessages.youJoinedTeamMessage(nickname, teamName, team));
		else p.sendMessage(FormattedMessages.youWereAddedToTeamMessage(nickname, teamName, team, sender));
		return 0;
	}
	
	private static void internalRemFromOtherAndNotify(String nickname, ManhuntTeam team, CommandSender sender) {
		for(ManhuntTeam t : teams) {
			if(t != team) {
				t.removePlayer(nickname);
				t.sendMessage(FormattedMessages.playerLeftTeamMessage(nickname), nickname);
			}
		}
	}
	
	public static void removeFromTeamAndNotify(String nickname, CommandSender sender) {
		Player p = Bukkit.getPlayerExact(nickname);
		ManhuntTeam team = TeamManager.getTeamByPlayerNickname(nickname);
		log(team);
		if(team == null) {
			if(sender == p) sender.sendMessage(FormattedMessages.youAreNotInATeamYetMessage());
			else sender.sendMessage(FormattedMessages.someoneIsNotInATeamYetMessage(nickname));
			return;
		}
		String teamName = TeamManager.getTeamNameByPlayerNickname(nickname);
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
			boolean isInATeam = TeamManager.getPlayer(p.getName()) != null;
			if(toAdd && !isInATeam || !toAdd && isInATeam) players.add(p.getName());
		}
		return players;
	}
}
