package me.elgamer.UKBungee.utils;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import me.elgamer.UKBungee.Main;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;


public class Points {

	Main instance = Main.getInstance(); 
	Weekly w = new Weekly();

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

	public void updateMessages() {

		Configuration config;
		int messagesPerPoint = 30;
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(instance.getDataFolder(), "config.yml"));
			messagesPerPoint = config.getInt("messagePerPoint");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		PreparedStatement statement;

		try {

			statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.pointsData + " WHERE MESSAGES>=" + messagesPerPoint);
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				statement = instance.getPoints().prepareStatement
						("UPDATE " + instance.pointsData + " SET POINTS=POINTS+" + 1 + ",MESSAGES=MESSAGES-" + messagesPerPoint + " WHERE UUID=?");
				statement.setString(1,results.getString("UUID"));
				statement.executeUpdate();

				w.addPoints(results.getString("UUID"), 1);
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void updatePoints() {

		PreparedStatement statement;

		try {

			statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + instance.pointsData + " WHERE ADD_POINTS>=1");
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				statement = instance.getPoints().prepareStatement
						("UPDATE " + instance.pointsData + " SET POINTS=POINTS+" + results.getInt("ADD_POINTS") + ",ADD_POINTS=" + 0 + " WHERE UUID=?");
				statement.setString(1,results.getString("UUID"));
				statement.executeUpdate();

				w.addPoints(results.getString("UUID"), results.getInt("ADD_POINTS"));
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
