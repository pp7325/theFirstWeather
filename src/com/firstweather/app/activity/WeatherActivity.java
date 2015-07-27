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
		requestWindowFeature(Window.FEATURE_NO_TITLE);// ���ر�����
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

		// ʵ����linearlayout���󣬸ö���Ϊweather_layout�е�һ��linearlayout�����а�����������
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info);

		// ��ȡǰһactivity����Intent�еļ�Ϊ��county_code����ֵ
		String countyCode = getIntent().getStringExtra("county_code");

		if (!TextUtils.isEmpty(countyCode)) {
			// ���ؼ�����countyCodeʱ�Ͳ�ѯ����
			updateTimeText.setText("ͬ���С�����");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			// û���ؼ�����ʱ��ֱ����ʾ��������
			showWeather();
		}
	}

	// ���ݴ����countyCode��ѯ��Ӧ������code
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	// ���ݴ��������code��ѯ������Ϣ
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/"
				+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}

	// ���ݴ����type���ͣ��ӷ�������ѯ��Ӧ���͵�����
	private void queryFromServer(final String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			// ���ݴ����code���ͣ�������ѯ�� �������� ���� ������Ϣ
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {

						// ��ͨ���ؼ�url�õ������ݰ���|���ָ������õ�array����Ϊ{�ؼ����ţ���������}
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {

							// �õ��������ţ�����queryWeatherInfo����
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {

					// ��ʱresponseΪJSON��ʽ��������Ϣת�����ɵ��ַ���
					Log.i("TAG", response);

					// Utility�ľ�̬�������õ���������Ϣ�������������浽sharedPreference�ļ���
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);

					// ����http������Ϊ��ʱ��������ʱ�������߳��У������޸�UI
					// ͨ��runOnUiThread���������ص����߳��У����޸�UI����ʾ������Ϣ
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
						updateTimeText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}

	// һ��������ֻ��һ��SharedPreference�ļ����õ����ļ�������������Ϣ��ʾ����ǰactivity��
	private void showWeather() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(preferences.getString("city_name", ""));
		temp1Text.setText(preferences.getString("temp1", ""));
		temp2Text.setText(preferences.getString("temp2", ""));
		weatherDescText.setText(preferences.getString("weather_desc", ""));
		updateTimeText.setText("����" + preferences.getString("update_time", "")
				+ "����");
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
			updateTimeText.setText("ͬ���С�����");
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
