package com.DBOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.AsmModels.User;
import com.asm_web_app.DBConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserManagementDB {

	// Method for retrieving List of Users
	public static List<User> displayUsers() throws SQLException {
		DBConnection.getConnection();
		List<User> userList = new ArrayList<>();
		String query = "SELECT u.user_id, u.user_name,ut.user_type, ut.user_type_id FROM users u \n"
				+ "JOIN user_types ut USING (user_type_id) \n" + "WHERE u.is_working = TRUE ORDER BY u.user_id;";
		ResultSet rs = DBConnection.executeQuery(query);
		if (rs == null) {
			return new ArrayList<>();
		}
		while (rs.next()) {
			int userID = rs.getInt(1);
			String userName = rs.getString(2);
			User.USERTYPE userType = User.USERTYPE.valueOf(rs.getString(3));
			User user = new User(userID, userName, userType);
			userList.add(user);
		}
		DBConnection.closeConnection();
		return userList;
	}

	// Method For Retrieving The List Of Assets Allocated To Each User
	public static JSONArray displayUsersAndAssets() throws SQLException, JSONException {

		JSONArray usersAndAssets = new JSONArray();

		DBConnection.getConnection();
		String query = "SELECT u.user_id, u.user_name, STRING_AGG(aas.asset_id::text, ', ') AS allocated_asset_id, STRING_AGG(a.asset_name::text, ', ') AS allocated_asset_name FROM users u \n"
				+ "JOIN asset_assignments_summary aas USING (user_id)\n"
				+ "JOIN assets a USING (asset_id)\n"
				+ "WHERE aas.is_active = TRUE GROUP BY u.user_id ORDER BY u.user_id;";
		ResultSet rs = DBConnection.executeQuery(query);
		while (rs.next()) {
			int userID = rs.getInt(1);
			String userName = rs.getString(2);
			String assetIDs = rs.getString(3);
			String assetName = rs.getString(4);
			JSONObject usersAndAssetsObject = new JSONObject();
			usersAndAssetsObject.put("user-ID", Integer.toString(userID));
			usersAndAssetsObject.put("userName", userName);
			usersAndAssetsObject.put("asset-IDs", assetIDs);
			usersAndAssetsObject.put("assetName", assetName);
			usersAndAssets.put(usersAndAssetsObject);
		}
		DBConnection.closeConnection();
		return usersAndAssets;
	}

	// Method to get the history of all users
	public static JSONArray getHistoryOfAll() throws SQLException, JSONException {
		JSONArray historyArray = new JSONArray();
		DBConnection.getConnection();
		String query = "SELECT aas.assignment_id, aas.user_id, u.user_name, aas.asset_id, a.asset_name, aas.operation, aas.completed_date_time, aas.is_active\n"
				+ "FROM asset_assignments_summary aas \n" + "JOIN users u USING (user_id)\n"
				+ "JOIN assets a USING (asset_id)\n" + "ORDER BY aas.assignment_id;";
		ResultSet rs = DBConnection.executeQuery(query);
		while (rs.next()) {
			JSONObject history = new JSONObject();
			int assignmentID = rs.getInt(1);
			int userID = rs.getInt(2);
			String userName = rs.getString(3);
			int assetID = rs.getInt(4);
			String assetName = rs.getString(5);
			String operation = rs.getString(6);
			String completedDateTime = rs.getString(7);
			boolean isActive = rs.getBoolean(8);

			history.put("assignmentID", assignmentID);
			history.put("userID", userID);
			history.put("userName", userName);
			history.put("assetID", assetID);
			history.put("assetName", assetName);
			history.put("operation", operation);
			history.put("completedDateTime", completedDateTime);
			history.put("isActive", isActive);
			historyArray.put(history);
		}
		DBConnection.closeConnection();
		return historyArray;
	}

	// Method to get History of particular User
	public static JSONArray getHistoryByID(int userID) throws SQLException, JSONException {
		JSONArray historyArray = new JSONArray();
		Connection conn = DBConnection.getConnection();
		String query = "SELECT aas.assignment_id, aas.user_id, u.user_name, aas.asset_id, a.asset_name, aas.operation, aas.completed_date_time, aas.is_active\n"
				+ "FROM asset_assignments_summary aas\n" + "JOIN users u USING (user_id)\n"
				+ "JOIN assets a USING (asset_id)\n" + "WHERE aas.user_id = ?\n" + "ORDER BY aas.assignment_id;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setInt(1, userID);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			JSONObject historyObject = new JSONObject();
			historyObject.put("assignmentID", rs.getInt(1)); // AssignmentID
			historyObject.put("userID", rs.getInt(2)); // User-ID
			historyObject.put("userName", rs.getString(3)); // Username
			historyObject.put("assetID", rs.getString(4)); // Asset-ID
			historyObject.put("assetName", rs.getString(5)); // AssetName
			historyObject.put("operation", rs.getString(6)); // Operation(ASSIGN/RETAIN)
			historyObject.put("completedDateTime", rs.getString(7)); // CompletedDateTime
			historyObject.put("isActive", rs.getBoolean(8)); // isActive(TRUE/FALSE)
			historyArray.put(historyObject);
		}
		DBConnection.closeConnection();
		return historyArray;
	}

	// Add New User
	public static boolean addNewUser(String newUserName, String newUserType) throws SQLException {
		Connection conn = DBConnection.getConnection();
		String procedureCall = "CALL add_new_user(?, ?, ?)";
		int newUserID = 0;
		// Begin Transaction
		conn.setAutoCommit(false);
		try {
			CallableStatement stmt = conn.prepareCall(procedureCall);
			stmt.setString(1, newUserName);
			stmt.setInt(2, User.USERTYPE.valueOf(newUserType).value);
			stmt.registerOutParameter(3, Types.INTEGER);
			stmt.execute();
			newUserID = stmt.getInt(3);
			// Committing Transaction
			conn.commit();
		} catch (SQLException e) {
			// Rollback on Exception
			conn.rollback();
			System.out.println("Transaction Rollback !!!");
		} finally {
			conn.setAutoCommit(true);
			if (conn != null) {
				DBConnection.closeConnection();
			}
		}
		if (newUserID > 0) {
			return true;
		}
		return false;
	}

	// Method to remove an User
	public static String removeUser(int userID) throws SQLException {
		Connection conn = DBConnection.getConnection();
		conn.setAutoCommit(false);
		try {
			String query = "SELECT aas.asset_id AS allocated_asset_id FROM asset_assignments_summary aas\n"
					+ "WHERE aas.is_active = TRUE AND user_id = ? ORDER BY user_id;";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, userID);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {
				String q = "UPDATE users SET is_working = FALSE WHERE user_id = ?";
				PreparedStatement ps1 = conn.prepareStatement(q);
				ps1.setInt(1, userID);
				if (ps1.executeUpdate() > 0) {
					conn.commit();
					DBConnection.closeConnection();
					return "Success";
				}
			} else {
				String assets = "";
				while (rs.next()) {
					assets = assets + rs.getInt(1) + ", ";
				}
				return "User has these assets " + assets + " So, Retain the assets then try Removing.";
			}
		} catch (SQLException e) {
			conn.rollback();
		}
		DBConnection.closeConnection();
		return "Failed";
	}
	
	//Update User Info AND Roles
	public static String updateUserInfoAndRoles(int newUserID, String newUserName, int newUserTypeID) throws SQLException {
		Connection conn = DBConnection.getConnection();
		String result = "";
		//Update user Info
		String query1 = "UPDATE users SET user_name = ?, user_type_id = ? WHERE user_id = ?";
		PreparedStatement ps1 = conn.prepareStatement(query1);
		ps1.setString(1, newUserName);
		ps1.setInt(2, newUserTypeID);
		ps1.setInt(3, newUserID);
		ps1.executeUpdate();
		//Check for Allocation of Unmapped Assets		
		String query2 = "(select aas.asset_id, u.user_type_id from users u \n"
				+ "join asset_assignments_summary aas using (user_id)\n"
				+ "where is_active = true and user_id = ?)\n"
				+ "except\n"
				+ "select uam.asset_id, uam.user_type_id from user_asset_mapping uam;";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		ps2.setInt(1, newUserID);
		ResultSet rs = ps2.executeQuery();
		if(!rs.isBeforeFirst()) {
			result = "No Assets Retained ";
		} else {
			while(rs.next()) {
				if(AssetManagementDB.retainAsset(newUserID, rs.getInt(1)).equals("Success")) {
					result = result +"Asset ID = "+ rs.getInt(1) + " " + "Retained, ";
					continue;
				} else {
					result = "Failed";
				}
			}
		}
		result += "And User Updated !!!";
		return result;
	}
	
	//Method to get Assets Allocated To single user
	public static JSONArray getAssetsOfSingleUser(int userID) throws SQLException, JSONException {
	JSONArray assetArray = new JSONArray();
	try {
		Connection conn = DBConnection.getConnection();
		String query = "-- List of assets allocated to particular user\n"
				+ "SELECT aas.user_id, STRING_AGG(aas.asset_id::text, ', ') AS asset_ids, STRING_AGG(a.asset_name::text, ', ') AS asset_names\n"
				+ "FROM asset_assignments_summary aas\n"
				+ "JOIN assets a USING (asset_id)\n"
				+ "where user_id = ? AND \n"
				+ "operation='ASSIGN'::operation_type AND\n"
				+ "is_active = TRUE GROUP BY aas.user_id;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setInt(1, userID);
		ResultSet rs = ps.executeQuery();
		while(rs.next()) {
			JSONObject asset = new JSONObject();
			asset.put("userID", rs.getInt(1));
			asset.put("assetIds", rs.getString(2));
			asset.put("assetNames", rs.getString(3));
			
			assetArray.put(asset);
		}
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	DBConnection.closeConnection();
	return assetArray;
	}
}