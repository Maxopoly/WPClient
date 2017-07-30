package com.github.maxopoly.WPClient.util;

import com.github.maxopoly.WPClient.model.PlayerAuth;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class AuthTokenManager {

	private final static String authServerAdress = "https://authserver.mojang.com";
	private final static String sessionServerAdress = "https://sessionserver.mojang.com/session/minecraft/join";

	public static String refreshToken(Logger logger, String accessToken, String clientToken) throws IOException {
		String result = sendPost(constructRefreshJSON(accessToken, clientToken), authServerAdress + "/refresh", logger);
		JSONObject jsonResult = new JSONObject(result);
		accessToken = jsonResult.getString("accessToken");
		String receivedClientToken = jsonResult.getString("clientToken");
		if (!clientToken.equals(receivedClientToken)) {
			throw new IOException("Received different client token during access token refresh");
		}
		return accessToken;
	}

	public static boolean validateToken(Logger logger, String accessToken, String clientToken) throws IOException {
		try {
			sendPost(constructValidationJSON(accessToken, clientToken), authServerAdress + "/validate", logger);
		} catch (IOException e) {
			if (e.getMessage().startsWith("POST to")) {
				// 403 response code
				return false;
			} else {
				throw e;
			}
		}
		// we dont have an actual response in this case, just a 204 response
		return true;
	}

	/**
	 * For some reason minecraft only allows setting the auth token once in the launcher and doesnt refresh it further,
	 * which results in invalid sessions. Here we do some dirty stuff to update this final token field.
	 */
	public static void overwriteAuthSession(PlayerAuth auth) {
		Minecraft mc = Minecraft.getMinecraft();
		Session replacementSession = new Session(auth.getName(), auth.getUUID(), auth.getAuthToken(), "mojang");
		try {
			Session session = mc.getSession();
			// we dont know the actual name of the field, because I dont feel like digging through tons of obfuscated code.
			// This will do
			for (Field field : mc.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				if (field.get(mc) == session) {
					// we found the session field!
					// make it not final
					Field modifiersField = Field.class.getDeclaredField("modifiers");
					modifiersField.setAccessible(true);
					modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
					// insert our new session
					field.set(mc, replacementSession);
				} else {
					field.setAccessible(false);
				}
			}
		} catch (NoSuchFieldException | IllegalAccessException e) {
			FMLLog.getLogger().error("Failed to set new session", e);
		}
	}

	public static void authAgainstSessionServer(String sha, Logger logger, String accessToken, String playerID)
			throws IOException {
		JSONObject json = new JSONObject();
		json.put("accessToken", accessToken);
		json.put("selectedProfile", playerID);
		json.put("serverId", sha);
		sendPost(json.toString(), sessionServerAdress, logger);
	}

	private static String sendPost(String content, String url, Logger logger) throws IOException {
		byte[] contentBytes = content.getBytes("UTF-8");
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Accept-Charset", "UTF-8");
		con.setRequestProperty("Content-Length", Integer.toString(contentBytes.length));

		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.write(contentBytes, 0, contentBytes.length);
		wr.close();
		int responseCode = con.getResponseCode();
		if ((responseCode / 100) != 2) { // we want a 200 something response code
			throw new IOException("POST to " + url + " returned bad response code " + responseCode);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	private static String constructRefreshJSON(String oldToken, String clientToken) {
		JSONObject json = new JSONObject();
		json.put("accessToken", oldToken);
		json.put("clientToken", clientToken);
		return json.toString();
	}

	private static String constructValidationJSON(String accessToken, String clientToken) {
		JSONObject json = new JSONObject();
		json.put("accessToken", accessToken);
		json.put("clientToken", clientToken);
		return json.toString();
	}

}
