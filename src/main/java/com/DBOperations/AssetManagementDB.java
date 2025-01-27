package com.DBOperations;

import com.asm_web_app.DBConnection;
import com.AsmModels.Asset;
import com.AsmModels.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.*;

public class AssetManagementDB {

	// Method for retrieving List of Assets
	public static List<Asset> displayAssets() throws SQLException {
		DBConnection.getConnection();
		List<Asset> assetList = new ArrayList<>();
		String query = "SELECT a.asset_id, a.asset_name, a.asset_type, ac.asset_count FROM assets a \n"
				+ "JOIN asset_count ac USING (asset_id)\n" + "WHERE a.is_giving = TRUE  ORDER BY a.asset_id;";
		ResultSet rs = DBConnection.executeQuery(query);
		if(rs == null) {
			return new ArrayList<>();
		}
		while (rs.next()) {
			int assetID = rs.getInt(1);
			String assetName = rs.getString(2);
			Asset.ASSETTYPE assetType = Asset.ASSETTYPE.valueOf(rs.getString(3));
			int assetCount = rs.getInt(4);
			Asset asset = new Asset(assetID, assetName, assetType, assetCount);
			assetList.add(asset);
		}
		DBConnection.closeConnection();
		return assetList;
	}

	// Method For Retrieving list of retained Assets
	public static List<Asset> displayRetainedAssets() throws SQLException {
		DBConnection.getConnection();
		List<Asset> retainedAssetList = new ArrayList<>();
		String query = "SELECT ra.asset_id, a.asset_name, a.asset_type, ra.retained_asset_count FROM retained_assets ra\n"
				+ "JOIN assets a USING (asset_id);";
		ResultSet rs = DBConnection.executeQuery(query);
		if(rs == null) {
			return new ArrayList<>();
		}
		while (rs.next()) {
			int retainedAssetID = rs.getInt(1);
			String retainedAssetName = rs.getString(2);
			Asset.ASSETTYPE retainedAssetType = Asset.ASSETTYPE.valueOf(rs.getString(3));
			int retainedAssetCount = rs.getInt(4);
			Asset asset = new Asset(retainedAssetID, retainedAssetName, retainedAssetType, retainedAssetCount);
			retainedAssetList.add(asset);
		}
		DBConnection.closeConnection();
		return retainedAssetList;
	}

	// Method for retrieving all possible assignments for an user
	public static JSONArray displayPossibleAssignments() throws SQLException, JSONException {
		DBConnection.getConnection();
		JSONArray possibleAssignmentsList = new JSONArray();
		String query = "SELECT u.user_id, u.user_name, u.user_type_id, ut.user_type, STRING_AGG(uam.asset_id::TEXT, ', ') AS asset_ids,\n"
				+ "STRING_AGG(a.asset_name, ', ') AS asset_names FROM users u\n"
				+ "JOIN user_asset_mapping uam USING (user_type_id)\n"
				+ "JOIN assets a USING (asset_id)\n"
				+ "JOIN user_types ut USING (user_type_id)\n"
				+ "WHERE a.is_giving = TRUE AND is_working = TRUE\n"
				+ "GROUP BY u.user_id, ut.user_type\n"
				+ "ORDER BY u.user_id;";
		ResultSet rs = DBConnection.executeQuery(query);
		while (rs.next()) {
			JSONObject possible = new JSONObject();
			int userID = rs.getInt(1);
			String userName = rs.getString(2);
			int userTypeID = rs.getInt(3);
			String userType = rs.getString(4);
			String assetIDs = rs.getString(5);
			String assetNames = rs.getString(6);

			possible.put("userID", userID);
			possible.put("userName", userName);
			possible.put("userTypeID", userTypeID);
			possible.put("userType", userType);
			possible.put("assetIDs", assetIDs);
			possible.put("assetNames", assetNames);
			possibleAssignmentsList.put(possible);
		}

		DBConnection.closeConnection();
		return possibleAssignmentsList;
	}

