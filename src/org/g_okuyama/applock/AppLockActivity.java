package org.g_okuyama.applock;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AppLockActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_lock);

		//TODO　ロックリストの取得(sharedpreferenceなどから)
		
		displayAppList();
		setListener();
	}
	
	private void displayAppList(){
		ArrayList<AppData> appDataList = new ArrayList<AppData>();
		
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> applist = pm.getInstalledApplications(BIND_AUTO_CREATE);
		
		for(ApplicationInfo item: applist){
			AppData data = new AppData();
			
			//アプリ名の設定
			if(item.loadLabel(pm).toString() != null){
				data.setAppName(item.loadLabel(pm).toString());
			}
			else{
				data.setAppName("No Name");				
			}
			
			//パッケージ名の設定
			data.setPackageName(item.packageName);

			//TODO パッケージ名とロックリストと比較し、ロック対象ならロックフラグを設定する
			
			//アイコンの設定
			Drawable icon = null;
			try {
				icon = pm.getApplicationIcon(item.packageName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				//TODO デフォルトアイコンの設定
			}
			data.setDrawable(icon);
			
			appDataList.add(data);
		}
		
        AppArrayAdapter adapter = new AppArrayAdapter(this, android.R.layout.simple_list_item_1, appDataList);
        ListView listview = (ListView)findViewById(R.id.app_lock_list);
        listview.setAdapter(adapter);
        listview.setScrollingCacheEnabled(false); 
        listview.setOnItemClickListener(new OnItemClickListener(){
    		@Override
    		public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {

    		}
        });
	}
	
	private void setListener(){
		Button btn = (Button)findViewById(R.id.app_lock_ok);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//TODO ロック保存
				//String[] lockList = new String[]{};
				ArrayList<String> lockList = new ArrayList<String>();
		        ListView view = (ListView)findViewById(R.id.app_lock_list);
		        AppArrayAdapter adapter = (AppArrayAdapter)view.getAdapter();
		        int length = adapter.getCount();
		        for(int i=0; i<length; i++){
		        	AppData item = adapter.getItem(i);
		        	if(item.getLockFlag()){
		        		//lockList[i] = item.getPackageName();
		        		lockList.add(item.getPackageName());
		        	}
		        }
		        
		        Intent intent = new Intent(AppLockActivity.this, AppWatchService.class);
		        intent.putExtra("LockList", lockList);
		        startService(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_lock, menu);
		return true;
	}

}
