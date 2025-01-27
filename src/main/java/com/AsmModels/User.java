package com.AsmModels;

public class User {
	//Enum for User Types
	public enum USERTYPE{
		MANAGER(1),EMPLOYEE(2),TRAINEE(3);
		public int value;
		private USERTYPE(int value) {
			this.value = value;
		}
	}
	private int userID;
	private String userName;
	private USERTYPE userType;
	private int userTypeID;
	
	public User(int userID, String userName, USERTYPE userType) {
		this.userID = userID;
		this.userName = userName;
		this.userType = userType;
		this.userTypeID = userType.value;
	}
	
	public int getUserID() {
		return userID;
	}
	public String getUserName() {
		return userName;
	}
	public USERTYPE getUserType() {
		return userType;
	}
	public int getUserTypeID() {
		return userTypeID;
	}
	
}
