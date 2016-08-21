package me.jprichards.elsimclient;

import java.io.IOException;

import org.json.JSONObject;

public class StupidController extends Controller
{

	public StupidController(String host, int port) throws IOException
	{
		super(host, port);
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
		int floor = event.getJSONObject("description").getInt("floor");
		
		sendCar(0, floor, "up", null, null);
	}

}
