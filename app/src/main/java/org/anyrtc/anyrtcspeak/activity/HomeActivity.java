package org.anyrtc.anyrtcspeak.activity;

import android.Manifest;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;

import org.anyrtc.anyrtcspeak.R;
import org.anyrtc.anyrtcspeak.utils.AppManager;
import org.anyrtc.anyrtcspeak.utils.ToastUtil;

import java.util.List;

import butterknife.OnClick;

public class HomeActivity extends BaseActivity {


    @Override
    public int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
    }


    @OnClick({R.id.btn_join})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_join:
                requestPermission();
                break;
        }
    }

    public void requestPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        startAnimActivity(MainActivity.class);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        ToastUtil.show("请先开启音视频权限");
                    }
                })
                .start();

    }


    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtil.show(getString(R.string.press_again));
                mExitTime = System.currentTimeMillis();
            } else {
                AppManager.getAppManager().AppExit(HomeActivity.this);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
