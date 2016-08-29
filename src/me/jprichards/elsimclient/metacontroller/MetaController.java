package me.jprichards.elsimclient.metacontroller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import me.jprichards.elsimclient.Controller;
import me.jprichards.elsimclient.Direction;
import me.jprichards.elsimclient.model.Car;
import me.jprichards.elsimclient.model.Floor;
import me.jprichards.elsimclient.model.ModelRepresentation;

public class MetaController extends Controller
{
	private ModelRepresentation model;
	private Map<Integer, CarController> carControllers;
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	public MetaController(String host, int port) throws IOException
	{
		super(host, port);
	}

	@Override
	protected void handleEvent(JSONObject event) throws IOException
	{
		logger.log(Level.INFO, event.toString(4));
		super.handleEvent(event);
	}

	@Override
	protected void onModelChanged(JSONObject event) throws IOException
	{
		model = new ModelRepresentation(event.getJSONObject("description"));
		carControllers = new HashMap<>();
		for (Map.Entry<Integer, Car> entry : model.getCars().entrySet())
		{
			carControllers.put(entry.getKey(), new CarController(entry.getValue()));
		}
	}

	@Override
	protected void onCarRequested(JSONObject event) throws IOException
	{
		JSONObject description = event.getJSONObject("description");
		Direction direction = (description.getString("direction").equals("up")) ?
				Direction.UP : Direction.DOWN;
		Floor origin = model.getFloors().get(description.getInt("floor"));
		
		CarController cc = findBestCar(origin);
		cc.addDestination(origin);
		if (!cc.getCar().movingEh())
		{
			String nextDirection = (origin.getHeight() > cc.getCar().getCurrentHeight()) ? "up" : "down";
			sendCar(cc.getCar().getId(), origin.getId(), nextDirection, null, null);
			cc.getCar().depart(origin);
		}
	}
	
	private CarController findBestCar(Floor f)
	{
		double shortestDist = Double.MAX_VALUE;
		CarController closestCar = null;
		
		for (CarController candidate : carControllers.values())
		{
			Car car = candidate.getCar();
			double dist = f.getHeight() - car.getCurrentHeight();
			
			if (candidate.getCurrentDirection() == Direction.DOWN)
			{
				dist = 0 - dist;
			}
			
			if (dist > 0 && dist < shortestDist)
			{
				closestCar = candidate;
				shortestDist = dist;
			}
		}
		
		// this is lazy. I am rushing
		return (closestCar != null) ?
				closestCar : carControllers.values().iterator().next();
	}

	@Override
	protected void onCarArrived(JSONObject event) throws IOException
	{
		JSONObject description = event.getJSONObject("description");
		int carId = description.getInt("car");
		int floorId = description.getInt("floor");
		
		carControllers.get(carId).onArrive(model.getFloors().get(floorId));
	}

	@Override
	protected void onPersonEnteredCar(JSONObject event) throws IOException
	{
		model.getCars().get(event.getJSONObject("description").getInt("car")).personEntered();
	}

	@Override
	protected void onPersonLeftCar(JSONObject event) throws IOException
	{
		model.getCars().get(event.getJSONObject("description").getInt("car")).personLeft();
	}

	@Override
	protected void onFloorRequested(JSONObject event) throws IOException
	{
		JSONObject description = event.getJSONObject("description");
		int carId = description.getInt("car");
		int floorId = description.getInt("floor");
		
		carControllers.get(carId).addDestination(model.getFloors().get(floorId));
	}

	@Override
	protected void onDoorClosed(JSONObject event) throws IOException
	{
		int carId = event.getJSONObject("description").getInt("car");
		Floor nextFloor = carControllers.get(carId).getNextDestination();
		String nextDirection = (carControllers.get(carId).getCurrentDirection() == Direction.UP) ?
				"up" : "down";
		
		if (nextFloor != null)
		{
			sendCar(carId, nextFloor.getId(), nextDirection, null, null);
			model.getCars().get(carId).depart(nextFloor);
		}
	}

	
}
