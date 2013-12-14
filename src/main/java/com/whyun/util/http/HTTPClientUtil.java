package com.whyun.util.http;

//import java.io.InputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
//import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

public class HTTPClientUtil {
	public static final int POST_METHOD = 1;
	public static final int GET_METHOD = 2;
	public static final int HTML_CONTENT_TYPE = 0;
	public static final int XML_CONTENT_TYPE = 1;
	public static final int FORM_CONTENT_TYPE = 2;
	public static final int FILE_CONTENT_TYPE = 3;
	static Logger logger = Logger.getLogger(HTTPClientUtil.class);

	private static HttpPost getXMLPostMethod(String requestContent, String url)
			throws UnsupportedEncodingException {
		HttpPost httppost = new HttpPost(url);
		
		StringEntity myEntity = new StringEntity(requestContent, "UTF-8");
		httppost.addHeader("Content-Type", "text/xml");
		httppost.setEntity(myEntity);

		return httppost;
	}

	private static HttpPost getFormPostMethod(Map<String, String> data,
			String url) throws Exception {
		HttpPost httpost = new HttpPost(url);
		// 建立HttpPost对象
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		// 建立一个NameValuePair数组，用于存储欲传送的参数
		for (Map.Entry<String, String> entry : data.entrySet()) {
			params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		httpost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

		return httpost;
	}
	
	private static HttpPost getFilePostMethod(Map<String, String> files, Map<String, String> data,
			String url) throws Exception {
		HttpPost httpost = new HttpPost(url);
//		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		if (data != null && data.size() > 0) {
			for (Map.Entry<String, String> entry : data.entrySet()) {
				builder.addPart(entry.getKey(),
						new StringBody(entry.getValue(),ContentType.TEXT_PLAIN));
			}
		}
		if (files != null && files.size() > 0) {
			for (Map.Entry<String, String> entry : files.entrySet()) {
				FileBody  bin = new FileBody (new File(entry.getValue()));
				builder.addPart(entry.getKey(), bin);
			}
		}
		httpost.setEntity(builder.build());
		
		return httpost;
	}
	public static String getFullUrl(String url, Map<String ,String>data)
			throws UnsupportedEncodingException {
		if (data != null && data.size() > 0) {
			if (url.indexOf('?') != -1) {
				url += "&";
			} else {
				url += "?";
			}
			int size = data.size();
			int i = 0;
			String paramStr = "";
			for (Map.Entry<String, String> entry : data.entrySet()) {

				paramStr += entry.getKey() + '='
						+ URLEncoder.encode(entry.getValue(), "UTF-8");
				if (i != size - 1) {
					paramStr += "&";
				}
				i++;
			}
			url += paramStr;
		}
		return url;
	}

	private static HttpGet getGetMethod(String url, Map<String, String> data)
			throws UnsupportedEncodingException {
		

		HttpGet httpget = new HttpGet(getFullUrl(url,data));

		return httpget;
	}

	public static int getHttpResponseCode(String url,
			Map<String, String> data) throws ClientProtocolException,
			IOException {
		Http http = Http.getInstance();
		HttpClient client = http.getClient();

		HttpGet get = getGetMethod(url, data);
		HttpResponse response = client.execute(get);

		int responseCode = response.getStatusLine().getStatusCode();		
		return responseCode;
	}

	public static String getResponse(HttpRequest request) {

		Http http = Http.getInstance();
		CloseableHttpClient client = http.getClient();

		// HttpRequestBase post = null;
		String response = null;
		CloseableHttpResponse  responseHttp = null;
		HttpUriRequest uriReq = null;
		int method = request.getMethod();
		String url = request.getUrl();
		
		try {
			if (method == POST_METHOD) {
				int contentType = request.getSendType();
				if (contentType == XML_CONTENT_TYPE) {
					uriReq = getXMLPostMethod(request.getSendContent(), url);
					
				} else if (contentType == FILE_CONTENT_TYPE) {
					getFilePostMethod(request.getFiles(),request.getParams(),url);
				} else {
					uriReq = getFormPostMethod(request.getParams(), url);		
				}
			} else {
				uriReq = getGetMethod(url, request.getParams());				
			}
			Map<String,String> headers = request.getHeader();
			if (headers != null && headers.size() > 0) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					uriReq.addHeader(entry.getKey(), entry.getValue());
				}
			}
			responseHttp = client.execute(uriReq, HttpClientContext.create());			

