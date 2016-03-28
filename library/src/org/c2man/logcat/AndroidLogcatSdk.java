package org.c2man.logcat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.c2man.logcat.service.LogService;
import org.c2man.logcat.utils.LogHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class AndroidLogcatSdk {

	private static AndroidLogcatSdk _instance;

	
	private final String FTP_PATH = "";
	private final String SETTING_NAME = "AndroidLogcatBoolean";
	
	private Intent _service;
	private String _url;
	private int _port;
	private String _userName;
	private String _password;
	private String _prefix;

	public Activity MainActivity;

	public static AndroidLogcatSdk Instance() {
		if (_instance == null)
			_instance = new AndroidLogcatSdk();

		return _instance;
	}

	public void Init(Activity activity, String url, int port, String userName,
			String password, String prefix) {
		if (activity == null)
			return;

		MainActivity = activity;
		_url = url;
		_port = port;
		_userName = userName;
		_password = password;
		_prefix = prefix;

		_service = new Intent(MainActivity, LogService.class);
		MainActivity.startService(_service);
	}

	public boolean UploadCurrentLog() {
		boolean success = false;
		FTPClient ftp = new FTPClient();
		try {
			int reply;
			ftp.connect(_url, _port);// 连接FTP服务器
			// 如果采用默认端口，可以使用ftp.connect(url)的方式直接连接FTP服务器
			ftp.login(_userName, _password);// 登录
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return success;
			}
			FileInputStream input = new FileInputStream(new File(LogHelper.Instance().GetLogPath()
					+ LogHelper.Instance().CurrentInstallLogName));
			ftp.changeWorkingDirectory(FTP_PATH);
			ftp.storeFile(_prefix + LogHelper.Instance().CurrentInstallLogName, input);

			input.close();
			ftp.logout();
			success = true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return success;
	}

	public void EnableLogcat(Activity activity, boolean isEnable)
	{
		SharedPreferences settings = activity.getSharedPreferences(activity.getPackageName(), 0);  
		SharedPreferences.Editor editor = settings.edit();  
		editor.putBoolean(SETTING_NAME, isEnable);
		editor.commit();
	}
	
	public boolean IsEnabled(Activity activity)
	{
		if(activity == null)
			return false;
		
		SharedPreferences settings = activity.getSharedPreferences(activity.getPackageName(), 0);
		return settings.getBoolean(SETTING_NAME, false);
	}
	
	public void Destroy() {
		if (MainActivity != null && _service != null)
			MainActivity.stopService(_service);
	}
}
