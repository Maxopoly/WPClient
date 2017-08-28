package com.github.maxopoly.WPClient.connection;

import com.github.maxopoly.WPClient.packetHandling.ClientSideMapDataCompletionPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.ClientSideMapDataPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.CrashHandler;
import com.github.maxopoly.WPClient.packetHandling.ItemLocationPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.MapDataRequestPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.PlayerInformationPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.PlayerLocationUpdatePacketHandler;
import com.github.maxopoly.WPCommon.packetHandling.incoming.BinaryDataForwarder;
import com.github.maxopoly.WPCommon.packetHandling.incoming.IncomingDataHandler;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketForwarder;
import com.github.maxopoly.WPCommon.util.AES_CFB8_Encrypter;
import java.io.DataInputStream;
import org.apache.logging.log4j.Logger;

public class ClientSidePacketForwarder extends IncomingDataHandler {

	public ClientSidePacketForwarder(Logger logger, DataInputStream input, AES_CFB8_Encrypter encrypter,
			Runnable failureCallback) {
		super(logger, input, encrypter, failureCallback);
		JSONPacketForwarder jsonHandler = new JSONPacketForwarder(logger);
		jsonHandler.registerHandler(new PlayerInformationPacketHandler());
		jsonHandler.registerHandler(new PlayerLocationUpdatePacketHandler());
		jsonHandler.registerHandler(new ItemLocationPacketHandler());
		jsonHandler.registerHandler(new CrashHandler());
		jsonHandler.registerHandler(new MapDataRequestPacketHandler());
		jsonHandler.registerHandler(new ClientSideMapDataCompletionPacketHandler());
		BinaryDataForwarder binaryHandler = new BinaryDataForwarder(logger);
		binaryHandler.registerHandler(new ClientSideMapDataPacketHandler());
		registerHandler(jsonHandler);
		registerHandler(binaryHandler);
	}

}