	// Retrieving the list of All Requests
	public static JSONArray getAllRequests() throws SQLException, JSONException {
		DBConnection.getConnection();
		String query = "SELECT rt.request_id, rt.assignment_id, aas.user_id, u.user_name, aas.asset_id, a.asset_name, aas.operation, rt.requested_date_time, aas.completed_date_time\n"
				+ "FROM request_table rt \n" + "JOIN asset_assignments_summary aas using (assignment_id)\n"
				+ "JOIN users u USING (user_id)\n"
				+ "JOIN assets a USING (asset_id) ORDER BY rt.requested_date_time ASC;";
		ResultSet rs = DBConnection.executeQuery(query);
		JSONArray requestsArray = displayRequests(rs);
		return requestsArray;
	}

	// Retrieving the List of Completed Requests
	public static JSONArray getCompletedRequests() throws SQLException, JSONException {
		DBConnection.getConnection();
		String query = "SELECT rt.request_id, rt.assignment_id, aas.user_id, u.user_name, aas.asset_id, a.asset_name, aas.operation, rt.requested_date_time, aas.completed_date_time\n"
				+ "FROM request_table rt \n" + "JOIN asset_assignments_summary aas using (assignment_id)\n"
				+ "JOIN users u USING (user_id)\n" + "JOIN assets a USING (asset_id) \n"
				+ "WHERE aas.completed_date_time IS NOT NULL \n" + "ORDER BY rt.requested_date_time ASC;";
		ResultSet rs = DBConnection.executeQuery(query);
		JSONArray requestsArray = displayRequests(rs);
		return requestsArray;
	}

	// Retrieving the List of Incompleted Requests
	public static JSONArray getInCompletedRequests() throws SQLException, JSONException {
		DBConnection.getConnection();
		String query = "SELECT rt.request_id, rt.assignment_id, aas.user_id, u.user_name, aas.asset_id, a.asset_name, aas.operation, rt.requested_date_time, aas.completed_date_time\n"
				+ "FROM request_table rt \n" + "JOIN asset_assignments_summary aas using (assignment_id)\n"
				+ "JOIN users u USING (user_id)\n" + "JOIN assets a USING (asset_id) \n"
				+ "WHERE aas.completed_date_time IS NULL \n" + "ORDER BY rt.requested_date_time ASC;";
		ResultSet rs = DBConnection.executeQuery(query);
		JSONArray requestsArray = displayRequests(rs);
		return requestsArray;
	}

	// Display the Requests List
	public static JSONArray displayRequests(ResultSet rs) throws SQLException, JSONException {
		JSONArray requestsArray = new JSONArray();
		while (rs.next()) {
			JSONObject request = new JSONObject();
			request.put("requestID", rs.getInt(1)); // Request-ID
			request.put("assignmentId", rs.getInt(2)); // Assignment-ID
			request.put("userID", rs.getInt(3)); // User-ID
			request.put("userName", rs.getString(4)); // Username
			request.put("assetID", rs.getInt(5)); // Asset-ID
			request.put("assetName", rs.getString(6)); // AssetName
			request.put("Operation", rs.getString(7)); // Operation(ASSIGN/RETAIN)
			request.put("RequestedDateTime", rs.getString(8)); // RequestedDateTime
			request.put("CompletedDateTime", rs.getString(9)); // CompletedDateTime
			requestsArray.put(request);

		}
		return requestsArray;
	}

