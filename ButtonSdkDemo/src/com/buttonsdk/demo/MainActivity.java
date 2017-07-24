package com.buttonsdk.demo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.medica.buttonsdk.bluetooth.ButtonHelper;
import com.medica.buttonsdk.bluetooth.SleepData;
import com.medica.buttonsdk.domain.BleDevice;
import com.medica.buttonsdk.domain.Detail;
import com.medica.buttonsdk.domain.SleepStatus;
import com.medica.buttonsdk.domain.Summary;
import com.medica.buttonsdk.interfs.BleStateChangedListener;
import com.medica.buttonsdk.interfs.Method;
import com.medica.buttonsdk.interfs.ResultCallback;
import com.medica.buttonsdk.interfs.SleepStatusListener;
import com.medica.buttonsdk.interfs.UpgradeCallback;
import com.medica.milky.jni.MilkyAlgorithmUtil;
import com.medica.xiangshui.jni.sleepdot.SleepDotAlgorithmOut;

public class MainActivity extends Activity{
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private static final String[] ITEMS = { "搜索设备", "连接设备", "获取设备ID", "登录设备", "监测状态", "睡眠状态", "获取电量",
		"停止采集", "查询概要信息", "查询详细信息", "睡眠分析", "设置自动监测", "设置智能闹钟", "当前版本", "固件升级", "断开连接", "同步时间","一键取数据"};
	
	private GridView gridView;
	private GridAdapter adapter;
	
	private ButtonHelper btnHelper;
	private BleDevice selectedBleDevice;
	
	private static final int REQCODE_OPEN_BT = 1;
	private static final int REQCODE_SEACH_DEVICE = 2;
	
	public static final int userId = 25209;
	
	private Summary summary;
	private Detail detail;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnHelper = ButtonHelper.getInstance(this);
		btnHelper.addBleStateChangedListener(bleStateChangedListener);
		btnHelper.addSleepStatusListener(statusListener);
		
		IntentFilter filter = new IntentFilter(ButtonHelper.ACTION_LOW_POWER);
		registerReceiver(lowPowerReceiver, filter);
		 
		gridView = (GridView) findViewById(R.id.gridview);
		adapter = new GridAdapter();
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(itemClickListener);
	}
	
	
	private final OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
