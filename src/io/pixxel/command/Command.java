package io.pixxel.command;

import io.pixxel.Pixxel;
import io.pixxel.permissions.Permissible;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Command {
	private final String name;
	private String nextLabel;
	private String label;
	private List<String> aliases;
	private List<String> activeAliases;
	private CommandMap commandMap = null;
	protected String description = "";
	protected String usageMessage;
	private String permission;
	private String permissionMessage;
	
	protected Command(String name) {
		this(name, "", "/" + name, new ArrayList<String>());
	}
	
	protected Command(String name, String description, String usageMessage, List<String> aliases) {
		this.name = name;
		this.nextLabel = name;
		this.label = name;
		this.description = description;
		this.usageMessage = usageMessage;
		this.aliases = aliases;
		this.activeAliases = new ArrayList<String>(aliases);
	}
	
	
	public abstract boolean execute(CommandSender sender, String commandLabel, String[] args);
	
	public String getName() {
		return name;
	}
	
	public String getPermission() {
		return permission;
	}
	
	public void setPermission(String permission) {
		this.permission = permission;
	}
	
	public boolean testPermission(CommandSender target) {
		if (testPermissionSilent(target)) {
			return true;
		}
		
		if (permissionMessage == null) {
			target.sendMessage("Pixxel", "I'm sorry, but you do not have permission to perform this command. Please contact the server administartors if you believe that this is an error.");
		} else if (permissionMessage.length() != 0) {
			for (String line : permissionMessage.replace("<permission>", permission).split("\n"))
				target.sendMessage("Pixxel", line);
		}
		
		return false;
	}
	
	public boolean testPermissionSilent(CommandSender target) {
		if ((permission == null) || (permission.length() == 0))
			return true;
		
		for (String p : permission.split(";")) {
			if (target.hasPermission(p)) {
				return true;
			}
		}
		
		return false;
	}
	
	public String getLabel() {
		return label;
	}
	
	public boolean setLabel(String name) {
		this.nextLabel = name;
		if (!isRegistered()) {
			this.label = name;
			return true;
		}
		return false;
	}
	
	public boolean register(CommandMap commandMap) {
		if (allowChangesFrom(commandMap)) {
			this.commandMap = commandMap;
			return true;
		}
		
		return false;
	}
	
	public boolean unregister(CommandMap commandMap) {
		if (allowChangesFrom(commandMap)) {
			this.commandMap = null;
			this.activeAliases = new ArrayList<String>(this.aliases);
			this.label = this.nextLabel;
			return true;
		}
		
		return false;
	}
	
	private boolean allowChangesFrom(CommandMap commandMap) {
		return (null == this.commandMap || this.commandMap == commandMap);
	}
	
	public boolean isRegistered() {
		return (null != this.commandMap);
	}
	
	public List<String> getAliases() {
		return activeAliases;
	}
	
	public String getPermissionMessage() {
		return permissionMessage;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getUsage() {
		return usageMessage;
	}
	
	public Command setAliases(List<String> aliases) {
		this.aliases = aliases;
		if (!isRegistered())
			this.activeAliases = new ArrayList<String>(aliases);
		return this;
	}
	
	public Command setDescription(String description) {
		this.description = description;
		return this;
	}
	
	public Command setPermissionMessage(String permissionMessage) {
		this.permissionMessage = permissionMessage;
		return this;
	}
	
	public Command setUsage(String usage) {
		this.usageMessage = usage;
		return this;
	}
	
	public static void broadcastCommandMessage(CommandSender source, String message) {
		broadcastCommandMessage(source, message, true);
	}
	
	public static void broadcastCommandMessage(CommandSender source, String message, boolean sendToSource) {
		String result = source.getName() + ": " + message;
		
		Set<Permissible> users = Pixxel.getServer().getPluginManager().getPermissionSubscriptions("pixxel.broadcast.admin");
		String colored = "[" + result + "]";
		
		if (sendToSource)
			source.sendMessage("Pixxel", message);
		
		for (Permissible user : users) {
			if (user instanceof CommandSender) {
				CommandSender target = (CommandSender) user;
				
				if (target instanceof ConsoleCommandSender) {
					target.sendMessage("Pixxel", result);
				} else if (target != source) {
					target.sendMessage("Pixxel", colored);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return getClass().getName() + '(' + name + ')';
	}
}
