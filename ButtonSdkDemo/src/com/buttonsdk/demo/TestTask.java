package com.buttonsdk.demo;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;

import com.medica.buttonsdk.bluetooth.ButtonHelper;
import com.medica.buttonsdk.bluetooth.SleepData;
import com.medica.buttonsdk.domain.BleDevice;
import com.medica.buttonsdk.domain.SleepStatus;
import com.medica.buttonsdk.interfs.Method;
import com.medica.buttonsdk.interfs.ResultCallback;

public class TestTask {
	private String TAG = "test_task";
	private MainActivity activity;
	private ButtonHelper btnHelper;
	private BleDevice selectedBleDevice;
	private int userId = 13447;
	private int index = 0;
	
	public static Map<String,int[]> data = new HashMap<String,int[]>();
	
	
	public TestTask(MainActivity activity,ButtonHelper btnHelper){
		this.activity = activity;
		this.btnHelper = btnHelper;
	}
	public void task(){
		selectedBleDevice = new BleDevice();
		selectedBleDevice.deviceId = "wt2wy8aqaabvq";
		selectedBleDevice.deviceName = "B502T17700027";
		selectedBleDevice.address = "E3:DC:E6:C5:2A:02";
		btnHelper.connDevice(selectedBleDevice, resultCallback);
		
	}
	private final ResultCallback resultCallback = new ResultCallback() {
		@Override
		public void onResult(final Method method, final Object result) {
			// TODO Auto-generated method stub
			activity.runOnUiThread(new Runnable() {
				public void run() {
					LogUtil.log(TAG+" onResult m:"+ method+",result:"+ result);
					
					if(method == Method.CONNECT_DEVICE){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(activity, "连接成功", Toast.LENGTH_SHORT).show();
							btnHelper.loginDevice(userId, resultCallback);
						}else{
							Toast.makeText(activity, "连接失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_ID){
						String deviceId = (String) result;
						if(deviceId == null){
							Toast.makeText(activity, "获取失败", Toast.LENGTH_SHORT).show();
						}else{
							selectedBleDevice.deviceId = deviceId;
						}
					}
					
					else if(method == Method.LOGIN){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(activity, "登录成功", Toast.LENGTH_SHORT).show();
							btnHelper.querySleepData(1502726400, 1503283158, resultCallback);
						}else{
							Toast.makeText(activity, "登录失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_STATUS){
						Integer res = (Integer) result;
						if(res == -1){
							Toast.makeText(activity, "获取设备状态失败", Toast.LENGTH_SHORT).show();
						}else if(res == 0){
							Toast.makeText(activity, "设备状态：非监测状态", Toast.LENGTH_SHORT).show();
						}else if(res == 1){
							Toast.makeText(activity, "设备状态：监测状态", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.QUERY_SLEEP_STATUS){
						SleepStatus status = (SleepStatus) result;
						Toast.makeText(activity, "入睡标识:" + status.sleepFlag+",清醒标识:"+ status.wakeFlag, Toast.LENGTH_SHORT).show();
					}
					
					else if(method == Method.GET_DEVICE_POWER){//电量低于10%时，会有低电量警告
						Integer res = (Integer) result;
						if(res == -1){
							Toast.makeText(activity, "获取电量失败", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(activity, "可用电量:" + res + "%", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.STOP_COLLECT){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(activity, "设备停止采集", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(activity, "操作失败", Toast.LENGTH_SHORT).show();
						}
					}
					else if(method == Method.QUERY_HISTORY_SUMMARY){
						
					}
					
					else if(method == Method.QUERY_HISTORY_DETAIL){
						
					}
					
					else if(method == Method.SET_AUTO_START){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(activity, "设置成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(activity, "设置失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.SET_ALARM_TIME){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(activity, "设置成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(activity, "设置失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.GET_DEVICE_VERSION){
						if(result!=null){
							String ver = result.toString();
							Toast.makeText(activity, "当前版本：" + ver, Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(activity, "获取版本失败", Toast.LENGTH_SHORT).show();
						}
					}
					
					else if(method == Method.SYNC_TIME){
						Boolean res = (Boolean) result;
						if(res){
							Toast.makeText(activity, "对时成功", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(activity, "对时失败", Toast.LENGTH_SHORT).show();
						}
					} else if(method == Method.QUERY_DATA){
						List<SleepData> datas = (List<SleepData>)result;
						index ++;
						if(datas!=null&&datas.size()>0){
							for(SleepData data:datas){
								LogUtil.log(TAG+"分数:"+new Date(data.getSummary().startTime*1000l).toLocaleString()+" "+data.getAnalys().getSleepscore());
								if(index==1){
									TestTask.data.put(data.getSummary().startTime+"_f1", data.getDetail().feature1);
									TestTask.data.put(data.getSummary().startTime+"_f2", data.getDetail().feature2);
									TestTask.data.put(data.getSummary().startTime+"_sf", data.getDetail().statusFlag);
									LogUtil.logTemp("f1="+Arrays.toString(data.getDetail().feature1));
									LogUtil.logTemp("f2="+Arrays.toString(data.getDetail().feature2));
									LogUtil.logTemp("sf="+Arrays.toString(data.getDetail().statusFlag));
								}
								if((!Arrays.equals(TestTask.data.get(data.getSummary().startTime+"_f1"), data.getDetail().feature1))||(!Arrays.equals(TestTask.data.get(data.getSummary().startTime+"_f2"), data.getDetail().feature2))||(!Arrays.equals(TestTask.data.get(data.getSummary().startTime+"_sf"), data.getDetail().statusFlag))){
									LogUtil.logTemp("e_f1="+Arrays.toString(data.getDetail().feature1));
									LogUtil.logTemp("e_f2="+Arrays.toString(data.getDetail().feature2));
									LogUtil.logTemp("e_sf="+Arrays.toString(data.getDetail().statusFlag));
								} else{
									LogUtil.logTemp("true");
								}
								

							}
							LogUtil.log(TAG+"--------");
							if(index>100)
								return;
							btnHelper.connDevice(selectedBleDevice, resultCallback);
						} else{
							LogUtil.log(TAG+"data is empty");
						}
					}
				}
			});
		}
	};

}
