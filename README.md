



### AR-Talk-Android SDK for Android
### 简介
AR-Talk-Android对讲调度demo,包括对讲、视频上报、监看、音视频通话、发消息等功能



### app体验

##### 扫码下载
![image](https://www.pgyer.com/app/qrcode/LfFY)
##### [点击下载](https://www.pgyer.com/LfFY)
##### [WEB在线体验](https://www.anyrtc.io/demo/dispatch)

### SDK集成

# > 方式一
>1. 下载本项目
>1. 将本项目libs目录下的rtmax_kit-release.aar文件放入你项目的libs目录中
>2. 在Model下的build.gradle文件添加如下代码依赖

```
android
{

 repositories {
        flatDir {dirs 'libs'}
    }

 }

```
```
dependencies {
   implementation(name: 'rtmax_kit-release', ext: 'aar')
}
```

### 安装

### 编译环境

开发工具 AndroidStudio
Gradle 3.0.1

### 运行环境

Android API 16+
真机运行

### 如何使用

### 注册开发者信息

>如果您还未注册anyRTC开发者账号，请登录[anyRTC官网](http://www.anyrtc.io)注册及获取更多的帮助。

### 替换开发者账号
在[anyRTC官网](http://www.anyrtc.io)获取了开发者账号，AppID等信息后，替换DEMO中
/utils/Constans.class 类中的开发者信息即可

### 操作步骤

1、两台移动设备加入即可开始对讲，要体验监看，视频呼叫等功能请[WEB在线体验](https://www.anyrtc.io/demo/dispatch)；


### 完整文档
SDK集成，API介绍，详见官方完整文档：[点击查看](https://www.anyrtc.io/resoure)

### AR-Talk-iOS

[AR-Talk-iOS](https://github.com/anyRTC/AR-Talk-iOS)

### AR-Talk-Web

[AR-Talk-Web](https://github.com/anyRTC/AR-Talk-Web)


### 支持的系统平台
**Android** 4.3及以上

### 支持的CPU架构
**Android** arm64-v8a  armeabi armeabi-v7a



### 技术支持
- anyRTC官方网址：[https://www.anyrtc.io](https://www.anyrtc.io/resoure)
- QQ技术咨询群：554714720
- 联系电话:021-65650071-816
- Email:hi@dync.cc


### 关于直播

本公司有一整套完整音视频解决方案。本公司开发者平台www.anyrtc.io。除了基于RTMP协议的直播系统外，我公司还有基于WebRTC的时时交互直播系统、P2P呼叫系统、会议，智能调度系统等。快捷集成SDK，便可让你的应用拥有实时音视频交互功能。欢迎您的来电~

### License

- AR-Talk-Android is available under the MIT license. See the LICENSE file for more info.





   



 
