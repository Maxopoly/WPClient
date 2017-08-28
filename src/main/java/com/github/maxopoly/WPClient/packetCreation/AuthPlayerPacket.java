package com.github.maxopoly.WPClient.packetCreation;

import com.github.maxopoly.WPCommon.packetHandling.PacketIndex;
import com.github.maxopoly.WPCommon.packetHandling.outgoing.AbstractJsonPacket;
import org.json.JSONObject;

public class AuthPlayerPacket extends AbstractJsonPacket {

	private String name;
	private String uuid;
	private String tag;

	public AuthPlayerPacket(String name, String uuid, String tag) {
		this.name = name;
		this.uuid = uuid;
		this.tag = tag;
	}

	@Override
	public PacketIndex getPacket() {
		return PacketIndex.InitAuth;
	}

	@Override
	public void setupJSON(JSONObject json) {
		json.put("name", name);
		json.put("uuid", uuid);
		json.put("tag", tag);

	}

}
