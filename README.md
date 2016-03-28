#AndroidLogcatSDK

AndroidLogcatSDK是一个在android上输出日志的工具，以往调试往往需要首先使用usb连接到电脑，然后使用logcat工具在电脑上查看日志，而AndroidLogcatSDK可以仅仅使用移动设备就可以输出日志到sd卡上。

这个工程只有AndroidLogcatSdk.java类是我实现的，其余的代码都是对别人代码的简单重构。
参考代码：http://easion-zms.iteye.com/blog/981568#comments

##使用方法：
###API简介
启动LogService服务,开始输出日志

			AndroidLogcatSdk.Instance().Init(_activity, "", 0, "", "", "");

设置开启日志的变量，这里现在只是用SharedPreferences保存一个变量

			AndroidLogcatSdk.Instance().EnableLogcat(_activity, true);

判断是否开始输出日志

			AndroidLogcatSdk.Instance().IsEnabled(_activity)
			
Ftp上传，这个未测试啊

			AndroidLogcatSdk.Instance().UploadCurrentLog()
			
###我的一般写法
我一般会这样写，在app初始化时，调用：

			if(AndroidLogcatSdk.Instance().IsEnabled(_activity))
			{
				AndroidLogcatSdk.Instance().Init(_activity, "", 0, "", "", "");
			}

然后玩家若是想要开启日志输出，则调用，

			AndroidLogcatSdk.Instance().EnableLogcat(_activity, true);
			
那么重启app之后，就会开启日志输出了，注意是重启app之后。

###自定义

当然其实可以随时开启日志输出，然后随时关闭，开启日志输出就是启动LogService服务，关闭则是停止LogService服务，代码如下：

			//启动LogService服务
			_service = new Intent(MainActivity, LogService.class);
			MainActivity.startService(_service);

			//停止LogService服务
			MainActivity.stopService(_service);
			
##jar包介绍：
*	androidlogcat.jar	这个是src目录里的几个源文件编译后的jar包
*	commons-net-3.4.jar		如果你需要用到ftp接口的话，加上这个没问题

##注意事项：
*	Ftp上传没有测试过
*	StreamConsumer还有问题，不过不影响使用，只是清理缓存日志时会有问题

