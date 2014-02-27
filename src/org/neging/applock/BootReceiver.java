package org.neging.applock;

import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(AppLockActivity.TAG, "start service");
    	//ロック対象が設定されているか？
		SharedPreferences prefs = context.getSharedPreferences(AppLockActivity.PREF_LOCK, Context.MODE_PRIVATE);
		Map map = prefs.getAll();
		if(map.size() == 0){
			return;
		}
		
		//電源ON時は必ずロックする
		/*
		//ロック解除中でないか？
		SharedPreferences modePref = context.getSharedPreferences(AppLockActivity.PREF_MODE, Context.MODE_PRIVATE);
		boolean isUnlock = modePref.getBoolean(AppLockActivity.MODE_UNLOCK, false);
		if(isUnlock){
			return;
		}
		*/
		SharedPreferences modePref = context.getSharedPreferences(AppLockActivity.PREF_MODE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = modePref.edit();
		editor.putBoolean(AppLockActivity.MODE_UNLOCK, false);
		editor.commit();

		//上記以外は監視用サービス起動
		Intent i = new Intent(context, AppWatchService.class);
		context.startService(i);
    }
}
