package org.ar.rtmax_kit;

import org.ar.common.enums.ARNetQuality;

public abstract class ARMaxEvent {

    /**
     * 加入对讲组成功回调
     * @param groupId 群组id
     */
    public abstract void onRTCJoinTalkGroupOK(String groupId);

    /**
     * 加入对讲组失败回调
     * @param groupId 群组id
     * @param code 错误码
     * @param reason 错误原因
     */
    public abstract void onRTCJoinTalkGroupFailed(String groupId, int code, String reason);

    /**
     * 离开对讲组回调
     * @param code 错误码 0：正常退出；100：网络错误，与服务器断开连接；207：强制退出。
     */
    public abstract void onRTCLeaveTalkGroup(int code);

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
     * @param userId 用户的第三方userid
     * @param userData 用户的自定义数据
     */
    public abstract void onRTCTalkOn(String userId, String userData);

    /**
     * 当用户处于对讲状态时，控制台强制发起P2P通话时回调信息
     * @param userId
     * @param userData
     */
    public abstract void onRTCTalkP2POn(String userId, String userData);

    /**
     * 与控制台的P2P讲话结束回调
     * @param userData 用户自定义数据
     */
    public abstract void onRTCTalkP2POff(String userData);

    /**
     * 结束对讲回调
     * @param code 错误码 0：正常结束对讲；其他参考错误码
     * @param userId 用户的第三方userid
     * @param userData 用户的自定义数据
     */
    public abstract void onRTCTalkClosed(int code, String userId, String userData);

    /**
     * 视频监看请求回调
     * @param userId
     * @param userData
     */
    public abstract void onRTCVideoMonitorRequest(String userId, String userData);

    /**
     * 视频监看关闭回调
     * @param userId
     * @param userData 用户自定义数据
     */
    public abstract void onRTCVideoMonitorClose(String userId, String userData);

    /**
     * 视频监看请求结果回调
     * @param userId
     * @param code 830:视频监看时对方不在线或者下线了 831：视频监看被抢占了
     * @param userData 用户自定义数据
     */
    public abstract void onRTCVideoMonitorResult(String userId, int code, String userData);

    /**
     * 收到视频上报请求回调
     * @param userId
     * @param userData
     */
    public abstract void onRTCVideoReportRequest(String userId, String userData);

    /**
     * 视频上报关闭回调
     * @param userId
     */
    public abstract void onRTCVideoReportClose(String userId);

    /**
     * 主叫方发起通话成功回调
     * @param callId
     */
    public abstract void onRTCMakeCallOK(String callId);

    /**
     * 主叫方收到被叫方同意通话回调
     * @param userId
     * @param userData
     */
    public abstract void onRTCAcceptCall(String userId, String userData);

    /**
     * 主叫方收到被叫方拒绝通话回调
     * @param userId
     * @param code 840：对方不在线或者掉线了；841：发起呼叫时自己有其他业务在进行（资源被占用）；842：会话不存在
     * @param userData
     */
    public abstract void onRTCRejectCall(String userId, int code, String userData);

    /**
     * 被叫方调用leaveCall方法时，主叫方收到回调信息
     * @param userId
     */
    public abstract void onRTCLeaveCall(String userId);

    /**
     * 主叫方收到通话结束的回调（被叫方和被邀请方已全部退出或者主叫方挂断所有参与者）
     * @param callId
     */
    public abstract void onRTCReleaseCall(String callId);

    /**
     * 被叫方收到通话请求回调
     * @param callId
     * @param nCallType
     * @param userId
     * @param userData
     */
    public abstract void onRTCMakeCall(String callId, int nCallType, String userId, String userData);

    /**
     * 被叫方收到主叫方挂断通话回调
     * @param callId
     * @param userId
     * @param code 840：对方不在线或者掉线了；841：发起呼叫时自己有其他业务在进行（资源被占用）；842：会话不存在
     */
    public abstract void onRTCEndCall(String callId, String userId, int code);

    /**
     * 视频窗口打开回调信息
     * @param peerId 视频peerid
     * @param publishId 视频发布id
     * @param userId 第三方用户平台的userid
     * @param userData 用户自定义数据
     */
    public abstract void onRTCOpenRemoteVideoRender(String peerId, String publishId, String userId, String userData);

    /**
     * 视频窗口关闭回调信息
     * @param peerId 视频peerid
     * @param publishId 视频发布id
     * @param userId 第三方用户平台的userid
     */
    public abstract void onRTCCloseRemoteVideoRender(String peerId, String publishId, String userId);

    /**
     * 打开音频通道回调信息
     * @param peerId 视频peerid
     * @param userId 第三方用户平台的userid
     * @param userData 用户自定义数据
     */
    public abstract void onRTCOpenRemoteAudioTrack(String peerId, String userId, String userData);

    /**
     * 关闭音频通道回调信息
     * @param peerId 音频peerid
     * @param userId 第三方用户平台的userid
     */
    public abstract void onRTCCloseRemoteAudioTrack(String peerId, String userId);

    /**
     * 用户的音视频状态回调
     * @param peerId 视频peerid
     * @param audio 音频状态：true：打开，false: 关闭
     * @param video 视频状态：true：打开，false：关闭
     */
    public abstract void onRTCRemoteAVStatus(String peerId, boolean audio, boolean video);

    public abstract void onRTCLocalAVStatus(boolean audio, boolean video);
    /**
     * 音频实时监测回调
     * @param peerId 视频peerid
     * @param userId 用户第三方平台的userid
     * @param nLevel 音量大小
     * @param nShowtime 监测间隔时间（毫秒）
     */
    public abstract void onRTCRemoteAudioActive(String peerId, String userId, int nLevel, int nShowtime);
    public abstract void onRTLocalAudioActive( int nLevel, int nTime);
    /**
     * 实时网络监测状况
     * @param peerId 视频peerid
     * @param userId 用户第三方平台的userid
     * @param nNetSpeed 当掉网络速度
     * @param nPacketLost 当前丢包率
     */
    public abstract void onRTCRemoteNetworkStatus(String peerId, String userId, int nNetSpeed, int nPacketLost, ARNetQuality netQuality);

    public abstract void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality);
    /**
     * 用户的消息信息回调
     * @param userId 用户第三方平台的userid
     * @param userName 用户第三方平台的昵称
     * @param headerUrl 用户第三方平台的头像url
     * @param content 消息信息
     */
    public abstract void onRTCUserMessage(String userId, String userName, String headerUrl, String content);

    /**
     * 当前对讲组在线人数回调
     * @param nNum 人数总数
     */
    public abstract void onRTCMemberNum(int nNum);

    /**
     * 用户自定义数据信息数据变化通知
     * @param userData 用户自定义数据
     */
    //public abstract void onRTCUserDataNotify(String userData);

    /**
     * 录像地址回调信息
     * @param nRecType 录音的类型（0/1/2/3：对讲本地录音/对讲远端录音/强插P2P录音/语音通话呼叫录音）
     * @param userData 用户自定义数据
     * @param strFilePath 录音文件的路径
     */
    public abstract void onRTCGotRecordFile(int nRecType, String userData, String filePath);
}
