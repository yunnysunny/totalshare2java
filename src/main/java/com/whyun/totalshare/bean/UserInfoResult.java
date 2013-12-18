package com.whyun.totalshare.bean;

import com.alibaba.fastjson.JSON;

public class UserInfoResult {
	private int rv;
	private String errmsg;
    private String nickname;
    private String gender;
    private String figureurl;
	public UserInfoResult() {
		
	}
	public int getRv() {
		return rv;
	}
	public void setRv(int rv) {
		this.rv = rv;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getFigureurl() {
		return figureurl;
	}
	public void setFigureurl(String figureurl) {
		this.figureurl = figureurl;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
    
}
