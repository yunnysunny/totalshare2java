package com.whyun.totalshare.bean;

public class OAuthInfo {
	private String accessToken;
    private String uid;
    private String snsType;

    private String nickname;

    private String avatar;

    private String gender;

	public OAuthInfo() {
		
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSnsType() {
		return snsType;
	}

	public void setSnsType(String snsType) {
		this.snsType = snsType;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
    
    
}
