package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.WPConfiguration;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPWayPoint;
import com.github.maxopoly.WPCommon.model.WPWayPointGroup;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.display.WaypointGroup;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Logger;

public class WPWayPointHandler {

	private static WPWayPointHandler instance;

	public static WPWayPointHandler getInstance() {
		return instance;
	}

	private IClientAPI jmAPI;
	private Logger logger;
	private Minecraft mc;

	private Map<WPWayPointGroup, Set<Waypoint>> wayPoints;
	private Map<WPWayPointGroup, WaypointGroup> groups;
	private int refreshRate;

	private ScheduledExecutorService scheduler;

	WPWayPointHandler(IClientAPI jmAPI, Logger logger, Minecraft mc) {
		this.jmAPI = jmAPI;
		this.logger = logger;
		this.mc = mc;
		this.wayPoints = new HashMap<WPWayPointGroup, Set<Waypoint>>();
		this.groups = new HashMap<WPWayPointGroup, WaypointGroup>();
		instance = this;
		adjustRefreshIntervall(WPClientForgeMod.getInstance().getConfig().getWayPointRefreshRate());
	}

	public void adjustRefreshIntervall(long ms) {
		if (scheduler != null) {
			scheduler.shutdown();
		}
		scheduler = Executors.newScheduledThreadPool(1);
		FMLLog.getLogger().info("Scheduled with " + ms);
		scheduler.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				int configRate = WPClientForgeMod.getInstance().getConfig().getWayPointRefreshRate();
				if (configRate == refreshRate) {
					updateWayPoints();
				} else {
					refreshRate = configRate;
					adjustRefreshIntervall(configRate);
				}
			}
		}, ms, ms, TimeUnit.MILLISECONDS);
	}

	public synchronized void overwriteWayPoints(Set<WPWayPoint> providedPoints) {
		for (Set<Waypoint> points : wayPoints.values()) {
			for (Waypoint point : points) {
				JourneyMapPlugin.dirtyWayPointRemoval(point);
			}
			points.clear();
		}
		for (WPWayPoint point : providedPoints) {
			Set<Waypoint> groupSet = wayPoints.get(point.getGroup());
			if (groupSet == null) {
				groupSet = new HashSet<Waypoint>();
				wayPoints.put(point.getGroup(), groupSet);
			}
			Waypoint jmPoint = new Waypoint(WPClientForgeMod.MODID, point.getName(), 0,
					JourneyMapPlugin.convertPosition(point.getLocation()));
			WaypointGroup group = groups.get(point.getGroup());
			if (group == null) {
				group = new WaypointGroup(WPClientForgeMod.MODID, point.getGroup().name());
				groups.put(point.getGroup(), group);
			}
			jmPoint.setGroup(group);
			jmPoint.setColor(point.getColor());
			jmPoint.setEditable(false);
			jmPoint.setPersistent(false);
			jmPoint.setDisplayed(0, false);
			groupSet.add(jmPoint);
		}
		updateWayPoints();
	}

	public synchronized void updateWayPoints() {
		FMLLog.getLogger().info("Updating");
		if (mc.thePlayer == null) {
			return;
		}
		WPConfiguration config = WPClientForgeMod.getInstance().getConfig();
		Location playerLoc = new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
		FMLLog.getLogger().info("Handling");
		for (Entry<WPWayPointGroup, Set<Waypoint>> entry : wayPoints.entrySet()) {
			FMLLog.getLogger().info("Handling " + entry.getKey().toString());
			boolean hideAll = !config.isWPWayPointVisible(entry.getKey());
			int maxDistance = config.getMaxWPWayPointDistance(entry.getKey());
			for (Waypoint point : entry.getValue()) {
				FMLLog.getLogger().info("Point " + point.getName());
				Location pointLoc = JourneyMapPlugin.convertPosition(point.getPosition());
				int distance = playerLoc.distance(pointLoc);
				FMLLog.getLogger().info("Distance " + distance);
				FMLLog.getLogger().info(hideAll);
				if (point.isDisplayed(0)) {
					FMLLog.getLogger().info("a");
					if (hideAll || distance > maxDistance) {
						FMLLog.getLogger().info("Hiding " + point.getName());
						point.setDisplayed(0, false);
					}
				} else {
					FMLLog.getLogger().info("b");
					if (maxDistance == 0 || (distance <= maxDistance && !hideAll)) {
						FMLLog.getLogger().info("Showing " + point.getName());
						point.setDisplayed(0, true);
					}
				}
				try {
					jmAPI.show(point);
				} catch (Exception e) {
					logger.error("Failed to update waypoint", e);
				}
			}
		}
	}
}
