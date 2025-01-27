package com.UserManagementServlets;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.DBOperations.UserManagementDB;
import org.json.*;

@WebServlet("/DisplayUsersAndAllocatedAssets")
public class DisplayUsersAndAllocatedAssets extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONArray usersAndAssets = UserManagementDB.displayUsersAndAssets();
			String responseJson = usersAndAssets.toString(4);
			response.getWriter().write(responseJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
