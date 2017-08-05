package com.github.maxopoly.WPClient.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PotionTranslater {

	private static Map<Integer, String> byId = new TreeMap<Integer, String>();
	private static Map<String, Integer> byName = new HashMap<String, Integer>();
	private static Map<Integer, String> prettyNamebyId = new TreeMap<Integer, String>();

	private static String[] potions = new String[] { "empty", "water", "mundane", "thick", "awkward", "night_vision",
			"long_night_vision", "invisibility", "long_invisibility", "leaping", "strong_leaping", "long_leaping",
			"fire_resistance", "long_fire_resistance", "swiftness", "strong_swiftness", "long_swiftness", "slowness",
			"long_slowness", "water_breathing", "long_water_breathing", "healing", "strong_healing", "harming",
			"strong_harming", "poison", "strong_poison", "long_poison", "regeneration", "strong_regeneration",
			"long_regeneration", "strength", "strong_strength", "long_strength", "weakness", "long_weakness", "luck" };

	static {
		for (int i = 1; i <= potions.length; i++) {
			String potName = potions[i - 1];
			String name = "minecraft:" + potions[i - 1];
			byId.put(i, name);
			byName.put(name, i);
			String[] split = potName.split("_");
			if (split.length > 1) {
				StringBuilder sb = new StringBuilder();
				boolean modified = split[0].equals("strong") || split[0].equals("long");
				for (int k = modified ? 1 : 0; k < split.length; k++) {
					sb.append(capitalize(split[k]));
					sb.append(" ");
				}
				String actualPotion = sb.toString();
				// cut off space at the end
				actualPotion = actualPotion.substring(0, actualPotion.length() - 1);
				String finalName;
				if (split[0].equals("strong")) {
					finalName = actualPotion + " II";
				} else {
					if (split[0].equals("long")) {
						finalName = actualPotion + " Ext.";
					} else {
						finalName = actualPotion + " I";
					}
				}
				prettyNamebyId.put(i, finalName);
			} else {
				prettyNamebyId.put(i, capitalize(potName) + " I");
			}
		}
	}

	private static String capitalize(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
	}

	public static String getPrettyName(int id) {
		return prettyNamebyId.get(id);
	}

	public static int getIdByName(String name) {
		Integer id = byName.get(name);
		if (id == null) {
			return 0;
		}
		return id;
	}

	public static String getNameById(int id) {
		return byId.get(id);
	}

}
