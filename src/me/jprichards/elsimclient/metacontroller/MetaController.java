package me.jprichards.elsimclient.metacontroller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import me.jprichards.elsimclient.Controller;
import me.jprichards.elsimclient.Direction;
import me.jprichards.elsimclient.ModelHolder;

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
	protected void onModelChanged(int id, long time, ModelHolder newModel) throws IOException
	{
		if (model != null)
		{
			//ignore incremental updates to model. we'll track state with events
			return;
		}

		model = new ModelRepresentation(newModel);
		carControllers = new HashMap<>();
		for (Map.Entry<Integer, Car> entry : model.getCars().entrySet())
		{
			carControllers.put(entry.getKey(), new CarController(entry.getValue()));
		}
	}

	@Override
	protected void onCarRequested(int id, long time, int floor, String directionStr) throws IOException
	{
		Direction direction = (directionStr.equals("up")) ?
				Direction.UP : Direction.DOWN;
		Floor origin = model.getFloors().get(floor);

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
		// 10 days in. no one suspects a thing...
		return (closestCar != null) ?
				closestCar : carControllers.values().iterator().next();
	}

	@Override
	protected void onCarArrived(int id, long time, int floor, int car) throws IOException
	{
		carControllers.get(car).onArrive(model.getFloors().get(floor));
	}

	@Override
	protected void onPersonEnteredCar(int id, long time, int car) throws IOException
	{
		model.getCars().get(car).personEntered();
	}

	@Override
	protected void onPersonLeftCar(int id, long time, int car) throws IOException
	{
		model.getCars().get(car).personLeft();
	}

	@Override
	protected void onFloorRequested(int id, long time, int floor, int car) throws IOException
	{
		carControllers.get(car).addDestination(model.getFloors().get(floor));
	}

	@Override
	protected void onDoorClosed(int id, long time, int floor, int car) throws IOException
	{
		Floor nextFloor = carControllers.get(car).getNextDestination();
		String nextDirection = (carControllers.get(car).getCurrentDirection() == Direction.UP) ?
				"up" : "down";

		if (nextFloor != null)
		{
			sendCar(car, nextFloor.getId(), nextDirection, null, null);
			model.getCars().get(car).depart(nextFloor);
		}
	}


}
