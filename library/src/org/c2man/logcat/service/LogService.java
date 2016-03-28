package org.c2man.logcat.service;

import java.io.IOException;
import java.util.Calendar;

import org.c2man.logcat.receiver.LogTaskReceiver;
import org.c2man.logcat.receiver.SDStateMonitorReceiver;
import org.c2man.logcat.thread.LogCollectorThread;
import org.c2man.logcat.utils.Constant;
import org.c2man.logcat.utils.LogHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

/**
 * 日志服务，日志默认会存储在SDcar里如果没有SDcard会存储在内存中的安装目录下面。 1.本服务默认在SDcard中每天生成一个日志文件,
 * 2.如果有SDCard的话会将之前内存中的文件拷贝到SDCard中 3.如果没有SDCard，在安装目录下只保存当前在写日志
 * 4.SDcard的装载卸载动作会在步骤2,3中切换 5.SDcard中的日志文件只保存7天
 * 
 * @author Administrator
 * 
 */
public class LogService extends Service {

	private SDStateMonitorReceiver _sdStateReceiver; // SDcard状态监测
	private LogTaskReceiver _logTaskReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		init();
		register();
		deploySwitchLogFileTask();
		new LogCollectorThread().start();
	}

	private void init() {
		LogHelper.Instance().Init(this);
		PowerManager pm = (PowerManager) getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		LogHelper.Instance().PartialWakeLock = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, Constant.TAG);
	}

	private void register() {
		IntentFilter sdCarMonitorFilter = new IntentFilter();
		sdCarMonitorFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		sdCarMonitorFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		sdCarMonitorFilter.addDataScheme("file");
		_sdStateReceiver = new SDStateMonitorReceiver();
		registerReceiver(_sdStateReceiver, sdCarMonitorFilter);

		IntentFilter logTaskFilter = new IntentFilter();
		logTaskFilter.addAction(Constant.MONITOR_LOG_SIZE_ACTION);
		logTaskFilter.addAction(Constant.SWITCH_LOG_FILE_ACTION);
		_logTaskReceiver = new LogTaskReceiver();
		registerReceiver(_logTaskReceiver, logTaskFilter);
	}

	/**
	 * 部署日志切换任务，每天凌晨切换日志文件
	 */
	private void deploySwitchLogFileTask() {
		Intent intent = new Intent(Constant.SWITCH_LOG_FILE_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		// 部署任务
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, sender);
		LogHelper.Instance().RecordLogServiceLog(
				"deployNextTask succ,next task time is:"
						+ Constant.MyLogSdf.format(calendar.getTime()));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogHelper.Instance().RecordLogServiceLog("LogService onDestroy");
		if (LogHelper.Instance().LogWriter != null) {
			try {
				LogHelper.Instance().LogWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (LogHelper.Instance().ComandProcess != null) {
			LogHelper.Instance().ComandProcess.destroy();
		}

		unregisterReceiver(_sdStateReceiver);
		unregisterReceiver(_logTaskReceiver);
	}

}