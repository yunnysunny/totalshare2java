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
 * Class oauth2.
 *
 * 
 */
public abstract class AbstractOAuth2 {
	
	/** 腾讯sns标示. */
	public static final String SNS_TYPE_TX = "tx";

	/** 新浪sns标示. */
	public static final String SNS_TYPE_SINA = "sina";
	
	/** 人人网sns标示. */
	public static final String SNS_TYPE_RENREN = "renren";

	/** 豆瓣网sns标示. */
	public static final String SNS_TYPE_DOUBAN = "douban";

	/** 腾讯获取授权的PC端地址. */
	public static final String TX_AUTH_URL_PC = "https://graph.qq.com/oauth2.0/authorize";
	
	/** 腾讯获取授权的手机端地址. */
	public static final String TX_AUTH_URL_MOBILE = "https://graph.z.qq.com/moc2/authorize";
	
	/** 腾讯获取access token的电脑端地址. */
	public static final String TX_ACCESS_TOKEN_URL_PC = "https://graph.qq.com/oauth2.0/token";
	
	/** 腾讯获取access token的手机端地址. */
	public static final String TX_ACCESS_TOKEN_URL_MOBILE = "https://graph.z.qq.com/moc2/token";
	
	/** 腾讯获取open id的地址. */
	public static final String TX_GET_OPEN_ID = "https://graph.qq.com/oauth2.0/me";

	/** 腾讯获取用户昵称. */
	public static final String TX_GET_NICKNAME = "https://graph.qq.com/user/get_user_info";

	/** 腾讯发送带图片的微薄. */
	public static final String TX_PUBLISH_MESSAGE_WITH_PIC = "https://graph.qq.com/t/add_pic_t";

	/** 腾讯发布不带图片的微博. */
	public static final String TX_PUBLISH_MESSAGE_TEXT = "https://graph.qq.com/t/add_t";
	
	/** 新浪授权地址. */
	public static final String SINA_AUTH_URL = "https://api.weibo.com/oauth2/authorize";
	
	/** 新浪获取access token地址. */
	public static final String SINA_ACCESS_TOKEN_URL = "https://api.weibo.com/oauth2/access_token";

	/** 新浪获取uid的地址. */
	public static final String SINA_GET_UID = "https://api.weibo.com/oauth2/get_token_info";

	/** 新浪获取昵称. */
	public static final String SINA_GET_USER_INFO = "https://api.weibo.com/2/users/show.json";

	/** sina发送带图片的微薄. */
	public static final String SINA_PUBLISH_MESSAGE_WITH_PIC = "https://upload.api.weibo.com/2/statuses/upload.json";

	/** sina发送文本微博. */
	public static final String SINA_PUBLISH_MESSAGE_TEXT = "https://api.weibo.com/2/statuses/update.json";

	/** The Constant RENREN_AUTH_URL. */
	public static final String RENREN_AUTH_URL = "https://graph.renren.com/oauth/authorize";

	/** The Constant RENREN_ACCESS_TOKEN_URL. */
	public static final String RENREN_ACCESS_TOKEN_URL = "https://graph.renren.com/oauth/token";

	/** The Constant RENREN_GET_USER_INFO. */
	public static final String RENREN_GET_USER_INFO = "https://api.renren.com/v2/user/login/get";

	/** renren网上传图片到相册. */
	public static final String RENREN_PUBLISH_MESSAGE_WITH_PIC = "https://api.renren.com/v2/photo/upload";

	/** 人人网发布新鲜事. */
	public static final String RENREN_PUBLICSH_MESSAGE_TEXT = "https://api.renren.com/v2/feed/put";

	/** The Constant DOUBAN_AUTH_URL. */
	public static final String DOUBAN_AUTH_URL = "https://www.douban.com/service/auth2/auth";

	/** The Constant DOUBAN_ACCESS_TOKEN_URL. */
	public static final String DOUBAN_ACCESS_TOKEN_URL = "https://www.douban.com/service/auth2/token";

