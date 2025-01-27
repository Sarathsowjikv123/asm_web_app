package com.UserManagementServlets;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.DBOperations.UserManagementDB;

@WebServlet("/UpdateUserInfoAndRoles")
public class UpdateUserInfoAndRoles extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int newUserID = Integer.parseInt(request.getParameter("new-user-id"));
		String newUserName = request.getParameter("new-user-name");
		int newUserTypeID = Integer.parseInt(request.getParameter("new-user-type-id"));
		
		try {
			String res = UserManagementDB.updateUserInfoAndRoles(newUserID, newUserName, newUserTypeID);
			response.getWriter().write(res);
		} catch (SQLException e) {
			response.getWriter().write(e.toString());
		}
	}

}
