package org.anyrtc.anyrtcspeak.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.anyrtc.anyrtcspeak.MyApplication;
import org.anyrtc.anyrtcspeak.R;
import org.anyrtc.anyrtcspeak.adapter.MessageAdapter;
import org.anyrtc.anyrtcspeak.adapter.TabAdapter;
import org.anyrtc.anyrtcspeak.bean.MessageBean;
import org.anyrtc.anyrtcspeak.utils.SoundPlayUtils;
import org.anyrtc.anyrtcspeak.utils.ToastUtil;
import org.anyrtc.anyrtcspeak.weight.CustomDialog;
import org.anyrtc.anyrtcspeak.weight.RTCVideoView;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.meet_kit.AnyRTCMeetEngine;
import org.anyrtc.rtmax_kit.RTMaxEvent;
import org.anyrtc.rtmax_kit.RTMaxKit;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.Logging;
import org.webrtc.PercentFrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity implements View.OnTouchListener, View.OnClickListener {


    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_tab)
    TabLayout tvTab;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.rl_message)
    RecyclerView rvMessage;
    @BindView(R.id.btn_apply)
    ImageButton btnApply;
    @BindView(R.id.tv_user_id)
    TextView tvUserId;
    @BindView(R.id.ll_main_layout)
    LinearLayout llMainLayout;
    @BindView(R.id.rl_call_video)
    RelativeLayout rl_call_video;
    @BindView(R.id.ll_remote_video)
    LinearLayout llRemoteVideo;
    @BindView(R.id.rl_call_layout)
    RelativeLayout rl_call_layout;
    String[] tabItem = {"上报视频", "广播消息"};
    List<View> tabView = new ArrayList<>();
    TabAdapter tabAdapter;
    private CustomDialog exitDialog;
    private RTMaxKit mRTMaxKit;
    RTCVideoView localVideo;
    private AnyRTCAudioManager mRTCAudioManager;
    CameraVideoCapturer.CameraEventsHandler eventsHandler;
    private CustomDialog CallRequestDialog;
    private CustomDialog qiangChaDialog;
    private CustomDialog ReportVideoDialog;
    private CustomDialog AudioCallDialog;
    private String current_people_num = "";
    private String current_people_speaking = "";
    private boolean hadSomeOneSpeaking = false;
    private boolean isJoinAnyRTCSuccess = false;
    private boolean isPressed = false;
    boolean isCall = false;
    boolean isAudioCall=false;
    Vibrator vb;
    boolean isMonitoring = false;//是否被监看

    boolean isReporting = false;//是否正在上报
    private MessageAdapter messageAdapter;
    private HashMap<String, PercentFrameLayout> remoteVideoList = new HashMap<>();
    private EditText etMessage, etReport;

    private Button btnSendMessage, btnReport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsHandler = new CameraVideoCapturer.CameraEventsHandler() {
            @Override
            public void onCameraError(String errorDescription) {

            }

            @Override
            public void onCameraDisconnected() {

            }

            @Override
            public void onCameraFreezed(String errorDescription) {

            }

            @Override
            public void onCameraOpening(String cameraName) {

            }

            @Override
            public void onFirstFrameAvailable() {

            }

            @Override
            public void onCameraClosed() {

            }
        };
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {

        for (int i = 0; i < tabItem.length; i++) {
            tvTab.getTabAt(i).setCustomView(getTabView(i));
        }
        View view_report = LayoutInflater.from(this).inflate(R.layout.tab_view_report, null);
        View view_message = LayoutInflater.from(this).inflate(R.layout.tab_view_message, null);

        etMessage = view_message.findViewById(R.id.et_message);
        btnSendMessage = view_message.findViewById(R.id.btn_send_message);
        btnSendMessage.setOnClickListener(this);

        localVideo = new RTCVideoView(AnyRTCMeetEngine.Inst().Egl(), this);


        etReport = view_report.findViewById(R.id.et_report_id);
        btnReport = view_report.findViewById(R.id.btn_report);
        btnReport.setOnClickListener(this);

        tabView.add(view_report);
        tabView.add(view_message);
        tabAdapter = new TabAdapter(tabView);
        viewPager.setAdapter(tabAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvTab.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tvTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                changeTabSelect(tab);
                hideIputKeyboard(MainActivity.this);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                changeTabNormal(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        messageAdapter = new MessageAdapter();
        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        rvMessage.setAdapter(messageAdapter);

        mRTMaxKit = new RTMaxKit(rtMaxEvent);

        vb = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        showLog("myuserid======strUserId===" + MyApplication.tempUserid);
        mRTMaxKit.joinTalkGroup("123456789", MyApplication.tempUserid, getUserData());
        tvUserId.setText("用户ID:" + MyApplication.tempUserid);
        mRTCAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
            // This method will be called each time the audio state (number
            // and
            // type of devices) has been changed.
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        mRTCAudioManager.init();
        btnApply.setOnTouchListener(this);

    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void changeTabNormal(TabLayout.Tab tab) {
        final View view = tab.getCustomView();
        ObjectAnimator anim = ObjectAnimator
                .ofFloat(tab.getCustomView(), "ScaleX", 1.0F, 0.9F)
                .setDuration(200);
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                view.setAlpha(1f - (1f - cVal) * (0.5f / 0.1f));
                view.setScaleX(cVal);
                view.setScaleY(cVal);
            }
        });
    }

    private void changeTabSelect(TabLayout.Tab tab) {
        final View view = tab.getCustomView();
        ObjectAnimator anim = ObjectAnimator
                .ofFloat(view, "ScaleX", 1.0F, 1.1F)
                .setDuration(200);
        anim.start();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float cVal = (Float) animation.getAnimatedValue();
                view.setAlpha(0.5f + (cVal - 1f) * (0.5f / 0.1f));
                view.setScaleX(cVal);
                view.setScaleY(cVal);
            }
        });
    }

    public void hideIputKeyboard(final Context context) {
        final Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                InputMethodManager mInputKeyBoard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (activity.getCurrentFocus() != null) {
                    mInputKeyBoard.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                }
            }
        });
    }

    private View getTabView(int index) {
        //自定义View布局
        View view = LayoutInflater.from(this).inflate(R.layout.tab_cutom_view, null);
        TextView content = (TextView) view.findViewById(R.id.tv_text);
        content.setText(tabItem[index]);
        if (index == 0) {
            view.setScaleX(1.1f);
            view.setScaleY(1.1f);
        }
        return view;
    }

    public String getUserData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", MyApplication.tempUserid);
            jsonObject.put("nickName", MyApplication.tempNickName);
            jsonObject.put("headUrl", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public void toggleVideoLayout() {
        if (llMainLayout.getTranslationX() == 0) {
            hideIputKeyboard(this);
            ObjectAnimator animator = ObjectAnimator.ofFloat(llMainLayout, "translationX", llMainLayout.getTranslationX(), -llMainLayout.getWidth());
            animator.setDuration(400);
            animator.start();
            mRTMaxKit.setLocalVideoCapturer(localVideo.openVideoRender("localRender").GetRenderPointer(), true, AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium3, eventsHandler);
            localVideo.getVideoRender().mView.setZOrderMediaOverlay(false);
            rl_call_video.addView(localVideo.getVideoRender().mLayout);
            rl_call_layout.setVisibility(View.VISIBLE);
        } else {
            ObjectAnimator animator = ObjectAnimator.ofFloat(llMainLayout, "translationX", llMainLayout.getTranslationX(), 0);
            animator.setDuration(400);
            animator.start();
            rl_call_layout.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.tv_exit, R.id.btn_switch, R.id.btn_hang_up})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_exit:
                ShowExitDialog();
                break;
            case R.id.btn_switch:
                mRTMaxKit.switchCamera();
                break;
            case R.id.btn_hang_up:
                mRTMaxKit.leaveCall();
                rl_call_video.removeAllViews();
                mRTMaxKit.closeLocalVideoCapture();
                isCall = false;
                toggleVideoLayout();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.btn_apply) {
            if (isJoinAnyRTCSuccess) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (hadSomeOneSpeaking) {
                        tvTitle.setText(current_people_speaking);
                        return false;
                    }
                    isPressed = false;
                    showLog("抬起");
                    cancelTalk();
                    SoundPlayUtils.play(1);
                    tvTitle.setText( current_people_num);
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (hadSomeOneSpeaking) {
                        tvTitle.setText("当前有人正在上麦");
                        return false;
                    }
                    isPressed = true;
                    showLog("按下");
                    SoundPlayUtils.play(1);
                    tvTitle.setText("准备中...");
                    int result = applyTalk(2);
                    if (result != 0) {
                        ToastMessage("操作过于频繁");
                    }
                }
            } else {
                ToastUtil.show(getString(R.string.join_group_faild));
            }
        }
        return false;
    }


    public void ShowExitDialog() {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.dialog_base_layout_two_btn)
                .setAnimId(R.style.AnimBottom)
                .setGravity(Gravity.CENTER)
                .setLayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                .setBackgroundDrawable(true)
                .build();
        exitDialog = builder.show(new CustomDialog.Builder.onInitListener() {
            @Override
            public void init(CustomDialog view) {
                TextView cancle = view.findViewById(R.id.tv_no);
                TextView ok = view.findViewById(R.id.tv_ok);
                cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != exitDialog && exitDialog.isShowing()) {
                            exitDialog.dismiss();
                        }
                    }
                });

                ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != exitDialog && exitDialog.isShowing()) {
                            exitDialog.dismiss();
                            if (isCall) {
                                mRTMaxKit.leaveCall();
                            }
                            finishAnimActivity();
                        }

                    }
                });
            }
        });
    }


    private void showLog(String message) {
        Logging.d(this.getClass().getSimpleName(), "-------------------->" + message);
    }

    private void ToastMessage(String messages) {
        //LayoutInflater的作用：对于一个没有被载入或者想要动态载入的界面，都需要LayoutInflater.inflate()来载入，LayoutInflater是用来找res/layout/下的xml布局文件，并且实例化
        LayoutInflater inflater = getLayoutInflater();//调用Activity的getLayoutInflater()
        View view = inflater.inflate(R.layout.item_toast, null); //加載layout下的布局
        TextView text = view.findViewById(R.id.tv_content);
        text.setText(messages); //toast内容
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 12, 20);//setGravity用来设置Toast显示的位置，相当于xml中的android:gravity或android:layout_gravity
        toast.setDuration(Toast.LENGTH_SHORT);//setDuration方法：设置持续时间，以毫秒为单位。该方法是设置补间动画时间长度的主要方法
        toast.setView(view); //添加视图文件
        toast.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ShowExitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    RTMaxEvent rtMaxEvent = new RTMaxEvent() {
        @Override
        public void onRTCJoinTalkGroupOK(final String strGroupId) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcJoinTalkGroupOK====" + strGroupId);
                    ToastUtil.show("加入群组成功");
                    isJoinAnyRTCSuccess = true;
                }
            });
        }

        @Override
        public void onRTCJoinTalkGroupFailed(String strGroupId, final int nCode) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcJoinTalkGroupFailed====" + nCode);
                    ToastUtil.show("加入群组失败" + nCode);
                    isJoinAnyRTCSuccess = false;
                    finish();
                }
            });
        }

        @Override
        public void onRTCLeaveTalkGroup(final int nCode) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcLeaveTalkGroup====" + nCode);
                }
            });
        }

        @Override
        public void onRTCApplyTalkOk() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcApplyTalkOk====");
                    if (isPressed) {
                        tvTitle.setText("准备中...");
                    } else {
                        cancelTalk();
                    }
                }
            });
        }

        @Override
        public void onRTCTalkCouldSpeak() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkCouldSpeak====");
                    if (isPressed) {
                        tvTitle.setText("我正在发言");
                        current_people_speaking = "我正在发言";
                        long[] pattern = {100, 100};   // 停止 开启 停止 开启
                        vb.vibrate(pattern, -1);
                        SoundPlayUtils.play(2);
                    } else {
                        cancelTalk();
                    }
                }
            });
        }

        @Override
        public void onRTCTalkOn(final String strUserId, final String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkOn====userId==" + strUserId + "==userData===" + strUserData);
                    hadSomeOneSpeaking = true;
                    tvTitle.setText("用户" + strUserId + "正在发言");
                    current_people_speaking = "用户" + strUserId + "正在发言";
                }
            });
        }

        @Override
        public void onRTCTalkP2POn(final String strUserId, final String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkP2POn====userId==" + strUserId + "==userData===" + strUserData);
                    hadSomeOneSpeaking = true;
                    ShowQiangchaDialog(strUserId);
                    tvTitle.setText("用户" + strUserId + "正在发言");
                    current_people_speaking = "用户" + strUserId + "正在发言";
                }
            });
        }

        @Override
        public void onRTCTalkP2POff(final String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkP2POff======userData===" + strUserData);
                    current_people_speaking = "";
                    hadSomeOneSpeaking = false;
                    tvTitle.setText( current_people_num);
                    if (qiangChaDialog != null) {
                        qiangChaDialog.dismiss();
                    }
                }
            });
        }

        @Override
        public void onRTCTalkClosed(final int nCode, final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkClosed======strUserId===" + strUserId + "nCode=" + nCode);
                    hadSomeOneSpeaking = false;
                    current_people_speaking = "";
                    tvTitle.setText(current_people_num);
                    if (nCode == 0) {

                    } else if (nCode == 802) {
                        ToastMessage(getString(R.string.speak_busy));
                    } else if (nCode == 810) {
                        ToastUtil.show("讲话被打断！");
                    } else if (nCode == 811) {
                        ToastUtil.show("讲话被打断！");
                    }

                }
            });
        }

        //收到监看请求
        @Override
        public void onRTCVideoMonitorRequest(final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcVideoMonitorRequest======strUserId===" + strUserId);
                    if (isReporting){
                        mRTMaxKit.rejectVideoMonitor(strUserId);
                        ToastUtil.show("你正在上报视频，已拒绝"+strUserId+"监看请求");
                        return;
                    }
                    if (isMonitoring){
                        mRTMaxKit.rejectVideoMonitor(strUserId);
                        ToastUtil.show("你正在被监看，已拒绝"+strUserId+"监看请求");
                        return;
                    }
                    if (isCall){
                        mRTMaxKit.rejectVideoMonitor(strUserId);
                        ToastUtil.show("你正在通话中，已拒绝"+strUserId+"监看请求");
                        return;
                    }
                    ToastUtil.show("正在被用户" + strUserId + "监看");
                    mRTMaxKit.acceptVideoMonitor(strUserId);
                    isMonitoring = true;
                    mRTMaxKit.setLocalVideoCapturer(localVideo.openVideoRender("localRender").GetRenderPointer(), true, AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium3, eventsHandler);
                    localVideo.getVideoRender().mLayout.setPosition(0,0,0,0);


                }
            });
        }

        @Override
        public void onRTCVideoMonitorClose(final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show("用户" + strUserId + "停止监看");
                    isMonitoring = false;
                    mRTMaxKit.closeLocalVideoCapture();
                    showLog("OnRtcVideoMonitorClose======strUserId===" + strUserId);
                }
            });
        }

        @Override
        public void onRTCVideoMonitorResult(final String strUserId, int nCode, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcVideoMonitorResult======strUserId===" + strUserId);
                }
            });
        }

        @Override
        public void onRTCVideoReportRequest(final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcVideoReportRequest======strUserId===" + strUserId);
                    mRTMaxKit.closeReportVideo();
                }
            });
        }

        @Override
        public void onRTCVideoReportClose(final String strUserId) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showLog("OnRtcVideoReportClose======strUserId===" + strUserId);
                }
            });
        }

        @Override
        public void onRTCMakeCallOK(final String strCallId) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcMakeCallOK======strCallId===" + strCallId);
                }
            });
        }

        @Override
        public void onRTCAcceptCall(final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcAcceptCall======strUserId===" + strUserId);
                }
            });
        }

        @Override
        public void onRTCRejectCall(final String strUserId, int nCode, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcRejectCall======strUserId===" + strUserId);
                    ToastUtil.show("用户" + strUserId + "拒绝了你的通话请求");
                }
            });
        }

        @Override
        public void onRTCLeaveCall(final String strUserId) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcLeaveCall======strUserId===" + strUserId);
                }
            });
        }

        @Override
        public void onRTCReleaseCall(final String strCallId) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcReleaseCall======strCallId===" + strCallId);
                }
            });
        }

        @Override
        public void onRTCMakeCall(final String strCallId, final int nCallType, final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcMakeCall======strCallId===" + strCallId + "=======nCallType" + nCallType);
                    if (isReporting){
                        mRTMaxKit.rejectCall(strCallId);
                        ToastUtil.show("你正在上报视频，无法接受视频呼叫");
                        return;
                    }
                    if (isMonitoring){
                        mRTMaxKit.rejectCall(strCallId);
                        ToastUtil.show("你正在被监看，无法接受视频呼叫");
                        return;
                    }
                    if (isCall){
                        ToastUtil.show("你正在通话中..");
                        return;
                    }
                    ShowCallRequestDialog(strCallId, strUserId, nCallType);
                }
            });
        }

        @Override
        public void onRTCEndCall(final String strCallId, final String strUserId, int nCode) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcEndCall======strCallId===" + strCallId + "=======strUserId" + strUserId);
                    ToastUtil.show("主叫方已挂断本次通话");
                    isCall = false;
                    mRTMaxKit.leaveCall();
                    if (!isAudioCall){
                        rl_call_video.removeAllViews();
                        mRTMaxKit.closeLocalVideoCapture();
                        toggleVideoLayout();
                    }else {
                        if (AudioCallDialog!=null){
                            AudioCallDialog.dismiss();
                        }
                    }

                }
            });
        }

        @Override
        public void onRTCOpenVideoRender(final String strRTCPeerId, final String strRTCPubId, final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcOpenVideoRender======strRtcPeerId===" + strRTCPeerId + "=======strUserId" + strUserId);
                    final RTCVideoView rtcVideoView = new RTCVideoView(AnyRTCMeetEngine.Inst().Egl(), MainActivity.this);
                    mRTMaxKit.setRTCVideoRender(strRTCPubId, rtcVideoView.openVideoRender(strRTCPeerId).GetRenderPointer());
                    rtcVideoView.getVideoRender().mView.setZOrderMediaOverlay(true);
                    rtcVideoView.getVideoRender().mLayout.setTag(strUserId);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 266);
                    llRemoteVideo.addView(rtcVideoView.getVideoRender().mLayout, params);
                    remoteVideoList.put(strRTCPeerId, rtcVideoView.getVideoRender().mLayout);
                }
            });
        }

        @Override
        public void onRTCCloseVideoRender(final String strRTCPeerId, final String strRTCPubId, final String strUserId) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcCloseVideoRender======strRtcPeerId===" + strRTCPeerId + "=======strUserId" + strUserId);
                    if (null != mRTMaxKit) {
                        mRTMaxKit.setRTCVideoRender(strRTCPubId, 0);
                        PercentFrameLayout view = remoteVideoList.get(strRTCPeerId);
                        if (null != view) {
                            if (llRemoteVideo != null) {
                                llRemoteVideo.removeView(view);
                            }
                            remoteVideoList.remove(strRTCPeerId);
                        }
                    }
                }
            });
        }

        @Override
        public void onRTCOpenAudioTrack(final String strRTCPeerId, final String strUserId, String strUserData) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcOpenAudioTrack======strRtcPeerId===" + strRTCPeerId + "=======strUserId" + strUserId);

                }
            });
        }

        @Override
        public void onRTCCloseAudioTrack(final String strRTCPeerId, final String strUserId) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcCloseAudioTrack======strRtcPeerId===" + strRTCPeerId + "=======strUserId" + strUserId);


                }
            });
        }

        @Override
        public void onRTCAVStatus(final String strRTCPeerId, boolean bAudio, boolean bVideo) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcAVStatus======strRtcPeerId===" + strRTCPeerId);
                }
            });
        }

        @Override
        public void onRTCAudioActive(final String strRTCPeerId, String strUserId, final int nLevel, int nShowtime) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcAudioActive======strRtcPeerId===" + strRTCPeerId + "level" + nLevel);
                }
            });
        }

        @Override
        public void onRTCNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost) {

        }

        @Override
        public void onRTCUserMessage(final String strUserId, String strUserName, String strUserHeader, final String strContent) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcUserMessage======strUserId===" + strUserId + "content===" + strContent);
                    messageAdapter.addData(new MessageBean(false, strUserId, strContent));
                    if (messageAdapter != null && messageAdapter.getData().size() > 0) {
                        if (rvMessage != null) {
                            rvMessage.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    }
                }
            });
        }

        @Override
        public void onRTCMemberNum(final int nNum) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    current_people_num="当前在线人数："+nNum;
                    tvTitle.setText(current_people_num);
                }
            });
        }

        @Override
        public void onRTCGotRecordFile(int nRecType, final String strUserData, String strFilePath) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcGotRecordFile======strUserData===" + strUserData);
                }
            });
        }
    };


    private int applyTalk(int nPriority) {
        return mRTMaxKit.applyTalk(nPriority);
    }

    private void cancelTalk() {
        mRTMaxKit.cancelTalk();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRTMaxKit.clear();
        //销毁音频管理器对象
        if (mRTCAudioManager != null) {
            mRTCAudioManager.close();
            mRTCAudioManager = null;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_message:
                if (etMessage.getText().toString().trim().isEmpty()){
                    ToastUtil.show("消息不能为空");
                    return;
                }
                mRTMaxKit.sendUserMessage(MyApplication.tempNickName, "a", etMessage.getText().toString());
                messageAdapter.addData(new MessageBean(true, MyApplication.tempUserid, etMessage.getText().toString()));
                if (messageAdapter != null && messageAdapter.getData().size() > 0) {
                    if (rvMessage != null) {
                        rvMessage.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                }
                etMessage.setText("");
                hideIputKeyboard(this);
                break;
            case R.id.btn_report:
                if (etReport.getText().toString().isEmpty()) {
                    ToastUtil.show("请输入上报用户的ID");
                    return;
                }
                if (!isJoinAnyRTCSuccess) {
                    ToastUtil.show("加入房间未成功");
                    return;
                }
                if (isMonitoring) {
                    ToastUtil.show("你正在被监看，视频通道被占用，无法上报视频");
                    return;
                }
                int result = mRTMaxKit.reportVideo(etReport.getText().toString());
                if (result != 0) {
                    ToastUtil.show("异常 error:" + result);
                } else {
                    isReporting=true;
                    ShowReportVideoDialog();
                }
                break;
        }
    }


    public void ShowCallRequestDialog(final String strCallId, final String strUserId, final int type) {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.dialog_call_request)
                .setAnimId(R.style.AnimBottom)
                .setCancelable(false)
                .setGravity(Gravity.CENTER)
                .setLayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                .setBackgroundDrawable(true)
                .build();
        CallRequestDialog = builder.show(new CustomDialog.Builder.onInitListener() {
            @Override
            public void init(CustomDialog view) {
                TextView cancle = view.findViewById(R.id.tv_no);
                TextView tvAccept = view.findViewById(R.id.tv_ok);
                TextView content = view.findViewById(R.id.content);
                content.setText("用户" + strUserId + "对你发起" + (type == 0 ? "视频呼叫" : "音频呼叫"));
                cancle.setText("拒绝");
                tvAccept.setText("同意");
                cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isCall = false;
                        mRTMaxKit.rejectCall(strCallId);
                        if (null != CallRequestDialog && CallRequestDialog.isShowing()) {
                            CallRequestDialog.dismiss();
                        }
                    }
                });

                tvAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRTMaxKit.acceptCall(strCallId);
                        isCall = true;
                        if (type == 0) {
                            isAudioCall=false;
                            toggleVideoLayout();
                        } else {
                            isAudioCall=true;
                            ShowAudioCallDialog(strUserId);
                        }
                        if (null != CallRequestDialog && CallRequestDialog.isShowing()) {
                            CallRequestDialog.dismiss();
                        }
                    }
                });
            }
        });
    }


    public void ShowReportVideoDialog() {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.dialog_report_layout)
                .setAnimId(R.style.AnimBottom)
                .setCancelable(false)
                .setGravity(Gravity.CENTER)
                .setLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                .setBackgroundDrawable(true)
                .build();
        ReportVideoDialog = builder.show(new CustomDialog.Builder.onInitListener() {
            @Override
            public void init(CustomDialog view) {

                final RelativeLayout reportVideoLayout = view.findViewById(R.id.rl_report_video);
                mRTMaxKit.setLocalVideoCapturer(localVideo.openVideoRender("localRender").GetRenderPointer(), true, AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium3, eventsHandler);
                reportVideoLayout.addView(localVideo.getVideoRender().mLayout);
                ImageButton btnClose = view.findViewById(R.id.btn_close_report);
                ImageButton btnSwitch = view.findViewById(R.id.btn_switch);
                btnClose.setVisibility(View.VISIBLE);
                btnSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mRTMaxKit.switchCamera();
                    }
                });
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isReporting=false;
                        mRTMaxKit.closeReportVideo();
                        mRTMaxKit.closeLocalVideoCapture();
                        reportVideoLayout.removeAllViews();
                        ReportVideoDialog.dismiss();
                    }
                });
            }
        });
    }

    public void ShowAudioCallDialog(final String userId) {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.dialog_audio_call_layout)
                .setAnimId(R.style.AnimBottom)
                .setCancelable(false)
                .setGravity(Gravity.CENTER)
                .setLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
                .setBackgroundDrawable(true)
                .build();
        AudioCallDialog = builder.show(new CustomDialog.Builder.onInitListener() {
            @Override
            public void init(CustomDialog view) {
                TextView tv_userid = view.findViewById(R.id.tv_userid);
                ImageButton btn_close_audio = view.findViewById(R.id.btn_close_audio);
                tv_userid.setText(userId);
                btn_close_audio.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        isCall=false;
                        mRTMaxKit.leaveCall();
                        AudioCallDialog.dismiss();
                    }
                });
            }
        });
    }

    public void ShowQiangchaDialog(final String userId) {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.dialog_qiangcha)
                .setAnimId(R.style.AnimBottom)
                .setCancelable(false)
                .setGravity(Gravity.CENTER)
                .setLayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
                .setBackgroundDrawable(true)
                .build();
        qiangChaDialog = builder.show(new CustomDialog.Builder.onInitListener() {
            @Override
            public void init(CustomDialog view) {
                TextView tv_userid = view.findViewById(R.id.tv_user_id);
                ImageView ivAudio = view.findViewById(R.id.iv_audio);
                tv_userid.setText(userId + "正在强插对话");
                ivAudio.setImageResource(R.drawable.audio_anim);
                AnimationDrawable animationDrawable = (AnimationDrawable) ivAudio.getDrawable();
                animationDrawable.start();
            }
        });
    }
}
