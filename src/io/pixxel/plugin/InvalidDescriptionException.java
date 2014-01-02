package io.pixxel.plugin;

public class InvalidDescriptionException extends Exception {
	private static final long serialVersionUID = -1183682566136866148L;

	public InvalidDescriptionException(final Throwable cause, final String message) {
		super(message, cause);
	}
	
	public InvalidDescriptionException(final Throwable cause) {
		super("Invalid plugin.yml", cause);
	}
	
	public InvalidDescriptionException(final String message) {
		super(message);
	}
	
	public InvalidDescriptionException() {
		super("Invalid plugin.yml");
	}
}
