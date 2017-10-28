package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.MessageHandler;
import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketHandler;
import com.github.maxopoly.WPCommon.util.WPStatics;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLLog;
import org.json.JSONObject;

public class LoginSuccessPacketHandler implements JSONPacketHandler {

	@Override
	public PacketIndex getPacketToHandle() {
		return PacketIndex.LoginSuccess;
	}

	@Override
	public void handle(JSONObject json) {
		FMLLog.getLogger().info("Successfully connected to WPServer");
		int version = json.getInt("version");
		if (version != WPStatics.protocolVersion) {
			MessageHandler.getInstance().queueMessage(
					new TextComponentString(
							"[WPC] You are running an outdated version. It may no longer work properly,"
									+ " asking Max for a newer version is highly recommended. Your version: 1."
									+ WPStatics.protocolVersion + ", latest: 1." + version));
		}
	}

}
