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
package org.androidpn.server.xmpp.handler;

import java.util.HashMap;
import java.util.Map;

import gnu.inet.encoding.Stringprep;
import gnu.inet.encoding.StringprepException;

import org.androidpn.server.model.User;
import org.androidpn.server.service.ServiceLocator;
import org.androidpn.server.service.UserExistsException;
import org.androidpn.server.service.UserNotFoundException;
import org.androidpn.server.service.UserService;
import org.androidpn.server.util.PostMethods;
import org.androidpn.server.xmpp.UnauthorizedException;
import org.androidpn.server.xmpp.session.ClientSession;
import org.androidpn.server.xmpp.session.Session;
import org.codehaus.jettison.json.JSONObject;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;


/** 
 * This class is to handle the TYPE_IQ jabber:iq:register protocol.
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class IQRegisterHandler extends IQHandler {

    private static final String NAMESPACE = "jabber:iq:register";

    private UserService userService;

    private Element probeResponse;

    /**
     * Constructor.
     */
    public IQRegisterHandler() {
        userService = ServiceLocator.getUserService();
        probeResponse = DocumentHelper.createElement(QName.get("query",
                NAMESPACE));
        probeResponse.addElement("username");
        probeResponse.addElement("password");
        probeResponse.addElement("email");
        probeResponse.addElement("name");
    }

    /**
     * Handles the received IQ packet.
     * 
     * @param packet the packet
     * @return the response to send back
     * @throws UnauthorizedException if the user is not authorized
     */
    public IQ handleIQ(IQ packet) throws UnauthorizedException {
        IQ reply = null;

        ClientSession session = sessionManager.getSession(packet.getFrom());
        if (session == null) {
            log.error("Session not found for key " + packet.getFrom());
            reply = IQ.createResultIQ(packet);
            reply.setChildElement(packet.getChildElement().createCopy());
            reply.setError(PacketError.Condition.internal_server_error);
            return reply;
        }

        if (IQ.Type.get.equals(packet.getType())) {
            reply = IQ.createResultIQ(packet);
            if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
                // TODO
            } else {
                reply.setTo((JID) null);
                reply.setChildElement(probeResponse.createCopy());
            }
        } else if (IQ.Type.set.equals(packet.getType())) {
            try {
                Element query = packet.getChildElement();
                if (query.element("remove") != null) {
                    if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
                        // TODO
                    } else {
                        throw new UnauthorizedException();
                    }
                } else {
                    String username = query.elementText("username");
                    String password = query.elementText("password");
                    String email = query.elementText("email");
                    String name = query.elementText("name");

                    // Verify the username
                    if (username != null) {
                    	Class.forName("gnu.inet.encoding.Composition");
                        Stringprep.nodeprep(username);
                    }

                    // Deny registration of users with no password
                    if (password == null || password.trim().length() == 0) {
                        reply = IQ.createResultIQ(packet);
                        reply.setChildElement(packet.getChildElement()
                                .createCopy());
                        reply.setError(PacketError.Condition.not_acceptable);
                        return reply;
                    }

                    if (email != null && email.matches("\\s*")) {
                        email = null;
                    }

                    if (name != null && name.matches("\\s*")) {
                        name = null;
                    }
                    User user;
                    if (session.getStatus() == Session.STATUS_AUTHENTICATED) {
                        user = userService.getUserByUsername(session.getUsername());
                        if (null==user) {
                        	user = new User();
						}
                    } else {
                        user = new User();
                    }
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setEmail(email);
                    user.setName(name);
                    
                    if (password.equals("111111")) {//游客账号的验证
                    	if (userService.checkPassword(Long.parseLong(username))!=null) {
                    		try {
                    			user=userService.getUserByUsername(user.getUsername());
                    			user.setPassword(password);
                    		} catch (UserNotFoundException e) {
                    			userService.saveUser(user);
                    		}
                    	}else {
                    		throw new UserNotFoundException();
                    	}
					}else{//注册用户的验证
						PostMethods pm = new PostMethods();
						Map<String, String> map = new HashMap<String, String>();
						map.put("username", username);
						map.put("password", password);
						JSONObject paraJson = pm.SzbPost(PostMethods.UserLoginURL,map);
						 String codeAndMSG = null;
			                try {
			                    codeAndMSG = paraJson.getString("error");
			                } catch (Exception ex) {
			                	throw new Exception();
			                }
			                
			                if (codeAndMSG == null || "".equals(codeAndMSG)){
			                	throw new Exception();
			                }else{
			                    if ("0".equals(codeAndMSG)){
			                        //以下为通行证验证通过执行
			                    	try {
		                    			user=userService.getUserByUsername(user.getUsername());
		                    		} catch (UserNotFoundException e) {
		                    			user.setPassword("111111");
		                    			userService.saveUser(user);
		                    		}
			                    }else if (codeAndMSG.startsWith("-2")){
			                        //001|用户不存在 5120
			                    	throw new UserNotFoundException();
			                    } else if (codeAndMSG.startsWith("-1")) {
			                        //003|登录失败，密码错误！您还可以尝试5次！
			                        //003|登录失败！60分钟内不能再次登录！
			                        //003|该用户已经被锁定,暂时无法登录！
			                        //003|邮箱未激活，无法登陆！
			                    	throw new IllegalArgumentException();
			                    } else {
			                    	throw new Exception();
			                    }
			                }
					}
                    
                    reply = IQ.createResultIQ(packet);
                }
            } catch (Exception ex) {
                log.debug(ex);
                reply = IQ.createResultIQ(packet);
                reply.setChildElement(packet.getChildElement().createCopy());
                if (ex instanceof UserExistsException) {
                    reply.setError(PacketError.Condition.conflict); //409
                } else if (ex instanceof StringprepException) {
                    reply.setError(PacketError.Condition.jid_malformed);//400
                } else if (ex instanceof UserNotFoundException) {
                    reply.setError(PacketError.Condition.item_not_found); //404
                }  else if (ex instanceof IllegalArgumentException) {
                    reply.setError(PacketError.Condition.not_acceptable);  //406
                } else if (ex instanceof UnauthorizedException) {
                    reply.setError(PacketError.Condition.forbidden); //403
                }else {
                    reply.setError(PacketError.Condition.internal_server_error);  //500
                }
            }
        }
        // Send the response directly to the session
        if (reply != null) {
            session.process(reply);
        }
        return null;
    }

    /**
     * Returns the namespace of the handler.
     * 
     * @return the namespace
     */
    public String getNamespace() {
        return NAMESPACE;
    }
    public static void main(String[] args) {
		System.out.println(PacketError.Condition.conflict.getLegacyCode());
		System.out.println(PacketError.Condition.jid_malformed.getLegacyCode());
		System.out.println(PacketError.Condition.forbidden.getLegacyCode());
		System.out.println(PacketError.Condition.not_acceptable.getLegacyCode());
		System.out.println(PacketError.Condition.internal_server_error.getLegacyCode());
		System.out.println(PacketError.Condition.fromLegacyCode(401));
	}
}
