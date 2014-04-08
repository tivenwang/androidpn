package com.onlan.servlet;

import java.io.Serializable;
import java.util.HashMap;

import org.androidpn.server.util.Config;
import org.androidpn.server.xmpp.push.NotificationManager;
import org.apache.log4j.Logger;


/** 
 * @author Pecan 
 * 类说明 
 */
public class UserBroadCastRunnable  implements Runnable{

	private Logger logger=Logger.getLogger(UserBroadCastRunnable.class);
	@Override
	public void run() {
		NotificationManager notificationManager=new NotificationManager();
		logger.warn("定时任务开始");
        String apiKey = Config.getString("apiKey", "");
        HashMap<Serializable, Serializable> hashMap=new HashMap<Serializable, Serializable>();
        hashMap.put("apiKey", apiKey);
        hashMap.put("title", "0");
        hashMap.put("message", "0");
        hashMap.put("uri", "0");
        hashMap.put("infoid","0");
        hashMap.put("infoType", "5");
		notificationManager.sendBroadcast(hashMap);
	}
}
