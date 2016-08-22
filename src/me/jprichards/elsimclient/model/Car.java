package me.jprichards.elsimclient.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import me.jprichards.elsimclient.Direction;

public class Car
{
	private List<Floor> servicedFloors;
	private double currentHeight;
	private Floor currentFloor;
	private int numOccupants;
	private final int capacity;
	private final int id;
	private final double speed = (1000 * 10.0 / 4030.0);
	
	private Floor destination;
	private Floor origin;
	private long departureTime;
	private Direction direction = Direction.NONE;

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
		
		for (Floor floor : floors.values())
		{
			if (floor.getHeight() == currentHeight)
			{
				currentFloor = floor;
			}
		}
	}
	
	public void depart(Floor destination)
	{
		if (currentFloor == null || destination == currentFloor)
		{
			return;
		}

		currentHeight = -1;
		origin = currentFloor;
		currentFloor = null;
		this.destination = destination;
		departureTime = System.currentTimeMillis();
		direction = (destination.getHeight() > origin.getHeight()) ?
				Direction.UP : Direction.DOWN;
	}
	
	public void arrive()
	{
		if (destination == null)
		{
			return;
		}
		
		currentFloor = destination;
		destination = null;
		origin = null;
		currentHeight = currentFloor.getHeight();
		departureTime = -1;
		direction = Direction.NONE;
	}
	
	/**
	 * returns the height of the current floor if the car is not moving.
	 * returns an estimation of the car's current height if the it's moving.
	 * @return
	 */
	public double getCurrentHeight()
	{
		if (currentHeight != -1)
		{
			return currentHeight;
		}
		
		long secondsSinceDeparture = (System.currentTimeMillis() - departureTime) / 1000;
		double distanceTraveled = speed * secondsSinceDeparture;
		
		if (direction == Direction.DOWN)
		{
			distanceTraveled = 0 - distanceTraveled;
		}
		
		return origin.getHeight() + distanceTraveled;
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
