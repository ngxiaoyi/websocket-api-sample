package com.huobi.api.market;

public class Main {
	public static void main(String[] args) {
		try {
			WebSocketUtils.executeWebSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
