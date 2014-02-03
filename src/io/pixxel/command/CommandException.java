package io.pixxel.command;

public class CommandException extends RuntimeException {
	private static final long serialVersionUID = -6178810890960540169L;

	public CommandException() {}
	
	public CommandException(String msg) {
		super(msg);
	}
	
	public CommandException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
