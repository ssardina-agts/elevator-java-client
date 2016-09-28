package me.jprichards.elsimclient.metacontroller;

import org.json.JSONObject;

import me.jprichards.elsimclient.ModelHolder;
import me.jprichards.elsimclient.ModelHolder.FloorHolder;

public class Floor
{
	private final int id;
	private final double height;

	public Floor(ModelHolder.FloorHolder fh)
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
