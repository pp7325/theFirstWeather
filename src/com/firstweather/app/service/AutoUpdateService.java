package com.firstweather.app.service;

import com.firstweather.app.receiver.AutoUpdateReceiver;
import com.firstweather.app.util.HttpCallbackListener;
import com.firstweather.app.util.HttpUtil;
import com.firstweather.app.util.Utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 开启一个线程来更新天气信息,并保存在sharedpreference文件中
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
			}
		}).start();
		
		//得到AlarmManager定时任务管理器实例
		AlarmManager manager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
//		设置一个 即将执行的意图，用来传给定时任务管理器
		Intent i=new Intent(this , AutoUpdateReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		
		//设置服务被触发的时间间隔参数，=开机至当前所经历的毫秒数+8小时
		//设定每隔8小时后启动 一个接受者AutoUpdateReceiver，执行onReceive()方法
		//在接受者中再跳转回当前service，达到每隔8小时执行一次service的目的，便形成了一个定时的后台服务
		//没有stopService（）或stopSelf（）方法服务便不会停止
		long triggerAtTime=SystemClock.elapsedRealtime()+8*60*60*1000;
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}

	protected void updateWeather() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String weatherCode = preferences.getString("weather_code", "");
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			// 根据传入的code类型，决定查询到 天气代号 还是 天气信息
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}

			public void onError(Exception e) {
				e.printStackTrace();
			}
		});
	}

}
