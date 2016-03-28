package org.c2man.logcat.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.c2man.logcat.AndroidLogcatSdk;
import org.c2man.logcat.thread.LogCollectorThread;
import org.c2man.logcat.thread.StreamConsumer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class LogHelper {

	private static LogHelper _instance;

	public String MemoryLogPath;
	public String ServiceLogPath;
	public String SdcardLogPath;
	public String CurrentInstallLogName = "";
	public int CurrentLogType = 0;

	public Process ComandProcess;
	public WakeLock PartialWakeLock;
	public Service LogService;

	public OutputStreamWriter LogWriter;

	private boolean logSizeMoniting = false;

	public static LogHelper Instance() {
		if (_instance == null)
			_instance = new LogHelper();
		return _instance;
	}

	public void Init(Service service) {
		Log.d(Constant.TAG, "LogHelper Init");
		LogService = service;

		MemoryLogPath = service.getFilesDir().getAbsolutePath()
				+ File.separator + "log";
		ServiceLogPath = LogHelper.Instance().MemoryLogPath + File.separator
				+ Constant.LogServiceLogName;
		SdcardLogPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ "Android/data/"
				+ AndroidLogcatSdk.Instance().MainActivity.getPackageName()
				+ File.separator + "log";
		Log.d(Constant.TAG, MemoryLogPath);
		CreateLogDirectory();

		try {
			LogWriter = new OutputStreamWriter(new FileOutputStream(
					ServiceLogPath, true));
		} catch (FileNotFoundException e) {
			Log.e(Constant.TAG, e.getMessage(), e);
		}

		CurrentLogType = FileHelper.Instance().GetCurrLogType();
	}

	/**
	 * 检查日志文件大小是否超过了规定大小 如果超过了重新开启一个日志收集进程
	 */
	public void CheckLogSize() {
		if (CurrentInstallLogName != null && !"".equals(CurrentInstallLogName)) {
			String path = MemoryLogPath + File.separator
					+ CurrentInstallLogName;
			File file = new File(path);
			if (!file.exists()) {
				return;
			}
			Log.d(Constant.TAG,
					"checkLog() ==> The size of the log is too big?");
			if (file.length() >= Constant.MEMORY_LOG_FILE_MAX_SIZE) {
				Log.d(Constant.TAG, "The log's size is too big!");
				new LogCollectorThread().start();
			}
		}
	}

	/**
	 * 创建日志目录
	 */
	public void CreateLogDirectory() {
		if (!(FileHelper.Instance().CreateDirectory(MemoryLogPath)))
			Log.d(Constant.TAG,
					String.format("CreateMemoryLogFailed %s!", MemoryLogPath));

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			if (!(FileHelper.Instance().CreateDirectory(SdcardLogPath)))
				Log.d(Constant.TAG, String.format("CreateSdcardLogFailed %s!",
						SdcardLogPath));
		}
	}

	/**
	 * 将日志文件转移到SD卡下面
	 */
	public void MoveLogfile() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			RecordLogServiceLog("move file failed, sd card does not mount");
			return;
		}

		FileHelper.Instance().CopyDirectory(MemoryLogPath, SdcardLogPath);
	}

	/**
	 * 删除内存下过期的日志
	 */
	public void DeleteSDcardExpiredLog() {
		FileHelper.Instance().ClearDirectory(SdcardLogPath,
				Constant.SDCARD_LOG_FILE_SAVE_DAYS);
	}

	/**
	 * 删除内存中的过期日志，删除规则： 除了当前的日志和离当前时间最近的日志保存其他的都删除
	 */
	public void DeleteMemoryExpiredLog() {
		FileHelper.Instance().ClearDirectory(MemoryLogPath,
				Constant.SDCARD_LOG_FILE_SAVE_DAYS);
	}

	/**
	 * 开始收集日志信息
	 */
	public void CreateLogCollector() {
		CurrentInstallLogName = Constant.Sdf.format(new Date()) + ".log";// 日志文件名称
		List<String> commandList = new ArrayList<String>();
		commandList.add("logcat");
		commandList.add("-f");
		// commandList.add(LOG_PATH_INSTALL_DIR + File.separator + logFileName);
		commandList.add(GetLogPath() + CurrentInstallLogName);
		commandList.add("-v");
		commandList.add("time");
		commandList.add("*:I");

		// commandList.add("*:E");// 过滤所有的错误信息

		// 过滤指定TAG的信息
		// commandList.add("MyAPP:V");
		// commandList.add("*:S");
		try {
			ComandProcess = Runtime.getRuntime().exec(
					commandList.toArray(new String[commandList.size()]));
			RecordLogServiceLog("start collecting the log,and log name is:"
					+ CurrentInstallLogName);
			// process.waitFor();
		} catch (Exception e) {
			Log.e(Constant.TAG, "CollectorThread == >" + e.getMessage(), e);
			RecordLogServiceLog("CollectorThread == >" + e.getMessage());
		}
	}

	/**
	 * 每次记录日志之前先清除日志的缓存, 不然会在两个日志文件中记录重复的日志
	 */
	public void ClearLogCache() {
		Process proc = null;
		List<String> commandList = new ArrayList<String>();
		commandList.add("logcat");
		commandList.add("-c");
		try {
			proc = Runtime.getRuntime().exec(
					commandList.toArray(new String[commandList.size()]));
			StreamConsumer errorGobbler = new StreamConsumer(
					proc.getErrorStream());

			StreamConsumer outputGobbler = new StreamConsumer(
					proc.getInputStream());

			errorGobbler.start();
			outputGobbler.start();
			if (proc.waitFor() != 0) {
				Log.e(Constant.TAG, " clearLogCache proc.waitFor() != 0");
				RecordLogServiceLog("clearLogCache clearLogCache proc.waitFor() != 0");
			}
		} catch (Exception e) {
			Log.e(Constant.TAG, "clearLogCache failed", e);
			RecordLogServiceLog("clearLogCache failed");
		} finally {
			try {
				proc.destroy();
			} catch (Exception e) {
				Log.e(Constant.TAG, "clearLogCache failed", e);
				RecordLogServiceLog("clearLogCache failed");
			}
		}
	}

	/**
	 * 根据当前的存储位置得到日志的绝对存储路径
	 * 
	 * @return
	 */
	public String GetLogPath() {
		if (CurrentLogType == Constant.MEMORY_TYPE) {
			return MemoryLogPath + File.separator;
		} else {
			return SdcardLogPath + File.separator;
		}
	}

	/**
	 * 处理日志文件 1.如果日志文件存储位置切换到内存中，删除除了正在写的日志文件 并且部署日志大小监控任务，控制日志大小不超过规定值
	 * 2.如果日志文件存储位置切换到SDCard中，删除7天之前的日志，移 动所有存储在内存中的日志到SDCard中，并将之前部署的日志大小 监控取消
	 */
	public void HandleLog() {
		if (CurrentLogType == Constant.MEMORY_TYPE) {
			DeployLogSizeMonitorTask();
			DeleteMemoryExpiredLog();
		} else {
			MoveLogfile();
			CancelLogSizeMonitorTask();
			DeleteSDcardExpiredLog();
		}
	}

	/**
	 * 部署日志大小监控任务
	 */
	public void DeployLogSizeMonitorTask() {
		if (logSizeMoniting) { // 如果当前正在监控着，则不需要继续部署
			return;
		}
		logSizeMoniting = true;
		Intent intent = new Intent(Constant.MONITOR_LOG_SIZE_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(LogService, 0,
				intent, 0);
		AlarmManager am = (AlarmManager) LogService
				.getSystemService(LogService.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				Constant.MEMORY_LOG_FILE_MONITOR_INTERVAL, sender);
		Log.d(Constant.TAG, "deployLogSizeMonitorTask() succ !");
	}

	/**
	 * 取消部署日志大小监控任务
	 */
	public void CancelLogSizeMonitorTask() {
		logSizeMoniting = false;
		AlarmManager am = (AlarmManager) LogService
				.getSystemService(LogService.ALARM_SERVICE);
		Intent intent = new Intent(Constant.MONITOR_LOG_SIZE_ACTION);
		PendingIntent sender = PendingIntent.getBroadcast(LogService, 0,
				intent, 0);
		am.cancel(sender);

		Log.d(Constant.TAG, "canelLogSizeMonitorTask() succ");
	}

	/**
	 * 记录日志服务的基本信息 防止日志服务有错，在LogCat日志中无法查找 此日志名称为Log.log
	 * 
	 * @param msg
	 */
	public void RecordLogServiceLog(String msg) {
		if (LogWriter != null) {
			try {
				Date time = new Date();
				LogWriter.write(Constant.MyLogSdf.format(time) + " : " + msg);
				LogWriter.write("\n");
				LogWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(Constant.TAG, e.getMessage(), e);
			}
		}
	}
}
