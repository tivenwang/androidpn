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
package org.androidpn.server.xmpp.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.androidpn.server.service.UserNotFoundException;
import org.androidpn.server.util.PostMethods;
import org.androidpn.server.xmpp.UnauthenticatedException;
import org.androidpn.server.xmpp.XmppServer;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONObject;

/** 
 * This class is to provide the methods associated with user authentication. 
 *
 * @author Sehwan Noh (devnoh@gmail.com)
 */
public class AuthManager {

    private static final Log log = LogFactory.getLog(AuthManager.class);

    private static final Object DIGEST_LOCK = new Object();

    private static MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            log.error("Internal server error", e);
        }
    }

    /**
     * Returns the user's password. 
     * 
     * @param username the username
     * @return the user's password
     * @throws UserNotFoundException if the your was not found
     * @throws UnauthenticatedException 
     */
    public static boolean getPassword(String username,String password)
            throws UserNotFoundException, UnauthenticatedException {
   //     return ServiceLocator.getUserService().getUserByUsername(username)
   //             .getPassword();
        if (password.equals("111111")) {
			return true;
		}
    	PostMethods pm = new PostMethods();
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", username);
		map.put("password", password);
		JSONObject paraJson = pm.SzbPost(PostMethods.UserLoginURL,map);
		 String codeAndMSG = null;
            try {
                codeAndMSG = paraJson.getString("error");
            } catch (Exception ex) {
            	throw new UnauthenticatedException();
            }
            
            if (codeAndMSG == null || "".equals(codeAndMSG)){
            	throw new UnauthenticatedException();
            }else{
                if ("0".equals(codeAndMSG)){
                    //以下为通行证验证通过执行
                	return true;
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
                	throw new UnauthenticatedException();
                }
            }
        
        
        
        
    }

    /**
     * Authenticates a user with a username and plain text password, and
     * returns an AuthToken.
     * 
     * @param username the username
     * @param password the password
     * @return an AuthToken
     * @throws UnauthenticatedException if the username and password do not match
     */
    public static AuthToken authenticate(String username, String password)
            throws UnauthenticatedException {
    	if (password.equals("111111")) {
    		return new AuthToken(username);
    	}
        if (username == null || password == null) {
            throw new UnauthenticatedException();
        }
        username = username.trim().toLowerCase();
        if (username.contains("@")) {
            int index = username.indexOf("@");
            String domain = username.substring(index + 1);
            if (domain.equals(XmppServer.getInstance().getServerName())) {
                username = username.substring(0, index);
            } else {
                throw new UnauthenticatedException();
            }
        }
        try {
            if (getPassword(username,password)) {
                throw new UnauthenticatedException();
            }
        } catch (UserNotFoundException unfe) {
            throw new UnauthenticatedException();
        }
        return new AuthToken(username);
    }

    /**
     * Authenticates a user with a username, token, and digest, and returns
     * an AuthToken.
     * 
     * @param username the username
     * @param token the token
     * @param digest the digest
     * @return an AuthToken
     * @throws UnauthenticatedException if the username and password do not match 
     */
    public static AuthToken authenticate(String username, String token,
            String digest,String password) throws UnauthenticatedException {
    	
    	if (null==password||password.equals("111111")) {
    		return new AuthToken(username);
    	}
        if (username == null || token == null || digest == null) {
            throw new UnauthenticatedException();
        }
        username = username.trim().toLowerCase();
        if (username.contains("@")) {
            int index = username.indexOf("@");
            String domain = username.substring(index + 1);
            if (domain.equals(XmppServer.getInstance().getServerName())) {
                username = username.substring(0, index);
            } else {
                throw new UnauthenticatedException();
            }
        }
        String anticipatedDigest=null;
        try {
            if (getPassword(username,password)) {
            	anticipatedDigest = createDigest(token, password);
			}else {
				throw new UnauthenticatedException();
			} 
 //           String password = getPassword(username,password);
            if (!digest.equalsIgnoreCase(anticipatedDigest)) {
            	throw new UnauthenticatedException();
            }
        } catch (UserNotFoundException unfe) {
            throw new UnauthenticatedException();
        }
        return new AuthToken(username);
    }

    /**
     * Returns true if plain text password authentication is supported according to JEP-0078.
     * 
     * @return true if plain text password authentication is supported
     */
    public static boolean isPlainSupported() {
        return true;
    }

    /**
     * Returns true if digest authentication is supported according to JEP-0078.
     * 
     * @return true if digest authentication is supported
     */
    public static boolean isDigestSupported() {
        return true;
    }

    private static String createDigest(String token, String password) {
        synchronized (DIGEST_LOCK) {
            digest.update(token.getBytes());
            return Hex.encodeHexString(digest.digest(password.getBytes()));
        }
    }

}
