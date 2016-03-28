package org.c2man.logcat.process;

public class ProcessInfo {
	public String user;
	public String pid;
	public String ppid;
	public String name;

	@Override
	public String toString() {
		String str = "user=" + user + " pid=" + pid + " ppid=" + ppid
				+ " name=" + name;
		return str;
	}
}