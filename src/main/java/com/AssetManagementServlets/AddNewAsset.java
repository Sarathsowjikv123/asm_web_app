package com.AssetManagementServlets;

import java.io.IOException;

import com.DBOperations.AssetManagementDB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AddNewAsset")
public class AddNewAsset extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String newAssetname = request.getParameter("new-assetname");
		String newAssetType = request.getParameter("new-asset-type");
		int newAssetCount = Integer.parseInt(request.getParameter("new-asset-count"));
		String[] toUserTypes = request.getParameterValues("to-user-types");
		
		boolean result = false;
		try {
			result = AssetManagementDB.addNewAsset(newAssetname, newAssetType, newAssetCount, toUserTypes);
			if(result) {
				response.getWriter().write("Asset Added and Mapped Successfully !!!");
			}
		}catch(Exception e) {
			System.out.println(e);
			response.getWriter().write(e.toString());
		}
	}

}
