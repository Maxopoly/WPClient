package com.github.maxopoly.WPClient;

import com.github.maxopoly.WPCommon.model.WPWayPointGroup;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.common.FMLLog;

public class WPConfiguration {

	private Configuration forgeConfig;
	private Property lastMapSync;
	private Property itemWayPointDistance;
	private Property itemWayPointTimer;
	private Property allyWayPointTimer;
	private Property neutWayPointTimer;
	private Property hostileWayPointTimer;
	private Property connectTestServer;
	private Property syncReminderInterval;
	private Map<WPWayPointGroup, Property[]> wayPointGroups;
	private Property wayPointRefreshRate;
	private Property showItemWaypointPercentage;

	public WPConfiguration(File file) {
		forgeConfig = new Configuration(file);
		forgeConfig.load();
		loadProperties();
	}

	private void loadProperties() {
		// ensure exists, not sure if needed
		forgeConfig.getCategory("JourneyMap");

		String jmSection = "Journeymap";
		String generalSection = "General";
		String customWayPointSection = "Waypoints";

		lastMapSync = forgeConfig.get(generalSection, "lastMapSync", 0);
		lastMapSync.setShowInGui(false);

		connectTestServer = forgeConfig.get(generalSection, "Use test server", false,
				"Don't use this unless you know what you are doing");

		itemWayPointDistance = forgeConfig.get(jmSection, "Item waypoint max distance", 0,
				"Items further away than this number will not show up" + " in item searches. Set to 0 for infinite", 0,
				5000);
		itemWayPointDistance.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

		itemWayPointTimer = forgeConfig.get(jmSection, "Item waypoint timer", 60,
				"How long item way points should take to disappear, measured in seconds", 5, 300);
		itemWayPointTimer.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

		allyWayPointTimer = forgeConfig.get(jmSection, "Ally waypoint timer", 5,
				"Once a waypoint of a player with positive standing reaches this timer, it disappears. Config value is in minutes,"
						+ " a value of 0 will only display live data", 0, 30);
		allyWayPointTimer.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

		neutWayPointTimer = forgeConfig.get(jmSection, "Neutral waypoint timer", 10,
				"Once a waypoint of a player with neutral standing reaches this timer, it disappears, Config value is in minutes,"
						+ " a value of 0 will only display live data", 0, 30);
		neutWayPointTimer.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

		hostileWayPointTimer = forgeConfig.get(jmSection, "Hostile waypoint timer", 20,
				"Once a waypoint of a player with hostile standing reaches this timer, it disappears, Config value is in minutes,"
						+ " a value of 0 will only display live data", 0, 30);
		hostileWayPointTimer.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

		syncReminderInterval = forgeConfig.get(generalSection, "Map sync reminder interval", 7,
				"How often you should be reminded to sync your map data, measured in days. Set to 0 for never", 0, 30);
		syncReminderInterval.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

		wayPointGroups = new HashMap<WPWayPointGroup, Property[]>();
		for (WPWayPointGroup pointGroup : WPWayPointGroup.values()) {
			String name = pointGroup.name();
			name = name.length() > 1 ? name.substring(0, 1).toUpperCase()
					+ name.substring(1, name.length()).toLowerCase() : name.toUpperCase();
			Property[] props = new Property[2];
			props[0] = forgeConfig.get(customWayPointSection, name + " enabled", true, "Whether waypoints marking "
					+ name + " are visible");
			props[1] = forgeConfig.get(customWayPointSection, name + " visible range", 1000,
					"Maximum distance at which waypoints marking " + name + " are visible. Set to 0 for infinite", 0,
					5000);
			props[1].setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);
			wayPointGroups.put(pointGroup, props);
		}
		wayPointRefreshRate = forgeConfig.get(customWayPointSection, "Waypoint refresh rate", 1000,
				"How often waypoints are refreshed to check whether they should be "
						+ "displayed/hidden based on how far they are away (in ms)", 200, 5000);
		wayPointRefreshRate.setConfigEntryClass(GuiConfigEntries.NumberSliderEntry.class);

		showItemWaypointPercentage = forgeConfig.get(customWayPointSection, "Show percentages in item waypoints", true,
				"Allows for sorting by clicking on 'Name' in the JM waypoint list");

		// save default values possibly created
		saveConfig();
	}

	public void saveConfig() {
		if (forgeConfig.hasChanged()) {
			forgeConfig.save();
		}
	}

	public long getLastMapSync() {
		return lastMapSync.getLong();
	}

	public void updateMapSyncTimeStamp() {
		FMLLog.getLogger().info(System.currentTimeMillis());
		lastMapSync.set((int) (System.currentTimeMillis() / 1000));
		saveConfig();
	}

	public int getWayPointRefreshRate() {
		return wayPointRefreshRate.getInt();
	}

	public int getMaxWPWayPointDistance(WPWayPointGroup group) {
		Property[] props = wayPointGroups.get(group);
		if (props == null) {
			return 0;
		}
		return props[1].getInt();
	}

	public boolean isWPWayPointVisible(WPWayPointGroup group) {
		Property[] props = wayPointGroups.get(group);
		if (props == null) {
			return true;
		}
		return props[0].getBoolean();
	}

	public int getMaxItemWayPointDistance() {
		return itemWayPointDistance.getInt();
	}

	public int getMapSyncReminderInterval() {
		return syncReminderInterval.getInt();
	}

	public int getMaxAllyTimer() {
		return allyWayPointTimer.getInt();
	}

	public int getMaxNeutTimer() {
		return neutWayPointTimer.getInt();
	}

	public int getMaxHostileTimer() {
		return hostileWayPointTimer.getInt();
	}

	public boolean useTestServer() {
		return connectTestServer.getBoolean();
	}

	public int getMaxItemWayPointTimer() {
		return itemWayPointTimer.getInt();
	}

	public boolean showItemWaypointPercentage() {
		return showItemWaypointPercentage.getBoolean();
	}

	public Configuration getForgeConfigObject() {
		return forgeConfig;
	}

}
