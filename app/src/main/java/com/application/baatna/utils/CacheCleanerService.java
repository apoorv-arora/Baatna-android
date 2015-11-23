package com.application.baatna.utils;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CacheCleanerService  extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			File fileDirectory = new File(getApplicationContext().getCacheDir().getAbsolutePath());
			
			if(fileDirectory.isDirectory()) {
				File files[] = fileDirectory.listFiles();
				for (int i=0; i < files.length; i++) {
					if((files[i].getName().endsWith("jpg"))  
							||files[i].getName().endsWith("png")
							||files[i].getName().endsWith("jpeg")
							||files[i].getName().endsWith("userlarge")) {

						if(System.currentTimeMillis() - files[i].lastModified() > 2*24*60*60*1000) {
							files[i].delete();
						}
					}
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		stopSelf();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}