package com.github.maxopoly.WPClient.listener;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyBindListener {

	private KeyBinding wayPointToggle;
	private static boolean wayPointsShown;

	public KeyBindListener() {
		wayPointToggle = new KeyBinding("Toggle waypoints", Keyboard.KEY_H, "WPClient");
		ClientRegistry.registerKeyBinding(wayPointToggle);
	}

	@SubscribeEvent
	public void onKeyPress(InputEvent.KeyInputEvent event) {
		if (wayPointToggle.isPressed()) {

		}
	}

	public void showWayPoints() {

	}

	public void hideWayPoints() {

	}

}
