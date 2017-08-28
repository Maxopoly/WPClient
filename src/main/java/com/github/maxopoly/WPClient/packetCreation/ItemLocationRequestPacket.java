package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class ItemLocationRequestPacket extends AbstractJsonPacket {

	private WPItem item;

	public ItemLocationRequestPacket(WPItem item) {
		this.item = item;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.ItemLocationRequest;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("item", item.serialize());

	}

}
