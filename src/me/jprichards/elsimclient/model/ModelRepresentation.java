package me.jprichards.elsimclient.model;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class ModelRepresentation
{
	Map<Integer, Car> cars;
	Map<Integer, Floor> floors;
	
	public ModelRepresentation(JSONObject modelJson)
	{
		floors = new HashMap<>();
		for (Object o : modelJson.getJSONArray("floors"))
		{
			Floor floor = new Floor((JSONObject) o);
			floors.put(floor.getId(), floor);
		}
		
		cars = new HashMap<>();
		for (Object o : modelJson.getJSONArray("cars"))
		{
			Car car = new Car((JSONObject) o, floors);
			cars.put(car.getId(), car);
		}
	}
	
	public Map<Integer, Car> getCars()
	{
		return new HashMap<>(cars);
	}
	
	public Map<Integer, Floor> getFloors()
	{
		return new HashMap<>(floors);
	}
}
