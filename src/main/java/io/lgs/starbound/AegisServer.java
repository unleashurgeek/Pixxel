package io.lgs.starbound;

import java.util.List;
import java.util.Set;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.file.ServerProperties;
import io.lgs.starbound.proxy.ThreadClient;
import io.lgs.starbound.util.ChatColor;

public class AegisServer {
	private final String wrapperVersion = "1.0b";
	private final String starboundVersion;
	private final StarboundServer starbound;
	private final ServerProperties serverProperties;
	public final PlayerList playerList;
	
	
	
	public AegisServer(StarboundServer starbound, PlayerList playerlist, ServerProperties serverProperties) {
		this.starbound = starbound;
		this.starboundVersion = starbound.getVersion();
		this.playerList = playerlist;
		this.serverProperties = serverProperties;
		
		Wrapper.setServer(this);
	}
	
	public String getVersion() {
		return wrapperVersion;
	}
	
	public String getStarboundVersion() {
		return starboundVersion;
	}
	
	public ServerProperties getConfig() {
		return serverProperties;
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
