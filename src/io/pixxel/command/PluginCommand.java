package io.pixxel.command;

import io.pixxel.plugin.Plugin;

public final class PluginCommand extends Command {
	private final Plugin owningPlugin;
	private CommandExecutor executor;
	
	protected PluginCommand(String name, Plugin owner) {
		super(name);
		this.executor = owner;
		this.owningPlugin = owner;
		this.usageMessage = "";
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		boolean success = false;
		
		if(!owningPlugin.isEnabled())
			return false;
		
		if (!testPermission(sender))
			return true;
		
		try {
			success = executor.onCommand(sender, this, commandLabel, args);
		} catch (Throwable e) {
			throw new CommandException("Unhandled exception executing command '" + commandLabel + "' in plugin " + owningPlugin.getDescription().getFullName(), e);
		}
		
		if (!success && usageMessage.length() > 0) {
			for (String line : usageMessage.replace("<command>", commandLabel).split("\n")) {
				sender.sendMessage(owningPlugin.getName(), line);
			}
		}
		
		return success;
	}
	
	public void setExecutor(CommandExecutor executor) {
		this.executor = executor == null ? owningPlugin : executor;
	}
	
	public CommandExecutor getExecutor() {
		return executor;
	}
	
	public Plugin getPlugin() {
		return owningPlugin;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		sb.deleteCharAt(sb.length() - 1);
		sb.append(", ").append(owningPlugin.getDescription().getFullName()).append(')');
		return sb.toString();
	}
}
