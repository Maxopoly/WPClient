package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.SnitchHitAction;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;

public class SnitchHitPacket extends AbstractJsonPacket {

	public SnitchHitPacket(String playerName, Location loc, SnitchHitAction action) {
		super("snitchhit");
		msg.put("name", playerName);
		msg.put("location", loc.serialize());
		msg.put("action", action.toString());
	}

}
