package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.journeyMap.WPWayPointHandler;
import com.github.maxopoly.WPCommon.model.WPWayPoint;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

public class WPWayPointPacketHandler implements JSONPacketHandler {

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.WaypointInformation;
	}

	@Override
	public void handle(JSONObject json) {
		Set<WPWayPoint> pointSet = new HashSet<WPWayPoint>();
		JSONArray points = json.getJSONArray("points");
		for (int i = 0; i < points.length(); i++) {
			JSONObject pointJson = points.getJSONObject(i);
			pointSet.add(new WPWayPoint(pointJson));
		}
		if (WPWayPointHandler.getInstance() != null) {
			WPWayPointHandler.getInstance().overwriteWayPoints(pointSet);
		}
	}

}
