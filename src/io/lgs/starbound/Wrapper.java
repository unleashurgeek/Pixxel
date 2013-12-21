package io.lgs.starbound;

public class Wrapper {
	private static AegisServer server;
	
	// Static class cannot be initialized. 
	private Wrapper() {}
	
	public static AegisServer getServer() {
		return server;
	}
	
	public static void setServer(AegisServer server) {
		if (Wrapper.server != null) {
			throw new UnsupportedOperationException("Cannot redefine singleton Server");
		}
		
		Wrapper.server = server;
	}
}
