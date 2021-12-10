package me.elgamer.UKBungee.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import me.elgamer.UKBungee.Main;
import me.elgamer.UKBungee.utils.GetDay;
import me.elgamer.UKBungee.utils.Leaderboard;
import net.md_5.bungee.api.ProxyServer;

public class Weekly {

	DataSource dataSource;
	PlayerData playerData;

	public Weekly(DataSource dataSource) {
		this.dataSource = dataSource;
		this.playerData = Main.getInstance().playerData;
	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public void createUserIfNew(String uuid) {

		if (userExists(uuid) == false) {

			try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
					"INSERT INTO weekly_data(uuid, points, monday, tuesday, wednesday, thursday, friday, saturday, sunday) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"
					)){

				statement.setString(1, uuid);
				statement.setInt(2, 0);
				statement.setInt(3, 0);
				statement.setInt(4, 0);
				statement.setInt(5, 0);
				statement.setInt(6, 0);
				statement.setInt(7, 0);
				statement.setInt(8, 0);
				statement.setInt(9, 0);
				statement.executeUpdate();		

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean userExists(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM weekly_data WHERE uuid=?;"
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

		updateDay();
		createUserIfNew(uuid);
		int day = GetDay.getDay();

		String dayString = null;


		switch (day) {

		case 1: 
			dayString = "monday";
			break;
		case 2: 
			dayString = "tuesday";
			break;
		case 3:
			dayString = "wednesday";
			break;
		case 4: 
			dayString = "thursday";
			break;
		case 5: 
			dayString = "friday";
			break;
		case 6: 
			dayString = "saturday";
			break;
		case 7: 
			dayString = "sunday";
			break;

		}

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE weekly_data SET points=points+" + points + ", " + dayString + "=" + dayString + "+" + points + " WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		updateLeader();

	}

	public void removePoints(String uuid, int points) {

		updateDay();
		createUserIfNew(uuid);
		int day = GetDay.getDay();

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE weekly_data SET points=points-" + points + ", " + day + "=" + day + "-" + points + " WHERE uuid=?;"
				)){

			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		updateLeader();

	}
	
	public void removePointsForDay(String day) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE weekly_data SET points=points-" + day + ";"
				)){

			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void clearDay(String day) {
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE weekly_data SET "+ day + "=" + 0 + ";"
				)){

			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void setDay(String day) {
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE data SET value=" + day + " WHERE data=?;"
				)){

			statement.setString(1,"day");
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateDay() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT value FROM data WHERE data=?;"
				)){

			statement.setString(1, "day");
			ResultSet results = statement.executeQuery();
			results.next();

			int day;
			try {
				day = Integer.parseInt(results.getString("value"));
			}
			catch (NumberFormatException e)
			{
				return;
			}

			String dayString = null;

			if (day != GetDay.getDay()) {

				switch (GetDay.getDay()) {

				case 1: 
					dayString = "monday";
					break;
				case 2: 
					dayString = "tuesday";
					break;
				case 3: 
					dayString = "wednesday";
					break;
				case 4: 
					dayString = "thursday";
					break;
				case 5: 
					dayString = "friday";
					break;
				case 6: 
					dayString = "saturday";
					break;
				case 7: 
					dayString = "sunday";
					break;

				}

				removePointsForDay(dayString);
				clearDay(dayString);
				setDay(String.valueOf(GetDay.getDay()));

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void updateLeader() {

		if (!(currentLeader().equals(getLeader()))) {

			if (!(getLeader().equals("null"))) {

				String cmd = "/lpb user " + playerData.getName(getLeader()) + " parent add leader";
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), cmd);
			}

			if (!(currentLeader().equals("null"))) {

				String cmd = "/lpb user " + playerData.getName(currentLeader()) + " parent remove leader";
				ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), cmd);
			}

			try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
					"UPDATE data SET value=? WHERE data=?;"
					)){

				statement.setString(1, getLeader());
				statement.setString(2, "leader");
				statement.executeUpdate();

			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	public String currentLeader() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT value FROM data WHERE data=?;"
				)){

			statement.setString(1, "leader");
			ResultSet results = statement.executeQuery();
			results.next();

			return results.getString("value");

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getLeader() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM weekly_data ORDER BY points DESC;"
				)){

			ResultSet results = statement.executeQuery();
			results.next();

			return results.getString("uuid");

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public Leaderboard getOrderedPoints(String uuid, Leaderboard lead) {

		createUserIfNew(uuid);
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid, points FROM weekly_data ORDER BY points DESC;"
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
				"SELECT uuid, points FROM weekly_data ORDER BY points DESC;",
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE
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
}
