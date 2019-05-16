package org.ar.arrtmax.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.gyf.barlibrary.ImmersionBar;

import org.ar.arrtmax.utils.AppManager;

public abstract class BaseActivity extends AppCompatActivity {
    protected ImmersionBar mImmersionBar;
    protected boolean neetChenJin = false;
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppManager.getAppManager().addActivity(this);
        super.onCreate(savedInstanceState);
        this.setContentView(this.getLayoutId());
        if (neetChenJin) {
            mImmersionBar = ImmersionBar.with(this);
        }else {
            mImmersionBar = ImmersionBar.with(this).statusBarColor("#22C485").fitsSystemWindows(true).statusBarDarkFont(true,0.2f);
        }
        mImmersionBar.init();
        this.initView(savedInstanceState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null) {
            mImmersionBar.destroy();
        }
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }


    public void startAnimActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    protected void startAnimActivity(Class<?> cls, String key, int value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(key, value);
        startActivity(intent);
    }

    protected void startAnimActivity(Class<?> cls, String key, String value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(key, value);
        startActivity(intent);
    }

    public abstract int getLayoutId();

    public abstract void initView(Bundle savedInstanceState);


    /**
     * 添加Fragment
     *
     * @param resLayId
     * @param showFragment
     * @param isAddBackStack
     * @param hideFragments  要隐藏的Fragment数组
     */
    protected void addFragment(int resLayId, Fragment showFragment, boolean isAddBackStack, Fragment... hideFragments) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (hideFragments != null) {
            for (Fragment hideFragment : hideFragments) {
                if (hideFragment != null) {
                    fragmentTransaction.hide(hideFragment);
                }
            }
        }
        if (showFragment!=null) {
            if (showFragment.isAdded()) {
                fragmentTransaction.show(showFragment);
            } else {
                fragmentTransaction.add(resLayId, showFragment, showFragment.getClass().getName());
                fragmentTransaction.show(showFragment);
            }
        }
//		fragmentTransaction.replace(resLayId,showFragment);
        if (isAddBackStack) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commitAllowingStateLoss();
//        fragmentTransaction.commit();
    }

    public void finishAnimActivity() {
        finish();
    }




    @Override
    protected void onPause() {
        super.onPause();
    }



    @Override
    protected void onStop() {
        super.onStop();
    }
}
