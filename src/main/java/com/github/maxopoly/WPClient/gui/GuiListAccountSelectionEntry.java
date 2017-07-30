package com.github.maxopoly.WPClient.gui;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.model.PlayerAuth;
import com.github.maxopoly.WPClient.session.SessionManager;
import com.github.maxopoly.WPClient.util.AuthTokenManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiListAccountSelectionEntry implements GuiListExtended.IGuiListEntry {

	private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
	private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
	private final Minecraft client;
	private final GuiAccountSelection accountSelectionScreen;
	private final PlayerAuth auth;
	private final GuiListAccountSelection authListGui;
	private long lastClickTime;
	private String status;
	private int statusColor;

	public GuiListAccountSelectionEntry(GuiListAccountSelection authListGui, PlayerAuth auth, String status,
			int statusColor) {
		this.authListGui = authListGui;
		this.accountSelectionScreen = authListGui.getAccountSelectionGUI();
		this.client = Minecraft.getMinecraft();
		this.auth = auth;
		this.status = status;
		this.statusColor = statusColor;
	}

	public void updateStatus(String status, int color) {
		this.status = status;
		this.statusColor = color;
	}

	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY,
			boolean isSelected) {
		this.client.fontRendererObj.drawString(auth.getName(), x + 32 + 3, y + 1, 16777215);
		this.client.fontRendererObj.drawString(auth.getEmail(), x + 32 + 3,
				y + this.client.fontRendererObj.FONT_HEIGHT + 3, 8421504);
		this.client.fontRendererObj.drawString(status, x + 32 + 3, y + 2 * this.client.fontRendererObj.FONT_HEIGHT + 5,
				statusColor);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
		GlStateManager.disableBlend();

		if (this.client.gameSettings.touchscreen || isSelected) {
			this.client.getTextureManager().bindTexture(ICON_OVERLAY_LOCATION);
			Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			int j = mouseX - x;
			int i = j < 32 ? 32 : 0;
			Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, i, 32, 32, 256.0F, 256.0F);
		}
	}

	/**
	 * Called when the mouse is clicked within this entry. Returning true means that something within this entry was
	 * clicked and the list should not be dragged.
	 */
	@Override
	public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
		this.authListGui.selectAccount(slotIndex);
		if (relativeX <= 32 && relativeX < 32) {
			changeAccount();
			return true;
		} else if (Minecraft.getSystemTime() - this.lastClickTime < 250L) {
			changeAccount();
			return true;
		} else {
			this.lastClickTime = Minecraft.getSystemTime();
			return false;
		}
	}

	public void changeAccount() {
		if (Minecraft.getMinecraft().getSession().getUsername().equalsIgnoreCase(auth.getName())) {
			return;
		}
		if (auth.isBroken()) {
			return;
		}
		SessionManager session = WPClientForgeMod.getInstance().getSessionManager();
		if (!session.hasValidToken(auth)) {
			if (!session.refreshToken(auth)) {
				status = "Failed to auth account";
				statusColor = 0xff0000;
				return;
			}
		}
		AuthTokenManager.overwriteAuthSession(auth);
		accountSelectionScreen.updateCurrentAccount();
	}

	public String getAccountName() {
		return auth.getName();
	}

	/**
	 * Fired when the mouse button is released. Arguments: index, x, y, mouseEvent, relativeX, relativeY
	 */
	@Override
	public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
	}

	@Override
	public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {
	}

}
