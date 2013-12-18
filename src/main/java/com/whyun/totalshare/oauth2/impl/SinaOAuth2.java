package com.whyun.totalshare.oauth2.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.whyun.totalshare.bean.AccessTokenResult;
import com.whyun.totalshare.bean.MessagePublishResult;
import com.whyun.totalshare.bean.SNSConfigBean;
import com.whyun.totalshare.bean.UidResult;
import com.whyun.totalshare.bean.UserInfoResult;
import com.whyun.totalshare.oauth2.AbstractOAuth2;
import com.whyun.util.http.HTTPClientUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class SinaOAuth2.
 */
public class SinaOAuth2 extends AbstractOAuth2
{
    
    /**
     * Instantiates a new sina o auth2.
     *
     * @param config the config
     * @param redirectUri the redirect uri
     */
    public  SinaOAuth2(SNSConfigBean config, String redirectUri)
    {
        super(config, redirectUri);
        this.snsType = SNS_TYPE_SINA;
    }

    /* (non-Javadoc)
     * @see com.whyun.totalshare.oauth2.AbstractOAuth2#getAuthorizeUrl(java.lang.String)
     */
    public String  getAuthorizeUrl(String state)
    {
        return getAuthorizeUrlStr(SINA_AUTH_URL, state, this.scope);
    }
    
    /* (non-Javadoc)
     * @see com.whyun.totalshare.oauth2.AbstractOAuth2#getAccessToken(java.lang.String)
     * 
     * 成功时返回
     * {
     * "access_token":"2.00WvC24BZtKqoD42041956f1lAgmJB",
     * "remind_in":"157679999",
     * "expires_in":157679999,
     * "uid":"1261004702"
     * }
     * 失败时返回：
     * {
		"error":"unsupported_response_type",
		"error_code":21329
		"error_description":"不支持的 ResponseType."
		}
     */
    public AccessTokenResult getAccessToken(String code)
    {
        String respStr = getAccessTokenStr(SINA_ACCESS_TOKEN_URL, code);
        
        AccessTokenResult result = new AccessTokenResult();
        if (respStr == null || "".equals(respStr)) {
        	result.setRv(ERROR_NETWORK_CALL);
        	return result;
        }
        
        Object obj = JSON.parse(respStr);
		JSONObject msg = (JSONObject)obj;
		
		int errorCode = msg.getIntValue("error_code");
        if (errorCode > 0) {
            result.setRv(errorCode);
            result.setErrmsg(msg.getString("error_description"));
        } else {
            result.setRv(ERROR_SUCCESS);
            result.setAccessToken(msg.getString("access_token"));
            long now = new Date().getTime();
            result.setLoginTime(now);
            result.setExpireTime(now + msg.getLong("expires_in") - 600);
            this.accessToken = msg.getString("access_token");
            this.uid = msg.getString("uid");
        }
        return result;
    }

    /* (non-Javadoc)
     * @see com.whyun.totalshare.oauth2.AbstractOAuth2#getUid(java.lang.String)
     * 成功返回：
     *  {
		       "uid": 1073880650,
		       "appkey": 1352222456,
		       "scope": null,
		       "create_at": 1352267591,
		       "expire_in": 157679471
		 }
		 失败返回：
		 {
		"error":"unsupported_response_type",
		"error_code":21329
		"error_description":"不支持的 ResponseType."
		}
     */
    public UidResult getUid(String accessToken)
    {
    	UidResult result = new UidResult();
    	if (this.uid != null) {
    		result.setRv(ERROR_SUCCESS);
    		result.setUid(this.uid);
    		return result;
    	}
    	
        String respStr = getUidStr(accessToken, SINA_GET_UID);
        if (respStr == null || "".equals(respStr)) {
        	result.setRv(ERROR_NETWORK_CALL);
        	return result;
        }
        Object obj = JSON.parse(respStr);
		JSONObject msg = (JSONObject)obj;
        
		
		int errorCode = msg.getIntValue("error_code");
        if (errorCode > 0) {
            result.setRv(errorCode);
            result.setErrmsg(msg.getString("error_description"));
        } else {
            result.setRv(ERROR_SUCCESS);
            String uid = msg.getString("uid");
            result.setUid(uid);
            this.uid = uid;
        }
        return result;
    }

