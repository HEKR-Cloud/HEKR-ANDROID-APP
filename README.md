HEKR-ANDROID-APP-V1.0 

项目可以用IntelliJ IDEA、Android Studio、Eclipse编写，Gradle、Ant构建

壹：使用IntelliJ IDEA、Android Studio编写项目

    1.Intellij Idea(Android_Studio)_Version_1.0文件夹下自行导入 

    2.IntelliJ IDEA开发环境搭建：http://www.tuicool.com/articles/a2MNna

    3.Android API Guides：http://wear.techbrood.com/guide/index.html

    4.Gradle基础：http://stormzhang.com/devtools/2014/12/18/android-studio-tutorial4/

    5.请将项目的android-sdk指向自己的sdk路径

贰：使用Eclipse编写项目

    Eclipse_Version_Code_Version_1.0文件夹下自行导入

    一、项目环境说明：

    1、项目编码格式：utf-8

	   修改：eclipse软件菜单栏Window鼠标左键单击一下->Preferences->General左边箭头鼠标左键单击一下->Workspace->text file encoding

    2、项目build tool 版本19(4.4.2)

	   修改：
	       1、方法一：项目根目录鼠标右键单击一下->Properties->Android->选择你想要的build tool版本
		
	       2、方法二：项目project.properties文件中修改

	   建议将缺少的build tool版本下载下来，修改build-tool版本出现问题自行负责

	       1、方法一：国内镜像下载地址(里面build-tool最低版本20.0.0)，示例：http://www.androiddevtools.cn/

	       2、方法二：软件中Android SDK Manager下载(需要翻墙)本人使用Shadowsocks

    3、项目android-sdk版本:13-22(可在项目文件中的AndroidManifest.xml文件修改)

	   sdk添加：eclipse软件菜单栏Window鼠标左键单击一下->Preferences->Android->Browse...->您电脑上已经安装的sdk路径

    4、Android Support Library package 系列的包来保证高版本sdk开发的向下兼容性，即我们用4.x开发时，在1.6等版本上，可以使用高版本的有些特性，如fragement,ViewPager等

	   Android Support v4:  这个包是为了照顾1.6及更高版本而设计的，eclipse新建工程时，都默认带有了。

	   Android Support v7:  这个包是为了照顾2.1及以上版本而设计的，但不包含更低，故如果不考虑1.6,我们可以采用再加上这个包，另外注意，v7是要依赖v4这个包的，即，两个得同时被包含。

           添加library依赖 ：项目根目录鼠标右键单击一下->Android->Add->选择你想要的library

           添加jar包依赖：

	       1、方法一：项目根目录鼠标右键单击一下->Build Path->Configure Build Path...->Libraries->Add JARs->你需要添加的jar包(Android Private Libraries下有的jar不需要再次添加)
		   
	       2、方法二：将jar包拷到该项目工作空间路径下libs文件夹
		   
    
    二、jar包使用说明：

    1、fastjson 参考示例：http://www.open-open.com/lib/view/open1421744060171.html

    2、lambdaTM2.0 :获取设备detail中具体信息。

	detail:设备明细快照信息,数据为s表达式。具体参考：http://docs.hekr.me/cloud/cloud-1/#c1-3
    
	使用示例：
	如：传入的detail为:("mid" 0 "pid" 0)

	import com.lambdatm.runtime.lang.Cell;
	import com.lambdatm.runtime.lib.Base;
	import com.lambdatm.runtime.util.Util;
	
	List<Object> stateList=Util.tolist((Cell) Base.read.pc(detail, null))

	那么stateList里的元素个数为4,具体内容为： "mid" 0 "pid" 0

	
        
        
        
