package org.g_okuyama.applock;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class AppWatchService extends Service {
	static ArrayList<String> mLockList;

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("AppWatchService", "service start");
		Bundle bundle = intent.getExtras();
		ArrayList<String> list = bundle.getStringArrayList("LockList");
		if(list != null){
			mLockList = list;
		}

		startAlarmManager(AlarmManager.RTC, System.currentTimeMillis() + 1000);
		ActivityManager am = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(5);
		
		String top = taskInfo.get(0).topActivity.getPackageName();
		Log.d("AppWatchService", "top app = " + top);

		for(String name: mLockList){
			Log.d("AppWatchService", "name = " + name);

			if(name.equals(top)) {
				Log.d("AppWatchService", "Lock App Launch!!");
			}					
		}
	}

	private void startAlarmManager(int type, long autoChkTime) {
		Intent intent = new Intent(this, AppWatchService.class);
		PendingIntent pending = PendingIntent.getService(
									this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(type, autoChkTime, pending);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
