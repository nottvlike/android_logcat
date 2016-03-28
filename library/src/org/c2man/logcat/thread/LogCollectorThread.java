package org.c2man.logcat.thread;

import java.util.List;

import org.c2man.logcat.process.ProcessInfo;
import org.c2man.logcat.utils.Constant;
import org.c2man.logcat.utils.LogHelper;
import org.c2man.logcat.utils.ProcessHelper;

import android.util.Log;

/**
 * 日志收集 1.清除日志缓存 2.杀死应用程序已开启的Logcat进程防止多个进程写入一个日志文件 3.开启日志收集进程 4.处理日志文件 移动 OR 删除
 */
public class LogCollectorThread extends Thread {

	public LogCollectorThread() {
		super("LogCollectorThread");
		Log.d(Constant.TAG, "LogCollectorThread is create");
	}

	@Override
	public void run() {
		try {
			LogHelper.Instance().PartialWakeLock.acquire(); // 唤醒手机

			LogHelper.Instance().ClearLogCache();

			List<String> orgProcessList = ProcessHelper.Instance()
					.GetAllProcess();
			List<ProcessInfo> processInfoList = ProcessHelper.Instance()
					.GetProcessInfoList(orgProcessList);
			ProcessHelper.Instance().KillLogcatProc(processInfoList);

			LogHelper.Instance().CreateLogCollector();

			Thread.sleep(1000);// 休眠，创建文件，然后处理文件，不然该文件还没创建，会影响文件删除

			LogHelper.Instance().HandleLog();

			LogHelper.Instance().PartialWakeLock.release(); // 释放
		} catch (Exception e) {
			e.printStackTrace();
			LogHelper.Instance()
					.RecordLogServiceLog(Log.getStackTraceString(e));
		}
	}
}