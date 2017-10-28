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
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.model.MapImage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

public class WPWayPointHandler {

	private static WPWayPointHandler instance;

	public static WPWayPointHandler getInstance() {
		return instance;
	}

	private Logger logger;
	private Minecraft mc;

	private Map<WPWayPointGroup, Set<ModWaypoint>> wayPoints;
	private int refreshRate;
	private MapImage icon;

	private ScheduledExecutorService scheduler;

	WPWayPointHandler(Logger logger, Minecraft mc) {
		this.logger = logger;
		this.mc = mc;
		this.icon = new MapImage(new ResourceLocation("wpclient:images/head.png"), 32, 32).setAnchorX(16)
				.setAnchorY(16);
		this.wayPoints = new HashMap<WPWayPointGroup, Set<ModWaypoint>>();
		instance = this;
		this.refreshRate = 1000;
		adjustRefreshIntervall(refreshRate);
	}

	public void adjustRefreshIntervall(long ms) {
		if (scheduler != null) {
			scheduler.shutdown();
		}
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {
				int configRate = WPClientForgeMod.getInstance() != null
						&& WPClientForgeMod.getInstance().getConfig() != null ? WPClientForgeMod.getInstance()
						.getConfig().getWayPointRefreshRate() : refreshRate;
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
		for (Set<ModWaypoint> points : wayPoints.values()) {
			for (ModWaypoint point : points) {
				JourneyMapPlugin.dirtyWayPointRemoval(point);
				// jmAPI.remove(point);
			}
			points.clear();
		}
		int i = 0;
		for (WPWayPoint point : providedPoints) {
			Set<ModWaypoint> groupSet = wayPoints.get(point.getGroup());
			if (groupSet == null) {
				groupSet = new HashSet<ModWaypoint>();
				wayPoints.put(point.getGroup(), groupSet);
			}
			ModWaypoint jmPoint = new ModWaypoint(WPClientForgeMod.MODID, "WPPoints" + (i++), "wpShared",
					point.getName(), JourneyMapPlugin.convertPosition(point.getLocation()), icon, point.getColor(),
					false, 0);
			jmPoint.setColor(point.getColor());
			jmPoint.setEditable(false);
			jmPoint.setPersistent(false);
			groupSet.add(jmPoint);
		}
		updateWayPoints();
	}

	public synchronized void updateWayPoints() {
		if (mc.thePlayer == null) {
			return;
		}
		WPConfiguration config = WPClientForgeMod.getInstance().getConfig();
		Location playerLoc = new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
		for (Entry<WPWayPointGroup, Set<ModWaypoint>> entry : wayPoints.entrySet()) {
			boolean hideAll = !config.isWPWayPointVisible(entry.getKey());
			int maxDistance = config.getMaxWPWayPointDistance(entry.getKey());
			for (ModWaypoint point : entry.getValue()) {
				Location pointLoc = JourneyMapPlugin.convertPosition(point.getPoint());
				int distance = playerLoc.distance(pointLoc);
				if (point.getDimensions()[0] == 0) {
					if (hideAll || (distance > maxDistance && maxDistance != 0)) {
						JourneyMapPlugin.dirtyWayPointRemoval(point);
						point.setDimensions(55);
						continue;
					}
				} else {
					if ((maxDistance == 0 || distance <= maxDistance) && !hideAll) {
						point.setDimensions(0);
					}
				}
				try {
					JourneyMapPlugin.queueWayPointToShow(point);
				} catch (Exception e) {
					logger.error("Failed to update waypoint", e);
				}
			}
		}
	}
}