    /**
     * 错误返回{"error":"miss parameter accesstoken","error_code":10016,"request":"/oauth2/get_token_info"}
     * 正确返回
     * {"id":1261004702,"idstr":"1261004702","screen_name":"yunnysunny","name":"yunnysunny","province":"37","city":"1",
     * "location":"山东 济南","description":"仗剑漂泊许些年，白衣飒飒；失意离群几庚余，银髯飘飘。",
     * "url":"http://blog.csdn.net/yunnysunny","profile_image_url":"http://tp3.sinaimg.cn/1261004702/50/1301135858/1",
     * "profile_url":"u/1261004702","domain":"","weihao":"","gender":"m","followers_count":85,"friends_count":31,
     * "statuses_count":257,"favourites_count":32,"created_at":"Fri Nov 19 21:27:49 +0800 2010",
     * "following":false,"allow_all_act_msg":false,"geo_enabled":true,"verified":false,"verified_type":-1,"remark":"",
     * "status":{"created_at":"Sat Jun 29 09:15:37 +0800 2013","id":3594455921332679,"mid":"3594455921332679",
     * "idstr":"3594455921332679",
     * "text":"xxx",
     * "source":"新浪微博","favorited":false,"truncated":false,"in_reply_to_status_id":"","in_reply_to_user_id":"",
     * "in_reply_to_screen_name":"","pic_urls":[],"geo":null,"reposts_count":0,"comments_count":0,"attitudes_count":0,
     * "mlevel":0,"visible":{"type":0,"list_id":0}},"allow_all_comment":true,
     * "avatar_large":"http://tp3.sinaimg.cn/1261004702/180/1301135858/1","avatar_hd":"",
     * "verified_reason":"","follow_me":false,"online_status":0,"bi_followers_count":11,"lang":"zh-cn","star":0,"mbtype":0,"mbrank":0,"block_word":0}
     *
     * @return the user info
     */
    public UserInfoResult getUserInfo()
    {
    	UserInfoResult result = new UserInfoResult();
        Map<String,String>params = new HashMap<String,String>();
        params.put("access_token" , this.accessToken);
        params.put("uid", this.uid);
        
        String respStr = this.response(HTTPClientUtil.GET_METHOD, SINA_GET_USER_INFO, params);
        if (respStr == null || "".equals(respStr)) {
        	result.setRv(ERROR_NETWORK_CALL);
        	return result;
        }
        Object obj = JSON.parse(respStr);
		JSONObject msg = (JSONObject)obj;
		
        int errorCode = msg.getIntValue("error_code");
        if (errorCode > 0) {
            result.setRv(errorCode);
            result.setErrmsg(msg.getString("error_description"));
        } else {
            result.setNickname(msg.getString("screen_name"));
            result.setGender(msg.getString("gender"));
            result.setFigureurl(msg.getString("avatar_large"));
            result.setRv(ERROR_SUCCESS);
        }
        return result;
    }

    /**
     * 新浪发送微博
     * 
     * 必选参数access_token、status（文本内容）
     * 如果发送图片必选参数pic（图片内容）
     * 发送普通文本微博，地址为@see OAuth2::SINA_PUBLISH_MESSAGE_TEXT
     * 发送图片微博，地址为@see OAuth2::SINA_PUBLISH_MESSAGE_WITH_PIC
     * 返回类型为json：
     * 
     * {
     * "created_at": "Wed Oct 24 23:49:17 +0800 2012",
     * "id": 3504803600500000,
     * "mid": "3504803600502730",
     * "idstr": "3504803600502730",
     * "text": "分组定向图片微博",
     * "source": "新浪微博</a>",
     * "favorited": false,
     * "truncated": false,
     * "in_reply_to_status_id": "",
     * "in_reply_to_user_id": "",
     * "in_reply_to_screen_name": "",
     * "thumbnail_pic": "http://ww2.sinaimg.cn/thumbnail/71666d49jw1dy6q8t3p0rj.jpg",
     * "bmiddle_pic": "http://ww2.sinaimg.cn/bmiddle/71666d49jw1dy6q8t3p0rj.jpg",
     * "original_pic": "http://ww2.sinaimg.cn/large/71666d49jw1dy6q8t3p0rj.jpg",
     * 
     * ....//省略剩余信息
     * }
     * 失败时返回：
     * {
		    "error": "invalid_access_token", 
		    "error_code": 21332, 
		    "request": "/2/statuses/update.json"
	   }
     * @return MessagePublishResult
     */
    public MessagePublishResult publishMessage(String accessToken, String message,
    		String url,String filename,Map<String,String> params)
    {
    	MessagePublishResult result = new MessagePublishResult();
        Map<String,String>data = new HashMap<String,String>();
        data.put("status" , message + ' ' + url);
        data.put("access_token" , accessToken);
        String resp = null;
        if (filename != null) {
        	Map<String,String> file = new HashMap<String,String>();
        	file.put("pic", filename);
            resp = this.response(HTTPClientUtil.POST_METHOD, SINA_PUBLISH_MESSAGE_WITH_PIC,
                data, null, file);
        } else {
            resp = this.response(HTTPClientUtil.POST_METHOD, SINA_PUBLISH_MESSAGE_TEXT, data, null, null);
        }
        if (resp != null) {
        	Object obj = JSON.parse(resp);
    		JSONObject msg = (JSONObject)obj;
    		int errorCode = msg.getIntValue("error_code");
            if (errorCode >0) {
                result.setRv(errorCode);
                result.setErrmsg(msg.getString("error"));
            } else {
                result.setRv(ERROR_SUCCESS);
            }
        } else {
            result.setRv(ERROR_NETWORK_CALL);
        }
        return result;
    }

}