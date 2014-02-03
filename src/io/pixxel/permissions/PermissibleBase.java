package io.pixxel.permissions;

import io.pixxel.Pixxel;
import io.pixxel.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: GOOD
public class PermissibleBase implements Permissible {
	private ServerOperator opable = null;
	private Permissible parent = this;
	private final List<PermissionAttachment> attachments = new LinkedList<PermissionAttachment>();
	private final Map<String, PermissionAttachmentInfo> permissions = new HashMap<String, PermissionAttachmentInfo>();
	
	public PermissibleBase(ServerOperator opable) {
		this.opable = opable;
		
		if (opable instanceof Permissible) {
			this.parent = (Permissible) opable;
		}
		
		recalculatePermissions();
	}
	
	@Override
	public boolean isOp() {
		if (opable == null)
			return false;
		else
			return opable.isOp();
	}

	@Override
	public void setOp(boolean value) {
		if (opable == null)
			throw new UnsupportedOperationException("Cannot change op value as no ServerOperator is set");
		else
			opable.setOp(value);
	}

	@Override
	public boolean isPermissionSet(String name) {
		if (name == null)
			throw new IllegalArgumentException("Permission name cannot be null");
		
		return permissions.containsKey(name.toLowerCase());
	}

	@Override
	public boolean isPermissionSet(Permission perm) {
		if (perm == null)
			throw new IllegalArgumentException("Permission cannot be null");
		
		return isPermissionSet(perm.getName());
	}

	@Override
	public boolean hasPermission(String inName) {
		if (inName == null)
			throw new IllegalArgumentException("Permission name cannot be null");
		
		String name = inName.toLowerCase();
		
		if (isPermissionSet(name)) {
			return permissions.get(name).getValue();
		} else {
			Permission perm = Pixxel.getServer().getPluginManager().getPermission(name);
			
			if (perm != null)
				return perm.getDefault().getValue(isOp());
			else
				return Permission.DEFAULT_PERMISSION.getValue(isOp());
		}
	}

	@Override
	public boolean hasPermission(Permission perm) {
		if (perm == null)
			throw new IllegalArgumentException("Permission cannot be null");
		
		String name = perm.getName().toLowerCase();
		
		if (isPermissionSet(name)) {
			return permissions.get(name).getValue();
		}
		return perm.getDefault().getValue(isOp());
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		if (name == null)
			throw new IllegalArgumentException("Permission name cannot be null");
		else if (plugin == null)
			throw new IllegalArgumentException("Plugin cannot be null");
		else if (!plugin.isEnabled())
			throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is Disabled");
		
		PermissionAttachment result = addAttachment(plugin);
		result.setPermission(name, value);
		
		recalculatePermissions();
		
		return result;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin) {
		if (plugin == null)
			throw new IllegalArgumentException("Plugin cannot be null");
		else if (!plugin.isEnabled())
			throw new IllegalArgumentException("Plugin " + plugin.getDescription().getFullName() + " is Disabled");
		
		PermissionAttachment result = new PermissionAttachment(plugin, parent);
		
		attachments.add(result);
		recalculatePermissions();
		
		return result;
	}

	@Override
	public void removeAttachment(PermissionAttachment attachment) {
		if (attachment == null)
			throw new IllegalArgumentException("Attachment cannot be null");
		
		if (attachments.contains(attachment)) {
			attachments.remove(attachment);
			PermissionRemovedExecutor ex = attachment.getRemovalCallback();
			
			if (ex != null) ex.attachmentRemoved(attachment);
			
			recalculatePermissions();
		} else {
			throw new IllegalArgumentException("Given attachment is not part of Permissible object " + parent);
		}
	}

	@Override
	public void recalculatePermissions() {
		clearPermissions();
		Set<Permission> defaults = Pixxel.getServer().getPluginManager().getDefaultPermissions(isOp());
		Pixxel.getServer().getPluginManager().subscribeToDefaultPerms(isOp(), parent);
		
		for (Permission perm : defaults) {
			String name = perm.getName().toLowerCase();
			permissions.put(name, new PermissionAttachmentInfo(parent, name, null, true));
			Pixxel.getServer().getPluginManager().subscribeToPermission(name, parent);
			calculateChildPermissions(perm.getChildren(), false, null);
		}
		
		for (PermissionAttachment attachment : attachments) {
			calculateChildPermissions(attachment.getPermissions(), false, attachment);
		}
	}
	
	public synchronized void clearPermissions() {
		Set<String> perms = permissions.keySet();
		
		for (String name : perms)
			Pixxel.getServer().getPluginManager().unsubscribeFromPermission(name, parent);
		
		Pixxel.getServer().getPluginManager().unsubscribeFromDefaultPerms(false, parent);
		Pixxel.getServer().getPluginManager().unsubscribeFromDefaultPerms(true, parent);
		
		permissions.clear();
	}
	
	private void calculateChildPermissions(Map<String, Boolean> children, boolean invert, PermissionAttachment attachment) {
		Set<String> keys = children.keySet();
		
		for (String name : keys) {
			Permission perm = Pixxel.getServer().getPluginManager().getPermission(name);
			boolean value = children.get(name) ^ invert;
			String lname = name.toLowerCase();
			
			permissions.put(lname, new PermissionAttachmentInfo(parent, lname, attachment, value));
			Pixxel.getServer().getPluginManager().subscribeToPermission(name, parent);
			
			if (perm != null)
				calculateChildPermissions(perm.getChildren(), !value, attachment);
		}
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return new HashSet<PermissionAttachmentInfo>(permissions.values());
	}

}
