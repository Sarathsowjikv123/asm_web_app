package com.AssetManagementServlets;

import java.io.BufferedReader;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.DBOperations.AssetManagementDB;


@WebServlet("/RemoveAsset")
public class RemoveAsset extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		StringBuilder json = new StringBuilder();
        String line;
        try(BufferedReader reader = request.getReader()){
            while((line = reader.readLine()) != null){
                json.append(line);
            }
        }
            
        JSONObject object;
		try {
			object = new JSONObject(json.toString());
			int assetID = object.getInt("assetID");

            String result = AssetManagementDB.removeAsset(assetID);
            
            response.getWriter().write(result);
           
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
