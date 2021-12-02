package me.elgamer.UKBungee.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import me.elgamer.UKBungee.Main;
import me.elgamer.UKBungee.utils.Leaderboard;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;


public class Points {

	DataSource dataSource;
	Weekly w;
	PlayerData playerData;

	public Points(DataSource dataSource) {
		this.dataSource = dataSource;
		this.w = Main.getInstance().weekly;
		this.playerData = Main.getInstance().playerData;
	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public void createUserIfNew(String uuid) {

		if (userExists(uuid) == false) {

			try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
					"INSERT INTO points_data(uuid, points, messages, add_points) VALUE(?, ?, ?, ?);"
					)){

				statement.setString(1, uuid);
				statement.setInt(2, 0);
				statement.setInt(3, 0);
				statement.setInt(4, 0);
				statement.executeUpdate();		

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean userExists(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM points_data WHERE uuid=?;"
				)){

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

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE points_data SET points=points+" + points + " WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void removePoints(String uuid, int points) {

		createUserIfNew(uuid);

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE points_data SET points=points-" + points + " WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void addMessage(String uuid) {

		createUserIfNew(uuid);

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE points_data SET messages=messages+" + 1 + " WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getMessageCount(String uuid) {

		createUserIfNew(uuid);

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT messages FROM points_data WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();
			results.next();

			return results.getInt("messages");

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void removeMessages(String uuid, int count) {

		createUserIfNew(uuid);

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE points_data SET messages=messages-" + count + " WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Leaderboard getOrderedPoints(String uuid, Leaderboard lead) {

		createUserIfNew(uuid);

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid, points FROM points_data ORDER BY points DESC;"
				)){

			ResultSet results = statement.executeQuery();
			int pos = 0;
			for (int i = 0; i < 9; i++) {

				if (results.next()) {
					pos += 1;
					lead.position[i] = pos;
					lead.uuids[i] = results.getString("uuid");
					lead.points[i] = results.getInt("points");
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

		createUserIfNew(uuid);

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid, points FROM points_data ORDER BY points DESC;"
				)){

			ResultSet results = statement.executeQuery();
			int pos = 0;

			while (results.next()) {
				pos += 1;
				if (results.getString("uuid").equals(uuid)) {
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
					lead.uuids[i] = results.getString("uuid");
					lead.points[i] = results.getInt("points");
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
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(Main.getInstance().getDataFolder(), "config.yml"));
			messagesPerPoint = config.getInt("messagesPerPoint");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}


		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM points_data WHERE messages>=" + messagesPerPoint + ";"
				)){

			ResultSet results = statement.executeQuery();

			while (results.next()) {

				messagesToPoints(results.getString("uuid"), messagesPerPoint);

				TextComponent message = new TextComponent("Added 1 point to " + playerData.getName(results.getString("uuid")));
				message.setColor(ChatColor.GREEN);
				BungeeCord.getInstance().getConsole().sendMessage(message);

				w.addPoints(results.getString("uuid"), 1);
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void messagesToPoints(String uuid, int messagesPerPoint) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE points_data SET points=points+" + 1 + ",messages=messages-" + messagesPerPoint + " WHERE uuid=?;"
				)){
			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void updatePoints() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM points_data WHERE add_points>=1;"
				)){

			ResultSet results = statement.executeQuery();

			while (results.next()) {

				addPointsToPoints(results.getString("uuid"), results.getInt("add_points"));
				
				TextComponent message = new TextComponent("Added " + results.getInt("add_points") + " point(s) to " + playerData.getName(results.getString("uuid")));
				message.setColor(ChatColor.GREEN);
				BungeeCord.getInstance().getConsole().sendMessage(message);

				w.addPoints(results.getString("uuid"), results.getInt("add_points"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addPointsToPoints(String uuid, int points) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE points_data SET points=points+" + points + ",add_points=" + 0 + " WHERE uuid=?;"
				)){
			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
