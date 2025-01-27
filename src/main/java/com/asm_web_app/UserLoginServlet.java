package com.asm_web_app;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;

@WebServlet("/UserLoginServlet")
public class UserLoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int userID = Integer.parseInt(request.getParameter("user-id"));

		HttpSession session = request.getSession();
		if (verifyCredentials(userID)) {
			session.setAttribute("user-id", userID);
			response.sendRedirect("UserPage.jsp");
		} else {
			response.sendRedirect("/asm_web_app");
		}
	}
	
	private boolean verifyCredentials(int userID) {
		try {
			DBConnection.getConnection();
			ResultSet rs = DBConnection.executeQuery("SELECT user_id FROM users WHERE is_working = TRUE");
			if(rs == null) {
				return false;
			}
			while (rs.next()) {
				int userId = rs.getInt(1);
				if (userId == userID) {
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
