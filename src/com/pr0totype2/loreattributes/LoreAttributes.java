package com.pr0totype2.loreattributes;

import java.io.File;
import java.io.IOException;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import sirspoodles.centeredtext.CenteredText;

public class LoreAttributes extends JavaPlugin {

	public LoreManager loreManager;
	public LoreEvents loreevents;
	public MetricsLite metricslite;

	public Logger log = getServer().getLogger();

	public FileConfiguration config;
	public YamlConfiguration LANG;
	public File LANG_FILE;

	public void onEnable() {
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();

		if (loreManager == null)
			loreManager = new LoreManager(this);

		if (loreevents == null)
			loreevents = new LoreEvents(this);

		getServer().getPluginManager().registerEvents(this.loreevents, this);
	}

	public void onDisable() {
		HandlerList.unregisterAll(this);
	}

	public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {
		Player player = ((Player) p).getPlayer();

		if (cmd.getLabel().equalsIgnoreCase("la")) {
			if (args.length == 0) {
				player.sendMessage("");
				CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------------------------------------*");
				player.sendMessage("");
				CenteredText.sendCenteredMessage(player, ChatColor.YELLOW + ". . . " + ChatColor.GOLD + " LOREATTRIBUTESRECODED " + ChatColor.YELLOW + " . . .");
				player.sendMessage("");
				CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------------------------------------*");
				player.sendMessage("");
				player.sendMessage(ChatColor.RED + "       /la                           " + ChatColor.DARK_GRAY + "// " + ChatColor.YELLOW + "Main Command");
				player.sendMessage(ChatColor.RED + "       /la help                     " + ChatColor.DARK_GRAY + "// " + ChatColor.YELLOW + "Help Command (This page)");
				player.sendMessage(ChatColor.RED + "       /la reload                  " + ChatColor.DARK_GRAY + "// " + ChatColor.YELLOW + "Reloads configuration");
				player.sendMessage("");
				CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------- [1/1] -----------------------*");
				player.sendMessage("");
			} else if (args.length >= 0) {
				if (args[0].equalsIgnoreCase("reload")) {
					reloadConfig();
					player.sendMessage("");
					CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------------------------------------*");
					CenteredText.sendCenteredMessage(player, ChatColor.AQUA + ". . . " + ChatColor.DARK_AQUA + " LOREATTRIBUTESRECODED RELOADED" + ChatColor.AQUA + ". . .");
					CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------------------------------------*");
					player.sendMessage("");
				} else if (args[0].equalsIgnoreCase("help")) {
					player.sendMessage("");
					CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------------------------------------*");
					player.sendMessage("");
					CenteredText.sendCenteredMessage(player, ChatColor.YELLOW + ". . . " + ChatColor.GOLD + " LOREATTRIBUTESRECODED " + ChatColor.YELLOW + " . . .");
					player.sendMessage("");
					CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------------------------------------*");
					player.sendMessage("");
					player.sendMessage(ChatColor.RED + "       /la                           " + ChatColor.DARK_GRAY + "// " + ChatColor.YELLOW + "Main Command");
					player.sendMessage(ChatColor.RED + "       /la help                     " + ChatColor.DARK_GRAY + "// " + ChatColor.YELLOW + "Help Command (This page)");
					player.sendMessage(ChatColor.RED + "       /la reload                  " + ChatColor.DARK_GRAY + "// " + ChatColor.YELLOW + "Reloads configuration");
					player.sendMessage("");
					CenteredText.sendCenteredMessage(player, ChatColor.DARK_GRAY + "*---------------------- [1/1] -----------------------*");
					player.sendMessage("");
				}
			}
		}

		if (cmd.getLabel().equalsIgnoreCase("hp"))
			player.sendMessage(ChatColor.DARK_AQUA + "HEALTH: " + ChatColor.RED + player.getHealth() + ChatColor.GRAY + " / " + ChatColor.RED + player.getMaxHealth());

		if (cmd.getLabel().equalsIgnoreCase("lorestats"))
			loreManager.displayLoreStats(player);
		return false;
	}
}