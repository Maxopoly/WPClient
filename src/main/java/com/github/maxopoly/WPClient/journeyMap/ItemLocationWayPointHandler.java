package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.listener.ChestContentListener;
import com.github.maxopoly.WPClient.packetCreation.ChestDeletionPacket;
import com.github.maxopoly.WPClient.util.ItemUtils;
import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.WPItem;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.display.WaypointGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

public class ItemLocationWayPointHandler {

	private static ItemLocationWayPointHandler instance;

	private Set<Waypoint> points;
	private ScheduledExecutorService cleanUpExec;
	private WaypointGroup group;

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
		this.points = new HashSet<Waypoint>();
		this.group = new WaypointGroup(WPClientForgeMod.MODID, "items");
		instance = this;
	}

	public synchronized void markLocations(WPItem item, List<Chest> chests) {
		hideAll();
		scheduleRemoval();
		String prettyName = ItemUtils.getPrettyName(item);
		if (chests.isEmpty() && mc.thePlayer != null) {
			mc.thePlayer.addChatMessage(new TextComponentString(String.format(
					"%s[WPC]  %sError:  %sCouldn't find any %s%s%s.", TextFormatting.BLUE, TextFormatting.RED,
					TextFormatting.GRAY, TextFormatting.WHITE, prettyName, TextFormatting.GRAY)));
			return;
		}
		int sum = 0;
		Iterator<Chest> iter = chests.iterator();
		Location playerLoc = null;
		if (mc.thePlayer != null) {
			playerLoc = new Location(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
		}
		int maxDistance = WPClientForgeMod.getInstance().getConfig().getMaxItemWayPointDistance();
		while (iter.hasNext()) {
			Chest chest = iter.next();
			if (!isLocationValid(chest.getLocation())) {
				if (WPClientForgeMod.getInstance().isConnectionReady()) {
					WPClientForgeMod.getInstance().getServerConnection()
							.sendMessage(new ChestDeletionPacket(chest.getLocation()));
				}
				iter.remove();
				continue;
			}
			if (maxDistance > 0 && playerLoc != null && playerLoc.distance(chest.getLocation()) > maxDistance) {
				iter.remove();
				continue;
			}
			sum += ItemUtils.calculateItemCount(chest);
		}
		for (Chest chest : chests) {
			createWaypoint(item, chest, prettyName, sum);
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
	}

	public boolean isLocationValid(Location loc) {
		World world = Minecraft.getMinecraft().theWorld;
		if (world == null) {
			return true;
		}
		BlockPos pos = JourneyMapPlugin.convertPosition(loc);
		if (world.isBlockLoaded(pos)) {
			int id = ChestContentListener.getBlockID(pos);
			// chest or obfuscated
			if (ChestContentListener.isChest(id)) {
				if (!ChestContentListener.adJustChestLocation(pos).equals(loc)) {
					return false;
				}
				return true;
			} else {
				if (id == 1) {
					// stone, assume it's obfuscated
					return true;
				}
				return false;
			}
		}
		return true;
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
		}, WPClientForgeMod.getInstance().getConfig().getMaxItemWayPointTimer(), TimeUnit.SECONDS);
	}

	public synchronized void hideAll() {
		for (Waypoint point : points) {
			JourneyMapPlugin.dirtyWayPointRemoval(point);
		}
		points.clear();
	}

	private void createWaypoint(WPItem item, Chest chest, String name, int totalCount) {
		Location loc = chest.getLocation();
		int stackSize = ItemUtils.getStackSizeById(item.getID());
		int compactionMultiplier = stackSize == 1 ? 8 : stackSize;
		int itemCount = 0;
		int color = 0x66fff;
		for (WPItem chestItem : chest.getContent()) {
			if (chestItem.getAmount() == 0) {
				continue;
			}
			if (chestItem.isCompacted()) {
				itemCount += compactionMultiplier * chestItem.getAmount();
				color = 0x9966ff;
			} else {
				itemCount += chestItem.getAmount();
			}
			if (chestItem.isEnchanted()) {
				color = 0x99ccff;
			}
		}
		Waypoint point = new Waypoint(WPClientForgeMod.MODID, ItemUtils.prettifyItemCountWaypointName(item.getID(),
				itemCount, totalCount, false) + " " + name, 0, new BlockPos(loc.getX(), loc.getY(), loc.getZ()));
		point.setPersistent(false);
		point.setColor(color);
		point.setEditable(false);
		point.setGroup(group);
		points.add(point);
		try {
			jmAPI.show(point);
		} catch (Exception e) {
			logger.warn("Failed to show waypoint", e);
		}

	}
}
