package me.jprichards.elsimclient.model;

import org.json.JSONObject;

public class Floor
{
	private final int id;
	private final double height;

	public Floor(JSONObject floorJson)
	{
		id = floorJson.getInt("id");
		height = floorJson.getDouble("height");
	}
	
	public int getId()
	{
		return id;
	}
	
	public double getHeight()
	{
		return height;
	}
}
