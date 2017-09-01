package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.model.AccountCache;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import org.json.JSONObject;

public class InvalidateSinglePlayerInfoPacketHandler implements JSONPacketHandler {

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.InvalidateSinglePlayerInfo;
	}

	@Override
	public void handle(JSONObject json) {
		String name = json.getString("name");
		AccountCache.getInstance().invalidatePlayerInfo(name);
	}

}
