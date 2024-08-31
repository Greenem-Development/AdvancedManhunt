package org.daylight.plugins.manhunt.compass;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.daylight.plugins.manhunt.main.Main;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.util.Stash;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems.MainCompassType;

public class CompassUtils {
	private static void setCompassToNotFound(Player p, ItemStack compassItem) { // good
		CompassMeta meta = (CompassMeta) compassItem.getItemMeta();
		meta.setLodestoneTracked(true);
		meta.setLodestone(p.getLocation());
		compassItem.setItemMeta(meta);
	}
	
	private static void setUsualCompass(ItemStack compassItem) { // good
		CompassMeta meta = (CompassMeta) compassItem.getItemMeta();
		meta.setLodestoneTracked(false);
		try {
			meta.setLodestone(null);
		} catch (IllegalArgumentException e) {} // it is sometimes thrown from a new or maybe just usual compass, no idea how to manage other way
		compassItem.setItemMeta(meta);
	}
	
	private static void setLodestoneCompass(ItemStack compassItem, Location target) { // good
		CompassMeta meta = (CompassMeta) compassItem.getItemMeta();
  		meta.setLodestoneTracked(false);
  		if(target != null) meta.setLodestone(target);
        compassItem.setItemMeta(meta);
	}
	
	public static void setTarget(Player p, Location target) {
		if(target == null) {
			for(Entry<Integer, ? extends ItemStack> is : p.getInventory().all(Material.COMPASS).entrySet()) {
				setCompassToNotFound(p, is.getValue());
			}
			return;
		}
		
		if(!p.getLocation().getWorld().equals(target.getWorld())) throw new IllegalArgumentException("Called setTarget with a different dimension argument");
		for(Entry<Integer, ? extends ItemStack> is : p.getInventory().all(Material.COMPASS).entrySet()) {
			if(p.getWorld().getEnvironment() == Environment.NORMAL) {
				setUsualCompass(is.getValue());
			} else {
				setLodestoneCompass(is.getValue(), target);
			}
		}
		if(p.getWorld().getEnvironment() == Environment.NORMAL) {
			p.setCompassTarget(target);
		}
	}
	
	public static void giveCompass(Player p, MainCompassType mainCompasasType) {
		ItemStack compassTemplate = ManhuntItems.createTrackingCompass(mainCompasasType);
		ManhuntItems.updateCompassName(p.getName(), compassTemplate);
		Map<Integer, ItemStack> existing = ManhuntItems.getCompassIfHasType(p, mainCompasasType);
		
		debug(compassTemplate.getItemMeta().getDisplayName());
		
		int stays = -1;
		if(existing.size() >= 1) {
			if(existing.containsKey(getMainCompassTypeInvPos(mainCompasasType))) stays = getMainCompassTypeInvPos(mainCompasasType);
			else stays = existing.entrySet().iterator().next().getKey();
		}
		for(Map.Entry<Integer, ItemStack> entry : existing.entrySet()) {
			if(entry.getKey() != stays) p.getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR));
		}
		
		if(stays == -1) Stash.AddItemStack(p, compassTemplate, getMainCompassTypeInvPos(mainCompasasType));
		else p.getInventory().setItem(stays, compassTemplate);
	}
	
	private static List<String> playersWarnedOfNetherCompass = new ArrayList<>();
	
	public static void removeFromWarningList(String nickname) {
		playersWarnedOfNetherCompass.remove(nickname);
	}
	
	public static boolean isntInWarningList(String nickname) {
		return !playersWarnedOfNetherCompass.contains(nickname);
	}
	
	public static void addToWarningList(String nickname) {
		playersWarnedOfNetherCompass.add(nickname);
	}
	
	public static void removeAllCompasses(Player p) {
		p.getInventory().remove(Material.COMPASS);
	}
	
	public static void updatePositions(Player p, ManhuntTeam team, boolean inHandBypass) {
		if(team.canBeTracked()) CompassUpdating.updateOnTargetMovement(p, p.getLocation());
		if(team.canUseTracking()) CompassUpdating.updateFromTrackerRequest(p, inHandBypass);
	}
	
	public static void updatePositionsInATick(Player p, ManhuntTeam team, boolean inHandBypass) {
		Bukkit.getScheduler().runTaskLater(Main.getInstance().plugin, () -> {
			updatePositions(p, team, true);
		}, 1);
	}
	
	public static int getMainCompassTypeInvPos(MainCompassType mainType) {
		switch (mainType) {
		case TRACKED_TEAMS: {
			return 8;
		}
		case OWN_TEAM: {
			return 7;
		}
		default:
			throw new IllegalArgumentException("Unexpected value in getMainCompassTypeInvLocation: " + mainType);
		}
	}
	
	public static boolean hasCompassInHand(Player p) {
		return ManhuntItems.isAManhuntCompass(p.getInventory().getItemInMainHand()) || ManhuntItems.isAManhuntCompass(p.getInventory().getItemInOffHand());
	}
}
