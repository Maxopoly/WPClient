package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;

public class RequestPlayerInfo extends AbstractJsonPacket {

	public RequestPlayerInfo(String player) {
		super("requestPlayerInfo");
		msg.put("name", player);
	}
}
