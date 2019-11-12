package com.google.android.gms.samples.vision.face.facetracker.utils;

import android.content.Context;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;

public class BuglyMgr {

    public static void buglyInit(Context context, String buglyID) {
//        初始化 bugly SDK
        Bugly.init(context, buglyID, false);

//        高級配置
//        自動初始化開關
        Beta.autoInit = true;

//        自動檢查更新開關
//        false表示不会自动检查升级,需要手动调用Beta.checkUpgrade()方法
        Beta.autoCheckUpgrade = true;

//        升級檢查週期設置
//        设置升级检查周期为60s(默认检查周期为0s)，60s内SDK不重复向后台请求策略
        Beta.upgradeCheckPeriod = 60 * 1000;

//        延迟初始化
//        设置启动延时为1s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
        Beta.initDelay = 1 * 1000;

//        设置是否显示消息通知
//        如果你不想在通知栏显示下载进度，你可以将这个接口设置为false，默认值为true。
        Beta.enableNotification = true;

//        设置Wifi下自动下载
        Beta.autoDownloadOnWifi = false;

//        设置是否显示弹窗中的apk信息
        Beta.canShowApkInfo = true;
    }
}
