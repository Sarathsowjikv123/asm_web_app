package com.AssetManagementServlets;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.DBOperations.AssetManagementDB;

@WebServlet("/RetainAsset")
public class RetainAsset extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int userID, assetID;
		userID = Integer.parseInt(request.getParameter("user-id"));
		assetID = Integer.parseInt(request.getParameter("asset-id"));

		String result;
		try {
			result = AssetManagementDB.retainAsset(userID, assetID);
			response.getWriter().write(result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
