package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.gui.GuiAccountSelection;
import com.github.maxopoly.WPClient.gui.GuiMapSync;
import com.github.maxopoly.WPCommon.model.permission.Permission;
import com.github.maxopoly.WPCommon.model.permission.PermissionLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MainMenuGUIListener {

	private static final int switchButtonID = 38;
	private static final int mapButtonID = 39;

	@SubscribeEvent
	public void onMainMenuLaunch(net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent e) {
		if (!(e.getGui() instanceof GuiMainMenu)) {
			return;
		}
		PermissionLevel perms = WPClientForgeMod.getInstance().getPermissionLevel();
		if (perms == null) {
			return;
		}
		GuiMainMenu menu = (GuiMainMenu) e.getGui();
		int j = menu.height / 4 + 48;
		GuiButton switchButton = new GuiButton(switchButtonID, menu.width / 2 - 100, j + 72 + 36, 98, 20,
				"Switch accounts");
		GuiButton mapButton = new GuiButton(mapButtonID, menu.width / 2 + 2, j + 72 + 36, 98, 20, "Sync map");
		e.getButtonList().add(switchButton);
		if (perms.hasPermission(Permission.MAP_SYNC)) {
			e.getButtonList().add(mapButton);
		}
	}

	@SubscribeEvent
	public void onMainMenuClick(GuiScreenEvent.ActionPerformedEvent.Post e) {
		if (!(e.getGui() instanceof GuiMainMenu)) {
			return;
		}
		if (e.getButton().id == switchButtonID) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelection());
		} else if (e.getButton().id == mapButtonID) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiMapSync());
		}
	}

}
