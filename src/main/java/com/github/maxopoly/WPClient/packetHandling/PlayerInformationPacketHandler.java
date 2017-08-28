package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.model.AccountCache;
import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import org.json.JSONObject;

public class PlayerInformationPacketHandler implements JSONPacketHandler {

	@Override
	public void handle(JSONObject msg) {
		Player player = new Player(msg.getJSONObject("player"));
		AccountCache.getInstance().registerPlayer(player);
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.PlayerInfoReply;
	}
}
