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
import journeymap.client.api.display.ModWaypoint;
import journeymap.client.api.model.MapImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

public class ItemLocationWayPointHandler {

	private static ItemLocationWayPointHandler instance;

	private Set<ModWaypoint> points;
	private ScheduledExecutorService cleanUpExec;
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

	public synchronized void markLocations(WPItem item, List<Chest> chests) {
		hideAll();
		scheduleRemoval();
		String prettyName = ItemUtils.getPrettyName(item);
		ITextComponent msgNoItems = new TextComponentString(String.format("%s[WPC]  %sCouldn't find any %s%s%s.",
				TextFormatting.WHITE, TextFormatting.GRAY, TextFormatting.WHITE, prettyName, TextFormatting.GRAY));
		if (chests.isEmpty() && mc.thePlayer != null) {
			mc.thePlayer.addChatMessage(msgNoItems);
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
			if (sum == 0) {
				mc.thePlayer.addChatMessage(msgNoItems);
			} else {
				mc.thePlayer.addChatMessage(new TextComponentString(String.format(
						"%s[WPC]  %sFound %s%s %sof %s%s %sin %d location%s.", TextFormatting.WHITE, TextFormatting.GRAY,
						TextFormatting.WHITE, ItemUtils.prettifyItemCount(item.getID(), sum), TextFormatting.GRAY,
						TextFormatting.WHITE, prettyName, TextFormatting.GRAY, chests.size(), s)));
			}
		}
	}

	public boolean isLocationValid(Location loc) {
		World world = Minecraft.getMinecraft().theWorld;
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (world == null || player == null) {
			return true;
		}
		Location playerLoc = new Location(player.posX, player.posY, player.posZ);
		BlockPos pos = JourneyMapPlugin.convertPosition(loc);
		if (loc.distance(playerLoc) < 32) {
			// must be loaded
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
		for (ModWaypoint point : points) {
			JourneyMapPlugin.dirtyWayPointRemoval(point);
			// jmAPI.remove(point);
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
		boolean showTotalPercentage = WPClientForgeMod.getInstance().getConfig().showItemWaypointPercentage();
		ModWaypoint point = new ModWaypoint(WPClientForgeMod.MODID, loc.toString() + ";;WPC", "items",
				ItemUtils.prettifyItemCountWaypointName(item.getID(), itemCount, totalCount, showTotalPercentage) + " " + name,
				new BlockPos(loc.getX(), loc.getY(), loc.getZ()), icon, color, false, 0);
		point.setPersistent(false);
		point.setColor(color);
		point.setEditable(false);
		points.add(point);
		try {
			jmAPI.show(point);
		} catch (Exception e) {
			logger.warn("Failed to show waypoint", e);
		}

	}
}
