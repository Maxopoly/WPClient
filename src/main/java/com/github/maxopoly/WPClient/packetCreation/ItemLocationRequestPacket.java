package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;

public class ItemLocationRequestPacket extends AbstractJsonPacket {

	public ItemLocationRequestPacket(WPItem item) {
		super("itemLocationRequest");
		msg.put("item", item.serialize());
	}

}
