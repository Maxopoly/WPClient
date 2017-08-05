package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.model.AccountCache;
import com.github.maxopoly.WPClient.packetHandling.PlayerLocationUpdatePacketHandler;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.Player;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.model.MapImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.apache.logging.log4j.Logger;

public class PlayerLocationWaypointHandler {

	// after this time waypoints will show when the location was last reported
	private static final int timerStartMilliSeconds = 5000;
	// 30 minutes
	private static final int playerLocationTimeout = 900000;

	private Map<String, ModWaypoint> existingWaypoints;
	private IClientAPI jmAPI;
	private Logger logger;
	private Minecraft mc;
	private MapImage icon;

	public PlayerLocationWaypointHandler(IClientAPI jmAPI, Logger logger, Minecraft mc) {
		logger.info("Creating handler");
		this.existingWaypoints = new HashMap<String, ModWaypoint>();
		this.mc = mc;
		this.jmAPI = jmAPI;
		this.logger = logger;
		this.icon = new MapImage(new ResourceLocation("wpclient:images/head.png"), 32, 32).setAnchorX(16).setAnchorY(16);
	}

	public ModWaypoint createPlayerWaypoint(String playerName, Location loc) {
		ModWaypoint point = existingWaypoints.get(playerName);
		AccountCache accountCache = AccountCache.getInstance();
		Player player = accountCache.getPlayerInfoFor(playerName);
		String wayPointName = constructPlayerInfoString(playerName, player);
		int standing;
		if (player != null) {
			standing = player.getTransitiveStanding();
		} else {
			standing = 0;
		}
		int color = getStandingColor(standing);
		if (point != null) {
			point.setPoint(new BlockPos(loc.getX(), loc.getY() + 1, loc.getZ()));
			point.setWaypointName(wayPointName);
			point.setColor(color);
		} else {
			point = new ModWaypoint(WPClientForgeMod.MODID, wayPointName + ";;" + "WPC", "playerLocations", wayPointName,
					(int) loc.getX(), (int) loc.getY() + 1, (int) loc.getZ(), icon, color, false, 0);
			// this needs to be done explicitly because the api is bugged
			point.setPersistent(false);
			existingWaypoints.put(playerName, point);
		}
		try {
			jmAPI.show(point);
		} catch (Exception e) {
			logger.error("Failed to add waypoint", e);
		}
		return point;
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onTick(ClientTickEvent event) {
		if (mc.theWorld == null) {
			return;
		}
		if (!WPClientForgeMod.getInstance().isEnabled()) {
			return;
		}
		Map<String, ModWaypoint> pointsToUpdate = new HashMap<String, ModWaypoint>(existingWaypoints);
		LocationTracker tracker = LocationTracker.getInstance();
		AccountCache accountCache = AccountCache.getInstance();
		List<String> toUpdate = PlayerLocationUpdatePacketHandler.popLocationsToUpdate();
		for (Entity entity : mc.theWorld.playerEntities) {
			if (!(entity instanceof EntityOtherPlayerMP)) {
				continue;
			}
			toUpdate.remove(entity.getName());
			Vec3d pos = entity.getPositionVector();
			createPlayerWaypoint(entity.getName(), new Location(pos.xCoord, pos.yCoord, pos.zCoord));
			pointsToUpdate.remove(entity.getName());
		}
		for (String leftOver : toUpdate) {
			if (leftOver.equals(mc.thePlayer.getName())) {
				continue;
			}
			createPlayerWaypoint(leftOver, tracker.getLastKnownLocation(leftOver).getLocation());
			pointsToUpdate.remove(leftOver);
		}
		// all other way points are old and their timer needs to be updated
		Iterator<Entry<String, ModWaypoint>> iter = pointsToUpdate.entrySet().iterator();
		long currentTime = System.currentTimeMillis();
		while (iter.hasNext()) {
			Entry<String, ModWaypoint> entry = iter.next();
			String playerName = entry.getKey();
			if (playerName.equals(mc.thePlayer.getName())) {
				continue;
			}
			Player play = accountCache.getPlayerInfoFor(playerName);
			int standing;
			if (play != null) {
				standing = play.getTransitiveStanding();
			} else {
				standing = 0;
			}
			int color = getStandingColor(standing);
			long lastSeen = tracker.getLastKnownLocation(playerName).getTimeStamp();
			long sinceLastSeen = currentTime - lastSeen;
			ModWaypoint wayPoint = entry.getValue();
			if (sinceLastSeen > timerStartMilliSeconds) {
				if (sinceLastSeen > playerLocationTimeout) {
					JourneyMapPlugin.dirtyWayPointRemoval(wayPoint);
					continue;
				}
				String wayPointName = constructPlayerInfoString(playerName, play);
				String newName = wayPointName + "  " + constructTimeString(sinceLastSeen);
				if (newName.equals(wayPoint.getWaypointName()) && color == wayPoint.getColor()) {
					continue;
				}
				wayPoint.setWaypointName(newName);
				wayPoint.setColor(color);
				try {
					jmAPI.show(wayPoint);
				} catch (Exception e) {
					logger.error("Failed to update waypoint", e);
				}
			}
		}
	}

	private String constructPlayerInfoString(String accName, Player player) {
		if (player != null && !player.getMain().getName().equals(accName)) {
			return accName + " (" + player.getMain().getName() + ")";
		}
		return accName;
	}

	private String constructTimeString(long millis) {
		int seconds = (int) (millis / 1000);
		int minutes = seconds / 60;
		seconds -= minutes * 60;
		StringBuilder sb = new StringBuilder();
		if (minutes > 0) {
			sb.append(minutes + " m");
		}
		if (seconds > 0) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(seconds + " s");
		}
		return sb.toString();
	}

	private static int getStandingColor(int standing) {
		if (standing >= 0) {
			return scaleColor(0xffff66, 0x009933, standing / 10.0);
		}
		return scaleColor(0xffff66, 0xff0000, -1 * (standing / 10.0));
	}

	private static int scaleColor(int lowerBound, int upperBound, double progress) {
		if (progress > 1.0 || progress < 0.0) {
			throw new IllegalArgumentException("Progress must be within [0,1]");
		}
		return subScaleInt(lowerBound, upperBound, progress, 16) | subScaleInt(lowerBound, upperBound, progress, 8)
				| subScaleInt(lowerBound, upperBound, progress, 0);
	}

	private static int subScaleInt(int lower, int upper, double progress, int shift) {
		int shiftedLower = (lower >>> shift) & 0xff;
		int shiftedUpper = (upper >>> shift) & 0xff;
		return ((int) (((shiftedUpper - shiftedLower) * progress) + shiftedLower) & 0xff) << shift;
	}
}
