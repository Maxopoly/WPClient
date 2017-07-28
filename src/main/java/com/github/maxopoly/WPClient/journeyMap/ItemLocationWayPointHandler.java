package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPClient.model.WPItem;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.model.MapImage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Logger;

public class ItemLocationWayPointHandler {

	private static ItemLocationWayPointHandler instance;

	private Set<ModWaypoint> points;
	private MapImage icon;

	public static ItemLocationWayPointHandler getInstance() {
		return instance;
	}

	private IClientAPI jmAPI;
	private Logger logger;
	private Minecraft mc;

	ItemLocationWayPointHandler(IClientAPI jmAPI, Logger logger, Minecraft mc) {
		this.jmAPI = jmAPI;
		this.logger = logger;
		this.mc = mc;
		this.points = new HashSet<ModWaypoint>();
		instance = this;
		this.icon = new MapImage(new ResourceLocation("wpclient:images/head.png"), 32, 32).setAnchorX(16).setAnchorY(16);
	}

	public synchronized void markLocations(WPItem item, Map<Location, Integer> amount) {
		hideAll();
		if (amount.isEmpty() && mc.thePlayer != null) {
			mc.thePlayer.addChatMessage(new TextComponentString(
					String.format("%s[WPC]  %sCouldn't find any %s%s%s.",
						TextFormatting.BLUE, TextFormatting.DARK_RED, TextFormatting.WHITE, item.getPrettyName(),
						TextFormatting.DARK_RED)));
			return;
		}
		int sum = 0;
		for (Entry<Location, Integer> entry : amount.entrySet()) {
			createWaypoint(entry.getKey(), item.getPrettyName(), item.prettifyItemCountShort(entry.getValue()));
			sum += entry.getValue();
		}
		if (mc.thePlayer != null) {
			String s = "s";
			if (amount.size() == 1) {
				s = "";
			}
			mc.thePlayer.addChatMessage(new TextComponentString(String.format(
					"%s[WPC]  %sFound %s%s %sof %s%s %sin %d location%s.",
					TextFormatting.BLUE, TextFormatting.GRAY, TextFormatting.WHITE, item.prettifyItemCount(sum),
					TextFormatting.GRAY, TextFormatting.WHITE, item.getPrettyName(), TextFormatting.GRAY,
					amount.size(), s)));
		}
	}

	public synchronized void hideAll() {
		for (ModWaypoint point : points) {
			jmAPI.remove(point);
		}
		points.clear();
	}

	private void createWaypoint(Location loc, String name, String prettyAmount) {
		ModWaypoint point = new ModWaypoint(WPClientForgeMod.MODID, loc.toString() + ";;WPC", "itemLocations",
				prettyAmount + " " + name, (int) loc.getX(), (int) loc.getY(), (int) loc.getZ(), icon, 0x99ccff,
				false, 0);
		points.add(point);
		try {
			jmAPI.show(point);
		} catch (Exception e) {
			logger.warn("Failed to show waypoint", e);
		}

	}
}
