package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.journeyMap.ItemLocationWayPointHandler;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemLocationPacketHandler extends AbstractPacketHandler {

	public ItemLocationPacketHandler() {
		super("itemLocation");
	}

	@Override
	public void handle(JSONObject json) {
		JSONArray array = json.getJSONArray("chests");
		Map<Location, Integer> locs = new HashMap<Location, Integer>();
		int id = json.getInt("id");
		for (int i = 0; i < array.length(); i++) {
			JSONObject innerJson = array.getJSONObject(i);
			Location loc = new Location(innerJson.getJSONObject("loc"));
			int amount = innerJson.getInt("amount");
			locs.put(loc, amount);
		}
		ItemLocationWayPointHandler.getInstance().markLocations(id, locs);
	}

}
