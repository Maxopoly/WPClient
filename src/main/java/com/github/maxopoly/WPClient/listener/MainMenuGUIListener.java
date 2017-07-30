package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.gui.GuiAccountSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MainMenuGUIListener {

	private static final int buttonID = 38;

	@SubscribeEvent
	public void onMainMenuLaunch(net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent e) {
		if (!(e.getGui() instanceof GuiMainMenu)) {
			return;
		}
		GuiMainMenu menu = (GuiMainMenu) e.getGui();
		int j = menu.height / 4 + 48;
		GuiButton button = new GuiButton(buttonID, menu.width / 2 - 100, j + 72 + 36, "Switch accounts");
		e.getButtonList().add(button);
	}

	@SubscribeEvent
	public void onMainMenuClick(GuiScreenEvent.ActionPerformedEvent.Post e) {
		if (!(e.getGui() instanceof GuiMainMenu)) {
			return;
		}
		if (e.getButton().id == buttonID) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiAccountSelection());
		}
	}

}
