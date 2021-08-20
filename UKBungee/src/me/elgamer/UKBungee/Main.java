package me.elgamer.UKBungee;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import me.elgamer.UKBungee.commands.AddPoints;
import me.elgamer.UKBungee.commands.PointsCommand;
import me.elgamer.UKBungee.commands.RemovePoints;
import me.elgamer.UKBungee.listeners.JoinEvent;
import me.elgamer.UKBungee.listeners.QuitEvent;
import me.elgamer.UKBungee.sql.PublicBuilds;
import me.elgamer.UKBungee.utils.GetDay;
import me.elgamer.UKBungee.utils.Points;
import net.md_5.bungee.BungeeCord;
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
	private Connection connection_publicbuilds, connection_points;
	public String host, database_publicbuilds, database_points, username, password, plotData, submitData, 
	playerData, pointsData, weeklyData, data;
	public int port;

	@Override
	public void onEnable() {

		//Config Setup
		Main.instance = this;

		try {
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//MySQL		
		mysqlSetup();

		//Creates the mysql table if not existing
		createPointsTable();
		createPlayerDatabase();

		createWeeklyTable();
		createPointsData();

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

				getPublicBuilds();
				getPoints();

				Points pt = new Points();
				
				if (PublicBuilds.newSubmit()) {
					TextComponent message = new TextComponent("A plot has been submitted on the building server!");
					message.setColor(ChatColor.GREEN);

					for (ProxiedPlayer p : getProxy().getPlayers()) {
						if (p.hasPermission("group.reviewer")) {
							p.sendMessage(message);
						}
					}
				}
				
				//Update points from messages and add_points.
				pt.updateMessages();
				pt.updatePoints();

			}

		}, 0L, 60L, TimeUnit.SECONDS);


	}

	public void onDisable() {

		//MySQL
		try {
			if (connection_publicbuilds != null && !connection_publicbuilds.isClosed()) {

				connection_publicbuilds.close();
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
				
				TextComponent message = new TextComponent("MySQL disconnected from " + config.getString("database_publicbuilds"));
				message.setColor(ChatColor.GREEN);
				BungeeCord.getInstance().getConsole().sendMessage(message);
			}

			if (connection_points != null && !connection_points.isClosed()) {

				connection_points.close();
				Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));

				TextComponent message = new TextComponent("MySQL disconnected from " + config.getString("database_points"));
				message.setColor(ChatColor.GREEN);
				BungeeCord.getInstance().getConsole().sendMessage(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//Creates the mysql connection.
	public void mysqlSetup() {

		Configuration config;
		try {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
			host = config.getString("host");
			port = config.getInt("port");

			database_publicbuilds = config.getString("database_publicbuilds");
			database_points = config.getString("database_points");

			username = config.getString("username");
			password = config.getString("password");

			plotData = config.getString("plotData");
			submitData = config.getString("submitData");

			playerData = config.getString("playerData");
			pointsData = config.getString("pointsData");
			weeklyData = config.getString("weeklyData");
			data = config.getString("data");

			try {

				synchronized (this) {
					if (connection_publicbuilds == null || connection_publicbuilds.isClosed()) {
						Class.forName("com.mysql.jdbc.Driver");
						setPublicBuilds(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" 
								+ this.port + "/" + this.database_publicbuilds + "?&useSSL=false&", this.username, this.password));

						TextComponent message = new TextComponent("MySQL connected to " + config.getString("database_publicbuilds"));
						message.setColor(ChatColor.GREEN);
						BungeeCord.getInstance().getConsole().sendMessage(message);
					}

					if (connection_points == null || connection_points.isClosed()) {
						Class.forName("com.mysql.jdbc.Driver");
						setPoints(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" 
								+ this.port + "/" + this.database_points + "?&useSSL=false&", this.username, this.password));

						TextComponent message = new TextComponent("MySQL connected to " + config.getString("database_points"));
						message.setColor(ChatColor.GREEN);
						BungeeCord.getInstance().getConsole().sendMessage(message);
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

	//Returns the mysql publicbuilds connection.
	public Connection getPublicBuilds() {

		try {
			if (connection_publicbuilds == null || connection_publicbuilds.isClosed()) {
				mysqlSetup();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection_publicbuilds;
	}

	//Returns the mysql points connection.
	public Connection getPoints() {

		try {
			if (connection_points == null || connection_points.isClosed()) {
				mysqlSetup();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection_points;
	}

	//Sets the mysql connection as the variable 'connection'.
	public void setPublicBuilds(Connection connection) {
		this.connection_publicbuilds = connection;
	}

	public void setPoints(Connection connection) {
		this.connection_points = connection;
	}

	//Returns an instance of the plugin.
	public static Main getInstance() {
		return instance;
	}

	public void createPointsTable() {
		try {
			PreparedStatement statement = instance.getPoints().prepareStatement
					("CREATE TABLE IF NOT EXISTS " + pointsData
							+ " ( UUID VARCHAR(36) NOT NULL , POINTS INT NOT NULL , MESSAGES INT NOT NULL , ADD_POINTS INT NOT NULL , UNIQUE (UUID))");
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createPlayerDatabase() {
		try {
			PreparedStatement statement = instance.getPoints().prepareStatement
					("CREATE TABLE IF NOT EXISTS " + playerData
							+ " ( UUID VARCHAR(36) NOT NULL , NAME VARCHAR(36) NOT NULL , BUILDING_TIME INT NOT NULL , UNIQUE (UUID))");
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createWeeklyTable() {
		try {
			PreparedStatement statement = instance.getPoints().prepareStatement
					("CREATE TABLE IF NOT EXISTS " + weeklyData
							+ " ( UUID VARCHAR(36) NOT NULL , POINTS INT NOT NULL , MONDAY INT NOT NULL , TUESDAY INT NOT NULL , WEDNESDAY INT NOT NULL , THURSDAY INT NOT NULL , FRIDAY INT NOT NULL , SATURDAY INT NOT NULL , SUNDAY INT NOT NULL , UNIQUE (UUID))");
			statement.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createPointsData() {
		try {
			PreparedStatement statement = instance.getPoints().prepareStatement
					("CREATE TABLE IF NOT EXISTS " + data
							+ " ( DATA VARCHAR(36) NOT NULL , VALUE VARCHAR(36) NOT NULL , UNIQUE (DATA))");
			statement.executeUpdate();
			addDay();
			addLeader();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addDay() {
		try {
			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + data + " WHERE DATA=?");
			statement.setString(1, "day");
			ResultSet results = statement.executeQuery();
			if (!(results.next())) {
				statement = instance.getPoints().prepareStatement
						("INSERT INTO " + data + " (DATA,VALUE) VALUE (?,?)");
				statement.setString(1, "day");
				statement.setString(2, String.valueOf(GetDay.getDay()));
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void addLeader() {
		try {
			PreparedStatement statement = instance.getPoints().prepareStatement
					("SELECT * FROM " + data + " WHERE DATA=?");
			statement.setString(1, "leader");
			ResultSet results = statement.executeQuery();
			if (!(results.next())) {
				statement = instance.getPoints().prepareStatement
						("INSERT INTO " + data + " (DATA,VALUE) VALUE (?,?)");
				statement.setString(1, "leader");
				statement.setString(2, "null");
				statement.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
