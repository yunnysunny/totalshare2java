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
if (from != null && !"".equals(from)) {//回调操作
	String code = request.getParameter("code");
	Map<String,String> state = OAuth2Util.compareAndGetState(
			request.getParameter("state"),request.getSession());
	if (state != null) {
		String type = state.get("type");
		if (type != null) {
			String redirectUri = REDIRECT_BASE_URL + type;
			AbstractOAuth2 instance = OAuth2Util.getInstance(type, redirectUri);
			if (instance == null) {
				writer.write("type["+type+"]不被支持");
			} else {
				AccessTokenResult accessTokenResult = instance.getAccessToken(code);
				if (accessTokenResult.getRv() != 0) {
					writer.write("获取accessToken失败："+accessTokenResult.getRv());
					return;
				}
				UidResult uidResult = instance.getUid(accessTokenResult.getAccessToken());
				if (uidResult.getRv() != 0) {
					writer.write("获取uid失败："+uidResult.getRv());
					return;
				}
				UserInfoResult userInfoResult = instance.getUserInfo();
				if (userInfoResult.getRv() != 0) {
					writer.write("获取用户信息失败："+userInfoResult.getRv());
					return;
				}
				writer.write("用户信息:"+userInfoResult);
				OAuth2Info info = instance.getOauthInfo();
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
} else {//加载登录页操作
	String type = request.getParameter("type");
	if (type == null || "".equals(type)) {
		type = "tx";
	}
	String redirectUri = REDIRECT_BASE_URL + type;
	AbstractOAuth2 instance = OAuth2Util.getInstance(type, redirectUri);
	if (instance == null) {
		writer.write("不被支持的type["+type+"]");
		return;
	}
	String state = OAuth2Util.genState(instance, redirectUri, request.getSession());
	String snsUrl = instance.getAuthorizeUrl(state);
	response.sendRedirect(snsUrl);
}
%>
</body>
</html>