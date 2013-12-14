package com.whyun.totalshare.oauth2.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.whyun.totalshare.bean.AccessTokenResult;
import com.whyun.totalshare.bean.MessagePublishResult;
import com.whyun.totalshare.bean.SNSConfigBean;
import com.whyun.totalshare.bean.UidResult;
import com.whyun.totalshare.bean.UserInfoResult;
import com.whyun.totalshare.oauth2.AbstractOAuth2;
import com.whyun.util.http.HTTPClientUtil;

class RenRenOAuth2 extends AbstractOAuth2
{
	private static Logger logger = Logger.getLogger(RenRenOAuth2.class);
//	@Override
//	public MessagePublishResult publishMessage(String accessToken,
//			String message, String url, String filename,
//			Map<String, String> params) {
//		// TODO Auto-generated method stub
//		return null;
//	}

    public  RenRenOAuth2(SNSConfigBean config, String redirectUri)
    {
        super(config, redirectUri);
        this.snsType = SNS_TYPE_RENREN;
    }

    public String getAuthorizeUrl(String state)
    {
        return super.getAuthorizeUrlStr(RENREN_AUTH_URL, state,this.scope);
    }

    /***
     * 失败时返回：
     * {
        "error": "失败类型",
        "error_description": "失败描述"
        }
     * 成功时返回：
     * {
    "access_token" : "239085|6.83bbf02bf94cd17fe5dcdc978e9b8239.2592000.1387807200-242164211",
    "expires_in" : 2592554,
    "refresh_token" : "239085|0.ZJp9TkFUWryTfYLymfaD7YMcLqOzWXRP.242164211.1375087787476",
    "scope" : "当前登录用户权限，以空格隔开各个权限字符串",
    "token_type" : "bearer",
    "user" : {
        "avatar" : [
        {
        "type" : "avatar",
        "url" : "http://hdn.xnimg.cn/photos/hdn121/20120610/1355/h_head_g7sd_66c5000002c01375.jpg"
        },
        {
        "type" : "tiny",
        "url" : "http://hdn.xnimg.cn/photos/hdn321/20120610/1355/tiny_EIyS_38a0000115821376.jpg"
        },
        {
        "type" : "main",
        "url" : "http://hdn.xnimg.cn/photos/hdn121/20120610/1355/h_main_QzSH_66c5000002c01375.jpg"
        },
        {
        "type" : "large",
        "url" : "http://hdn.xnimg.cn/photos/hdn121/20120610/1355/h_large_NIyy_66c5000002c01375.jpg"
        }
        ],
        "id" : 242164211,
        "name" : "高阳"
        }
    }
     * @param code
     * @return AccessTokenResult
     */
    public AccessTokenResult getAccessToken(String code)
    {
        String respStr = getAccessTokenStr(RENREN_ACCESS_TOKEN_URL, code);
        if (logger.isDebugEnabled()) {
            logger.debug("accesstokenstr:" + respStr);
        }
        AccessTokenResult result = new AccessTokenResult();
        if (respStr == null || "".equals(respStr)) {
        	result.setRv(ERROR_NETWORK_CALL);
        	return result;
        }
        
        Object obj = JSON.parse(respStr);
		JSONObject msg = (JSONObject)obj;
        
		
		String error = msg.getString("error");
        if (error != null) {
            result.setRv(1);
            result.setErrmsg(msg.getString("error_description"));
        } else {
            result.setRv(ERROR_SUCCESS);
            result.setAccessToken(msg.getString("access_token"));
            long now = new Date().getTime();
            result.setLoginTime(now);
            result.setExpireTime(now + msg.getLong("expires_in") - 600);
            this.accessToken = msg.getString("access_token");

            JSONObject user = msg.getJSONObject("user");//.user;
            if (user != null) {
            	this.nickname = user.getString("name");
                this.uid = user.getString("id");
                JSONArray avatars = user.getJSONArray("avatar");
                if (avatars != null && avatars.size() > 1) {
                	JSONObject avatarsObj = (JSONObject)avatars.get(1);
                	if (avatarsObj != null) {
                		this.avatar = avatarsObj.getString("url");
                	}                	
                }
                
            }
            
        }
        return result;
    }

