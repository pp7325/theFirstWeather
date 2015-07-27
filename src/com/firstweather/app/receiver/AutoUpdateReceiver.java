package com.firstweather.app.receiver;

import com.firstweather.app.service.AutoUpdateService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		//启动服务，实现8小时执行一次的循环
		Intent i=new Intent(context,AutoUpdateService.class);
		context.startService(i);
		
		

	}

}
