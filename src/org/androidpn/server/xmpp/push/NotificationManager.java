/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.androidpn.server.xmpp.push;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;

/** 
 * This class is to manage sending the notifcations to the users.  
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationManager {

    private static final String NOTIFICATION_NAMESPACE = "androidpn:iq:notification";

    private final Log log = LogFactory.getLog(getClass());

    private static SessionManager sessionManager;

    /**
     * Constructor.
     */
    public NotificationManager() {
        sessionManager = SessionManager.getInstance();
    }

    /**
     * Broadcasts a newly created notification message to all connected users.
     * 
     * @param apiKey the API key
     * @param title the title
     * @param message the message details
     * @param uri the uri
     */
    public void sendBroadcast(HashMap<Serializable, Serializable> hashMap) {
        log.warn("sendBroadcast()...");
        IQ notificationIQ = createNotificationIQ(hashMap);
        for (ClientSession session : sessionManager.getSessions()) {
            if (session.getPresence().isAvailable()) {
                notificationIQ.setTo(session.getAddress());
                session.deliver(notificationIQ);
            }
        }
    }
    
    /**
     * Sends a newly created notification message to the specific user.
     * 
     * @param apiKey the API key
     * @param title the title
     * @param message the message details
     * @param uri the uri
     */
    public void sendNotifcationToUser(String apiKey, String username, String title, String message, String uri) {
        log.warn("sendNotifcationToUser()...");
        IQ notificationIQ = createNotificationIQ(apiKey, title, message, uri);
        ClientSession session = sessionManager.getSession(username);
        if (session != null) {
            if (session.getPresence().isAvailable()) {
                notificationIQ.setTo(session.getAddress());
                session.deliver(notificationIQ);
            }
        }
    }

    public void sendNotifcationToUsers(HashMap<Serializable, Serializable> hashMap,String[] usernames){
    	log.warn("sendNotifcationToUsers()...");
    	StringBuffer stringBuffer=new StringBuffer("[");
    	if (null!=usernames&&usernames.length!=0) {
    		ClientSession session = null;
    		IQ notificationIQ = createNotificationIQ(hashMap);
			for (String username : usernames) {
				username=username.trim();
				session=sessionManager.getSession(username);
				 if (session != null) {
			            if (session.getPresence().isAvailable()) {
			            	stringBuffer.append(username).append(",");
			                notificationIQ.setTo(session.getAddress());
			                session.deliver(notificationIQ);
			            }
			        }
			}
		}
    	log.warn("成功推送用户名单："+stringBuffer.toString()+"]");
    }
    
    /**
     * Creates a new notification IQ and returns it.
     */
    private IQ createNotificationIQ(String apiKey, String title,
            String message, String uri) {
        Random random = new Random();
        String id = Integer.toHexString(random.nextInt());
        // String id = String.valueOf(System.currentTimeMillis());

        Element notification = DocumentHelper.createElement(QName.get(
                "notification", NOTIFICATION_NAMESPACE));
        notification.addElement("id").setText(id);
        notification.addElement("apiKey").setText(apiKey);
        notification.addElement("title").setText(title);
        notification.addElement("message").setText(message);
        notification.addElement("uri").setText(uri);
        notification.addElement("pushType").setText(uri);
        notification.addElement("infoid").setText(uri);
        IQ iq = new IQ();
        iq.setType(IQ.Type.set);
        iq.setChildElement(notification);

        return iq;
    }
    
    private IQ createNotificationIQ(HashMap<Serializable, Serializable> hashMap) {
        Random random = new Random();
        String id = Integer.toHexString(random.nextInt());
        hashMap.put("id", id);
        Element notification = DocumentHelper.createElement(QName.get(
                "notification", NOTIFICATION_NAMESPACE));
		if (hashMap!=null&&hashMap.size()!=0) {
			Set<Entry<Serializable, Serializable>> entrys = hashMap.entrySet();
			for (Entry<Serializable, Serializable> entry : entrys) {
				System.out.println(entry.getKey().toString()+"<><><>"+entry.getValue());
				notification.addElement(entry.getKey().toString()).setText((String) entry.getValue());
			}
		}
        IQ iq = new IQ();
        iq.setType(IQ.Type.set);
        iq.setChildElement(notification);
        return iq;
    }
    public static void main(String[] args) {
		String helloString=" yes";
		System.out.println(helloString.trim());
	}
}
