package com.whyun.totalshare.bean;

public class MessagePublishResult {
	private int rv;
    private String errmsg;
    private String messageId;
	public MessagePublishResult() {
		
	}
	public int getRv() {
		return rv;
	}
	public void setRv(int rv) {
		this.rv = rv;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String message) {
		this.errmsg = message;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
    
    
}
