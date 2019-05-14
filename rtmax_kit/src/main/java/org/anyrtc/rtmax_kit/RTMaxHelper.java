package org.anyrtc.rtmax_kit;

/**
 *
 * @author Eric
 * @date 2017/12/8
 */
@Deprecated
public interface RTMaxHelper {

    /**
     * 加入对讲组成功回调
     * @param strGroupId 群组id
     */
    public void OnRtcJoinTalkGroupOK(String strGroupId);

    /**
     * 加入对讲组失败回调
     * @param strGroupId 群组id
     * @param nCode 错误码
     * @param strReason 错误原因（删除）
     */
    public void OnRtcJoinTalkGroupFailed(String strGroupId, int nCode, String strReason);

    /**
     * 离开对讲组回调
     * @param nCode 错误码 0：正常退出；100：网络错误，与服务器断开连接；207：强制退出。
     */
    public void OnRtcLeaveTalkGroup(int nCode);

    /**
     * 申请对讲成功回调
     */
    public void OnRtcApplyTalkOk();

    /**
     * 申请对讲成功后，语音通道建立成功回调，可以开始讲话
     */
    public void OnRtcTalkCouldSpeak();

    /**
     * 其他人正在对讲组中讲话回调
     * @param strUserId 用户的第三方userid
     * @param strUserData 用户的自定义数据
     */
    public void OnRtcTalkOn(String strUserId, String strUserData);

    /**
     * 当用户处于对讲状态时，控制台强制发起P2P通话时回调信息
     * @param strUserId
     * @param strUserData
     */
    public void OnRtcTalkP2POn(String strUserId, String strUserData);

    /**
     * 与控制台的P2P讲话结束回调
     * @param strUserData 用户自定义数据
     */
    public void OnRtcTalkP2POff(String strUserData);

    /**
     * 结束对讲回调
     * @param nCode 错误码 0：正常结束对讲；其他参考错误码
     * @param strUserId 用户的第三方userid
     * @param strUserData 用户的自定义数据
     */
    public void OnRtcTalkClosed(int nCode, String strUserId, String strUserData);

    /**
     * 视频监看请求回调
     * @param strUserId
     * @param strUserData
     */
    public void OnRtcVideoMonitorRequest(String strUserId, String strUserData);

    /**
     * 视频监看关闭回调
     * @param strUserId
     * @param strUserData 用户自定义数据
     */
    public void OnRtcVideoMonitorClose(String strUserId, String strUserData);

    /**
     * 视频监看请求结果回调
     * @param strUserId
     * @param nCode 830:视频监看时对方不在线或者下线了 831：视频监看被抢占了
     * @param strUserData 用户自定义数据
     */
    public void OnRtcVideoMonitorResult(String strUserId, int nCode, String strUserData);

    /**
     * 收到视频上报请求回调
     * @param strUserId
     * @param strUserData
     */
    public void OnRtcVideoReportRequest(String strUserId, String strUserData);

    /**
     * 视频上报关闭回调
     * @param strUserId
     */
    public void OnRtcVideoReportClose(String strUserId);

    /**
     * 主叫方发起通话成功回调
     * @param strCallId
     */
    public void OnRtcMakeCallOK(String strCallId);

    /**
     * 主叫方收到被叫方同意通话回调
     * @param strUserId
     * @param strUserData
     */
    public void OnRtcAcceptCall(String strUserId, String strUserData);

    /**
     * 主叫方收到被叫方拒绝通话回调
     * @param strUserId
     * @param nCode 840：对方不在线或者掉线了；841：发起呼叫时自己有其他业务在进行（资源被占用）；842：会话不存在
     * @param strUserData
     */
    public void OnRtcRejectCall(String strUserId, int nCode, String strUserData);

    /**
     * 被叫方调用leaveCall方法时，主叫方收到回调信息
     * @param strUserId
     */
    public void OnRtcLeaveCall(String strUserId);

    /**
     * 主叫方收到通话结束的回调（被叫方和被邀请方已全部退出或者主叫方挂断所有参与者）
     * @param strCallId
     */
    public void OnRtcReleaseCall(String strCallId);

