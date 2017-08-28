package com.github.maxopoly.WPClient.gui;

import com.github.maxopoly.WPClient.journeyMap.MapDataSyncSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

public class GuiMapSync extends GuiScreen {

	/** The screen to return to when this closes (always Main Menu). */
	protected GuiScreen prevScreen;

	private MapDataSyncSession session;

	private GuiButton backButton;

	@Override
	public void initGui() {
		session = MapDataSyncSession.newSyncSession();
		this.backButton = new GuiButton(0, this.width / 2 - 154, this.height - 52, 150, 20, "Back");
		addButton(backButton);
		new Thread(new Runnable() {

			@Override
			public void run() {
				session.initSync();
			}
		}).start();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRendererObj, session.getStatus(), this.width / 2, 20, 16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
		}
	}
}
