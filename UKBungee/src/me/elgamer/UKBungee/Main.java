package me.elgamer.UKBungee;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

import me.elgamer.UKBungee.commands.AddPoints;
import me.elgamer.UKBungee.commands.PointsCommand;
import me.elgamer.UKBungee.commands.RemovePoints;
import me.elgamer.UKBungee.listeners.JoinEvent;
import me.elgamer.UKBungee.listeners.QuitEvent;
import me.elgamer.UKBungee.sql.PlayerData;
import me.elgamer.UKBungee.sql.Points;
import me.elgamer.UKBungee.sql.PublicBuilds;
import me.elgamer.UKBungee.sql.Weekly;
import me.elgamer.UKBungee.utils.GetDay;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Main extends Plugin {

	static Main instance;

	//MySQL
	public DataSource publicbuildsDataSource, pointsDataSource, uknetDataSource;

	public String host, database_publicbuilds, database_points, database_uknet, username, password;
	public int port;

	public String channel;

	public PlayerData playerData;
	public PublicBuilds publicBuilds;
	public Points points;
	public Weekly weekly;

	@Override
	public void onEnable() {

		//Config Setup
		Main.instance = this;

		try {
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));

			//MySQL		
			host = config.getString("host");
			port = config.getInt("port");

			database_publicbuilds = config.getString("database_publicbuilds");
			database_points = config.getString("database_points");
			database_uknet = config.getString("database_uknet");

			username = config.getString("username");
			password = config.getString("password");

			publicbuildsDataSource = publicBuildsMysql(host, port, username, password, database_publicbuilds);
			pointsDataSource = pointsMysql(host, port, username, password, database_points);
			uknetDataSource = uknetMysql(host, port, username, password, database_uknet);

			initPointsDb();
			
			addDay();
			addLeader();

			playerData = new PlayerData(pointsDataSource);
			points = new Points(pointsDataSource);
			weekly = new Weekly(pointsDataSource);			
			
			publicBuilds = new PublicBuilds(publicbuildsDataSource);

		

		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}

		//Commands
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new AddPoints());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new PointsCommand());
		ProxyServer.getInstance().getPluginManager().registerCommand(this, new RemovePoints());

		//Listeners
		getProxy().getPluginManager().registerListener(this, new JoinEvent());
		getProxy().getPluginManager().registerListener(this, new QuitEvent());

		//1 minute timer
		getProxy().getScheduler().schedule(this, new Runnable() {

			@Override
			public void run() {

				if (publicBuilds.newSubmit()) {
					TextComponent message = new TextComponent("A plot has been submitted on the building server!");
					message.setColor(ChatColor.GREEN);

					for (ProxiedPlayer p : getProxy().getPlayers()) {
						if (p.hasPermission("group.reviewer")) {
							p.sendMessage(message);
						}
					}
				}
				
				//Update points from messages and add_points.
				points.updateMessages();
				points.updatePoints();

			}

		}, 0L, 60L, TimeUnit.SECONDS);


	}

	public void onDisable() {

	}

	//Creates the publicbuilds mysql connection.
	private DataSource publicBuildsMysql(String host, int port, String username, String password, String database) throws SQLException {

		MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

		dataSource.setServerName(host);
		dataSource.setPortNumber(port);
		dataSource.setDatabaseName(database + "?&useSSL=false&");
		dataSource.setUser(username);
		dataSource.setPassword(password);

		testDataSource(dataSource);
		return dataSource;

	}

	//Creates the points mysql connection.
	private DataSource pointsMysql(String host, int port, String username, String password, String database) throws SQLException {

		MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

		dataSource.setServerName(host);
		dataSource.setPortNumber(port);
		dataSource.setDatabaseName(database + "?&useSSL=false&");
		dataSource.setUser(username);
		dataSource.setPassword(password);

		testDataSource(dataSource);
		return dataSource;

	}

	//Creates the uknet mysql connection.
	private DataSource uknetMysql(String host, int port, String username, String password, String database) throws SQLException {

		MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

		dataSource.setServerName(host);
		dataSource.setPortNumber(port);
		dataSource.setDatabaseName(database + "?&useSSL=false&");
		dataSource.setUser(username);
		dataSource.setPassword(password);

		testDataSource(dataSource);
		return dataSource;

	}

	private void testDataSource(DataSource dataSource) throws SQLException {

		try (Connection connection = dataSource.getConnection()) {
			if (!connection.isValid(1000)) {
				throw new SQLException("Could not establish database connection.");
			}
		}
	}

	private void initPointsDb() throws SQLException, IOException {
		// first lets read our setup file.
		// This file contains statements to create our inital tables.
		// it is located in the resources.
		String setup;
		try (InputStream in = this.getResourceAsStream("dbsetup.sql")) {
			// Legacy way
			setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
			throw e;
		}
		// Mariadb can only handle a single query per statement. We need to split at ;.
		String[] queries = setup.split(";");
		// execute each query to the database.
		for (String query : queries) {
			// If you use the legacy way you have to check for empty queries here.
			if (query.trim().isEmpty()) continue;
			try (Connection conn = pointsDataSource.getConnection();
					PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.execute();
			}
		}
		getLogger().info("§2Database setup complete.");
	}

	//Returns an instance of the plugin.
	public static Main getInstance() {
		return instance;
	}

	public boolean hasDay() {
		try (Connection conn = pointsDataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(
				"SELECT data FROM data WHERE data=?;"
				)){
			statement.setString(1, "day");
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

	public void addDay() {
		try (Connection conn = pointsDataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO data(data, value) VALUES (? , ?);"
				)){

			if (!hasDay()) { 
				statement.setString(1, "day");
				statement.setString(2, String.valueOf(GetDay.getDay()));
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean hasLeader() {

		try (Connection conn = pointsDataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(
				"SELECT data FROM data WHERE data=?;"
				)){
			statement.setString(1, "leader");
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


	public void addLeader() {

		try (Connection conn = pointsDataSource.getConnection(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO data(data, value) VALUES (? , ?);"
				)){

			if (!hasLeader()) { 
				statement.setString(1, "leader");
				statement.setString(2, "null");
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}