			HttpEntity resEntity = responseHttp.getEntity();
			// resEntity.
			InputStreamReader reader = new InputStreamReader(
					resEntity.getContent(), "UTF-8");
			char[] buff = new char[1024];
			int length = 0;
			response = "";
			while ((length = reader.read(buff)) != -1) {
				response += new String(buff, 0, length);
				buff = new char[1024];
			}
			buff = null;
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			
			try {
				client.close();
				if(responseHttp != null) {
					responseHttp.close();
				}
			} catch (IOException e) {
				logger.warn("", e);
			}
			
		}
		return response;
	}

	public static String xmlRequest(String url,String requestContent) {
		HttpRequest req = new HttpRequest();
		req.setSendContent(requestContent)
		.setUrl(url)
		.setSendType(XML_CONTENT_TYPE)
		.setMethod(POST_METHOD);
		return getResponse(req);
	}

	public static String getQequest(String url) {
		HttpRequest req = new HttpRequest();
		req.setMethod(GET_METHOD).setUrl(url);
		return getResponse(req);
	}

	public static String postRequest(String url, Map<String, String> data) {
		HttpRequest req = new HttpRequest();
		req.setMethod(POST_METHOD)
		.setSendType(FORM_CONTENT_TYPE)
		.setParams(data)
		.setUrl(url);
		return getResponse(req);
	}

	public static String getQequest(String url, Map<String, String> data) {
		HttpRequest req = new HttpRequest();
		req.setMethod(GET_METHOD).setParams(data).setUrl(url);
		return getResponse(req);
	}
	
	public static Map<String,String> parseUriParam(String uriStr) {
		Map<String,String> params = null;
		
		List<NameValuePair> list =
				URLEncodedUtils.parse(uriStr, Charset.forName("utf-8"));
		if (list != null && list.size() > 0) {
			params = new HashMap<String,String>();
			for(NameValuePair pair : list) {
				params.put(pair.getName(),pair.getValue());
			}
		}		
		
		return params;
	}
	
	public static class HttpRequest {
		private int method;
		private String url;
		private int sendType;
		private String sendContent;
		private Map<String,String> params;
		private Map<String,String> header;
		private Map<String,String> files;
		
		public HttpRequest() {
			
		}
		public int getMethod() {
			return method;
		}
		public HttpRequest setMethod(int method) {
			this.method = method;
			return this;
		}
		public int getSendType() {
			return sendType;
		}
		public HttpRequest setSendType(int sendType) {
			this.sendType = sendType;
			return this;
		}
		public Map<String, String> getParams() {
			return params;
		}
		public HttpRequest setParams(Map<String, String> params) {
			this.params = params;
			return this;
		}
		public Map<String, String> getHeader() {
			return header;
		}
		public HttpRequest setHeader(Map<String, String> header) {
			this.header = header;
			return this;
		}
		public Map<String, String> getFiles() {
			return files;
		}
		public HttpRequest setFiles(Map<String, String> files) {
			this.files = files;
			return this;
		}
		public String getSendContent() {
			return sendContent;
		}
		public HttpRequest setSendContent(String sendContent) {
			this.sendContent = sendContent;
			return this;
		}
		public String getUrl() {
			return url;
		}
		public HttpRequest setUrl(String url) {
			this.url = url;
			return this;
		}			
	}

	public static void main(String argc[]) throws IOException {
		String url = "https://github.com";
		 String response = getQequest(url);
		 System.out.println(response);
	}
}
