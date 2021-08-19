package me.elgamer.UKAlerts.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.elgamer.UKAlerts.Main;

public class Points {

	Main instance = Main.getInstance(); 

	public void createUserIfNew(String uuid) {

		if (userExists(uuid) == false) {

			try {

				PreparedStatement statement = instance.getPoints().prepareStatement
						("INSERT INTO " + instance.pointsData + " (UUID,POINTS,MESSAGES) VALUE (?,?,?)");
				statement.setString(1, uuid);
				statement.setInt(2, 0);
				statement.setInt(3, 0);
				statement.executeUpdate();		

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean userExists(String uuid) {

		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.pointsData + " WHERE UUID=?");
			statement.setString(1, uuid);

			ResultSet results = statement.executeQuery();

			return (results.next());

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public void addPoints(String uuid, int points) {

		createUserIfNew(uuid);

		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("UPDATE " + instance.pointsData + " SET POINTS=POINTS+" + points + " WHERE UUID=?");
			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	public void removePoints(String uuid, int points) {

		createUserIfNew(uuid);

		try {

			PreparedStatement statement = instance.getPoints().prepareStatement
					("UPDATE " + instance.pointsData + " SET POINTS=POINTS-" + points + " WHERE UUID=?");
			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void addMessage(String uuid) {

		try {

			createUserIfNew(uuid);

			PreparedStatement statement = instance.getPoints().prepareStatement
					("UPDATE " + instance.pointsData + " SET MESSAGES=MESSAGES+" + 1 + " WHERE UUID=?");
			statement.setString(1, uuid);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getMessageCount(String uuid) {
		try {
			createUserIfNew(uuid);
			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.pointsData + " WHERE UUID=?");
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();
			results.next();

			return results.getInt("MESSAGES");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void removeMessages(String uuid, int count) {
		try {
			createUserIfNew(uuid);
			PreparedStatement statement = instance.getPoints().prepareStatement
					("UPDATE " + instance.pointsData + " SET MESSAGES=MESSAGES-" + count + " WHERE UUID=?");
			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Leaderboard getOrderedPoints(String uuid, Leaderboard lead) {

		try {
			createUserIfNew(uuid);
			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.pointsData + " ORDER BY POINTS DESC");
			ResultSet results = statement.executeQuery();
			int pos = 0;
			for (int i = 0; i < 9; i++) {

				if (results.next()) {
					pos += 1;
					lead.position[i] = pos;
					lead.uuids[i] = results.getString("UUID");
					lead.points[i] = results.getInt("POINTS");
				} else {
					return lead;
				}

			}

			return lead;

		} catch (SQLException e) {
			e.printStackTrace();
			return lead;
		}
	}
	
	public Leaderboard getPoints(String uuid, Leaderboard lead) {

		try {
			createUserIfNew(uuid);
			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.pointsData + " ORDER BY POINTS DESC");
			ResultSet results = statement.executeQuery();
			int pos = 0;
			
			while (results.next()) {
				pos += 1;
				if (results.getString("UUID").equals(uuid)) {
					break;
				}
			}
			
			for (int j = 0; j < 5; j++) {
				pos -= 1;
				if (results.previous()) {
				} else {
					break;
				}
			}
			
			for (int i = 0; i < 9; i++) {

				if (results.next()) {
					pos += 1;
					lead.position[i] = pos;
					lead.uuids[i] = results.getString("UUID");
					lead.points[i] = results.getInt("POINTS");
				} else {
					return lead;
				}

			}

			return lead;

		} catch (SQLException e) {
			e.printStackTrace();
			return lead;
		}
	}
}
