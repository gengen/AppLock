package org.g_okuyama.applock;

import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	//ロック対象が設定されているか？
		SharedPreferences prefs = context.getSharedPreferences(AppLockActivity.PREF_LOCK, Context.MODE_PRIVATE);
		Map map = prefs.getAll();
		if(map.size() == 0){
			return;
		}
		
		//ロック解除中でないか？
		SharedPreferences modePref = context.getSharedPreferences(AppLockActivity.PREF_MODE, Context.MODE_PRIVATE);
		boolean isUnlock = modePref.getBoolean(AppLockActivity.MODE_UNLOCK, false);
		if(isUnlock){
			return;
		}

		//上記以外は監視用サービス起動
		Intent i = new Intent(context, AppWatchService.class);
		context.startService(i);
    }
}
