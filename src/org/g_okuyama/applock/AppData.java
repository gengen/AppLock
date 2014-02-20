package org.g_okuyama.applock;

import android.graphics.drawable.Drawable;

public class AppData {
	private boolean mLockFlag;
	private Drawable mDrawable;
	private String mAppName;
	private String mPackageName;
	
	public void setLockFlag(boolean flag){
		mLockFlag = flag;
	}
	
	public boolean getLockFlag(){
		return mLockFlag;
	}
	
	public void setDrawable(Drawable drawable){
		mDrawable = drawable;
	}
	
	public Drawable getDrawable(){
		return mDrawable;
	}
	
	public void setAppName(String name){
		mAppName = name;
	}
	
	public String getAppName(){
		return mAppName;
	}

	public void setPackageName(String name){
		mPackageName = name;
	}
	
	public String getPackageName(){
		return mPackageName;
	}
}
