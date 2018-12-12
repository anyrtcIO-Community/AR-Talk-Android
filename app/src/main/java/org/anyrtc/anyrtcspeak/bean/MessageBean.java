package org.anyrtc.anyrtcspeak.bean;

/**
 * Created by liuxiaozhong on 2018/10/25.
 */
public class MessageBean {
    public boolean isSelf;
    public String userId;
    public String content;

    public MessageBean(boolean isSelf, String userId, String content) {
        this.isSelf = isSelf;
        this.userId = userId;
        this.content = content;
    }
}
