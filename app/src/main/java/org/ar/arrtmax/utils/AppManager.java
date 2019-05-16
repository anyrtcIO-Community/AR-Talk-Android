package org.ar.arrtmax.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;


import org.ar.arrtmax.activity.BaseActivity;

import java.util.Stack;

/**
 * 
 * @Title: AppManager.java
 * @Description:activity
 * @Copyright: Copyright (c) 2016
 */
public class AppManager {
	private static Stack<BaseActivity> activityStack;
	private static AppManager instance;
	private AppManager() {
		
	}

	/**
	 * App Manager one time create Object
	 */
	public static AppManager getAppManager() {
		if (instance == null) {
			instance = new AppManager();
		}
		return instance;
	}


	/**
	 * addActivity(baseActivity)
	 */
	public void addActivity(BaseActivity activity) {
		if (activityStack == null) {
			activityStack = new Stack<BaseActivity>();
		}
		activityStack.add(activity);
	}


	/**
	 * currentActivity()
	 */
	public BaseActivity currentActivity() {
		if (activityStack == null || activityStack.isEmpty()) {
			return null;
		}
		BaseActivity activity = activityStack.lastElement();
		return activity;
	}

	/**
	 * findActivity(null)
	 */
	public BaseActivity findActivity(Class<?> cls) {
		BaseActivity activity = null;
		for (BaseActivity aty : activityStack) {
			if (aty.getClass().equals(cls)) {
				activity = aty;
				break;
			}
		}
		return activity;
	}


	
	/**
	 * finishActivity
	 */
	public void finishActivity() {
		BaseActivity activity = activityStack.lastElement();
		finishActivity(activity);
	}

	/**
	 * finishActivity
	 */
	public void finishActivity(Activity activity) {
		if (activity != null) {
			activityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}

	public void removeActivity(Activity activity){
		if (activity != null) {
			activityStack.remove(activity);
		}
	}

	/**
	 * finishActivity
	 */
	public void finishActivity(Class<?> cls) {
		for (BaseActivity activity : activityStack) {
			if (activity.getClass().equals(cls)) {
				finishActivity(activity);
			}
		}
	}

	/**
	 * finishOthersActivity(cls)
	 * 
	 * @param cls
	 */
	public void finishOthersActivity(Class<?> cls) {
		Stack<BaseActivity> tempStack = new Stack<>();
		for (BaseActivity activity : activityStack) {
			if (!(activity.getClass().equals(cls))) {
				if (activity != null) {
					tempStack.add(activity);
				}
			}
		}
		for (BaseActivity activity : tempStack) {
			if (activityStack.contains(activity)){
				finishActivity(activity);
			}
		}
	}




	/**
	 *finishAllActivity()
	 */
	public void finishAllActivity() {
		for (int i = 0, size = activityStack.size(); i < size; i++) {
			if (null != activityStack.get(i)) {
				activityStack.get(i).finishAnimActivity();
			}
		}
		activityStack.clear();
	}

	/**
	 * AppExit
	 */
	public void AppExit(Context context) {
		try {
			finishAllActivity();
			ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.killBackgroundProcesses(context.getPackageName());
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		} catch (Exception e) {
			System.exit(0);
		}
	}
}
