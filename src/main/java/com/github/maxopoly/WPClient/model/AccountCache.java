package com.github.maxopoly.WPClient.model;

import com.github.maxopoly.WPClient.WPClientForgeMod;
import com.github.maxopoly.WPClient.packetCreation.RequestPlayerInfoPacket;
import com.github.maxopoly.WPCommon.model.Faction;
import com.github.maxopoly.WPCommon.model.MCAccount;
import com.github.maxopoly.WPCommon.model.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.StringUtils;

public class AccountCache {

	private static AccountCache instance;

	public static AccountCache getInstance() {
		if (instance == null) {
			instance = new AccountCache();
		}
		return instance;
	}

	private Map<String, Player> players;
	private Map<String, Faction> factions;
	private Set<String> requestedPlayers;

	private AccountCache() {
		this.players = new HashMap<String, Player>();
		this.factions = new HashMap<String, Faction>();
		this.requestedPlayers = new HashSet<String>();
	}

	public synchronized Player getPlayerInfoFor(String acc) {
		Player player = players.get(acc);
		if (player == null && WPClientForgeMod.getInstance().connectedToWPServer()
				&& !requestedPlayers.contains(acc.toLowerCase())) {
			WPClientForgeMod.getInstance().getServerConnection().sendMessage(new RequestPlayerInfoPacket(acc));
			requestedPlayers.add(acc.toLowerCase());
		}
		return player;
	}

	public synchronized void registerPlayer(Player player) {
		for (MCAccount alt : player.getAccounts()) {
			players.put(alt.getName(), player);
		}
	}

	public synchronized void invalidatePlayerInfo(String name) {
		players.remove(name);
		requestedPlayers.remove(name);
		getPlayerInfoFor(name);
	}

	public synchronized void invalidateAllPlayerInfo() {
		players.clear();
		factions.clear();
		requestedPlayers.clear();
		// rebuild cache with all online players
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.theWorld != null) {
			for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
				getPlayerInfoFor(StringUtils.stripControlCodes(info.getGameProfile().getName()));
			}
		}
	}

}
