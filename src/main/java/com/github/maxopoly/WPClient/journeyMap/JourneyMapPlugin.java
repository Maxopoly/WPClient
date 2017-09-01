package com.github.maxopoly.WPClient.journeyMap;

import com.github.maxopoly.WPCommon.model.Location;
import com.google.common.cache.Cache;
import com.google.common.collect.HashBasedTable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.Waypoint;
import journeymap.client.api.event.ClientEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;

@journeymap.client.api.ClientPlugin
public class JourneyMapPlugin implements IClientPlugin {

	private static IClientAPI jmAPI;

	@Override
	public void initialize(IClientAPI jmClientApi) {
		jmAPI = jmClientApi;
		PlayerLocationWaypointHandler handler = new PlayerLocationWaypointHandler(jmAPI, FMLLog.getLogger(),
				Minecraft.getMinecraft());
		new ItemLocationWayPointHandler(jmAPI, FMLLog.getLogger(), Minecraft.getMinecraft());
		new WPWayPointHandler(jmClientApi, FMLLog.getLogger(), Minecraft.getMinecraft());
		MinecraftForge.EVENT_BUS.register(handler);
	}

	@Override
	public String getModId() {
		return "wpclient";
	}

	public static void dirtyWayPointRemoval(Waypoint modWaypoint) {
		try {
			Method getPluginMethod = jmAPI.getClass().getDeclaredMethod("getPlugin", String.class);
			getPluginMethod.setAccessible(true);
			Object pluginWrapper = getPluginMethod.invoke(jmAPI, modWaypoint.getModId());
			Field wayPointTable = pluginWrapper.getClass().getDeclaredField("waypoints");
			wayPointTable.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashBasedTable<Object, Object, Object> wayPoints = (HashBasedTable<Object, Object, Object>) wayPointTable
					.get(pluginWrapper);
			Object wayPoint = wayPoints.remove(modWaypoint.getId(), modWaypoint);
			if (wayPoint == null) {
				Class<?> wayPointClass = Class.forName("journeymap.client.model.Waypoint");
				Constructor<?> wayPointConstructor = wayPointClass.getConstructor(Waypoint.class);
				wayPoint = wayPointConstructor.newInstance(modWaypoint);
			}
			Method getIDMethod = wayPoint.getClass().getDeclaredMethod("getId");
			getIDMethod.setAccessible(true);
			String wayPointId = (String) getIDMethod.invoke(wayPoint);
			Class<?> wayPointStoreClass = Class.forName("journeymap.client.waypoint.WaypointStore");
			Field instanceField = wayPointStoreClass.getDeclaredField("INSTANCE");
			instanceField.setAccessible(true);
			Object wayPointStore = instanceField.get(null);
			Field cacheField = wayPointStore.getClass().getDeclaredField("cache");
			cacheField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Cache<String, ?> cache = (Cache<String, ?>) cacheField.get(wayPointStore);
			cache.invalidate(wayPointId);
			Field drawStepsUpdateNeededField = jmAPI.getClass().getDeclaredField("drawStepsUpdateNeeded");
			drawStepsUpdateNeededField.setAccessible(true);
			drawStepsUpdateNeededField.set(jmAPI, true);
		} catch (Exception e) {
			FMLLog.getLogger().error("Couldnt delete JM waypoint", e);
		}
	}

	@Override
	public void onEvent(ClientEvent event) {
		// this has to be here
	}

	public static Location convertPosition(BlockPos pos) {
		return new Location(pos.getX(), pos.getY(), pos.getZ());
	}

	public static BlockPos convertPosition(Location loc) {
		return new BlockPos(loc.getX(), loc.getY(), loc.getZ());
	}

}
