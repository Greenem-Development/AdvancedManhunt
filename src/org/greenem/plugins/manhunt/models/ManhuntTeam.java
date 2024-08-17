package org.greenem.plugins.manhunt.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ManhuntTeam {
	private List<ManhuntPlayer> players = new ArrayList<>();
	private boolean canUseTracking = true;
	private boolean canBeTracked = true;
	
	private List<ManhuntTeam> beingTrackedByTeams = new ArrayList<>(); // can later be implemented with a trackableAndTracking interface lists
	private List<ManhuntTeam> trackingTeams = new ArrayList<>();
	
	public ManhuntTeam(boolean canUseTracking, boolean canBeTracked) {
		super();
		this.canUseTracking = canUseTracking;
		this.canBeTracked = canBeTracked;
	}
	
	public void addPlayer(ManhuntPlayer p) {
		players.add(p);
		p.setOwnTeam(this);
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

	public void setCanUseTracking(boolean canUseTracking) {
		this.canUseTracking = canUseTracking;
	}

	public boolean canBeTracked() {
		return canBeTracked;
	}

	public void setCanBeTracked(boolean canBeTracked) {
		this.canBeTracked = canBeTracked;
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
		team.addBeingTrackedByTeam(this);
	}
	
	public void stopTrackingTeam(ManhuntTeam team) {
		trackingTeams.remove(team);
		team.stopBeingTrackedByTeam(this);
	}
	
	public void addBeingTrackedByTeam(ManhuntTeam team) {
		beingTrackedByTeams.add(team);
	}
	
	public void stopBeingTrackedByTeam(ManhuntTeam team) {
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
}
