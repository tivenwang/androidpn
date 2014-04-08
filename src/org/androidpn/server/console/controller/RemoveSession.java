package org.androidpn.server.console.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.androidpn.server.xmpp.session.SessionManager;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/** 
 * @author Pecan 
 * 类说明 
 */
public class RemoveSession extends MultiActionController{

	
	public void list(HttpServletRequest request,HttpServletResponse response){
		
		try {
		SessionManager sessionManager=SessionManager.getInstance();
		if (sessionManager.removeSession(sessionManager.getSession(request.getParameter("username")))) {
			response.getWriter().print("{\"result\": \"true\",\"resultId\":\"1000\",\"resultMSG\":\"remove session success!\"}");
		}else {
			response.getWriter().print("{\"result\": \"false\",\"resultId\":\"3000\",\"resultMSG\":\"session not exist or remove session fail!\"}");
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
