package com.github.maxopoly.WPClient.connection;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.packetCreation.AuthPlayerPacket;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.IPacket;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.OutgoingDataHandler;
import com.github.maxopoly.WPCommon.util.AES_CFB8_Encrypter;
import com.github.maxopoly.WPCommon.util.ConnectionUtils;
import com.github.maxopoly.WPCommon.util.PKCSEncrypter;
import com.github.maxopoly.WPCommon.util.VarInt;
import com.github.maxopoly.WPCommon.util.WPStatics;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class ServerConnection {

	private static final String serverAdress = "168.235.102.74";
	private final static String sessionServerAdress = "https://sessionserver.mojang.com/session/minecraft/join";

	private static final String tag = "blue";

	private Logger logger;
	private ClientSidePacketForwarder packetHandler;
	private OutgoingDataHandler packetSender;
	private boolean closed;
	private boolean initialized;

	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private Key serverPubKey;
	private byte[] sharedSecret;
	private AES_CFB8_Encrypter encrypter;
	private Minecraft mc;

	public ServerConnection(Minecraft mc, Logger logger) {
		this.logger = logger;
		this.mc = mc;
		this.initialized = false;
	}

	public void start() {
		closed = false;
		try {
			reestablishConnection();
		} catch (IOException e) {
			logger.warn("Failed to connect to server");
			close();
			return;
		}
		figureOutEncryption();
		if (closed) {
			return;
		}
		Runnable failureCallback = new Runnable() {

			@Override
			public void run() {
				close();

			}
		};
		this.packetHandler = new ClientSidePacketForwarder(logger, input, encrypter, failureCallback);
		this.packetSender = new OutgoingDataHandler(output, encrypter, failureCallback);
		if (authPlayer()) {
			initialized = true;
			packetHandler.startHandling();
		} else {
			logger.info("Failed to connect to server");
			close();
		}
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void sendMessage(final IPacket packet) {
		if (closed) {
			logger.info("Tried to send packet, but connection was already closed");
			return;
		}
		packetSender.queuePacket(packet);
	}

	private void reestablishConnection() throws IOException {
		this.socket = new Socket();
		int port = WPClientForgeMod.getInstance().getConfig().useTestServer() ? WPStatics.testPort : WPStatics.port;
		socket.connect(new InetSocketAddress(serverAdress, port), 3000);
		try {
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		} catch (ConnectException e) {
			throw new IOException("Failed to connect to " + serverAdress + ":" + port);
		} catch (IOException e) {
			logger.error("Exception occured", e);
			throw e;
		}
	}

	private boolean authPlayer() {
		Session session = mc.getSession();
		String token = session.getToken();
		String uuidWithoutDash = session.getPlayerID();
		try {
			authAgainstSessionServer(token, uuidWithoutDash,
					ConnectionUtils.generateKeyHash(uuidWithoutDash, sharedSecret, serverPubKey.getEncoded()), logger);
		} catch (IOException e) {
			logger.error("Failed to auth against yggdrassil", e);
			return false;
		}
		sendMessage(new AuthPlayerPacket(session.getUsername(), uuidWithoutDash, tag));
		return true;
	}

	private void figureOutEncryption() {
		byte[] confirmationKey = new byte[16];
		try {
			int keyLength = VarInt.readVarInt(input);
			byte[] keyData = new byte[keyLength];
			input.readFully(keyData, 0, keyLength);
			X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(keyData);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			serverPubKey = kf.generatePublic(X509publicKey);
			input.readFully(confirmationKey, 0, 16);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Failed to figure out encryption stuff", e);
			close();
			return;
		}
		SecureRandom rng = new SecureRandom();
		sharedSecret = new byte[16];
		rng.nextBytes(sharedSecret); // gen random secret
		encrypter = new AES_CFB8_Encrypter(sharedSecret, sharedSecret);
		byte[] encryptedSecret = PKCSEncrypter.encrypt(sharedSecret, serverPubKey);
		byte[] encryptedConfirmationKey = PKCSEncrypter.encrypt(confirmationKey, serverPubKey);
		try {
			VarInt.writeVarInt(output, encryptedSecret.length);
			output.write(encryptedSecret);
			VarInt.writeVarInt(output, encryptedConfirmationKey.length);
			output.write(encryptedConfirmationKey);
		} catch (IOException e) {
			logger.error("Failed to send encryption reply", e);
			close();
		}
	}

	public boolean isClosed() {
		return closed;
	}

	private void authAgainstSessionServer(String accessToken, String playerID, String sha, Logger logger)
			throws IOException {
		JSONObject json = new JSONObject();
		json.put("accessToken", accessToken);
		json.put("selectedProfile", playerID);
		json.put("serverId", sha);
		ConnectionUtils.sendPost(json.toString(), sessionServerAdress, logger);
	}

	private void close() {
		if (closed) {
			return;
		}
		closed = true;
		try {
			if (packetHandler != null) {
				packetHandler.stopHandling();
			}
			if (packetSender != null) {
				packetSender.stop();
			}
			socket.close();
		} catch (IOException e) {
			// its fine
		}
		WPClientForgeMod.getInstance().reconnect();
	}
}
