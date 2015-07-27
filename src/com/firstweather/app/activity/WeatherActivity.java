package com.firstweather.app.activity;

import com.firstweather.app.R;
import com.firstweather.app.service.AutoUpdateService;
import com.firstweather.app.util.HttpCallbackListener;
import com.firstweather.app.util.HttpUtil;
import com.firstweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener {

	TextView cityNameText;
	TextView updateTimeText;
	TextView currentDateText;
	TextView temp1Text;
	TextView temp2Text;
	TextView weatherDescText;
	private LinearLayout weatherInfoLayout;
	Button switchButton;
	Button flashButton;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		setContentView(R.layout.weather_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		updateTimeText = (TextView) findViewById(R.id.update_time);
		currentDateText = (TextView) findViewById(R.id.current_data);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		weatherDescText = (TextView) findViewById(R.id.weather_desc);
		switchButton = (Button) findViewById(R.id.switch_city);
		flashButton = (Button) findViewById(R.id.flash);
		
		Intent intent=new Intent(this,AutoUpdateService.class);
		startService(intent);

		switchButton.setOnClickListener(this);
		flashButton.setOnClickListener(this);

		// 实例化linearlayout对象，该对象为weather_layout中的一个linearlayout，其中包裹天气内容
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info);

		// 获取前一activity传入Intent中的键为“county_code”的值
		String countyCode = getIntent().getStringExtra("county_code");

		if (!TextUtils.isEmpty(countyCode)) {
			// 有县级代号countyCode时就查询天气
			updateTimeText.setText("同步中。。。");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// 没有县级代号时就直接显示本地天气
			showWeather();
		}
	}

	// 根据传入的countyCode查询对应的天气code
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	// 根据传入的天气code查询天气信息
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	// 根据传入的type类型，从服务器查询对应类型的数据
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			// 根据传入的code类型，决定查询到 天气代号 还是 天气信息
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {

						// 将通过县级url得到的数据按“|”分隔开，得到array数组为{县级代号，天气代号}
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {

							// 得到天气代号，调用queryWeatherInfo方法
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {

					// 此时response为JSON格式的天气信息转换而成的字符串
					Log.i("TAG", response);

					// Utility的静态方法将得到的天气信息解析出来，并存到sharedPreference文件中
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);

					// 发送http的请求为耗时操作，此时还在子线程中，不能修改UI
					// 通过runOnUiThread（）方法回到主线程中，并修改UI，显示天气信息
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}

			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						updateTimeText.setText("同步失败");
					}
				});
			}
		});
	}

	// 一个程序中只有一个SharedPreference文件，得到该文件，并将其中信息显示到当前activity上
	private void showWeather() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(preferences.getString("city_name", ""));
		temp1Text.setText(preferences.getString("temp1", ""));
		temp2Text.setText(preferences.getString("temp2", ""));
		weatherDescText.setText(preferences.getString("weather_desc", ""));
		updateTimeText.setText("今天" + preferences.getString("update_time", "")
				+ "发布");
		currentDateText.setText(preferences.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);

			intent.putExtra("from_weatherActivity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.flash:
			updateTimeText.setText("同步中。。。");
			SharedPreferences preferences = PreferenceManager
					.getDefaultSharedPreferences(this);
			String weatherCode = preferences.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;

		default:
			break;
		}
	}
}
