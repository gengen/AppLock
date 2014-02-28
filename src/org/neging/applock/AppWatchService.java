package org.neging.applock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AppWatchService extends Service {
	static ArrayList<String> mLockList;
	boolean mInit = true;

	@Override
	public void onCreate(){
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if(intent != null){
			Bundle extras = intent.getExtras();
			if(extras != null){
				boolean isPending = extras.getBoolean(AppLockActivity.SERVICE_PENDING);
				if(isPending){
					stopAlarmManager();
					return;
				}
			}
		}
		
		if(mInit){
			displayNotificationArea();

			mLockList = new ArrayList<String>();
			SharedPreferences prefs = getSharedPreferences(AppLockActivity.PREF_LOCK, Context.MODE_PRIVATE);
			Map map = prefs.getAll();
			int num = map.size();
			//Log.d(AppLockActivity.TAG, "num = " + num);
			for(int i=1; i<=num; i++){
				String name = (String)map.get(""+i);
				mLockList.add(name);
				//Log.d(AppLockActivity.TAG, "name = " + name);
			}
		}
		mInit = false;
		
		startAlarmManager(AlarmManager.RTC, System.currentTimeMillis() + 1000);
		ActivityManager am = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
		//直近5つ取得
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(5);
		
		String top = taskInfo.get(0).topActivity.getPackageName();
		//Log.d(AppLockActivity.TAG, "top app = " + top);

		for(String name: mLockList){
			//Log.d(AppLockActivity.TAG, "name = " + name);

			if(name.equals(top)) {
				//Log.d(AppLockActivity.TAG, "Lock App Launch!!");
				//ホームアプリ起動
				Intent i = new Intent(); 
				i.setAction(Intent.ACTION_MAIN); 
				i.addCategory(Intent.CATEGORY_HOME); 
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i); 
			}					
		}
	}
	
	// デフォルトホーム名取得
	public String getDefaultHomeName(Context context)
	{
		PackageManager packagemanager = context.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		ResolveInfo resolveInfo = packagemanager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		ActivityInfo activityInfo = resolveInfo.activityInfo;
		/*
		Log.d(AppLockActivity.TAG, "package name:"+activityInfo.packageName);
		Log.d(AppLockActivity.TAG, "package name:"+activityInfo.name);
		Log.d(AppLockActivity.TAG, "package name:"+activityInfo.loadLabel(packagemanager).toString());
		*/
		return activityInfo.name;
	}
	
	/*
	public void getHomeApp(){
        PackageManager pm = this.getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        //呼び出したいActivityのカテゴリを指定する
        intent.addCategory(Intent.CATEGORY_HOME);
        //カテゴリとアクションに一致するアクティビティの情報を取得する
        List<ResolveInfo> appInfoList = pm.queryIntentActivities(intent, 0);
        
        for(ResolveInfo ri : appInfoList){
            if(ri.loadLabel(pm).toString()!=null){
            	Log.d(TAG, "home = " + ri.loadLabel(pm).toString());
            }
        }
	}
	*/

	private void startAlarmManager(int type, long autoChkTime) {
		Intent intent = new Intent(this, AppWatchService.class);
		PendingIntent pending = PendingIntent.getService(
									this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.set(type, autoChkTime, pending);
	}
	
	private void stopAlarmManager(){
		Intent intent = new Intent(this, AppWatchService.class);
		PendingIntent pending = PendingIntent.getService(
									this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(pending);
	}
	
    private void displayNotificationArea(){
        Intent intent = new Intent(getApplicationContext(), AppLockActivity.class);
        intent.putExtra("FromNotification", true);
        PendingIntent contentIntent = PendingIntent.getActivity(
        		getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext());
        builder.setContentIntent(contentIntent);
        builder.setTicker(getString(R.string.app_name));
        builder.setSmallIcon(R.drawable.ic_lock_lock);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.notification_message));
        builder.setOngoing(true);
        
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(R.string.app_name, builder.build());
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
        //AlarmManager解除
		stopAlarmManager();

		//Notificationを非表示
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(R.string.app_name);   
	}
}
