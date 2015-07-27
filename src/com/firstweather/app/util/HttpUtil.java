//package com.firstweather.app.util;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URISyntaxException;
//import java.net.URL;
//
//import org.apache.http.HttpConnection;
//
//import android.net.Uri;
//
//public class HttpUtil {
//
//	public static void sendHttpRequest(final String address,
//			final HttpCallbackListener listener) {
//		new Thread(new Runnable() {
//			public void run() {
//				HttpURLConnection connection = null;
//				try {
//					// 发送http请求
//					URL url = new URL(address);
//					connection = (HttpURLConnection) url.openConnection();
//					connection.setRequestMethod("GET");
//					connection.setConnectTimeout(5000);
//					connection.setReadTimeout(5000);
//
//					//得到该请求所返回的输入流（字节流）对象
//					InputStream in = connection.getInputStream();
//					//将字节流转换成字符流，并存到缓冲区
//					BufferedReader reader = new BufferedReader(
//							new InputStreamReader(in));
//					StringBuilder response=new StringBuilder();
//					String line;
//					while ((line=reader.readLine())!=null) {
//						response.append(line);
//					}
//					if(listener!=null) {
//						listener.onFinish(response.toString());
//					}
//
//				} catch (Exception e) {
//					if (listener!=null) {
//						listener.onError(e);
//					}
//				}finally{
//					if (connection!=null) {
//						connection.disconnect();
//					}
//				}
//
//			}
//		}).start();
//
//	}
//}
package com.firstweather.app.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpUtil {
	
	public static void sendHttpRequest(final String address,
			final HttpCallbackListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					if (listener != null) {
						// 回调onFinish()方法
						listener.onFinish(response.toString());
//						Log.i("TAG",response.toString());
					}
				} catch (Exception e) {
					if (listener != null) {
						// 回调onError()方法
						listener.onError(e);
					}
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}

}
