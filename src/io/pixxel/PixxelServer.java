package io.pixxel;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.pixxel.command.Command;
import io.pixxel.command.CommandSender;
import io.pixxel.command.PluginCommand;
import io.pixxel.command.SimpleCommandMap;
import io.pixxel.entity.Player;
import io.pixxel.file.ServerProperties;
import io.pixxel.permissions.Permission;
import io.pixxel.plugin.Plugin;
import io.pixxel.plugin.PluginLoader;
import io.pixxel.plugin.PluginManager;
import io.pixxel.proxy.ThreadClient;
import io.pixxel.util.ChatColor;

public class PixxelServer {
	private final String wrapperVersion = "1.0b";
	private final String starboundVersion;
	private final StarboundServer starbound;
	private final ServerProperties serverProperties;
	private final Logger logger = Logger.getLogger("Starbound");
	private final SimpleCommandMap commandMap = new SimpleCommandMap();
	// TODO: HelpMap
	private final PluginManager pluginManager = new PluginManager(this, commandMap);
	public final PlayerList playerList;
	
	
	
	public PixxelServer(StarboundServer starbound, PlayerList playerlist, ServerProperties serverProperties) {
		this.starbound = starbound;
		this.starboundVersion = starbound.getVersion();
		this.playerList = playerlist;
		this.serverProperties = serverProperties;
		
		Pixxel.setServer(this);
		
		loadPlugins();
		enablePlugins();
	}
	
	public void loadPlugins() {
		pluginManager.registerInterface(PluginLoader.class);
		
		File pluginFolder = new File("plugins");
		
		if (pluginFolder.exists()) {
			Plugin[] plugins = pluginManager.loadPlugins(pluginFolder);
			for (Plugin plugin : plugins) {
				try {
					String message = String.format("Loading %s", plugin.getDescription().getFullName());
					plugin.getLogger().info(message);
					plugin.onLoad();
				} catch (Throwable ex) {
					Logger.getLogger(PixxelServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " initializing " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
				}
			}
		} else {
			pluginFolder.mkdir();
		}
	}
	
	public void enablePlugins() {
		// TODO: Clear HelpMap
		
		Plugin[] plugins = pluginManager.getPlugins();
		
		for (Plugin plugin : plugins) {
			if ((!plugin.isEnabled())) {
				loadPlugin(plugin);
			}
		}
		
		// TODO: register Command Map Server Aliases?
	}
	
	public void disablePlugins() {
		pluginManager.disablePlugins();
	}
	
	private void loadPlugin(Plugin plugin) {
		try {
			pluginManager.enablePlugin(plugin);
			
			List<Permission> perms = plugin.getDescription().getPermissions();
			
			for (Permission perm : perms) {
				try {
					pluginManager.addPermission(perm);
				} catch (IllegalArgumentException ex) {
					getLogger().log(Level.WARNING, "Plugin " + plugin.getDescription().getFullName() + " tried to register permission '" + perm.getName() + "' but it's already registered", ex);
				}
			}
		} catch (Throwable ex) {
            Logger.getLogger(PixxelServer.class.getName()).log(Level.SEVERE, ex.getMessage() + " loading " + plugin.getDescription().getFullName() + " (Is it up to date?)", ex);
		}
	}
	
	public boolean dispatchCommand(CommandSender sender, String commandLine) {
		if (sender == null)
			throw new IllegalArgumentException("Sender cannot be null!");
		if (commandLine == null)
			throw new IllegalArgumentException("CommandLine cannot be null!");
		
		if (commandMap.dispatch(sender, commandLine)) {
			return true;
		}
		
		if (sender instanceof Player) {
			sender.sendMessage("Pixxel", "Unkown command. Type \"/help\" for help.");
		} else {
			sender.sendMessage("Pixxel", "Unkown command. Type \"help\" for help.");
		}
		
		return false;
	}
	
	public PluginManager getPluginManager() {
		return pluginManager;
	}
	
	public String getVersion() {
		return wrapperVersion;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public String getStarboundVersion() {
		return starboundVersion;
	}
	
	public ServerProperties getConfig() {
		return serverProperties;
	}
	
	public PluginCommand getPluginCommand(String name) {
		Command command = commandMap.getCommand(name);
		
		if (command instanceof PluginCommand) {
			return (PluginCommand) command;
		} else {
			return null;
		}
	}
	
	public SimpleCommandMap getCommandMap() {
		return commandMap;
	}
	
	public Player[] getOnlinePlayers() {
		List<ThreadClient> online = playerList.clients;
		Player[] players = new Player[online.size()];
		
		for (int i = 0; i < players.length; i++) {
			players[i] = online.get(i).getPlayer();
		}
		return players;
	}
	
	// Need the Starbound.config location
	public int getMaxPlayers() {
		return 0;
	}
	
	/** 
	 * Sends message to all players on server. Returns number of players sent to.
	 */
	public int broadcastMessage(String message, ChatColor color) {
		Player[] players = getOnlinePlayers();
		int count = 0;
		for (Player player : players) {
			player.sendMessage(message, color);
			count++;
		}
		return count;
	}
	
	public Player getPlayer(String name) {
		Player[] players = getOnlinePlayers();
		Player found = null;
		String lowerName = name.toLowerCase();
		int delta = Integer.MAX_VALUE;
		for (Player player : players) {
			if (player.getName().toLowerCase().startsWith(lowerName)) {
				int curDelta = player.getName().length() - lowerName.length();
				if (curDelta < delta) {
					found = player;
					delta = curDelta;
				}
				if (curDelta == 0) break;
			}
		}
		return found;
	}
	
	// case-sensitive
	public Player getPlayerExact(String name) {
		
		for (Player player : getOnlinePlayers()) {
			if (player.getName().equals(name)) {
				return player;
			}
		}
		return null;
	}
	
	// by IP
	public Player getPlayerByIP(String ip) {
		
		for (Player player : getOnlinePlayers()) {
			if (player.getAddress().getAddress().toString().equals("ip")) {
				return player;
			}
		}
		return null;
	}
	
	public void kickAll() {
		Player[] players = getOnlinePlayers();
		for (Player player : players)
			player.kickPlayer();
	}
	
	// shutdown server
	public void shutdown() {
		starbound.shutdown();
	}
	
	// List all banned IPs
	public Set<String> getBans() {
		return serverProperties.banFile().getBans();
	}
	
	// returns true if banned, false if not.
	public boolean banIP(String address) {
		return serverProperties.banFile().addBan(address);
	}
	
	public boolean unbanIP(String address) {
		return serverProperties.banFile().removeBan(address);
	}
}
