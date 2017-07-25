package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;

public class RequestPlayerInfoPacket extends AbstractJsonPacket {

	public RequestPlayerInfoPacket(String player) {
		super("requestPlayerInfo");
		msg.put("name", player);
	}
}
