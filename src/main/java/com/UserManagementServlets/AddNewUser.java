package com.UserManagementServlets;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.DBOperations.UserManagementDB;
import java.sql.*;

@WebServlet("/AddNewUser")
public class AddNewUser extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String newUsername = request.getParameter("new-username");
		String newUserType = request.getParameter("new-user-type");
		boolean result = false;
		try {
			result = UserManagementDB.addNewUser(newUsername, newUserType);
			if(result) {
				response.getWriter().write("User Added Successfully !!!");
			} else {
				response.getWriter().write("Error Adding User !!!");
			}
		} catch (SQLException e) {
			System.out.println(e);
		}
	}
}
