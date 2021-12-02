package me.elgamer.UKBungee.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;


public class PublicBuilds {

	DataSource dataSource;

	public PublicBuilds(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	//Checks whether there is a submitted plot to review.
	public boolean reviewExists(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM plot_data WHERE status=?;"
				)){

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
	public int reviewCount(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT COUNT(uuid) FROM plot_data WHERE status=? AND uuid <> ?;"
				)){

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

	public boolean submitExists() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT * FROM submit_data;"
				)){

			ResultSet results = statement.executeQuery();

			if (results.next()) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	public boolean newSubmit() {

		if (submitExists()) { 
			try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
					"DELETE FROM submit_data;"
					)){

				statement.executeUpdate();
				return true;

			} catch (SQLException sql) {
				sql.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

}
