package me.elgamer.UKBungee.listeners;

import me.elgamer.UKBungee.Main;
import me.elgamer.UKBungee.sql.PlayerData;
import me.elgamer.UKBungee.sql.Points;
import me.elgamer.UKBungee.sql.PublicBuilds;
import me.elgamer.UKBungee.sql.Weekly;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinEvent implements Listener {

	@EventHandler
	public void onPostLogin(PostLoginEvent e) {
		
		ProxiedPlayer p = e.getPlayer();
		
		Main instance = Main.getInstance();
		
		String uuid = e.getPlayer().getUniqueId().toString();
		String name = e.getPlayer().getName();
		
		TextComponent joinmessage;
		
		PlayerData playerData = Main.getInstance().playerData;
		PublicBuilds publicBuilds = Main.getInstance().publicBuilds;
		
		if (playerData.userExists(uuid)) {
			playerData.updateName(uuid, name);
			joinmessage = new TextComponent(name + " joined UKnet");
			joinmessage.setColor(ChatColor.YELLOW);
		} else {
			playerData.createUser(uuid, name);
			joinmessage = new TextComponent(name + " joined UKnet for the first time!");
			joinmessage.setColor(ChatColor.YELLOW);
		}
		
		for (ProxiedPlayer pl : instance.getProxy().getPlayers()) {
			
			pl.sendMessage(joinmessage);
		}
			
		if (p.hasPermission("group.reviewer")) {
			
			if (publicBuilds.reviewExists(uuid)) {
				if (publicBuilds.reviewCount(uuid) == 1) {
					TextComponent message = new TextComponent("There is 1 plot available for review on the building server!");
					message.setColor(ChatColor.GREEN);
					p.sendMessage(message);
				} else {
					TextComponent message = new TextComponent("There are " + publicBuilds.reviewCount(uuid) + " plots available for review on the building server!");
					message.setColor(ChatColor.GREEN);
					p.sendMessage(message);
				}
			}
			
		}
		
		Weekly w = Main.getInstance().weekly;
		Points pt = Main.getInstance().points;
		
		pt.createUserIfNew(uuid);
		w.createUserIfNew(uuid);
		w.updateDay();
		w.updateLeader();
		
	}
	
	
}
