package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;

public class RenderDistancePlayersPacket extends AbstractJsonPacket {

	public RenderDistancePlayersPacket(Map<String, Location> players) {
		super("nearbyPlayers");
		JSONObject locs = new JSONObject();
		for (Entry<String, Location> entry : players.entrySet()) {
			locs.put(entry.getKey(), entry.getValue().serialize());
		}
		msg.put("locations", locs);
	}
}
