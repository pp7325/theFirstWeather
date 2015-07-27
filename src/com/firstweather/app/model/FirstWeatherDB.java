package com.firstweather.app.model;

import java.util.ArrayList;
import java.util.List;

import com.firstweather.app.db.FirstWeatherOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class FirstWeatherDB {
	public static final String DB_NAME = "second_weather"; // ���ݿ�����
	public static final int VERSION = 1; // ���ݿ�汾
	private static FirstWeatherDB firstWeather;
	private SQLiteDatabase db;

	private FirstWeatherDB(Context context) { // ���캯��˽�л�
		FirstWeatherOpenHelper dbHelper = new FirstWeatherOpenHelper(context,
				DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}

	public synchronized static FirstWeatherDB getInstance(Context context) { // ��ȡFirstWeatherʵ��
		// �ж�ʵ���Ƿ���ڣ���֤ʵ����Ψһ��
		if (firstWeather == null) {
			firstWeather = new FirstWeatherDB(context);
		}
		return firstWeather;
	}

	// ��Provinceʵ���洢�����ݿ���
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

	// �����ݿ��ȡȫ��ʡ����Ϣ
	public List<Province> loadProvinces() {
		// �½�һ��list���Province����
		List<Province> list = new ArrayList<Province>();
		
		// ��ѯProvince���������Ϣ���õ�curson����
		Cursor cursor = db
				.query("province", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do { // ����cursor����ȡ������
				Province province = new Province();
				// �õ���id���ڱ��е�������Ȼ���������ȡ��ֵ��������֪��ȡ�����������ͣ��磺getInt��
				// Ȼ����province�����з���ͨ��cursorȡ����ֵ
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor
						.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor
						.getColumnIndex("province_code")));
				// ��province�������list��
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
		// ��ѯprovice_id=provinceIdʱ��city��
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
