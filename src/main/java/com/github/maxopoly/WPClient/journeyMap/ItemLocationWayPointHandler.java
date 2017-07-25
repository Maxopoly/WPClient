package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPCommon.model.Location;
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

	public synchronized void markLocations(int id, Map<Location, Integer> amount) {
		hideAll();
		Item item = Item.getItemById(id);
		String name = item.getItemStackDisplayName(new ItemStack(item));
		String[] splitName = name.split("\\.");
		name = splitName[splitName.length - 1];
		if (amount.isEmpty() && mc.thePlayer != null) {
			mc.thePlayer.addChatMessage(new TextComponentString("[WPC] Server doesnt know of a chest containing " + name));
			return;
		}
		int sum = 0;
		for (Entry<Location, Integer> entry : amount.entrySet()) {
			createWaypoint(entry.getKey(), name, entry.getValue());
			sum += entry.getValue();
		}
		if (mc.thePlayer != null) {
			mc.thePlayer.addChatMessage(new TextComponentString("[WPC] Loaded " + amount.size()
					+ " locations for a total of " + sum + " " + name));
		}
	}

	public synchronized void hideAll() {
		for (ModWaypoint point : points) {
			jmAPI.remove(point);
		}
		points.clear();
	}

	private void createWaypoint(Location loc, String name, int amount) {
		ModWaypoint point = new ModWaypoint(WPClientForgeMod.MODID, loc.toString() + ";;WPC", "itemLocations", amount + " "
				+ name, (int) loc.getX(), (int) loc.getY(), (int) loc.getZ(), icon, 0x99ccff, false, 0);
		points.add(point);
		try {
			jmAPI.show(point);
		} catch (Exception e) {
			logger.warn("Failed to show waypoint", e);
		}

	}
}