	// Add New Asset And Map Asset to Users Types
	public static boolean addNewAsset(String assetName, String assetType, int assetCount, String[] toUserTypes)
			throws SQLException {
		Connection conn = DBConnection.getConnection();
		// Begin transaction
		conn.setAutoCommit(false);
		int newAssetID = 0;
		String procedureCall1 = "CALL add_new_asset(?, ?, ?, ?)";
		try {
			CallableStatement stmt1 = conn.prepareCall(procedureCall1);
			stmt1.setString(1, assetName);
			stmt1.setString(2, assetType);
			stmt1.setInt(3, assetCount);
			stmt1.registerOutParameter(4, Types.INTEGER);
			stmt1.execute();
			newAssetID = stmt1.getInt(4);

			String procedureCall2 = "CALL asset_and_user_type_mapping(?, ?, ?)";
			CallableStatement stmt2 = conn.prepareCall(procedureCall2);
			for (String toUserType : toUserTypes) {
				stmt2.setInt(1, newAssetID);
				stmt2.setInt(2, User.USERTYPE.valueOf(toUserType).value);
				stmt2.registerOutParameter(3, Types.INTEGER);
				stmt2.execute();
			}
			// Commit Transaction
			conn.commit();
			if (stmt2.getInt(3) > 0) {
				return true;
			}
		} catch (Exception e) {
			// Rollback
			conn.rollback();
			System.out.println(e);
			System.out.println("Transaction Rollback !!!");
		} finally {
			conn.setAutoCommit(true);
			if (conn != null) {
				DBConnection.closeConnection();
			}
		}
		return false;
	}

	// Method to Allocate an asset to an user
	public static String allocateAsset(int userID, int assetID) throws SQLException {
		Connection conn = DBConnection.getConnection();
		conn.setAutoCommit(false);
		try {
			String query = "CALL allocate_asset(?, ?, ?)";
			CallableStatement ps = conn.prepareCall(query);
			ps.setInt(1, userID);
			ps.setInt(2, assetID);
			ps.registerOutParameter(3, Types.VARCHAR);
			ps.execute();
			String res = ps.getString(3);
			conn.commit();
			return res;
		} catch (SQLException e) {
			conn.rollback();
			return e.toString();
		} finally {
			if (conn != null) {
				// conn.setAutoCommit(true);
				DBConnection.closeConnection();
			}
		}
	}

	// Method to Raise Request
	public static String raiseRequest(int userID, int assetID, String operation) throws SQLException {
		Connection conn = DBConnection.getConnection();
		conn.setAutoCommit(false);
		try {
			String query = "CALL raise_request(?, ?, ?, ?, ?)";
			CallableStatement ps = conn.prepareCall(query);
			ps.setInt(1, userID);
			ps.setInt(2, assetID);
			ps.setString(3, operation);
			ps.registerOutParameter(4, Types.VARCHAR);
			ps.registerOutParameter(5, Types.BOOLEAN);
			ps.execute();
			String msg = ps.getString(4);
			//boolean res = ps.getBoolean(5);
			conn.commit();
			return msg;
		} catch (SQLException e) {
			conn.rollback();
			return e.toString();
		} finally {
			if (conn != null) {
				conn.setAutoCommit(true);
				DBConnection.closeConnection();
			}
		}
	}

