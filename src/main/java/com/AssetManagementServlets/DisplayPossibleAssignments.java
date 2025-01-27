package com.AssetManagementServlets;

import java.io.IOException;
import org.json.*;
import com.DBOperations.AssetManagementDB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/DisplayPossibleAssignments")
public class DisplayPossibleAssignments extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			JSONArray possibleAssignmentsList = AssetManagementDB.displayPossibleAssignments();
			String responseJson = possibleAssignmentsList.toString(4);
			response.getWriter().write(responseJson);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
