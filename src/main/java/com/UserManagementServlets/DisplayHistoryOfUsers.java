package com.UserManagementServlets;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import com.DBOperations.UserManagementDB;

@WebServlet("/DisplayHistoryOfUsers/*")
public class DisplayHistoryOfUsers extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String pathInfo = request.getPathInfo();
		//System.out.println(pathInfo);
		if (pathInfo.equals("/") || pathInfo == null) {
			try {
				JSONArray allHistoryArray = UserManagementDB.getHistoryOfAll();
				String responseJson = allHistoryArray.toString(4);
				response.getWriter().write(responseJson);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			int userID = Integer.parseInt(pathInfo.substring(1));
			try {
				JSONArray historyArray = UserManagementDB.getHistoryByID(userID);
				String responseJson = historyArray.toString(4);
				response.getWriter().write(responseJson);
			} catch (java.lang.NumberFormatException e) {
				response.getWriter().write("User-ID Must be a Number !!!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
