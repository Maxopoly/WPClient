package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.journeyMap.PlayerLocationWaypointHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class SkyNetListener {

	// I can steal code too, fuck you teal

	private static Minecraft mc;
	private List<String> previousPlayerList = new ArrayList<String>();

	public SkyNetListener() {
		mc = Minecraft.getMinecraft();
	}

	public String filterChatColors(String s) {
		return TextFormatting.getTextWithoutFormattingCodes(s);
	}

	public static void onPlayerJoin(String player) {
		showMessage(player, "joined", TextFormatting.DARK_GREEN);
	}

	public static void onPlayerLeave(String player) {
		showMessage(player, "left", TextFormatting.GRAY);
	}

	private static void showMessage(String player, String action, TextFormatting actionColor) {
		if (PlayerLocationWaypointHandler.isBarCode(player)) {
			player = String.format("%s (%s)", player, PlayerLocationWaypointHandler.barCodeDeobfuscate(player));
		}
		mc.thePlayer.addChatMessage(new TextComponentString("[WPC] ").setStyle(
				new Style().setColor(TextFormatting.DARK_AQUA)).appendSibling(
				new TextComponentString(String.format("%s %s the game", player, action)).setStyle(new Style()
						.setColor(actionColor))));
	}

	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			if (mc.theWorld != null) {
				ArrayList<String> playerList = new ArrayList<String>();
				Collection<NetworkPlayerInfo> players = mc.getConnection().getPlayerInfoMap();
				for (Object o : players) {
					if ((o instanceof NetworkPlayerInfo)) {
						NetworkPlayerInfo info = (NetworkPlayerInfo) o;
						playerList.add(filterChatColors(info.getGameProfile().getName()));
					}
				}
				ArrayList<String> temp = (ArrayList<String>) playerList.clone();
				playerList.removeAll(previousPlayerList);
				previousPlayerList.removeAll(temp);
				for (String player : previousPlayerList) {
					onPlayerLeave(player);
				}
				for (String player : playerList) {
					onPlayerJoin(player);
				}
				previousPlayerList = temp;
			}
		}
	}
}
