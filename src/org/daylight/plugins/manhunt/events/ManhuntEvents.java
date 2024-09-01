package org.daylight.plugins.manhunt.events;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.daylight.plugins.manhunt.compass.CompassUpdating;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;

public class ManhuntEvents implements Listener {
	@EventHandler
	public void Move(PlayerMoveEvent e) {
		String pName = e.getPlayer().getName();
		ManhuntTeam team = TeamManager.getTeamByPlayerNickname(pName);
		if(team == null) return;
		
		// Compass functionality
		if(team.canBeTracked()) CompassUpdating.updateOnTargetMovement(e.getPlayer(), e.getTo());
		// Frozen players functuonality
		if(FreezingFeature.isPlayerFrozen(pName)) cancelMovement(e);
	}
	
	@EventHandler
	public void PlayerPortalEvent(PlayerPortalEvent e) {
		CompassUpdating.onPortalEvent(e);
	}
	
	@EventHandler
	public void PlayerDies(PlayerDeathEvent e) {
		CompassUpdating.onPlayerDeath(e);
	}
	
	@EventHandler
	public void Respawn(PlayerRespawnEvent e) {
		CompassUpdating.onPlayerRespawn(e);
	}
	
	@EventHandler
	public void onScroll(PlayerItemHeldEvent e) {
		CompassUpdating.checkHotbarScrollCompassMessage(e);
	}
	
	/**
	 * Cancel a move event for freezing the player
	 * @param e The move event to cancel
	 */
	private static void cancelMovement(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(!e.getFrom().getWorld().equals(e.getTo().getWorld())) return;
		Location loc = e.getTo().clone();
		p.setAllowFlight(true);
		p.setFlying(true);
		loc.setWorld(FreezingFeature.getPlayerFreezeLocation(p.getName()).getWorld());
		loc.setX(FreezingFeature.getPlayerFreezeLocation(p.getName()).getX());
		loc.setY(FreezingFeature.getPlayerFreezeLocation(p.getName()).getY());
		loc.setZ(FreezingFeature.getPlayerFreezeLocation(p.getName()).getZ());
		p.teleport(loc);
	}
}
