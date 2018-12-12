package org.anyrtc.anyrtcspeak;

import android.app.Application;

import org.anyrtc.anyrtcspeak.utils.Constans;
import org.anyrtc.anyrtcspeak.utils.NameUtils;
import org.anyrtc.anyrtcspeak.utils.SoundPlayUtils;
import org.anyrtc.rtmax_kit.AnyRTCMaxEngine;

import java.util.Random;

/**
 * Created by liuxiaozhong on 2018/6/7.
 */
public class MyApplication extends Application {

    private static MyApplication mInstance;

    public static String tempUserid="";
    public static String tempNickName="";

    public static MyApplication app(){
        return mInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        tempNickName= NameUtils.getNickName();
        tempUserid=randomNum(6);
        SoundPlayUtils.init(this);
        AnyRTCMaxEngine.Inst().initEngineWithAnyrtcInfo(getApplicationContext(),false, Constans.DEVELOPERID,
                Constans.APPID, Constans.APPKEY, Constans.APPTOKEN);
        AnyRTCMaxEngine.Inst().configServerForPriCloud("pro.anyrtc.io",9060);
        AnyRTCMaxEngine.Inst().setAuidoModel(true,true);
    }
    public static String randomNum(int num){
        StringBuilder str=new StringBuilder();//定义变长字符串
        Random random=new Random();
        for (int i = 0; i < num; i++) {
            str.append(random.nextInt(10));
        }
        return str.toString();
    }
}
