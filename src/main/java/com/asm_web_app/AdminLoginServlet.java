package com.asm_web_app;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.*;

@WebServlet("/AdminLoginServlet")
public class AdminLoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String username = request.getParameter("admin-username");
		String password = request.getParameter("admin-password");

		HttpSession session = request.getSession();

		if (verifyCredentials(username, password)) {
			session.setAttribute("admin-username", username);
			session.setAttribute("admin-password", password);
			response.sendRedirect("AdminPage.jsp");
		} else {
			response.sendRedirect("/asm_web_app");
		}

	}

	// Credentials Verification
	private boolean verifyCredentials(String username, String password) {
		try {
			DBConnection.getConnection();
			ResultSet rs = DBConnection.executeQuery("SELECT * FROM admin_credentials");
			while (rs.next()) {
				String adminUsername = rs.getString(1);
				String adminPassword = rs.getString(2);
				if (username.equals(adminUsername) && password.equals(adminPassword)) {
					return true;
				}
			}
			DBConnection.closeConnection();
		} catch (SQLException e) {
			System.out.println(e + "Cannot Conect To the Database !!!");
		}
		return false;
	}

}