    /**
     * 根据accesstoken获取用户信息
     * 失败时返回：
     * {
     *      "error":
     *      {
     *              "message":"验证参数错误。",
     *              "code":"invalid_authorization.INVALID-AUTHORIZATION-PARAMETERS"
     *      }
     * }
     * 成功时返回：
     * {
    "response" : {
        "avatar" : [
            {
            "size" : "TINY",
            "url" : "http://hdn.xnimg.cn/photos/hdn321/20120610/1355/tiny_EIyS_38a0000115821376.jpg"
            },
            {
            "size" : "HEAD",
            "url" : "http://hdn.xnimg.cn/photos/hdn121/20120610/1355/h_head_g7sd_66c5000002c01375.jpg"
            },
            {
            "size" : "MAIN",
            "url" : "http://hdn.xnimg.cn/photos/hdn121/20120610/1355/h_main_QzSH_66c5000002c01375.jpg"
            },
            {
            "size" : "LARGE",
            "url" : "http://hdn.xnimg.cn/photos/hdn121/20120610/1355/h_large_NIyy_66c5000002c01375.jpg"
            }
        ],
        "basicInformation" : null,
        "education" : null,
        "emotionalState" : null,
        "id" : 242164211,
        "like" : null,
        "name" : "高阳",
        "star" : 1,
        "work" : null
        }
    }
     * @param accessToken
     * @return UidResult
     */
    public UidResult getUid(String accessToken)
    {
    	UidResult result = new UidResult();

        String respStr = getUidStr(accessToken, RENREN_GET_USER_INFO, HTTPClientUtil.GET_METHOD);
        if (logger.isDebugEnabled()) {
            logger.debug("uidstr:" + respStr);
        }
        if (respStr == null || "".equals(respStr)) {
        	result.setRv(ERROR_NETWORK_CALL);
        	return result;
        }
        
        Object obj = JSON.parse(respStr);
		JSONObject msg = (JSONObject)obj;

		JSONObject error = (JSONObject)msg.getJSONObject("error");
        if (error != null) {
            result.setRv(ERROR_GET_UID);
            result.setErrmsg(error.getString("message"));
        } else {

            result.setRv(ERROR_SUCCESS);
            JSONObject response = (JSONObject)msg.getJSONObject("response");
            
            String id = response.getString("id");
            result.setUid(id);
            this.uid = id;
            
            JSONArray avatars = response.getJSONArray("avatar");
            if (avatars != null && avatars.size() > 1) {
            	JSONObject avatar = (JSONObject)avatars.get(1);
            	if (avatar != null) {
            		this.avatar = avatar.getString("url");
            	}
            	
            }
            
            this.nickname = response.getString("name");
            JSONObject basicInformation = response.getJSONObject("basicInformation");
            if (basicInformation != null) {
            	String sex  = basicInformation.getString("sex");
            	if (sex != null && !"".equals(sex)) {
            		this.gender = sex == "MALE" ? "m" : "f";
            	} else {
            		this.gender = "un";
            	}                
            } else {
                this.gender = "un";
            }

        }
        return result;
    }

    /**
     * 获取用户信息
     */
    public UserInfoResult getUserInfo()
    {
    	UserInfoResult result = new UserInfoResult();

        if (this.uid != null) {
            result.setNickname(this.nickname);
            result.setGender(this.gender);
            result.setFigureurl(this.avatar);
            result.setRv(ERROR_SUCCESS);
        } else {
            result.setRv(ERROR_UID_NOT_EXIST);
        }

        return result;
    }

    /**
     * @param accessToken
     * @param message
     * @param url
     * @param  filename
     * @param  params
     * @return MessagePublishResult
     */
    public MessagePublishResult  publishMessage(String accessToken, String message,
    		String url, String filename,Map<String,String> params)
    {
    	MessagePublishResult result = new MessagePublishResult();

        Map<String,String> data = new HashMap<String,String>();
        data.put("access_token", accessToken);
        
        if(params != null && params.size() > 0) {
            data.putAll(params);
        }
        String resp = null;
        if (filename != null) {
            data.put("description", message+' '+url);
            Map<String,String>file = new HashMap<String,String>();
            file.put("file" , filename);
            resp = this.response(HTTPClientUtil.POST_METHOD,
            		RENREN_PUBLISH_MESSAGE_WITH_PIC,data,null,file);
        } else {

        	data.put("targetUrl" , url);
            data.put("message",  message);
            data.put("description" , message);
            data.put("title" , "消息");
            resp = this.response(HTTPClientUtil.POST_METHOD,RENREN_PUBLICSH_MESSAGE_TEXT,data);

        }
        if (resp != null && !"".equals(resp)) {
            if (logger.isDebugEnabled()) {
                logger.debug("publish:" + resp);
            }
            
            Object obj = JSON.parse(resp);
    		JSONObject msg = (JSONObject)obj;
    		
    		JSONObject error = msg.getJSONObject("error");
            if (error != null) {
                result.setRv(ERROR_PUBLISH_MESSAGE);
                result.setErrmsg(error.getString("message"));
            } else {
                result.setRv(ERROR_SUCCESS);
            }
        } else {
            result.setRv(ERROR_NETWORK_CALL);
        }
        return result;
    }

}


	

	