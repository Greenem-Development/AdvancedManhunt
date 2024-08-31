package org.daylight.plugins.manhunt.util.projectspecific.nbt;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages;

public class ManhuntItems {
	private static String itemNameMainColor = ChatColor.AQUA + "" + ChatColor.ITALIC;
	
	public enum MainCompassType {
		DISABLED,
		TRACKED_TEAMS,
		OWN_TEAM
	};
	
	public enum CompassTargetChoiceType {
		DISABLED,
		CAN_CHOOSE_TEAM,
		CAN_ONLY_CHOOSE_TEAM_PLAYER,
		CAN_CHOOSE_TEAM_AND_PLAYER
	};
	
	public enum CurrentTargetValueType {
		NEAREST_PLAYER,
		SPECIFIC_PLAYER
	};
	
	public static boolean isAManhuntCompass(ItemStack compass) {
		return compass != null && compass.getType() != null && compass.getType() == Material.COMPASS && getCompassType(compass) != null;
	}
	
	public static MainCompassType getCompassType(ItemStack compass) {
		return NbtManhuntUtils.getManhuntEnumValue(MainCompassType.class, compass, "main_type");
	}
	
	public static void setCompassType(ItemStack compass, MainCompassType type) {
		NbtManhuntUtils.setManhuntStringValue(compass, "main_type", type);
	}
	
	public static CompassTargetChoiceType getCompassTargetChoiceType(ItemStack compass) {
		return NbtManhuntUtils.getManhuntEnumValue(CompassTargetChoiceType.class, compass, "target_choice_type");
	}
	
	public static void setCompassTargetChoiceType(ItemStack compass, CompassTargetChoiceType type) {
		NbtManhuntUtils.setManhuntStringValue(compass, "target_choice_type", type);
	}
	
	public static CurrentTargetValueType getCurrentTargetValueType(ItemStack compass) {
		return NbtManhuntUtils.getManhuntEnumValue(CurrentTargetValueType.class, compass, "current_target_value_type");
	}
	
	public static void setCurrentTargetValueType(ItemStack compass, CurrentTargetValueType type) {
		NbtManhuntUtils.setManhuntStringValue(compass, "current_target_value_type", type);
	}
	
	public static String getCurrentTargetPlayerName(ItemStack compass) {
		return NbtManhuntUtils.getManhuntStringValue(compass, "current_target_player_name");
	}
	
	public static void setCurrentTargetPlayerName(ItemStack compass, String playerName) {
		NbtManhuntUtils.setManhuntStringValue(compass, "current_target_player_name", playerName);
	}
	
	public static ItemStack createTrackingCompass(MainCompassType mainType) {
		ItemStack is = new ItemStack(Material.COMPASS);
		setCompassType(is, mainType);
		setCompassTargetChoiceType(is, CompassTargetChoiceType.DISABLED);
		setCurrentTargetValueType(is, CurrentTargetValueType.NEAREST_PLAYER);
		setCurrentTargetPlayerName(is, null);
		return is;	
	}
	
	public static void updateCompassName(String playerName, ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("Compass");
		
		ManhuntTeam team = TeamManager.getTeamByPlayerNickname(playerName);
		if(team == null || !team.canUseTracking() || team.getListTrackingOtherTeams().size() == 0
				|| getCompassType(item) == null || getCompassType(item) == MainCompassType.DISABLED) {}
		else {
			if(getCurrentTargetValueType(item) == CurrentTargetValueType.NEAREST_PLAYER) {
				if(getCompassType(item) == MainCompassType.OWN_TEAM) {
					meta.setDisplayName(itemNameMainColor + "Tracking own team");
				}
				if(getCompassType(item) == MainCompassType.TRACKED_TEAMS) {
					ManhuntTeam targetTeam = team.getListTrackingOtherTeams().get(0);
					if(team.getListTrackingOtherTeams().size() == 1) meta.setDisplayName(itemNameMainColor + "Tracking " + FormattedMessages.getTeamColor(targetTeam, ChatColor.ITALIC + "") + targetTeam.getName());
					else meta.setDisplayName(itemNameMainColor + "Tracking " + team.getListTrackingOtherTeams().size() + " teams");
				}
			}
			else if(getCurrentTargetValueType(item) == CurrentTargetValueType.SPECIFIC_PLAYER) {
				if(getCurrentTargetPlayerName(item) != null) {
					String targetPlayerName = getCurrentTargetPlayerName(item);
					ManhuntTeam targetTeam = TeamManager.getTeamByPlayerNickname(targetPlayerName);
					if(targetTeam != null) {
						meta.setDisplayName(itemNameMainColor + "Tracking " + FormattedMessages.getTeamColor(targetTeam, ChatColor.ITALIC + "") + targetPlayerName);
					}
				}
			}
		}
		
		item.setItemMeta(meta);
	}
	
	public static Map<Integer, ItemStack> getCompassIfHasType(Player p, MainCompassType compassType) {
		Map<Integer, ItemStack> compasses = new HashMap<>();
		for(Map.Entry<Integer, ? extends ItemStack> entry : p.getInventory().all(Material.COMPASS).entrySet()) {
			if(getCompassType(entry.getValue()) == compassType) {
				compasses.put(entry.getKey(), entry.getValue());
			}
		}
		return compasses;
	}
	
//	public static void modifyItemStackToTemplate(ItemStack is, ItemStack template) { // Use setItem instead
//		is.setItemMeta(template.getItemMeta());
//		is.setAmount(1);
//	}
}
