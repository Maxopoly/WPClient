package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.gui.GuiIngameHUD;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class IngameGUIListener {

	// @SubscribeEvent
	public void onRenderTick(TickEvent.RenderTickEvent event) {

		Minecraft mc = Minecraft.getMinecraft();
		if (mc.currentScreen == null) {
			FMLLog.getLogger().info("Ingame");
			if (!(mc.ingameGUI instanceof GuiIngameHUD)) {
				FMLLog.getLogger().info("Replacing GUI");
				mc.ingameGUI = new GuiIngameHUD(mc);
			}
		}
	}
}
