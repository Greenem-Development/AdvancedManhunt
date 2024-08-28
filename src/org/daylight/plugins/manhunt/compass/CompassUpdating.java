package org.daylight.plugins.manhunt.compass;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.daylight.plugins.manhunt.structs.ManhuntPlayer;
import org.daylight.plugins.manhunt.structs.ManhuntTeam;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages;

public class CompassUpdating {
	public static Environment getEnvironment(Player p) {
		return p.getWorld().getEnvironment();
	}
	
	public static Environment getEnvironment(Location l) {
		return l.getWorld().getEnvironment();
	}
	
	public static void updateBecauseATrackerAppeared(Player tracker) {
		updateBecauseATrackerAppeared(tracker, false);
	}
	
	public static void updateBecauseATrackerAppeared(Player tracker, boolean compassInHandBypass) {
		ManhuntTeam ownTeam = TeamsManager.getTeamByPlayerNickname(tracker.getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		for(ManhuntTeam team : ownTeam.getListTrackingOtherTeams()) {
			for(ManhuntPlayer trackedMp : team.getPlayers()) {
				Player trackedP = Bukkit.getPlayerExact(trackedMp.getNickname());
				if(trackedP != null) {
					updateOnMovement(trackedP, trackedP.getLocation(), tracker, compassInHandBypass);
				}
			}
		}
	}
	
	public static void updateOnMovement(Player p, Location newLocation) {
		updateOnMovement(p, newLocation, null, false);
	}
	
	public static void updateOnMovement(Player p, Location newLocation, Player bypassForPlayer, boolean compassInHandBypass) {
		ManhuntTeam ownTeam = TeamsManager.getTeamByPlayerNickname(p.getName());
		if(ownTeam == null || !ownTeam.canBeTracked()) return;
		ManhuntPlayer mp = ownTeam.getPlayer(p.getName());
		Environment env = newLocation.getWorld().getEnvironment();
		if(env == Environment.NORMAL) {
			mp.setLastOverworldLocation(newLocation);
		} else if(env == Environment.NETHER) {
			mp.setLastNetherLocation(newLocation);
		} else if(env == Environment.THE_END) {
			mp.setLastEndLocation(newLocation);
		}
		
		for(ManhuntTeam trackedByTeam : ownTeam.getListBeingTrackedByTeams()) {
			for(ManhuntPlayer trackedByMp : trackedByTeam.getPlayers()) {
				Player trackedByP = Bukkit.getPlayerExact(trackedByMp.getNickname());
				if(trackedByP == null) continue;
				updateCompassForTracker(trackedByTeam, trackedByP, newLocation, bypassForPlayer, compassInHandBypass);
			}
		}
	}
	
	private static void updateCompassForTracker(ManhuntTeam trackedByTeam, Player trackedByP, Location newLocationn, Player bypassForPlayer, boolean compassInHandBypass) {
		if(trackedByP.getWorld().getEnvironment() != Environment.NORMAL && !(compassInHandBypass && bypassForPlayer == trackedByP) && ((trackedByP.getInventory().getItemInMainHand() != null && trackedByP.getInventory().getItemInMainHand().getType() == Material.COMPASS) || 
				(trackedByP.getInventory().getItemInOffHand() != null && trackedByP.getInventory().getItemInOffHand().getType() == Material.COMPASS))) {
			return;
		}
		Environment env = trackedByP.getLocation().getWorld().getEnvironment();
		List<Location> locations = new ArrayList<>();
		for(ManhuntTeam trackingTeam : trackedByTeam.getListTrackingOtherTeams()) {
			for(ManhuntPlayer trackingMp : trackingTeam.getPlayers()) {
				Player trackingP = Bukkit.getPlayerExact(trackingMp.getNickname());
				if(trackingP == null) continue;
				if(env == Environment.NORMAL) {
					locations.add(trackingMp.getLastOverworldLocation());
				} else if(env == Environment.NETHER) {
					locations.add(trackingMp.getLastNetherLocation());
				} else if(env == Environment.THE_END) {
					locations.add(trackingMp.getLastEndLocation());
				}
			}
		}
		
		double minDistance = Double.MAX_VALUE;
		Location bestLocation = null;
		for(Location l : locations) {
			if(l != null) {
				double newDistance = l.distance(trackedByP.getLocation());
				if(newDistance < minDistance) {
					minDistance = newDistance;
					bestLocation = l;
				}
			}
		}
		
		CompassUtils.setTarget(trackedByP, bestLocation);
	}
	
	public static void checkHotbarScrollCompassMessage(PlayerItemHeldEvent e) {
		ManhuntTeam ownTeam = TeamsManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		
		PlayerInventory inv = e.getPlayer().getInventory();
		String pName = e.getPlayer().getName();
		
		if(e.getPlayer().getWorld().getEnvironment() == Environment.NORMAL) return;
		
		if((inv.getItem(e.getNewSlot()) != null && inv.getItem(e.getNewSlot()).getType() == Material.COMPASS) && (inv.getItem(e.getPreviousSlot()) == null || inv.getItem(e.getPreviousSlot()).getType() != Material.COMPASS) ||
				(inv.getItemInOffHand() != null && inv.getItemInOffHand().getType() == Material.COMPASS)) {
//			log("checked");
			if(CompassUtils.isntInWarningList(pName)) {
//				log("added");
				CompassUtils.addToWarningList(pName);
				e.getPlayer().sendMessage(FormattedMessages.cantHoldCompassHereMessage());
				return;
			}
		}
		
		if((inv.getItem(e.getPreviousSlot()) != null && inv.getItem(e.getPreviousSlot()).getType() == Material.COMPASS) && (inv.getItem(e.getNewSlot()) == null || inv.getItem(e.getNewSlot()).getType() != Material.COMPASS) &&
				!(inv.getItemInOffHand() != null && inv.getItemInOffHand().getType() == Material.COMPASS)) {
			CompassUtils.removeFromWarningList(pName);
//			log("removed");	
			updateBecauseATrackerAppeared(e.getPlayer(), true);
		}
	}
	
	public static void onPortalEvent(PlayerPortalEvent e) {
		ManhuntTeam ownTeam = TeamsManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || (!ownTeam.canUseTracking() && !ownTeam.canBeTracked())) return;
//		log(e.getPlayer().getLocation() + " " + e.getFrom() + " " + e.getTo());
		CompassUtils.updatePositionsInATick(e.getPlayer(), ownTeam, true);
	}
	
	public static void onPlayerDeath(PlayerDeathEvent e) {
		ManhuntTeam ownTeam = TeamsManager.getTeamByPlayerNickname(e.getEntity().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		for (Iterator<ItemStack> it = e.getDrops().iterator(); it.hasNext(); ) {
		    ItemStack is = it.next();
		    if(is.getType() == Material.COMPASS)it.remove();
		}
	}
	
	public static void onPlayerRespawn(PlayerRespawnEvent e) {
		ManhuntTeam ownTeam = TeamsManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		CompassUtils.giveCompass(e.getPlayer());
		CompassUtils.updatePositionsInATick(e.getPlayer(), ownTeam, true);
	}
}
