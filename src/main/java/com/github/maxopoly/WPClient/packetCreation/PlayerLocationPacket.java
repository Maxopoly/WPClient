package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.LoggedPlayerLocation;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerLocationPacket extends AbstractJsonPacket {

	private Set<LoggedPlayerLocation> playerLocs;

	public PlayerLocationPacket(Set<LoggedPlayerLocation> playerLocs) {
		this.playerLocs = playerLocs;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.PlayerLocationPush;
	}

	@Override
	public void setupJSON(JSONObject json) {
		JSONArray locs = new JSONArray();
		for (LoggedPlayerLocation playerLoc : playerLocs) {
			locs.put(playerLoc.serialize());
		}
		json.put("locs", locs);
	}
}
