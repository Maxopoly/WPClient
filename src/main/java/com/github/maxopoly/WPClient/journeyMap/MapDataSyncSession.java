package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.packetCreation.MapSyncPacket;
import com.github.maxopoly.WPCommon.model.CoordPair;
import com.github.maxopoly.WPCommon.model.WPMappingTile;
import com.github.maxopoly.WPCommon.util.MapDataFileHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.commons.io.FileUtils;

public class MapDataSyncSession extends MapDataFileHandler {

	private static int sessionCounter = 0;

	private static MapDataSyncSession instance;

	private String status;
	private int sessionID;
	private int expectedReturnFiles;
	private int receivedReturnFiles;
	private boolean active;
	private Map<CoordPair, WPMappingTile> cachedTiles;

	public MapDataSyncSession(int sessionID) {
		super(FMLLog.getLogger());
		this.sessionID = sessionID;
		status = WPClientForgeMod.getInstance().connectedToWPServer() ? "Connected to server and ready to sync"
				: "No connection to server, can't sync";
	}

	public synchronized static MapDataSyncSession getInstance() {
		if (instance == null) {
			return newSyncSession();
		}
		return instance;
	}

	public synchronized static MapDataSyncSession newSyncSession() {
		if (instance != null) {
			instance.active = false;
		}
		instance = new MapDataSyncSession(sessionCounter++);
		return instance;
	}

	public int getID() {
		return sessionID;
	}

	public synchronized String getStatus() {
		return status;
	}

	public synchronized void setStatus(String status) {
		this.status = status;
	}

	public synchronized void setExpectedReturnFiles(int expected) {
		this.expectedReturnFiles = expected;
		this.receivedReturnFiles = 0;
	}

	public synchronized boolean isActive() {
		return active;
	}

	public synchronized void cancel() {
		active = false;
		status = "Cancelled sync";
		newSyncSession();
	}

	public synchronized void incrementReturnFileCounter() {
		this.receivedReturnFiles++;
		status = String.format("Receiving merged map data from server  (%d / %d) downloaded", receivedReturnFiles,
				expectedReturnFiles);
	}

	public void finish() {
		saveCachedTileHashes(cachedTiles);
		WPClientForgeMod.getInstance().getConfig().updateMapSyncTimeStamp();
		status = "Successfully updated your map data. " + receivedReturnFiles + " files were updated";
		active = false;
	}

	public synchronized void initSync() {
		status = "Started collecting local map data";
		File folder = getDayDataFolder();
		File[] pics = folder.listFiles();
		status = String.format("Detected a total of %d possible local map data files", pics.length);
		List<WPMappingTile> localTiles = new LinkedList<WPMappingTile>();
		cachedTiles = loadCachedTileHashes();
		for (int i = 0; i < pics.length; i++) {
			status = String.format("Loading file hashes and time stamps, (%d / %d) done", i, pics.length);
			File f = pics[i];
			Matcher m = regionPicRegex.matcher(f.getName());
			if (m.matches()) {
				int x = Integer.parseInt(m.group(1));
				int z = Integer.parseInt(m.group(2));
				WPMappingTile tile = cachedTiles.get(new CoordPair(x, z));
				long lastModified = f.lastModified();
				if (tile == null || lastModified != tile.getTimeStamp()) {
					tile = loadMapTile(f);
					tile.getHash(); // calculates hash, so it is available when sending data
				}
				if (tile != null) {
					localTiles.add(tile);
				}
			}
		}
		if (WPClientForgeMod.getInstance().connectedToWPServer()) {
			status = "Sent index of local map data, awaiting server reply";
			WPClientForgeMod.getInstance().getServerConnection().sendMessage(new MapSyncPacket(localTiles, sessionID));
		} else {
			active = false;
			status = "Lost connection to server, cancelled sync";
		}
	}

	public void handleReceivedTile(WPMappingTile tile) {
		incrementReturnFileCounter();
		saveTile(tile);
		cachedTiles.put(tile.getCoords(), new WPMappingTile(tile.getTimeStamp(), tile.getCoords().getX(), tile.getCoords()
				.getZ(), tile.getHash()));
	}

	@Override
	public File getBaseDirectory() {
		return Minecraft.getMinecraft().mcDataDir;
	}

	public File getColorPalette() {
		return new File(getBaseDirectory().getAbsolutePath() + File.separator + "journeymap" + File.separator
				+ "colorpalette.json");
	}

	@Override
	public String getMapDataPath() {
		return "journeymap" + File.separator + "data" + File.separator + "mp" + File.separator + getWorldFolderName();
	}

	public static String getWorldFolderName() {
		return "CivClassicsWPClientData";
	}

	public static void deploySettings() {
		URL paletteUrl = MapDataSyncSession.class.getResource("/colorpalette.json");
		File mcFolder = Minecraft.getMinecraft().mcDataDir;
		File jmFolder = new File(mcFolder, "journeymap");
		File configFolder = new File(jmFolder, "config");
		File latestConfigFolder = new File(configFolder, "5.4");
		latestConfigFolder.mkdirs();
		File paletteFile = new File(jmFolder, "colorpalette.json");
		try {
			FileUtils.copyURLToFile(paletteUrl, paletteFile);
			copyConfigFile(latestConfigFolder, "journeymap.topo.config", true);
			copyConfigFile(latestConfigFolder, "journeymap.core.config", true);
			copyConfigFile(latestConfigFolder, "journeymap.minimap.config", false);
			copyConfigFile(latestConfigFolder, "journeymap.waypoint.config", false);
			copyConfigFile(latestConfigFolder, "journeymap.fullmap.config", false);

		} catch (IOException e) {
			FMLLog.getLogger().error("Failed to place files", e);
		}
	}

	private static void copyConfigFile(File folder, String name, boolean overwrite) {
		URL configUrl = MapDataSyncSession.class.getResource("/" + name);
		File file = new File(folder, name);
		if (!file.exists() || overwrite) {
			try {
				FileUtils.copyURLToFile(configUrl, file);
			} catch (IOException e) {
				FMLLog.getLogger().error("Failed to place files", e);
			}
		}
	}
}
