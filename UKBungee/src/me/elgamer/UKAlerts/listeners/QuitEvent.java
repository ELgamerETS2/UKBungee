package me.elgamer.UKAlerts.listeners;

import me.elgamer.UKAlerts.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class QuitEvent implements Listener {
	
	@EventHandler
	public void onPostLogin(PlayerDisconnectEvent e) {
		
		ProxiedPlayer p = e.getPlayer();
		
		Main instance = Main.getInstance();
		
		TextComponent joinmessage = new TextComponent(p.getName() + " left UKnet");
		joinmessage.setColor(ChatColor.YELLOW);
		
		for (ProxiedPlayer pl : instance.getProxy().getPlayers()) {
			
			pl.sendMessage(joinmessage);
		}
		
	}

}
