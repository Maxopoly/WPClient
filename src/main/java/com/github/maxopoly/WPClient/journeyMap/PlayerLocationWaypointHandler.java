package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.model.AccountCache;
import com.github.maxopoly.WPClient.packetCreation.RequestPlayerInfo;
import com.github.maxopoly.WPClient.packetHandling.PlayerLocationUpdatePacketHandler;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	private static final int playerLocationTimeout = 1800000;

	private Map<String, ModWaypoint> existingWaypoints;
	private IClientAPI jmAPI;
	private Logger logger;
	private Minecraft mc;
	private MapImage icon;
	private Set<String> requestedNames;

	public PlayerLocationWaypointHandler(IClientAPI jmAPI, Logger logger, Minecraft mc) {
		logger.info("Creating handler");
		this.existingWaypoints = new HashMap<String, ModWaypoint>();
		this.mc = mc;
		this.requestedNames = new HashSet<String>();
		this.jmAPI = jmAPI;
		this.logger = logger;
		this.icon = new MapImage(new ResourceLocation("wpclient:images/head.png"), 32, 32).setAnchorX(16).setAnchorY(16);
	}

	public ModWaypoint createPlayerWaypoint(String playerName, Location loc) {
		ModWaypoint point = existingWaypoints.get(playerName);
		AccountCache accountCache = AccountCache.getInstance();
		Player player = accountCache.getPlayerInfoFor(playerName);
		int standing;
		if (player != null) {
			standing = player.getStanding();
		} else {
			standing = 0;
			if (!requestedNames.contains(playerName)) {
				WPClientForgeMod.getInstance().getServerConnection()
						.sendMessage(new RequestPlayerInfo(playerName).getMessage());
				requestedNames.add(playerName);
			}
		}
		int color = getStandingColor(standing);
		if (point != null) {
			point.setPoint(new BlockPos(loc.getX(), loc.getY() + 1, loc.getZ()));
			point.setWaypointName(playerName);
			point.setColor(color);
		} else {
			point = new ModWaypoint(WPClientForgeMod.MODID, playerName + ";;" + "WPC", "playerLocations", playerName,
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
			pointsToUpdate.remove(createPlayerWaypoint(entity.getName(), new Location(pos.xCoord, pos.yCoord, pos.zCoord)));
		}
		for (String leftOver : toUpdate) {
			if (leftOver.equals(mc.thePlayer.getName())) {
				continue;
			}
			pointsToUpdate.remove(createPlayerWaypoint(leftOver, tracker.getLastKnownLocation(leftOver)));
		}
		// all other way points are old and their timer needs to be updated
		for (Entry<String, ModWaypoint> entry : pointsToUpdate.entrySet()) {
			String player = entry.getKey();
			if (player.equals(mc.thePlayer.getName())) {
				continue;
			}
			Player play = accountCache.getPlayerInfoFor(player);
			int standing;
			if (play != null) {
				standing = play.getStanding();
			} else {
				standing = 0;
			}
			int color = getStandingColor(standing);
			long sinceLastSeen = tracker.getMillisSinceLastReport(player);
			if (sinceLastSeen > timerStartMilliSeconds) {
				ModWaypoint wayPoint = entry.getValue();
				String newName = player + "  " + constructTimeString(sinceLastSeen);
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

	private int getStandingColor(int standing) {
		if (standing > 5) {
			return 0x009933;
		}
		if (standing > 0) {
			return 0x99ff66;
		}
		if (standing == 0) {
			return 0xffff66;
		}
		if (standing > -5) {
			return 0xff9933;
		}
		return 0xff0000;
	}
}
