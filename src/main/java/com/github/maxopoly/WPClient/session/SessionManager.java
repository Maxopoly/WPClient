package com.github.maxopoly.WPClient.session;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class SessionManager {

	private static final String launcherProfileFileName = "launcher_profiles.json";

	private File launcherProfileFile;
	private Logger logger;

	private String clientToken;

	public SessionManager(Minecraft mc, Logger logger) {
		this.logger = logger;
		launcherProfileFile = new File(mc.mcDataDir.getAbsolutePath() + File.pathSeparator + launcherProfileFileName);
		if (!launcherProfileFile.exists()) {
			return;
		}
	}

	public void reloadFileContent() {
		String content;
		try {
			content = new String(Files.readAllBytes(launcherProfileFile.toPath()));
		} catch (IOException e) {
			logger.error("Failed to read profile file", e);
			return;
		}
		JSONObject json = new JSONObject(content);
	}

}
