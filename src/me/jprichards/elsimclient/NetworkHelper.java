package me.jprichards.elsimclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

/**
 * Abstracts the networking code so that Controllers need only work with JSON.
 * @author Joshua Richards
 *
 */
public class NetworkHelper
{
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public NetworkHelper(String host, int port) throws IOException
	{
		socket = new Socket(host, port);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
	}
	
	/**
	 * Synchronously listens for the next message from the server.
	 * @return the event that was received
	 * @throws IOException if there was a connection problem
	 */
	public JSONObject receiveMessage() throws IOException
	{
		synchronized (in)
		{
			String message = in.readUTF();
			return new JSONObject(message);
		}
	}
	
	/**
	 * Synchronously transmits a message to the server
	 * @param action the message to be transmitted
	 * @throws IOException if there was a connection problem
	 */
	public void sendMessage(JSONObject action) throws IOException
	{
		synchronized (out)
		{
			out.writeUTF(action.toString());
		}
	}
}
