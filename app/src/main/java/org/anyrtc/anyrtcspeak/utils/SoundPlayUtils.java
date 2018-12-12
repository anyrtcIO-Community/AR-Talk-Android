package org.anyrtc.anyrtcspeak.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import org.anyrtc.anyrtcspeak.R;


/**
 * Created by liuxiaozhong on 2017/12/12.
 */

public class SoundPlayUtils {
    // SoundPool对象
    public static SoundPool mSoundPlayer = new SoundPool(1,
            AudioManager.STREAM_SYSTEM, 0);
    public static SoundPlayUtils soundPlayUtils;
    // 上下文
    static Context mContext;

    /**
     * 初始化
     *
     * @param context
     */
    public static SoundPlayUtils init(Context context) {

        if (soundPlayUtils == null) {
            soundPlayUtils = new SoundPlayUtils();
        }

        // 初始化声音
        mContext = context;
        mSoundPlayer.load(mContext, R.raw.down, 1);// 1
        mSoundPlayer.load(mContext, R.raw.speaking, 1);// 2
        return soundPlayUtils;
    }

    /**
     * 播放声音
     *
     * @param soundID
     */
    public static int play(int soundID) {
        int play = mSoundPlayer.play(soundID, 1, 1, 0, 0, 1);
        return play;
    }

    public static void stop(int soundID) {
        mSoundPlayer.stop(soundID);
    }
}
