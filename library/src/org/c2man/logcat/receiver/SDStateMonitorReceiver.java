package org.c2man.logcat.receiver;

import org.c2man.logcat.thread.LogCollectorThread;
import org.c2man.logcat.utils.Constant;
import org.c2man.logcat.utils.FileHelper;
import org.c2man.logcat.utils.LogHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * ¼à¿ØSD¿¨×´Ì¬
 * 
 * @author Administrator
 * 
 */
public class SDStateMonitorReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {

		if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) { // ´æ´¢¿¨±»Ð¶ÔØ
			if (FileHelper.Instance().GetCurrLogType() == Constant.SDCARD_TYPE) {
				Log.d(Constant.TAG, "SDcar is UNMOUNTED");
				LogHelper.Instance().CurrentLogType = Constant.MEMORY_TYPE;
				new LogCollectorThread().start();
			}
		} else { // ´æ´¢¿¨±»¹ÒÔØ
			if (FileHelper.Instance().GetCurrLogType() == Constant.MEMORY_TYPE) {
				Log.d(Constant.TAG, "SDcar is MOUNTED");
				LogHelper.Instance().CurrentLogType = Constant.SDCARD_TYPE;
				new LogCollectorThread().start();

			}
		}
	}
}