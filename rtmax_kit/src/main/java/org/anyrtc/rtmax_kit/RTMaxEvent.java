package org.anyrtc.rtmax_kit;

import org.anyrtc.common.enums.AnyRTCNetQuality;

/**
 * RTMAX Callback Event.
 */
@Deprecated
public abstract class RTMaxEvent implements RTMaxHelper {

    @Override
    public void OnRtcJoinTalkGroupOK(String strGroupId) {
        onRTCJoinTalkGroupOK(strGroupId);
    }

    @Override
    public void OnRtcJoinTalkGroupFailed(String strGroupId, int nCode, String strReason) {
        onRTCJoinTalkGroupFailed(strGroupId, nCode, strReason);
    }

    @Override
    public void OnRtcLeaveTalkGroup(int nCode) {
        onRTCLeaveTalkGroup(nCode);
    }

    @Override
    public void OnRtcApplyTalkOk() {
        onRTCApplyTalkOk();
    }

    @Override
    public void OnRtcTalkCouldSpeak() {
        onRTCTalkCouldSpeak();
    }

    @Override
    public void OnRtcTalkOn(String strUserId, String strUserData) {
        onRTCTalkOn(strUserId, strUserData);
    }

    @Override
    public void OnRtcTalkP2POn(String strUserId, String strUserData) {
        onRTCTalkP2POn(strUserId, strUserData);
    }

    @Override
    public void OnRtcTalkP2POff(String strUserData) {
        onRTCTalkP2POff(strUserData);
    }

    @Override
    public void OnRtcTalkClosed(int nCode, String strUserId, String strUserData) {
        onRTCTalkClosed(nCode, strUserId, strUserData);
    }

    @Override
    public void OnRtcVideoMonitorRequest(String strUserId, String strUserData) {
        onRTCVideoMonitorRequest(strUserId, strUserData);
    }

    @Override
    public void OnRtcVideoMonitorClose(String strUserId, String strUserData) {
        onRTCVideoMonitorClose(strUserId, strUserData);
    }

    @Override
    public void OnRtcVideoMonitorResult(String strUserId, int nCode, String strUserData) {
        onRTCVideoMonitorResult(strUserId, nCode, strUserData);
    }

    @Override
    public void OnRtcVideoReportRequest(String strUserId, String strUserData) {
        onRTCVideoReportRequest(strUserId, strUserData);
    }

    @Override
    public void OnRtcVideoReportClose(String strUserId) {
        onRTCVideoReportClose(strUserId);
    }

    @Override
    public void OnRtcMakeCallOK(String strCallId) {
        onRTCMakeCallOK(strCallId);
    }

    @Override
    public void OnRtcAcceptCall(String strUserId, String strUserData) {
        onRTCAcceptCall(strUserId, strUserData);
    }

    @Override
    public void OnRtcRejectCall(String strUserId, int nCode, String strUserData) {
        onRTCRejectCall(strUserId, nCode, strUserData);
    }

    @Override
    public void OnRtcLeaveCall(String strUserId) {
        onRTCLeaveCall(strUserId);
    }

    @Override
    public void OnRtcReleaseCall(String strCallId) {
        onRTCReleaseCall(strCallId);
    }

    @Override
    public void OnRtcMakeCall(String strCallId, int nCallType, String strUserId, String strUserData) {
        onRTCMakeCall(strCallId, nCallType, strUserId, strUserData);
    }

    @Override
    public void OnRtcEndCall(String strCallId, String strUserId, int nCode) {
        onRTCEndCall(strCallId, strUserId, nCode);
    }

