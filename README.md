HEKR-ANDROID-APP-V1.0 

项目用IntelliJ IDEA编写，Gradle构建

1.IntelliJ IDEA开发环境搭建：http://www.tuicool.com/articles/a2MNna

2.Android API Guides：http://wear.techbrood.com/guide/index.html

3.请将files-2.1文件夹下的文件拷贝到.gradle\caches\modules-2\files-2.1下

4.将项目的android-sdk指向自己的sdk路径

5.如果使用Eclipse开发工具编写请自行修改依赖关系（暂未提供）,提示：用Eclipse开发工具编写需引入android-support-v7-appcompat包

6.AndroidMainfest.xml中的UMENG_APPKEY值请到友盟平台自行申请key值填入到相应位置，或者并不想申请也可以根据源代码去除数据统计功能（并不影响app主功能）
可以参考http://dev.umeng.com/analytics/android-doc/integration

7.AndroidMainfest.xml中的PUSH_APPID、PUSH_APPKEY、PUSH_APPSECRET值、(第三方包名即为应用标识)请到个推平台自行申请并填入到相应位置，或者根据源代码去除推送功能（并不影响app主功能）
可以参考http://docs.getui.com/pages/viewpage.action?pageId=589991
