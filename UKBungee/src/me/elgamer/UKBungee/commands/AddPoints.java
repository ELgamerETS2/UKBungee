package me.elgamer.UKBungee.commands;

import me.elgamer.UKBungee.Main;
import me.elgamer.UKBungee.sql.PlayerData;
import me.elgamer.UKBungee.sql.Points;
import me.elgamer.UKBungee.sql.Weekly;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class AddPoints extends Command {

	public AddPoints() {
		super("addpoints");
		TextComponent message = new TextComponent("Registered addpoints");
		message.setColor(ChatColor.GREEN);
		BungeeCord.getInstance().getConsole().sendMessage(message);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {

		if (sender instanceof ProxiedPlayer) {
			//Convert sender to ProxiedPlayer
			ProxiedPlayer p = (ProxiedPlayer) sender;

			if (!(p.hasPermission("btepoints.addpoints"))) {
				p.sendMessage(new ComponentBuilder ("You do not have permission for this command!").color(ChatColor.RED).create());  
				return;
			}
		}
		
		if (args.length != 2) {
			sender.sendMessage(new ComponentBuilder ("/addpoints <name> <points>").color(ChatColor.RED).create());
			return;
		}
		
		int value = 0;
		
		try {
			value = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage(new ComponentBuilder ("/addpoints <name> <points>").color(ChatColor.RED).create());
			return ;
		}
		
		Points points = Main.getInstance().points;
		Weekly weekly = Main.getInstance().weekly;
		PlayerData playerData = Main.getInstance().playerData;

		String uuid = playerData.getUuid(args[0]);
		
		if (uuid == null) {
			sender.sendMessage(new ComponentBuilder (args[0] + " does not exist!").color(ChatColor.RED).create());
			return;
		} else {
			points.addPoints(uuid, value);
			weekly.addPoints(uuid, value);
			sender.sendMessage(new ComponentBuilder ("Added " + args[1] + " points to " + args[0]).color(ChatColor.GREEN).create());
			return;
		}
		
	}
}
