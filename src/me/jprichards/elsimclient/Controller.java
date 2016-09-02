package me.jprichards.elsimclient;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import me.jprichards.elsimclient.model.ModelRepresentation;

/**
 * Abstract class containing basic logic for an external controller for
 * our modified elevator simulator.
 * Contains empty methods that are called to handle all event types and upcalls
 * to performs actions. Performs initial set-up of the model and handles action responses.
 * Subclasses need only override the handler methods for the events they wish to handle.
 * @author Joshua Richards
 *
 */
public abstract class Controller
{
	private NetworkHelper connection;

	private int nextActionId = 0;
	private Map<Integer, Runnable> successCallbacks = new HashMap<>();
	private Map<Integer, Runnable> failureCallbacks = new HashMap<>();
	
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	public Controller(String host, int port) throws IOException
	{
		connection = new NetworkHelper(host, port);
	}

	/**
	 * Synchronously runs the main loop for the client side of the an elevator simulation.
	 * @throws IOException if there is a connection problem
	 */
	public void start() throws IOException
	{
		while (true)
		{
			JSONObject event = connection.receiveMessage();
			handleEvent(event);
		}
	}

	/**
	 * Calls the corresponding handler method for the given event type.
	 * If the type is unknown, an error message will be printed to stderr
	 * @param event the entire event message as sent from the server
	 */
	protected void handleEvent(JSONObject event) throws IOException
	{
		String type = event.getString("type");
		
		logger.log(Level.INFO, "Event receivied: " + event.toString(4));

		switch (type)
		{
			case "modelChanged":
				onModelChanged(event);
				break;
			case "carRequested":
				onCarRequested(event);
				break;
			case "doorOpened":
				onDoorOpened(event);
				break;
			case "doorClosed":
				onDoorClosed(event);
				break;
			case "doorSensorClear":
				onDoorSensorClear(event);
				break;
			case "carArrived":
				onCarArrived(event);
				break;
			case "personEnteredCar":
				onPersonEnteredCar(event);
				break;
			case "personLeftCar":
				onPersonLeftCar(event);
				break;
			case "floorRequested":
				onFloorRequested(event);
				break;
			case "actionProcessed":
				onActionProcessed(event);
				break;
			default:
				throw new UnsupportedOperationException("Unkown action type: " + type);
		}
	}

	/**
	 * Constructs an action message with a unique id, transmits the message,
	 * and stores the given callbacks to be called in onActionPerformed.
	 * @param type a String that the server will recognize as a valid action type
	 * @param params a JSONObject containing all the required information the server is expecting
	 * @param onSuccess Runnable to be called once the action is successfully performed
	 * @param onFailure Runnable to be called if the action is unsuccessful
	 * @throws IOException if there is a connection problem
	 */
	private void performAction(String type, JSONObject params,
			Runnable onSuccess, Runnable onFailure) throws IOException
	{
		int id = nextActionId++;
		JSONObject action = new JSONObject();
		action.put("type", type);
		action.put("id", id++);
		action.put("params", params);

		successCallbacks.put(id, onSuccess);
		failureCallbacks.put(id, onFailure);

		connection.sendMessage(action);
	}

	/**
	 * Performs the sendCar action
	 * @param carId the car to be sent
	 * @param floorId the floor to send it to
	 * @param nextDirection the direction it will travel after it arrives
	 * @param onSuccess success callback
	 * @param onFailure failure callback
	 * @throws IOException if there is a connection problem
	 */
	public void sendCar(int carId, int floorId, String nextDirection,
			Runnable onSuccess, Runnable onFailure) throws IOException
	{
		JSONObject params = new JSONObject();
		params.put("car", carId);
		params.put("floor", floorId);
		params.put("nextDirection", nextDirection);

		performAction("sendCar", params, onSuccess, onFailure);
	}

	/**
	 * Performs the changeNextDirection action
	 * @param carId the id of the car whose nextDirection is being changes
	 * @param nextDirection the car's new nextDirection
	 * @param onSuccess success callback
	 * @param onFailure failure callback
	 * @throws IOException
	 */
	public void changeNextDirection(int carId, String nextDirection,
			Runnable onSuccess, Runnable onFailure) throws IOException
	{
		JSONObject params = new JSONObject();
		params.put("car", carId);
		params.put("nextDirection", nextDirection);


		performAction("changeNextDirection", params, onSuccess, onFailure);
	}

	/**
	 * Handler method for the modelChanged event
	 * @param event the full event message
	 */
	protected void onModelChanged(JSONObject event) throws IOException
	{
		UnpackedEvent unpacked = new UnpackedEvent(event);
		ModelRepresentation newModel = new ModelRepresentation(
				unpacked.description
		);

		onModelChanged(unpacked.id, unpacked.time, newModel);
	}

	/**
	 * Handler method for the modelChanged event
	 * @param newModel representation of the current model
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 */
	protected void onModelChanged(int id, long time, ModelRepresentation newModel) throws IOException {}

	/**
	 * Handler method for the carRequested event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onCarRequested(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		String direction = ue.description.getString("direction");

		onCarRequested(ue.id, ue.time, floor, direction);
	}

	protected void onCarRequested(int id, long time, int floor, String direction) throws IOException {}

	/**
	 * Handler method for the doorOpened event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onDoorOpened(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onDoorOpened(ue.id, ue.time, floor, car);
	}

	protected void onDoorOpened(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the doorClosed event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onDoorClosed(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onDoorClosed(ue.id, ue.time, floor, car);
	}

	protected void onDoorClosed(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the doorSensorClear event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onDoorSensorClear(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onDoorSensorClear(ue.id, ue.time, floor, car);
	}

	protected void onDoorSensorClear(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the carArrived event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onCarArrived(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onCarArrived(ue.id, ue.time, floor, car);
	}

	protected void onCarArrived(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the personEnteredCar event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onPersonEnteredCar(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int car = ue.description.getInt("car");

		onPersonEnteredCar(ue.id, ue.time, car);
	}

	protected void onPersonEnteredCar(int id, long time, int car) throws IOException {}

	/**
	 * Handler method for the personLeftCar event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onPersonLeftCar(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int car = ue.description.getInt("car");

		onPersonLeftCar(ue.id, ue.time, car);
	}

	protected void onPersonLeftCar(int id, long time, int car) throws IOException {}

	/**
	 * Handler method for the floorRequested event.
	 * Should be overridden by subclasses wishing to handle this event
	 * @param event the full event message
	 */
	protected void onFloorRequested(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onFloorRequested(ue.id, ue.time, floor, car);
	}

	protected void onFloorRequested(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the floorRequest event.
	 * Removes the corresponding callbacks for the action and calls
	 * the correct one.
	 * @param event the full event message
	 */
	protected void onActionProcessed(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int actionId = ue.description.getInt("actionId");
		String status = ue.description.getString("status");
		String failureReason = ue.description.optString("failureReason");
	}

	protected void onActionProcessed(int id, long time, int actionId, String status, String failureReason)
	{
		Runnable onSuccess = successCallbacks.remove(actionId);
		Runnable onFailure = failureCallbacks.remove(actionId);
		Runnable callback = (!status.equals("failed")) ? onSuccess : onFailure;

		if (callback != null)
		{
			callback.run();
		}
	}

	private static class UnpackedEvent
	{
		public final int id;
		public final long time;
		public final JSONObject description;

		public UnpackedEvent(JSONObject event)
		{
			id = event.getInt("id");
			time = event.getLong("time");
			description = event.getJSONObject("description");
		}
	}
}
