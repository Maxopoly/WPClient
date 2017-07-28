package com.github.maxopoly.WPClient;

import com.github.maxopoly.WPClient.connection.ServerConnection;
import com.github.maxopoly.WPClient.packetCreation.ItemLocationRequestPacket;
import com.github.maxopoly.WPClient.model.WPItem;
import mezz.jei.input.WPClientGUIClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class JEI_GUI_Listener {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onClick(WPClientGUIClickEvent e) {
		if (!WPClientForgeMod.getInstance().isConnectionReady()) {
			return;
		}
		WPItem item = new WPItem(e.getItemID());
		ItemLocationRequestPacket packet = new ItemLocationRequestPacket(item.getID());
		ServerConnection conn = WPClientForgeMod.getInstance().getServerConnection();
		if (conn.isInitialized()) {
			conn.sendMessage(packet.getMessage());
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString(String.format(
					"%s[WPC]  %sError: %sNot connected to the server, could not request item location.",
					TextFormatting.BLUE, TextFormatting.RED, TextFormatting.GRAY)));
		}
	}
}
