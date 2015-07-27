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
	// 解析和处理服务器返回的省级数据，并存到本地数据库中
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

	// 解析服务器返回的json数据，存到本地
	public static void handleWeatherResponse(Context context, String response) {
		try {
			// 得到一个JSONObje对象，response为服务器返回的json对象转换成的字符串
			// 再根据“weatherinfo”键得到其对应的JSON对象
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			
			// 根据JSON对象中的键得到相应的值
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

	// handleWeatherResponce中调用此方法，将解析JSON文件得到的数据存入SharedPreference文件中
	public static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesc,
			String updateTime) {
		
		//得到日期格式对象
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		
		// 得到SharedPreferences的Editor对象，通过SharedPreferences对象的edit（）方法
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();

		//设置一个键为“city_selected”的值，用于下次启动时通过该值判断sharedPreference文件是否 存在
		editor.putBoolean("city_selected", true);

		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desc", weatherDesc);
		editor.putString("update_time", updateTime);
		
		// 通过日期格式对象将当前日期格式化
		editor.putString("current_date", sdf.format(new Date()));
		
		// 通过SharedPreferences.Editor的commit（）方法将editor存入sharedpreference文件对象中
		editor.commit(); 

	}
}
