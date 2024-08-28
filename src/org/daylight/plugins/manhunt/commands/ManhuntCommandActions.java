package org.daylight.plugins.manhunt.commands;

import static org.daylight.plugins.manhunt.data.References.Frozen;
import static org.daylight.plugins.manhunt.data.References.FrozenLocs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.daylight.plugins.manhunt.config.ConfigData;
import org.daylight.plugins.manhunt.main.Main;

public class ManhuntCommandActions {
	public static void mhStart(int delay) {
		mhStart(delay, ConfigData.speedrunnerDelayMilliseconds);
	}
	
	private static List<String> OLD_Hunters = new ArrayList<>();
	private static List<String> OLD_Speedrunners = new ArrayList<>();
	
	@Deprecated
	public static void mhStart(int delay, int speedrunnerDelay) { // TO REMAKE, THIS IS TOO BAD LOL
		int delayInSecondsInt = (int) ConfigData.speedrunnerDelayMilliseconds/1000;
		for(String speendrunner : OLD_Speedrunners) {
			Player spPl = Bukkit.getPlayerExact(speendrunner);
			if(spPl==null) continue;
			spPl.setGameMode(GameMode.SURVIVAL);
//			disableFlightIfSurv(spPl, true);
			if(ConfigData.clearItemsOnStart) spPl.getInventory().clear();
			Frozen.add(speendrunner); // make it before and put value when back online TOD0
			FrozenLocs.put(spPl, spPl.getLocation());
			if(spPl.getGameMode().equals(GameMode.SURVIVAL) || spPl.getGameMode().equals(GameMode.ADVENTURE)) {
				if(!spPl.getGameMode().equals(GameMode.CREATIVE) && !spPl.getGameMode().equals(GameMode.SPECTATOR)) {
					spPl.setFlying(false);
					spPl.setAllowFlight(false);
				}
			}
			if(delayInSecondsInt>0) {
				Bukkit.getPlayerExact(speendrunner).sendMessage(ChatColor.YELLOW + "You have " + delayInSecondsInt + " seconds to prepare");
			} else {
				Bukkit.getPlayerExact(speendrunner).sendMessage(ChatColor.YELLOW + "You have " + (float) ConfigData.speedrunnerDelayMilliseconds/1000 + " seconds to prepare");
			}
		}
		for(String hunter: OLD_Hunters) {
			Player pHunter = Bukkit.getPlayerExact(hunter);
			if(pHunter==null) continue;
			pHunter.setGameMode(GameMode.SURVIVAL);
//			disableFlightIfSurv(pHunter, true);
			if(ConfigData.clearItemsOnStart) {
				ItemStack[] all = pHunter.getInventory().getContents();
				for (int i = 0; i < all.length; i++) {
					ItemStack is = all[i];
					if(is!=null && is.getType()!=Material.COMPASS) {
						pHunter.getInventory().setItem(i, new ItemStack(Material.AIR));
					}
				}
			}
			Frozen.add(hunter);
			FrozenLocs.put(pHunter, pHunter.getLocation());
			if(pHunter.getGameMode().equals(GameMode.SURVIVAL) || pHunter.getGameMode().equals(GameMode.ADVENTURE)) {
				if(!pHunter.getGameMode().equals(GameMode.CREATIVE) && !pHunter.getGameMode().equals(GameMode.SPECTATOR)) {
					pHunter.setFlying(false);
					pHunter.setAllowFlight(false);
				}
			}
			if(((int) ConfigData.speedrunnerDelayMilliseconds/1000)>0) {
				pHunter.sendMessage(ChatColor.GRAY + "The speedrunners have " + delayInSecondsInt + " seconds to prepare..");
				pHunter.sendTitle(ChatColor.GRAY + "Wait..", "", 5, 18*speedrunnerDelay/1000, 4);
			}
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
			@Override
			public void run() {
				for(String speendrunner : OLD_Speedrunners) {
					Frozen.remove(speendrunner);
					Player spPl = Bukkit.getPlayerExact(speendrunner);
					FrozenLocs.remove(spPl);
					if(spPl==null) continue;
					Bukkit.getPlayerExact(speendrunner).sendMessage(ChatColor.YELLOW + "Now run!");
					if(!spPl.getGameMode().equals(GameMode.CREATIVE) && !spPl.getGameMode().equals(GameMode.SPECTATOR)) {
						spPl.setFlying(false);
						spPl.setAllowFlight(false);
					}
				}
				for(String hunter: OLD_Hunters) {
					Player pHunter = Bukkit.getPlayerExact(hunter);
					//Frozen.add(hunter);
					final Player theHunter = pHunter;
//					if(theHunter==null) continue;
					Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
						@Override
						public void run() {
							Frozen.remove(theHunter.getName());
							FrozenLocs.remove(theHunter);
							if(!theHunter.getGameMode().equals(GameMode.CREATIVE) && !theHunter.getGameMode().equals(GameMode.SPECTATOR)) {
								theHunter.setFlying(false);
								theHunter.setAllowFlight(false);
							}
						}
					}, 20*delay);
					for (int i = delay; i>=0; i--) {
						final int iCopy = i;
						Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
							@Override
							public void run() {	
								if(iCopy>0) {
									if(iCopy==delay) {
										pHunter.sendTitle(ChatColor.GRAY + "The game starts in", ChatColor.GREEN + "" + iCopy, 5, 21, 0);
									}
									else {
										pHunter.sendTitle(ChatColor.GRAY + "The game starts in", ChatColor.GREEN + "" + iCopy, 0, 21, 0);
									}
								}
								else {
									pHunter.sendTitle(ChatColor.GREEN + "Go!", "", 5, 4, 7);
								}
							}
						}, 20*(delay-i));
					}
				}
			}
		}, 20 * speedrunnerDelay/1000);
	}
	
	public static void disableFlightIfSurv(Player p) {
		disableFlightIfSurv(p, false);
	}
	
	public static void disableFlightIfSurv(Player p, boolean fastMode) {
		if(fastMode || (p.getGameMode()==GameMode.SURVIVAL || p.getGameMode()==GameMode.ADVENTURE)) {
			p.setAllowFlight(false);
		}
		p.setFlying(false);
	}
}
