package com.github.maxopoly.WPClient.connection;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPCommon.packetHandling.PacketForwarder;
import com.github.maxopoly.WPCommon.util.AES_CFB8_Encrypter;
import com.github.maxopoly.WPCommon.util.CompressionManager;
import com.github.maxopoly.WPCommon.util.ConnectionUtils;
import com.github.maxopoly.WPCommon.util.PKCSEncrypter;
import com.github.maxopoly.WPCommon.util.VarInt;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerConnection {

	private static final int port = 23452;
	private static final String serverAdress = "168.235.102.74";
	private final static String sessionServerAdress = "https://sessionserver.mojang.com/session/minecraft/join";

	private static final String tag = "awoo";

	private Logger logger;
	private PacketForwarder packetHandler;
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
		this.packetHandler = new ClientSidePacketForwarder(logger);
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
		authPlayer();
		logger.info("Successfully connected to server");
		handleAvailablePackets();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void handleAvailablePackets() {
		logger.info("Setting up packet listener for WPC");
		this.initialized = true;
		while (!closed) {
			try {
				while (input.available() > 0) {
					int packetLength = VarInt.readVarInt(input, encrypter);
					byte[] dataArray = new byte[packetLength];
					input.readFully(dataArray);
					byte[] decrypted = encrypter.decrypt(dataArray);
					byte[] decompressed = CompressionManager.decompress(decrypted, logger);
					String dataString = new String(decompressed, StandardCharsets.UTF_8);
					JSONObject json;
					try {
						json = new JSONObject(dataString);
					} catch (JSONException e) {
						logger.error("Received invalid msg that could not be turned into json: " + dataString);
						continue;
					}
					packetHandler.handlePacket(json);
				}
			} catch (IOException e) {
				logger.error("Error handling incoming packets", e);
				closed = true;
				WPClientForgeMod.getInstance().reconnect();
				break;
			}

		}
		logger.warn("Stopped packet handling, connection is gone?");
	}

	public void sendMessage(final JSONObject json) {
		if (closed) {
			logger.info("Tried to send json, but connection was already closed");
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					byte[] rawData = json.toString().getBytes(StandardCharsets.UTF_8);
					byte[] compressed = CompressionManager.compress(rawData);
					VarInt.writeVarInt(output, compressed.length, encrypter);
					byte[] encrypted = encrypter.encrypt(compressed);
					output.write(encrypted);
				} catch (IOException e) {
					logger.error("Error while sending packet", e);
					close();
				}
			}
		}).start();
	}

	private void reestablishConnection() throws IOException {
		this.socket = new Socket();
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

	private void authPlayer() {
		Session session = mc.getSession();
		String token = session.getToken();
		String uuidWithoutDash = session.getPlayerID();
		try {
			authAgainstSessionServer(token, uuidWithoutDash,
					ConnectionUtils.generateKeyHash(uuidWithoutDash, sharedSecret, serverPubKey.getEncoded()), logger);
		} catch (IOException e) {
			logger.error("Failed to auth against yggdrassil", e);
		}
		JSONObject playerInfoJson = new JSONObject();
		playerInfoJson.put("name", session.getUsername());
		playerInfoJson.put("uuid", uuidWithoutDash);
		playerInfoJson.put("tag", tag);
		sendMessage(playerInfoJson);
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
		closed = true;
		WPClientForgeMod.getInstance().reconnect();
	}

}
