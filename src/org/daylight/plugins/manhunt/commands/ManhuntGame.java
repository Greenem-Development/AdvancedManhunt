package org.daylight.plugins.manhunt.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.daylight.plugins.manhunt.config.ConfigData;
import org.daylight.plugins.manhunt.events.FreezingFeature;
import org.daylight.plugins.manhunt.main.Main;
import org.daylight.plugins.manhunt.teams.ManhuntPlayer;
import org.daylight.plugins.manhunt.teams.ManhuntTeam;
import org.daylight.plugins.manhunt.teams.TeamManager;
import org.daylight.plugins.manhunt.util.Stash;

public class ManhuntGame {
	public static void mhStartGame(int delay) {
		mhStartGame(delay, ConfigData.speedrunnerDelayMilliseconds);
	}
	
	public static void mhStartGame(int mainDelaySeconds, Integer customPreTimerDurationMillis) {
		int preTimerDurationMillis = (int) ConfigData.speedrunnerDelayMilliseconds;
		if(customPreTimerDurationMillis != null) preTimerDurationMillis = customPreTimerDurationMillis;
		
		for(ManhuntTeam team : TeamManager.getAllTeams()) {
			// Heal everyone, clear items, etc.
			performGeneralPreparations(team);
			// Announce the first seconds to look around
			announcePreTimer(team, preTimerDurationMillis);
		}
		// Execute after those seconds are out
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
			for(ManhuntTeam team : TeamManager.getAllTeams()) {
				// Release freeze-immune teams and create the main timer for others
				releasePreTimerFrozenAndDoCoundown(team, mainDelaySeconds);
			}
		}, 20 * preTimerDurationMillis / 1000);
	}
	
	private static void performGeneralPreparations(ManhuntTeam team) {
		for(ManhuntPlayer mp : team.getPlayers()) {
			Player p = mp.getPlayer();
			if(p == null) continue;
			
			if(ConfigData.forceSurvivalOnStart) p.setGameMode(GameMode.SURVIVAL);
			Stash.disableFlightIfSurv(p);
			if(ConfigData.clearItemsOnStart) clearItemsOnStart(p);
			if(ConfigData.healOnStart) Stash.healPlayer(p);
			FreezingFeature.freezePlayer(p, p.getLocation()); // make it before and put value when back online TODO
		}
	}
	
	private static void announcePreTimer(ManhuntTeam team, int preStartDurationMillis) {
		if(preStartDurationMillis <= 0) return;
		for(ManhuntPlayer mp : team.getPlayers()) {
			Player p = mp.getPlayer();
			if(p == null) continue;
			
			float durationSeconds = (float) (Math.round((preStartDurationMillis / 1000) * 100.0) / 100.0);
			
			if(team.getFrozenOnStart()) {
				// For those who stays here
				p.sendMessage(ChatColor.GRAY + "The freeze-immune teams have " + durationSeconds + " seconds to prepare...");
				p.sendTitle(ChatColor.GRAY + "Wait...", "", 5, (int) (18 * durationSeconds), 4);
			} else {
				// For those who is about to leave
				p.sendMessage(ChatColor.YELLOW + "You have " + durationSeconds + " seconds to prepare");
			}
		}
	}
	
	private static void releasePreTimerFrozenAndDoCoundown(ManhuntTeam team, int mainDelaySeconds) {
		for(ManhuntPlayer mp : team.getPlayers()) {
			final Player p = mp.getPlayer();
			if(p == null) continue;
			
			if(team.getFrozenOnStart()) {
				// Unfreeze when time is out
				Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
					FreezingFeature.unfreezePlayer(p);
				}, 20 * mainDelaySeconds);
				// Schedule timer messages
				scheduleMainTimerMessages(p, mainDelaySeconds);
			} else {
				// Let those go
				FreezingFeature.unfreezePlayer(p);
				p.sendMessage(ChatColor.YELLOW + "Now go!");
			}
		}
	}
	
	private static void scheduleMainTimerMessages(Player p, int mainDelaySeconds) {
		// Scedule a message/title for each second
		for (int i = mainDelaySeconds; i >= 0; i--) {
			final int secondsLeft = i;
			Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
				// For all messages/titles except the last one
				if(secondsLeft > 0) {
					String mainTimerMessageTop = ChatColor.GRAY + "The game starts in";
					String mainTimerMessageBottom = ChatColor.GREEN + "" + secondsLeft;
					if(secondsLeft == mainDelaySeconds) {
						// Fade in
						p.sendTitle(mainTimerMessageTop, mainTimerMessageBottom, 5, 21, 0);
					}
					else {
						// Instant
						p.sendTitle(mainTimerMessageTop, mainTimerMessageBottom, 0, 21, 0);
					}
				}
				// When the time is up and we release everyone remaining
				else {
					p.sendTitle(ChatColor.GREEN + "Go!", "", 5, 4, 7);
				}
			}, 20 * (mainDelaySeconds - secondsLeft));
		}
	}
	
	private static void clearItemsOnStart(Player p) {
		ItemStack[] all = p.getInventory().getContents();
		for (int i = 0; i < all.length; i++) {
			ItemStack is = all[i];
			if(is!=null && is.getType()!=Material.COMPASS) {
				p.getInventory().setItem(i, new ItemStack(Material.AIR));
			}
		}
	}
}
