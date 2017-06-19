package com.huobi.api.market;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import com.alibaba.fastjson.JSONObject;

public class WebSocketUtils extends WebSocketClient {

//	private static final String url = "wss://be.huobi.com/ws";
	private static final String url = "wss://api.huobi.com/ws";

	private static WebSocketUtils chatclient = null;

	public WebSocketUtils(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public WebSocketUtils(URI serverURI) {
		super(serverURI);
	}

	public WebSocketUtils(URI serverUri, Map<String, String> headers, int connecttimeout) {
		super(serverUri, new Draft_17(), headers, connecttimeout);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		System.out.println("开流--opened connection");
	}

	@Override
	public void onMessage(ByteBuffer socketBuffer) {
		try {
			String marketStr = CommonUtils.byteBufferToString(socketBuffer);
			String market = CommonUtils.uncompress(marketStr);
			if (market.contains("ping")) {
				System.out.println(market.replace("ping", "pong"));
				// Client 心跳
				chatclient.send(market.replace("ping", "pong"));
			} else {
				System.out.println(" market:" + market);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessage(String message) {
		System.out.println("接收--received: " + message);
	}

	public void onFragment(Framedata fragment) {
		System.out.println("片段--received fragment: " + new String(fragment.getPayloadData().array()));
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		System.out.println("关流--Connection closed by " + (remote ? "remote peer" : "us"));
	}

	@Override
	public void onError(Exception ex) {
		System.out.println("WebSocket 连接异常: " + ex);
	}

	public static Map<String, String> getWebSocketHeaders() throws IOException {
		Map<String, String> headers = new HashMap<String, String>();
		return headers;
	}

	private static void trustAllHosts(WebSocketUtils appClient) {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			appClient.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sc));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void executeWebSocket() throws Exception {
		// WebSocketImpl.DEBUG = true;
		chatclient = new WebSocketUtils(new URI(url), getWebSocketHeaders(), 1000);
		trustAllHosts(chatclient);
		chatclient.connectBlocking();
		// 订阅K线数据 sub 根据自己需要订阅数据
		SubModel subModel = new SubModel();
		subModel.setSub("market.btccny.kline.1min");
		subModel.setId(10000L);
		chatclient.send(JSONObject.toJSONString(subModel));

		// 订阅数据深度
		SubModel subModel1 = new SubModel();
		subModel1.setSub("market.btccny.depth.percent10");
		subModel1.setId(10001L);
		chatclient.send(JSONObject.toJSONString(subModel1));
		// 取消订阅省略

		// 请求数据 sub 根据自己需要请求数据
		ReqModel reqModel = new ReqModel();
		reqModel.setReq("market.btccny.depth.percent10");
		reqModel.setId(10002L);
		chatclient.send(JSONObject.toJSONString(reqModel));

		// 请求数据
		ReqModel reqModel1 = new ReqModel();
		reqModel1.setReq("market.btccny.detail");
		reqModel1.setId(10003L);
		chatclient.send(JSONObject.toJSONString(reqModel1));
		System.out.println("send : " + JSONObject.toJSONString(reqModel));
	}
}