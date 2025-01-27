package com.UserManagementServlets;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;

import com.DBOperations.UserManagementDB;

@WebServlet("/DisplayAssetsOfSingleUser/*")
public class DisplayAssetsOfSingleUser extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		//System.out.println(pathInfo);
		int userID = Integer.parseInt(pathInfo.substring(1));
		try {
			JSONArray assetArray = UserManagementDB.getAssetsOfSingleUser(userID);
			String responseJson = assetArray.toString(4);
			response.getWriter().write(responseJson);
		} catch(Exception e) {
			System.out.println(e);
		}
	}

}
