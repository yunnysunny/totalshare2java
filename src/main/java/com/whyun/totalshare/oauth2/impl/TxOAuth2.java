
package com.whyun.totalshare.oauth2.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.whyun.totalshare.bean.AccessTokenResult;
import com.whyun.totalshare.bean.MessagePublishResult;
import com.whyun.totalshare.bean.SNSConfigBean;
import com.whyun.totalshare.bean.UidResult;
import com.whyun.totalshare.bean.UserInfoResult;
import com.whyun.totalshare.oauth2.AbstractOAuth2;
import com.whyun.util.http.HTTPClientUtil;

public class TxOAuth2 extends AbstractOAuth2
{
	private static Logger logger = Logger.getLogger(TxOAuth2.class);

    public  TxOAuth2(SNSConfigBean config, String redirectUri)
    {
        super(config, redirectUri);
        this.snsType = SNS_TYPE_TX;
    }

    public String getAuthorizeUrl(String state)
    {
        return getAuthorizeUrlStr(TX_AUTH_URL_PC, state, this.scope);
    }

    /**
     * 出错的时候返回callback({"error":100005,"error_description":"param code is wrong or lost "});
     * 成功的时候返回access_token=xx&expires_in=7776000&refresh_token=xxxx
     */
    public AccessTokenResult getAccessToken(String code)
    {
        String respStr = getAccessTokenStr(TX_ACCESS_TOKEN_URL_PC, code);

        AccessTokenResult result = new AccessTokenResult();
        if (respStr == null || "".equals(respStr)) {
        	result.setRv(ERROR_NETWORK_CALL);
        	return result;
        }
        
		
        if (respStr.startsWith("callback")) {
            int lpos = respStr.indexOf("(");
            int rpos = respStr.indexOf(")");
            respStr = respStr.substring(lpos + 1, rpos - 1);
            Object obj = JSON.parse(respStr);
    		JSONObject msg = (JSONObject)obj;
    		
    		int errcode = msg.getIntValue("error");
            if (errcode != 0) {
                result.setRv(errcode);
                result.setErrmsg(msg.getString("error_description"));
            } else {
                result.setRv(ERROR_FAIL_DEFAULT);
            }
        } else {
            Map<String,String>msg = HTTPClientUtil.parseUriParam(respStr);
            if (msg ==null || msg.get("access_token") == null) {
            	result.setRv(ERROR_PARSE_CALLBACK);
            } else {
            	result.setRv(ERROR_SUCCESS);
            	String accessToken = msg.get("access_token");
                result.setAccessToken(accessToken);
                long now = new Date().getTime();
                result.setLoginTime(now);
                String expiresIn = msg.get("expires_in");
                try {
                	long expires = Long.parseLong(expiresIn);
                	result.setExpireTime(now + expires - 600);
                } catch (Exception e) {
                	
                }
                
                this.accessToken = accessToken;
            }
            
            
        }
        return result;
    }

    /**
     * 成功返回callback( {"client_id":"YOUR_APPID","openid":"YOUR_OPENID"} );
     * 失败返回callback( {"error":100016,"error_description":"access token check failed"} );
     */
    public UidResult  getUid(String accessToken)
    {
        String respStr = getUidStr(accessToken, TX_GET_OPEN_ID);

        UidResult result = new UidResult();
        if (respStr == null || "".equals(respStr)) {
        	result.setRv(ERROR_NETWORK_CALL);
        	return result;
        }
        
        if (respStr.startsWith("callback")) {
        	int lpos = respStr.indexOf("(");
            int rpos = respStr.indexOf(")");
            String response = respStr.substring(lpos + 1, rpos - 1);

            Object obj = JSON.parse(response);
    		JSONObject msg = (JSONObject)obj;
    		
    		int errcode = msg.getIntValue("error");
            if (errcode != 0) {
                result.setRv(errcode);
                result.setErrmsg(msg.getString("error_description"));
            } else if (msg.getString("openid") != null) {
                result.setRv(ERROR_SUCCESS);
                String uid = msg.getString("openid");
                result.setUid(uid);
                this.uid = uid;
            } else {
                result.setRv(ERROR_FAIL_DEFAULT);
            }
        }
        return result;
    }

