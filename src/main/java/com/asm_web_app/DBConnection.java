package com.asm_web_app;
import java.sql.*;

public class DBConnection {
	private static Connection conn = null;
	
	//Database Connection Establishment
	public static Connection getConnection() throws SQLException{
		final String url = "jdbc:postgresql://localhost:5432/ASM";
		final String userName = "postgres";
		final String passWord = "Sarath@0508";
		if(conn == null || conn.isClosed()) {
			conn = DriverManager.getConnection(url, userName, passWord);
		}
		return conn;
	}
	
	//Closing the Database Connection
	public static void closeConnection() throws SQLException{
		if(conn != null) {
			conn.close();
		}
	}
	
	public static ResultSet executeQuery(String query) throws SQLException{
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		if(!rs.isBeforeFirst()) {
			return null;
		}
		return rs;
	}
	
}
