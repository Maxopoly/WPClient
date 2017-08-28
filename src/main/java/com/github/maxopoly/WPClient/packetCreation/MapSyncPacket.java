package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapSyncPacket extends AbstractJsonPacket {

	private List<WPMappingTile> localTiles;
	private int sessionID;

	public MapSyncPacket(List<WPMappingTile> localTiles, int sessionID) {
		this.localTiles = localTiles;
		this.sessionID = sessionID;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.MapDataSyncInit;
	}

	@Override
	public void setupJSON(JSONObject json) {
		JSONArray array = new JSONArray();
		for (WPMappingTile tile : localTiles) {
			array.put(tile.serialize());
		}
		json.put("tileData", array);
		json.put("id", sessionID);
	}

}
