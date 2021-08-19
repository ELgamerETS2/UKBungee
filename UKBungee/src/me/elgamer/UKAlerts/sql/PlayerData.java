package me.elgamer.UKAlerts.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.elgamer.UKAlerts.Main;

public class PlayerData {

	public static Boolean userExists(String uuid) {

		Main instance = Main.getInstance();
		
		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.playerData + " WHERE UUID=?");
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

	public static void updateName(String uuid, String name) {

		Main instance = Main.getInstance();
		
		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("UPDATE " + instance.playerData + " SET NAME=? WHERE UUID=?");
			statement.setString(2, uuid);
			statement.setString(1, name);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void createUser(String uuid, String name) {

		Main instance = Main.getInstance();
		
		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("INSERT INTO " + instance.playerData + " (UUID,NAME) VALUE (?,?)");
			statement.setString(1, uuid);
			statement.setString(2, name);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public static String getName(String uuid) {
		
		Main instance = Main.getInstance();
		
		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.playerData + " WHERE UUID=?");
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getString("NAME");
			} else {
				return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	public static String[] getNames(String[] uuids) {
		
		String[] names = new String[uuids.length];
		
		for (int i = 0; i < uuids.length; i++) {
			
			names[i] = getName(uuids[i]);
			
		}

		return names;
	}
	
	public static String getUuid(String name) {
		
		Main instance = Main.getInstance();
		
		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.playerData + " WHERE NAME=?");
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getString("UUID");
			} else {
				return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
