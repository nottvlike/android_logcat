package org.c2man.logcat.utils;

import java.text.SimpleDateFormat;

public class Constant {
	public static final String TAG = "LogService";

	public static final int MEMORY_LOG_FILE_MAX_SIZE = 10 * 1024 * 1024; // 内存中日志文件最大值，10M
	public static final int MEMORY_LOG_FILE_MONITOR_INTERVAL = 10 * 60 * 1000; // 内存中的日志文件大小监控时间间隔，10分钟
	public static final int SDCARD_LOG_FILE_SAVE_DAYS = 7; // sd卡中日志文件的最多保存天数

	public static String MONITOR_LOG_SIZE_ACTION = "MONITOR_LOG_SIZE"; // 日志文件监测action
	public static String SWITCH_LOG_FILE_ACTION = "SWITCH_LOG_FILE_ACTION"; // 切换日志文件action

	public static final int SDCARD_TYPE = 0; // 当前的日志记录类型为存储在SD卡下面
	public static final int MEMORY_TYPE = 1; // 当前的日志记录类型为存储在内存中

	public static final String LogServiceLogName = "Log.log";// 本服务输出的日志文件名称
	public static final SimpleDateFormat MyLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static final SimpleDateFormat Sdf = new SimpleDateFormat("yyyy-MM-dd HHmmss");// 日志名称格式

}
