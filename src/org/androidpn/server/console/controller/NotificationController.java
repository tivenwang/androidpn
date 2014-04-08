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
package org.androidpn.server.console.controller;

import java.io.Serializable;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.androidpn.server.util.Config;
import org.androidpn.server.xmpp.push.NotificationManager;
import org.apache.log4j.Logger;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/** 
 * A controller class to process the notification related requests.  
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class NotificationController extends MultiActionController {

	private Logger logger=Logger.getLogger(NotificationController.class);
    private NotificationManager notificationManager;

    public NotificationController() {
        notificationManager = new NotificationManager();
    }

    public ModelAndView list(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        ModelAndView mav = new ModelAndView();
        // mav.addObject("list", null);
        mav.setViewName("notification/form");
        return mav;
    }

    public ModelAndView send(HttpServletRequest request,HttpServletResponse response) throws Exception {
        String broadcast = ServletRequestUtils.getStringParameter(request,"broadcast", "Y");
        String username = ServletRequestUtils.getStringParameter(request,"username");
        String title = ServletRequestUtils.getStringParameter(request, "title");
        String message = ServletRequestUtils.getStringParameter(request,"message");
        String uri = ServletRequestUtils.getStringParameter(request, "uri","");
        String apiKey = Config.getString("apiKey", "");
        logger.debug("apiKey=" + apiKey);

        HashMap<Serializable, Serializable> hashMap=new HashMap<Serializable, Serializable>();
        hashMap.put("apiKey", apiKey);
        hashMap.put("title", title);
        hashMap.put("message", message);
        hashMap.put("uri", uri);
        hashMap.put("infoid","1");
        hashMap.put("infoType", "2");
        logger.warn("接收参数列表："+hashMap.toString());
        if (broadcast.equalsIgnoreCase("Y")) {
            notificationManager.sendBroadcast(hashMap);
        } else {
        	logger.warn("接收用户列表："+username.toString());
            notificationManager.sendNotifcationToUsers(hashMap, username.split(","));
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("redirect:notification.do");
        return mav;
    }

}
