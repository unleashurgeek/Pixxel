package io.pixxel;

public class Pixxel {
	private static PixxelServer server;
	
	// Static class cannot be initialized. 
	private Pixxel() {}
	
	public static PixxelServer getServer() {
		return server;
	}
	
	public static void setServer(PixxelServer server) {
		if (Pixxel.server != null) {
			throw new UnsupportedOperationException("Cannot redefine singleton Server");
		}
		
		Pixxel.server = server;
	}
}
