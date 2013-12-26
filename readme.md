totalshare2java
===================================
totalshare系列类库是一个汇集常用SNS（现在支持腾讯、sina、人人三个网站）登陆的接口库。
## 目的 ##
在这个大数据时代，怎样才能跟得上时代的步伐，腾讯的用户活跃用户达到7亿，而你呢？不用担心，鲁迅教会了我们“拿来主义”，通过调用各个网站给出的api可以将别人家的用户顺利导入到自己家的数据库里。但是如果想吧各个网站中的对接接口依次封装调用就是个体力活了，为了让大家在代码集成的时候少走弯路，现在特意写了这么一套接口，来方便大家调用。
## 能做什么 ##
首先做到将第三方网站的用户数据和本地账号做关联，做到快速登录。其次，可以简化注册过程，直接使用第三方账号完成注册。最后还可以使用登录后的第三方账号发送分享信息，更加方便网站的推广。
## 不能做什么 ##
本套接口的分享功能很简单，仅仅提供了发送文字消息和发送图片消息的功能，但是一般网站都提供了各色各样的分享功能，为了降低代码编写的复杂度，同时更为了写出来的接口更加通过，这里提供的功能比较简单。
## 怎么运行demo ##
首先通过git工具将代码clone到你的本地目录，或者直接下载压缩包，然后解压到你的本地目录。之后，进入刚才项目所在的根目录后运行`mvn jetty:run`,然后打开浏览器输入http://whyun.com:8080/  
   
**注意**，这里用了域名whyun.com，SNS平台的api在集成的时候都需要注册开发者账号，并且提供一个可以访问的回调地址，我当初就填写了自己的域名，为此你需要在输入这个url之前先将你的hosts(在windows下位于%SystemRoot%/system32/Drivers/etc/hosts，linux下位于/etc/hosts)文件配置好，具体配置操作是在hosts文件中添加如下一行：
    
	127.0.0.1		whyun.com    
 
配置完成后保存。如果配置成功在命令行下`ping whyun.com`能看到解析到的ip为127.0.0.1。
## demo代码解析 ##
由于需要跳转到第三方网站完成登录功能，所以测试程序必须是web程序，所在在src/main/webapp目录下的创建了一个sns.jsp作为测试文件。其主要代码如下：

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
					UidResult uidResult = instance.getUid(accessTokenResult.getAccessToken());
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
## 配置 ##
第三方网站在进行oauth调用的时候，都需要给出指定的开发者注册信息，以完成调用校验。在本项目中，存在一个snsconfig.xml，存在于src/main/webapp目录下，其格式如下：

	<?xml version="1.0" encoding="UTF-8"?>
	<app>
	    <sns>
	        <type>tx</type>
	        <clientId>100474085</clientId>
	        <clientSecret>4a08759d140b989b8ca4f060e208864a</clientSecret>
	        <scope></scope>
	    </sns>
	    <sns>
	        <type>sina</type>
	        <clientId>3499650237</clientId>
	        <clientSecret>59c629bba60e99028f27f6e0de20393d</clientSecret>
	        <scope></scope>
	    </sns>
	    <sns>
	        <type>renren</type>
	        <clientId>085cf3221f984299a321a201f5948362</clientId>
	        <clientSecret>d93c0512130540768eb34f5f9720eaa2</clientSecret>
	        <scope></scope>
	    </sns>
	</app>
其中tx、sina、renren三个标示分别代表腾讯互联、sina微博和人人网。在OAuth2协议中，在跳转到第三方网站进行登录验证的时候需要提供clientId参数，在获取access token的时候需要提供clientId和clientSecret参数。但是对于不同的第三方网站，你注册完开发者账号后，给出的这两个参数的参数名称却是有差别的，腾讯互联中叫APP ID和APP KEY，新浪微博中叫App Key和App Sercet，人人网中叫APP KEY和Secret Key。在实际使用过程中，你注册完成后，将这些参数分别配置到这个配置文件中。同时应该注意，在构造一个AbstractOAuth2对象的时候要传入一个回调地址参数，这个回调地址中的域名必须和注册开发者账号的时候给出的域名一致，这就导致了你在本地测试的时候，不得不修改一下hosts文件将127.0.0.1映射为你注册的域名；当然你如果直接在线上测试就没有这个问题了。最后需要注意一下，在构造AbstractOAuth2对象时要保证，做登录验证前构造的对象和做获取access token是构造的对象，两者传递的redirectUri参数是相同的，否则获取access token的操作就会失败。

## license ##
Copyright 2013 whyun.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
