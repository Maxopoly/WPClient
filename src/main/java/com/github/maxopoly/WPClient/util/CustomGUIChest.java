package com.github.maxopoly.WPClient.util;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.packetCreation.ChestContentPacket;
import com.github.maxopoly.WPCommon.model.Location;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;

public class CustomGUIChest extends GuiChest {

	private Location location;

	public CustomGUIChest(IInventory upperInv, IInventory lowerInv, Location location) {
		super(upperInv, lowerInv);
		this.location = location;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		logAndSendInventories();
	}

	private void logAndSendInventories() {
		if (!WPClientForgeMod.getInstance().isConnectionReady()) {
			return;
		}
		Map<Integer, Integer> content = new TreeMap<Integer, Integer>();
		IInventory inv = getLowerInv();
		if (inv == null) {
			return;
		}
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack is = inv.getStackInSlot(i);
			if (is == null) {
				continue;
			}
			int amount = is.stackSize;
			if (amount <= 0) {
				continue;
			}
			int itemID = Item.getIdFromItem(is.getItem());
			Integer previousCount = content.get(itemID);
			if (previousCount == null) {
				previousCount = 0;
			}
			int newCount = previousCount + amount;
			content.put(itemID, newCount);
		}
		WPClientForgeMod.getInstance().getServerConnection()
				.sendMessage(new ChestContentPacket(location, content).getMessage());
	}

	private IInventory getUpperInv() {
		return retrieveInv("field_147016_v");
	}

	private IInventory getLowerInv() {
		return retrieveInv("field_147015_w");
	}

	private IInventory retrieveInv(String fieldIdentifier) {
		try {
			Field f = this.getClass().getSuperclass().getDeclaredField(fieldIdentifier);
			f.setAccessible(true);
			return (IInventory) f.get(this);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			FMLLog.getLogger().error("Failed to get inv via reflection", e);
			return null;
		}
	}

}
