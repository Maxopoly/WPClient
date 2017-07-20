package com.github.maxopoly.WPClient.model;

import com.github.maxopoly.WPCommon.model.Faction;
import com.github.maxopoly.WPCommon.model.Player;
import java.util.HashMap;
import java.util.Map;

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

	private AccountCache() {
		this.players = new HashMap<String, Player>();
		this.factions = new HashMap<String, Faction>();
	}

	public Player getPlayerInfoFor(String acc) {
		return players.get(acc);
	}

	public void registerPlayer(Player player) {
		for (String alt : player.getAccounts()) {
			players.put(alt, player);
		}
	}

}
