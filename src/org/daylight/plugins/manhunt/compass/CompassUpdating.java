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
	
	/**
	 * A shortcut
	 */
	public static void updateFromTrackerRequest(Player tracker) {
		updateFromTrackerRequest(tracker, false);
	}
	
	/**
	 * Updates the compass of the tracker with no need for the targets to move
	 * @param tracker The tracker player
	 * @param compassInHandBypass If should ignore the "no compass in hand in non-overworld" rule
	 */
	public static void updateFromTrackerRequest(Player tracker, boolean compassInHandBypass) {
		ManhuntTeam trackerTeam = TeamManager.getTeamByPlayerNickname(tracker.getName());
		if(trackerTeam == null || !trackerTeam.canUseTracking()) return;
		ManhuntPlayer trackerMp = trackerTeam.getPlayer(tracker);
		if(!trackerMp.isOnline()) return;
		
		// Determine new compass state for that tracker and set the compass data
		setTargetForTracker(trackerTeam, trackerMp.getPlayer(), tracker, compassInHandBypass);
		
//		for(ManhuntTeam team : trackerTeam.getListTrackingOtherTeams()) {
//			for(ManhuntPlayer trackedMp : team.getPlayers()) {
//				Player trackedP = trackedMp.getPlayer();
//				if(trackedP != null) {
//					updateOnMovement(trackedP, trackedP.getLocation(), tracker, compassInHandBypass);
//				}
//			}
//		}
	}
	
	/**
	 * A shortcut
	 */
	public static void updateOnTargetMovement(Player p, Location newLocation) {
		updateOnTargetMovement(p, newLocation, null, false);
	}
	
	/**
	 * Save new location of a target in it's ManhuntPlayer object in the corresponding dimension's "last location"
	 * @param mp The ManhuntPlayer object
	 * @param newLocation The new actual location of the player
	 */
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
	
	/**
	 * Update all trackers' compasses when a target has moved
	 * Please note that even trackers can be targets, this method is just for any movement of any kind of target
	 * @param p The player who moved / the target
	 * @param newLocation The new location of the target (can not only be p.getLocation, but also a location from an event)
	 * @param bypassForPlayer The player the "compass in hand bypass" should be active for
	 * @param compassInHandBypass If should ignore the "no compass in hand in non-overworld" rule
	 */
	public static void updateOnTargetMovement(Player p, Location newLocation, Player bypassForPlayer, boolean compassInHandBypass) {
		ManhuntTeam trackedTeam = TeamManager.getTeamByPlayerNickname(p.getName());
		if(trackedTeam == null || !trackedTeam.canBeTracked()) return;
		
		// Set new coordinates and dimension
		updateTrackedPlayerLocation(trackedTeam.getPlayer(p.getName()), newLocation);
		
		// Iterate over all teams that can track this player
		for(ManhuntTeam trackerTeam : trackedTeam.getListBeingTrackedByTeams()) {
			// Iterate over their members
			for(ManhuntPlayer trackerMp : trackerTeam.getPlayers()) {
				if(!trackerMp.isOnline()) continue;
				
				// Determine new compass state for that tracker and set the compass data
				setTargetForTracker(trackerTeam, trackerMp.getPlayer(), bypassForPlayer, compassInHandBypass);
			}
		}
	}
	
	/**
	 * Add the last target's location in the dimension to the list. May add nulls, but they are ignored later
	 * @param trackerEnv The dimension to search in
	 * @param targetMp The target ManhuntPlayer to get the info about
	 * @param resultList The list to add the result to
	 */
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
	
	/**
	 * Chooses the closest privided location to a specified location
	 * @param locations A list of possible options
	 * @param trackerLocation The location the distance is measured from
	 * @return
	 */
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
	
	/**
	 * Internal/private method, searches for the best compass target for the tracker and sets it
	 * @param trackerTeam The team of the tracker
	 * @param trackerP The tracker player
	 * @param bypassForPlayer The player the "compass in hand bypass" should be active for
	 * @param compassInHandBypass If should ignore the "no compass in hand in non-overworld" rule
	 */
	private static void setTargetForTracker(ManhuntTeam trackerTeam, Player trackerP, Player bypassForPlayer, boolean compassInHandBypass) {
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
	
	/**
	 * Check the hotbar scroll event and notify the player about the "compass held in not overworld" if needed
	 * @param e The event of hotbar scrolling
	 */
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
			updateFromTrackerRequest(e.getPlayer(), true);
		}
	}
	
	/**
	 * The handler of the portal event. Updates the target's position OR the tracker's compass a tick later, even if no one moves, but has been teleported to a different dimention.
	 * If it was done in the same tick, either dimention or the coords would be the old ones, don't remember exactly.
	 * @param e The portal event
	 */
	public static void onPortalEvent(PlayerPortalEvent e) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || (!ownTeam.canUseTracking() && !ownTeam.canBeTracked())) return;
		
//		log(e.getPlayer().getLocation() + " " + e.getFrom() + " " + e.getTo());
		CompassUtils.updatePositionsInATick(e.getPlayer(), ownTeam, true);
	}
	
	/**
	 * The handler of the death event. Removes the compass if needed
	 * @param e The death event
	 */
	public static void onPlayerDeath(PlayerDeathEvent e) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(e.getEntity().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		
		for (Iterator<ItemStack> it = e.getDrops().iterator(); it.hasNext(); ) {
		    ItemStack is = it.next();
		    if(ManhuntItems.isAManhuntCompass(is)) it.remove();
		}
	}
	
	/**
	 * The handler of the respawn event. Gives the compass if needed
	 * @param e The respawn event
	 */
	public static void onPlayerRespawn(PlayerRespawnEvent e) {
		ManhuntTeam ownTeam = TeamManager.getTeamByPlayerNickname(e.getPlayer().getName());
		if(ownTeam == null || !ownTeam.canUseTracking()) return;
		CompassUtils.giveCompass(e.getPlayer(), MainCompassType.TRACKED_TEAMS);
		CompassUtils.updatePositionsInATick(e.getPlayer(), ownTeam, true);
	}
}
