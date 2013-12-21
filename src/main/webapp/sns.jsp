<%@page import="com.whyun.totalshare.oauth2.OAuth2Util"%>
<%@page import="com.whyun.totalshare.oauth2.AbstractOAuth2"%>
<%@page import="com.whyun.totalshare.bean.*" %>
<%@page import="java.util.Map" %>
<%@page import="java.util.Date" %>
<%@page import="java.io.PrintWriter" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>SNS测试</title>
</head>
<body>
<%
	final String REDIRECT_BASE_URL = "http://whyun.com:8080/sns.jsp?from=";
	String from = request.getParameter("from");
	PrintWriter writer = response.getWriter();
	if (from != null && !"".equals(from)) {//回调操作，用户在第三方网站上登录完成后，会进入如下逻辑
		//这里的code变量登录成功后返回的值，在获取access token的时候要用得到
		String code = request.getParameter("code");
		//这里的state值，即为之前登录之前得到的那个state，这里需要和之前session中保存的值做比对
		Map<String,String> state = OAuth2Util.compareAndGetState(
				request.getParameter("state"),request.getSession());
		if (state != null) {//如果比对成功，则证明是一个合法的返回，接着进行数据处理
			String type = state.get("type");//从state中取出type属性
			if (type != null) {
				String redirectUri = REDIRECT_BASE_URL + type;//组合得到回调地址
				AbstractOAuth2 instance = OAuth2Util.getInstance(type, redirectUri);
				//得到AbstractOAuth2对象
				if (instance == null) {
					writer.write("type["+type+"]不被支持");
				} else {
					AccessTokenResult accessTokenResult = instance.getAccessToken(code);
					//根据code值获取access token
					if (accessTokenResult.getRv() != 0) {
						writer.write("获取accessToken失败："+accessTokenResult.getRv());
						return;
					}
					//根据access token获取第三方登录账号的uid数据，
					//此函数可以用于将第三方账号和本地账号进行绑定的场景
					UidResult uidResult = instance.getUid();
					if (uidResult.getRv() != 0) {
						writer.write("获取uid失败："+uidResult.getRv());
						return;
					}
					/**获取当前登录的第三方账号的基本信息，比如昵称、头像,
					* 在调用之前需要先保证getAccessToken函数之前已经被调用过。
					* 此函数可以用于使用第三方账号完成快速注册的场景
					*/
					UserInfoResult userInfoResult = instance.getUserInfo();
					if (userInfoResult.getRv() != 0) {
						writer.write("获取用户信息失败："+userInfoResult.getRv());
						return;
					}
					writer.write("用户信息:"+userInfoResult);
					//这里返回与oauth2相关的属性，比如access token、uid、nickname
					OAuth2Info info = instance.getOauthInfo();
					//发送消息
					MessagePublishResult result = instance.publishMessage(info.getAccessToken(),
							"这是我的测试发送消息 at " + new Date().getTime(), "http://whyun.com");
					if (result.getRv() == 0) {
						writer.write("发送消息成功");
					} else {
						writer.write("发送消息失败：" + result.getRv());
					}
				}
			} else {
				writer.write("type为空");
			}
			
		} else {
			writer.write("state值不正确，有可能是xss");
		}
	} else {//加载登录页操作，首次访问sns.jsp的时候会进入下面的逻辑，
		//其目的是为了获取第三方网站的登陆url，进行用户登录
		String type = request.getParameter("type");
		if (type == null || "".equals(type)) {
			type = "tx";
		}
		//定义回调地址，跳转到第三方网站登录完成后，要回跳到redirectUri这个地址上
		String redirectUri = REDIRECT_BASE_URL + type;
		//获取一个AbstractOAuth2对象
		AbstractOAuth2 instance = OAuth2Util.getInstance(type, redirectUri);
		if (instance == null) {
			writer.write("不被支持的type["+type+"]");
			return;
		}
		//获取一个状态值，这个地方主要是为了防止跨站访问攻击，所以在跳转之前生成一个状态字符串，
		//附带到跳转地址中，同时将其保存到session中；等跳转回sns.jsp时，再跟session中的值做比对。
		//state值其实是一个json字符串，其格式为{rand:一串随机字符串,type:当前单点登录的类型}
		String state = OAuth2Util.genState(instance, redirectUri, request.getSession());
		//获取跳转到第三方网站的登陆地址
		String snsUrl = instance.getAuthorizeUrl(state);
		//浏览器跳转到这个登录地址
		response.sendRedirect(snsUrl);
	}
%>
</body>
</html>