package io.lgs.starbound;

import io.lgs.starbound.entity.Player;
import io.lgs.starbound.proxy.ThreadClient;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerList {
	public  final List<Player> players = new CopyOnWriteArrayList<Player>();
	public  final List<ThreadClient> clients = new CopyOnWriteArrayList<ThreadClient>();
	private final File banList = Wrapper.getServer().getConfig().banFile();
	
	public PlayerList() {
		
	}
	
	// TODO: Add method to place clients into list
	// TOOD: Add method that creates player from chat. That should allow ThreadClient to have been created by then.
	// TODO: Possibly remove ThreadClient from clients list after setting it to player to safe storage?
	// TODO: Write disconnect Method that deletes Thread then Player.
	// TODO: Login method checks banList to see if it should deny. alternatively add that check to ThreadClient connect instead of chat reading login check
	
}
