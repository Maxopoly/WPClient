package com.github.maxopoly.WPClient;

import com.github.maxopoly.WPClient.connection.ServerConnection;
import com.github.maxopoly.WPClient.packetCreation.RenderDistancePlayersPacket;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = WPClientForgeMod.MODID, version = WPClientForgeMod.VERSION)
public class WPClientForgeMod {
	public static final String MODID = "wpclient";
	public static final String VERSION = "1.0";

	private static WPClientForgeMod instance;

	private Minecraft mc;
	private ServerConnection connection;
	private Logger logger;

	public static WPClientForgeMod getInstance() {
		return instance;
	}

	public ServerConnection getServerConnection() {
		return connection;
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		instance = this;
		mc = Minecraft.getMinecraft();
		MinecraftForge.EVENT_BUS.register(this);
		logger = FMLLog.getLogger();
		connection = new ServerConnection(mc, logger);
		new Thread(new Runnable() {

			@Override
			public void run() {
				connection.start();
			}
		}).start();
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
		exec.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				sendPlayerLocations();
			}
		}, 1, 1, TimeUnit.SECONDS);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTick(ClientTickEvent event) {
		// update locations of all players in render distance
		if (mc.theWorld == null) {
			return;
		}
		LocationTracker tracker = LocationTracker.getInstance();
		for (Entity entity : mc.theWorld.playerEntities) {
			if (!(entity instanceof EntityOtherPlayerMP)) {
				continue;
			}
			Vec3d pos = entity.getPositionVector();
			tracker.reportLocation(entity.getName(), new Location(pos.xCoord, pos.yCoord, pos.zCoord));
		}
		Vec3d pos = mc.thePlayer.getPositionVector();
		tracker.reportLocation(mc.thePlayer.getName(), new Location(pos.xCoord, pos.yCoord, pos.zCoord));
	}

	public void reconnect() {
		logger.info("Disconnected from server. Attempting to reconnect in 10 seconds");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {

		}
		connection = new ServerConnection(mc, logger);
		connection.start();
	}

	private void sendPlayerLocations() {
		LocationTracker tracker = LocationTracker.getInstance();
		List<String> pendingUpdates = tracker.pullAndClearRecentlyUpdatedPlayers();
		if (pendingUpdates.isEmpty()) {
			return;
		}
		Map<String, Location> players = new HashMap<String, Location>();
		for (String player : pendingUpdates) {
			players.put(player, tracker.getLastKnownLocation(player));
		}
		RenderDistancePlayersPacket updatePacket = new RenderDistancePlayersPacket(players);
		connection.sendMessage(updatePacket.getMessage());
	}

}
