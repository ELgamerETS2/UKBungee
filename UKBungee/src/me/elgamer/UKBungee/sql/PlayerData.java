package me.elgamer.UKBungee.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;


public class PlayerData {
	
	DataSource dataSource;
	
	public PlayerData(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public Boolean userExists(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM player_data WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			if (results.next()) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void updateName(String uuid, String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE player_data SET name=? WHERE uuid=?;"
				)){

			statement.setString(2, uuid);
			statement.setString(1, name);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void createUser(String uuid, String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO player_data(uuid, name, building_time) VALUES(?, ?, ?);"
				)){

			statement.setString(1, uuid);
			statement.setString(2, name);
			statement.setInt(3, 0);
			statement.executeUpdate();

			TextComponent message = new TextComponent("Added " + name + " to the database.");
			message.setColor(ChatColor.GREEN);
			BungeeCord.getInstance().getConsole().sendMessage(message);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public String getName(String uuid) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT name FROM player_data WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getString("name");
			} else {
				return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public String[] getNames(String[] uuids) {
		
		String[] names = new String[uuids.length];
		
		for (int i = 0; i < uuids.length; i++) {
			
			names[i] = getName(uuids[i]);
			
		}

		return names;
	}
	
	public String getUuid(String name) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM player_data WHERE name=?;"
				)){

			statement.setString(1, name);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getString("uuid");
			} else {
				return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
