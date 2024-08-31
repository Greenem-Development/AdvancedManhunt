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
import org.daylight.plugins.manhunt.teams.ManhuntPlayer;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems.MainCompassType;

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
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(tracker.getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		for(ManhuntTeam team : ownTeam.getListTrackingOtherTeams()) {
			for(ManhuntPlayer trackedMp : team.getPlayers()) {
				Player trackedP = trackedMp.getPlayer();
				if(trackedP != null) {
					updateOnMovement(trackedP, trackedP.getLocation(), tracker, compassInHandBypass);
				}
			}
		}
	}
	
	public static void updateOnMovement(Player p, Location newLocation) {
		updateOnMovement(p, newLocation, null, false);
	}
	
	private static void updateTrackedPlayerLocation(ManhuntPlayer mp, Location newLocation) {
		Environment env = newLocation.getWorld().getEnvironment();
		if(env == Environment.NORMAL) {
			mp.setLastOverworldLocation(newLocation);
		} else if(env == Environment.NETHER) {
			mp.setLastNetherLocation(newLocation);
		} else if(env == Environment.THE_END) {
			mp.setLastEndLocation(newLocation);
		}
	}
	
	public static void updateOnMovement(Player p, Location newLocation, Player bypassForPlayer, boolean compassInHandBypass) {
		ManhuntTeam trackedTeam = TeamManager.getTeamByPlayerNickname(p.getName());
		if(trackedTeam == null || !trackedTeam.canBeTracked()) return;
		
		// Set new coordinates and dimension
		updateTrackedPlayerLocation(trackedTeam.getPlayer(p.getName()), newLocation);
		
		// Iterate over all teams that can track this player
		for(ManhuntTeam trackerTeam : trackedTeam.getListBeingTrackedByTeams()) {
			// Iterate over their members
			for(ManhuntPlayer trackerMp : trackerTeam.getPlayers()) {
				Player trackerP = trackerMp.getPlayer();
				if(trackerP == null) continue;
				
				// Determine new compass state for that tracker and set the compass data
				updateCompassForTracker(trackerTeam, trackerP, newLocation, bypassForPlayer, compassInHandBypass);
			}
		}
	}
	
	private static void addTargetLocationToList(Environment trackerEnv, ManhuntPlayer targetMp, List<Location> resultList) {
		if(!targetMp.isOnline()) return;
		
		if(trackerEnv == Environment.NORMAL) {
			resultList.add(targetMp.getLastOverworldLocation());
		} else if(trackerEnv == Environment.NETHER) {
			resultList.add(targetMp.getLastNetherLocation());
		} else if(trackerEnv == Environment.THE_END) {
			resultList.add(targetMp.getLastEndLocation());
		}
	}
	
	private static Location pickBestLocation(List<Location> locations, Location trackerLocation) {
		double minDistance = Double.MAX_VALUE;
		Location bestLocation = null;
		
		for(Location l : locations) {
			if(l == null) continue;
			double newDistance = l.distance(trackerLocation);
			if(newDistance < minDistance) {
				minDistance = newDistance;
				bestLocation = l;
			}
		}
		
		return bestLocation;
	}
	
	private static void updateCompassForTracker(ManhuntTeam trackerTeam, Player trackerP, Location newLocationn, Player bypassForPlayer, boolean compassInHandBypass) {
		// Return if not the overworld and held in hand
		if(trackerP.getWorld().getEnvironment() != Environment.NORMAL &&
				!(compassInHandBypass && bypassForPlayer == trackerP) && CompassUtils.hasCompassInHand(trackerP)) return;
		
		// Create a list of all possible target locations
		List<Location> locations = new ArrayList<>();
		for(ManhuntTeam trackingTeam : trackerTeam.getListTrackingOtherTeams()) {
			for(ManhuntPlayer trackingMp : trackingTeam.getPlayers()) {
				addTargetLocationToList(trackerP.getLocation().getWorld().getEnvironment(), trackingMp, locations);
			}
		}
		
		// Pick the best location and set it
		CompassUtils.setTarget(trackerP, pickBestLocation(locations, trackerP.getLocation()));
	}
	
	public static void checkHotbarScrollCompassMessage(PlayerItemHeldEvent e) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		
		PlayerInventory inv = e.getPlayer().getInventory();
		String pName = e.getPlayer().getName();
		
		if(e.getPlayer().getWorld().getEnvironment() == Environment.NORMAL) return;
		
		if(ManhuntItems.isAManhuntCompass(inv.getItem(e.getNewSlot())) && !ManhuntItems.isAManhuntCompass(inv.getItem(e.getPreviousSlot())) && CompassUtils.hasCompassInHand(e.getPlayer())) {
			if(CompassUtils.isntInWarningList(pName)) {
				CompassUtils.addToWarningList(pName);
				e.getPlayer().sendMessage(FormattedMessages.cantHoldCompassHereMessage());
				return;
			}
		}
		
		if(!ManhuntItems.isAManhuntCompass(inv.getItem(e.getNewSlot())) && ManhuntItems.isAManhuntCompass(inv.getItem(e.getPreviousSlot())) && !CompassUtils.hasCompassInHand(e.getPlayer())) {
			CompassUtils.removeFromWarningList(pName);
			updateBecauseATrackerAppeared(e.getPlayer(), true);
		}
	}
	
	public static void onPortalEvent(PlayerPortalEvent e) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || (!ownTeam.canUseTracking() && !ownTeam.canBeTracked())) return;
//		log(e.getPlayer().getLocation() + " " + e.getFrom() + " " + e.getTo());
		CompassUtils.updatePositionsInATick(e.getPlayer(), ownTeam, true);
	}
	
	public static void onPlayerDeath(PlayerDeathEvent e) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(e.getEntity().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		for (Iterator<ItemStack> it = e.getDrops().iterator(); it.hasNext(); ) {
		    ItemStack is = it.next();
		    if(is.getType() == Material.COMPASS) it.remove();
		}
	}
	
	public static void onPlayerRespawn(PlayerRespawnEvent e) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		CompassUtils.giveCompass(e.getPlayer(), MainCompassType.TRACKED_TEAMS);
		CompassUtils.updatePositionsInATick(e.getPlayer(), ownTeam, true);
	}
}
