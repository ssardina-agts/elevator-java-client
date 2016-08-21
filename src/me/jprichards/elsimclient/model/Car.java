package me.jprichards.elsimclient.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class Car
{
	private List<Floor> servicedFloors;
	private double currentHeight;
	private int numOccupants;
	private final int capacity;
	private final int id;

	public Car(JSONObject carJson, Map<Integer, Floor> floors)
	{
		servicedFloors = new ArrayList<>();
		for (Object o : carJson.getJSONArray("servicedFloors"))
		{
			servicedFloors.add(floors.get(o));
		}
		
		currentHeight = carJson.getDouble("currentHeight");
		id = carJson.getInt("id");
		numOccupants = carJson.getInt("occupants");
		capacity = carJson.getInt("capacity");
	}
	
	public double getCurrentHeight()
	{
		return currentHeight;
	}
	
	public void setCurrentHeight(double d)
	{
		currentHeight = d;
	}
	
	public int getNumOccupants()
	{
		return numOccupants;
	}
	
	public void personEntered()
	{
		numOccupants++;
	}
	
	public void personLeft()
	{
		numOccupants--;
	}
	
	public int getCapacity()
	{
		return capacity;
	}
	
	public int getId()
	{
		return id;
	}
	
	public List<Floor> getServicedFloors()
	{
		return new ArrayList<Floor>(servicedFloors);
	}
}
