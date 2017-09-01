package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.model.AccountCache;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import org.json.JSONObject;

public class InvalidateAllPlayerInfoPacketHandler implements JSONPacketHandler {

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.InvalidateAllPlayerInfo;
	}

	@Override
	public void handle(JSONObject json) {
		AccountCache.getInstance().invalidateAllPlayerInfo();
	}

}
