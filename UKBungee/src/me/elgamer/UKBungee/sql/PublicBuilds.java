package me.elgamer.UKBungee.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.elgamer.UKBungee.Main;


public class PublicBuilds {

	//Checks whether there is a submitted plot to review.
	public static boolean reviewExists(String uuid) {

		Main instance = Main.getInstance();

		try {
			PreparedStatement statement = instance.getPublicBuilds().prepareStatement
					("SELECT * FROM " + instance.plotData + " WHERE status=?");
			statement.setString(1, "submitted");

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				if (results.getString("uuid").equals(uuid)) {
					continue;
				} else {
					return true;
				}
			}

			return false;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}
	
	//Count number of submitted plots
	public static int reviewCount(String uuid) {
		
		Main instance = Main.getInstance();
		
		try {
			PreparedStatement statement = instance.getPublicBuilds().prepareStatement
					("SELECT COUNT(*) FROM " + instance.plotData + " WHERE status=? AND uuid <> ?");
			statement.setString(1, "submitted");
			statement.setString(2, uuid);

			ResultSet results = statement.executeQuery();
			results.next();
			
			return (results.getInt(1));

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	public static boolean newSubmit() {
		
		Main instance = Main.getInstance();

		try {
			PreparedStatement statement = instance.getPublicBuilds().prepareStatement
					("SELECT * FROM " + instance.submitData);
			ResultSet results = statement.executeQuery();
			
			if (results.next()) {
				
				statement = instance.getPublicBuilds().prepareStatement
						("DELETE FROM " + instance.submitData);
				statement.executeUpdate();
				return true;
				
			} else {
				return false;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

}
