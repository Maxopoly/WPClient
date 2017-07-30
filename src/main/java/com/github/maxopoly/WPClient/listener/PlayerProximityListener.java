package com.github.maxopoly.WPClient.listener;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPCommon.model.Location;
import com.github.maxopoly.WPCommon.model.LocationTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class PlayerProximityListener {

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onTick(ClientTickEvent event) {
		// update locations of all players in render distance
		if (!WPClientForgeMod.getInstance().isConnectionReady()) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.theWorld == null) {
			return;
		}
		LocationTracker tracker = LocationTracker.getInstance();
		for (Entity entity : mc.theWorld.playerEntities) {
			if (!(entity instanceof EntityOtherPlayerMP)) {
				continue;
			}
			Vec3d pos = entity.getPositionVector();
			tracker.reportRadarLocation(entity.getName(), new Location(pos.xCoord, pos.yCoord, pos.zCoord));
		}
		Vec3d pos = mc.thePlayer.getPositionVector();
		tracker.reportRadarLocation(mc.thePlayer.getName(), new Location(pos.xCoord, pos.yCoord, pos.zCoord));
	}

}
