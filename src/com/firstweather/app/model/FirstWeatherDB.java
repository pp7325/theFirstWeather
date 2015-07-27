package com.firstweather.app.model;

import java.util.ArrayList;
import java.util.List;

import com.firstweather.app.db.FirstWeatherOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FirstWeatherDB {
	public static final String DB_NAME = "second_weather"; // 数据库名称
	public static final int VERSION = 1; // 数据库版本
	private static FirstWeatherDB firstWeather;
	private SQLiteDatabase db;

	private FirstWeatherDB(Context context) { // 构造函数私有化
		FirstWeatherOpenHelper dbHelper = new FirstWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	public synchronized static FirstWeatherDB getInstance(Context context) { // 获取FirstWeather实例
		// 判断实例是否存在，保证实例的唯一性
		if (firstWeather == null) {
			firstWeather = new FirstWeatherDB(context);
		}
		return firstWeather;
	}

	// 将Province实例存储到数据库中
	public void saveProvince(Province province) {
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("province", null, values);
		}
	}

	public void saveCity(City city) {
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("city", null, values);
		}
	}

	public void saveCounty(County county) {
		if (county != null) {
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("county", null, values);
		}
	}

	// 从数据库读取全国省份信息
	public List<Province> loadProvinces() {
		// 新建一个list存放Province对象
		List<Province> list = new ArrayList<Province>();
		
		// 查询Province表的所有信息，得到curson对象
		Cursor cursor = db
				.query("province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do { // 遍历cursor对象，取出数据
				Province province = new Province();
				// 得到“id”在表中的索引，然后根据索引取出值，必须先知道取出的数据类型（如：getInt）
				// 然后往province对象中放入通过cursor取出的值
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				// 将province对象加入list中
				list.add(province);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	public List<City> loadCities(int provinceId) {
		List<City> list = new ArrayList<City>();
		// 查询provice_id=provinceId时的city表
		Cursor cursor = db.query("city", null, "province_id = ?",
				new String[] { String.valueOf(provinceId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor
						.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor
						.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	public List<County> loadCounties(int cityId) {
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("county", null, "city_id=?",
				new String[] { String.valueOf(cityId) }, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("id")));
				county.setCountyName(cursor.getString(cursor
						.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor
						.getColumnIndex("county_code")));
				county.setCityId(cityId);
				list.add(county);
			} while (cursor.moveToNext());
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
}
