package org.anyrtc.arrtmax.activity;

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

import org.anyrtc.arrtmax.ARApplication;
import org.anyrtc.arrtmax.R;
import org.anyrtc.arrtmax.adapter.MessageAdapter;
import org.anyrtc.arrtmax.adapter.TabAdapter;
import org.anyrtc.arrtmax.bean.MessageBean;
import org.anyrtc.arrtmax.utils.SoundPlayUtils;
import org.anyrtc.arrtmax.utils.ToastUtil;
import org.anyrtc.arrtmax.weight.CustomDialog;
import org.anyrtc.arrtmax.weight.RTCVideoView;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.ar.common.enums.ARNetQuality;
import org.ar.common.utils.AR_AudioManager;
import org.ar.rtmax_kit.ARMaxEngine;
import org.ar.rtmax_kit.ARMaxEvent;
import org.ar.rtmax_kit.ARMaxKit;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Logging;
import org.webrtc.PercentFrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SpeakActivity extends BaseActivity implements View.OnClickListener,View.OnTouchListener{

    TextView tvTitle,tvUserId,tv_exit;
    TabLayout tvTab;
    ViewPager viewPager;
    RecyclerView rvMessage;
    ImageButton btnApply;
    LinearLayout llMainLayout,llRemoteVideo;
    RelativeLayout rl_call_video;
    RelativeLayout rl_call_layout;

    String[] tabItem = {"上报视频", "广播消息"};
    List<View> tabView = new ArrayList<>();
    TabAdapter tabAdapter;
    private CustomDialog exitDialog;
    private ARMaxKit mRTMaxKit;
    RTCVideoView localVideo;
    private AR_AudioManager mRTCAudioManager;
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
    public int getLayoutId() {
        return R.layout.activity_speak;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        tvTitle=findViewById(R.id.tv_title);
        tvTab=findViewById(R.id.tv_tab);
        viewPager=findViewById(R.id.viewPager);
        rvMessage=findViewById(R.id.rl_message);
        btnApply=findViewById(R.id.btn_apply);
        tvUserId=findViewById(R.id.tv_user_id);
        tv_exit=findViewById(R.id.tv_exit);
        llMainLayout=findViewById(R.id.ll_main_layout);
        rl_call_video=findViewById(R.id.rl_call_video);
        llRemoteVideo=findViewById(R.id.ll_remote_video);
        rl_call_layout=findViewById(R.id.rl_call_layout);
        tv_exit.setOnClickListener(this);



        for (int i = 0; i < tabItem.length; i++) {
            tvTab.getTabAt(i).setCustomView(getTabView(i));
        }
        View view_report = LayoutInflater.from(this).inflate(R.layout.tab_view_report, null);
        View view_message = LayoutInflater.from(this).inflate(R.layout.tab_view_message, null);

        etMessage = view_message.findViewById(R.id.et_message);
        btnSendMessage = view_message.findViewById(R.id.btn_send_message);
        btnSendMessage.setOnClickListener(this);

        localVideo = new RTCVideoView(ARMaxEngine.Inst().egl(), this);


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
                hideIputKeyboard(SpeakActivity.this);
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

        mRTMaxKit = new ARMaxKit(arMaxEvent);

        vb = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        mRTMaxKit.joinTalkGroup("123456789", ARApplication.tempUserid, getUserData());
        tvUserId.setText("用户ID:" + ARApplication.tempUserid);
        mRTCAudioManager = AR_AudioManager.create(this, new Runnable() {
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

    public String getUserData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userId", ARApplication.tempUserid);
            jsonObject.put("nickName", ARApplication.tempNickName);
            jsonObject.put("headUrl", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    ARMaxEvent arMaxEvent = new ARMaxEvent() {
        @Override
        public void onRTCJoinTalkGroupOK(final String groupId) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcJoinTalkGroupOK====" + groupId);
                    ToastUtil.show("加入群组成功");
                    isJoinAnyRTCSuccess = true;
                }
            });
        }

        @Override
        public void onRTCJoinTalkGroupFailed(String groupId, final int code, String reason) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcJoinTalkGroupFailed====" + code);
                    ToastUtil.show("加入群组失败" + code);
                    isJoinAnyRTCSuccess = false;
                    finish();
                }
            });
        }

        @Override
        public void onRTCLeaveTalkGroup(final int code) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcLeaveTalkGroup====" + code);
                }
            });
        }

        @Override
        public void onRTCApplyTalkOk() {
            SpeakActivity.this.runOnUiThread(new Runnable() {
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
            SpeakActivity.this.runOnUiThread(new Runnable() {
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
        public void onRTCTalkOn(final String userId, final String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkOn====userId==" + userId + "==userData===" + userData);
                    hadSomeOneSpeaking = true;
                    tvTitle.setText("用户" + userId + "正在发言");
                    current_people_speaking = "用户" + userId + "正在发言";
                }
            });
        }

        @Override
        public void onRTCTalkP2POn(final String userId, final String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkP2POn====userId==" + userId + "==userData===" + userData);
                    hadSomeOneSpeaking = true;
                    ShowQiangchaDialog(userId);
                    tvTitle.setText("用户" + userId + "正在发言");
                    current_people_speaking = "用户" + userId + "正在发言";
                }
            });
        }

        @Override
        public void onRTCTalkP2POff(final String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkP2POff======userData===" + userData);
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
        public void onRTCTalkClosed(final int code, final String userId, String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcTalkClosed======userId===" + userId + "nCode=" + code);
                    hadSomeOneSpeaking = false;
                    current_people_speaking = "";
                    tvTitle.setText(current_people_num);
                    if (code == 0) {

                    } else if (code == 802) {
                        ToastMessage(getString(R.string.speak_busy));
                    } else if (code == 810) {
                        ToastUtil.show("讲话被打断！");
                    } else if (code == 811) {
                        ToastUtil.show("讲话被打断！");
                    }

                }
            });
        }

        @Override
        public void onRTCVideoMonitorRequest(final String userId, String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcVideoMonitorRequest======userId===" + userId);
                    if (isReporting){
                        mRTMaxKit.rejectVideoMonitor(userId);
                        ToastUtil.show("你正在上报视频，已拒绝"+userId+"监看请求");
                        return;
                    }
                    if (isMonitoring){
                        mRTMaxKit.rejectVideoMonitor(userId);
                        ToastUtil.show("你正在被监看，已拒绝"+userId+"监看请求");
                        return;
                    }
                    if (isCall){
                        mRTMaxKit.rejectVideoMonitor(userId);
                        ToastUtil.show("你正在通话中，已拒绝"+userId+"监看请求");
                        return;
                    }
                    ToastUtil.show("正在被用户" + userId + "监看");
                    mRTMaxKit.acceptVideoMonitor(userId);
                    isMonitoring = true;
                    mRTMaxKit.setLocalVideoCapturer(localVideo.openVideoRender("localRender").GetRenderPointer());
                    localVideo.getVideoRender().mLayout.setPosition(0,0,0,0);


                }
            });
        }

        @Override
        public void onRTCVideoMonitorClose(final String userId, String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show("用户" + userId + "停止监看");
                    isMonitoring = false;
                    mRTMaxKit.closeLocalVideoCapture();
                    showLog("OnRtcVideoMonitorClose======userId===" + userId);
                }
            });
        }

        @Override
        public void onRTCVideoMonitorResult(final String userId, int code, String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcVideoMonitorResult======userId===" + userId);
                }
            });
        }

        @Override
        public void onRTCVideoReportRequest(final String userId, String strUserData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcVideoReportRequest======userId===" + userId);
                    mRTMaxKit.closeReportVideo();
                }
            });
        }

        @Override
        public void onRTCVideoReportClose(final String userId) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showLog("OnRtcVideoReportClose======userId===" + userId);
                }
            });
        }

        @Override
        public void onRTCMakeCallOK(final String strCallId) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcMakeCallOK======strCallId===" + strCallId);
                }
            });
        }

        @Override
        public void onRTCAcceptCall(final String userId, String strUserData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcAcceptCall======userId===" + userId);
                }
            });
        }

        @Override
        public void onRTCRejectCall(final String userId, int nCode, String strUserData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcRejectCall======userId===" + userId);
                    ToastUtil.show("用户" + userId + "拒绝了你的通话请求");
                }
            });
        }

        @Override
        public void onRTCLeaveCall(final String userId) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcLeaveCall======userId===" + userId);
                }
            });
        }

        @Override
        public void onRTCReleaseCall(final String strCallId) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcReleaseCall======strCallId===" + strCallId);
                }
            });
        }

        @Override
        public void onRTCMakeCall(final String strCallId, final int nCallType, final String userId, String strUserData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
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
                    ShowCallRequestDialog(strCallId, userId, nCallType);
                }
            });
        }

        @Override
        public void onRTCEndCall(final String strCallId, final String userId, int nCode) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcEndCall======strCallId===" + strCallId + "=======userId" + userId);
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
        public void onRTCOpenRemoteVideoRender(final String peerId, final String publishId, final String userId, String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcOpenVideoRender======strRtcPeerId===" + peerId + "=======userId" + userId);
                    final RTCVideoView rtcVideoView = new RTCVideoView(ARMaxEngine.Inst().egl(), SpeakActivity.this);
                    mRTMaxKit.setRTCRemoteVideoRender(publishId, rtcVideoView.openVideoRender(peerId).GetRenderPointer());
                    rtcVideoView.getVideoRender().mView.setZOrderMediaOverlay(true);
                    rtcVideoView.getVideoRender().mLayout.setTag(userId);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(200, 266);
                    llRemoteVideo.addView(rtcVideoView.getVideoRender().mLayout, params);
                    remoteVideoList.put(peerId, rtcVideoView.getVideoRender().mLayout);
                }
            });
        }

        @Override
        public void onRTCCloseRemoteVideoRender(final String peerId, final String publishId, final String userId) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcCloseVideoRender======strRtcPeerId===" + peerId + "=======userId" + userId);
                    if (null != mRTMaxKit) {
                        mRTMaxKit.setRTCRemoteVideoRender(publishId, 0);
                        PercentFrameLayout view = remoteVideoList.get(peerId);
                        if (null != view) {
                            if (llRemoteVideo != null) {
                                llRemoteVideo.removeView(view);
                            }
                            remoteVideoList.remove(peerId);
                        }
                    }
                }
            });
        }

        @Override
        public void onRTCOpenRemoteAudioTrack(final String peerId, final String userId, String userData) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcOpenAudioTrack======strRtcPeerId===" + peerId + "=======userId" + userId);

                }
            });
        }

        @Override
        public void onRTCCloseRemoteAudioTrack(final String peerId, final String userId) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcCloseAudioTrack======strRtcPeerId===" + peerId + "=======userId" + userId);


                }
            });
        }

        @Override
        public void onRTCRemoteAVStatus(final String peerId, boolean audio, boolean video) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcAVStatus======strRtcPeerId===" + peerId);
                }
            });
        }

        @Override
        public void onRTCLocalAVStatus(boolean audio, boolean video) {

        }

        @Override
        public void onRTCRemoteAudioActive(final String peerId, String userId, final int nLevel, int nShowtime) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcAudioActive======strRtcPeerId===" + peerId + "level" + nLevel);
                }
            });
        }

        @Override
        public void onRTLocalAudioActive(int nLevel, int nTime) {

        }

        @Override
        public void onRTCRemoteNetworkStatus(String peerId, String userId, int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {

        }


        @Override
        public void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {

        }

        @Override
        public void onRTCUserMessage(final String userId, String userName, String headerUrl, final String content) {
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLog("OnRtcUserMessage======userId===" + userId + "content===" + content);
                    messageAdapter.addData(new MessageBean(false, userId, content));
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
            SpeakActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    current_people_num="当前在线人数："+nNum;
                    tvTitle.setText(current_people_num);
                }
            });
        }

        @Override
        public void onRTCGotRecordFile(int nRecType, String userData, String filePath) {

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

    private void showLog(String message) {
        Logging.d(this.getClass().getSimpleName(), "-------------------->" + message);
    }
    public void toggleVideoLayout() {
        if (llMainLayout.getTranslationX() == 0) {
            hideIputKeyboard(this);
            ObjectAnimator animator = ObjectAnimator.ofFloat(llMainLayout, "translationX", llMainLayout.getTranslationX(), -llMainLayout.getWidth());
            animator.setDuration(400);
            animator.start();
            mRTMaxKit.setLocalVideoCapturer(localVideo.openVideoRender("localRender").GetRenderPointer());
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

    private void ToastMessage(String messages) {
        //LayoutInflater的作用：对于一个没有被载入或者想要动态载入的界面，都需要LayoutInflater.inflate()来载入，LayoutInflater是用来找res/layout/下的xml布局文件，并且实例化
        LayoutInflater inflater = getLayoutInflater();//调用Activity的getLayoutInflater()
        View view = inflater.inflate(R.layout.item_toast,  null); //加載layout下的布局
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
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_message:
                if (etMessage.getText().toString().trim().isEmpty()){
                    ToastUtil.show("消息不能为空");
                    return;
                }
                mRTMaxKit.sendMessage(ARApplication.tempNickName, "a", etMessage.getText().toString());
                messageAdapter.addData(new MessageBean(true, ARApplication.tempUserid, etMessage.getText().toString()));
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
            case R.id.tv_exit:
                ShowExitDialog();
                break;
        }
    }

    public void ShowCallRequestDialog(final String strCallId, final String userId, final int type) {
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
                content.setText("用户" + userId + "对你发起" + (type == 0 ? "视频呼叫" : "音频呼叫"));
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

                        if (isMonitoring){
                            ToastUtil.show("你正在被监看");
                            mRTMaxKit.rejectCall(strCallId);
                            if (null != CallRequestDialog && CallRequestDialog.isShowing()) {
                                CallRequestDialog.dismiss();
                            }
                            return;
                        }
                        mRTMaxKit.acceptCall(strCallId);
                        isCall = true;
                        if (type == 0) {
                            isAudioCall=false;
                            toggleVideoLayout();
                        } else {
                            if (isMonitoring){
                                ToastUtil.show("你正在被监看");
                                mRTMaxKit.rejectCall(strCallId);
                                if (null != CallRequestDialog && CallRequestDialog.isShowing()) {
                                    CallRequestDialog.dismiss();
                                }
                                return;
                            }
                            isAudioCall=true;
                            ShowAudioCallDialog(userId);
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
                mRTMaxKit.setLocalVideoCapturer(localVideo.openVideoRender("localRender").GetRenderPointer());
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
                ivAudio.setImageResource(R.drawable.audio_anim );
                AnimationDrawable animationDrawable = (AnimationDrawable) ivAudio.getDrawable();
                animationDrawable.start();
            }
        });
    }

    public void ShowExitDialog() {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setContentView(R.layout.dialog_base_layout_two_btn )
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
}
