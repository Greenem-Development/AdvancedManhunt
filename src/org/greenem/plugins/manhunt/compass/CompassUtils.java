package org.greenem.plugins.manhunt.compass;

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
import org.greenem.plugins.manhunt.main.Main;
import org.greenem.plugins.manhunt.models.ManhuntTeam;
import org.greenem.plugins.manhunt.util.Stash;

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
		meta.setLodestone(null);
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
	
	public static void giveCompass(Player p) {
		Stash.AddItemStack(p, new ItemStack(Material.COMPASS), 8);
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
		if(team.canBeTracked()) CompassUpdating.updateOnMovement(p, p.getLocation());
		if(team.canUseTracking()) CompassUpdating.updateBecauseATrackerAppeared(p, inHandBypass);
	}
	
	public static void updatePositionsInATick(Player p, ManhuntTeam team, boolean inHandBypass) {
		Bukkit.getScheduler().runTaskLater(Main.getInstance().plugin, () -> {
			updatePositions(p, team, true);
		}, 1);
	}
}
