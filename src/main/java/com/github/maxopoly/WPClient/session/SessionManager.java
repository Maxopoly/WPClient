package com.github.maxopoly.WPClient.session;

import com.github.maxopoly.WPClient.model.PlayerAuth;
import com.github.maxopoly.WPClient.util.AuthTokenManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class SessionManager {

	private static final String launcherProfileFileName = "launcher_profiles.json";
	public static final long authTimeOut = 5 * 60 * 1000; // 5 minutes
	public static final long authCoolDown = 30 * 1000; // wait at least 10 sec between auth

	private boolean oldFormat;
	private File launcherProfileFile;
	private Logger logger;

	private String clientToken;
	private Map<String, PlayerAuth> knownAuth;

	public SessionManager(Minecraft mc, Logger logger) {
		this.logger = logger;
		this.knownAuth = new HashMap<String, PlayerAuth>();
		launcherProfileFile = new File(mc.mcDataDir.getAbsolutePath() + File.separator + launcherProfileFileName);
		if (!launcherProfileFile.exists()) {
			FMLLog.getLogger().info("File did not exist");
			return;
		}
		reloadFileContent();
	}

	public boolean hasValidToken(PlayerAuth auth) {
		if (!auth.isValid()) {
			return false;
		}
		if ((System.currentTimeMillis() - auth.getLastVerified()) < authTimeOut) {
			return auth.isValid();
		}
		try {
			boolean result = AuthTokenManager.validateToken(logger, auth.getAuthToken(), clientToken);
			auth.setValid(result);
			auth.updateVerifyTimeStamp();
			return result;
		} catch (IOException e) {
			logger.error("Failed check whether token is valid", e);
			return false;
		}
	}

	public boolean refreshToken(PlayerAuth auth) {
		try {
			String token = AuthTokenManager.refreshToken(logger, auth.getAuthToken(), clientToken);
			auth.updateToken(token);
			auth.updateVerifyTimeStamp();
			writeBackToFile(auth);
			return true;
		} catch (IOException e) {
			logger.error("Failed to refresh token", e);
			auth.setValid(false);
			auth.breakAuth();
			return false;
		}
	}

	public Collection<PlayerAuth> getAvailableAuth() {
		return new LinkedList<PlayerAuth>(knownAuth.values());
	}

	public void writeBackToFile(PlayerAuth auth) {
		JSONObject json = loadAuthJson();
		if (json == null) {
			return;
		}
		json.put("clientToken", clientToken);
		JSONObject authSection = json.getJSONObject("authenticationDatabase");
		if (authSection == null) {
			FMLLog.getLogger().info("No auth section found in file to input replaced token");
			return;
		}

		JSONObject authObj;

		if (oldFormat) {
			authObj = authSection.optJSONObject(auth.getUUID());
		} else {
			authObj = authSection.optJSONObject(auth.getUserId());
		}
		if (authObj == null) {
			FMLLog.getLogger().info("Specific auth section didnt exist in token file, could not input replaced token");
			return;
		}
		authObj.put("accessToken", auth.getAuthToken());
		try (FileWriter writer = new FileWriter(launcherProfileFile)) {
			writer.write(json.toString());
		} catch (IOException e) {
			logger.error("Failed to save auth tokens to save file", e);
			return;
		}
	}

	private JSONObject loadAuthJson() {
		if (!launcherProfileFile.exists()) {
			logger.info("Could not load auth token file because it didnt exist?");
			return null;
		}
		String content;
		try {
			content = new String(Files.readAllBytes(launcherProfileFile.toPath()));
		} catch (IOException e) {
			logger.error("Failed to read profile file", e);
			return null;
		}
		return new JSONObject(content);
	}

	public void reloadFileContent() {
		JSONObject json = loadAuthJson();
		if (json == null) {
			return;
		}
		clientToken = json.optString("clientToken", UUID.randomUUID().toString());
		JSONObject authSection = json.optJSONObject("authenticationDatabase");
		if (authSection == null) {
			FMLLog.getLogger().info("No auth section found in file");
			return;
		}
		JSONArray names = authSection.names();
		if (names == null) {
			FMLLog.getLogger().info("Auth section in file was empty");
			return;
		}
		knownAuth.clear();
		for (int i = 0; i < names.length(); i++) {
			String identifier = names.getString(i);
			JSONObject authObj = authSection.getJSONObject(identifier);
			String name = authObj.optString("displayName", null);
			String accessToken = authObj.getString("accessToken");
			String email = authObj.getString("username");
			oldFormat = name != null;
			PlayerAuth auth;
			if (oldFormat) {
				String userID = authObj.getString("userid");
				auth = new PlayerAuth(name, accessToken, email, identifier, userID);
			} else {
				JSONObject profiles = authObj.optJSONObject("profiles");
				if (profiles == null) {
					continue;
				}
				JSONArray profileNames = profiles.names();
				if (profileNames == null) {
					continue;
				}
				String uuid = "";
				for (int k = 0; k < profileNames.length(); k++) {
					JSONObject section = profiles.getJSONObject(profileNames.getString(k));
					uuid = profileNames.getString(k);
					name = section.getString("displayName");
				}
				auth = new PlayerAuth(name, accessToken, email, uuid, identifier);
			}
			logger.info("Loaded " + auth.toString());
			knownAuth.put(auth.getName(), auth);
			FMLLog.getLogger().info("Loading auth for player " + name);
		}
	}
}
