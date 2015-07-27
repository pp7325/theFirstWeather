package com.firstweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.firstweather.app.model.City;
import com.firstweather.app.model.County;
import com.firstweather.app.model.FirstWeatherDB;
import com.firstweather.app.model.Province;

import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class Utility {
	// �����ʹ�����������ص�ʡ�����ݣ����浽�������ݿ���
	public synchronized static boolean handleProvincesResponse(
			FirstWeatherDB firstWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					firstWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	public static boolean handleCitiesResponse(FirstWeatherDB firstWeatherDB,
			String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					firstWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	public static boolean handleCountiesResponse(FirstWeatherDB firstWeatherDB,
			String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					firstWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

	// �������������ص�json���ݣ��浽����
	public static void handleWeatherResponse(Context context, String response) {
		try {
			// �õ�һ��JSONObje����responseΪ���������ص�json����ת���ɵ��ַ���
			// �ٸ��ݡ�weatherinfo�����õ����Ӧ��JSON����
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			
			// ����JSON�����еļ��õ���Ӧ��ֵ
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesc = weatherInfo.getString("weather");
			String updateTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2,
					weatherDesc, updateTime);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// handleWeatherResponce�е��ô˷�����������JSON�ļ��õ������ݴ���SharedPreference�ļ���
	public static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesc,
			String updateTime) {
		
		//�õ����ڸ�ʽ����
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
		
		// �õ�SharedPreferences��Editor����ͨ��SharedPreferences�����edit��������
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();

		//����һ����Ϊ��city_selected����ֵ�������´�����ʱͨ����ֵ�ж�sharedPreference�ļ��Ƿ� ����
		editor.putBoolean("city_selected", true);

		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desc", weatherDesc);
		editor.putString("update_time", updateTime);
		
		// ͨ�����ڸ�ʽ���󽫵�ǰ���ڸ�ʽ��
		editor.putString("current_date", sdf.format(new Date()));
		
		// ͨ��SharedPreferences.Editor��commit����������editor����sharedpreference�ļ�������
		editor.commit(); 

	}
}
