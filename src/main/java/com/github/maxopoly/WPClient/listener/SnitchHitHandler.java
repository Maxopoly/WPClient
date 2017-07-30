package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.WPClientForgeMod;

import com.github.maxopoly.WPClient.connection.ServerConnection;
import com.github.maxopoly.WPClient.packetHandling.PlayerLocationUpdatePacketHandler;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import com.github.maxopoly.WPCommon.model.SnitchHitAction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

public class SnitchHitHandler {

	private static final Pattern snitchPattern = Pattern
			.compile("\\s*\\*\\s*([^\\s]*)\\s\\b(entered snitch at|logged out in snitch at|logged in to snitch at)"
					+ "\\b\\s*([^\\s]*)\\s\\[([^\\s]*)\\s([-\\d]*)\\s([-\\d]*)\\s([-\\d]*)\\]");

	private Logger logger;

	public SnitchHitHandler(Logger logger) {
		this.logger = logger;
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChat(ClientChatReceivedEvent e) {
		if (!WPClientForgeMod.getInstance().isConnectionReady()) {
			return;
		}
		String msg = e.getMessage().getUnformattedText();
		msg = StringUtils.stripControlCodes(msg);
		// strip hover text
		msg = msg.replaceAll("(?i)\\u00A7[a-z0-9]", "");
		Matcher matcher = snitchPattern.matcher(msg);
		if (!matcher.matches()) {
			return;
		}
		ServerConnection conn = WPClientForgeMod.getInstance().getServerConnection();
		String playerName = matcher.group(1);
		String activity = matcher.group(2);
		String snitchName = matcher.group(3);
		String worldName = matcher.group(4);
		int x = Integer.parseInt(matcher.group(5));
		int y = Integer.parseInt(matcher.group(6));
		int z = Integer.parseInt(matcher.group(7));
		// TODO parse this out
		SnitchHitAction action = SnitchHitAction.ENTER;
		Location loc = new Location(x, y, z);
		LocationTracker.getInstance().reportSnitchLocation(playerName, loc);
		PlayerLocationUpdatePacketHandler.stagePlayerForUpdate(playerName);
	}

}
