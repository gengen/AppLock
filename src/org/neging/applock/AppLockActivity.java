package org.neging.applock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class AppLockActivity extends Activity {
	public static final String TAG = "AppLock";
	public static final String PREF_LOCK = "LockAppList";

	public static final String PREF_PASSWORD = "Password";
	public static final String PASSWORD_FLAG = "flag";
	public static final String PASSWORD_NUMBER = "pass";

	public static final String PREF_MODE = "Mode";
	public static final String MODE_UNLOCK = "unlock";
	
	public static final String SERVICE_PENDING = "pending";	
	
	public static final int INIT_LAUNCH = 1;
	public static final int NORMAL_LAUNCH = 2;
	public static final int FROM_NOTIFICATION = 3;
	public static final int CHANGE_PASSWORD = 4;
	
    ProgressDialog mProgressDialog = null;
    View mView;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Notificationからの起動はロック解除画面だけ表示するため透過にする
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			boolean isNotification = extras.getBoolean("FromNotification");
			if(isNotification){
				setTheme(android.R.style.Theme_Translucent_NoTitleBar);
			}
		}
		
		setContentView(R.layout.activity_app_lock);
		
		//有効/無効化時に張り付けるためのView
        mView = new View(AppLockActivity.this);
        mView.setBackgroundColor(Color.argb(200, 211, 211, 211));
	}
	
    @Override
    protected void onPause(){
    	super.onPause();
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        
        //タイミングによってロック解除されないことがあるため、いったんサービス側のアラームマネージャを止める
    	Intent intent = new Intent(AppLockActivity.this, AppWatchService.class);
    	intent.putExtra(SERVICE_PENDING, true);
    	startService(intent);
        
		setTitle(getString(R.string.title));
		int mode = INIT_LAUNCH;
		//パスワードが設定されているか？
		SharedPreferences prefs = getSharedPreferences(AppLockActivity.PREF_PASSWORD, Context.MODE_PRIVATE);
		boolean flag = prefs.getBoolean(PASSWORD_FLAG, false);
		if(flag){
			mode = NORMAL_LAUNCH;
		}

		Bundle extras = getIntent().getExtras();
		if(extras != null){
			boolean isNotification = extras.getBoolean("FromNotification");
			if(isNotification){
				mode = FROM_NOTIFICATION;
			}
		}

		inputPassword(mode);
    }
	
	//パスワード設定
	private void inputPassword(final int mode){
		//パスワード入力されるまではレイアウトを無効化
		if(mode != CHANGE_PASSWORD){
			LinearLayout layout = (LinearLayout)findViewById(R.id.bottom_layout);
			layout.setVisibility(View.INVISIBLE);
		}

		final EditText editView = new EditText(this);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			editView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		}
		else{
			editView.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getTitle(mode));		
	    builder.setView(editView);
	    builder.setPositiveButton(R.string.dialog_password_ok, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		//パスワード確認
	    		String pass = editView.getText().toString();
	    		checkPassword(pass, mode);
	    	}
	    });
	    builder.setNegativeButton(R.string.dialog_password_cancel, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int whichButton) {
	    		if(mode != CHANGE_PASSWORD){
	    			//アプリ終了
	    			finish();
	    		}
	    	}
	    });
	    builder.setOnKeyListener(new OnKeyListener() {
	    	@Override
	    	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
	    		//バックキーと検索キーを無効化
	    		switch (keyCode) {
	    		case KeyEvent.KEYCODE_BACK:
	    		case KeyEvent.KEYCODE_SEARCH:
	    			return true;
	    		default:
	    			return false;
	    		}
	    	}
	    });

	    AlertDialog dialog = builder.show();
	    //ダイアログ画面外を押された際に閉じないように設定
	    dialog.setCanceledOnTouchOutside(false);
	}
	
	private String getTitle(int mode){
		String title = getString(R.string.dialog_password_title_normal);
		switch(mode){
		case INIT_LAUNCH:
			title = getString(R.string.dialog_password_title_init);
			break;
		case FROM_NOTIFICATION:
			title = getString(R.string.dialog_password_title_notification);
			break;
		case CHANGE_PASSWORD:
			title = getString(R.string.dialog_password_title_changepass);
			break;			
		}
		
		return title;
	}
	
	private void checkPassword(String password, int mode){
		SharedPreferences prefs = getSharedPreferences(AppLockActivity.PREF_PASSWORD, Context.MODE_PRIVATE);
		
		//初回起動時はパスワードを保存
    	if(mode == INIT_LAUNCH || mode == CHANGE_PASSWORD){
    		//パスワードが1-8文字の間でないとき
    		if((password.length() == 0) || (password.length() > 8)){
    			displayPasswordErrorDialog(mode);
    			return;
    		}
    		Editor edit = prefs.edit();
    		edit.putBoolean(PASSWORD_FLAG, true);
    		edit.putString(PASSWORD_NUMBER, password);
    		edit.commit();
        	initAppDisplay();
        	return;
    	}

    	String savePass = prefs.getString(PASSWORD_NUMBER, "");
    	if(!(password.equals(savePass))){
    		//パスワード間違い
    		displayPasswordInvalidDialog();
    		return;
    	}

		//パスワードが合っている場合
    	//Notificationから起動された場合はロック解除し終了
    	if(mode == FROM_NOTIFICATION){
    		//解除フラグをセット
    		SharedPreferences pref = getSharedPreferences(AppLockActivity.PREF_MODE, Context.MODE_PRIVATE);
    		SharedPreferences.Editor editor = pref.edit();
    		editor.putBoolean(MODE_UNLOCK, true);
    		editor.commit();
    		
    		Intent intent = new Intent(AppLockActivity.this, AppWatchService.class);
    		stopService(intent);    		
        	Toast.makeText(this, getString(R.string.toast_unlock), Toast.LENGTH_SHORT).show();

    		finish();
    		return;
    	}

    	initAppDisplay();
	}
	
	//パスワード文字数入力エラー用
	private void displayPasswordErrorDialog(final int mode){
		AlertDialog.Builder builder = new AlertDialog.Builder(AppLockActivity.this);
		builder.setTitle(R.string.dialog_error_title);
		builder.setMessage(getString(R.string.dialog_error_password_message));
		builder.setPositiveButton(R.string.dialog_error_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				inputPassword(mode);
			}
		});
		builder.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				//バックキーと検索キーを無効化
				switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
				case KeyEvent.KEYCODE_SEARCH:
					return true;
				default:
					return false;
				}
			}
		});
		AlertDialog dialog = builder.show();
		dialog.setCanceledOnTouchOutside(false);
	}
	
	private void displayPasswordInvalidDialog(){
		//パスワード間違い
		AlertDialog.Builder builder = new AlertDialog.Builder(AppLockActivity.this);
		builder.setTitle(R.string.dialog_error_title);
		builder.setMessage(getString(R.string.dialog_error_message));
		builder.setPositiveButton(R.string.dialog_error_ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				//バックキーと検索キーを無効化
				switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
				case KeyEvent.KEYCODE_SEARCH:
					return true;
				default:
					return false;
				}
			}
		});
		AlertDialog dialog = builder.show();
		dialog.setCanceledOnTouchOutside(false);
	}
	
	void initAppDisplay(){
		initProgressDialog();
    	mProgressDialog.show();
    	displayAppList();
        Thread thread = new Thread(runnable);
        thread.start();
        
		setListener();
	}
	
	//プログレスバー初期化
    void initProgressDialog(){
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.dialog_progress_message));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
    }
	
	//プログレスバー表示用
    Runnable runnable = new Runnable() {
        public void run() {
        	handler.sendMessage(new Message());
        }
    };
    
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg)
        {
            mProgressDialog.dismiss();
        };
    };
	
	private void displayAppList(){		
		ArrayList<AppData> appDataList = new ArrayList<AppData>();
		
		//ランチャーから起動できるアプリリストを取得
		PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appInfo = pm.queryIntentActivities(intent, 0);

        //ロック対象済みアプリを取得
		SharedPreferences prefs = getSharedPreferences(AppLockActivity.PREF_LOCK, Context.MODE_PRIVATE);
		Map map = prefs.getAll();

		for(ResolveInfo item: appInfo){
			String packageName = item.activityInfo.packageName;
			//自身はロック対象から外す
			if(packageName.equals(getPackageName())){
				continue;
			}
			
			AppData data = new AppData();

			//アプリ名の設定
			if(item.loadLabel(pm).toString() != null){
				data.setAppName(item.loadLabel(pm).toString());
			}
			else{
				data.setAppName("No Name");				
			}
			
			//パッケージ名の設定
			data.setPackageName(packageName);
			
			//アイコンの設定
			Drawable icon = null;
			try {
				icon = pm.getApplicationIcon(packageName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				//nothing to do
			}
			data.setDrawable(icon);
			
			//ロック対象済みアプリはフラグをセット
			for(int i=1; i<=map.size(); i++){
				String name = (String)map.get(""+i);
				if(name.equals(packageName)){
					data.setLockFlag(true);
				}
			}			
			
			//ロック対象は前から挿入
			if(data.getLockFlag()){
				appDataList.add(0, data);
			}
			else{
				appDataList.add(data);
			}
        }
		
        AppArrayAdapter adapter = new AppArrayAdapter(this, android.R.layout.simple_list_item_1, appDataList);
        ListView listview = (ListView)findViewById(R.id.app_lock_list);
        listview.setAdapter(adapter);
        listview.setScrollingCacheEnabled(false); 
        listview.setOnItemClickListener(new OnItemClickListener(){
    		@Override
    		public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
    			//クリックで起動ロックをONにするかどうかは要検討
    		}
        });
        
		//レイアウトを有効化
		LinearLayout layout = (LinearLayout)findViewById(R.id.bottom_layout);
		layout.setVisibility(View.VISIBLE);
		
		//ロック解除設定されている場合は、チェックボックスをtrueにする
		SharedPreferences pref = getSharedPreferences(AppLockActivity.PREF_MODE, Context.MODE_PRIVATE);
		boolean isUnlock = pref.getBoolean(AppLockActivity.MODE_UNLOCK, false);
		if(isUnlock){
			CheckBox checkbox = (CheckBox)findViewById(R.id.unlock_flag);
			checkbox.setChecked(true);
			
			FrameLayout framelayout = (FrameLayout)findViewById(R.id.main_layout);
			framelayout.addView(mView);
			listview.setEnabled(false);
		}		
	}
	
	private void setListener(){
		Button btn = (Button)findViewById(R.id.app_lock_ok);
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//選択されたロック対象を保存
				saveLockList();

		        //Activityを終了し画面を閉じる
		        //finish();
			}
		});
		
		//有効化/無効化でビューを変える
		CheckBox checkbox = (CheckBox)findViewById(R.id.unlock_flag);
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				FrameLayout layout = (FrameLayout)findViewById(R.id.main_layout);
		        ListView listview = (ListView)findViewById(R.id.app_lock_list);

				if(isChecked){
					layout.addView(mView);
					listview.setEnabled(false);
				}
				else{
					layout.removeView(mView);
					listview.setEnabled(true);
				}
			}
		});
	}

	//SharedPreferenceにロック対象アプリを設定
	private void saveLockList(){
		SharedPreferences prefs = getSharedPreferences(PREF_LOCK, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		//設定済みSharedPreferenceをクリア
		editor.clear();
		
		//保存
        ListView view = (ListView)findViewById(R.id.app_lock_list);
        AppArrayAdapter adapter = (AppArrayAdapter)view.getAdapter();
        int length = adapter.getCount();
        int num = 0;
        for(int i=0; i<length; i++){
        	AppData item = adapter.getItem(i);
        	if(item.getLockFlag()){
        		//Log.d(TAG, "lock = " + item.getPackageName());
        		editor.putString(""+(++num), item.getPackageName());
        	}
        }
        editor.commit();
        
        if(num == 0){
        	controlService(false);
        }
        else{
        	controlService(true);
        }
	}
	
	private void controlService(boolean flag){
		SharedPreferences pref = getSharedPreferences(AppLockActivity.PREF_MODE, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		CheckBox checkbox = (CheckBox)findViewById(R.id.unlock_flag);
		if(checkbox.isChecked()){
			editor.putBoolean(MODE_UNLOCK, true);
			//ロック解除フラグがたっている場合はサービスを起動しないためflagをfalseに設定しなおす
			flag = false;
		}
		else{
			editor.putBoolean(MODE_UNLOCK, false);
		}
		editor.commit();

		if(flag){
        	//Log.d(TAG, "launch service");
        	//監視用サービスを起動
        	Intent intent = new Intent(AppLockActivity.this, AppWatchService.class);
        	startService(intent);
        	
        	Toast.makeText(this, getString(R.string.toast_lock), Toast.LENGTH_SHORT).show();
		}
		else{
        	//Log.d(TAG, "stop service");
        	//監視用サービス終了
        	Intent intent = new Intent(AppLockActivity.this, AppWatchService.class);
        	stopService(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_lock, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle presses on the action bar items
    	switch (item.getItemId()) {
    	/*
    	case R.id.action_clear_all:
    		clear();
    		return true;
    		*/
    		
    	case R.id.action_change_password:
    		changePassword();
    		return true;
    		
    		/*
    	case R.id.action_help:
    		help();
    		return true;
    		*/
    		
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private void clear(){
    	//ロックリストクリア
    }
    
    private void changePassword(){
    	//パスワード変更
    	inputPassword(CHANGE_PASSWORD);
    }
    
    private void help(){
    	//ヘルプ表示
    }
}
