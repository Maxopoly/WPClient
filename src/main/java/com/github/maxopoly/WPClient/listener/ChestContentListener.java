package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.util.CustomGUIChest;
import com.github.maxopoly.WPCommon.model.Location;
import java.lang.reflect.Field;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChestContentListener {

	private static final long clickTimeOut = 1000;

	private Location lastClickedChest;
	private long timeStamp;

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onChestOpen(GuiOpenEvent e) {
		if (!WPClientForgeMod.getInstance().isConnectionReady()) {
			return;
		}
		if (lastClickedChest == null) {
			return;
		}
		if (System.currentTimeMillis() - timeStamp > clickTimeOut) {
			return;
		}
		if (!(e.getGui() instanceof GuiChest)) {
			return;
		}
		GuiChest ogChest = (GuiChest) e.getGui();
		CustomGUIChest replacement = constructCustomChestGUI(ogChest);
		if (replacement != null) {
			e.setGui(replacement);
		}
	}

	@SubscribeEvent
	public void onBlockInteract(PlayerInteractEvent event) {
		BlockPos pos = event.getPos();
		int itemID = getBlockID(pos);
		if (!isChest(itemID)) {
			return;
		}
		timeStamp = System.currentTimeMillis();
		lastClickedChest = adJustChestLocation(pos);
	}

	public static int getBlockID(BlockPos pos) {
		IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(pos);
		return Block.getIdFromBlock(state.getBlock());
	}

	public static Location adJustChestLocation(BlockPos pos) {
		int mainID = getBlockID(pos);
		int southID = getBlockID(pos.south());
		if (mainID == southID) {
			pos = pos.south();
		} else {
			int eastID = getBlockID(pos.east());
			if (eastID == mainID) {
				pos = pos.east();
			}
		}
		return new Location(pos.getX(), pos.getY(), pos.getZ());

	}

	public static boolean isChest(int id) {
		return id == 146 || id == 54;
	}

	private CustomGUIChest constructCustomChestGUI(GuiChest original) {
		// get fields via reflection and use them as constructor parameters for our version
		try {
			Field upperField = original.getClass().getDeclaredField("field_147016_v");
			upperField.setAccessible(true);
			IInventory upper = (IInventory) upperField.get(original);
			Field lowerField = original.getClass().getDeclaredField("field_147015_w");
			lowerField.setAccessible(true);
			IInventory lower = (IInventory) lowerField.get(original);
			return new CustomGUIChest(upper, lower, lastClickedChest);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			FMLLog.getLogger().error("Failed to reflect chest fields", e);
			return null;
		}

	}

	/**
	 * Useful for getting field names after version changes
	 */
	private static void printFieldNames() {
		GuiChest ogChest = null;
		Field[] fields = ogChest.getClass().getDeclaredFields();
		for (Field field : fields) {
			FMLLog.getLogger().info("CCC " + field.getName() + "  " + field.getType().toString());
		}
	}

}