	/** The Constant DOUBAN_GET_USER_INFO. */
	public static final String DOUBAN_GET_USER_INFO = "https://api.douban.com/v2/user/~me";

	/** The Constant DOUBAN_PUBLISH_MESSAGE. */
	public static final String DOUBAN_PUBLISH_MESSAGE = "https://api.douban.com/shuo/v2/statuses/";

	/** The Constant ERROR_SUCCESS. */
	public static final int ERROR_SUCCESS = 0;

	/** The Constant ERROR_FAIL_DEFAULT. */
	public static final int ERROR_FAIL_DEFAULT = 1;

	/** The Constant ERROR_UID_NOT_EXIST. */
	public static final int ERROR_UID_NOT_EXIST = 2;

	/** The Constant ERROR_NETWORK_CALL. */
	public static final int ERROR_NETWORK_CALL = 3;

	/** The Constant ERROR_PARSE_CALLBACK. */
	public static final int ERROR_PARSE_CALLBACK = 4;

	/** The Constant ERROR_PARAM_EMPTY. */
	public static final int ERROR_PARAM_EMPTY = 5;
	
	/** The Constant ERROR_GET_UID. */
	public static final int ERROR_GET_UID = 6;
	
	/** The Constant ERROR_PUBLISH_MESSAGE. */
	public static final int ERROR_PUBLISH_MESSAGE = 7;

	/** The Constant HTTP_METHOD_GET. */
	public static final int HTTP_METHOD_GET = 0;
	
	/** The Constant HTTP_METHOD_POST. */
	public static final int HTTP_METHOD_POST = 1;

	/** client_id . */
	protected String clientId;
	/** client_key. */
	protected String clientKey;

	/** The scope. */
	protected String scope;

	/** 回调地址. */
	private String redirectUri;

	/** The access token. */
	protected String accessToken;

	/** The uid. */
	protected String uid = null;

	/** SNS类型. */
	protected String snsType;

	/** 昵称. */
	protected String nickname;

	/** 头像. */
	protected String avatar;

	/** 性别. */
	protected String gender;

	/**
	 * Instantiates a new abstract o auth2.
	 *
	 * @param config the config
	 */
	public AbstractOAuth2(SNSConfigBean config) {
		this.clientId = config.getClientId();
		this.clientKey = config.getClientKey();
		this.scope = config.getScope();
		this.redirectUri = config.getRedirectUri();
		this.accessToken = config.getAccessToken();
	}

	/**
	 * 获取当前的SNS类型.
	 *
	 * @return the sns type
	 */
	public String getSnsType() {
		return this.snsType;
	}

	/**
	 * 获取授权的网址,获取如下格式的请求地址：
	 * authorizeUrl?client_id=YOUR_CLIENT_ID&response_type=code
	 * &redirect_uri=YOUR_REGISTERED_REDIRECT_URI 上层程序在调用完成后，跳转到这个地址即可.
	 *
	 * @param authorizeUrl sns授权页的地址
	 * @param state oauth2中的state参数
	 * @param scope 要传递的一些额外参数
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

	/**
	 * 获取要跳转到的第三方网站的登陆地址
	 *
	 * @param state the state
	 * @return the authorize url
	 */
	public abstract String getAuthorizeUrl(String state);

	/**
	 * 获取access token,请求的url格式如下：
	 * accessTokenUrl?client_id=YOUR_CLIENT_ID&client_secret
	 * =YOUR_CLIENT_SECRET&grant_type=authorization_code
	 * &redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE.
	 *
	 * @param accessTokenUrl access token的获取地址
	 * @param code 获取access tooken 需要传递的code参数
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

	/**
	 * 获取access token
	 *
	 * @param code the code
	 * @return the access token
	 */
	public abstract AccessTokenResult getAccessToken(String code);

	/**
	 * 通过请求接口的方式获取第三方账号uid，返回请求后的响应信息.
	 *
	 * @param accessToken the access token
	 * @param uidUrl the uid url
	 * @param method the method
	 * @return the uid str
	 */
	protected String getUidStr(String accessToken, String uidUrl, int method) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", accessToken);

