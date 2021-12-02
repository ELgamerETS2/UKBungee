package me.elgamer.UKBungee.listeners;

import java.io.File;
import java.io.IOException;

import me.elgamer.UKBungee.Main;
import me.elgamer.UKBungee.sql.Points;
import me.elgamer.UKBungee.sql.Weekly;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

public class QuitEvent implements Listener {
	
	@EventHandler
	public void onPostLogin(PlayerDisconnectEvent e) {
		
		ProxiedPlayer p = e.getPlayer();
		
		Main instance = Main.getInstance();
		
		TextComponent joinmessage = new TextComponent(p.getName() + " left UKnet");
		joinmessage.setColor(ChatColor.YELLOW);
		
		Configuration config;
		//If the value can not be accessed from the config, use the default value of 30.
		int messagesPerPoint = 30;
		
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(instance.getDataFolder(), "config.yml"));
			messagesPerPoint = config.getInt("messagesPerPoint");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Points pt = Main.getInstance().points;
		Weekly w = Main.getInstance().weekly;
		double messageCount = pt.getMessageCount(e.getPlayer().getUniqueId().toString());
		int points = (int) Math.floor(messageCount/messagesPerPoint);
		
		
		pt.addPoints(e.getPlayer().getUniqueId().toString(), points);
		w.addPoints(e.getPlayer().getUniqueId().toString(), points);
		
		pt.removeMessages(e.getPlayer().getUniqueId().toString(), points*messagesPerPoint);
		
		for (ProxiedPlayer pl : instance.getProxy().getPlayers()) {
			
			pl.sendMessage(joinmessage);
		}
		
	}

}
