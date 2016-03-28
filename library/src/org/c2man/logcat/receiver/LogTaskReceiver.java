package org.c2man.logcat.receiver;

import org.c2man.logcat.thread.LogCollectorThread;
import org.c2man.logcat.utils.Constant;
import org.c2man.logcat.utils.LogHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 日志任务接收 切换日志，监控日志大小
 * 
 * @author Administrator
 * 
 */
public class LogTaskReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Constant.SWITCH_LOG_FILE_ACTION.equals(action)) {
			new LogCollectorThread().start();
		} else if (Constant.MONITOR_LOG_SIZE_ACTION.equals(action)) {
			LogHelper.Instance().CheckLogSize();
		}
	}
}