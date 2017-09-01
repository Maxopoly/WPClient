package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.MessageHandler;
import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.WPConfiguration;
import com.github.maxopoly.WPClient.journeyMap.MapDataSyncSession;
import com.github.maxopoly.WPClient.model.AccountCache;
import java.util.LinkedList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

public class MiscListener {

	private String serverIp;
	private LinkedList<String> cachedPlayers;
	private boolean reminded;

	public MiscListener(String serverIp) {
		this.serverIp = serverIp;
		this.cachedPlayers = new LinkedList<String>();
		this.reminded = false;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onConnect(ClientConnectedToServerEvent e) {
		String ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;
		FMLLog.getLogger().info("[WPC]Connecting to  " + ip);
		boolean enabled;
		if (ip.endsWith(serverIp)) {
			enabled = true;
			FMLLog.getLogger().info("[WPC]Enabling functionality as player is connecting to right ip");
			fixJourneyMapFolder();
		} else {
			enabled = false;
			FMLLog.getLogger().info("[WPC]Disabling functionality as player is connecting to wrong ip");
		}
		WPClientForgeMod.getInstance().setFunctionalityEnabled(enabled);
		if (reminded == false) {
			reminded = true;
			WPConfiguration config = WPClientForgeMod.getInstance().getConfig();
			long sinceLastSync = System.currentTimeMillis() - config.getLastMapSync();
			if (config.getMapSyncReminderIntervall() != 0
					&& sinceLastSync > (config.getMapSyncReminderIntervall() * 24 * 60 * 60)) {
				String msg;
				if (config.getLastMapSync() == 0) {
					msg = "You have never";
				} else {
					msg = "It has been " + (sinceLastSync / 60 / 60 / 24) + " days since you last";
				}
				MessageHandler.getInstance()
						.queueMessage(
								new TextComponentString("[WPC] " + msg
										+ " synced your map data. Syncing it now is recommended"));
			}
		}
	}

	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if (event.phase == Phase.START) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.theWorld != null && WPClientForgeMod.getInstance().isConnectionReady()) {
				LinkedList<String> currentPlayers = new LinkedList<String>();
				for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
					currentPlayers.add(StringUtils.stripControlCodes(info.getGameProfile().getName()));
				}
				LinkedList<String> joined = (LinkedList<String>) currentPlayers.clone();
				joined.removeAll(cachedPlayers);
				for (String joinPlayer : joined) {
					// load data when player joins, so it's available when needed
					AccountCache.getInstance().getPlayerInfoFor(joinPlayer);
				}
				cachedPlayers = currentPlayers;
			}
			if (mc.thePlayer != null) {
				for (ITextComponent comp : MessageHandler.getInstance().popMessages()) {
					mc.thePlayer.addChatMessage(comp);
				}
			}
		}
	}

	@SubscribeEvent
	public void onConfigChanged(OnConfigChangedEvent event) {
		if (event.getModID().equals(WPClientForgeMod.MODID)) {
			WPClientForgeMod.getInstance().getConfig().saveConfig();
		}
	}

	private void fixJourneyMapFolder() {
		ServerData data = Minecraft.getMinecraft().getCurrentServerData();
		data.serverName = MapDataSyncSession.getWorldFolderName();
	}
}
