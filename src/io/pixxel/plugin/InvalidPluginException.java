package io.pixxel.plugin;

public class InvalidPluginException extends Exception {
	private static final long serialVersionUID = -5665207538819010145L;

	public InvalidPluginException(final Throwable cause) {
		super(cause);
	}
	
	public InvalidPluginException() { }
	
	public InvalidPluginException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public InvalidPluginException(final String message) {
		super(message);
	}
}
