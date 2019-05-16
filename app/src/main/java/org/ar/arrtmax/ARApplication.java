package org.ar.arrtmax;

import android.app.Application;

import org.ar.arrtmax.utils.NameUtils;
import org.ar.rtmax_kit.ARMaxEngine;

import java.util.Random;

/**
 * Created by liuxiaozhong on 2019/3/13.
 */
public class ARApplication extends Application {

    private static ARApplication mInstance;
    public static ARApplication app(){
        return mInstance;
    }

    public static String tempUserid="";
    public static String tempNickName="";
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        tempNickName= NameUtils.getNickName();
        tempUserid=randomNum(6);
        ARMaxEngine.Inst().initEngine(getApplicationContext(), false, DeveloperInfo.APPID,  DeveloperInfo.APPTOKEN);
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
