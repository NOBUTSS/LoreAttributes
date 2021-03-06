package com.pr0totype2.loreattributes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoreManager {

	private LoreAttributes plugin;

	private Pattern healthRegex;
	private Pattern regenRegex;
	private Pattern attackSpeedRegex;
	private Pattern damageValueRegex;
	private Pattern damageRangeRegex;
	private Pattern dodgeRegex;
	private Pattern critChanceRegex;
	private Pattern critDamageRegex;
	private Pattern lifestealRegex;
	private Pattern armorRegex;
	private Pattern restrictionRegex;

	private HashMap<String, Timestamp> attackLog;
	private boolean healthEnabled;
	private boolean attackSpeedEnabled;

	private Random generator;

	public LoreManager(LoreAttributes plugin) {
		this.plugin = plugin;

		generator = new Random();

		healthEnabled = false;
		attackSpeedEnabled = false;

		if (plugin.config.getBoolean("lore.health.enabled")) {
			healthEnabled = true;
			attackLog = new HashMap<String, Timestamp>();
		}

		if (plugin.config.getBoolean("lore.attack-speed.enabled")) {
			attackSpeedEnabled = true;
			attackLog = new HashMap<String, Timestamp>();
		}

		healthRegex = Pattern.compile("[+](\\d+)[ ](" + plugin.config.getString("lore.health.keyword").toLowerCase() + ")");
		regenRegex = Pattern.compile("[+](\\d+)[ ](" + plugin.config.getString("lore.regen.keyword").toLowerCase() + ")");
		attackSpeedRegex = Pattern.compile("[+](\\d+)[ ](" + plugin.config.getString("lore.attack-speed.keyword").toLowerCase() + ")");
		damageValueRegex = Pattern.compile("[+](\\d+)[ ](" + plugin.config.getString("lore.damage.keyword").toLowerCase() + ")");
		damageRangeRegex = Pattern.compile("(\\d+)(-)(\\d+)[ ](" + plugin.config.getString("lore.damage.keyword").toLowerCase() + ")");
		dodgeRegex = Pattern.compile("[+](\\d+)[%][ ](" + plugin.config.getString("lore.dodge.keyword").toLowerCase() + ")");
		critChanceRegex = Pattern.compile("[+](\\d+)[%][ ](" + plugin.config.getString("lore.critical-chance.keyword").toLowerCase() + ")");
		critDamageRegex = Pattern.compile("[+](\\d+)[ ](" + plugin.config.getString("lore.critical-damage.keyword").toLowerCase() + ")");
		lifestealRegex = Pattern.compile("[+](\\d+)[ ](" + plugin.config.getString("lore.life-steal.keyword").toLowerCase() + ")");
		armorRegex = Pattern.compile("[+](\\d+)[ ](" + plugin.config.getString("lore.armor.keyword").toLowerCase() + ")");
		restrictionRegex = Pattern.compile("(" + plugin.config.getString("lore.restriction.keyword").toLowerCase() + ": )(\\w*)");
	}

	public void disable() {
		healthEnabled = false;
		attackSpeedEnabled = false;
		if (attackLog != null) {
			attackLog.clear();
		}
	}

	public void handleArmorRestriction(Player player) {
		if (!(canUse(player, player.getInventory().getBoots()))) {
			if (player.getInventory().firstEmpty() >= 0)
				player.getInventory().addItem(player.getInventory().getBoots());
			else
				player.getWorld().dropItem(player.getLocation(), player.getInventory().getBoots());
			player.getInventory().setBoots(null);
		}

		if (!(canUse(player, player.getInventory().getChestplate()))) {
			if (player.getInventory().firstEmpty() >= 0)
				player.getInventory().addItem(player.getInventory().getChestplate());
			else
				player.getWorld().dropItem(player.getLocation(), player.getInventory().getChestplate());
			player.getInventory().setChestplate(null);
		}

		if (!(canUse(player, player.getInventory().getHelmet()))) {
			if (player.getInventory().firstEmpty() >= 0)
				player.getInventory().addItem(player.getInventory().getHelmet());
			else
				player.getWorld().dropItem(player.getLocation(), player.getInventory().getHelmet());
			player.getInventory().setHelmet(null);
		}

		if (!(canUse(player, player.getInventory().getLeggings()))) {
			if (player.getInventory().firstEmpty() >= 0)
				player.getInventory().addItem(player.getInventory().getLeggings());
			else
				player.getWorld().dropItem(player.getLocation(), player.getInventory().getLeggings());

			player.getInventory().setLeggings(null);
		}
	}

	public boolean canUse(Player player, ItemStack item) {
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher valueMatcher = restrictionRegex.matcher(allLore);
					if (valueMatcher.find()) {
						if (player.hasPermission("loreattributes." + valueMatcher.group(2))) {
							return true;
						} else {
							if (plugin.config.getBoolean("lore.restriction.display-message")) {
								player.sendMessage(plugin.config.getString("lore.restriction.message").replace("%itemname%", item.getType().toString()));
							}
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public int getDodgeBonus(LivingEntity entity) {
		Integer dodgeBonus = 0;
		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher valueMatcher = dodgeRegex.matcher(allLore);
						if (valueMatcher.find()) {
							dodgeBonus += Integer.valueOf(valueMatcher.group(1));
						}
					}
				}
			}

		}
		ItemStack item = entity.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher valueMatcher = dodgeRegex.matcher(allLore);
					if (valueMatcher.find())
						dodgeBonus += Integer.valueOf(valueMatcher.group(1));
				}
			}
		}
		return dodgeBonus;
	}

	public boolean dodgedAttack(LivingEntity entity) {
		if (!entity.isValid())
			return false;
		Integer chance = getDodgeBonus(entity);

		Integer roll = generator.nextInt(100) + 1;

		if (chance >= roll)
			return true;
		return false;
	}

	private int getCritChance(LivingEntity entity) {
		Integer chance = 0;

		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher valueMatcher = critChanceRegex.matcher(allLore);
						if (valueMatcher.find())
							chance += Integer.valueOf(valueMatcher.group(1));
					}
				}
			}

		}
		ItemStack item = entity.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher valueMatcher = critChanceRegex.matcher(allLore);
					if (valueMatcher.find()) {
						chance += Integer.valueOf(valueMatcher.group(1));
					}
				}
			}
		}
		return chance;
	}

	private boolean critAttack(LivingEntity entity) {
		if (!entity.isValid()) {
			return false;
		}
		Integer chance = getCritChance(entity);

		Integer roll = generator.nextInt(100) + 1;

		if (chance >= roll) {
			return true;
		}
		return false;
	}

	public int getArmorBonus(LivingEntity entity) {
		Integer armor = 0;

		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher valueMatcher = armorRegex.matcher(allLore);
						if (valueMatcher.find()) {
							armor += Integer.valueOf(valueMatcher.group(1));
						}
					}
				}
			}

		}
		ItemStack item = entity.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher valueMatcher = armorRegex.matcher(allLore);
					if (valueMatcher.find()) {
						armor += Integer.valueOf(valueMatcher.group(1));
					}
				}
			}
		}
		return armor;
	}

	public int getLifeSteal(LivingEntity entity) {
		Integer steal = 0;

		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher valueMatcher = lifestealRegex.matcher(allLore);
						if (valueMatcher.find()) {
							steal += Integer.valueOf(valueMatcher.group(1));
						}
					}
				}
			}

		}
		ItemStack item = entity.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher valueMatcher = lifestealRegex.matcher(allLore);
					if (valueMatcher.find()) {
						steal += Integer.valueOf(valueMatcher.group(1));
					}
				}
			}
		}
		return steal;
	}

	public int getCritDamage(LivingEntity entity) {
		if (!critAttack(entity)) {
			return 0;
		}
		Integer damage = 0;

		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher valueMatcher = critDamageRegex.matcher(allLore);
						if (valueMatcher.find()) {
							damage += Integer.valueOf(valueMatcher.group(1));
						}
					}
				}
			}

		}
		ItemStack item = entity.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher valueMatcher = critDamageRegex.matcher(allLore);
					if (valueMatcher.find())
						damage += Integer.valueOf(valueMatcher.group(1));
				}
			}
		}

		return damage;
	}

	private double getAttackCooldown(Player player) {
		if (!attackSpeedEnabled)
			return 0;
		else
			return (double) (((double) plugin.config.getDouble("lore.attack-speed.base-delay")) / getAttackSpeed(player));
	}

	public void addAttackCooldown(String playerName) {
		if (!attackSpeedEnabled)
			return;

		Timestamp able = new Timestamp((long) (new Date().getTime() + (getAttackCooldown(Bukkit.getPlayerExact(playerName)) * 1000L)));

		attackLog.put(playerName, able);
	}

	public boolean canAttack(String playerName) {
		if (!attackSpeedEnabled)
			return true;

		if (!attackLog.containsKey(playerName))
			return true;

		Date now = new Date();
		if (now.after(attackLog.get(playerName)))
			return true;

		return false;
	}

	private double getAttackSpeed(Player player) {
		if (player == null)
			return 1;

		double speed = 1;

		for (ItemStack item : player.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher valueMatcher = attackSpeedRegex.matcher(allLore);
						if (valueMatcher.find())
							speed += Integer.valueOf(valueMatcher.group(1));
					}
				}
			}

		}
		ItemStack item = player.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher valueMatcher = attackSpeedRegex.matcher(allLore);
					if (valueMatcher.find())
						speed += Integer.valueOf(valueMatcher.group(1));
				}
			}
		}

		return speed;
	}

	public void applyHpBonus(LivingEntity entity) {
		if (!healthEnabled)
			return;

		if (!entity.isValid())
			return;

		Integer hpToAdd = getHpBonus(entity);

		if (entity instanceof Player) {
			if (entity.getHealth() > (getBaseHealth((Player) entity) + hpToAdd))
				entity.setHealth(getBaseHealth((Player) entity) + hpToAdd);
			entity.setMaxHealth(getBaseHealth((Player) entity) + hpToAdd);
		}
		// else {
		// entity.resetMaxHealth();
		// }
		// entity.setMaxHealth(entity.getMaxHealth()+hpToAdd);
	}

	public int getHpBonus(LivingEntity entity) {
		if (!healthEnabled) {
			return 0;
		}

		Integer hpToAdd = 0;
		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher matcher = healthRegex.matcher(allLore);
						if (matcher.find())
							hpToAdd += Integer.valueOf(matcher.group(1));
					}
				}
			}
		}
		return hpToAdd;
	}

	public int getBaseHealth(Player player) {
		if (!healthEnabled)
			return 20;

		int hp = plugin.config.getInt("lore.health.base-health");
		/*
		 * if(LoreAttributes.config.getBoolean("integration.heroes") &&
		 * baseHPHook != null) { hp=getHeroesHealth(player);
		 */
		return hp;
	}

	public int getRegenBonus(LivingEntity entity) {
		if (!entity.isValid())
			return 0;

		Integer regenBonus = 0;
		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher matcher = regenRegex.matcher(allLore);
						if (matcher.find())
							regenBonus += Integer.valueOf(matcher.group(1));
					}
				}
			}
		}
		return regenBonus;
	}

	public int getDamageBonus(LivingEntity entity) {
		if (!entity.isValid()) {
			return 0;
		}
		Integer damageMin = 0;
		Integer damageMax = 0;
		Integer damageBonus = 0;
		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher rangeMatcher = damageRangeRegex.matcher(allLore);
						Matcher valueMatcher = damageValueRegex.matcher(allLore);
						if (rangeMatcher.find()) {
							damageMin += Integer.valueOf(rangeMatcher.group(1));
							damageMax += Integer.valueOf(rangeMatcher.group(3));
						}
						if (valueMatcher.find())
							damageBonus += Integer.valueOf(valueMatcher.group(1));
					}
				}
			}

		}
		ItemStack item = entity.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher rangeMatcher = damageRangeRegex.matcher(allLore);
					Matcher valueMatcher = damageValueRegex.matcher(allLore);
					if (rangeMatcher.find()) {
						damageMin += Integer.valueOf(rangeMatcher.group(1));
						damageMax += Integer.valueOf(rangeMatcher.group(3));
					}
					if (valueMatcher.find())
						damageBonus += Integer.valueOf(valueMatcher.group(1));
				}
			}
		}
		return (int) Math.round(Math.random() * (damageMax - damageMin) + damageMin + damageBonus + getCritDamage(entity));
	}

	public boolean useRangeOfDamage(LivingEntity entity) {
		if (!entity.isValid()) {
			return false;
		}
		for (ItemStack item : entity.getEquipment().getArmorContents()) {
			if (item != null) {
				if (item.hasItemMeta()) {
					if (item.getItemMeta().hasLore()) {
						List<String> lore = item.getItemMeta().getLore();
						String allLore = lore.toString().toLowerCase();

						Matcher rangeMatcher = damageRangeRegex.matcher(allLore);
						if (rangeMatcher.find())
							return true;
					}
				}
			}
		}
		ItemStack item = entity.getEquipment().getItemInMainHand();
		if (item != null) {
			if (item.hasItemMeta()) {
				if (item.getItemMeta().hasLore()) {
					List<String> lore = item.getItemMeta().getLore();
					String allLore = lore.toString().toLowerCase();

					Matcher rangeMatcher = damageRangeRegex.matcher(allLore);
					if (rangeMatcher.find())
						return true;
				}
			}
		}
		return false;
	}

	public void displayLoreStats(Player sender) {
		HashSet<String> message = new HashSet<String>();

		if (getHpBonus(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.health.keyword") + ": " + ChatColor.WHITE + getHpBonus(sender));

		if (getRegenBonus(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.regen.keyword") + ": " + ChatColor.WHITE + getRegenBonus(sender));

		if (plugin.config.getBoolean("lore.attack-speed.enabled"))
			message.add(ChatColor.GRAY + plugin.config.getString("lore.attack-speed.keyword") + ": " + ChatColor.WHITE + getAttackSpeed(sender));

		if (getDamageBonus(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.damage.keyword") + ": " + ChatColor.WHITE + getDamageBonus(sender));

		if (getDodgeBonus(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.dodge.keyword") + ": " + ChatColor.WHITE + getDodgeBonus(sender) + "%");

		if (getCritChance(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.critical-chance.keyword") + ": " + ChatColor.WHITE + getCritChance(sender) + "%");

		if (getCritDamage(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.critical-damage.keyword") + ": " + ChatColor.WHITE + getCritDamage(sender));

		if (getLifeSteal(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.life-steal.keyword") + ": " + ChatColor.WHITE + getLifeSteal(sender));

		if (getArmorBonus(sender) != 0)
			message.add(ChatColor.GRAY + plugin.config.getString("lore.armor.keyword") + ": " + ChatColor.WHITE + getArmorBonus(sender));

		String newMessage = "";
		for (String toSend : message) {
			newMessage = newMessage + "     " + toSend;
			if (newMessage.length() > 40) {
				sender.sendMessage(newMessage);
				newMessage = "";
			}
		}

		if (newMessage.length() > 0)
			sender.sendMessage(newMessage);

		message.clear();
	}
}
