package com.asm_web_app;

import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.DBOperations.AssetManagementDB;
@WebServlet("/PrePopulate")
public class PrePopulate extends HttpServlet {
	private static final long serialVersionUID = 1L;
    public PrePopulate() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			AssetManagementDB.prePopulateAssets("Laptop", "HARDWARE", 50);
			AssetManagementDB.prePopulateAssets("Phone", "HARDWARE", 35);
			AssetManagementDB.prePopulateAssets("MS-OFFICE", "SOFTWARE", 40);
			AssetManagementDB.prePopulateUsers("Sample User 1", "MANAGER");
			AssetManagementDB.prePopulateUsers("Sample User 2", "EMPLOYEE");
			AssetManagementDB.prePopulateUsers("Sample User 3", "TRAINEE");
			response.getWriter().write("PrePopulate Success");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			response.getWriter().write(e.getMessage());
		}
	}


}
