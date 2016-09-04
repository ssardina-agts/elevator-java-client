package me.jprichards.elsimclient.metacontroller;

import org.json.JSONObject;

import me.jprichards.elsimclient.Controller.FloorHolder;

public class Floor
{
	private final int id;
	private final double height;

	public Floor(FloorHolder fh)
	{
		id = fh.id;
		height = fh.height;
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
