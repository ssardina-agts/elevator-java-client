package me.jprichards.elsimclient.metacontroller;

import java.io.IOException;

import org.json.JSONObject;

import me.jprichards.elsimclient.Controller;
import me.jprichards.elsimclient.model.ModelRepresentation;

public class MetaController extends Controller
{
	private ModelRepresentation model;

	public MetaController(String host, int port) throws IOException
	{
		super(host, port);
	}

	@Override
	protected void onModelChange(JSONObject event) throws IOException
	{
		model = new ModelRepresentation(event.getJSONObject("description"));
	}

	@Override
	protected void onCarRequested(JSONObject event) throws IOException
	{
		// TODO Auto-generated method stub
		super.onCarRequested(event);
	}

	@Override
	protected void onCarArrived(JSONObject event) throws IOException
	{
		// TODO Auto-generated method stub
		super.onCarArrived(event);
	}

	@Override
	protected void onPersonEnteredCar(JSONObject event) throws IOException
	{
		// TODO Auto-generated method stub
		super.onPersonEnteredCar(event);
	}

	@Override
	protected void onPersonLeftCar(JSONObject event) throws IOException
	{
		// TODO Auto-generated method stub
		super.onPersonLeftCar(event);
	}

	@Override
	protected void onFloorRequest(JSONObject event) throws IOException
	{
		// TODO Auto-generated method stub
		super.onFloorRequest(event);
	}

	
}
