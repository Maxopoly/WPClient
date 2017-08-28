package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class RequestPlayerInfoPacket extends AbstractJsonPacket {

	private String player;

	public RequestPlayerInfoPacket(String player) {
		this.player = player;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.PlayerInfoRequest;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("name", player);
	}
}