	// Method to update Asset Information
	public static boolean updateAssetInfo(int assetID, String assetName, String assetType, int assetCount)
			throws SQLException {
		Connection conn = DBConnection.getConnection();

		String query1 = "UPDATE assets SET asset_name = ?, asset_type = ?::asset_type\n" + "	WHERE asset_id = ?;";
		PreparedStatement ps1 = conn.prepareStatement(query1);
		ps1.setString(1, assetName);
		ps1.setString(2, assetType);
		ps1.setInt(3, assetID);

		String query2 = "UPDATE asset_count SET asset_count = ? WHERE asset_id = ?";
		PreparedStatement ps2 = conn.prepareStatement(query2);
		ps2.setInt(1, assetCount);
		ps2.setInt(2, assetID);

		conn.setAutoCommit(false);

		try {
			if (ps1.executeUpdate() > 0 & ps2.executeUpdate() > 0) {
				conn.commit();
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			conn.rollback();
			return false;
		} finally {
			conn.setAutoCommit(true);
			DBConnection.closeConnection();
		}
	}

	// Method to Retain an asset from an user
	public static String retainAsset(int userID, int assetID) throws SQLException {
		Connection conn = DBConnection.getConnection();
		conn.setAutoCommit(false);
		try {
			String query = "CALL retain_asset(?, ?, ?)";
			CallableStatement ps = conn.prepareCall(query);
			ps.setInt(1, userID);
			ps.setInt(2, assetID);
			ps.registerOutParameter(3, Types.VARCHAR);
			ps.execute();
			String res = ps.getString(3);
			conn.commit();
			return res;
		} catch (SQLException e) {
			conn.rollback();
			return e.toString();
		} finally {
			if (conn != null) {
				conn.setAutoCommit(true);
				// DBConnection.closeConnection();
			}
		}
	}

	// Method to remove an asset
	public static String removeAsset(int assetID) throws SQLException {
		Connection conn = DBConnection.getConnection();
		conn.setAutoCommit(false);
		try {
			String query = "SELECT user_id FROM asset_assignments_summary \n" + "where asset_id = ?\n"
					+ "and operation = 'ASSIGN'::operation_type\n" + "and is_active = true;";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setInt(1, assetID);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {
				String q = "update assets set is_giving = false where asset_id = ?";
				PreparedStatement ps1 = conn.prepareStatement(q);
				ps1.setInt(1, assetID);
				if (ps1.executeUpdate() > 0) {
					conn.commit();
					DBConnection.closeConnection();
					return "Success";
				}
			} else {
				String users = "";
				while (rs.next()) {
					users = users + rs.getInt(1) + ", ";
				}
				return "Asset was Currently Allocated to these users " + users
						+ " So, Retain the assets then try Removing.";
			}
		} catch (SQLException e) {
			conn.rollback();
		}
		DBConnection.closeConnection();
		return "Failed";
	}

	// Method to Retain All Assets
	public static String retainAllAssets(int userID) throws SQLException {
		Connection conn = DBConnection.getConnection();
		String query = "SELECT asset_id FROM asset_assignments_summary \n" + "	where user_id = ? AND \n"
				+ "	operation='ASSIGN'::operation_type AND\n" + "	is_active = TRUE;";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setInt(1, userID);
		ResultSet rs = ps.executeQuery();
		if (!rs.isBeforeFirst()) {
			return "No assets Allocated";
		} else {
			while (rs.next()) {
				System.out.println(retainAsset(userID, rs.getInt(1)));
			}
			return "Success";
		}
	}
	
	//Prepopulate Assets
	public static void prePopulateAssets(String assetName, String assetType, int assetCount) throws SQLException {
		boolean result;
		Connection conn = DBConnection.getConnection();
		String query = "SELECT 1 WHERE EXISTS (SELECT asset_name, asset_type FROM assets where asset_name = ? and asset_type = ?)";
		PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, assetName);
        ps.setString(2, assetType);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            result = true;
        }else{
            result = false;
        }
        String[] toAssetTypes = {"MANAGER", "EMPLOYEE", "TRAINEE"};
        if(!result){
            addNewAsset(assetName, assetType, assetCount, toAssetTypes);
        }
        DBConnection.closeConnection();
	}
	
	//Prepopulate Users
	public static void prePopulateUsers(String userName, String userType) throws SQLException {
		boolean result;
		Connection conn = DBConnection.getConnection();
		String query = "SELECT 1 WHERE EXISTS (SELECT asset_name, asset_type FROM assets where asset_name = ? and asset_type = ?)";
		PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, userName);
        ps.setString(2, userType);
        ResultSet rs = ps.executeQuery();
        if(rs.next()){
            result = true;
        }else{
            result = false;
        }
        if(!result){
            UserManagementDB.addNewUser(userName, userType);
        }
        DBConnection.closeConnection();
	}


}
