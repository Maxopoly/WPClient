package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.LoggedPlayerLocation;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class PlayerLocationUpdatePacketHandler implements JSONPacketHandler {

	private static Set<String> updatedPlayers = new HashSet<String>();

	public static void stagePlayerForUpdate(String name) {
		synchronized (updatedPlayers) {
			updatedPlayers.add(name);
		}
	}

	public static List<String> popLocationsToUpdate() {
		synchronized (updatedPlayers) {
			List<String> copy = new LinkedList<String>(updatedPlayers);
			updatedPlayers.clear();
			return copy;
		}
	}

	@Override
	public void handle(JSONObject msg) {
		synchronized (updatedPlayers) {
			JSONArray data = msg.getJSONArray("locs");
			LocationTracker tracker = LocationTracker.getInstance();
			for (int i = 0; i < data.length(); i++) {
				LoggedPlayerLocation loc = new LoggedPlayerLocation(data.getJSONObject(i));
				updatedPlayers.add(loc.getPlayer());
				tracker.reportLocally(loc);
			}
		}
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.PlayerLocationPush;
	}

}
