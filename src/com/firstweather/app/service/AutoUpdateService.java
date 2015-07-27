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
		// ����һ���߳�������������Ϣ,��������sharedpreference�ļ���
		new Thread(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
			}
		}).start();
		
		//�õ�AlarmManager��ʱ���������ʵ��
		AlarmManager manager=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
//		����һ�� ����ִ�е���ͼ������������ʱ���������
		Intent i=new Intent(this , AutoUpdateReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		
		//���÷��񱻴�����ʱ����������=��������ǰ�������ĺ�����+8Сʱ
		//�趨ÿ��8Сʱ������ һ��������AutoUpdateReceiver��ִ��onReceive()����
		//�ڽ�����������ת�ص�ǰservice���ﵽÿ��8Сʱִ��һ��service��Ŀ�ģ����γ���һ����ʱ�ĺ�̨����
		//û��stopService������stopSelf������������㲻��ֹͣ
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

			// ���ݴ����code���ͣ�������ѯ�� �������� ���� ������Ϣ
			public void onFinish(String response) {
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
			}

			public void onError(Exception e) {
				e.printStackTrace();
			}
		});
	}

}
