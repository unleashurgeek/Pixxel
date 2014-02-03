package io.pixxel.command;

import io.pixxel.PixxelServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SimpleCommandMap implements CommandMap {
	private static final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);
	protected final Map<String, Command> knownCommands = new HashMap<String, Command>();
	protected final Set<String>	aliases = new HashSet<String>();
	protected static final Set<Command> fallbackCommands = new HashSet<Command>();
	
	public SimpleCommandMap() {
		setDefaultCommands();
	}
	
	private void setDefaultCommands() {
		// TODO: Add Important default commands that cannot be overrided here.
	}
	
	public void registerAll(String fallbackPrefix, List<Command> commands) {
		if (commands != null) {
			for (Command c : commands)  {
				register(fallbackPrefix, c);
			}
		}
	}
	
	public boolean register(String fallbackPrefix, Command command) {
		return register(command.getName(), fallbackPrefix, command);
	}
	
	public boolean register(String label, String fallbackPrefix, Command command) {
		boolean registeredPassedLevel = register(label, fallbackPrefix, command, true);
		
		Iterator<String> iterator = command.getAliases().iterator();
		while (iterator.hasNext()) {
			if (!register(iterator.next(), fallbackPrefix, command, true)) {
				iterator.remove();
			}
		}
		
		command.register(this);
		
		return registeredPassedLevel;
	}
	
	private synchronized boolean register(String label, String fallbackPrefix, Command command, boolean isAlias) {
		String lowerLabel = label.trim().toLowerCase();
		
		if (isAlias && knownCommands.containsKey(lowerLabel))
			return false;
		
		String lowerPrefix = fallbackPrefix.trim().toLowerCase();
		boolean registeredPassedLabel = true;
		
		while (knownCommands.containsKey(lowerLabel) && !aliases.contains(lowerLabel)) {
			lowerLabel = lowerPrefix + ":" + lowerLabel;
			registeredPassedLabel = false;
		}
		
		if (isAlias) {
			aliases.add(lowerLabel);
		} else {
			aliases.remove(lowerLabel);
			command.setLabel(lowerLabel);
		}
		knownCommands.put(lowerLabel, command);
		
		return registeredPassedLabel;
	}
	
	protected Command getFallback(String label) {
		for (Command cmd : fallbackCommands)	 {
			if (cmd.matches(label)) {
				return cmd;
			}
		}
		return null;
	}
	
	public Set<Command> getFallbackCommands() {
		return Collections.unmodifiableSet(fallbackCommands);
	}
	
	public boolean dispatch(CommandSender sender, String commandLine) throws CommandException {
		String[] args = PATTERN_ON_SPACE.split(commandLine);
		
		if (args.length == 0) {
			return false;
		}
		
		String sentCommandLabel = args[0].toLowerCase();
		Command target = getCommand(sentCommandLabel);
		
		if (target == null)
			return false;
		
		try {
			target.execute(sender, sentCommandLabel, Arrays.copyOfRange(args, 1, args.length));
		} catch (CommandException e) {
			throw e;
		} catch (Throwable t) {
			throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + target, t);
		}
		
		return true;
	}
	
	public synchronized void clearCommands() {
		for (Map.Entry<String, Command> entry : knownCommands.entrySet())
			entry.getValue().unregister(this);
		
		knownCommands.clear();
		aliases.clear();
		setDefaultCommands();
	}
	
	public Command getCommand(String name) {
		Command target = knownCommands.get(name.toLowerCase());
		if (target == null)
			target = getFallback(name);
		
		return target;
	}
	
	public Collection<Command> getCommands() {
		return knownCommands.values();
	}
	
	static {
		// TOOD: Add default commands here (fallbackCommands)
	}
}
