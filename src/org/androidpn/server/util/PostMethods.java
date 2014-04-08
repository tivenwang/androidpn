/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.androidpn.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;

/**
 * 
 * @author Pecan 
 * 类说明：
 * 上证报通行证接口调用方法
 */

public class PostMethods {

    private final static Logger log = Logger.getLogger(PostMethods.class);
    private HttpURLConnection conn = null;
    public static String CheckNameURL;
    public static String RegUserURL;
    public static String UserLoginURL;
    public static String ChangePasswordURL;
    public static String GetBindMobileURL;
    public static String BindingMobileURL;
    public static String GetBindEmailURL;
    public static String BindingEmailURL;
    public static String ResetPasswordByMobileURL;
    public static String ResetPasswordByEmailURL;
    public static String CheckNameAndPhoneIsMatchedURL;
    public static String SendSMSURL;
    
    static{
        try {
            UserLoginURL = Config.getString("url");
            if (null==UserLoginURL||UserLoginURL.equals("")) {
            	UserLoginURL = "http://app.cnstock.com/api/account/mob_user_login";
			}
        } catch (Exception e) {
            log.error(e);
        }
    }
    
    public JSONObject SzbPost(String urlAddr, Map<?, ?> map) {
        JSONObject paraJson = null;
        StringBuilder params = new StringBuilder();
        Iterator<?> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<?, ?> element = (Entry<?, ?>) it.next();
            try {
                params.append(URLEncoder.encode(element.getKey().toString(), "UTF-8"));
                params.append("=");
                params.append(URLEncoder.encode(element.getValue().toString(), "UTF-8"));
                params.append("&");
            } catch (UnsupportedEncodingException ex) {
                log.error("URLEncoder转换异常"+ex);
            }
        }

        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }

        BufferedReader br = null;
        try {
            URL url = new URL(urlAddr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(params.length()));
            conn.setDoInput(true);
            conn.connect();
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(params.toString());
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            if (code == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                paraJson = new JSONObject(sb.toString());           
            }
        } catch (Exception ex) {
            log.error("调用通行证接口异常,调用接口地址:"+urlAddr+",  "+ex);
        } finally {
            if (br != null){
                try {
                    br.close();
                } catch (IOException ex) {
                    log.error("调用通行证接口 关闭输入流异常, "+ex);
                }
            }   
            conn.disconnect();
        }
        return paraJson;
    }
}
