package com.github.maxopoly.WPClient.journeyMap;

import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.event.ClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;

@journeymap.client.api.ClientPlugin
public class JourneyMapPlugin implements IClientPlugin {

	private IClientAPI jmAPI;

	@Override
	public void initialize(IClientAPI jmClientApi) {
		this.jmAPI = jmClientApi;
		PlayerLocationWaypointHandler handler = new PlayerLocationWaypointHandler(jmAPI, FMLLog.getLogger(),
				Minecraft.getMinecraft());
		MinecraftForge.EVENT_BUS.register(handler);
	}

	@Override
	public String getModId() {
		return "wpclient";
	}

	@Override
	public void onEvent(ClientEvent event) {
		// TODO Auto-generated method stub

	}

}
