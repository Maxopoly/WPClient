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

	public Player getPlayerInfoFor(String acc) {
		Player player = players.get(acc);
		if (player == null && !requestedPlayers.contains(acc.toLowerCase())) {
			WPClientForgeMod.getInstance().getServerConnection().sendMessage(new RequestPlayerInfoPacket(acc).getMessage());
			requestedPlayers.add(acc.toLowerCase());
		}
		return player;
	}

	public void registerPlayer(Player player) {
		for (MCAccount alt : player.getAccounts()) {
			players.put(alt.getName(), player);
		}
	}

}
