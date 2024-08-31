package org.daylight.plugins.manhunt.events;

import static org.daylight.plugins.manhunt.util.SimpleLogger.debug;
import static org.daylight.plugins.manhunt.util.SimpleLogger.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.daylight.plugins.manhunt.util.Stash;

public class FreezingFeature implements Listener {
	private static Map <String, Location> frozenPlayersSavedLocations = new HashMap<>();
	private static Map <String, Integer> frozenAirLevel = new HashMap<>();
	
	public static void freezePlayer(Player p, Location atLocation) {
		freezePlayer(p.getName(), atLocation);
	}
	
	public static void freezePlayer(String name, Location atLocation) {
		frozenPlayersSavedLocations.put(name, atLocation);
	}
	
	public static void unfreezePlayer(Player p) {
		unfreezePlayer(p.getName());
	}
	
	public static void unfreezePlayer(String name) {
		frozenPlayersSavedLocations.remove(name);
		Player p = Bukkit.getPlayerExact(name);
		if(p != null) Stash.disableFlightIfSurv(p); // TODO do this on relog during the thing too
	}
	
	public static boolean isPlayerFrozen(Player p) {
		return isPlayerFrozen(p.getName());
	}
	
	public static boolean isPlayerFrozen(String name) {
		return frozenPlayersSavedLocations.containsKey(name);
	}
	
	public static Location getPlayerFreezeLocation(Player p) {
		return getPlayerFreezeLocation(p.getName());
	}
	
	public static Location getPlayerFreezeLocation(String name) {
		if(!isPlayerFrozen(name)) return null;
		return frozenPlayersSavedLocations.get(name);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void flightAttempt(PlayerToggleFlightEvent e) {
		if(isPlayerFrozen(e.getPlayer().getName())) e.setCancelled(true);
	}
	
	@EventHandler
    public void AuthAttacking(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player Damager = (Player) e.getDamager();
			if (isPlayerFrozen(Damager.getName()) || isPlayerFrozen(e.getEntity().getName())) {
				e.setCancelled(true);
			}
		}
		if (e.getDamager() instanceof Projectile) {
            if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
            	if (isPlayerFrozen(((Player) ((Projectile) e.getDamager()).getShooter()).getName()) || isPlayerFrozen(e.getEntity().getName())) {
    				e.setCancelled(true);
    			}
    		}
        }
	}
	
	@EventHandler
    public void AuthInteracting(PlayerInteractEntityEvent e) {
		if(isPlayerFrozen(e.getPlayer().getName())) {
			e.setCancelled(true);
        }
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void EntityTargetEvent(EntityTargetEvent e) {
		if(e.getTarget() instanceof Player) {
			Player p = (Player) e.getTarget();
	        if (e.getTarget() instanceof LivingEntity){
	            if(isPlayerFrozen(p.getName())) {
	            	e.setCancelled(true);
	            }
	        }
		}
	}
	
	public void PlayerPickupItemEvent(PlayerPickupArrowEvent e) { //cancelling picking an arrow up
		if(isPlayerFrozen(e.getPlayer().getName())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
    public void onHungingBrake(HangingBreakByEntityEvent e) {
		if(e.getRemover().getType() == EntityType.PLAYER){
			if(isPlayerFrozen(e.getRemover().getName())) {
				e.setCancelled(true);
    		}
    	}
    }
	
	@EventHandler
	public void PlayerDropItemEvent (PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if(isPlayerFrozen(p.getName())) {
			e.setCancelled(true);
		}
	}
	
	public void PlayerHitsPlayerEvt(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player) {
			if(isPlayerFrozen(e.getDamager().getName()) || isPlayerFrozen(e.getEntity().getName())) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void DisableArmorStandIntaract (PlayerInteractAtEntityEvent e) {
		if(isPlayerFrozen(e.getPlayer().getName())) {
			if(e.getRightClicked() instanceof ArmorStand) {
				e.setCancelled(true);
			}
		}
		if(e.getRightClicked() instanceof ArmorStand) {
			if(isPlayerFrozen(e.getPlayer().getName())) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void DamageRulesManager(EntityDamageEvent e) {
		//log.info(e.getEntity().getType() + "");
		if (e.getEntity().getType().toString().equals("PLAYER")){
			if(isPlayerFrozen(((Player) e.getEntity()).getName())) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void BlockPlaceEvent(BlockPlaceEvent e) {
		if(isPlayerFrozen(e.getPlayer().getName())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void BlockBreakEvent(BlockBreakEvent e) {
		if(isPlayerFrozen(e.getPlayer().getName())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void PlayerInteractEvent(PlayerInteractEvent e){
		Player p = e.getPlayer();
//        Block b = e.getClickedBlock();
        //if(e.getClickedBlock() != null) log.info(e.getClickedBlock().getType().toString());
        if(isPlayerFrozen(p.getName())) {
        	e.setCancelled(true);
        }
	}
	
	@EventHandler
	public void FoodLevelChangeEvent(FoodLevelChangeEvent e) {
		if(e.getEntity() instanceof Player) {
			if(isPlayerFrozen(((Player) e.getEntity()).getName())){
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void EntityAirChangeEvent(EntityAirChangeEvent e) {
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(isPlayerFrozen(((Player) e.getEntity()).getName())) {
				if(!frozenAirLevel.containsKey(p.getName())) frozenAirLevel.put(p.getName(), p.getRemainingAir());
				if(e.getAmount()<frozenAirLevel.get(p.getName())) {
					//e.setAmount(-300);
					e.setAmount(frozenAirLevel.get(p.getName()));
				}
			}
		}
	}
	
	//DESABLED DUE TO 1.18 REMOVED IT
	/*@EventHandler
	public void EEntityDismount(EntityDismountEvent e) {
		if(e.getEntity() instanceof Player) {
			//log.info("EntityDismountEvent");
			if(Frozen.contains(((Player) e.getEntity()).getName())){
				e.setCancelled(true);
			}
		}
	}*/
}
