package com.whyun.totalshare.oauth2;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.whyun.totalshare.bean.AccessTokenResult;
import com.whyun.totalshare.bean.MessagePublishResult;
import com.whyun.totalshare.bean.OAuth2Info;
import com.whyun.totalshare.bean.SNSConfigBean;
import com.whyun.totalshare.bean.UidResult;
import com.whyun.totalshare.bean.UserInfoResult;
import com.whyun.util.http.HTTPClientUtil;
import com.whyun.util.http.HTTPClientUtil.HttpRequest;

/**
 * Class oauth2
 * 
 * @package com.whyun.
 */
public abstract class AbstractOAuth2 {
	/**
	 * 腾讯sns标示
	 */
	public static final String SNS_TYPE_TX = "tx";

	/**
	 * 新浪sns标示
	 */
	public static final String SNS_TYPE_SINA = "sina";
	/**
	 * 人人网sns标示
	 */
	public static final String SNS_TYPE_RENREN = "renren";

	/**
	 * 豆瓣网sns标示
	 */
	public static final String SNS_TYPE_DOUBAN = "douban";

	/**
	 * 腾讯获取授权的PC端地址
	 */
	public static final String TX_AUTH_URL_PC = "https://graph.qq.com/oauth2.0/authorize";
	/**
	 * 腾讯获取授权的手机端地址
	 */
	public static final String TX_AUTH_URL_MOBILE = "https://graph.z.qq.com/moc2/authorize";
	/**
	 * 腾讯获取access token的电脑端地址
	 */
	public static final String TX_ACCESS_TOKEN_URL_PC = "https://graph.qq.com/oauth2.0/token";
	/**
	 * 腾讯获取access token的手机端地址
	 */
	public static final String TX_ACCESS_TOKEN_URL_MOBILE = "https://graph.z.qq.com/moc2/token";
	/**
	 * 腾讯获取open id的地址
	 */
	public static final String TX_GET_OPEN_ID = "https://graph.qq.com/oauth2.0/me";

	/**
	 * 腾讯获取用户昵称
	 */
	public static final String TX_GET_NICKNAME = "https://graph.qq.com/user/get_user_info";

	/**
	 * 腾讯发送带图片的微薄
	 */
	public static final String TX_PUBLISH_MESSAGE_WITH_PIC = "https://graph.qq.com/t/add_pic_t";

	/**
	 * 腾讯发布不带图片的微博
	 */
	public static final String TX_PUBLISH_MESSAGE_TEXT = "https://graph.qq.com/t/add_t";
	/**
	 * 新浪授权地址
	 */
	public static final String SINA_AUTH_URL = "https://api.weibo.com/oauth2/authorize";
	/**
	 * 新浪获取access token地址
	 */
	public static final String SINA_ACCESS_TOKEN_URL = "https://api.weibo.com/oauth2/access_token";

	/**
	 * 新浪获取uid的地址
	 */
	public static final String SINA_GET_UID = "https://api.weibo.com/oauth2/get_token_info";

	/**
	 * 新浪获取昵称
	 */
	public static final String SINA_GET_USER_INFO = "https://api.weibo.com/2/users/show.json";

	/**
	 * sina发送带图片的微薄
	 */
	public static final String SINA_PUBLISH_MESSAGE_WITH_PIC = "https://upload.api.weibo.com/2/statuses/upload.json";

	/**
	 * sina发送文本微博
	 */
	public static final String SINA_PUBLISH_MESSAGE_TEXT = "https://api.weibo.com/2/statuses/update.json";

	public static final String RENREN_AUTH_URL = "https://graph.renren.com/oauth/authorize";

	public static final String RENREN_ACCESS_TOKEN_URL = "https://graph.renren.com/oauth/token";

	public static final String RENREN_GET_USER_INFO = "https://api.renren.com/v2/user/login/get";

	/**
	 * renren网上传图片到相册
	 */
	public static final String RENREN_PUBLISH_MESSAGE_WITH_PIC = "https://api.renren.com/v2/photo/upload";

	/**
	 * 人人网发布新鲜事
	 */
	public static final String RENREN_PUBLICSH_MESSAGE_TEXT = "https://api.renren.com/v2/feed/put";

	public static final String DOUBAN_AUTH_URL = "https://www.douban.com/service/auth2/auth";

	public static final String DOUBAN_ACCESS_TOKEN_URL = "https://www.douban.com/service/auth2/token";

	public static final String DOUBAN_GET_USER_INFO = "https://api.douban.com/v2/user/~me";

	public static final String DOUBAN_PUBLISH_MESSAGE = "https://api.douban.com/shuo/v2/statuses/";

	public static final int ERROR_SUCCESS = 0;

	public static final int ERROR_FAIL_DEFAULT = 1;

	public static final int ERROR_UID_NOT_EXIST = 2;

