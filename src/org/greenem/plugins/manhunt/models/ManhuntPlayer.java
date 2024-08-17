package org.greenem.plugins.manhunt.models;

import org.bukkit.Location;

public class ManhuntPlayer {
	private String nickname;
	private Location lastOverworldLocation = null;
	private Location lastNetherLocation = null;
	private Location lastEndLocation = null;
	private ManhuntTeam compassTargetTeam = null;
	private ManhuntTeam ownTeam = null;
	
	public ManhuntPlayer(String nickname) {
		super();
		this.nickname = nickname;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public ManhuntTeam getOwnTeam() {
		return ownTeam;
	}

	public void setOwnTeam(ManhuntTeam ownTeam) {
		this.ownTeam = ownTeam;
	}

	public Location getLastOverworldLocation() {
		return lastOverworldLocation;
	}
	
	public void setLastOverworldLocation(Location lastOverworldLocation) {
		this.lastOverworldLocation = lastOverworldLocation;
	}
	
	public Location getLastNetherLocation() {
		return lastNetherLocation;
	}
	
	public void setLastNetherLocation(Location lastNetherLocation) {
		this.lastNetherLocation = lastNetherLocation;
	}
	
	public Location getLastEndLocation() {
		return lastEndLocation;
	}
	
	public void setLastEndLocation(Location lastEndLocation) {
		this.lastEndLocation = lastEndLocation;
	}

	public ManhuntTeam getCompassTargetTeam() {
		return compassTargetTeam;
	}

	public void setCompassTargetTeam(ManhuntTeam compassTargetTeam) {
		this.compassTargetTeam = compassTargetTeam;
	}
}
