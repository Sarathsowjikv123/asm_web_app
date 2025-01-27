package com.AssetManagementServlets;

import java.io.BufferedReader;
import org.json.*;

import com.DBOperations.AssetManagementDB;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/UpdateAssetInfo")
public class UpdateAssetInfo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            String assetName = object.getString("assetName");
            String assetType = object.getString("assetType");
            int assetCount = object.getInt("assetCount");

            boolean result = AssetManagementDB.updateAssetInfo(assetID, assetName, assetType, assetCount);
            
            if(result) {
            	response.getWriter().write("Asset Info Updated Successfully !!!");
            } else {
            	response.getWriter().write("Asset Info Not Updated !!!");
            }
            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
