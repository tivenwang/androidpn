package org.androidpn.server.console.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.androidpn.server.util.Config;

/** 
 * @author Pecan 
 * 类说明 
 */
public class UserFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,FilterChain arg2) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		String checkString=request.getParameter("checkString");
		String username=request.getParameter("username");
		
		if (null!=username) {
			arg2.doFilter(request, response);
			return;
		}
		if (null==checkString||!checkString.equals("ACD2883214FFB6A1BA80F37C805C09CE214D0577")) {
			response.getWriter().print("{\"result\": \"false\",\"resultId\":\"3000\",\"resultMSG\":\"No right to call this interface!\"}");
		}else {
			arg2.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		Config.classPath=arg0.getServletContext().getRealPath("/");
	}

}
