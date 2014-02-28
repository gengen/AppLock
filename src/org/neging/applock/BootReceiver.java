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
    	//ロック対象が設定されているか？
		SharedPreferences prefs = context.getSharedPreferences(AppLockActivity.PREF_LOCK, Context.MODE_PRIVATE);
		Map map = prefs.getAll();
		if(map.size() == 0){
			return;
		}
		
		//電源ON時およびスリープからの復帰時(スライドロックなど何かしらの端末ロックが外されたとき)は必ずロックする
		SharedPreferences modePref = context.getSharedPreferences(AppLockActivity.PREF_MODE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = modePref.edit();
		editor.putBoolean(AppLockActivity.MODE_UNLOCK, false);
		editor.commit();

		//上記以外は監視用サービス起動
		Intent i = new Intent(context, AppWatchService.class);
		context.startService(i);
    }
}
