package org.g_okuyama.applock;

import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    public static final String EXTRA_BOOL_AUTOSTART = "auto_start";
	
    @Override
    public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = context.getSharedPreferences(AppLockActivity.PREF_LOCK, Context.MODE_PRIVATE);
		Map map = prefs.getAll();
		
		if(map.size() > 0){
			Intent i = new Intent(context, AppWatchService.class);
			context.startService(i);
		}
    }
}
