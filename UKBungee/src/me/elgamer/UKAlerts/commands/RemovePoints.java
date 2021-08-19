package me.elgamer.UKAlerts.commands;

import me.elgamer.UKAlerts.sql.PlayerData;
import me.elgamer.UKAlerts.utils.Points;
import me.elgamer.UKAlerts.utils.Weekly;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class RemovePoints extends Command {

	public RemovePoints() {
		super("RemovePoints");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		
		if (sender instanceof ProxiedPlayer) {
			//Convert sender to ProxiedPlayer
			ProxiedPlayer p = (ProxiedPlayer) sender;

			if (!(p.hasPermission("btepoints.removepoints"))) {
				sender.sendMessage(new ComponentBuilder ("You do not have permission for this command!").color(ChatColor.RED).create());  
				return;
			}
		}
		
		if (args.length != 2) {
			sender.sendMessage(new ComponentBuilder ("/removepoints <name> <points>").color(ChatColor.RED).create());  
			return;
		}
		
		int value = 0;
		
		try {
			value = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			return;
		}
		
		Points points = new Points();	
		Weekly weekly = new Weekly();
		
		String uuid = PlayerData.getUuid(args[0]);
		
		if (uuid == null) {
			sender.sendMessage(new ComponentBuilder (args[0] + " does not exist!").color(ChatColor.RED).create());  
			return;
		} else {
			points.removePoints(uuid, value);
			weekly.addPoints(uuid, value);
			sender.sendMessage(new ComponentBuilder ("Removed " + args[1] + " points from " + args[0]).color(ChatColor.GREEN).create());  
			return;
		}
		
	}
}
