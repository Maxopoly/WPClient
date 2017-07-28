package com.github.maxopoly.WPClient.model;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class WPItem {

	private int id;
	private String prettyName;

	public WPItem(int id) {
		this.id = id;
		this.prettyName = this.genPrettyName();
	}

	public int getID() {
		return this.id;
	}

	public String getPrettyName() {
		return this.prettyName;
	}

	public String prettifyItemCount(int count) {
		int stacks = count / 64;
		if (stacks == 0) {
			return String.format("%d", count);
		}
		int leftover = count % 64;
		if (leftover == 0) {
			return String.format("%d x 64 [%,d]", stacks, count);
		}
		return String.format("%d x 64 + %d [%,d]", stacks, leftover, count);
	}

	public String prettifyItemCountShort(int count) {
		int stacks = count / 64;
		if (stacks == 0) {
			return String.format("%d", count);
		}
		int leftover = count % 64;
		if (leftover == 0) {
			return String.format("%d x 64", stacks);
		}
		return String.format("~%d x 64", stacks);
	}

	private String genPrettyName() {
		Item item = Item.getItemById(this.id);
		String name = item.getItemStackDisplayName(new ItemStack(item));
		String[] splitName = name.split("\\.");
		return splitName[splitName.length - 1];
	}
}
