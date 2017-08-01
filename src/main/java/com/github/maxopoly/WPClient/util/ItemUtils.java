package com.github.maxopoly.WPClient.util;

import com.github.maxopoly.WPCommon.model.Chest;
import com.github.maxopoly.WPCommon.model.WPItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class ItemUtils {

	private static final String compactedLore = "Compacted Item";

	public static WPItem convertItem(ItemStack is) {
		int durability = is.getItemDamage();
		boolean enchanted = is.isItemEnchanted();
		int itemID = Item.getIdFromItem(is.getItem());
		int amount = is.stackSize;
		boolean compacted = false;
		NBTTagCompound tag = is.getTagCompound();
		if (tag != null) {
			NBTTagCompound displayTag = tag.getCompoundTag("display");
			if (displayTag != null) {
				NBTTagList loreList = displayTag.getTagList("Lore", NBT.TAG_STRING);
				if (loreList != null) {
					for (int k = 0; k < loreList.tagCount(); k++) {
						String loreLine = loreList.getStringTagAt(k);
						if (loreLine.equals(compactedLore)) {
							compacted = true;
							break;
						}
					}
				}
			}
		}
		return new WPItem(itemID, amount, durability, compacted, enchanted);
	}

	/**
	 * Turns a WPItem into a minecraft ItemStack. Only takes id, amount and durability into account, enchanting and
	 * compaction is ignored as this is only needed for getting the name of the item
	 *
	 * @param item
	 *          Item to convert
	 * @return Equivalent ItemStack
	 */
	public static ItemStack convertItem(WPItem item) {
		return new ItemStack(Item.getItemById(item.getID()), item.getAmount(), item.getDurability());
	}

	public static String prettifyItemCount(int id, int count) {
		int stackSize = getStackSizeById(id);
		if (stackSize == 1) {
			return String.valueOf(count);
		}
		int stacks = count / stackSize;
		if (stacks == 0) {
			return String.format("%d", count);
		}
		int leftover = count % stackSize;
		if (leftover == 0) {
			return String.format("%d x %d [%,d]", stacks, stackSize, count);
		}
		return String.format("%d x %d + %d [%,d]", stacks, stackSize, leftover, count);
	}

	public static String prettifyItemCountWaypointName(int id, int count) {
		int stackSize = getStackSizeById(id);
		if (stackSize == 1) {
			return String.valueOf(count);
		}
		int stacks = count / stackSize;
		if (stacks == 0) {
			return String.format("%d", count);
		}
		int leftover = count % stackSize;
		if (leftover == 0) {
			if (stacks == 27) {
				return "SC";
			} else if (stacks == 54) {
				return "DC";
			} else if (stacks == 27 * stackSize) {
				return "SC compacted";
			} else if (stacks == 54 * stackSize) {
				return "DC compacted";
			}
			return String.format("%d x %d", stacks, stackSize);
		}
		return String.format("~%d x %d", stacks, stackSize);
	}

	public static int calculateItemCount(Chest c) {
		int count = 0;
		for (WPItem item : c.getContent()) {
			if (!item.isCompacted()) {
				count += item.getAmount();
			} else {
				count += getStackSizeById(item.getID()) * item.getAmount();
			}
		}
		return count;
	}

	public static int getStackSizeById(int id) {
		Item item = Item.getItemById(id);
		return item.getItemStackLimit(new ItemStack(item));
	}

	public static String getPrettyName(WPItem wpItem) {
		Item item = Item.getItemById(wpItem.getID());
		ItemStack itemStack = new ItemStack(item, 0, wpItem.getDurability());
		return itemStack.getDisplayName();
	}
}
