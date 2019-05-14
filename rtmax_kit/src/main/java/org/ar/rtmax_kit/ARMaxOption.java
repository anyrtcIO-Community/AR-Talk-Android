package org.ar.rtmax_kit;

import org.ar.common.enums.ARVideoCommon;

/**
 * Created by liuxiaozhong on 2019/1/24.
 */
public class ARMaxOption {
    /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    private boolean isDefaultFrontCamera = true;
    /**
     * anyRTC屏幕方向；默认：竖屏
     */
    private ARVideoCommon.ARVideoOrientation mScreenOriention = ARVideoCommon.ARVideoOrientation.Portrait;
    /**
     * anyRTC视频清晰标准；默认：标清（AnyRTC_Video_SD）
     */
    private ARVideoCommon.ARVideoProfile videoProfile = ARVideoCommon.ARVideoProfile.ARVideoProfile360x640;
    /**
     * anyRTC视频帧率；默认：15帧（ARVideoFrameRateFps15）
     */
    private ARVideoCommon.ARVideoFrameRate videoFps = ARVideoCommon.ARVideoFrameRate.ARVideoFrameRateFps15;


    public void setOptionParams(boolean isDefaultFrontCamera, ARVideoCommon.ARVideoOrientation mScreenOriention, ARVideoCommon.ARVideoProfile videoProfile, ARVideoCommon.ARVideoFrameRate videoFps) {
        this.isDefaultFrontCamera = isDefaultFrontCamera;
        this.mScreenOriention = mScreenOriention;
        this.videoProfile = videoProfile;
        this.videoFps = videoFps;
    }

    public ARMaxOption() {
    }

    protected boolean isDefaultFrontCamera() {
        return isDefaultFrontCamera;
    }

    public void setDefaultFrontCamera(boolean defaultFrontCamera) {
        isDefaultFrontCamera = defaultFrontCamera;
    }

    protected ARVideoCommon.ARVideoOrientation getScreenOriention() {
        return mScreenOriention;
    }

    public void setScreenOriention(ARVideoCommon.ARVideoOrientation mScreenOriention) {
        this.mScreenOriention = mScreenOriention;
    }

    protected ARVideoCommon.ARVideoProfile getVideoProfile() {
        return videoProfile;
    }

    public void setVideoProfile(ARVideoCommon.ARVideoProfile videoProfile) {
        this.videoProfile = videoProfile;
    }

    protected ARVideoCommon.ARVideoFrameRate getVideoFps() {
        return videoFps;
    }

    public void setVideoFps(ARVideoCommon.ARVideoFrameRate videoFps) {
        this.videoFps = videoFps;
    }

}
