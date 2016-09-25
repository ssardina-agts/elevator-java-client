package me.jprichards.elsimclient.metacontroller;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import me.jprichards.elsimclient.ClientController.CarHolder;
import me.jprichards.elsimclient.ClientController.FloorHolder;
import me.jprichards.elsimclient.ClientController.ModelHolder;

public class ModelRepresentation
{
	Map<Integer, Car> cars;
	Map<Integer, Floor> floors;
	
	public ModelRepresentation(ModelHolder modelHolder)
	{
		floors = new HashMap<>();
		for (FloorHolder fh : modelHolder.floors)
		{
			Floor floor = new Floor(fh);
			floors.put(floor.getId(), floor);
		}
		
		cars = new HashMap<>();
		for (CarHolder ch : modelHolder.cars)
		{
			Car car = new Car(ch, floors);
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
