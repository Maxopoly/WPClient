package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.connection.ServerConnection;
import com.github.maxopoly.WPClient.journeyMap.MapDataSyncSession;
import com.github.maxopoly.WPCommon.model.CoordPair;
import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import com.github.maxopoly.WPCommon.packetHandling.packets.MapDataCompletionPacket;
import com.github.maxopoly.WPCommon.packetHandling.packets.MapDataPacket;
import java.io.File;
import net.minecraftforge.fml.common.FMLLog;
import org.json.JSONArray;
import org.json.JSONObject;

public class MapDataRequestPacketHandler implements JSONPacketHandler {

	@Override
	public void handle(JSONObject json) {
		MapDataSyncSession session = MapDataSyncSession.getInstance();
		int sessionID = json.getInt("id");
		if (session.getID() != sessionID) {
			return;
		}
		session.setStatus("Uploading map data requested by server");
		session.setExpectedReturnFiles();
		JSONArray data = json.getJSONArray("coords");
		int filesToSend = data.length();
		for (int i = 0; i < data.length(); i++) {
			JSONObject obj = data.getJSONObject(i);
			int x = obj.getInt("x");
			int z = obj.getInt("z");
			String fileName = new WPMappingTile(new CoordPair(x, z)).getFileName();
			File f = new File(session.getDayDataFolder(), fileName);
			if (!f.exists()) {
				FMLLog.getLogger().warn("Server requested file " + fileName + ", but it did not exist");
				continue;
			}
			WPMappingTile tile = session.loadMapTile(f);
			if (!WPClientForgeMod.getInstance().connectedToWPServer()) {
				FMLLog.getLogger().error("Lost connection to WPServer while syncing map data");
				break;
			}
			ServerConnection conn = WPClientForgeMod.getInstance().getServerConnection();
			conn.sendMessage(new MapDataPacket(tile, sessionID));
			session.setStatus(String.format("Uploaded (%d / %d) map tiles to server", i, filesToSend));
		}
		WPClientForgeMod.getInstance().getServerConnection().sendMessage(new MapDataCompletionPacket(sessionID));
		session.setStatus("Successfully uploaded all data requested by server. Awaiting merged data from server...");
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.PlayerMapDataRequest;
	}
}
