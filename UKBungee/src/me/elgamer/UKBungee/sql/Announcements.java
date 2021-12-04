package me.elgamer.UKBungee.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class Announcements {
	
	DataSource dataSource;

	public Announcements(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}
	
	public String[] getAnnouncement() {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id, message, colour FROM announcements;"
				)){
			
			ResultSet results = statement.executeQuery();
			
			if (results.next()) {
				
				//Create the array to return
				String[] announcement = new String[] {results.getString("message"), results.getString("colour")};
				
				//Remove the announcement from the database
				removeAnnouncement(results.getInt("id"));
				
				//Return announcement
				return announcement;
				
			} else {
				//No new announcement can be found, so return null.
				return null;
			}			
			
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public void removeAnnouncement(int id) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"DELETE FROM announcements WHERE id=?;"
				)){
			
			statement.setInt(1, id);
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}

}
