package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.model.AccountCache;
import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import net.minecraftforge.fml.common.FMLLog;
import org.json.JSONObject;

public class PlayerInformationPacketHandler extends AbstractPacketHandler {

	public PlayerInformationPacketHandler() {
		super("playerinfo");
	}

	@Override
	public void handle(JSONObject msg) {
		FMLLog.getLogger().info("PLAYER " + msg.toString());
		Player player = new Player(msg.getJSONObject("player"));
		AccountCache.getInstance().registerPlayer(player);
	}
}
