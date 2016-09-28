package me.jprichards.elsimclient;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import me.jprichards.elsimclient.metacontroller.ModelRepresentation;

/**
 * Abstract class containing basic logic for an external controller for
 * our modified elevator simulator.
 * Contains empty methods that are called to handle all event types and upcalls
 * to performs actions. Performs initial set-up of the model and handles action responses.
 * Subclasses need only override the handler methods for the events they wish to handle.
 * @author Joshua Richards
 *
 */
public abstract class ClientController implements Runnable
{
	private NetworkHelper connection;

	private int nextActionId = 0;
	private Map<Integer, Runnable> successCallbacks = new HashMap<>();
	private Map<Integer, Runnable> failureCallbacks = new HashMap<>();
	private Map<Integer, JSONObject> unprocessedActions = new HashMap<>();
	
	private Set<Integer> processedEvents = new HashSet<>();
	
	private boolean ended = false;
	
	private Logger logger = Logger.getLogger(getClass().getSimpleName());

	public ClientController(String host, int port) throws IOException
	{
		connection = new NetworkHelper(host, port);
	}

	/**
	 * Synchronously runs the main loop for the client side of the an elevator simulation.
	 * @throws IOException if there is a connection problem
	 */
	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				JSONObject event = connection.receiveMessage();
				handleEvent(event);
			}
			catch (IOException e)
			{
				if (!ended)
				{
					logger.log(Level.SEVERE, "Unrecoverable network error", e);
				}
				return;
			}
		}
	}
	
	/**
	 * Call run() on another thread
	 * @return the Thread that was created and started. For convenience
	 */
	public Thread start()
	{
		Thread ret = new Thread(this);
		ret.start();
		return ret;
	}

	/**
	 * Calls the corresponding handler method for the given event type.
	 * If the type is unknown, an error message will be printed to stderr
	 * @param event the entire event message as sent from the server
	 */
	protected void handleEvent(JSONObject event) throws IOException
	{
		String type = event.getString("type");
		int id = event.getInt("id");
		
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
			case "simulationEnded":
				onSimulationEnded(id, event.getLong("time"));
				break;
			case "reconnected":
				onReconnected(event);
				break;
			default:
				throw new UnsupportedOperationException("Unkown event type: " + type);
		}
		
		sendEventResponse(id);
		processedEvents.add(id);
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
		action.put("id", id);
		action.put("params", params);

		successCallbacks.put(id, onSuccess);
		failureCallbacks.put(id, onFailure);
		unprocessedActions.put(id, action);

		connection.sendMessage(action);
	}
	
	private void sendEventResponse(int id) throws IOException
	{
		JSONObject eventProcessedMessage = new JSONObject();
		eventProcessedMessage.put("type", "eventProcessed");
		eventProcessedMessage.put("id", id);
		
		connection.sendMessage(eventProcessedMessage);
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
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onModelChanged(int, long, ModelRepresentation)
	 * @param event the full event message
	 */
	protected void onModelChanged(JSONObject event) throws IOException
	{
		UnpackedEvent unpacked = new UnpackedEvent(event);
		ModelHolder newModel = new ModelHolder(unpacked.description);

		onModelChanged(unpacked.id, unpacked.time, newModel);
	}

	/**
	 * Handler method for the modelChanged event
	 * @param newModel representation of the current model
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 */
	protected void onModelChanged(int id, long time, ModelHolder newModel) throws IOException {}

	/**
	 * Handler method for the carRequested event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onCarRequested(int, long, int, String)
	 * @param event the full event message
	 */
	protected void onCarRequested(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		String direction = ue.description.getString("direction");

		onCarRequested(ue.id, ue.time, floor, direction);
	}

	/**
	 * Handler method for the carRequested event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param floor the id of the floor at which a car has been requested
	 * @param direction the direction that was requested
	 */
	protected void onCarRequested(int id, long time, int floor, String direction) throws IOException {}

	/**
	 * Handler method for the doorOpened event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onDoorOpened(int, long, int, int)
	 * @param event the full event message
	 */
	protected void onDoorOpened(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onDoorOpened(ue.id, ue.time, floor, car);
	}

	/**
	 * Handler method for the doorOpened event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param floor the id of the floor at which the car is currently docked
	 * @param car the id of the car whose doors have opened
	 */
	protected void onDoorOpened(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the doorClosed event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onDoorClosed(int, long, int, int)
	 * @param event the full event message
	 */
	protected void onDoorClosed(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onDoorClosed(ue.id, ue.time, floor, car);
	}

	/**
	 * Handler method for the doorClosed event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param floor the id of the floor at which the car is currently docked
	 * @param car the id of the car whose doors have closed
	 */
	protected void onDoorClosed(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the doorSensorClear event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onDoorSensorClear(int, long, int, int)
	 * @param event the full event message
	 */
	protected void onDoorSensorClear(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onDoorSensorClear(ue.id, ue.time, floor, car);
	}

	/**
	 * Handler method for the doorSensorClear event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param floor the id of the floor at which the car is currently docked
	 * @param car the id of the car whose sensor is now clear
	 */
	protected void onDoorSensorClear(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the carArrived event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onCarArrived(int, long, int, int)
	 * @param event the full event message
	 */
	protected void onCarArrived(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onCarArrived(ue.id, ue.time, floor, car);
	}

	/**
	 * Handler method for the carArrived event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param floor the id of the floor at which the car is currently docked
	 * @param car the id of the car which has arrived
	 */
	protected void onCarArrived(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the personEnteredCar event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onPersonEnteredCar(int, long, int)
	 * @param event the full event message
	 */
	protected void onPersonEnteredCar(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int car = ue.description.getInt("car");

		onPersonEnteredCar(ue.id, ue.time, car);
	}

	/**
	 * Handler method for the personEnteredCar event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param car the id of the car which a person has entered
	 */
	protected void onPersonEnteredCar(int id, long time, int car) throws IOException {}

	/**
	 * Handler method for the personLeftCar event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onPersonLeftCar(int, long, int)
	 * @param event the full event message
	 */
	protected void onPersonLeftCar(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int car = ue.description.getInt("car");

		onPersonLeftCar(ue.id, ue.time, car);
	}

	/**
	 * Handler method for the personLeftCar event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param car the id of the car which a person has left
	 */
	protected void onPersonLeftCar(int id, long time, int car) throws IOException {}

	/**
	 * Handler method for the floorRequested event.
	 * Clients wishing to handle this event may override this method
	 * but it is recommended they instead override onFloorRequested(int, long, int, int)
	 * @param event the full event message
	 */
	protected void onFloorRequested(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int floor = ue.description.getInt("floor");
		int car = ue.description.getInt("car");

		onFloorRequested(ue.id, ue.time, floor, car);
	}

	/**
	 * Handler method for the personLeftCar event
	 * @param id the event id
	 * @param time the time the event was processed in simulation time
	 * @param floor the floor that was requested
	 * @param car the id of the car in which an occupant has requested a destination
	 */
	protected void onFloorRequested(int id, long time, int floor, int car) throws IOException {}

	/**
	 * Handler method for the actionProcessed event.
	 * @see onActionProcessed(int, long, int, String, String)
	 * @param event the full event message
	 */
	protected void onActionProcessed(JSONObject event) throws IOException
	{
		UnpackedEvent ue = new UnpackedEvent(event);
		int actionId = ue.description.getInt("actionId");
		String status = ue.description.getString("status");
		String failureReason = ue.description.optString("failureReason");
		onActionProcessed(ue.id, ue.time, actionId, status, failureReason);
	}

	/**
	 * Handler method for the actionProcessed event.
	 * Current implementation calls one of the Runnables given to the corresponding action methods
	 * and removes all references to them
	 * @param id the event id
	 * @param time the time the event occurred on simulation time
	 * @param actionId the id for the action that was processed
	 * @param status the status of the action. can be "failed", "completed" or "inProgress"
	 * @param failureReason a human readable error message if the status is "failed", empty String otherwise
	 */
	protected void onActionProcessed(int id, long time, int actionId, String status, String failureReason)
	{
		Runnable onSuccess = successCallbacks.remove(actionId);
		Runnable onFailure = failureCallbacks.remove(actionId);
		Runnable callback = (!status.equals("failed")) ? onSuccess : onFailure;

		if (callback != null)
		{
			callback.run();
		}
		
		unprocessedActions.remove(actionId);
	}
	
	protected void onSimulationEnded(int id, long time) throws IOException
	{
		ended = true;
		sendEventResponse(id);
		connection.close();
	}
	
	protected void onReconnected(JSONObject event) throws IOException
	{
		onReconnected(event.getInt("id"), event.getLong("time"));
		JSONArray unprocessedEvents = event.getJSONObject("description").getJSONArray("unprocessedEvents");
		
		for (int i = 0; i < unprocessedEvents.length(); i++)
		{
			JSONObject unprocessedEvent = unprocessedEvents.getJSONObject(i);
			int id = unprocessedEvent.getInt("id");
			
			if (processedEvents.contains(id))
			{
				sendEventResponse(id);
			}
			else
			{
				handleEvent(unprocessedEvent);
			}
		}
		
		JSONObject reconnectedActionParams = new JSONObject();
		JSONArray unprocessedActionsJson = new JSONArray();
		
		for (JSONObject unprocessedAction : unprocessedActions.values())
		{
			unprocessedActionsJson.put(unprocessedAction);
		}
		
		reconnectedActionParams.put("unprocessedActions",  unprocessedActionsJson);
		
		performAction("reconnected", reconnectedActionParams, null, null);
	}
	
	protected void onReconnected(int id, long time) throws IOException {}
	

	/**
	 * Convenient container for unpacking and storing the common items in event messages
	 * @author Joshua Richards
	 */
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
