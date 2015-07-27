package com.firstweather.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class FirstWeatherOpenHelper extends SQLiteOpenHelper {

	public FirstWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	// 建立三张表：province省份、city城市、county县
	public static final String CREATE_PROVINCE = "create table province("
			+ "id integer primary key autoincrement," + "province_name text,"
			+ "province_code text)";
	public static final String CREATE_CITY = "create table city ("
			+ "id integer primary key autoincrement," + "city_name text,"
			+ "city_code text," + "province_id integer)";
	public static final String CREATE_COUNTY = "create table county("
			+ "id integer primary key autoincrement," + "county_name text,"
			+ "county_code text," + "city_id integer)";

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE); // 将三张表放在数据库db里
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
