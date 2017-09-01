package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class ChestDeletionPacket extends AbstractJsonPacket {

	private Location loc;

	public ChestDeletionPacket(Location loc) {
		this.loc = loc;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.DeleteChest;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("loc", loc.serialize());
	}

}
