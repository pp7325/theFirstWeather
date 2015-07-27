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

		// ȡ������л���ťʱ���롰from_weatherActivity���е�ֵtrue
		isFromWeatherActivity = getIntent().getBooleanExtra(
				"from_weatherActivity", false);
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		
//		�Ѿ������������ļ����ҴӲ��ǵ����ť���л�����ת����ʱ������ת��WeatherActivity
		if ((preferences.getBoolean("city_selected", false))
				&& !isFromWeatherActivity) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		// ���ر�����
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);

		// ��ʼ��
		listview = (ListView) findViewById(R.id.listview);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, dataList);
		listview.setAdapter(adapter);

		// ����getInstance()�����õ����ݿ����
		firstWeatherDB = FirstWeatherDB.getInstance(this);

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE) {

					// ���ݵ��listView��λ��position�õ�province����Ȼ��ֵ��selectedprovince
					selectedProvince = provinceList.get(position);

					queryCities();
				} else if (currentLevel == LEVEL_CITY) {
					selectedcCity = cityList.get(position);
					queryCounties();
				}
				// ���ؼ�������listViewʱ���õ��������county��code
				// ��intent�з���countyCode������ת����һactivity���رյ�ǰactivity
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
		// ����firstWeatherDB�����е�province���󣬲�ȡ��
		provinceList = firstWeatherDB.loadProvinces();
		if (provinceList.size() > 0) {
			dataList.clear();
			for (Province p : provinceList) {

				// ��provinceList�е����ݴ浽dataList��
				dataList.add(p.getProvinceName());
			}
			// ������������ݸı�ʱ��ͨ��getView��ˢ��ÿ��Item����
			// ����listview��ʾ�ڵ�0��λ�ã�Ҳ���ǵ�һ��
			adapter.notifyDataSetChanged();
			listview.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		} else {
			// ������ݿ�province��������Ϊ��ʱ�����ø÷����ӷ�������ѯ����
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
			adapter.notifyDataSetChanged();// ������������ݸı�ʱ��ͨ��getView��ˢ��ÿ��Item����
			listview.setSelection(0); // ��listView��ʾ�ڵ�һ��
			titleText.setText(selectedcCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		} else {
			queryFromServer(selectedcCity.getCityCode(), "county");
		}
	}

	private void queryFromServer(String code, final String type) {
		String address;

		// ���ݴ����code�����÷��͵�url���Ӷ��жϻ�ȡʲô���͵�����
		if (!TextUtils.isEmpty(code)) {
			address = "http://www.weather.com.cn/data/list3/city" + code
					+ ".xml";
		} else {
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();

		// ����address������http���󣬲����д���ص��ӿڵ�ʵ���ࣨ�����ڲ��ࣩ�������ص��ķ���
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			// �ӷ�������ȡ���ݳɹ�ʱ,��sendHttpRequest�����лص��÷���
			public void onFinish(String response) {
				boolean result = false;

				// ���ݴ����type���ͣ������������ص��ļ���������
				if ("province".equals(type)) {

					// type=provinceʱ�������ļ���������province���У�������boolean��ֵresult
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
					// ͨ���÷����ص����̴߳����߼�
					runOnUiThread(new Runnable() {
						public void run() {
							Log.d("TAG", "��ȡ���ݳɹ�");
							closeProgressDialog();
							Log.d("TAG", "�رճɹ�");
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

			// �ӷ�������ȡ����ʧ��ʱ,��sendHttpRequest�����лص��÷���
			public void onError(Exception e) {

				runOnUiThread(new Runnable() {
					public void run() {
						Log.d("TAG", "��ȡ����shib");
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��",
								Toast.LENGTH_SHORT).show();
					}
				});

			}
		});
	}

	private void showProgressDialog() {
		if (progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("�����У����Ժ󡣡���");
			// ���õ������λ���Ƿ���ȡ��progressDialog��false����ȡ��
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}

	private void closeProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

	// ���ð��·��ؼ�ʱ��ִ����Ӧ�Ĳ��������ؼ��б�ʱ�����м��б���ʡ���б�ʱ�˳�activity
	public void onBackPressed() {
		if (currentLevel == LEVEL_COUNTY) {
			queryCities();

		} else if (currentLevel == LEVEL_CITY) {
			queryProvinces();

		} else {
//			�����ǰactivityʱ��weatherACtivity��ת�������������ʱ�����ص�weatherACtivity
			if (isFromWeatherActivity) {
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}

}