    /**
     * 失败后返回{"ret":-1,"msg":"xxx"}
     * 成功后返回
     * {
    "ret": 0,
    "msg": "",
    "nickname": "xxx",
    "gender": "x",
    "figureurl": "http:././qzapp.qlogo.cn./qzapp./100474085./94EFEEB8BF3431F910FD22B3C8D5F8C8./30",
    "figureurl_1": "http:././qzapp.qlogo.cn./qzapp./100474085./94EFEEB8BF3431F910FD22B3C8D5F8C8./50",
    "figureurl_2": "http:././qzapp.qlogo.cn./qzapp./100474085./94EFEEB8BF3431F910FD22B3C8D5F8C8./100",
    "figureurl_qq_1": "http:././q.qlogo.cn./qqapp./100474085./94EFEEB8BF3431F910FD22B3C8D5F8C8./40",
    "figureurl_qq_2": "http:././q.qlogo.cn./qqapp./100474085./94EFEEB8BF3431F910FD22B3C8D5F8C8./100",
    "is_yellow_vip": "0",
    "vip": "0",
    "yellow_vip_level": "0",
    "level": "0",
    "is_yellow_year_vip": "0"
    }
     */
    public UserInfoResult getUserInfo()
    {
    	UserInfoResult result = new UserInfoResult();
        if (this.uid == null) {
            result.setRv(ERROR_UID_NOT_EXIST);
        } else {
        	Map<String,String> params = new HashMap<String,String>();
        	params.put("access_token", this.accessToken);
        	params.put("openid", this.uid);
        	params.put("oauth_consumer_key", this.clientId);
        	
            String respStr = super.response(HTTPClientUtil.POST_METHOD, TX_GET_NICKNAME, params);
            if (respStr == null || "".equals(respStr)) {
            	result.setRv(ERROR_NETWORK_CALL);
            	return result;
            }

            if (logger.isDebugEnabled()) {
                logger.debug("response:" + respStr);
            }
            Object obj = JSON.parse(respStr);
    		JSONObject msg = (JSONObject)obj;
    		int ret = msg.getIntValue("ret");
            
            if (ret == 0) {
                result.setRv(ERROR_SUCCESS);
                result.setNickname(msg.getString("nickname"));
                result.setGender("男".equals( msg.getString("gender")) ? "m" : "f");
                result.setFigureurl(msg.getString("figureurl_2"));
            } else {
                result.setRv(ret);
            }
            
        }
        return result;
    }

    /**
     * 腾讯发布微博
     *
     * 需要4个必须参数：content（文本内容）、access_token、oauth_consumer_key、openid，
     * 发送带图片的时候，pic（图片内容）也是必须的
     * 发送普通文本的链接参见@see TX_PUBLISH_MESSAGE_TEXT,
     * 发送图片文本的连接参见@see TX_PUBLISH_MESSAGE_WITH_PIC
     * 默认返回类型为json：
     *  {
		    ret:0,//公共返回码
		    msg:"ok",
		    errcode:0,//二级返回码
		    data:
		    {
		    	id:12345678,
		    	time:12863444444
		    }
    	}
     */
    public MessagePublishResult publishMessage(String accessToken,String message, 
    		String url, String filename,Map<String,String> params)
    {
    	MessagePublishResult result = new MessagePublishResult();
        Map<String, String>data = new HashMap<String,String>();
        data.put("content", message + ' ' + url);
        data.put("access_token" , accessToken);
        data.put("oauth_consumer_key", this.clientId);
        if (this.uid != null) {
            data.put("openid", this.uid);
        } else {
            data.put("openid",params.get("uid"));
        }
        
        String resp = null;
        if (filename != null) {
        	Map<String,String> file = new HashMap<String,String>();
            resp = this.response(HTTPClientUtil.POST_METHOD, TX_PUBLISH_MESSAGE_WITH_PIC,
                data, null, file);
        } else {
            resp = this.response(HTTPClientUtil.POST_METHOD, TX_PUBLISH_MESSAGE_TEXT,
                data, null, null);
        }
        if (resp != null) {
        	Object obj = JSON.parse(resp);
    		JSONObject msg = (JSONObject)obj;
    		
    		int ret = msg.getIntValue("ret");
            
            if (ret == 0) {
                result.setRv(0);
            } else {
            	int errcode = msg.getIntValue("errcode");
                result.setRv(errcode != 0 ? errcode : ret);
                result.setErrmsg(msg.getString("msg"));
            }
            
        } else {
            result.setRv(ERROR_NETWORK_CALL);
        }
        return result;
    }

}