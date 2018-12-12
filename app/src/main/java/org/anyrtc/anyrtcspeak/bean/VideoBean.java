package org.anyrtc.anyrtcspeak.bean;

import org.webrtc.PercentFrameLayout;

/**
 * Created by liuxiaozhong on 2018/10/19.
 */
public class VideoBean {
    String publishId;
    String userId;
    String peerID;
    PercentFrameLayout videoView;

    public VideoBean(String publishId, String userId, String peerID) {
        this.publishId = publishId;
        this.userId = userId;
        this.peerID = peerID;
    }


    public String getPublishId() {
        return publishId;
    }

    public void setPublishId(String publishId) {
        this.publishId = publishId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }

    public PercentFrameLayout getVideoView() {
        return videoView;
    }

    public void setVideoView(PercentFrameLayout videoView) {
        this.videoView = videoView;
    }

}
