package com.github.maxopoly.WPClient.gui;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.model.PlayerAuth;
import com.github.maxopoly.WPClient.session.SessionManager;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;

public class GuiListAccountSelection extends GuiListExtended {

	private final GuiAccountSelection superGui;
	private final List<GuiListAccountSelectionEntry> entries;
	private int selectedElement;

	public GuiListAccountSelection(GuiAccountSelection superGui, Minecraft mcIn, int widthIn, int heightIn, int topIn,
			int bottomIn, int slotHeightIn) {
		super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
		entries = new LinkedList<GuiListAccountSelectionEntry>();
		this.superGui = superGui;
		selectedElement = -1;
		this.loadList();
		// check async whether tokens are valid so we dont freeze the gui
		new Thread(new Runnable() {

			@Override
			public void run() {
				SessionManager session = WPClientForgeMod.getInstance().getSessionManager();
				for (PlayerAuth auth : WPClientForgeMod.getInstance().getSessionManager().getAvailableAuth()) {
					GuiListAccountSelectionEntry entry = getEntryByName(auth.getName());
					if (entry == null) {
						continue;
					}
					if (session.hasValidToken(auth)) {
						entry.updateStatus("Authenticated account", 0x33cc33);
					} else {
						entry.updateStatus("Token timed out. Refreshing...", 0xffff00);
						boolean success = session.refreshToken(auth);
						if (success) {
							entry.updateStatus("Authenticated account", 0x33cc33);
						} else {
							entry.updateStatus("Failed to auth account", 0xff0000);
						}
					}
				}

			}
		}).start();
		;
	}

	private GuiListAccountSelectionEntry getEntryByName(String name) {
		for (GuiListAccountSelectionEntry entry : entries) {
			if (entry.getAccountName().equals(name)) {
				return entry;
			}
		}
		return null;
	}

	public void loadList() {
		SessionManager sessionManager = WPClientForgeMod.getInstance().getSessionManager();
		entries.clear();
		Collection<PlayerAuth> auths = sessionManager.getAvailableAuth();
		for (PlayerAuth auth : auths) {
			entries.add(new GuiListAccountSelectionEntry(this, auth, "Token provided, validating it...", 0xffff00));
		}
	}

	/**
	 * Gets the IGuiListEntry object for the given index
	 */
	@Override
	public GuiListAccountSelectionEntry getListEntry(int index) {
		return this.entries.get(index);
	}

	@Override
	protected int getSize() {
		return this.entries.size();
	}

	@Override
	protected int getScrollBarX() {
		return super.getScrollBarX() + 20;
	}

	/**
	 * Gets the width of the list
	 */
	@Override
	public int getListWidth() {
		return super.getListWidth() + 50;
	}

	public void selectAccount(int idx) {
		this.selectedElement = idx;
	}

	public GuiAccountSelection getAccountSelectionGUI() {
		return superGui;
	}

	/**
	 * Returns true if the element passed in is currently selected
	 */
	@Override
	protected boolean isSelected(int slotIndex) {
		return slotIndex == this.selectedElement;
	}

	@Nullable
	public GuiListAccountSelectionEntry getSelectedAccount() {
		return this.selectedElement >= 0 && this.selectedElement < this.getSize() ? this.getListEntry(this.selectedElement)
				: null;
	}

}
