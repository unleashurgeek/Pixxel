package io.pixxel.permissions;

import io.pixxel.plugin.Plugin;

import java.util.Set;

public interface Permissible extends ServerOperator {
	
	public boolean isPermissionSet(String name);
	
	public boolean isPermissionSet(Permission perm);
	
	public boolean hasPermission(String name);
	
	public boolean hasPermission(Permission perm);
	
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value);
	
	public PermissionAttachment addAttachment(Plugin plugin);
	
	public void removeAttachment(PermissionAttachment attachment);
	
	public void recalculatePermissions();
	
	public Set<PermissionAttachmentInfo> getEffectivePermissions();
}
