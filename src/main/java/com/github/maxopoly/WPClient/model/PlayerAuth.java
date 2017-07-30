package com.github.maxopoly.WPClient.model;

public class PlayerAuth {

	private String name;
	private String authToken;
	private String email;
	private String uuid;
	private String userID;
	private long lastVerified;
	private boolean valid;
	private boolean broken;

	public PlayerAuth(String name, String authToken, String email, String uuid, String userID) {
		this.name = name;
		this.authToken = authToken;
		this.email = email;
		this.uuid = uuid;
		this.userID = userID;
		this.lastVerified = -1;
		this.valid = true;
		this.broken = false;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void updateToken(String token) {
		this.authToken = token;
	}

	public String getUUID() {
		return uuid;
	}

	public long getLastVerified() {
		return lastVerified;
	}

	public boolean isBroken() {
		return broken;
	}

	public void breakAuth() {
		broken = true;
	}

	public void updateVerifyTimeStamp() {
		this.lastVerified = System.currentTimeMillis();
	}

	public String getEmail() {
		return email;
	}

	public String getName() {
		return name;
	}

	public String getAuthToken() {
		return authToken;
	}

	public String getUserId() {
		return userID;
	}

	@Override
	public String toString() {
		return String.format("name: %s, email: %s, token: %s, uuid: %s, userId: %s", name, email, authToken, uuid, userID);
	}
}
