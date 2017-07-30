package com.github.maxopoly.WPClient;

import com.github.maxopoly.WPClient.connection.ServerConnection;
import com.github.maxopoly.WPClient.listener.ChestContentListener;
import com.github.maxopoly.WPClient.listener.JEI_GUI_Listener;
import com.github.maxopoly.WPClient.listener.MainMenuGUIListener;
import com.github.maxopoly.WPClient.listener.PlayerProximityListener;
import com.github.maxopoly.WPClient.listener.SnitchHitHandler;
import com.github.maxopoly.WPClient.packetCreation.PlayerLocationPacket;
import com.github.maxopoly.WPClient.session.SessionManager;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.LoggedPlayerLocation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = WPClientForgeMod.MODID, version = WPClientForgeMod.VERSION)
public class WPClientForgeMod {
	public static final String MODID = "wpclient";
	public static final String VERSION = "1.0";

	private final static String serverIP = "mc.civclassic.com";

	private static WPClientForgeMod instance;

	private Minecraft mc;
	private ServerConnection connection;
	private SessionManager sessionManager;
	private Logger logger;
	private boolean enabled;

	public static WPClientForgeMod getInstance() {
		return instance;
	}

	public ServerConnection getServerConnection() {
		return connection;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		instance = this;
		mc = Minecraft.getMinecraft();
		logger = FMLLog.getLogger();
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new SnitchHitHandler(logger));
		MinecraftForge.EVENT_BUS.register(new JEI_GUI_Listener());
		MinecraftForge.EVENT_BUS.register(new ChestContentListener());
		MinecraftForge.EVENT_BUS.register(new MainMenuGUIListener());
		MinecraftForge.EVENT_BUS.register(new PlayerProximityListener());
		sessionManager = new SessionManager(mc, logger);
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
		}, 5, 500, TimeUnit.MILLISECONDS);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onConnect(ClientConnectedToServerEvent e) {
		String ip = mc.getCurrentServerData().serverIP;
		if (ip.endsWith(serverIP)) {
			enabled = true;
			logger.info("Enabling WPClient, connecting to " + ip);
		} else {
			enabled = false;
			logger.info("Disabling WPClient, wrong IP " + ip);
		}
	}

	public void reconnect() {
		logger.info("Disconnected from server. Attempting to reconnect in 10 seconds");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {

		}
		new Thread(new Runnable() {

			@Override
			public void run() {
				connection = new ServerConnection(mc, logger);
				connection.start();
			}
		}).start();
		;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isConnectionReady() {
		return isEnabled() && connection != null && connection.isInitialized() && !connection.isClosed();
	}

	private void sendPlayerLocations() {
		if (!isEnabled() || !connection.isInitialized()) {
			return;
		}
		LocationTracker tracker = LocationTracker.getInstance();
		List<String> pendingUpdates = tracker.pullAndClearRecentlyUpdatedPlayers();
		if (pendingUpdates.isEmpty()) {
			return;
		}
		Set<LoggedPlayerLocation> players = new HashSet<LoggedPlayerLocation>();
		for (String player : pendingUpdates) {
			players.add(tracker.getLastKnownLocation(player));
		}
		PlayerLocationPacket updatePacket = new PlayerLocationPacket(players);
		connection.sendMessage(updatePacket.getMessage());
	}

}