    /**
     * 被叫方收到通话请求回调
     * @param strCallId
     * @param nCallType
     * @param strUserId
     * @param strUserData
     */
    public void OnRtcMakeCall(String strCallId, int nCallType, String strUserId, String strUserData);

    /**
     * 被叫方收到主叫方挂断通话回调
     * @param strCallId
     * @param strUserId
     * @param nCode 840：对方不在线或者掉线了；841：发起呼叫时自己有其他业务在进行（资源被占用）；842：会话不存在
     */
    public void OnRtcEndCall(String strCallId, String strUserId, int nCode);

    /**
     * 视频窗口打开回调信息
     * @param strRtcPeerId 视频peerid
     * @param strRtcPubId 视频发布id
     * @param strUserId 第三方用户平台的userid
     * @param strUserData 用户自定义数据
     */
    public void OnRtcOpenVideoRender(String strRtcPeerId, String strRtcPubId, String strUserId, String strUserData);

    /**
     * 视频窗口关闭回调信息
     * @param strRtcPeerId 视频peerid
     * @param strRtcPubId 视频发布id
     * @param strUserId 第三方用户平台的userid
     */
    public void OnRtcCloseVideoRender(String strRtcPeerId, String strRtcPubId, String strUserId);

    /**
     * 打开音频通道回调信息
     * @param strRtcPeerId 视频peerid
     * @param strUserId 第三方用户平台的userid
     * @param strUserData 用户自定义数据
     */
    public void OnRtcOpenAudioTrack(String strRtcPeerId, String strUserId, String strUserData);

    /**
     * 关闭音频通道回调信息
     * @param strRtcPeerId 音频peerid
     * @param strUserId 第三方用户平台的userid
     */
    public void OnRtcCloseAudioTrack(String strRtcPeerId, String strUserId);

    /**
     * 用户的音视频状态回调
     * @param strRtcPeerId 视频peerid
     * @param bAudio 音频状态：true：打开，false: 关闭
     * @param bVideo 视频状态：true：打开，false：关闭
     */
    public void OnRtcAVStatus(String strRtcPeerId, boolean bAudio, boolean bVideo);

    /**
     * 音频实时监测回调
     * @param strRtcPeerId 视频peerid
     * @param strUserId 用户第三方平台的userid
     * @param nLevel 音量大小
     * @param nShowtime 监测间隔时间（毫秒）
     */
    public void OnRtcAudioActive(String strRtcPeerId, String strUserId, int nLevel, int nShowtime, boolean isOpenSLRecordError, boolean isOpenSLPlayerError);

    /**
     * 实时网络监测状况
     * @param strRtcPeerId 视频peerid
     * @param strUserId 用户第三方平台的userid
     * @param nNetSpeed 当掉网络速度
     * @param nPacketLost 当前丢包率
     */
    public void OnRtcNetworkStatus(String strRtcPeerId, String strUserId, int nNetSpeed, int nPacketLost);

    /**
     * 用户的消息信息回调
     * @param strUserId 用户第三方平台的userid
     * @param strUserName 用户第三方平台的昵称
     * @param strUserHeader 用户第三方平台的头像url
     * @param strContent 消息信息
     */
    public void OnRtcUserMessage(String strUserId, String strUserName, String strUserHeader, String strContent);

    /**
     * 当前对讲组在线人数回调
     * @param nNum 人数总数
     */
    public void OnRtcMemberNum(int nNum);

    /**
     * 用户自定义数据信息数据变化通知
     * @param strUserData 用户自定义数据
     */
    public void OnRtcUserDataNotify(String strUserData);

    /**
     * 录像地址回调信息
     * @param nRecType 录音的类型（0/1/2/3：对讲本地录音/对讲远端录音/强插P2P录音/语音通话呼叫录音）
     * @param strUserData 用户自定义数据
     * @param strFilePath 录音文件的路径
     */
    public void OnRtcGotRecordFile(int nRecType, String strUserData, String strFilePath);
}
