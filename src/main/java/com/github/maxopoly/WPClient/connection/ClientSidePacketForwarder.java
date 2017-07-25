package com.github.maxopoly.WPClient.connection;

import com.github.maxopoly.WPClient.packetHandling.ItemLocationPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.PlayerInformationPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.PlayerLocationUpdatePacketHandler;
import com.github.maxopoly.WPCommon.packetHandling.PacketForwarder;
import org.apache.logging.log4j.Logger;

public class ClientSidePacketForwarder extends PacketForwarder {

	public ClientSidePacketForwarder(Logger logger) {
		super(logger);
	}

	@Override
	protected void registerHandler() {
		registerPacketHandler(new PlayerInformationPacketHandler());
		registerPacketHandler(new PlayerLocationUpdatePacketHandler());
		registerPacketHandler(new ItemLocationPacketHandler());
	}

}
