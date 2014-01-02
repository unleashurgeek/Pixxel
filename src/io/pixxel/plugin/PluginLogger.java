package io.pixxel.plugin;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

// TODO: GOOD
public class PluginLogger extends Logger {
	private String pluginName;
	
	public PluginLogger(Plugin context) {
		super(context.getClass().getCanonicalName(), null);
		String prefix = context.getDescription().getPrefix();
		pluginName = prefix != null ? new StringBuilder().append("[").append(prefix).append("] ").toString() : "[" + context.getDescription().getName() + "] ";
		setParent(context.getServer().getLogger());
		setLevel(Level.ALL);
	}
	
	@Override
	public void log(LogRecord record) {
		record.setMessage(pluginName + record.getMessage());
		super.log(record);
	}
}
