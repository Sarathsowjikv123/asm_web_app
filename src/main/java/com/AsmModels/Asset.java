package com.AsmModels;


public class Asset {
	//Enum for Asset Types
	public static enum ASSETTYPE{
		HARDWARE, SOFTWARE
	}
	private int assetID;
	private String assetName;
	private ASSETTYPE assetType;
	private int assetCount;
	
	public Asset(int assetID, String assetName, ASSETTYPE assetType, int assetCount) {
		this.assetID = assetID;
		this.assetName = assetName;
		this.assetType = assetType;
		this.assetCount = assetCount;
	}
	
	public int getAssetID() {
		return assetID;
	}
	public String getAssetName() {
		return assetName;
	}
	public ASSETTYPE getAssetType() {
		return assetType;
	}
	public int getAssetCount() {
		return assetCount;
	}
}
