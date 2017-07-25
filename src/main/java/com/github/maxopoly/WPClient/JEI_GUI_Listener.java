package com.github.maxopoly.WPClient;

import com.github.maxopoly.WPClient.connection.ServerConnection;
import com.github.maxopoly.WPClient.packetCreation.ItemLocationRequestPacket;
import mezz.jei.input.WPClientGUIClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class JEI_GUI_Listener {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onClick(WPClientGUIClickEvent e) {
		if (!WPClientForgeMod.getInstance().isConnectionReady()) {
			return;
		}
		ItemLocationRequestPacket packet = new ItemLocationRequestPacket(e.getItemID());
		ServerConnection conn = WPClientForgeMod.getInstance().getServerConnection();
		if (conn.isInitialized()) {
			conn.sendMessage(packet.getMessage());
			Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString(
					"[WPC] Requesting item location from server"));
		} else {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new TextComponentString(
					"[WPC] Not connected to the server, could not request location"));
		}
	}
}
