package com.AssetManagementServlets;

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

@WebFilter("/AddNewAsset")
public class AddNewAssetFilter extends HttpFilter implements Filter {
      
	private static final long serialVersionUID = 1L;

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		
		String newAssetname = req.getParameter("new-assetname");
		String newAssetType = req.getParameter("new-asset-type");
		
		if(newAssetname.length() > 3) {
			newAssetType = newAssetType.toUpperCase();
			req.setAttribute("new-asset-type", newAssetType);
			chain.doFilter(req, res);
		} else {
			res.getWriter().write("Assetname must be more than 3 Characters.....");
		}
	}

	public void init(FilterConfig fConfig) throws ServletException {
	}

}
