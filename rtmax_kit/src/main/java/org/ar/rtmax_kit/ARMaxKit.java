package org.ar.rtmax_kit;

import android.util.Log;

import org.ar.common.enums.ARNetQuality;
import org.ar.common.enums.ARVideoCommon;
import org.ar.common.utils.ARUtils;
import org.ar.common.utils.LooperExecutor;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Exchanger;

public class ARMaxKit {
    private static final String TAG = "ARMaxKit";

    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;
    /**
     * 当前对讲组的anyrtcid
     */
    private String mCurAnyRTCId;
    /**
     * 当前用户的userid
     */
    private String mUserid;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;
    private ARMaxEvent maxEvent;

    public ARMaxKit(final ARMaxEvent maxEvent) {
        ARUtils.assertIsTrue(maxEvent != null);
        this.maxEvent=maxEvent;
        mExecutor = ARMaxEngine.Inst().Executor();
        mEglBase = ARMaxEngine.Inst().egl();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(arMaxHelper);
                nativeSetLocalVideoProfileMode(ARMaxEngine.Inst().getArMaxOption().getVideoProfile().level);
                nativeSetLocalVideoFpsProfile(ARMaxEngine.Inst().getArMaxOption().getVideoFps().level);
                if (ARMaxEngine.Inst().getArMaxOption().getScreenOriention()== ARVideoCommon.ARVideoOrientation.Portrait){
                    nativeSetScreenToPortrait();
                }else {
                    nativeSetScreenToLandscape();
                }
            }
        });
    }

    /**
     * 销毁本地视频
     */
    public void closeLocalVideoCapture() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetLocalVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
            }
        });
    }

    /**
     * 销毁初始化对象
     */
    public void clear() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeLeave();
                if (mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetLocalVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
                nativeDestroy();
            }
        });
    }

    /**
     * 设置验证token
     *
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    public boolean setUserToken(final String strUserToken) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                if (null == strUserToken || strUserToken.equals("")) {
                    ret = false;
                } else {
                    nativeSetUserToken(strUserToken);
                    ret = true;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 是否打开回音消除
     *
     * @param bEnable true:打开，false: 关闭
     */
    public void setForceAecEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeForceSetAecEnable(bEnable);
            }
        });
    }

    /**
     * 设置本地音频是否可用
     *
     * @param bEnabled true：本地音频可用，false：本地音频不可用
     */
    public void setLocalAudioEnable(final boolean bEnabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalAudioEnable(bEnabled);
            }
        });
    }


    /**
     * 设置本地视频是否可用
     *
     * @param bEnabled true：本地视频可用，false：本地视频不可用
     */
    public void setLocalVideoEnable(final boolean bEnabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoEnable(bEnabled);
            }
        });
    }

    /**
     * 打开关闭摄像头闪光灯 true 打开  false关闭
     *
     * @param BOpen
     */
    public void openCameraTorchMode(final boolean BOpen) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVideoCapturer.openCameraTorchMode(BOpen);
            }
        });
    }

    /**
     * 设置远端用户是否可以接收自己的音视频
     *
     * @param userId     远端用户UserId
     * @param audioEnable true：音频可用，false：音频不可用
     * @param videoEnable true：视频可用，false：视频不可用
     */
    public void setRemoteAVEnable(final String userId, final boolean audioEnable, final boolean videoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRemoteAVEnable(userId, audioEnable, videoEnable);
            }
        });
    }


    /**
     * 不接收某路的音频
     *
     * @param publishId
     * @param audioEnable true:接收远端音频， false，不接收远端音频
     */
    public void muteRemoteAudioStream(final String publishId, final boolean audioEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerAudioEnable(publishId, audioEnable);
            }
        });
    }

    /**
     * 不接收某路的视频
     *
     * @param publishId
     * @param videoEnable true:接收远端视频， false，不接收远端视频
     */
    public void muteRemoteVideoStream(final String publishId, final boolean videoEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerVideoEnable(publishId, videoEnable);
            }
        });
    }


    /**
     * 设置本地视频采集的分辨率大小
     *
     * @param width  视频的宽
     * @param height 视频的高
     */
    public void setLocalVideoSize(final int width, final int height) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoSize(width, height);
            }
        });
    }

    /**
     * 设置视频横屏模式
     */
    public void setScreenToLandscape() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToLandscape();
            }
        });
    }

    /**
     * 设置视频竖屏模式
     */
    public void setScreenToPortrait() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToPortrait();
            }
        });
    }

    /**
     * 设置本地视频采集预览
     *
     * @param render 底层图像地址
     * @return 0：摄像头打开成功， 1：摄像头打开失败
     */
    public int setLocalVideoCapturer(final long render) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer == null) {
                    mCameraId = 0;
                    String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                    String frontCameraDeviceName =
                            CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                    int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                    if (numberOfCameras > 1 && frontCameraDeviceName != null && ARMaxEngine.Inst().getArMaxOption().isDefaultFrontCamera()) {
                        cameraDeviceName = frontCameraDeviceName;
                        mCameraId = 1;
                    }
                    Log.d(TAG, "Opening camera: " + cameraDeviceName);
                    mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                    if (mVideoCapturer == null) {
                        Log.e("sys", "Failed to open camera");
                        LooperExecutor.exchange(result, 1);
                    }


                    nativeSetLocalVideoCapturer(mVideoCapturer, render);
                    LooperExecutor.exchange(result, 0);
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

    /**
     * 设置USB相机相机
     *
     * @param usbCamera usb相机
     * @return
     */
    public int setUvcVideoCapturer(final Object usbCamera) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer == null) {
                    mCameraId = 0;
                    nativeSetUvcVideoCapturer(usbCamera, "");
                    LooperExecutor.exchange(result, 0);
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

    /**
     * 切换USB相机或者本机相机
     *
     * @param isUsb         是否是usb相机（当为true时，为采用usb相机，当为false时，为采用本机相机）
     * @param usbCamera     usb相机
     * @param render 本机相机的底层图像地址
     * @param isFront       本机相机是否是前置摄像头
     * @param eventsHandler 相机打开事件回调
     * @return 0：摄像头打开成功， 1：摄像头打开失败
     */
    public int selectCamera(final boolean isUsb, final Object usbCamera, final long render,
                            final boolean isFront, final CameraVideoCapturer.CameraEventsHandler eventsHandler) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (isUsb) {
                    //本地相机置空
                    if (mVideoCapturer != null) {
                        try {
                            mVideoCapturer.stopCapture();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nativeSetLocalVideoCapturer(null, 0);
                        mVideoCapturer = null;
                    }
                    if (mVideoCapturer == null) {
                        mCameraId = 0;
                        nativeSetUvcVideoCapturer(usbCamera, "");
                        LooperExecutor.exchange(result, 0);
                    }
                } else {
                    //USB相机置空
                    if (mVideoCapturer != null) {
                        nativeSetUvcVideoCapturer(null, "");
                        mVideoCapturer = null;
                    }

                    //加载本地相机
                    if (mVideoCapturer == null) {
                        mCameraId = 0;
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                        String frontCameraDeviceName =
                                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                        if (numberOfCameras > 1 && frontCameraDeviceName != null && isFront) {
                            cameraDeviceName = frontCameraDeviceName;
                            mCameraId = 1;
                        }
                        Log.d(TAG, "Opening camera: " + cameraDeviceName);
                        mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, eventsHandler);
                        if (mVideoCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 1);
                        }
                        nativeSetLocalVideoCapturer(mVideoCapturer, render);
                        LooperExecutor.exchange(result, 0);
                    }
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

    /**
     * 获取UVC相机的采集数据
     *
     * @return
     */
    public long getAnyrtcUvcCallabck() {
        return nativeGetAnyrtcUvcCallabck();
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(null);
                }
            }
        });
    }

    /**
     * 切换摄像头
     */
    public void switchCamera(final CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(cameraSwitchHandler);
                }
            }
        });
    }


    /**
     * 加入对讲组
     *
     * @param groupId  groupId 对讲组id（同一个anyrtc平台的appid内保持唯一性）
     * @param userId   用户的第三方平台的用户id
     * @param userData 用户的自定义数据
     * @return -1：groupId为空；0：成功，1：失败；2：userData 大于512
     */
    public int joinTalkGroup(final String groupId, final String userId, final String userData) {
        mUserid = userId;
        try {
            if (userData.getBytes("utf-8").length > 512) {
                return 2;
            } else {
                final Exchanger<Integer> result = new Exchanger<Integer>();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mCurAnyRTCId = groupId;
                        nativeSetDeviceInfo(ARMaxEngine.Inst().getDeviceInfo());
                        int ret = nativeJoin(groupId, userId, userData);
                        LooperExecutor.exchange(result, ret);
                    }
                });
                return LooperExecutor.exchange(result, 1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return 1;
        }
    }


    /**
     * 切换对讲组
     *
     * @param groupId  对讲组id（同一个anyrtc平台的appid内保持唯一性）
     * @param userData userData 自定义数据
     * @return -2：切换对讲组失败，-1：groupId为空，0：成功；2：userData 大于512字节
     */
    public int switchTalkGroup(final String groupId, final String userData) {
        try {
            if (userData.getBytes("utf-8").length > 512) {
                return 2;
            } else {
                final Exchanger<Integer> result = new Exchanger<Integer>();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int ret = -2;
                        mCurAnyRTCId = groupId;
                        ret = nativeReJoin(groupId, userData);
                        LooperExecutor.exchange(result, ret);
                    }
                });
                return LooperExecutor.exchange(result, -2);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -2;
        }
    }


    /**
     * 退出对讲组
     */
    public void leaveTalkGroup() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeLeave();
                mCurAnyRTCId = null;
            }
        });
    }

    /**
     * 申请对讲
     *
     * @param priority 申请抢麦用户的级别（0权限最大（数值越大，权限越小）；除0意外，可以后台设置0-10之间的抢麦权限大小））
     * @return 0: 调用OK  -1:未登录  -2:正在对讲中  -3: 资源还在释放中 -4: 操作太过频繁
     */
    public int applyTalk(final int priority) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeApplyTalk(priority);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 取消对讲；申请对讲成功（onRTCApplyTalkOk）之后方可结束对讲
     */
    public void cancelTalk() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeCancelTalk();
            }
        });
    }

    /**
     * 关闭P2P通话
     * 说明：在控制台强插对讲后，关闭和控制台之间的P2P通话。
     */
    public void closeP2PTalk() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeTalkP2PClose();
            }
        });
    }


    /**
     * 设置录音文件的路径
     * @param callPath 呼叫文件的保存路径（文件夹路径）
     * @param talkPath 对讲文件保存路径（文件夹路径）
     * @param talkP2PPath 强插P2P文件保存路径（文件夹路径）
     * @return 0/1:设置成功/文件夹不存在
     */
    public int setRecordPath(final String callPath, final String talkPath, final String talkP2PPath) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        if(null == callPath) {
            return 1;
        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int ret = 0;
                    File file = new File(callPath);
                    if (file.exists()) {
                        nativeSetRecordPath(callPath, talkPath, talkP2PPath);
                        ret = 0;
                    } else {
                        ret = 1;
                    }
                    LooperExecutor.exchange(result, ret);
                }
            });
        }
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 发起呼叫
     * @param userId 被叫用户userid
     * @param type 0:视频 1:音频
     * @param userData 自定义数据
     * @return 0:调用OK;-1:未登录;-2:没有通话-3:视频资源占用中;-5:本操作不支持自己对自己;-6:会话未创建（没有被呼叫用户）
     */
    public int makeCall(final String userId, final int type/*0:视频 1:音频*/, final String userData) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeMakeCall(userId, type, userData);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 呼叫邀请：视频/音频通话建立成功后，主叫邀请其他人加入通话
     * @param userId 被邀请用户userid
     * @param userData 自定义数据
     * @return 0:调用OK;-1:未登录;-2:没有通话-3:视频资源占用中;-5:本操作不支持自己对自己;-6:会话未创建（没有被呼叫用户）
     */
    public int inviteCall(final String userId, final String userData) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeInviteCall(userId, userData);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 主叫端结束某一路正在进行的通话
     * @param userId 指定用户userid
     */
    public void endCall(final String userId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeEndCall(userId);
            }
        });
    }

    /**
     * 接受呼叫或通话邀请
     * @param callId 呼叫请求时收到的callId
     * @return 0:调用OK;-1:未登录;-2:会话不存在-3:视频资源占用中;
     */
    public int acceptCall(final String callId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeAcceptCall(callId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 拒绝呼叫
     * @param callId 呼叫请求时收到的callId
     */
    public void rejectCall(final String callId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeRejectCall(callId);
            }
        });
    }

    /**
     * 被叫端退出当前通话
     */
    public void leaveCall() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeLeaveCall();
            }
        });
    }



    /**
     * 发起视频监看（或者收到视频上报请求时查看视频）
     * @param userId 被监看用户userId
     * @param userData 自定义数据
     * @return 返回值  0: 调用OK  -1:未登录	-5:本操作不支持自己对自己
     */
    public int monitorVideo(final String userId, final String userData) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeMonitorVideo(userId, userData);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 同意视频监看
     * @param hostId 发起人的userid
     * @return 返回值  0: 调用OK  -1:未登录 -3:视频资源占用中 -5:本操作不支持自己对自己
     */
    public int acceptVideoMonitor(final String hostId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeAcceptVideoMonitor(hostId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 拒绝视频监看
     * @param hostId 发起人的userid
     * @return 返回值  0: 调用OK  -1:未登录 -3:视频资源占用中 -5:本操作不支持自己对自己
     */
    public int rejectVideoMonitor(final String hostId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeRejectVideoMonitor(hostId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 监看发起者关闭视频监看
     *
     * @param userId 被监看用户的userid
     */
    public void closeVideoMonitor(final String userId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeCloseVideoMonitor(userId);
            }
        });
    }

    /**
     * 视频上报（接收端收到上报请求时，调用monitorVideo进行视频查看）
     *
     * @param userId 用户的userid
     * @return 返回值  0: 调用OK  -1:未登录 -3:视频资源占用中	-5:本操作不支持自己对自己
     */
    public int reportVideo(final String userId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeReportVideo(userId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }


    /**
     * 上报者关闭视频上报
     */
    public void closeReportVideo() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeReportVideoClose();
            }
        });
    }

    /**
     * 设置远端视频窗口
     * 该方法用于远端接通后，游客图像打开回调中（OnRTCOpenVideoRender）使用
     *  
     * 示例：VideoRenderer render = mVideoView.OnRtcOpenRemoteRender("strRtcPeerId", RendererCommon.ScalingType.SCALE_ASPECT_FIT);
     *       mRTMaxKit.setRTCVideoRender(strRtcPeerId,render.GetRenderPointer());
     *
     * @param publishId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     * @param render     SDK底层视频显示对象  （通过VideoRenderer对象获取，参考连麦窗口管理对象-添加连麦窗口渲染器方法）
     */
    public void setRTCRemoteVideoRender(final String publishId, final long render) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRender(publishId, render);
            }
        });
    }

    /**
     * 小组内发送消息
     *
     * @param userId        指定用户的userid（最大256个字节）
     * @param headerUrl 消息发送者的业务平台的头像url（最大512个字节）
     * @param content       消息内容（最大256个字节）
     * @return 返回结果，0：成功；1：失败；4：参数非法；如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    public int sendMessage(final String userId, final String headerUrl, final String content) {
        if (userId.getBytes().length > 384) {
            return 4;
        }
        if (headerUrl.getBytes().length > 1536) {
            return 4;
        }
        if (content.getBytes().length > 1536) {
            return 4;
        }

        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String headUrl = headerUrl;
                if (headUrl.length() == 0) {
                    headUrl = "strCustomHeaderUrl can't be empty string";
                }
                boolean ret = false;
                ret = nativeSendUserMsg(userId, headUrl, content);
                LooperExecutor.exchange(result, ret ? 0 : 1);
            }
        });
        return LooperExecutor.exchange(result, 1);
    }



    public void setAudioActiveCheck(final boolean audioOnly, final boolean bAudioDetect) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAuidoModel(audioOnly, bAudioDetect);
            }
        });
    }

    /**
     * 打开或关闭前置摄像头镜面
     * @param enable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetCameraMirror(enable);
            }
        });
    }

    /**
     * 打开或关闭网络状态监测
     *
     * @param enable true: 打开; false: 关闭
     */
    public void setNetworkStatus(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetNetworkStatus(enable);
            }
        });
    }

    /**
     * 网络监测是否打开
     *
     * @return true:可用， false：不可用
     */
    public boolean networkStatusEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeNetworkStatusEnabled();
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }


    ARMaxHelper arMaxHelper = new ARMaxHelper() {
        @Override
        public void OnRtcJoinTalkGroupOK(String groupId) {
            if (maxEvent!=null){
                maxEvent.onRTCJoinTalkGroupOK(groupId);
            }
        }

        @Override
        public void OnRtcJoinTalkGroupFailed(String groupId, int nCode, String strReason) {
            if (maxEvent!=null){
                maxEvent.onRTCJoinTalkGroupFailed(groupId,nCode,strReason);
            }
        }

        @Override
        public void OnRtcLeaveTalkGroup(int nCode) {
            if (maxEvent!=null){
                maxEvent.onRTCLeaveTalkGroup(nCode);
            }
        }

        @Override
        public void OnRtcApplyTalkOk() {
            if (maxEvent!=null){
                maxEvent.onRTCApplyTalkOk();
            }
        }

        @Override
        public void OnRtcTalkCouldSpeak() {
            if (maxEvent!=null){
                maxEvent.onRTCTalkCouldSpeak();
            }
        }

        @Override
        public void OnRtcTalkOn(String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCTalkOn(userId,userData);
            }
        }

        @Override
        public void OnRtcTalkP2POn(String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCTalkP2POn(userId,userData);
            }
        }

        @Override
        public void OnRtcTalkP2POff(String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCTalkP2POff(userData);
            }
        }

        @Override
        public void OnRtcTalkClosed(int nCode, String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCTalkClosed(nCode,userId,userData);
            }
        }

        @Override
        public void OnRtcVideoMonitorRequest(String userId, String userData) {
            if (maxEvent!=null){
            maxEvent.onRTCVideoMonitorRequest(userId,userData);
            }
        }

        @Override
        public void OnRtcVideoMonitorClose(String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCVideoMonitorClose(userId,userData);
            }
        }

        @Override
        public void OnRtcVideoMonitorResult(String userId, int nCode, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCVideoMonitorResult(userId,nCode,userData);
            }
        }

        @Override
        public void OnRtcVideoReportRequest(String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCVideoReportRequest(userId,userData);
            }
        }

        @Override
        public void OnRtcVideoReportClose(String userId) {
            if (maxEvent!=null){
                maxEvent.onRTCVideoReportClose(userId);
            }
        }

        @Override
        public void OnRtcMakeCallOK(String callId) {
            if (maxEvent!=null){
                maxEvent.onRTCMakeCallOK(callId);
            }
        }

        @Override
        public void OnRtcAcceptCall(String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCAcceptCall(userId,userData);
            }
        }

        @Override
        public void OnRtcRejectCall(String userId, int nCode, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCRejectCall(userId,nCode,userData);
            }
        }

        @Override
        public void OnRtcLeaveCall(String userId) {
            if (maxEvent!=null){
                maxEvent.onRTCLeaveCall(userId);
            }
        }

        @Override
        public void OnRtcReleaseCall(String callId) {
            if (maxEvent!=null){
                maxEvent.onRTCReleaseCall(callId);
            }
        }

        @Override
        public void OnRtcMakeCall(String callId, int nCallType, String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCMakeCall(callId,nCallType,userId,userData);
            }
        }

        @Override
        public void OnRtcEndCall(String callId, String userId, int nCode) {
            if (maxEvent!=null){
                maxEvent.onRTCEndCall(callId,userId,nCode);
            }
        }

        @Override
        public void OnRtcOpenVideoRender(String strRtcPeerId, String publishId, String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCOpenRemoteVideoRender(strRtcPeerId,publishId,userId,userData);
            }
        }

        @Override
        public void OnRtcCloseVideoRender(String strRtcPeerId, String publishId, String userId) {
            if (maxEvent!=null){
                maxEvent.onRTCCloseRemoteVideoRender(strRtcPeerId,publishId,userId);
            }
        }

        @Override
        public void OnRtcOpenAudioTrack(String strRtcPeerId, String userId, String userData) {
            if (maxEvent!=null){
                maxEvent.onRTCOpenRemoteAudioTrack(strRtcPeerId,userId,userData);
            }
        }

        @Override
        public void OnRtcCloseAudioTrack(String strRtcPeerId, String userId) {
            if (maxEvent!=null){
                maxEvent.onRTCCloseRemoteAudioTrack(strRtcPeerId,userId);
            }
        }

        @Override
        public void OnRtcAVStatus(String strRtcPeerId, boolean bAudio, boolean bVideo) {
            if (maxEvent!=null){
                if (strRtcPeerId.equals("RTCMainParticipanter")) {
                    maxEvent.onRTCLocalAVStatus(bAudio, bVideo);
                } else {
                    maxEvent.onRTCRemoteAVStatus(strRtcPeerId, bAudio, bVideo);
                }
            }
        }

        @Override
        public void OnRtcAudioActive(String peerId, String userId, int nLevel, int nShowtime) {
            if (maxEvent!=null){
                if (peerId.equals("RtcPublisher")) {
                    maxEvent.onRTLocalAudioActive(nLevel, nShowtime);
                } else {
                    maxEvent.onRTCRemoteAudioActive(peerId,userId, nLevel, nShowtime);
                }
            }
        }

        @Override
        public void OnRtcNetworkStatus(String strRtcPeerId, String userId, int nNetSpeed, int nPacketLost) {
            if (maxEvent!=null){
                ARNetQuality netQuality = null;
                if (nPacketLost <= 1) {
                    netQuality = ARNetQuality.ARNetQualityExcellent;
                } else if (nPacketLost > 1 && nPacketLost <= 3) {
                    netQuality = ARNetQuality.ARNetQualityGood;
                } else if (nPacketLost > 3 && nPacketLost <= 5) {
                    netQuality = ARNetQuality.ARNetQualityAccepted;
                } else if (nPacketLost > 5 && nPacketLost <= 10) {
                    netQuality = ARNetQuality.ARNetQualityBad;
                } else {
                    netQuality = ARNetQuality.ARNetQualityVBad;
                }
                if (strRtcPeerId.equals("RtcPublisher")) {
                    maxEvent.onRTCLocalNetworkStatus(nNetSpeed, nPacketLost, netQuality);
                } else {
                    maxEvent.onRTCRemoteNetworkStatus(strRtcPeerId, userId,nNetSpeed, nPacketLost, netQuality);
                }
            }
        }

        @Override
        public void OnRtcUserMessage(String userId, String strUserName, String strUserHeader, String strContent) {
            if (maxEvent!=null){
                maxEvent.onRTCUserMessage(userId,strUserName,strUserHeader,strContent);
            }
        }

        @Override
        public void OnRtcMemberNum(int nNum) {
            if (maxEvent!=null){
                maxEvent.onRTCMemberNum(nNum);
            }
        }

        @Override
        public void OnRtcUserDataNotify(String userData) {
        }

        @Override
        public void OnRtcGotRecordFile(int nRecType, String userData, String strFilePath) {
            if (maxEvent!=null){
                maxEvent.onRTCGotRecordFile(nRecType,userData,strFilePath);
            }
        }
    };

    /**
     * Jni interface
     */
    private native long nativeCreate(Object obj);
    private native void nativeSetUserToken(String strUserToken);

    private native void nativeSetDeviceInfo(String strDevInfo);
    private native void nativeForceSetAecEnable(boolean bEnable);
    private native void nativeSetLocalAudioEnable(boolean enabled);
    private native void nativeSetLocalVideoEnable(boolean enabled);
    private native void nativeSetRemoteAVEnable(String strUserId, boolean bAudioEnabled, boolean bVideoEnabled);
    private native void nativeSetRemotePeerAVEnable(String strPeerId, boolean bAudioEnabled, boolean bVideoEnabled);

    private native void nativeSetLocalPeerAudioEnable(String publishId, boolean audioEnable);

    private native void nativeSetLocalPeerVideoEnable(String publishId, boolean videoEnable);

    private native long nativeGetAnyrtcUvcCallabck();
    private native void nativeSetUvcVideoCapturer(Object capturer, String strImg);
    private native void nativeSetLocalVideoCapturer(VideoCapturer capturer, long nativeRenderer);
    private native void nativeSetLocalVideoSize(int width, int height);
    private native void nativeSetLocalVideoModeExcessive(int mode);

    private native void nativeSetLocalVideoBitrate(int bitrate);

    private native void nativeSetLocalVideoFps(int fps);

    private native void nativeSetLocalVideoProfileMode(int nVideoMode);

    private native void nativeSetLocalVideoFpsProfile(int nFpsMode);

    private native void nativeSetScreenToLandscape();

    private native void nativeSetScreenToPortrait();
    private native void nativeSetRecordPath(String strPath, String strTalkPath, String strTalkP2PPath);
    private native int nativeJoin(String strAnyrtcId, String strUserId, String strCustomData);
    private native int nativeReJoin(String strAnyrtcId, String strCustomData);
    private native void nativeLeave();
    private native int nativeApplyTalk(int nPriority);
    private native void nativeCancelTalk();
    private native void nativeTalkP2PClose();
    private native int nativeMonitorVideo(String strUserId, String strCustomData);
    private native int nativeAcceptVideoMonitor(String strHostUserId);
    private native int nativeRejectVideoMonitor(String strHostUserId);
    private native void nativeCloseVideoMonitor(String strUserId);
    private native int nativeReportVideo(String strUserId);
    private native void nativeReportVideoClose();
    private native int nativeMakeCall(String strUserId, int nType/*0:视频 1:音频*/, String strCustomData);
    private native int nativeInviteCall(String strUserId, String strCustomData);
    private native void nativeEndCall(String strUserId);
    private native int nativeAcceptCall(String strCallId);
    private native void nativeRejectCall(String strCallId);
    private native void nativeLeaveCall();
    private native void nativeSetRTCVideoRender(String strRtcPubId, long nativeRenderer);
    private native boolean nativeSendUserMsg(String strCustomName, String strCustomHeaderUrl, String strContent);
    //private native boolean nativeBroadcastUserMsg(String strCustomName, String strCustomHeaderUrl, String strContent);
    private native void nativeDestroy();


    private native void nativeSetAuidoModel(boolean enabled, boolean audioDetect);
    private native void nativeSetCameraMirror(boolean bEnable);
    private native void nativeSetNetworkStatus(boolean bEnable);
    private native boolean nativeNetworkStatusEnabled();
}
