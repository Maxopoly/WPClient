package com.github.maxopoly.WPClient.packetHandling;

import com.github.maxopoly.WPClient.model.AccountCache;
import com.github.maxopoly.WPCommon.model.Player;
import com.github.maxopoly.WPCommon.packetHandling.AbstractPacketHandler;
import org.json.JSONObject;

public class PlayerInformationPacketHandler extends AbstractPacketHandler {

	public PlayerInformationPacketHandler() {
		super("playerinfo");
	}

	@Override
	public void handle(JSONObject msg) {
		Player player = new Player(msg.getJSONObject("player"));
		AccountCache.getInstance().registerPlayer(player);
	}
}
