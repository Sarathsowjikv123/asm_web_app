package com.AssetManagementServlets;

import java.sql.*;
import java.io.IOException;
import com.DBOperations.AssetManagementDB;
import com.AsmModels.Asset;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//Display the Active Assets
@WebServlet("/DisplayActiveAssets")
public class DisplayActiveAssets extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
		List<Asset> assetList = AssetManagementDB.displayAssets();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String responseJson = gson.toJson(assetList);
		
		response.getWriter().write(responseJson);
		}catch(SQLException e){
			System.out.println(e);
		}
	}
}