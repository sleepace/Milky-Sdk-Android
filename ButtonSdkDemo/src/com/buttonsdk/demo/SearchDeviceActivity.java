package com.buttonsdk.demo;

import java.util.ArrayList;

import com.medica.buttonsdk.bluetooth.ButtonHelper;
import com.medica.buttonsdk.domain.BleDevice;
import com.medica.buttonsdk.interfs.BleScanListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SearchDeviceActivity extends Activity {
//	private static final String TAG = SearchDeviceActivity.class.getSimpleName();
	private ListView listView;
	private ButtonHelper btnHelper;
	private DeviceAdapter adapter;
	private LayoutInflater inflater;
	
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		
		inflater = getLayoutInflater();
		btnHelper = ButtonHelper.getInstance(this);
		adapter = new DeviceAdapter();
		
		listView = (ListView) findViewById(R.id.list_devices);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this.mDeviceClickListener);
		
		btnHelper.scanBleDevice(bleScanListener);
	}
	

	protected void onDestroy() {
		super.onDestroy();
//		LogUtil.showMsg(TAG+" onDestroy-----------");
	}
	
	private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> adapter,View view, int position, long id) {
			btnHelper.stopScan();
			BleDevice device = SearchDeviceActivity.this.adapter.getItem(position);
			Intent data = new Intent();
			data.putExtra("device", device);
			setResult(RESULT_OK, data);
			finish();
		}
	};



	private BleScanListener bleScanListener = new BleScanListener(){
		
		@Override
		public void onBleScanStart() {
			// TODO Auto-generated method stub
			setTitle("正在扫描新设备");
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		public void onBleScan(final BleDevice device) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					adapter.addItem(device);
				}
			});
		}

		@Override
		public void onBleScanFinish() {
			// TODO Auto-generated method stub
//			LogUtil.showMsg("onBleScanFinish-----------");
			setProgressBarIndeterminateVisibility(false);
			if(adapter.getCount() > 0){
				SearchDeviceActivity.this.setTitle("请选择要连接的设备");
			}else{
				SearchDeviceActivity.this.setTitle("没有扫描到设备");
			}
		}
	};
	
	private class DeviceAdapter extends BaseAdapter{

		private ArrayList<BleDevice> data = new ArrayList<BleDevice>();
		
		class ViewHolder{
			TextView tvName;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public BleDevice getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.device_name, null);
				holder.tvName = (TextView) convertView;
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			BleDevice device = getItem(position);
			holder.tvName.setText(device.deviceName + "\n" + device.address);
			return convertView;
		}
		
		public void addItem (BleDevice device){
			boolean isExist = false;
			for(BleDevice d : data){
				if(d.address.equals(device.address)){
					isExist = true;
					break;
				}
			}
			if(!isExist){
				data.add(device);
				notifyDataSetChanged();
			}
		}
	}
	
}


























