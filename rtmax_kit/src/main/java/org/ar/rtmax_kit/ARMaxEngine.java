package org.ar.rtmax_kit;

import android.content.Context;

import org.ar.common.enums.ARLogLevel;
import org.ar.common.utils.DeviceUtils;
import org.ar.common.utils.LooperExecutor;
import org.ar.common.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

public class ARMaxEngine {

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
    private ARMaxOption arMaxOption = new ARMaxOption();
    private static class SingletonHolder {
        private static final ARMaxEngine INSTANCE = new ARMaxEngine();
    }

    public static final ARMaxEngine Inst() {
        return ARMaxEngine.SingletonHolder.INSTANCE;
    }

    private ARMaxEngine() {
        executor = new LooperExecutor();
        eglBase = EglBase.create();
//        disableHWEncode();
//        disableHWDecode();
        executor.requestStart();
    }

    public ARMaxOption getArMaxOption() {
        return arMaxOption;
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
    public  void disableHWEncode() {
        MediaCodecVideoEncoder.disableVp8HwCodec();
        MediaCodecVideoEncoder.disableVp9HwCodec();
        MediaCodecVideoEncoder.disableH264HwCodec();
    }

    /**
     * 禁用硬件解码
     */
    public  void disableHWDecode() {
        MediaCodecVideoDecoder.disableVp8HwCodec();
        MediaCodecVideoDecoder.disableVp9HwCodec();
        MediaCodecVideoDecoder.disableH264HwCodec();
    }

    public boolean ismIsInit() {
        return mIsInit;
    }

    /**
     * 初始化anyRTC平台信息
     *
     * @param ctx            application Context
     * @param bUseJavaRecord 是否使用Java录音采集模式
     * @param strDeveloperId anyRTC开发者id
     * @param strAppId       anyRTC应用的appid
     * @param strKey         anyRTC应用的appkey
     * @param strToken       anyRTC应用的apptoken
     */
    public void initEngineWithARInfo(final Context ctx, final boolean bUseJavaRecord, final String strDeveloperId, final String strAppId,
                                         final String strKey, final String strToken) {
        if (!ARMaxEngine.Inst().ismIsInit()) {
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
                nativeInitEngineWithARInfo(strDeveloperId, strAppId, strKey, strToken);
            }
        });
    }

    /**
     *
     * @param ctx
     * @param bUseJavaRecord
     * @param strAppId
     * @param strToken
     */
    public void initEngineWithARInfo(final Context ctx, final boolean bUseJavaRecord, final String strAppId, final String strToken) {
        if (!ARMaxEngine.Inst().ismIsInit()) {
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
     *
     * @return 应用的package包名
     */
    public String getPackageName() {
        return context.getPackageName();
    }

    /**
     * 设置私有云地址
     *
     * @param strAddr 私有云ip地址或者域名
     * @param nPort   端口
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




    private void dispose() {
        executor.requestStop();
    }

    /**
     * 获取sdk版本号
     *
     * @return sdk版本号
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 设置日志显示级别
     *
     * @param logLevel 日志显示级别
     */
    public void setLogLevel(final ARLogLevel logLevel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLogLevel(logLevel.type);
            }
        });
    }

    /**
     * 获取设备信息
     *
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

    private static native void nativeInitEngineWithARInfo(String strDeveloperId, String strAppId,
                                                              String strAESKey, String strToken);
    private static native void nativeInitEngineWithAppInfo(String strAppId, String strToken);

    private static native void nativeConfigServerForPriCloud(String strAddr, int nPort);

    private static native void nativeSetLogLevel(int logLevel);
}
