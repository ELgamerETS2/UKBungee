package me.elgamer.UKAlerts.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.elgamer.UKAlerts.Main;

public class PublicBuilds {

	//Checks whether there is a submitted plot to review.
	public static boolean reviewExists(String uuid) {

		Main instance = Main.getInstance();

		try {
			PreparedStatement statement = instance.getPublicBuilds().prepareStatement
					("SELECT * FROM " + instance.plotData + " WHERE STATUS=?");
			statement.setString(1, "submitted");

			ResultSet results = statement.executeQuery();
			while (results.next()) {
				if (results.getString("OWNER").equals(uuid)) {
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
