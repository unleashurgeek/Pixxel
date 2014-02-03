package io.pixxel.entity;

import java.util.Set;

import io.pixxel.PixxelServer;
import io.pixxel.command.ConsoleCommandSender;
import io.pixxel.permissions.Permission;
import io.pixxel.permissions.PermissionAttachment;
import io.pixxel.permissions.PermissionAttachmentInfo;
import io.pixxel.plugin.Plugin;

public class PixxelConsoleCommandSender implements ConsoleCommandSender {
	
	public PixxelConsoleCommandSender()	{
		super();
	}
	
	@Override
	public void sendMessage(String sender, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendMessage(String sender, String[] messages) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PixxelServer getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPermissionSet(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPermissionSet(Permission perm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPermission(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPermission(Permission perm) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin, String name,
			boolean value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PermissionAttachment addAttachment(Plugin plugin) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeAttachment(PermissionAttachment attachment) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void recalculatePermissions() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOp() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setOp(boolean value) {
		// TODO Auto-generated method stub
		
	}

}
