package org.anyrtc.rtmax_kit;

import android.support.annotation.RequiresPermission;
import android.util.Log;

import org.anyrtc.common.enums.AnyRTCVideoQualityMode;
import org.anyrtc.common.utils.AnyRTCUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Exchanger;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static org.anyrtc.rtmax_kit.ARMaxConstans.AUDIO_SETTING;
import static org.anyrtc.rtmax_kit.ARMaxConstans.AV_ENABLE;
import static org.anyrtc.rtmax_kit.ARMaxConstans.CMD;
import static org.anyrtc.rtmax_kit.ARMaxConstans.TARGET_USERID;
import static org.anyrtc.rtmax_kit.ARMaxConstans.VIDEO_SETTING;

/**
 *
 * @author Eric
 * @date 2017/12/8
 */
@Deprecated
public class RTMaxKit {
    private static final String TAG = "RTMaxKit";

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

    @Deprecated
    public RTMaxKit(final RTMaxHelper rtMaxHelper) {
        AnyRTCUtils.assertIsTrue(rtMaxHelper != null);

        mExecutor = AnyRTCMaxEngine.Inst().Executor();
        mEglBase = AnyRTCMaxEngine.Inst().egl();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(rtMaxHelper);
            }
        });
    }

    public RTMaxKit(final RTMaxEvent rtMaxEvent) {
        AnyRTCUtils.assertIsTrue(rtMaxEvent != null);

        mExecutor = AnyRTCMaxEngine.Inst().Executor();
        mEglBase = AnyRTCMaxEngine.Inst().egl();

        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(rtMaxEvent);
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
                if(mVideoCapturer != null) {
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
                if(mVideoCapturer != null) {
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
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    public boolean setUserToken(final String strUserToken) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                if(null == strUserToken || strUserToken.equals("")) {
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
     * @param bEnabled true：本地音频可用，false：本地音频不可用
     */
    public void setLocalAudioEnable(final boolean bEnabled){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalAudioEnable(bEnabled);
            }
        });
    }

    /**
     * 设置远端音频是否可用
     * @param strUserid 对方userid
     * @param bEnabled true：远端音频可用，false：远端音频不可用
     * @return 0:成功， 1：strUserid是空
     */
    private int setRemoteAudioEnable(final String strUserid, final boolean bEnabled){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        if(null == strUserid || strUserid.length() == 0) {
            return 1;
        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(CMD, AUDIO_SETTING);
                        jsonObject.put(TARGET_USERID, strUserid);
                        jsonObject.put(AV_ENABLE, bEnabled);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //nativeSendUserMsg(strUserid, "", jsonObject.toString());
                    LooperExecutor.exchange(result, 0);
                }
            });
        }
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 设置本地视频是否可用
     * @param bEnabled true：本地视频可用，false：本地视频不可用
     */
    public void setLocalVideoEnable(final boolean bEnabled){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoEnable(bEnabled);
            }
        });
    }

    /**
     * 打开关闭摄像头闪光灯 true 打开  false关闭
     * @param BOpen
     */
    public void openCameraTorchMode(final boolean BOpen){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mVideoCapturer.openCameraTorchMode(BOpen);
            }
        });
    }

    /**
     * 设置远端用户是否可以接收自己的音视频
     * @param strUserId 远端用户UserId
     * @param bAudioEnabled true：音频可用，false：音频不可用
     * @param bVideoEnabled true：视频可用，false：视频不可用
     */
    public void setRemoteAVEnable(final String strUserId, final boolean bAudioEnabled, final boolean bVideoEnabled){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRemoteAVEnable(strUserId, bAudioEnabled, bVideoEnabled);
            }
        });
    }

    /**
     * 设置不接收指定通道的音视频
     * @param strPeerId 视频通道的PeerId或者PubId
     * @param bAudioEnabled true：音频可用，false：音频不可用
     * @param bVideoEnabled true：视频可用，false：视频不可用
     */
    public void setRemotePeerAVEnable(final String strPeerId, final boolean bAudioEnabled, final boolean bVideoEnabled){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRemotePeerAVEnable(strPeerId, bAudioEnabled, bVideoEnabled);
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
     * 设置远端视频是否可用
     * @param strUserid 对方userid
     * @param bEnabled true：远端视频可用，false：远端视频不可用
     * @return 0:成功， 1：strUserid是空
     */
    private int setRemoteVideoEnable(final String strUserid, final boolean bEnabled){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        if(null == strUserid || strUserid.length() == 0) {
            return 1;
        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(CMD, VIDEO_SETTING);
                        jsonObject.put(TARGET_USERID, strUserid);
                        jsonObject.put(AV_ENABLE, bEnabled);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //nativeSendUserMsg(strUserid, "", jsonObject.toString());
                    LooperExecutor.exchange(result, 0);
                }
            });
        }
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 设置本地视频采集的分辨率大小
     * @param width 视频的宽
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
     * @param renderPointer 底层图像地址
     * @param isFront 是否是前置摄像头；true：前置摄像头，false:后置摄像头
     * @param anyRTCVideoMode 视频采集大小；参考AnyRTCVideoQualityMode
     * @param eventsHandler 相机打开事件回调
     * @return 0：摄像头打开成功， 1：摄像头打开失败
     */
    @RequiresPermission(CAMERA)
    public int setLocalVideoCapturer(final long renderPointer, final boolean isFront, final AnyRTCVideoQualityMode
            anyRTCVideoMode, final CameraVideoCapturer.CameraEventsHandler eventsHandler){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer == null) {
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

                    if (null != anyRTCVideoMode) {
//                        if (videoMode == AnyRTCVideoMode.AnyRTC_Video_HHD) {
//                            nativeSetLocalVideoSize(1920, 1080);
//                        } else if (videoMode == AnyRTCVideoMode.AnyRTC_Video_HD) {
//                            nativeSetLocalVideoSize(1280, 720);
//                        } else if (videoMode == AnyRTCVideoMode.AnyRTC_Video_QHD) {
//                            nativeSetLocalVideoSize(960, 540);
//                        } else if (videoMode == AnyRTCVideoMode.AnyRTC_Video_SD) {
//                            nativeSetLocalVideoSize(640, 480);
//                        } else if (videoMode == AnyRTCVideoMode.AnyRTC_Video_Low) {
//                            nativeSetLocalVideoSize(355, 288);
//                        } else if (videoMode == AnyRTCVideoMode.AnyRTC_Video_FLow) {
//                            nativeSetLocalVideoSize(320, 240);
//                        }

                        nativeSetLocalVideoModeExcessive(anyRTCVideoMode.level);
                    }

                    nativeSetLocalVideoCapturer(mVideoCapturer, renderPointer);
                    LooperExecutor.exchange(result, 0);
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

    /**
     * 设置USB相机相机
     * @param usbCamera usb相机
     * @return
     */
    @RequiresPermission(CAMERA)
    public int setUvcVideoCapturer(final Object usbCamera){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer == null) {
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
     * @param isUsb 是否是usb相机（当为true时，为采用usb相机，当为false时，为采用本机相机）
     * @param usbCamera usb相机
     * @param renderPointer 本机相机的底层图像地址
     * @param isFront 本机相机是否是前置摄像头
     * @param eventsHandler 相机打开事件回调
     * @return 0：摄像头打开成功， 1：摄像头打开失败
     */
    @RequiresPermission(CAMERA)
    public int selectCamera(final boolean isUsb, final Object usbCamera, final long renderPointer,
                            final boolean isFront, final CameraVideoCapturer.CameraEventsHandler eventsHandler) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(isUsb) {
                    //本地相机置空
                    if(mVideoCapturer != null) {
                        try {
                            mVideoCapturer.stopCapture();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        nativeSetLocalVideoCapturer(null, 0);
                        mVideoCapturer = null;
                    }
                    if(mVideoCapturer == null) {
                        mCameraId = 0;
                        nativeSetUvcVideoCapturer(usbCamera, "");
                        LooperExecutor.exchange(result, 0);
                    }
                } else {
                    //USB相机置空
                    if(mVideoCapturer != null) {
                        nativeSetUvcVideoCapturer(null, "");
                        mVideoCapturer = null;
                    }

                    //加载本地相机
                    if(mVideoCapturer == null) {
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
                        nativeSetLocalVideoCapturer(mVideoCapturer, renderPointer);
                        LooperExecutor.exchange(result, 0);
                    }
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

    public long getAnyrtcUvcCallabck() {
        return nativeGetAnyrtcUvcCallabck();
    }

    /**
     * 切换摄像头
     */
    @RequiresPermission(CAMERA)
    public void switchCamera(){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(null);
                }
            }
        });
    }

    /**
     * 切换摄像头
     */
    @RequiresPermission(CAMERA)
    public void switchCamera(final CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(cameraSwitchHandler);
                }
            }
        });
    }

    /**
     * 加入对讲组
     * @param strAnyrtcId 对讲组id（同一个anyrtc平台的appid内保持唯一性）
     * @param strUserId 用户的第三方平台的用户id
     * @param strUserData 用户的自定义数据
     * @return 0：成功，1：失败；2：strCustomData 大于512
     */
    @Deprecated
    @RequiresPermission(RECORD_AUDIO)
    public int joinRTC(final String strAnyrtcId, final String strUserId, final String strUserData){
        mUserid = strUserId;
        try {
            if(strUserData.getBytes("utf-8").length > 512) {
                return 2;
            } else {
                final Exchanger<Integer> result = new Exchanger<Integer>();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mCurAnyRTCId = strAnyrtcId;
                        nativeSetDeviceInfo(AnyRTCMaxEngine.Inst().getDeviceInfo());
                        int ret = nativeJoin(strAnyrtcId, strUserId, strUserData);
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
     * 加入对讲组
     * @param strGroupId strGroupId 对讲组id（同一个anyrtc平台的appid内保持唯一性）
     * @param strUserId 用户的第三方平台的用户id
     * @param strUserData 用户的自定义数据
     * @return -1：strGroupId为空；0：成功，1：失败；2：strUserData 大于512
     */
    @RequiresPermission(RECORD_AUDIO)
    public int joinTalkGroup(final String strGroupId, final String strUserId, final String strUserData){
        mUserid = strUserId;
        try {
            if(strUserData.getBytes("utf-8").length > 512) {
                return 2;
            } else {
                final Exchanger<Integer> result = new Exchanger<Integer>();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mCurAnyRTCId = strGroupId;
                        nativeSetDeviceInfo(AnyRTCMaxEngine.Inst().getDeviceInfo());
                        int ret = nativeJoin(strGroupId, strUserId, strUserData);
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
     * @param strAnyrtcId 对讲组id（同一个anyrtc平台的appid内保持唯一性）
     * @param strUserData strUserData 自定义数据
     * @return 0：成功；1：失败；2：strUserData 大于512字节
     */
    @Deprecated
    @RequiresPermission(RECORD_AUDIO)
    public int reJoinRTC(final String strAnyrtcId, final String strUserData){
        try {
            if(strUserData.getBytes("utf-8").length > 512) {
                return 2;
            } else {
                final Exchanger<Integer> result = new Exchanger<Integer>();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int ret = 1;
                        mCurAnyRTCId = strAnyrtcId;
                        ret = nativeReJoin(strAnyrtcId, strUserData);
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
     * @param strGroupId 对讲组id（同一个anyrtc平台的appid内保持唯一性）
     * @param strUserData strUserData 自定义数据
     * @return -2：切换对讲组失败，-1：strGroupId为空，0：成功；2：strUserData 大于512字节
     */
    @RequiresPermission(RECORD_AUDIO)
    public int switchTalkGroup(final String strGroupId, final String strUserData){
        try {
            if(strUserData.getBytes("utf-8").length > 512) {
                return 2;
            } else {
                final Exchanger<Integer> result = new Exchanger<Integer>();
                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int ret = -2;
                        mCurAnyRTCId = strGroupId;
                        ret = nativeReJoin(strGroupId, strUserData);
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
    @Deprecated
    public void leaveRTC() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeLeave();
                mCurAnyRTCId = null;
            }
        });
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
     * @param nPriority 申请抢麦用户的级别（0权限最大（数值越大，权限越小）；除0意外，可以后台设置0-10之间的抢麦权限大小））
     * @return 0: 调用OK  -1:未登录  -2:正在对讲中  -3: 资源还在释放中 -4: 操作太过频繁
     */
    @RequiresPermission(RECORD_AUDIO)
    public int applyTalk(final int nPriority) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeApplyTalk(nPriority);
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
     * @param strPath 呼叫文件的保存路径（文件夹路径）
     * @param strTalkPath 对讲文件保存路径（文件夹路径）
     * @param strTalkP2PPath 强插P2P文件保存路径（文件夹路径）
     * @return 0/1:设置成功/文件夹不存在
     */
    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    public int setRecordPath(final String strPath, final String strTalkPath, final String strTalkP2PPath) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        if(null == strPath) {
            return 1;
        } else {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    int ret = 0;
                    File file = new File(strPath);
                    if (file.exists()) {
                        nativeSetRecordPath(strPath, strTalkPath, strTalkP2PPath);
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
     * @param strUserId 被叫用户userid
     * @param nType 0:视频 1:音频
     * @param strUserData 自定义数据
     * @return 0:调用OK;-1:未登录;-2:没有通话-3:视频资源占用中;-5:本操作不支持自己对自己;-6:会话未创建（没有被呼叫用户）
     */
    @RequiresPermission(RECORD_AUDIO)
    public int makeCall(final String strUserId, final int nType/*0:视频 1:音频*/, final String strUserData) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeMakeCall(strUserId, nType, strUserData);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 呼叫邀请：视频/音频通话建立成功后，主叫邀请其他人加入通话
     * @param strUserId 被邀请用户userid
     * @param strUserData 自定义数据
     * @return 0:调用OK;-1:未登录;-2:没有通话-3:视频资源占用中;-5:本操作不支持自己对自己;-6:会话未创建（没有被呼叫用户）
     */
    @RequiresPermission(RECORD_AUDIO)
    public int inviteCall(final String strUserId, final String strUserData) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeInviteCall(strUserId, strUserData);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 主叫端结束某一路正在进行的通话
     * @param strUserId 指定用户userid
     */
    public void endCall(final String strUserId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeEndCall(strUserId);
            }
        });
    }

    /**
     * 接受呼叫或通话邀请
     * @param strCallId 呼叫请求时收到的callId
     * @return 0:调用OK;-1:未登录;-2:会话不存在-3:视频资源占用中;
     */
    @RequiresPermission(RECORD_AUDIO)
    public int acceptCall(final String strCallId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeAcceptCall(strCallId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 拒绝呼叫
     * @param strCallId 呼叫请求时收到的callId
     */
    public void rejectCall(final String strCallId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeRejectCall(strCallId);
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
     * @param strUserId 被监看用户userId
     * @param strUserData 自定义数据
     * @return 返回值  0: 调用OK  -1:未登录	-5:本操作不支持自己对自己
     */
    @RequiresPermission(CAMERA)
    public int monitorVideo(final String strUserId, final String strUserData) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeMonitorVideo(strUserId, strUserData);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 同意视频监看
     * @param strHostUserId 发起人的userid
     * @return 返回值  0: 调用OK  -1:未登录 -3:视频资源占用中 -5:本操作不支持自己对自己
     */
    @RequiresPermission(CAMERA)
    public int acceptVideoMonitor(final String strHostUserId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeAcceptVideoMonitor(strHostUserId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 拒绝视频监看
     * @param strHostUserId 发起人的userid
     * @return 返回值  0: 调用OK  -1:未登录 -3:视频资源占用中 -5:本操作不支持自己对自己
     */
    public int rejectVideoMonitor(final String strHostUserId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeRejectVideoMonitor(strHostUserId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 监看发起者关闭视频监看
     * @param strUserId 被监看用户的userid
     */
    public void closeVideoMonitor(final String strUserId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeCloseVideoMonitor(strUserId);
            }
        });
    }

    /**
     * 视频上报（接收端收到上报请求时，调用monitorVideo进行视频查看）
     * @param strUserId 用户的userid
     * @return 返回值  0: 调用OK  -1:未登录 -3:视频资源占用中	-5:本操作不支持自己对自己
     */
    @RequiresPermission(CAMERA)
    public int reportVideo(final String strUserId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = nativeReportVideo(strUserId);
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 上报者关闭视频上报
     */
    @Deprecated
    public void reportVideoClose() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeReportVideoClose();
            }
        });
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
      
     示例：VideoRenderer render = mVideoView.OnRtcOpenRemoteRender("strRtcPeerId", RendererCommon.ScalingType.SCALE_ASPECT_FIT);
           mRTMaxKit.setRTCVideoRender(strRtcPeerId,render.GetRenderPointer());
     * @param strRtcPubId RTC服务生成的连麦者标识Id 。(用于标识连麦用户，每次连麦随机生成)
     * @param lRender SDK底层视频显示对象  （通过VideoRenderer对象获取，参考连麦窗口管理对象-添加连麦窗口渲染器方法）
     */
    public void setRTCVideoRender(final String strRtcPubId, final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRender(strRtcPubId, lRender);
            }
        });
    }

    /**
     * 小组内发送消息
     * @param strUserId 指定用户的userid（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url（最大512个字节）
     * @param strContent 消息内容（最大256个字节）
     * @return 返回结果，0：成功；1：失败；4：参数非法；如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    public int sendUserMessage(final String strUserId, final String strUserHeaderUrl, final String strContent) {
        if(strUserId.getBytes().length > 384) {
            return 4;
        }
        if(strUserHeaderUrl.getBytes().length > 1536) {
            return 4;
        }
        if(strContent.getBytes().length > 1536) {
            return 4;
        }

        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String headUrl = strUserHeaderUrl;
                if (headUrl.length() == 0) {
                    headUrl = "strCustomHeaderUrl can't be empty string";
                }
                boolean ret = false;
                ret = nativeSendUserMsg(strUserId, headUrl, strContent);
                LooperExecutor.exchange(result, ret ? 0 : 1);
            }
        });
        return LooperExecutor.exchange(result, 1);
    }


    /**
     * 小组内广播文本信息
     * @param strUserName 消息发送者的业务平台昵称（最大256个字节）
     * @param strUserHeaderUrl 消息发送者的业务平台的头像url（最大512个字节）
     * @param strContent 消息内容（最大256个字节）
     * @return 返回结果，0：成功；1：失败；4：参数非法；如果joinRTCLine时没有设置strCustomId或者消息发送失败，返回false，发送成功则返回true。
     */
    public int broadcastUserMsg(final String strUserName, final String strUserHeaderUrl, final String strContent) {
        if(strUserName.getBytes().length > 384) {
            return 4;
        }
        if(strUserHeaderUrl.getBytes().length > 1536) {
            return 4;
        }
        if(strContent.getBytes().length > 1536) {
            return 4;
        }

        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String headUrl = strUserHeaderUrl;
                if (headUrl.length() == 0) {
                    headUrl = "strCustomHeaderUrl can't be empty string";
                }
                boolean ret = false;
//                ret = nativeBroadcastUserMsg(strUserName, headUrl, strContent);
                ret = nativeSendUserMsg(strUserName, headUrl, strContent);
                LooperExecutor.exchange(result, ret ? 0 : 1);
            }
        });
        return LooperExecutor.exchange(result, 1);
    }


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

}
