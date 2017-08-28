package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.Set;
import org.json.JSONObject;

public class ChestContentPacket extends AbstractJsonPacket {

	private Location loc;

	private Set<WPItem> items;

	public ChestContentPacket(Location loc, Set<WPItem> items) {
		this.loc = loc;
		this.items = items;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.ChestContent;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("chest", new Chest(loc, items).serialize());
	}
}
