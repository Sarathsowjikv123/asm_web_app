package com.AssetManagementServlets;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.DBOperations.AssetManagementDB;

@WebServlet("/RaiseRequest")
public class RaiseRequest extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int userID = Integer.parseInt(request.getParameter("user-id"));
		int assetID = Integer.parseInt(request.getParameter("asset-id"));
		String operation = request.getParameter("operation");
		
		try {
			String result = AssetManagementDB.raiseRequest(userID, assetID, operation);
			response.getWriter().write(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
