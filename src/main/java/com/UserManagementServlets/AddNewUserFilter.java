package com.UserManagementServlets;

import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

@WebFilter("/AddNewUser")
public class AddNewUserFilter extends HttpFilter implements Filter {
       
	private static final long serialVersionUID = 1L;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		String newUsername = req.getParameter("new-username");
		String newUserType = req.getParameter("new-user-type");
		
		if(newUsername.length() > 3) {
			newUserType = newUserType.toUpperCase();
			req.setAttribute("new-user-type", newUserType);
			chain.doFilter(req, res);
		} else {
			res.getWriter().write("Username must be more than 3 Characters.....");
		}

	}

	public void init(FilterConfig fConfig) throws ServletException {
	}

}
