package com.UserManagementServlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.AsmModels.User;
import com.DBOperations.UserManagementDB;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/DisplayActiveUsers")
public class DisplayActiveUsers extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			List<User> userList = UserManagementDB.displayUsers();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String responseJson = gson.toJson(userList);
			
			response.getWriter().write(responseJson);
			}catch(SQLException e){
				System.out.println(e);
			}
	}
}
