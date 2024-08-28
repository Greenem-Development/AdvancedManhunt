package org.daylight.plugins.manhunt.teams;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ManhuntTeam {
	private String name;
	private ChatColor color;
	private List<ManhuntPlayer> players = new ArrayList<>();
	private boolean canUseTracking = true;
	private boolean canBeTracked = true;
	private boolean frozenOnStart = false;
	
	private List<ManhuntTeam> beingTrackedByTeams = new ArrayList<>(); // can later be implemented with a trackableAndTracking interface lists
	private List<ManhuntTeam> trackingTeams = new ArrayList<>();
	
	public ManhuntTeam(String name) {
		super();
		this.name = name;
//		this.canUseTracking = canUseTracking;
//		this.canBeTracked = canBeTracked;
	}
	
	public void addPlayer(ManhuntPlayer p) {
		if(getPlayer(p.getNickname()) != null) return;
		players.add(p);
		p.setOwnTeam(this);
//		debug("Added: " + getPlayer(p.getNickname()) != null);
	}
	
	public void removePlayer(ManhuntPlayer p) {
		if(!players.contains(p)) return;
		players.remove(p);
		p.setOwnTeam(null);
	}
	
	public void removePlayer(String nickName) {
		ManhuntPlayer p = getPlayer(nickName);
		if(p == null) return;
		players.remove(p);
		p.setOwnTeam(null);
	}
	
	public void setPlayers(List<ManhuntPlayer> players) {
		for(ManhuntPlayer p : this.players) {
			p.setOwnTeam(null);
		}
		this.players = players;
	}

	public ManhuntPlayer getPlayer(String nickname) {
		for(ManhuntPlayer p : players) {
			if(p.getNickname().equals(nickname)) return p;
		}
		return null;
	}

	public boolean canUseTracking() {
		return canUseTracking;
	}

	public ManhuntTeam setCanUseTracking(boolean canUseTracking) {
		this.canUseTracking = canUseTracking;
		return this;
	}

	public boolean canBeTracked() {
		return canBeTracked;
	}

	public ManhuntTeam setCanBeTracked(boolean canBeTracked) {
		this.canBeTracked = canBeTracked;
		return this;
	}
	
	public boolean getFrozenOnStart() {
		return frozenOnStart;
	}

	public ManhuntTeam setFrozenOnStart(boolean frozenOnStart) {
		this.frozenOnStart = frozenOnStart;
		return this;
	}
	
	public void sendMessage(String message) {
		sendMessage(message, null);
	}
	
	public void sendMessage(String message, String exceptPlayer) {
		for(ManhuntPlayer mp : players) {
			if(exceptPlayer == null || !mp.getNickname().equals(exceptPlayer)) {
				Player p = Bukkit.getPlayerExact(mp.getNickname());
				if(p != null) p.sendMessage(message);
			}
		}
	}
	
	public void addTeamToTrack(ManhuntTeam team) {
		trackingTeams.add(team);
		team.internalAddBeingTrackedByTeam(this);
	}
	
	public void stopTrackingTeam(ManhuntTeam team) {
		trackingTeams.remove(team);
		team.internalStopBeingTrackedByTeam(this);
	}
	
	public void internalAddBeingTrackedByTeam(ManhuntTeam team) {
		beingTrackedByTeams.add(team);
	}
	
	public void internalStopBeingTrackedByTeam(ManhuntTeam team) {
		beingTrackedByTeams.remove(team);
	}

	public List<ManhuntTeam> getListBeingTrackedByTeams() {
		return beingTrackedByTeams;
	}

	public List<ManhuntTeam> getListTrackingOtherTeams() {
		return trackingTeams;
	}

	public List<ManhuntPlayer> getPlayers() {
		return players;
	}

	public String getName() {
		return name;
	}

	public ChatColor getColor() {
		return color;
	}
}
