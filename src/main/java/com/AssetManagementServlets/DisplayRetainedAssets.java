package com.AssetManagementServlets;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.AsmModels.Asset;
import com.DBOperations.AssetManagementDB;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/DisplayRetainedAssets")
public class DisplayRetainedAssets extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			List<Asset> retainedAssetList = AssetManagementDB.displayRetainedAssets();
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			String responseJson = gson.toJson(retainedAssetList);
			response.getWriter().write(responseJson);
		} catch (SQLException e) {
			System.out.println(e);
		}
	}

}
