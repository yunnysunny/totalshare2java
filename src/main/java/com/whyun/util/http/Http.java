package com.whyun.util.http;

import java.util.concurrent.TimeUnit;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.config.SocketConfig.Builder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

public class Http {
	private static PoolingHttpClientConnectionManager connectionManager;
	private static int connectionTimeOut = 80000;
	private static int socketTimeOut = 30000;
	private static int maxConnectionPerHost = 400;
	private static int maxTotalConnections = 800;
	// Since the Configuration has not yet been setted,
	// then an unconfigured client is returned.
	private static Logger logger = Logger.getLogger(Http.class);
	private static Http http = new Http();
	
	
	private static final ConnectionKeepAliveStrategy MY_STRATEGY = new ConnectionKeepAliveStrategy() {
		public long getKeepAliveDuration(HttpResponse response,
				HttpContext context) {
			// 兑现'keep-alive'头部信息
			HeaderElementIterator it = new BasicHeaderElementIterator(
					response.headerIterator(HTTP.CONN_KEEP_ALIVE));
			while (it.hasNext()) {
				HeaderElement he = it.nextElement();
				String param = he.getName();
				String value = he.getValue();
				if (value != null && param.equalsIgnoreCase("timeout")) {
					try {
						return Long.parseLong(value) * 1000;
					} catch (NumberFormatException ignore) {
					}
				}
			}
			// 否则保持活动30秒
			return 30 * 1000;
		}
	};

	public CloseableHttpClient getClient() {
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setSocketTimeout(socketTimeOut)
				.setConnectTimeout(connectionTimeOut)
				.build();
		
		CloseableHttpClient httpClient = HttpClients.custom()
		        .setConnectionManager(connectionManager)
		        .setKeepAliveStrategy(MY_STRATEGY)
		        .setDefaultRequestConfig(defaultRequestConfig)
		        .build();

		
		return httpClient;
	}

	private Http() {
		configureClient();
	}
	
	private void configureClient() {
		int maxTotalConnInt = ClientConfig.getInt("maxTotalConnections");
		int maxConnPerHostInt = ClientConfig.getInt("maxConnectionPerHost");
		maxTotalConnections = maxTotalConnInt > 0 ? maxTotalConnInt
				: maxTotalConnections;
		maxConnectionPerHost = maxConnPerHostInt > 0 ? maxConnPerHostInt
				: maxConnectionPerHost;
		if (logger.isDebugEnabled()) {
			logger.debug("maxTotalConnections:"+maxTotalConnections+",maxConnectionPerHost:"+maxConnectionPerHost);
		}

//		SchemeRegistry schemeRegistry = new SchemeRegistry();
//		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
//				.getSocketFactory()));
//		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
//				.getSocketFactory()));
		connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(maxTotalConnections);
		

//				HttpConnectionManagerParams params = connectionManager.;
		//		params.setConnectionTimeout(connectionTimeOut);
		//		params.setSoTimeout(socketTimeOut);
		
		if (maxTotalConnections > maxConnectionPerHost) {			
			connectionManager.setDefaultMaxPerRoute(maxConnectionPerHost);
		} else {			
			connectionManager.setDefaultMaxPerRoute(maxTotalConnections);
		}
		
		int socketTimeoutConfig = ClientConfig.getInt("soTimeout");
		int connTimeoutConfig = ClientConfig.getInt("connTimeout");
		socketTimeOut = socketTimeoutConfig > 0 ? 
				socketTimeoutConfig : 
					socketTimeOut;
		connectionTimeOut = connTimeoutConfig > 0 ?
				connTimeoutConfig :
					connectionTimeOut;
		
		
		Builder build = SocketConfig.custom()
				.setTcpNoDelay(true)
				.setSoTimeout(socketTimeOut);
		connectionManager.setDefaultSocketConfig(build.build());
		
		
		new IdleConnectionMonitorThread(connectionManager).start();
	}

	public static Http getInstance() {
		return http;
	}

	public static class IdleConnectionMonitorThread extends Thread {
		private final HttpClientConnectionManager connMgr;
		private volatile boolean shutdown = false;

		public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
			super();
			this.connMgr = connMgr;
		}

		@Override
		public void run() {
			try {
				while (!shutdown) {
					synchronized (this) {
						wait(5000);
						// 关闭过期连接 
						connMgr.closeExpiredConnections();
						// 可选地，关闭空闲超过30秒的连接 
						connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
						//logger.debug("当前池中连接："+connMgr.getConnectionsInPool());
					}
				}
			} catch (InterruptedException ex) {
				// 终止 
			}
		}

		public void shutdown() {

			shutdown = true;
			synchronized (this) {
				notifyAll();
			}
		}
	}

}
