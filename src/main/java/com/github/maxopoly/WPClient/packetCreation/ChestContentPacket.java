package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.Set;

public class ChestContentPacket extends AbstractJsonPacket {

	public ChestContentPacket(Location loc, Set<WPItem> items) {
		super("chestContent");
		msg.put("chest", new Chest(loc, items).serialize());
	}
}
