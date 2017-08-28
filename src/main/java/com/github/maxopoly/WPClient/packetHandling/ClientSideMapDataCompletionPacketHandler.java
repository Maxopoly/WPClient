package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.journeyMap.MapDataSyncSession;
import com.github.maxopoly.WPCommon.packetHandling.handlers.MapDataCompletionPacketHandler;

public class ClientSideMapDataCompletionPacketHandler extends MapDataCompletionPacketHandler {

	@Override
	public void handle(int id) {
		MapDataSyncSession session = MapDataSyncSession.getInstance();
		if (session.getID() != id) {
			return;
		}
		session.finish();
	}

}
