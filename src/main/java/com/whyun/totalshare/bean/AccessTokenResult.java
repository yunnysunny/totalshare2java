package com.whyun.totalshare.bean;

public class AccessTokenResult {
	public static final int SUCCESS = 0;
    private int rv;
    private String accessToken;
    private long loginTime;
    private long expireTime;
    private String errmsg;  
    
    
	public AccessTokenResult() {
		
	}
	public int getRv() {
		return rv;
	}
	public void setRv(int rv) {
		this.rv = rv;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public long getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}
	public long getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
    
}
