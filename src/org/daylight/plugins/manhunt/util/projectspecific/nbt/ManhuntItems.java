package org.daylight.plugins.manhunt.util.projectspecific.nbt;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.daylight.plugins.manhunt.compass.TeamsManager;
import org.daylight.plugins.manhunt.structs.ManhuntTeam;
import org.daylight.plugins.manhunt.util.projectspecific.FormattedMessages;

public class ManhuntItems {
	private static String itemNameMainColor = ChatColor.AQUA + "" + ChatColor.ITALIC;
	
	public enum CompassType {
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
	
	public static CompassType getCompassType(ItemStack compass) {
		return NbtManhuntUtils.getManhuntEnumValue(CompassType.class, compass, "type");
	}
	
	public static void setCompassType(ItemStack compass, CompassType type) {
		NbtManhuntUtils.setManhuntStringValue(compass, "type", type);
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
	
	public static ItemStack createTrackingCompass() {
		ItemStack is = new ItemStack(Material.COMPASS);
		setCompassType(is, CompassType.TRACKED_TEAMS);
		setCompassTargetChoiceType(is, CompassTargetChoiceType.DISABLED);
		setCurrentTargetValueType(is, CurrentTargetValueType.NEAREST_PLAYER);
		setCurrentTargetPlayerName(is, null);
		return is;	
	}
	
	public static void updateCompassName(String playerName, ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName("Compass");
		
		ManhuntTeam team = TeamsManager.getTeamByPlayerNickname(playerName);
		if(team == null || !team.canUseTracking() || team.getListTrackingOtherTeams().size() == 0
				|| getCompassType(item) == null || getCompassType(item) == CompassType.DISABLED) {}
		else {
			if(getCurrentTargetValueType(item) == CurrentTargetValueType.NEAREST_PLAYER) {
				if(getCompassType(item) == CompassType.OWN_TEAM) {
					meta.setDisplayName(itemNameMainColor + "Tracking own team");
				}
				if(getCompassType(item) == CompassType.TRACKED_TEAMS) {
					ManhuntTeam targetTeam = team.getListTrackingOtherTeams().get(0);
					if(team.getListTrackingOtherTeams().size() == 1) meta.setDisplayName(itemNameMainColor + "Tracking " + FormattedMessages.getTeamColor(targetTeam, ChatColor.ITALIC + "") + targetTeam.getName());
					else meta.setDisplayName(itemNameMainColor + "Tracking " + team.getListTrackingOtherTeams().size() + " teams");
				}
			}
			else if(getCurrentTargetValueType(item) == CurrentTargetValueType.SPECIFIC_PLAYER) {
				if(getCurrentTargetPlayerName(item) != null) {
					String targetPlayerName = getCurrentTargetPlayerName(item);
					ManhuntTeam targetTeam = TeamsManager.getTeamByPlayerNickname(targetPlayerName);
					if(targetTeam != null) {
						meta.setDisplayName(itemNameMainColor + "Tracking " + FormattedMessages.getTeamColor(targetTeam, ChatColor.ITALIC + "") + targetPlayerName);
					}
				}
			}
		}
		
		item.setItemMeta(meta);
	}
}
