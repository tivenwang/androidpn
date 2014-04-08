package com.onlan.servlet;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.SessionManager;

/** 
 * @author Pecan 
 * 类说明 
 */
public class UserLoginRunnable implements Runnable{
	public static ConcurrentHashMap<Date, Integer> hashMap=new ConcurrentHashMap<Date, Integer>();
	public static Integer temp=0;
	@Override
	public void run() {
        ClientSession[] sessions = new ClientSession[0];
        sessions = SessionManager.getInstance().getSessions().toArray(sessions);
        if (null!=sessions&&sessions.length!=0) {
        	if (sessions.length!=temp) {
        		hashMap.put(new Date(),sessions.length);
        		temp=sessions.length;
			}
		}
	}
}
