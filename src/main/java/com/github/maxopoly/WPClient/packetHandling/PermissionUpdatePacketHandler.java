package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevelManagement;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import org.json.JSONObject;

public class PermissionUpdatePacketHandler implements JSONPacketHandler {

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.UpdatePermissions;
	}

	@Override
	public void handle(JSONObject json) {
		int level = json.getInt("level");
		WPClientForgeMod.getInstance().setPermissionLevel(PermissionLevelManagement.getPermissionLevel(level));
	}

}
