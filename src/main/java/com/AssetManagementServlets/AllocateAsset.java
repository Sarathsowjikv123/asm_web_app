package com.AssetManagementServlets;

import java.io.IOException;
import java.sql.SQLException;

import com.DBOperations.AssetManagementDB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/AllocateAsset")
public class AllocateAsset extends HttpServlet {
	private static final long serialVersionUID = 1L;
  
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int userID = Integer.parseInt(request.getParameter("user-id"));
		int assetID = Integer.parseInt(request.getParameter("asset-id"));
		
		String result;
		try {
			result = AssetManagementDB.allocateAsset(userID, assetID);
			response.getWriter().write(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	}

}
