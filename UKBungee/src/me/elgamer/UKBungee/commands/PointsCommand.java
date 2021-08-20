package me.elgamer.UKBungee.commands;

import me.elgamer.UKBungee.sql.PlayerData;
import me.elgamer.UKBungee.utils.Leaderboard;
import me.elgamer.UKBungee.utils.Points;
import me.elgamer.UKBungee.utils.Weekly;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PointsCommand extends Command {

	public PointsCommand() {
		super("PointsCommand");
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new ComponentBuilder ("This command can't be used from console").color(ChatColor.RED).create());  
			return;
		}

		ProxiedPlayer p = (ProxiedPlayer) sender;

		Points mysql = new Points();
		Weekly weekly = new Weekly();
		Leaderboard lead = new Leaderboard();

		if (args.length == 0) {

			lead = mysql.getPoints(p.getUniqueId().toString(), lead);

			if (lead == null) {
				p.sendMessage(new ComponentBuilder ("Not enough entries to create a leaderboard!").color(ChatColor.RED).create());  
				return;

			}

			if (lead.points[0] == 0) {
				p.sendMessage(new ComponentBuilder ("Nobody has points!").color(ChatColor.RED).create());  
				return;
			}

			p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", "#", "Points" , "Username")).color(ChatColor.WHITE).create());  
			p.sendMessage(new ComponentBuilder ("------------------------").color(ChatColor.WHITE).create());  

			String[] names = PlayerData.getNames(lead.uuids);

			for (int i = 0; i < lead.points.length; i++) {

				if (names[i] == null) {
					break;
				}
				p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , names[i])).color(ChatColor.WHITE).create());

			}
			return;
		}

		if (args.length != 1 && args.length != 2 ) {
			p.sendMessage(new ComponentBuilder ("/points top/<name> or /points weekly top/<name>").color(ChatColor.RED).create());
			return;
		}

		if (args[0].equalsIgnoreCase("top")) {
			lead = mysql.getOrderedPoints(p.getUniqueId().toString(), lead);

			if (lead == null) {
				p.sendMessage(new ComponentBuilder ("Not enough entries to create a leaderboard!").color(ChatColor.RED).create());
				return;
			}

			if (lead.points[0] == 0) {
				p.sendMessage(new ComponentBuilder ("Nobody has points!").color(ChatColor.RED).create());
				return;
			}

			p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", "#", "Points" , "Username")).color(ChatColor.WHITE).create());
			p.sendMessage(new ComponentBuilder ("------------------------").color(ChatColor.WHITE).create());

			String[] names = PlayerData.getNames(lead.uuids);

			for (int i = 0; i < lead.points.length; i++) {

				if (names[i] == null) {
					break;
				}
				p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , names[i])).color(ChatColor.WHITE).create());

			}

			return;
			
		} else if (args[0].equalsIgnoreCase("weekly")) {
			
			if (args.length == 1) {
				
				lead = weekly.getPoints(p.getUniqueId().toString(), lead);

				if (lead == null) {
					p.sendMessage(new ComponentBuilder ("Not enough entries to create a leaderboard!").color(ChatColor.RED).create());
					return;

				}

				if (lead.points[0] == 0) {
					p.sendMessage(new ComponentBuilder ("Nobody has points!").color(ChatColor.RED).create());
					return;
				}

				p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", "#", "Points" , "Username")).color(ChatColor.WHITE).create());
				p.sendMessage(new ComponentBuilder ("------------------------").color(ChatColor.WHITE).create());

				String[] names = PlayerData.getNames(lead.uuids);

				for (int i = 0; i < lead.points.length; i++) {

					if (names[i] == null) {
						break;
					}
					p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , names[i])).color(ChatColor.WHITE).create());

				}
				return;
				
			}
			
			if (args[1].equalsIgnoreCase("top")) {
				
				lead = weekly.getOrderedPoints(p.getUniqueId().toString(), lead);

				if (lead == null) {
					p.sendMessage(new ComponentBuilder ("Not enough entries to create a leaderboard!").color(ChatColor.RED).create());
					return;
				}

				if (lead.points[0] == 0) {
					p.sendMessage(new ComponentBuilder ("Nobody has points!").color(ChatColor.RED).create());
					return;
				}

				p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", "#", "Points" , "Username")).color(ChatColor.WHITE).create());
				p.sendMessage(new ComponentBuilder ("------------------------").color(ChatColor.WHITE).create());
				
				String[] names = PlayerData.getNames(lead.uuids);

				for (int i = 0; i < lead.points.length; i++) {

					if (names[i] == null) {
						break;
					}
					p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , names[i])).color(ChatColor.WHITE).create());

				}

				return;
			} else {
				
				String uuid = PlayerData.getUuid(args[1]);

				if (uuid == null) {
					p.sendMessage(new ComponentBuilder ("This Player has never connected to the server!").color(ChatColor.RED).create());
					return;
				} 

				if (weekly.userExists(uuid)) {
					lead = weekly.getPoints(uuid, lead);
				} else {
					p.sendMessage(new ComponentBuilder ("This Player has not connected to the server!").color(ChatColor.RED).create());
					return;
				}

				if (lead == null) {
					p.sendMessage(new ComponentBuilder ("Not enough entries to create a leaderboard!").color(ChatColor.RED).create());
					return;

				}

				if (lead.points[0] == 0) {
					p.sendMessage(new ComponentBuilder ("Nobody has points!").color(ChatColor.RED).create());
					return;
				}

				p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", "#", "Points" , "Username")).color(ChatColor.WHITE).create());
				p.sendMessage(new ComponentBuilder ("------------------------").color(ChatColor.WHITE).create());

				String[] names = PlayerData.getNames(lead.uuids);

				for (int i = 0; i < lead.points.length; i++) {

					if (names[i] == null) {
						break;
					}
					p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , names[i])).color(ChatColor.WHITE).create());

				}

				return;
			}
			
		} else {

			String uuid = PlayerData.getUuid(args[0]);

			if (uuid == null) {
				p.sendMessage(new ComponentBuilder ("This Player has never connected to the server!").color(ChatColor.RED).create());
				return;
			} 

			if (mysql.userExists(uuid)) {
				lead = mysql.getPoints(uuid, lead);
			} else {
				p.sendMessage(new ComponentBuilder ("This Player has not connected to the server!").color(ChatColor.RED).create());
				return;
			}

			if (lead == null) {
				p.sendMessage(new ComponentBuilder ("Not enough entries to create a leaderboard!").color(ChatColor.RED).create());
				return;

			}

			if (lead.points[0] == 0) {
				p.sendMessage(new ComponentBuilder ("Nobody has points!").color(ChatColor.RED).create());
				return;
			}

			p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", "#", "Points" , "Username")).color(ChatColor.WHITE).create());
			p.sendMessage(new ComponentBuilder ("------------------------").color(ChatColor.WHITE).create());

			String[] names = PlayerData.getNames(lead.uuids);

			for (int i = 0; i < lead.points.length; i++) {

				if (names[i] == null) {
					break;
				}
				p.sendMessage(new ComponentBuilder (String.format("%-6s%-8s%-16s", lead.position[i], lead.points[i] , names[i])).color(ChatColor.WHITE).create());

			}

			return;
		}
	}

}
