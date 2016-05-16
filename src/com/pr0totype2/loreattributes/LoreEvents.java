package com.pr0totype2.loreattributes;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class LoreEvents implements Listener {

	public LoreAttributes plugin;

	public LoreEvents(LoreAttributes plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void applyOnInventoryClose(InventoryCloseEvent e) {
		plugin.loreManager.applyHpBonus(e.getPlayer());

		if (e.getPlayer() instanceof Player) {
			plugin.loreManager.handleArmorRestriction((Player) e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void applyOnPlayerLogin(PlayerJoinEvent e) {
		plugin.loreManager.applyHpBonus(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void applyOnPlayerRespawn(PlayerRespawnEvent e) {
		plugin.loreManager.applyHpBonus(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void applyOnEntityTarget(EntityTargetEvent e) {
		if (e.getEntity() instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) e.getEntity();

			plugin.loreManager.applyHpBonus(entity);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void modifyEntityDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled() || !(e.getEntity() instanceof LivingEntity)) {
			return;
		}

		if (plugin.loreManager.dodgedAttack((LivingEntity) e.getEntity())) {
			e.setDamage(0);
			e.setCancelled(true);
			return;
		}

		if (e.getDamager() instanceof LivingEntity) {
			LivingEntity damager = (LivingEntity) e.getDamager();

			if (damager instanceof Player) {
				if (plugin.loreManager.canAttack(((Player) damager).getName())) {
					plugin.loreManager.addAttackCooldown(((Player) damager).getName());
				} else {
					if (!plugin.config.getBoolean("lore.attack-speed.display-message")) {
						e.setCancelled(true);
						return;
					} else {
						((Player) damager).sendMessage(plugin.config.getString("lore.attack-speed.message"));
						e.setCancelled(true);
						return;
					}
				}
			}

			if (plugin.loreManager.useRangeOfDamage(damager)) {
				e.setDamage(Math.max(0, plugin.loreManager.getDamageBonus(damager) - plugin.loreManager.getArmorBonus((LivingEntity) e.getEntity())));
			} else {
				e.setDamage(Math.max(0, e.getDamage() + plugin.loreManager.getDamageBonus(damager) - plugin.loreManager.getArmorBonus((LivingEntity) e.getEntity())));
			}

			damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + Math.min(plugin.loreManager.getLifeSteal(damager), e.getDamage())));
		} else if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getDamager();
			if (arrow.getShooter() != null && arrow.getShooter() instanceof LivingEntity) {
				LivingEntity damager = (LivingEntity) arrow.getShooter();

				if (damager instanceof Player) {
					if (plugin.loreManager.canAttack(((Player) damager).getName())) {
						plugin.loreManager.addAttackCooldown(((Player) damager).getName());
					} else {
						if (!plugin.config.getBoolean("lore.attack-speed.display-message")) {
							e.setCancelled(true);
							return;
						} else {
							((Player) damager).sendMessage(plugin.config.getString("lore.attack-speed.message"));
							e.setCancelled(true);
							return;
						}
					}
				}

				if (plugin.loreManager.useRangeOfDamage(damager))
					e.setDamage(Math.max(0, plugin.loreManager.getDamageBonus(damager) - plugin.loreManager.getArmorBonus((LivingEntity) e.getEntity())));
				else
					e.setDamage(Math.max(0, e.getDamage() + plugin.loreManager.getDamageBonus(damager)) - plugin.loreManager.getArmorBonus((LivingEntity) e.getEntity()));

				damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + Math.min(plugin.loreManager.getLifeSteal(damager), e.getDamage())));
			}
		}
	}

	/*
	 * @EventHandler(priority=EventPriority.MONITOR) public void
	 * debugDamage(EntityDamageByEntityEvent e) { if(e.getDamager() instanceof
	 * Player && e.getEntity() instanceof LivingEntity) {
	 * ((Player)e.getDamager()).sendMessage(ChatColor.GRAY +
	 * "[Lore Debug] Dealt: " + e.getDamage() + " damage. Enemy HP: " +
	 * (((LivingEntity)e.getEntity()).getHealth()-e.getDamage()) + "/" +
	 * ((LivingEntity)e.getEntity()).getMaxHealth()); } else if(e.getDamager()
	 * instanceof Arrow) { if(((Arrow)e.getDamager()).getShooter() instanceof
	 * Player) {
	 * ((Player)((Arrow)e.getDamager()).getShooter()).sendMessage(ChatColor.GRAY
	 * + "[Lore Debug] Dealt: " + e.getDamage() + " damage. Enemy HP: " +
	 * (((LivingEntity)e.getEntity()).getHealth()-e.getDamage()) + "/" +
	 * ((LivingEntity)e.getEntity()).getMaxHealth()); } } }
	 */

	@EventHandler(priority = EventPriority.NORMAL)
	public void applyHealthRegen(EntityRegainHealthEvent e) {
		if (e.isCancelled())
			return;
		if (e.getEntity() instanceof Player) {
			if (e.getRegainReason() == RegainReason.SATIATED) {
				e.setAmount(e.getAmount() + plugin.loreManager.getRegenBonus((LivingEntity) e.getEntity()));

				if (e.getAmount() <= 0)
					e.setCancelled(true);
			}
		}
	}

	// Type related / class restriction items
	@EventHandler(priority = EventPriority.HIGHEST)
	public void checkBowRestriction(EntityShootBowEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		if (!plugin.loreManager.canUse((Player) e.getEntity(), e.getBow()))
			e.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void checkCraftRestriction(CraftItemEvent e) {
		if (!(e.getWhoClicked() instanceof Player))
			return;
		for (ItemStack item : e.getInventory().getContents()) {
			if (!plugin.loreManager.canUse((Player) e.getWhoClicked(), item)) {
				e.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void checkWeaponRestriction(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player))
			return;
		if (!plugin.loreManager.canUse((Player) e.getDamager(), ((Player) e.getDamager()).getInventory().getItemInMainHand())) {
			e.setCancelled(true);
			return;
		}
	}
}
