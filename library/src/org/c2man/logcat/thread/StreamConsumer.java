package org.c2man.logcat.thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class StreamConsumer extends Thread {
	InputStream is;
	List<String> list;

	public StreamConsumer(InputStream is) {
		this.is = is;
	}

	public StreamConsumer(InputStream is, List<String> list) {
		this.is = is;
		this.list = list;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			char[] buffer = new char[1024];
			while ((isr.read(buffer)) > 0) {
				if (list != null) {
					list.add(buffer.toString());
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}