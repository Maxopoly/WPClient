package com.github.maxopoly.WPClient.gui;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiWPConfig extends GuiConfig {

	public GuiWPConfig(GuiScreen parentScreen) {
		super(parentScreen, getConfigElements(WPClientForgeMod.getInstance().getConfig().getForgeConfigObject()),
				WPClientForgeMod.MODID, false, false, GuiConfig.getAbridgedConfigPath(WPClientForgeMod.getInstance()
						.getConfig().getForgeConfigObject().toString()));
	}

	private static List<IConfigElement> getConfigElements(Configuration configuration) {
		List<IConfigElement> elements = new ArrayList<IConfigElement>();
		for (String name : configuration.getCategoryNames()) {
			ConfigCategory category = configuration.getCategory(name);
			if (category.parent == null) {
				elements.add(new ConfigElement(category));
			}
		}
		return elements;
	}
}
