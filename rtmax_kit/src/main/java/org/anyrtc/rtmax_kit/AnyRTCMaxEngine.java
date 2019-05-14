package org.anyrtc.rtmax_kit;

import android.content.Context;

import org.anyrtc.common.utils.DeviceUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.anyrtc.common.utils.NetworkUtils;
import org.ar.rtmax_kit.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

import java.util.concurrent.Exchanger;

/**
 *
 * @author Eric
 * @date 2017/12/8
 */
@Deprecated
public class AnyRTCMaxEngine {
    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("rtmax-jni");
    }
    private final LooperExecutor executor;
    private final EglBase eglBase;
    private Context context;
    private boolean mIsInit;
    private String developerId, appId, appKey, appToken;
    private String strSvrAddr = "cloud.anyrtc.io";

    private static class SingletonHolder {
        private static final AnyRTCMaxEngine INSTANCE = new AnyRTCMaxEngine();
    }

    public static final AnyRTCMaxEngine Inst() {
        return SingletonHolder.INSTANCE;
    }

    private AnyRTCMaxEngine() {
        executor = new LooperExecutor();
        eglBase = EglBase.create();
//        disableHWEncode();
//        disableHWDecode();
        executor.requestStart();
    }

    public LooperExecutor Executor() {
        return executor;
    }

    public EglBase egl() {
        return eglBase;
    }

    public Context getContext() {
        return context;
    }

    /**
     * 禁用硬件编码
     */
    public static void disableHWEncode() {
        MediaCodecVideoEncoder.disableVp8HwCodec();
        MediaCodecVideoEncoder.disableVp9HwCodec();
        MediaCodecVideoEncoder.disableH264HwCodec();
    }

    /**
     * 禁用硬件解码
     */
    public static void disableHWDecode() {
        MediaCodecVideoDecoder.disableVp8HwCodec();
        MediaCodecVideoDecoder.disableVp9HwCodec();
        MediaCodecVideoDecoder.disableH264HwCodec();
    }

    public boolean ismIsInit() {
        return mIsInit;
    }

    /**
     * 初始化anyRTC平台信息
     * @param ctx application Context
     * @param bUseJavaRecord 是否使用Java录音采集模式
     * @param strDeveloperId anyRTC开发者id
     * @param strAppId anyRTC应用的appid
     * @param strKey anyRTC应用的appkey
     * @param strToken anyRTC应用的apptoken
     */
    public void initEngineWithAnyrtcInfo(final Context ctx, final boolean bUseJavaRecord, final String strDeveloperId, final String strAppId,
                                         final String strKey, final String strToken) {
        if(!AnyRTCMaxEngine.Inst().ismIsInit()) {
            ContextUtils.initialize(ctx);
            mIsInit = true;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                developerId = strDeveloperId;
                appId = strAppId;
                appKey = strKey;
                appToken = strToken;
                context = ctx;
                nativeInitCtx(ctx, eglBase.getEglBaseContext(), bUseJavaRecord);
                nativeInitEngineWithAnyrtcInfo(strDeveloperId, strAppId, strKey, strToken);
            }
        });
    }

    /**
     * 初始化应用信息
     * @param ctx
     * @param strAppId
     * @param strToken
     */
    public void initEngineWithAppInfo(final Context ctx, final boolean bUseJavaRecord, final String strAppId, final String strToken) {
        if(!AnyRTCMaxEngine.Inst().ismIsInit()) {
            ContextUtils.initialize(ctx);
            mIsInit = true;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                appId = strAppId;
                appToken = strToken;
                context = ctx;
                nativeInitCtx(ctx, eglBase.getEglBaseContext(), bUseJavaRecord);
                nativeInitEngineWithAppInfo(strAppId, strToken);
            }
        });
    }

    /**
     * 获取应用的包名
     * @return 应用的package包名
     */
    public String getPackageName() {
        return context.getPackageName();
    }

    /**
     * 设置私有云地址
     * @param strAddr 私有云ip地址或者域名
     * @param nPort 端口
     */
    public void configServerForPriCloud(final String strAddr, final int nPort) {
        strSvrAddr = strAddr;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeConfigServerForPriCloud(strAddr, nPort);
            }
        });
    }

    public void setAuidoModel(final boolean bEnabled, final boolean bAudioDetect) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAuidoModel(bEnabled, bAudioDetect);
            }
        });
    }

    /**
     * 打开或关闭前置摄像头镜面
     * @param bEnable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean bEnable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetCameraMirror(bEnable);
            }
        });
    }
	
    /**
     * 打开或关闭网络状态监测
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setNetworkStatus(final boolean bEnable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetNetworkStatus(bEnable);
            }
        });
    }

    /**
     * 网络监测是否打开
     * @return true:可用， false：不可用
     */
    public boolean networkStatusEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeNetworkStatusEnabled();
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    public void dispose() {
        executor.requestStop();
    }

    /**
     * 获取sdk版本号
     * @return sdk版本号
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }
    /**
     * 获取设备信息
     * @return
     */
    protected String getDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operatorName", NetworkUtils.getNetworkOperatorName());
            jsonObject.put("devType", DeviceUtils.getModel());
            jsonObject.put("networkType", NetworkUtils.getNetworkType().toString().replace("NETWORK_", ""));
            jsonObject.put("osType", "Android");
            jsonObject.put("sdkVer", getSdkVersion());
            jsonObject.put("rtcVer", 60);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * Jni interface
     */
    private static native void nativeInitCtx(Context ctx, EglBase.Context context, boolean bUseJavaRecord);
    private static native void nativeInitEngineWithAnyrtcInfo(String strDeveloperId, String strAppId,
                                                              String strAESKey, String strToken);
    private static native void nativeInitEngineWithAppInfo(String strAppId, String strToken);
    private static native void nativeConfigServerForPriCloud(String strAddr, int nPort);
    private static native void nativeSetAuidoModel(boolean enabled, boolean audioDetect);
    private static native void nativeSetCameraMirror(boolean bEnable);

    private static native void nativeSetNetworkStatus(boolean bEnable);

    private static native boolean nativeNetworkStatusEnabled();
}