		return this.response(method, uidUrl, params);
	}

	/**
	 * 通过请求接口的方式获取第三方账号uid，返回请求后的响应信息，指定请求的方式为POST.
	 *
	 * @param accessToken the access token
	 * @param uidUrl the uid url
	 * @return the uid str
	 */
	protected String getUidStr(String accessToken, String uidUrl) {		
		return getUidStr(accessToken, uidUrl, HTTP_METHOD_POST);
	}

	/**
	 * 通过给定的access token的值来请求uid信息
	 *
	 * @param accessToken the access token
	 * @return the uid
	 */
	public abstract UidResult getUid(String accessToken);
	
	/**
	 * 通过当前对象的accessToken属性来请求uid信息，
	 * 调用前需保证之前的对象accessToken属性已经被赋值或者之前已经成功调用过{@link #getAccessToken}函数。
	 *
	 * @return the uid
	 */
	public UidResult getUid() {
		return getUid(accessToken);
	}

	/**
	 * 获取用户昵称、头像之类的信息，
	 * 调用前需保证之前的对象accessToken属性已经被赋值或者之前已经成功调用过{@link #getAccessToken}函数。
	 *
	 * @return the user info
	 */
	public abstract UserInfoResult getUserInfo();

	/**
	 * 发布消息.
	 *
	 * @param accessToken 
	 * @param message 消息内容
	 * @param url 消息中的url
	 * @param filename 要上传的图片
	 * @param params 额外附件参数
	 * @return the message publish result
	 */
	public abstract MessagePublishResult publishMessage(String accessToken,
			String message, String url, String filename,
			Map<String, String> params);
	/**
	 * 发布消息.	 *
	 * 调用前需保证之前的对象accessToken属性已经被赋值或者之前已经成功调用过{@link #getAccessToken}函数。
	 *  
	 * @param message 消息内容
	 * @param url 消息中的url
	 * @param filename 要上传的图片
	 * @param params 额外附件参数
	 * @return the message publish result
	 */
	public  MessagePublishResult publishMessage(
			String message, String url, String filename,
			Map<String, String> params) {
		return publishMessage(accessToken,message,url,filename,params);
	}
	/**
	 * 发布文本消息.
	 *
	 * @param accessToken the access token
	 * @param message 消息内容
	 * @param url 消息中的url
	 * @return the message publish result
	 */
	public MessagePublishResult publishMessage(String accessToken,
			String message, String url) {
		return publishMessage(accessToken,message,url,null,null);
	}
	
	/**
	 * 发布文本消息.
	 * 调用前需保证之前的对象accessToken属性已经被赋值或者之前已经成功调用过{@link #getAccessToken}函数。
	 *
	 * @param message 消息内容
	 * @param url 消息中的url
	 * @return the message publish result
	 */
	public MessagePublishResult publishMessage(String message, String url) {
		return publishMessage(accessToken,message,url,null,null);
	}

	/**
	 * Gets the oauth info.
	 *
	 * @return the oauth info
	 */
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
	 * HTTP请求
	 *
	 * @param method http请求方式，
	 * 可选值{@link com.whyun.util.http.HTTPClientUtil#POST_METHOD}、{@link com.whyun.util.http.HTTPClientUtil#GET_METHOD}
	 * @param url 请求url
	 * @param params 请求参数
	 * @param headers HTTP头部信息
	 * @param files 要上传的图片的本地地址
	 * @return 影响字符串
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

	/**
	 * HTTP请求
	 *
	 * @param method http请求方式，
	 * 可选值{@link com.whyun.util.http.HTTPClientUtil#POST_METHOD}、{@link com.whyun.util.http.HTTPClientUtil#GET_METHOD}
	 * @param url 请求url
	 * @param params 请求参数
	 * @return 影响字符串
	 */
	protected String response(int method, String url, Map<String, String> params) {
		return response(method, url, params, null, null);
	}
}
