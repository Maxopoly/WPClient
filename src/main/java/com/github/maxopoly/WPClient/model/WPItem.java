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

	private String genPrettyName() {
		Item item = Item.getItemById(this.id);
		String name = item.getItemStackDisplayName(new ItemStack(item));
		String[] splitName = name.split("\\.");
		return splitName[splitName.length - 1];
	}
}
