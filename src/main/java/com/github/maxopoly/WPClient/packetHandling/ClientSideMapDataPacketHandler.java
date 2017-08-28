package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.journeyMap.MapDataSyncSession;
import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.packetHandling.handlers.MapDataPacketHandler;
import net.minecraftforge.fml.common.FMLLog;

public class ClientSideMapDataPacketHandler extends MapDataPacketHandler {

	public ClientSideMapDataPacketHandler() {
		super(FMLLog.getLogger());
	}

	@Override
	public void handle(final WPMappingTile tile, int id) {
		final MapDataSyncSession session = MapDataSyncSession.getInstance();
		if (session.getID() != id) {
			return;
		}
		session.handleReceivedTile(tile);
	}

}
