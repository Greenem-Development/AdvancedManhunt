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
import org.daylight.plugins.manhunt.teams.ManhuntPlayer;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;
import org.daylight.plugins.manhunt.util.Stash;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems.CompassTargetChoiceType;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems.CurrentCompassTargetValueType;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.ManhuntItems.MainCompassType;
import org.daylight.plugins.manhunt.util.projectspecific.nbt.NbtManhuntUtils;

public class CompassUtils {
	/**
	 * A class/struct to store a compass configuration
	 */
	public static class CompassConfiguration {
		public MainCompassType mainType;
		public CurrentCompassTargetValueType currentTargetType;
		public String possiblyATargetNickname;
		public CompassTargetChoiceType choiceType;
		
		public CompassConfiguration(ItemStack is) {
			mainType = ManhuntItems.getCompassType(is);
			currentTargetType = ManhuntItems.getCurrentCompassTargetValueType(is);
			possiblyATargetNickname = ManhuntItems.getCurrentCompassTargetPlayerName(is);
			choiceType = ManhuntItems.getCompassTargetChoiceType(is);
		}
		
		public CompassConfiguration(MainCompassType mainType) {
			this.mainType = mainType;
		}
		
		public CompassConfiguration(MainCompassType mainType, CurrentCompassTargetValueType currentTargetType, String possiblyATargetNickname) {
			this(mainType);
			this.currentTargetType = currentTargetType;
			this.possiblyATargetNickname = possiblyATargetNickname;
		}
		
		public CompassConfiguration(MainCompassType mainType, CurrentCompassTargetValueType currentTargetType, String possiblyATargetNickname, CompassTargetChoiceType choiceType) {
			this(mainType, currentTargetType, possiblyATargetNickname);
			this.choiceType = choiceType;
		}
		
		public CompassConfiguration(MainCompassType mainType, CompassTargetChoiceType choiceType) {
			this(mainType);
			this.choiceType = choiceType;
		}
		
		public boolean matches(ItemStack is) {
			if(mainType != null && mainType != ManhuntItems.getCompassType(is)) return false;
			if(currentTargetType != null && currentTargetType != ManhuntItems.getCurrentCompassTargetValueType(is)) return false;
			if(possiblyATargetNickname != null && !possiblyATargetNickname.equals(ManhuntItems.getCurrentCompassTargetPlayerName(is))) return false;
			if(choiceType != null && choiceType != ManhuntItems.getCompassTargetChoiceType(is)) return false;
			return true;
		}
	}
	
	private static void setCompassToNotFound(Player p, ItemStack compassItem) { // good
		CompassMeta meta = (CompassMeta) compassItem.getItemMeta();
		meta.setLodestoneTracked(true);
		meta.setLodestone(p.getLocation());
		compassItem.setItemMeta(meta);
	}
	
	private static void setUsualCompass(Player p, int slot, ItemStack compassItem) { // good
//		CompassMeta meta = (CompassMeta) compassItem.getItemMeta();
//		meta.setLodestoneTracked(false);
//		try {
//			meta.setLodestone(null);
//		} catch (IllegalArgumentException e) {} // it is sometimes thrown from a new or maybe just usual compass, no idea how to manage other way
//		compassItem.setItemMeta(meta);
		p.getInventory().setItem(slot, NbtManhuntUtils.keepOnlyNameAndSection(compassItem));
	}
	
	private static void setLodestoneCompassTarget(ItemStack compassItem, Location target) { // good
		CompassMeta meta = (CompassMeta) compassItem.getItemMeta();
  		meta.setLodestoneTracked(false);
  		if(target != null) meta.setLodestone(target);
        compassItem.setItemMeta(meta);
	}
	
	private static void setTargetDependingOnCompassType(ManhuntPlayer mp, int slot, ItemStack is, Location target) {
		if(mp.getPlayer().getWorld().getEnvironment() == Environment.NORMAL && mp != null && mp.shouldBeNormalCompass(is)) {
//			debug("set usual");
			setUsualCompass(mp.getPlayer(), slot, is);
		}
		else {
//			debug("set lodestone");
			setLodestoneCompassTarget(is, target);
		}
	}
	
	public static void setTarget(Player p, Location target) {
		ManhuntPlayer mp = TeamManager.getPlayer(p.getName());
		
		if(target == null) {
			for(Entry<Integer, ? extends ItemStack> is : p.getInventory().all(Material.COMPASS).entrySet()) {
				setCompassToNotFound(p, is.getValue());
//				setTargetDependingOnCompassType(mp, is.getValue(), target);
			}
			return;
		}
		
		if(!p.getLocation().getWorld().equals(target.getWorld())) throw new IllegalArgumentException("Called setTarget with a different dimension argument");
		
		for(Entry<Integer, ? extends ItemStack> is : p.getInventory().all(Material.COMPASS).entrySet()) {
			setTargetDependingOnCompassType(mp, is.getKey(), is.getValue(), target);
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
		
		ManhuntPlayer mp = TeamManager.getPlayer(p);
		if(mainCompasasType == MainCompassType.TRACKED_TEAMS) mp.setNormalCompassParameters(new CompassConfiguration(compassTemplate));
		
		CompassUpdating.updateFromTrackerRequest(p, true);
	}
	
	public static void updateAllCompassesForPlayer(Player p) {
		for(Map.Entry<Integer, ItemStack> entry : ManhuntItems.getCompassesAll(p).entrySet()) {
			ManhuntItems.updateCompassName(p.getName(), entry.getValue());
		}
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
