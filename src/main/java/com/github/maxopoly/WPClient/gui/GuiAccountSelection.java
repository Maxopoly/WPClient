package com.github.maxopoly.WPClient.gui;

import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

public class GuiAccountSelection extends GuiScreen {

	/** The screen to return to when this closes (always Main Menu). */
	protected GuiScreen prevScreen;
	protected String currentAccount;

	private GuiListAccountSelection selectionList;

	private GuiButton backButton;

	@Override
	public void initGui() {
		updateCurrentAccount();
		this.selectionList = new GuiListAccountSelection(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
		this.backButton = new GuiButton(0, this.width / 2 - 154, this.height - 52, 150, 20, "Back");
		addButton(backButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.selectionList.drawScreen(mouseX, mouseY, partialTicks);
		this.drawCenteredString(this.fontRendererObj, "Current account: " + this.currentAccount, this.width / 2, 20,
				16777215);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiMainMenu());
		}
	}

	public void updateCurrentAccount() {
		this.currentAccount = mc.getSession().getUsername();
	}

	/**
	 * Handles mouse input.
	 */
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		this.selectionList.handleMouseInput();
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.selectionList.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Called when a mouse button is released.
	 */
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		this.selectionList.mouseReleased(mouseX, mouseY, state);
	}

}
