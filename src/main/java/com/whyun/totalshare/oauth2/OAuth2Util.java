package com.whyun.totalshare.oauth2;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.whyun.totalshare.SNSConfig;
import com.whyun.totalshare.bean.SNSConfigBean;
import com.whyun.totalshare.oauth2.impl.RenRenOAuth2;
import com.whyun.totalshare.oauth2.impl.SinaOAuth2;
import com.whyun.totalshare.oauth2.impl.TxOAuth2;
import com.whyun.util.security.RandomString;

public class OAuth2Util {
	private static Logger logger = Logger.getLogger(OAuth2Util.class);
	public static AbstractOAuth2 getInstance(String snsType, String redirectUri,String accessToken)
			throws FileNotFoundException
    {

		AbstractOAuth2 instance = null;

		SNSConfig config = SNSConfig.getInstance();
		SNSConfigBean configNow = config.getConfig(snsType);
		if (configNow != null) {
			if (redirectUri != null) {
				configNow.setRedirectUri(redirectUri);
			}
			if (accessToken != null) {
				configNow.setAccessToken(accessToken);
			}
		}
        
		if (AbstractOAuth2.SNS_TYPE_SINA.equals(snsType)) {                
                instance = new SinaOAuth2(configNow);
		} else if (AbstractOAuth2.SNS_TYPE_TX.equals(snsType)) {            	
                instance = new TxOAuth2(configNow);
		} else if (AbstractOAuth2.SNS_TYPE_RENREN.equals(snsType)) {            	
                instance = new RenRenOAuth2(configNow);
		}

        return instance;
    }
	
	public static AbstractOAuth2 getInstance(String snsType, String redirectUri)
			throws FileNotFoundException {
		return getInstance(snsType,redirectUri,null);
	}

    public static String genState(AbstractOAuth2 instance, String redirectUri, HttpSession session)
    {
        String type = instance.getSnsType();
        Map<String,String> data = new HashMap<String,String>();
        data.put("type", type);
        data.put("rand", RandomString.rand());
        String state = Base64.encodeBase64URLSafeString(JSON.toJSONBytes(data));
        
        session.setAttribute("state", state);
        return state;
    }

    public static Map<String,String> compareAndGetState(String stateRecive,HttpSession session)
    {
        
    	Map<String,String> data = null;
        if (session.getAttribute("state").equals(stateRecive)) {
            data = (Map<String,String>)JSON.parse(Base64.decodeBase64(stateRecive));
            
        } else {
        	logger.warn("session中的state:" + session.getAttribute("state") 
        			+",url中的code：" +stateRecive);
        }
        return data;
    }
}
