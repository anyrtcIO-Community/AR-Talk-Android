package org.anyrtc.arrtmax.activity;

import android.os.Bundle;
import android.view.View;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.anyrtc.arrtmax.R;
import org.anyrtc.arrtmax.utils.ToastUtil;

import java.util.List;

public class MainActivity extends BaseActivity {


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        findViewById(R.id.btn_join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AndPermission.with(MainActivity.this).runtime().permission(Permission.CAMERA,Permission.RECORD_AUDIO).onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        startAnimActivity(SpeakActivity.class);
                    }
                }).onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        ToastUtil.show("请开启音视频权限");
                    }
                }).start();
            }
        });
    }
}
