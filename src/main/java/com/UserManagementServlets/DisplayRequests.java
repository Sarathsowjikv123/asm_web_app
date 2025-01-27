package com.UserManagementServlets;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;

import com.DBOperations.AssetManagementDB;

@WebServlet("/Requests/*")
public class DisplayRequests extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		//System.out.println(pathInfo);
		JSONArray requestsArray = new JSONArray();
		if(pathInfo.equals("/DisplayAllRequests")) {
			try {
				requestsArray = AssetManagementDB.getAllRequests();
				String responseJson = requestsArray.toString(4);
				response.getWriter().write(responseJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (pathInfo.equals("/DisplayCompletedRequests")) {
			try {
				requestsArray = AssetManagementDB.getCompletedRequests();
				String responseJson = requestsArray.toString(4);
				response.getWriter().write(responseJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (pathInfo.equals("/DisplayInCompletedRequests")) {
			try {
				requestsArray = AssetManagementDB.getInCompletedRequests();
				String responseJson = requestsArray.toString(4);
				response.getWriter().write(responseJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