    @Override
    public void OnRtcOpenVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId, String strUserData) {
        onRTCOpenVideoRender(strRTCPeerId, strRTCPubId, strUserId, strUserData);
    }

    @Override
    public void OnRtcCloseVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId) {
        onRTCCloseVideoRender(strRTCPeerId, strRTCPubId, strUserId);
    }

    @Override
    public void OnRtcOpenAudioTrack(String strRTCPeerId, String strUserId, String strUserData) {
        onRTCOpenAudioTrack(strRTCPeerId, strUserId, strUserData);
    }

    @Override
    public void OnRtcCloseAudioTrack(String strRTCPeerId, String strUserId) {
        onRTCCloseAudioTrack(strRTCPeerId, strUserId);
    }

    @Override
    public void OnRtcAVStatus(String strRTCPeerId, boolean bAudio, boolean bVideo) {
        onRTCAVStatus(strRTCPeerId, bAudio, bVideo);
    }

    @Override
    public void OnRtcAudioActive(String strRTCPeerId, String strUserId, int nLevel, int nShowtime, boolean isOpenSLRecordError, boolean isOpenSLPlayerError) {
        onRTCAudioActive(strRTCPeerId, strUserId, nLevel, nShowtime, isOpenSLRecordError, isOpenSLPlayerError);
    }

    @Override
    public void OnRtcNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost) {
        AnyRTCNetQuality netQuality = null;
        if(nPacketLost <= 1) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityExcellent;
        } else if(nPacketLost > 1 && nPacketLost <= 3) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityGood;
        } else if(nPacketLost > 3 && nPacketLost <= 5) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityAccepted;
        } else if(nPacketLost > 5 && nPacketLost <= 10) {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityBad;
        } else {
            netQuality = AnyRTCNetQuality.AnyRTCNetQualityVBad;
        }
        onRTCNetworkStatus(strRTCPeerId, strUserId, nNetSpeed, nPacketLost, netQuality);
    }

    @Override
    public void OnRtcUserMessage(String strUserId, String strUserName, String strUserHeader, String strContent) {
        onRTCUserMessage(strUserId, strUserName, strUserHeader, strContent);
    }

    @Override
    public void OnRtcMemberNum(int nNum) {
        onRTCMemberNum(nNum);
    }

    @Override
    public void OnRtcUserDataNotify(String strUserData) {

    }

    @Override
    public void OnRtcGotRecordFile(int nRecType, String strUserData, String strFilePath) {
        onRTCGotRecordFile(nRecType, strUserData, strFilePath);
    }

    /**
     * 加入对讲组成功回调
     * @param strGroupId 群组id
     */
    public abstract void onRTCJoinTalkGroupOK(String strGroupId);

    /**
     * 加入对讲组失败回调
     * @param strGroupId 群组id
     * @param nCode 错误码
     * @param strReason 错误原因
     */
    public abstract void onRTCJoinTalkGroupFailed(String strGroupId, int nCode, String strReason);

    /**
     * 离开对讲组回调
     * @param nCode 错误码 0：正常退出；100：网络错误，与服务器断开连接；207：强制退出。
     */
    public abstract void onRTCLeaveTalkGroup(int nCode);

    /**
     * 申请对讲成功回调
     */
    public abstract void onRTCApplyTalkOk();

    /**
     * 申请对讲成功后，语音通道建立成功回调，可以开始讲话
     */
    public abstract void onRTCTalkCouldSpeak();

    /**
     * 其他人正在对讲组中讲话回调
     * @param strUserId 用户的第三方userid
     * @param strUserData 用户的自定义数据
     */
    public abstract void onRTCTalkOn(String strUserId, String strUserData);

    /**
     * 当用户处于对讲状态时，控制台强制发起P2P通话时回调信息
     * @param strUserId
     * @param strUserData
     */
    public abstract void onRTCTalkP2POn(String strUserId, String strUserData);

    /**
     * 与控制台的P2P讲话结束回调
     * @param strUserData 用户自定义数据
     */
    public abstract void onRTCTalkP2POff(String strUserData);

    /**
     * 结束对讲回调
     * @param nCode 错误码 0：正常结束对讲；其他参考错误码
     * @param strUserId 用户的第三方userid
     * @param strUserData 用户的自定义数据
     */
    public abstract void onRTCTalkClosed(int nCode, String strUserId, String strUserData);

    /**
     * 视频监看请求回调
     * @param strUserId
     * @param strUserData
     */
    public abstract void onRTCVideoMonitorRequest(String strUserId, String strUserData);

    /**
     * 视频监看关闭回调
     * @param strUserId
     * @param strUserData 用户自定义数据
     */
    public abstract void onRTCVideoMonitorClose(String strUserId, String strUserData);

    /**
     * 视频监看请求结果回调
     * @param strUserId
     * @param nCode 830:视频监看时对方不在线或者下线了 831：视频监看被抢占了
     * @param strUserData 用户自定义数据
     */
    public abstract void onRTCVideoMonitorResult(String strUserId, int nCode, String strUserData);

    /**
     * 收到视频上报请求回调
     * @param strUserId
     * @param strUserData
     */
    public abstract void onRTCVideoReportRequest(String strUserId, String strUserData);

    /**
     * 视频上报关闭回调
     * @param strUserId
     */
    public abstract void onRTCVideoReportClose(String strUserId);

    /**
     * 主叫方发起通话成功回调
     * @param strCallId
     */
    public abstract void onRTCMakeCallOK(String strCallId);

    /**
     * 主叫方收到被叫方同意通话回调
     * @param strUserId
     * @param strUserData
     */
    public abstract void onRTCAcceptCall(String strUserId, String strUserData);

    /**
     * 主叫方收到被叫方拒绝通话回调
     * @param strUserId
     * @param nCode 840：对方不在线或者掉线了；841：发起呼叫时自己有其他业务在进行（资源被占用）；842：会话不存在
     * @param strUserData
     */
    public abstract void onRTCRejectCall(String strUserId, int nCode, String strUserData);

    /**
     * 被叫方调用leaveCall方法时，主叫方收到回调信息
     * @param strUserId
     */
    public abstract void onRTCLeaveCall(String strUserId);

    /**
     * 主叫方收到通话结束的回调（被叫方和被邀请方已全部退出或者主叫方挂断所有参与者）
     * @param strCallId
     */
    public abstract void onRTCReleaseCall(String strCallId);

    /**
     * 被叫方收到通话请求回调
     * @param strCallId
     * @param nCallType
     * @param strUserId
     * @param strUserData
     */
    public abstract void onRTCMakeCall(String strCallId, int nCallType, String strUserId, String strUserData);

    /**
     * 被叫方收到主叫方挂断通话回调
     * @param strCallId
     * @param strUserId
     * @param nCode 840：对方不在线或者掉线了；841：发起呼叫时自己有其他业务在进行（资源被占用）；842：会话不存在
     */
    public abstract void onRTCEndCall(String strCallId, String strUserId, int nCode);

    /**
     * 视频窗口打开回调信息
     * @param strRTCPeerId 视频peerid
     * @param strRTCPubId 视频发布id
     * @param strUserId 第三方用户平台的userid
     * @param strUserData 用户自定义数据
     */
    public abstract void onRTCOpenVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId, String strUserData);

    /**
     * 视频窗口关闭回调信息
     * @param strRTCPeerId 视频peerid
     * @param strRTCPubId 视频发布id
     * @param strUserId 第三方用户平台的userid
     */
    public abstract void onRTCCloseVideoRender(String strRTCPeerId, String strRTCPubId, String strUserId);

    /**
     * 打开音频通道回调信息
     * @param strRTCPeerId 视频peerid
     * @param strUserId 第三方用户平台的userid
     * @param strUserData 用户自定义数据
     */
    public abstract void onRTCOpenAudioTrack(String strRTCPeerId, String strUserId, String strUserData);

    /**
     * 关闭音频通道回调信息
     * @param strRTCPeerId 音频peerid
     * @param strUserId 第三方用户平台的userid
     */
    public abstract void onRTCCloseAudioTrack(String strRTCPeerId, String strUserId);

    /**
     * 用户的音视频状态回调
     * @param strRTCPeerId 视频peerid
     * @param bAudio 音频状态：true：打开，false: 关闭
     * @param bVideo 视频状态：true：打开，false：关闭
     */
    public abstract void onRTCAVStatus(String strRTCPeerId, boolean bAudio, boolean bVideo);

    /**
     * 音频实时监测回调
     * @param strRTCPeerId 视频peerid
     * @param strUserId 用户第三方平台的userid
     * @param nLevel 音量大小
     * @param nShowtime 监测间隔时间（毫秒）
     */
    public abstract void onRTCAudioActive(String strRTCPeerId, String strUserId, int nLevel, int nShowtime, boolean isOpenSLRecordError, boolean isOpenSLPlayerError);

    /**
     * 实时网络监测状况
     * @param strRTCPeerId 视频peerid
     * @param strUserId 用户第三方平台的userid
     * @param nNetSpeed 当掉网络速度
     * @param nPacketLost 当前丢包率
     * @param netQuality
     */
    public abstract void onRTCNetworkStatus(String strRTCPeerId, String strUserId, int nNetSpeed, int nPacketLost, AnyRTCNetQuality netQuality);

    /**
     * 用户的消息信息回调
     * @param strUserId 用户第三方平台的userid
     * @param strUserName 用户第三方平台的昵称
     * @param strUserHeader 用户第三方平台的头像url
     * @param strContent 消息信息
     */
    public abstract void onRTCUserMessage(String strUserId, String strUserName, String strUserHeader, String strContent);

    /**
     * 当前对讲组在线人数回调
     * @param nNum 人数总数
     */
    public abstract void onRTCMemberNum(int nNum);

    /**
     * 用户自定义数据信息数据变化通知
     * @param strUserData 用户自定义数据
     */
    //public abstract void onRTCUserDataNotify(String strUserData);

    /**
     * 录像地址回调信息
     * @param nRecType 录音的类型（0/1/2/3：对讲本地录音/对讲远端录音/强插P2P录音/语音通话呼叫录音）
     * @param strUserData 用户自定义数据
     * @param strFilePath 录音文件的路径
     */
    public abstract void onRTCGotRecordFile(int nRecType, String strUserData, String strFilePath);
}
