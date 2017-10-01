package com.github.maxopoly.WPClient;

import com.github.maxopoly.WPClient.connection.ServerConnection;
import com.github.maxopoly.WPClient.journeyMap.MapDataSyncSession;
import com.github.maxopoly.WPClient.listener.ChestContentListener;
import com.github.maxopoly.WPClient.listener.IngameGUIListener;
import com.github.maxopoly.WPClient.listener.JEI_GUI_Listener;
import com.github.maxopoly.WPClient.listener.MainMenuGUIListener;
import com.github.maxopoly.WPClient.listener.MiscListener;
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
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = WPClientForgeMod.MODID, version = WPClientForgeMod.VERSION, guiFactory = "com.github.maxopoly.WPClient.gui.WPConfigGuiFactory")
public class WPClientForgeMod {
	public static final String MODID = "wpclient";
	public static final String VERSION = "1.1";

	private static WPClientForgeMod instance;

	private Minecraft mc;
	private ServerConnection connection;
	private SessionManager sessionManager;
	private Logger logger;
	private boolean enabled;
	private WPConfiguration config;

	public static WPClientForgeMod getInstance() {
		return instance;
	}

	public ServerConnection getServerConnection() {
		return connection;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public WPConfiguration getConfig() {
		return config;
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		MapDataSyncSession.deploySettings();
		config = new WPConfiguration(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		instance = this;
		mc = Minecraft.getMinecraft();
		logger = FMLLog.getLogger();
		logger.info("Enabling WPClient");
		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new SnitchHitHandler(logger));
		MinecraftForge.EVENT_BUS.register(new JEI_GUI_Listener());
		MinecraftForge.EVENT_BUS.register(new ChestContentListener());
		MinecraftForge.EVENT_BUS.register(new MainMenuGUIListener());
		MinecraftForge.EVENT_BUS.register(new PlayerProximityListener());
		MinecraftForge.EVENT_BUS.register(new IngameGUIListener());
		MinecraftForge.EVENT_BUS.register(new MiscListener("mc.civclassic.com"));

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
		}, 500, 500, TimeUnit.MILLISECONDS);
	}

	public void setFunctionalityEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public synchronized void reconnect() {
		if (connection == null) {
			// ensures we only reconnect once
			return;
		}
		connection = null;
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

	public boolean connectedToCivClassics() {
		return enabled;
	}

	/**
	 * @return Whether the player is connected to both civclassics and the wp server
	 */
	public boolean isConnectionReady() {
		return connectedToCivClassics() && connectedToWPServer();
	}

	/**
	 * @return Whether the player is connected to the WP server
	 */
	public boolean connectedToWPServer() {
		return connection != null && connection.isInitialized() && !connection.isClosed();
	}

	private void sendPlayerLocations() {
		if (!isConnectionReady()) {
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
		connection.sendMessage(updatePacket);
	}

}
