package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.journeyMap.ItemLocationWayPointHandler;
import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPItem;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ItemLocationPacketHandler implements JSONPacketHandler {

	@Override
	public void handle(JSONObject json) {
		JSONArray array = json.getJSONArray("chests");
		List<Chest> chests = new LinkedList<Chest>();
		String itemString = json.getString("item");
		WPItem item = new WPItem(itemString);
		for (int i = 0; i < array.length(); i++) {
			JSONObject innerJson = array.getJSONObject(i);
			Location loc = new Location(innerJson.getJSONObject("loc"));
			JSONArray contentArray = innerJson.getJSONArray("items");
			Chest c = new Chest(loc);
			for (int k = 0; k < contentArray.length(); k++) {
				String contentItemString = contentArray.getString(k);
				WPItem contentItem = new WPItem(contentItemString);
				c.addItem(contentItem);
			}
			chests.add(c);
		}
		ItemLocationWayPointHandler.getInstance().markLocations(item, chests);
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.ItemLocationReply;
	}
}
