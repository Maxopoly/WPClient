package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.LoggedPlayerLocation;
import com.github.maxopoly.WPCommon.packetHandling.AbstractJsonPacket;
import java.util.Set;
import org.json.JSONArray;

public class PlayerLocationPacket extends AbstractJsonPacket {

	public PlayerLocationPacket(Set<LoggedPlayerLocation> playerLocs) {
		super("nearbyPlayers");
		JSONArray locs = new JSONArray();
		for (LoggedPlayerLocation playerLoc : playerLocs) {
			locs.put(playerLoc.serialize());
		}
		msg.put("locs", locs);
	}
}
