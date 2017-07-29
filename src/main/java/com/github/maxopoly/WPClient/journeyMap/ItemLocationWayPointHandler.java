package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.util.ItemUtils;
import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPItem;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.model.MapImage;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Logger;

public class ItemLocationWayPointHandler {

	private static ItemLocationWayPointHandler instance;

	private Set<ModWaypoint> points;
	private MapImage icon;
	private ScheduledExecutorService cleanUpExec;

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

	public synchronized void markLocations(WPItem item, List<Chest> chests) {
		hideAll();
		String prettyName = ItemUtils.getPrettyName(item.getID());
		if (chests.isEmpty() && mc.thePlayer != null) {
			mc.thePlayer.addChatMessage(new TextComponentString(String.format(
					"%s[WPC]  %sError:  %sCouldn't find any %s%s%s.", TextFormatting.BLUE, TextFormatting.RED,
					TextFormatting.GRAY, TextFormatting.WHITE, prettyName, TextFormatting.GRAY)));
			return;
		}
		int sum = 0;
		for (Chest chest : chests) {
			createWaypoint(item, chest, prettyName);
			sum += ItemUtils.calculateItemCount(chest);
		}
		if (mc.thePlayer != null) {
			String s = "s";
			if (chests.size() == 1) {
				s = "";
			}
			mc.thePlayer.addChatMessage(new TextComponentString(String.format(
					"%s[WPC]  %sFound %s%s %sof %s%s %sin %d location%s.", TextFormatting.BLUE, TextFormatting.GRAY,
					TextFormatting.WHITE, ItemUtils.prettifyItemCount(item.getID(), sum), TextFormatting.GRAY,
					TextFormatting.WHITE, prettyName, TextFormatting.GRAY, chests.size(), s)));
		}
		scheduleRemoval();
	}

	public void scheduleRemoval() {
		if (cleanUpExec != null) {
			cleanUpExec.shutdownNow();
		}
		cleanUpExec = Executors.newScheduledThreadPool(1);
		cleanUpExec.schedule(new Runnable() {

			@Override
			public void run() {
				hideAll();
			}
		}, 60, TimeUnit.SECONDS);
	}

	public synchronized void hideAll() {
		for (ModWaypoint point : points) {
			jmAPI.remove(point);
		}
		points.clear();
	}

	private void createWaypoint(WPItem item, Chest chest, String name) {
		Location loc = chest.getLocation();
		// TODO include all items in chest
		ModWaypoint point = new ModWaypoint(WPClientForgeMod.MODID, loc.toString() + ";;WPC", "itemLocations",
				ItemUtils.prettifyItemCountWaypointName(item.getID(), 666) + " " + name, (int) loc.getX(), (int) loc.getY(),
				(int) loc.getZ(), icon, 0x99ccff, false, 0);
		points.add(point);
		try {
			jmAPI.show(point);
		} catch (Exception e) {
			logger.warn("Failed to show waypoint", e);
		}

	}
}
