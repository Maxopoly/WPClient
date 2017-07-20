package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;

public class PlayerLocationUpdatePacketHandler extends AbstractPacketHandler {

	private static Set<String> updatedPlayers = new HashSet<String>();

	public PlayerLocationUpdatePacketHandler() {
		super("playerLocations");
	}

	public static List<String> popLocationsToUpdate() {
		synchronized (updatedPlayers) {
			List<String> copy = new LinkedList(updatedPlayers);
			updatedPlayers.clear();
			return copy;
		}
	}

	@Override
	public void handle(JSONObject msg) {
		synchronized (updatedPlayers) {
			JSONObject data = msg.getJSONObject("locations");
			LocationTracker tracker = LocationTracker.getInstance();
			for (Object nameObject : data.names()) {
				String name = (String) nameObject;
				Location loc = new Location(data.getJSONObject(name));
				updatedPlayers.add(name);
				tracker.reportLocally(name, loc);
			}
		}
	}

}
