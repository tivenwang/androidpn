package org.androidpn.server.console.controller;

import java.io.Serializable;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.androidpn.server.util.Config;
import org.androidpn.server.xmpp.push.NotificationManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/** 
 * @author Pecan 
 * 类说明 
 */
public class NotificationForServerController extends MultiActionController{

	private Logger logger=Logger.getLogger(NotificationForServerController.class);
    private NotificationManager notificationManager;
    
	public NotificationForServerController() {
        notificationManager = new NotificationManager();
	}

	public void send(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String broadcast = ServletRequestUtils.getStringParameter(request,"broadcast", "0");
        String usernames = request.getParameter("usernames");
        String title = ServletRequestUtils.getStringParameter(request, "title");
        String message = ServletRequestUtils.getStringParameter(request,"message");
        String uri = ServletRequestUtils.getStringParameter(request, "uri","");
        String infoType = ServletRequestUtils.getStringParameter(request, "infoType");
        String infoId = ServletRequestUtils.getStringParameter(request, "infoId");
        String apiKey = Config.getString("apiKey", "");
        
        HashMap<Serializable, Serializable> hashMap=new HashMap<Serializable, Serializable>();
        hashMap.put("apiKey", apiKey);
        hashMap.put("title", title);
        hashMap.put("message", message);
        hashMap.put("uri", uri);
        hashMap.put("infoid",infoId);
        hashMap.put("infoType", infoType);
        logger.warn("接收参数列表："+hashMap.toString());
        if (broadcast.equals("0")) {
            notificationManager.sendBroadcast(hashMap);
        } else {
        	logger.warn("接收客户端参数："+usernames.toString());
        	usernames=usernames.replace("[", "").replace("]", "");
        	logger.warn("接收用户列表："+usernames.toString());
            notificationManager.sendNotifcationToUsers(hashMap, usernames.split(","));
        }
	}
}
