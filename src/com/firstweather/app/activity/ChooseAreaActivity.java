package com.firstweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firstweather.app.R;
import com.firstweather.app.R.id;
import com.firstweather.app.model.City;
import com.firstweather.app.model.County;
import com.firstweather.app.model.FirstWeatherDB;
import com.firstweather.app.model.Province;
import com.firstweather.app.util.HttpCallbackListener;
import com.firstweather.app.util.HttpUtil;
import com.firstweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
	private ListView listview;
	private TextView titleText;
	private ArrayAdapter<String> adapter;
	private List<String> dataList = new ArrayList<String>();
	FirstWeatherDB firstWeatherDB;

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	public int currentLevel;

	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	private Province selectedProvince;
	private City selectedcCity;
	private County selectedCounty;
	private ProgressDialog progressDialog;
	Boolean isFromWeatherActivity;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// 取出点击切换按钮时传入“from_weatherActivity”中的值true
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weatherActivity", false);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		
//		已经存在了天气文件，且从不是点击按钮“切换”跳转过来时，才跳转至WeatherActivity
		if ((preferences.getBoolean("city_selected", false))
				&& !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		// 隐藏标题栏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

		// 初始化
		listview = (ListView) findViewById(R.id.listview);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listview.setAdapter(adapter);

		// 调用getInstance()方法得到数据库对象
		firstWeatherDB = FirstWeatherDB.getInstance(this);

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {

					// 根据点击listView的位置position得到province对象，然后赋值给selectedprovince
					selectedProvince = provinceList.get(position);

					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedcCity = cityList.get(position);
					queryCounties();
				}
				// 在县级界面点击listView时，得到被点击的county的code
				// 往intent中放入countyCode，并跳转到另一activity，关闭当前activity
				else if (currentLevel == LEVEL_COUNTY) {
					String countyCode = countyList.get(position)
							.getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this,
							WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}

	private void queryProvinces() {
		// 遍历firstWeatherDB中所有的province对象，并取出
		provinceList = firstWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province p : provinceList) {

				// 将provinceList中的数据存到dataList中
				dataList.add(p.getProvinceName());
			}
			// 如果适配器内容改变时，通过getView来刷新每个Item内容
			// 设置listview显示在第0个位置，也就是第一行
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			// 如果数据库province表中数据为空时，调用该方法从服务器查询数据
			queryFromServer(null, "province");
		}
	}

	private void queryCities() {
		cityList = firstWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size() > 0) {
			dataList.clear();
			for (City c : cityList) {
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			Log.d("TAG", selectedProvince.getProvinceCode());
			Log.d("TAG", selectedProvince.getProvinceName());
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}

	private void queryCounties() {
		countyList = firstWeatherDB.loadCounties(selectedcCity.getId());
		if (countyList.size() > 0) {
			dataList.clear();
			for (County c : countyList) {
				dataList.add(c.getCountyName());
			}
			adapter.notifyDataSetChanged();// 如果适配器内容改变时，通过getView来刷新每个Item内容
			listview.setSelection(0); // 将listView显示在第一行
			titleText.setText(selectedcCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedcCity.getCityCode(), "county");
		}
	}

	private void queryFromServer(String code, final String type) {
		String address;

		// 根据传入的code来设置发送的url，从而判断获取什么类型的数据
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();

		// 传入address，发送http请求，参数中传入回调接口的实现类（匿名内部类）包含被回调的方法
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			// 从服务器获取数据成功时,在sendHttpRequest方法中回调该方法
			public void onFinish(String response) {
				boolean result = false;

				// 根据传入的type类型，将服务器返回的文件解析出来
				if ("province".equals(type)) {

					// type=province时，解析文件并保存至province表中，并返回boolean型值result
					result = Utility.handleProvincesResponse(firstWeatherDB,
							response);
				} else if ("city".equals(type)) {
					result = Utility.handleCitiesResponse(firstWeatherDB,
							response, selectedProvince.getId());
				} else if ("county".equals(type)) {
					result = Utility.handleCountiesResponse(firstWeatherDB,
							response, selectedcCity.getId());
				}
				if (result) {
					// 通过该方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						public void run() {
							Log.d("TAG", "获取数据成功");
							closeProgressDialog();
							Log.d("TAG", "关闭成功");
							if ("province".equals(type)) {
								queryProvinces();
							} else if ("city".equals(type)) {
								queryCities();
							} else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}

			// 从服务器获取数据失败时,在sendHttpRequest方法中回调该方法
			public void onError(Exception e) {

				runOnUiThread(new Runnable() {
					public void run() {
						Log.d("TAG", "获取数据shib");
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败",
								Toast.LENGTH_SHORT).show();
					}
				});

			}
		});
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("加载中，请稍后。。。");
			// 设置点击其他位置是否能取消progressDialog，false不能取消
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	// 设置按下返回键时，执行相应的操作，在县级列表时返回市级列表，在省级列表时退出activity
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();

		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();

		} else {
//			如果当前activity时从weatherACtivity跳转过来，点击返回时，返回到weatherACtivity
			if (isFromWeatherActivity) {
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}
