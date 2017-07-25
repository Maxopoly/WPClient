package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;

public class ItemLocationRequestPacket extends AbstractJsonPacket {

	public ItemLocationRequestPacket(int itemID) {
		super("itemLocationRequest");
		msg.put("itemId", itemID);
	}

}
