package me.jprichards.elsimclient;

import java.io.IOException;

import org.json.JSONObject;

public class TestController extends ClientController
{

	public TestController(String host, int port) throws IOException
	{
		super(host, port);
	}

	@Override
	protected void onDoorClosed(int id, long time, int floor, int car) throws IOException
	{
		if (floor == 5)
		{
			this.sendCar(0, 10, "down", null, null);
			sleep(1000);
			this.changeDestination(0, 1, "up", null, null);
			sleep(1000);
			this.changeDestination(0, 8, "up", null, null);
		}
	}

	@Override
	protected void onModelChanged(int id, long time, ModelHolder newModel) throws IOException
	{
		this.sendCar(0, 10, "down", null, null);
		sleep(1000);
		this.changeDestination(0, 5, "up", null, null);
	}
	
	private void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException e) {}
	}

	public static void main(String[] args) throws Exception
	{
		ClientController controller = new TestController("localhost", 8081);
		controller.run();
	}
}
