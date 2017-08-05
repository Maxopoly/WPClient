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
			// check for potion tag if its a potion, splash potion or lingering potion
			if (itemID == 373 || itemID == 438 || itemID == 441) {
				String potionTag = tag.getString("Potion");
				if (potionTag != null && !potionTag.equals("")) {
					durability = PotionTranslater.getIdByName(potionTag);
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
		int itemID = item.getID();
		int dura = item.getDurability();
		// check for potion tag if its a potion, splash potion or lingering potion
		if (dura != 0 && (itemID == 373 || itemID == 438 || itemID == 441)) {
			NBTTagCompound tag = new NBTTagCompound();
			String potionName = PotionTranslater.getNameById(dura);
			if (potionName != null) {
				tag.setString("Potion", PotionTranslater.getNameById(dura));
				dura = 0;
				return new ItemStack(Item.getItemById(itemID), item.getAmount(), dura, tag);
			}
		}
		return new ItemStack(Item.getItemById(itemID), item.getAmount(), dura);
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

	public static String prettifyItemCountWaypointName(int id, int count, int totalCount, boolean showTotalPercentage) {
		String totalCountPerc = "";
		if (showTotalPercentage) {
			totalCountPerc = String.format("{%02d%%} ", Math.round((((double) count) / totalCount) * 100));
		}
		int stackSize = getStackSizeById(id);
		int compactionMultiplier = stackSize == 1 ? 8 : stackSize;
		int stacks = count / stackSize;
		if (stacks == 0) {
			return String.format("%s%d", totalCountPerc, count);
		}
		int leftover = count % stackSize;
		if (leftover == 0) {
			if (stacks == 27) {
				return String.format("%sSC", totalCountPerc);
			} else if (stacks == 54) {
				return String.format("%sDC", totalCountPerc);
			} else if (stacks == 27 * compactionMultiplier) {
				return String.format("%sSC compacted", totalCountPerc);
			} else if (stacks == 54 * compactionMultiplier) {
				return String.format("%sDC compacted", totalCountPerc);
			}
		}
		return String.format("%s%s%d x %d", totalCountPerc, leftover != 0 ? "~" : "", stacks, stackSize);
	}

	public static int calculateItemCount(Chest c) {
		int count = 0;
		for (WPItem item : c.getContent()) {
			if (!item.isCompacted()) {
				count += item.getAmount();
			} else {
				int stackSize = getStackSizeById(item.getID());
				int compactionMultiplier = stackSize == 1 ? 8 : stackSize;
				count += compactionMultiplier * item.getAmount();
			}
		}
		return count;
	}

	public static int getStackSizeById(int id) {
		Item item = Item.getItemById(id);
		return item.getItemStackLimit(new ItemStack(item));
	}

	public static String getPrettyName(WPItem wpItem) {
		if (wpItem.getID() == 373) {
			return PotionTranslater.getPrettyName(wpItem.getDurability()) + " Pot";
		}
		if (wpItem.getID() == 438) {
			return PotionTranslater.getPrettyName(wpItem.getDurability()) + " Splash Pot";
		}
		if (wpItem.getID() == 441) {
			return PotionTranslater.getPrettyName(wpItem.getDurability()) + " Lingering Pot";
		}
		return convertItem(wpItem).getDisplayName();

	}
}