//			LogUtil.showMsg(TAG+" onItemClick pos:"+position);
			if(!btnHelper.isBluetoothOpen()){
				//注意：打开和关闭蓝牙的过程，是异步的
				Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enabler,REQCODE_OPEN_BT);
				return;
			}
			
			if(position == 0){//搜索设备
				Intent intent = new Intent(MainActivity.this, SearchDeviceActivity.class);
				startActivityForResult(intent, REQCODE_SEACH_DEVICE);
			}else{
				
				/*if(selectedBleDevice == null){
					Toast.makeText(MainActivity.this, "请先选择设备", Toast.LENGTH_SHORT).show();
					return;
				}*/
				
				if(position == 1){//连接设备
					btnHelper.connDevice(selectedBleDevice, resultCallback);
				}else if(position == 2){//获取设备id
					btnHelper.getDeviceId(resultCallback);
				}else if(position == 3){//登录设备
					btnHelper.loginDevice(userId, resultCallback);
				}else if(position == 4){//监测状态
					btnHelper.getDeviceStatus(resultCallback);
				}else if(position == 5){//睡眠状态
					btnHelper.querySleepStatus(resultCallback);
				}else if(position == 6){//获取电量
					btnHelper.getDevicePower(resultCallback);
				}else if(position == 7){//停止采集
					btnHelper.stopCollect(resultCallback);
				}else if(position == 8){//查询概要信息
					Calendar calendar = Calendar.getInstance();
					int endTime = (int) (calendar.getTimeInMillis() / 1000);
					calendar.add(Calendar.DAY_OF_MONTH, -100);
					int startTime = (int) (calendar.getTimeInMillis() / 1000);
					LogUtil.log(TAG+" queryHistorySummary stime:" + dateFormat.format(new Date(startTime * 1000l))+
							",etime:"+ dateFormat.format(new Date(endTime * 1000l)));
					btnHelper.queryHistorySummary(startTime, endTime, resultCallback);
				}else if(position == 9){//查询详细信息
					btnHelper.queryHistoryDetail(summary, resultCallback);
				}else if(position == 10){//睡眠分析
					if(summary==null||detail==null){
						summary = new Summary();
						detail = new Detail();
					}
					
					if(summary != null && detail != null){
						SleepDotAlgorithmOut analysis = MilkyAlgorithmUtil.analysis(summary.startTime, detail);
						int score = analysis == null ? -1 : analysis.getSleepscore();
						LogUtil.log(TAG+" analysis score:" + score);
					}
				}else if(position == 11){//自动开始监测
					Calendar calendar = Calendar.getInstance();
					byte startHour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
					byte startMinute = (byte) calendar.get(Calendar.MINUTE);
					byte endHour = startHour;
					byte endMinute = (byte) (startMinute + 10);
					boolean[] repeat = new boolean[7];
					for(int i=0;i<repeat.length;i++){
						repeat[i] = true;
					}
					LogUtil.log(TAG+" setAutoStart sh:" + startHour+",sm:" + startMinute+",eh:" + endHour+",em:" + endMinute+",repeat:"+ Arrays.toString(repeat));
					btnHelper.setAutoStart(startHour, startMinute, endHour, endMinute, repeat, resultCallback);
				}else if(position == 12){//设置智能闹钟
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.MINUTE, 30);
					
					byte alartOFF = 1;
					byte autoGet = 1;
					byte autoMove = 30;
					byte hourOfDay = (byte) calendar.get(Calendar.HOUR_OF_DAY);
					byte minute = (byte) (calendar.get(Calendar.MINUTE));
					byte repeat = 0;
					LogUtil.log(TAG+" setAlarmTime h:" + hourOfDay+",m:" + minute);
					btnHelper.setAlarmTime(alartOFF, autoGet, autoMove, hourOfDay, minute, repeat, resultCallback);
				}else if(position == 13){//当前版本
					btnHelper.getDeviceVersion(resultCallback);
				}else if(position == 14){//固件升级
					File file = new File("/storage/emulated/0/a.des");
                     
					btnHelper.upgradeFirmwareByThread(1.38f, (int)2763936600l, (int)1113535353, file, upgradeCallback);
				}else if(position == 15){//断开设备
					btnHelper.disconnect();
				}else if(position == 16){//同步时间
					btnHelper.syncTimeByThread(resultCallback);
				} else if(position == 17){//一键取数据:整合取概要、详细和算法分析接口
					Calendar calendar = Calendar.getInstance();
					int endTime = (int) (calendar.getTimeInMillis() / 1000);
					calendar.add(Calendar.DAY_OF_MONTH, -100);
					int startTime = (int) (calendar.getTimeInMillis() / 1000);
					LogUtil.log(TAG+" queryHistorySummary stime:" + dateFormat.format(new Date(startTime * 1000l))+
							",etime:"+ dateFormat.format(new Date(endTime * 1000l)));
					btnHelper.querySleepData(startTime, endTime, resultCallback);
				}
			}
		}
	};
	
	
	private final ResultCallback resultCallback = new ResultCallback() {
		@Override
		public void onResult(final Method method, final Object result) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				public void run() {
					LogUtil.log(TAG+" onResult m:"+ method+",result:"+ result);
					
					if(method == Method.CONNECT_DEVICE){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_ID){
						String deviceId = (String) result;
						if(deviceId == null){
							Toast.makeText(MainActivity.this, "获取失败", Toast.LENGTH_SHORT).show();
						}else{
							selectedBleDevice.deviceId = deviceId;
						}
					}
					
					else if(method == Method.LOGIN){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_STATUS){
						Integer res = (Integer) result;
						if(res == -1){
							Toast.makeText(MainActivity.this, "获取设备状态失败", Toast.LENGTH_SHORT).show();
						}else if(res == 0){
							Toast.makeText(MainActivity.this, "设备状态：非监测状态", Toast.LENGTH_SHORT).show();
						}else if(res == 1){
							Toast.makeText(MainActivity.this, "设备状态：监测状态", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.QUERY_SLEEP_STATUS){
						SleepStatus status = (SleepStatus) result;
						Toast.makeText(MainActivity.this, "入睡标识:" + status.sleepFlag+",清醒标识:"+ status.wakeFlag, Toast.LENGTH_SHORT).show();
					}
					
					else if(method == Method.GET_DEVICE_POWER){//电量低于10%时，会有低电量警告
						Integer res = (Integer) result;
						if(res == -1){
							Toast.makeText(MainActivity.this, "获取电量失败", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "可用电量:" + res + "%", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.STOP_COLLECT){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, "设备停止采集", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.QUERY_HISTORY_SUMMARY){
						if(result != null){
							ArrayList<Summary> list = (ArrayList<Summary>) result;
							int size = list.size();
							LogUtil.log(TAG+" query summary size:" + size);
							for(Summary s:list){
								Log.i("=====summary:", s.startTime+"");
								
							}
							if(size > 0){
								summary = list.get(0);
							}
						}
					}
					
					else if(method == Method.QUERY_HISTORY_DETAIL){
						if(result != null){
							detail = (Detail) result;
							LogUtil.log(TAG+" query detail:" + detail.statusFlag.length);
						}
					}
					
					else if(method == Method.SET_AUTO_START){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.SET_ALARM_TIME){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_VERSION){
						if(result!=null){
							String ver = result.toString();
							Toast.makeText(MainActivity.this, "当前版本：" + ver, Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "获取版本失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.SYNC_TIME){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(MainActivity.this, "对时成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(MainActivity.this, "对时失败", Toast.LENGTH_SHORT).show();
						}
					} else if(method == Method.QUERY_DATA){
						List<SleepData> datas = (List<SleepData>)result;
						if(datas!=null&&datas.size()>0){
							LogUtil.log(TAG+"分数:"+datas.get(0).getAnalys().getSleepscore());
						} else{

						}
					}
				}
			});
		}
	};
	
	
	private UpgradeCallback upgradeCallback = new UpgradeCallback() {
		
		@Override
		public void onResult(Method arg0, Object arg1) {
			// TODO Auto-generated method stub
			LogUtil.log(TAG+" onUpgrade m:" + arg0+",res:" + arg1);
		}
		
		@Override
		public void onUpgrade(int arg0) {
			// TODO Auto-generated method stub
			LogUtil.log(TAG+" onUpgrade progress:" + arg0);
		}
	};
	
	private BleStateChangedListener bleStateChangedListener = new BleStateChangedListener() {
		@Override
		public void onStateChanged(final int state) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				public void run() {
					LogUtil.log(TAG+" onStateChanged state:"+ state);
					switch(state){
					case ButtonHelper.STATE_CONNECTING:{
//						Toast.makeText(MainActivity.this, "设备连接中···", Toast.LENGTH_SHORT).show();
						break;
					}
					case ButtonHelper.STATE_CONNECTED:{
//						Toast.makeText(MainActivity.this, "设备连接成功", Toast.LENGTH_SHORT).show();
						break;
					}
					case ButtonHelper.STATE_DISCONNECTED:{
//						Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_SHORT).show();
						break;
					}
					}
				}
			});
		}
	};
	
	
	private SleepStatusListener statusListener = new SleepStatusListener() {
		@Override
		public void onSleepStatusChanged(SleepStatus arg0) {
			// TODO Auto-generated method stub
			LogUtil.log(TAG+" onSleepStatusChanged sleepFlag:"+arg0.sleepFlag+",wakeFlag:"+arg0.wakeFlag);
		}
	};
	
	private BroadcastReceiver lowPowerReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			LogUtil.log(TAG+" reston low power-----------");
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			if(requestCode == REQCODE_SEACH_DEVICE){
				selectedBleDevice = (BleDevice) data.getSerializableExtra("device");
				LogUtil.log(TAG+" onActivityResult device:"+ selectedBleDevice);
			}
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		LogUtil.showMsg(TAG+" onDestroy----------");
		unregisterReceiver(lowPowerReceiver);
		btnHelper.removeBleStateChangedListener(bleStateChangedListener);
		btnHelper.removeSleepStatusListener(statusListener);
		btnHelper.disconnect();
		super.onDestroy();
	}
	
	
	private class GridAdapter extends BaseAdapter{
		
		class ViewHolder{
			Button btn;
		}
		
		private LayoutInflater inflater;
		
		GridAdapter(){
			inflater = getLayoutInflater();
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ITEMS.length;
		}

		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return ITEMS[position];
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
				convertView = inflater.inflate(R.layout.grid_item, null);
				holder = new ViewHolder();
				holder.btn = (Button) convertView.findViewById(R.id.btn);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			String item = getItem(position);
			holder.btn.setText(item);
			return convertView;
		}
	}
}








































