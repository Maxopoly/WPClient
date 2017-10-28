package com.github.maxopoly.WPClient.connection;

import com.github.maxopoly.WPClient.packetHandling.ClientSideMapDataCompletionPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.ClientSideMapDataPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.InvalidateAllPlayerInfoPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.InvalidateSinglePlayerInfoPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.ItemLocationPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.LoginSuccessPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.MapDataRequestPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.PermissionUpdatePacketHandler;
import com.github.maxopoly.WPClient.packetHandling.PlayerInformationPacketHandler;
import com.github.maxopoly.WPClient.packetHandling.PlayerLocationUpdatePacketHandler;
import com.github.maxopoly.WPClient.packetHandling.WPWayPointPacketHandler;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevelManagement;
import com.github.maxopoly.WPCommon.packetHandling.incoming.BinaryDataForwarder;
import com.github.maxopoly.WPCommon.packetHandling.incoming.IncomingDataHandler;
import com.github.maxopoly.WPCommon.packetHandling.incoming.JSONPacketForwarder;
import com.github.maxopoly.WPCommon.util.AES_CFB8_Encrypter;
import java.io.DataInputStream;
import org.apache.logging.log4j.Logger;

public class ClientSidePacketForwarder extends IncomingDataHandler {

	public ClientSidePacketForwarder(Logger logger, DataInputStream input, AES_CFB8_Encrypter encrypter,
			Runnable failureCallback) {
		// accept all incoming data
		super(logger, input, encrypter, failureCallback, PermissionLevelManagement.getPermissionLevel(1));
		JSONPacketForwarder jsonHandler = new JSONPacketForwarder(logger);
		jsonHandler.registerHandler(new PlayerInformationPacketHandler());
		jsonHandler.registerHandler(new PlayerLocationUpdatePacketHandler());
		jsonHandler.registerHandler(new ItemLocationPacketHandler());
		// jsonHandler.registerHandler(new CrashHandler());
		jsonHandler.registerHandler(new MapDataRequestPacketHandler());
		jsonHandler.registerHandler(new ClientSideMapDataCompletionPacketHandler());
		jsonHandler.registerHandler(new InvalidateAllPlayerInfoPacketHandler());
		jsonHandler.registerHandler(new InvalidateSinglePlayerInfoPacketHandler());
		jsonHandler.registerHandler(new LoginSuccessPacketHandler());
		jsonHandler.registerHandler(new WPWayPointPacketHandler());
		jsonHandler.registerHandler(new PermissionUpdatePacketHandler());
		BinaryDataForwarder binaryHandler = new BinaryDataForwarder(logger);
		binaryHandler.registerHandler(new ClientSideMapDataPacketHandler());
		registerHandler(jsonHandler);
		registerHandler(binaryHandler);
	}

}
