package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.Map;

public class ChestContentPacket extends AbstractJsonPacket {

	public ChestContentPacket(Location loc, Map<Integer, Integer> content) {
		super("chestContent");
		msg.put("chest", new Chest(loc, content).serialize());
	}
}