	public static final int ERROR_NETWORK_CALL = 3;

	public static final int ERROR_PARSE_CALLBACK = 4;

	public static final int ERROR_PARAM_EMPTY = 5;
	
	public static final int ERROR_GET_UID = 6;
	
	public static final int ERROR_PUBLISH_MESSAGE = 7;

	public static final int HTTP_METHOD_GET = 0;
	public static final int HTTP_METHOD_POST = 1;

	/** client_id . */
	protected String clientId;
	/** client_key. */
	protected String clientKey;

	protected String scope;

	private String redirectUri;

	protected String accessToken;

	protected String uid = null;

	protected String snsType;

	protected String nickname;

	protected String avatar;

	protected String gender;

	/**
	 * @param config
	 * 
	 * @param redirectUri
	 *            授权成功后重定向到的地址
	 */
	public AbstractOAuth2(SNSConfigBean config, String redirectUri) {
		this.clientId = config.getClientId();
		this.clientKey = config.getClientKey();
		this.scope = config.getScope();
		this.redirectUri = redirectUri;

	}

	/**
	 * @return
	 */
	public String getSnsType() {
		return this.snsType;
	}

	/**
	 * 获取授权的网址,获取如下格式的请求地址：
	 * authorizeUrl?client_id=YOUR_CLIENT_ID&response_type=code
	 * &redirect_uri=YOUR_REGISTERED_REDIRECT_URI 上层程序在调用完成后，跳转到这个地址即可
	 * 
	 * @param authorizeUrl
	 *            sns授权页的地址
	 * @param state
	 *            oauth2中的state参数
	 * @param scope
	 *            要传递的一些额外参数
	 * @return 返回sns授权的完成地址
	 */
	protected String getAuthorizeUrlStr(String authorizeUrl, String state,
			String scope) {
		final AbstractOAuth2 self = this;
		Map<String, String> params = new HashMap<String, String>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
				put("client_id", self.clientId);
				put("redirect_uri", self.redirectUri);
				put("response_type", "code");
			}
		};
		if (state != null && !"".equals(state)) {
			params.put("state", state);
		}
		if (scope != null && !"".equals(scope)) {
			params.put("scope", scope);
		}
		String fullUrl = null;
		try {
			fullUrl = HTTPClientUtil.getFullUrl(authorizeUrl, params);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fullUrl;
	}

	public abstract String getAuthorizeUrl(String state);

	/**
	 * 获取access token,请求的url格式如下：
	 * accessTokenUrl?client_id=YOUR_CLIENT_ID&client_secret
	 * =YOUR_CLIENT_SECRET&grant_type=authorization_code
	 * &redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
	 * 
	 * @param accessTokenUrl
	 *            access token的获取地址
	 * @param code
	 *            获取access tooken 需要传递的code参数
	 * @param params
	 *            一些额外参数
	 * @return string
	 */
	protected String getAccessTokenStr(String accessTokenUrl, String code) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", this.clientId);
		params.put("client_secret", this.clientKey);
		params.put("code", code);
		params.put("grant_type", "authorization_code");
		params.put("redirect_uri", this.redirectUri);

		return this.response(HTTP_METHOD_POST, accessTokenUrl, params);
	}

	public abstract AccessTokenResult getAccessToken(String code);

	protected String getUidStr(String accessToken, String uidUrl, int method) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken);

		return this.response(method, uidUrl, params);
	}

	protected String getUidStr(String accessToken, String uidUrl) {
		return getUidStr(accessToken, uidUrl, HTTP_METHOD_POST);
	}

	public abstract UidResult getUid(String accessToken);

	public abstract UserInfoResult getUserInfo();

	public abstract MessagePublishResult publishMessage(String accessToken,
			String message, String url, String filename,
			Map<String, String> params);
	
	public MessagePublishResult publishMessage(String accessToken,
			String message, String url) {
		return publishMessage(accessToken,message,url,null,null);
	}

	public OAuth2Info getOauthInfo() {
		OAuth2Info info = new OAuth2Info();
		info.setUid(this.uid);
		info.setAccessToken(this.accessToken);
		info.setAvatar(this.avatar);
		info.setGender(this.gender);
		info.setNickname(this.nickname);
		info.setSnsType(this.snsType);
		return info;
	}

	/**
	 * @param method
	 * @param url
	 * @param params
	 * @param null headers
	 * @param null files
	 * @return mixed
	 */
	protected String response(int method, String url,
			Map<String, String> params, Map<String, String> headers,
			Map<String, String> files) {
		
		HttpRequest req = new HttpRequest();
		req.setMethod(method)
		.setFiles(files)
		.setParams(params)
		.setHeader(headers)
		.setUrl(url);
		return HTTPClientUtil.getResponse(req);
	}

	protected String response(int method, String url, Map<String, String> params) {
		return response(method, url, params, null, null);
	}
}
