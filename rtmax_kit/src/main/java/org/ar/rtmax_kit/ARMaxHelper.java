package org.ar.rtmax_kit;

/**
 *
 * @author Eric
 */
public interface ARMaxHelper {

    public void OnRtcJoinTalkGroupOK(String strGroupId);

    public void OnRtcJoinTalkGroupFailed(String strGroupId, int nCode, String strReason);

    public void OnRtcLeaveTalkGroup(int nCode);

    public void OnRtcApplyTalkOk();

    public void OnRtcTalkCouldSpeak();

    public void OnRtcTalkOn(String strUserId, String strUserData);
    public void OnRtcTalkP2POn(String strUserId, String strUserData);

    public void OnRtcTalkP2POff(String strUserData);
    public void OnRtcTalkClosed(int nCode, String strUserId, String strUserData);

    public void OnRtcVideoMonitorRequest(String strUserId, String strUserData);

    public void OnRtcVideoMonitorClose(String strUserId, String strUserData);

    public void OnRtcVideoMonitorResult(String strUserId, int nCode, String strUserData);

    public void OnRtcVideoReportRequest(String strUserId, String strUserData);

    public void OnRtcVideoReportClose(String strUserId);

    public void OnRtcMakeCallOK(String strCallId);

    public void OnRtcAcceptCall(String strUserId, String strUserData);

    public void OnRtcRejectCall(String strUserId, int nCode, String strUserData);

    public void OnRtcLeaveCall(String strUserId);

    public void OnRtcReleaseCall(String strCallId);

    public void OnRtcMakeCall(String strCallId, int nCallType, String strUserId, String strUserData);

    public void OnRtcEndCall(String strCallId, String strUserId, int nCode);

    public void OnRtcOpenVideoRender(String strRtcPeerId, String strRtcPubId, String strUserId, String strUserData);
    public void OnRtcCloseVideoRender(String strRtcPeerId, String strRtcPubId, String strUserId);

    public void OnRtcOpenAudioTrack(String strRtcPeerId, String strUserId, String strUserData);

    public void OnRtcCloseAudioTrack(String strRtcPeerId, String strUserId);
    public void OnRtcAVStatus(String strRtcPeerId, boolean bAudio, boolean bVideo);

    public void OnRtcAudioActive(String strRtcPeerId, String strUserId, int nLevel, int nShowtime);

    public void OnRtcNetworkStatus(String strRtcPeerId, String strUserId, int nNetSpeed, int nPacketLost);

    public void OnRtcUserMessage(String strUserId, String strUserName, String strUserHeader, String strContent);

    public void OnRtcMemberNum(int nNum);

    public void OnRtcUserDataNotify(String strUserData);

    public void OnRtcGotRecordFile(int nRecType, String strUserData, String strFilePath);
}
