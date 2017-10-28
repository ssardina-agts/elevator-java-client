package me.jprichards.elsimclient;

import java.io.IOException;

import org.json.JSONObject;

public class StupidController extends Controller
{

	public StupidController(String host, int port) throws IOException
	{
		super(host, port);
        reportMessage("Connected to elevator hardware...");
	}
	
	@Override
	protected void handleEvent(JSONObject event) throws IOException
	{
		System.out.println(event.toString(4));
		super.handleEvent(event);
	}
	
	@Override
	protected void onCarRequested(JSONObject event) throws IOException
	{
	    /*
	    This just ignores any request but reports it..
	     */
        int floor = event.getJSONObject("description").getInt("floor");
        String direction = event.getJSONObject("description").getString("direction");

        reportMessage("There is a new request in floor " + floor + " with direction " + direction);

        //		sendCar(0, floor, "up", null, null);
	}


	@Override
	protected void OnFloorPassed(JSONObject event)
    {
        int car= event.getJSONObject("description").getInt("car");
        int floor = event.getJSONObject("description").getInt("floor");

	    reportMessage("Elevator " + car + " has just passed floor " + floor);
        currFloor = floor;
    }

    @Override
    protected void OnModelChanged(JSONObject event) throws IOException
    {
        super.OnModelChanged(event);
        reportMessage("Number of floors: " + noFloors + " - Number of cars: " + noCars + " with capacity " + carCapacity);

        reportMessage("I will go all the way to the top...");
        sendCar(0, noFloors, "down", null, null);
    }


    @Override
    protected void onCarArrived(JSONObject event)
    {
        int floor = event.getJSONObject("description").getInt("floor");

        reportMessage("Arrived to floor " + floor);
    }

    @Override
    protected void onDoorClosed(JSONObject event) throws IOException
    {
        int car = event.getJSONObject("description").getInt("car");
        int floor = event.getJSONObject("description").getInt("floor");

        if (!simulationHasEnd) {
            // oscilate up and down forever...
            if (floor == noFloors) {
                sendCar(car, 1, "up", null, null);
            } else
                sendCar(car, noFloors, "down", null, null);
        }
    }


    @Override
    protected void OnSimulationTimeOut(JSONObject event)
    {
        super.OnSimulationTimeOut(event);
        reportMessage("SimulationTimeout received....");
        //System.exit(0);
    }


    private void reportMessage(String message)
    {
        reportMessage(message, 0);
    }

    private void reportMessage(String message, int level)
    {
        System.out.println("########### " + message);
    }
}
